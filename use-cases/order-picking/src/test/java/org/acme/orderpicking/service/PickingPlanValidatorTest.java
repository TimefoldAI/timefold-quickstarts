package org.acme.orderpicking.service;

import static org.acme.orderpicking.support.TestHelper.createProblem;
import static org.acme.orderpicking.support.TestHelper.input;
import static org.acme.orderpicking.support.TestHelper.locationDTO;
import static org.acme.orderpicking.support.TestHelper.pickTaskDTO;
import static org.acme.orderpicking.support.TestHelper.productDTO;
import static org.acme.orderpicking.support.TestHelper.trolleyDTO;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.List;

import ai.timefold.solver.service.definition.api.domain.ModelConfig;
import ai.timefold.solver.service.definition.api.validation.Issue;
import ai.timefold.solver.service.definition.api.validation.ValidationBuilder;
import ai.timefold.solver.service.definition.api.validation.dto.ValidationResult;

import org.acme.orderpicking.demo.DemoDataBuilder;
import org.acme.orderpicking.domain.ShelvingSide;
import org.acme.orderpicking.dto.input.PickTaskInputDTO;
import org.acme.orderpicking.dto.input.PickingPlanInput;
import org.acme.orderpicking.dto.input.ProductInputDTO;
import org.acme.orderpicking.dto.input.TrolleyInputDTO;
import org.acme.orderpicking.service.validation.PickingPlanIssue.DuplicatePickTaskIdIssue;
import org.acme.orderpicking.service.validation.PickingPlanIssue.DuplicateProductIdIssue;
import org.acme.orderpicking.service.validation.PickingPlanIssue.DuplicateTrolleyIdIssue;
import org.acme.orderpicking.service.validation.PickingPlanIssue.NonExistingPickTaskReferenceIssue;
import org.acme.orderpicking.service.validation.PickingPlanIssue.NonExistingProductReferenceIssue;
import org.acme.orderpicking.service.validation.PickingPlanIssue.PickTaskAssignedMoreThanOnceIssue;
import org.acme.orderpicking.service.validation.PickingPlanIssue.ProductExceedsBucketCapacityIssue;
import org.acme.orderpicking.service.validation.PickingPlanIssue.UnknownShelvingIssue;
import org.junit.jupiter.api.Test;

// OpenAPI spec compliance is enforced by the Service module at the REST layer, so it's covered by
// org.acme.orderpicking.rest.PickingPlanOpenApiValidationTest instead. This class only covers the
// domain-specific checks PickingPlanValidator implements itself.
class PickingPlanValidatorTest {

    private static final ProductInputDTO PRODUCT = productDTO("p1");
    private static final List<ProductInputDTO> PRODUCTS = List.of(PRODUCT, productDTO("p2"));
    private static final PickTaskInputDTO PICK_TASK = pickTaskDTO("1-0", "1", "p1");
    private static final List<PickTaskInputDTO> PICK_TASKS = List.of(PICK_TASK, pickTaskDTO("1-1", "1", "p2"));
    private static final TrolleyInputDTO TROLLEY = trolleyDTO("t1");
    private static final List<TrolleyInputDTO> TROLLEYS = List.of(TROLLEY, trolleyDTO("t2"));

    private final PickingPlanValidator validator = new PickingPlanValidator();

    @Test
    void validInputHasNoIssues() {
        ValidationResult<Issue> result = validate(createProblem());
        assertThat(result.issues()).isEmpty();
    }

    @Test
    void assignedInputHasNoIssues() {
        PickingPlanInput input = input(PRODUCTS, PICK_TASKS, List.of(trolleyDTO("t1", "1-1"), trolleyDTO("t2", "1-0")));
        assertThat(validate(input).issues()).isEmpty();
    }

    // A service that ships demo data its own validator rejects is broken out of the box.
    @Test
    void demoDataHasNoIssues() {
        ValidationResult<Issue> result = validate(DemoDataBuilder.builder().build());
        assertThat(result.issues()).isEmpty();
    }

    @Test
    void duplicateProductId() {
        PickingPlanInput input = input(List.of(PRODUCT, PRODUCT), List.of(PICK_TASK), TROLLEYS);
        assertSingleIssue(validate(input), DuplicateProductIdIssue.class);
    }

    @Test
    void duplicatePickTaskId() {
        PickingPlanInput input = input(PRODUCTS, List.of(PICK_TASK, PICK_TASK), TROLLEYS);
        assertSingleIssue(validate(input), DuplicatePickTaskIdIssue.class);
    }

    @Test
    void duplicateTrolleyId() {
        PickingPlanInput input = input(PRODUCTS, PICK_TASKS, List.of(TROLLEY, TROLLEY));
        assertSingleIssue(validate(input), DuplicateTrolleyIdIssue.class);
    }

    @Test
    void nonExistingProductReference() {
        PickingPlanInput input = input(PRODUCTS, List.of(pickTaskDTO("1-0", "1", "does-not-exist")), TROLLEYS);

        ValidationResult<Issue> result = validate(input);
        assertSingleIssue(result, NonExistingProductReferenceIssue.class);
        assertThat(((NonExistingProductReferenceIssue) result.issues().iterator().next()).getPickTaskId())
                .isEqualTo("1-0");
    }

