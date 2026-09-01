package org.acme.foodpackaging.service;

import static org.acme.foodpackaging.support.TestHelper.PRODUCT_1;
import static org.acme.foodpackaging.support.TestHelper.PRODUCT_2;
import static org.acme.foodpackaging.support.TestHelper.createProblem;
import static org.acme.foodpackaging.support.TestHelper.input;
import static org.acme.foodpackaging.support.TestHelper.inputWithJobs;
import static org.acme.foodpackaging.support.TestHelper.inputWithLines;
import static org.acme.foodpackaging.support.TestHelper.inputWithOperators;
import static org.acme.foodpackaging.support.TestHelper.inputWithProducts;
import static org.acme.foodpackaging.support.TestHelper.job;
import static org.acme.foodpackaging.support.TestHelper.line;
import static org.acme.foodpackaging.support.TestHelper.operator;
import static org.acme.foodpackaging.support.TestHelper.product;
import static org.acme.foodpackaging.support.TestHelper.scheduledLine;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.List;

import ai.timefold.solver.service.definition.api.domain.ModelConfig;
import ai.timefold.solver.service.definition.api.validation.Issue;
import ai.timefold.solver.service.definition.api.validation.ValidationBuilder;
import ai.timefold.solver.service.definition.api.validation.ValidationStatus;
import ai.timefold.solver.service.definition.api.validation.dto.ValidationResult;

import org.acme.foodpackaging.demo.DemoDataBuilder;
import org.acme.foodpackaging.dto.input.PackagingScheduleInput;
import org.acme.foodpackaging.service.validation.PackagingScheduleIssue.DuplicateJobIdIssue;
import org.acme.foodpackaging.service.validation.PackagingScheduleIssue.DuplicateLineIdIssue;
import org.acme.foodpackaging.service.validation.PackagingScheduleIssue.DuplicateOperatorIdIssue;
import org.acme.foodpackaging.service.validation.PackagingScheduleIssue.DuplicateProductIdIssue;
import org.acme.foodpackaging.service.validation.PackagingScheduleIssue.JobOnMultipleLinesIssue;
import org.acme.foodpackaging.service.validation.PackagingScheduleIssue.MissingCleaningDurationIssue;
import org.acme.foodpackaging.service.validation.PackagingScheduleIssue.NonExistingJobReferenceIssue;
import org.acme.foodpackaging.service.validation.PackagingScheduleIssue.NonExistingOperatorReferenceIssue;
import org.acme.foodpackaging.service.validation.PackagingScheduleIssue.NonExistingProductReferenceIssue;
import org.junit.jupiter.api.Test;

// OpenAPI spec compliance (Bean Validation) is enforced by the Service module at the REST layer, so it's
// covered by org.acme.foodpackaging.rest.PackagingScheduleOpenApiValidationTest instead.
// This class only covers the domain-specific checks PackagingScheduleValidator implements itself.
class PackagingScheduleValidatorTest {

    private final PackagingScheduleValidator validator = new PackagingScheduleValidator();

    @Test
    void validInputHasNoIssues() {
        ValidationResult<Issue> result = validate(createProblem());

        assertThat(result.issues()).isEmpty();
        assertThat(result.status()).isEqualTo(ValidationStatus.OK);
        assertThat(result.isValid()).isTrue();
    }

    // ------------------------------------------------------------------------
    // Products
    // ------------------------------------------------------------------------

    @Test
    void duplicateProductIdIsReportedWithTheOffendingId() {
        ValidationResult<Issue> result = validate(inputWithProducts(product(PRODUCT_1), product(PRODUCT_1)));

        DuplicateProductIdIssue issue = singleIssue(result, DuplicateProductIdIssue.class);
        assertThat(issue.getProductId()).isEqualTo(PRODUCT_1);
    }

    @Test
    void cleaningDurationReferencingNonExistingProductIsReported() {
        ValidationResult<Issue> result =
                validate(inputWithProducts(product(PRODUCT_1, List.of(PRODUCT_1, "does-not-exist"))));

        NonExistingProductReferenceIssue issue = singleIssue(result, NonExistingProductReferenceIssue.class);
        assertThat(issue.getProductId()).isEqualTo("does-not-exist");
    }

    @Test
    void productMissingTheCleaningDurationOfAnotherProductIsReported() {
        // p2 can be produced right before p1 on the same line, so p1 needs a cleaning duration for it.
        ValidationResult<Issue> result = validate(inputWithProducts(
                product(PRODUCT_1, List.of(PRODUCT_1)),
                product(PRODUCT_2, List.of(PRODUCT_1, PRODUCT_2))));

        MissingCleaningDurationIssue issue = singleIssue(result, MissingCleaningDurationIssue.class);
        assertThat(issue.getProductId()).isEqualTo(PRODUCT_1);
        assertThat(issue.getPreviousProductId()).isEqualTo(PRODUCT_2);
    }

    @Test
    void productMissingItsOwnCleaningDurationIsReported() {
        // A line can produce the same product twice in a row, so a product needs its own cleaning duration too.
        ValidationResult<Issue> result = validate(inputWithProducts(product(PRODUCT_1, List.of())));

        MissingCleaningDurationIssue issue = singleIssue(result, MissingCleaningDurationIssue.class);
        assertThat(issue.getProductId()).isEqualTo(PRODUCT_1);
        assertThat(issue.getPreviousProductId()).isEqualTo(PRODUCT_1);
    }

    @Test
    void onlyTheFirstMissingCleaningDurationOfAProductIsReported() {
        // A dataset that forgot the matrix entirely would otherwise report one issue per product pair.
        ValidationResult<Issue> result = validate(inputWithProducts(
                product(PRODUCT_1, List.of()),
                product(PRODUCT_2, List.of())));

        assertThat(codesOf(result)).containsExactly("MISSING_CLEANING_DURATION", "MISSING_CLEANING_DURATION");
    }

    // ------------------------------------------------------------------------
    // Operators
    // ------------------------------------------------------------------------

    @Test
    void duplicateOperatorIdIsReportedWithTheOffendingId() {
        ValidationResult<Issue> result = validate(inputWithOperators(operator("o1"), operator("o1")));

        DuplicateOperatorIdIssue issue = singleIssue(result, DuplicateOperatorIdIssue.class);
        assertThat(issue.getOperatorId()).isEqualTo("o1");
    }

    // ------------------------------------------------------------------------
    // Jobs
    // ------------------------------------------------------------------------

    @Test
    void duplicateJobIdIsReportedWithTheOffendingId() {
        ValidationResult<Issue> result = validate(inputWithJobs(job("j1", PRODUCT_1), job("j1", PRODUCT_2)));

        DuplicateJobIdIssue issue = singleIssue(result, DuplicateJobIdIssue.class);
        assertThat(issue.getJobId()).isEqualTo("j1");
    }

    @Test
    void jobReferencingNonExistingProductIsReported() {
        ValidationResult<Issue> result = validate(inputWithJobs(job("j1", "does-not-exist")));

        NonExistingProductReferenceIssue issue = singleIssue(result, NonExistingProductReferenceIssue.class);
        assertThat(issue.getProductId()).isEqualTo("does-not-exist");
    }

    // ------------------------------------------------------------------------
    // Lines
    // ------------------------------------------------------------------------

    @Test
    void duplicateLineIdIsReportedWithTheOffendingId() {
        ValidationResult<Issue> result = validate(inputWithLines(line("l1"), line("l1")));

        DuplicateLineIdIssue issue = singleIssue(result, DuplicateLineIdIssue.class);
        assertThat(issue.getLineId()).isEqualTo("l1");
    }