    @Test
    void nonExistingPickTaskReferenceIsReportedOncePerTrolley() {
        PickingPlanInput input =
                input(PRODUCTS, PICK_TASKS, List.of(trolleyDTO("t1", "1-0", "does-not-exist", "also-not")));

        ValidationResult<Issue> result = validate(input);
        assertSingleIssue(result, NonExistingPickTaskReferenceIssue.class);
        NonExistingPickTaskReferenceIssue issue = (NonExistingPickTaskReferenceIssue) result.issues().iterator().next();
        assertThat(issue.getTrolleyId()).isEqualTo("t1");
        assertThat(issue.getPickTaskId()).isEqualTo("does-not-exist");
    }

    @Test
    void pickTaskAssignedToTwoTrolleys() {
        PickingPlanInput input = input(PRODUCTS, PICK_TASKS, List.of(trolleyDTO("t1", "1-0"), trolleyDTO("t2", "1-0")));
        assertSingleIssue(validate(input), PickTaskAssignedMoreThanOnceIssue.class);
    }

    @Test
    void pickTaskAssignedTwiceToTheSameTrolley() {
        PickingPlanInput input = input(PRODUCTS, PICK_TASKS, List.of(trolleyDTO("t1", "1-0", "1-1", "1-0")));
        assertSingleIssue(validate(input), PickTaskAssignedMoreThanOnceIssue.class);
    }

    @Test
    void unknownShelvingIsReportedOnce() {
        List<ProductInputDTO> products = List.of(
                productDTO("p1", 100, locationDTO("F1", ShelvingSide.LEFT, 1)),
                productDTO("p2", 100, locationDTO("F1", ShelvingSide.RIGHT, 2)));
        List<TrolleyInputDTO> trolleys = List.of(trolleyDTO("t1", 1000, locationDTO("F1", ShelvingSide.LEFT, 0)));

        ValidationResult<Issue> result = validate(input(products, PICK_TASKS, trolleys));
        assertSingleIssue(result, UnknownShelvingIssue.class);
        assertThat(((UnknownShelvingIssue) result.issues().iterator().next()).getShelvingId()).isEqualTo("F1");
    }

    @Test
    void productExceedsBucketCapacity() {
        List<ProductInputDTO> products = List.of(PRODUCT, productDTO("p2", 2000, locationDTO("A2", ShelvingSide.LEFT, 1)));
        List<TrolleyInputDTO> trolleys = List.of(
                trolleyDTO("t1", 1000, locationDTO("A1", ShelvingSide.LEFT, 0)),
                trolleyDTO("t2", 1500, locationDTO("A1", ShelvingSide.LEFT, 0)));

        ValidationResult<Issue> result = validate(input(products, PICK_TASKS, trolleys));
        assertSingleIssue(result, ProductExceedsBucketCapacityIssue.class);
        ProductExceedsBucketCapacityIssue issue = (ProductExceedsBucketCapacityIssue) result.issues().iterator().next();
        assertThat(issue.getProductId()).isEqualTo("p2");
        assertThat(issue.getVolume()).isEqualTo(2000);
        assertThat(issue.getBucketCapacity()).isEqualTo(1500);
    }

    @Test
    void mixedDatasetReportsEveryIssue() {
        ProductInputDTO tooBig = productDTO("p2", 5000, locationDTO("Z9", ShelvingSide.LEFT, 1));
        PickTaskInputDTO dangling = pickTaskDTO("1-1", "1", "does-not-exist");
        PickingPlanInput input = input(List.of(PRODUCT, PRODUCT, tooBig), List.of(PICK_TASK, PICK_TASK, dangling),
                List.of(trolleyDTO("t1", "1-0", "does-not-exist"), trolleyDTO("t1", "1-0")));

        ValidationResult<Issue> result = validate(input);
        assertThat(result.issues()).hasAtLeastOneElementOfType(DuplicateProductIdIssue.class)
                .hasAtLeastOneElementOfType(DuplicatePickTaskIdIssue.class)
                .hasAtLeastOneElementOfType(DuplicateTrolleyIdIssue.class)
                .hasAtLeastOneElementOfType(NonExistingProductReferenceIssue.class)
                .hasAtLeastOneElementOfType(NonExistingPickTaskReferenceIssue.class)
                .hasAtLeastOneElementOfType(PickTaskAssignedMoreThanOnceIssue.class)
                .hasAtLeastOneElementOfType(UnknownShelvingIssue.class)
                .hasAtLeastOneElementOfType(ProductExceedsBucketCapacityIssue.class);
    }

    private ValidationResult<Issue> validate(PickingPlanInput input) {
        ValidationBuilder validationBuilder = new ValidationBuilder();
        validator.validate(validationBuilder, input, ModelConfig.empty());
        return validationBuilder.build();
    }

    private static <T extends Issue> void assertSingleIssue(ValidationResult<Issue> result, Class<T> expectedType) {
        Collection<Issue> issues = result.issues();
        assertThat(issues).hasSize(1);
        Issue issue = issues.iterator().next();
        assertThat(issue).isInstanceOf(expectedType);
    }
}