    @Test
    void lineReferencingNonExistingOperatorIsReported() {
        ValidationResult<Issue> result = validate(inputWithLines(scheduledLine("l1", "does-not-exist")));

        NonExistingOperatorReferenceIssue issue = singleIssue(result, NonExistingOperatorReferenceIssue.class);
        assertThat(issue.getLineId()).isEqualTo("l1");
    }

    @Test
    void lineReferencingNonExistingJobIsReported() {
        ValidationResult<Issue> result = validate(inputWithLines(scheduledLine("l1", "o1", "does-not-exist")));

        NonExistingJobReferenceIssue issue = singleIssue(result, NonExistingJobReferenceIssue.class);
        assertThat(issue.getLineId()).isEqualTo("l1");
    }

    @Test
    void lineReferencingExistingOperatorAndJobsIsAccepted() {
        ValidationResult<Issue> result = validate(inputWithLines(scheduledLine("l1", "o1", "j1", "j2")));

        assertThat(result.issues()).isEmpty();
    }

    @Test
    void unscheduledLineIsAccepted() {
        ValidationResult<Issue> result = validate(inputWithLines(line("l1")));

        assertThat(result.issues()).isEmpty();
    }

    @Test
    void jobScheduledOnTwoLinesIsReportedWithTheOffendingJobId() {
        ValidationResult<Issue> result = validate(inputWithLines(
                scheduledLine("l1", "o1", "j1"),
                scheduledLine("l2", "o2", "j1")));

        JobOnMultipleLinesIssue issue = singleIssue(result, JobOnMultipleLinesIssue.class);
        assertThat(issue.getJobId()).isEqualTo("j1");
    }

    // ------------------------------------------------------------------------
    // Accumulation across entity types
    // ------------------------------------------------------------------------

    @Test
    void issuesOfDifferentKindsAreAllReported() {
        PackagingScheduleInput input = input(
                List.of(product(PRODUCT_1, List.of(PRODUCT_1, PRODUCT_2)),
                        product(PRODUCT_1, List.of(PRODUCT_1, PRODUCT_2)),
                        product(PRODUCT_2, List.of(PRODUCT_1, PRODUCT_2))),
                List.of(operator("o1"), operator("o1")),
                List.of(line("l1"), scheduledLine("l1", "unknown-operator", "unknown-job")),
                List.of(job("j1", PRODUCT_1), job("j1", PRODUCT_1)));

        ValidationResult<Issue> result = validate(input);

        assertThat(codesOf(result)).containsExactlyInAnyOrder(
                "DUPLICATE_PRODUCT_ID",
                "DUPLICATE_OPERATOR_ID",
                "DUPLICATE_LINE_ID",
                "DUPLICATE_JOB_ID",
                "NON_EXISTING_OPERATOR_REFERENCE",
                "NON_EXISTING_JOB_REFERENCE");
        assertThat(result.status()).isEqualTo(ValidationStatus.ERRORS);
        assertThat(result.isValid()).isFalse();
    }

    @Test
    void theDemoDatasetIsValid() {
        // The demo dataset would otherwise be rejected by the very service that offers it.
        ValidationResult<Issue> result = validate(DemoDataBuilder.builder().build());

        assertThat(result.issues()).isEmpty();
    }

    // ------------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------------

    private ValidationResult<Issue> validate(PackagingScheduleInput input) {
        ValidationBuilder validationBuilder = new ValidationBuilder();
        validator.validate(validationBuilder, input, ModelConfig.empty());
        return validationBuilder.build();
    }

    private static <T extends Issue> T singleIssue(ValidationResult<Issue> result, Class<T> expectedType) {
        Collection<Issue> issues = result.issues();
        assertThat(issues).hasSize(1);
        Issue issue = issues.iterator().next();
        assertThat(issue).isInstanceOf(expectedType);
        return expectedType.cast(issue);
    }

    private static List<String> codesOf(ValidationResult<Issue> result) {
        return result.issues().stream().map(issue -> issue.getCode().value()).toList();
    }
}
