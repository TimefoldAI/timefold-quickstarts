package org.acme.facilitylocation.service;

import static org.acme.facilitylocation.support.TestHelper.aConsumerDTO;
import static org.acme.facilitylocation.support.TestHelper.aFacilityDTO;
import static org.acme.facilitylocation.support.TestHelper.createProblem;
import static org.acme.facilitylocation.support.TestHelper.input;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.List;

import ai.timefold.solver.service.definition.api.domain.ModelConfig;
import ai.timefold.solver.service.definition.api.validation.Issue;
import ai.timefold.solver.service.definition.api.validation.ValidationBuilder;
import ai.timefold.solver.service.definition.api.validation.dto.ValidationResult;

import org.acme.facilitylocation.demo.DemoDataBuilder;
import org.acme.facilitylocation.dto.input.ConsumerInputDTO;
import org.acme.facilitylocation.dto.input.FacilityInputDTO;
import org.acme.facilitylocation.dto.input.FacilityPlanInput;
import org.acme.facilitylocation.service.validation.FacilityPlanIssue.DuplicateConsumerIdIssue;
import org.acme.facilitylocation.service.validation.FacilityPlanIssue.DuplicateFacilityIdIssue;
import org.acme.facilitylocation.service.validation.FacilityPlanIssue.InsufficientTotalCapacityIssue;
import org.acme.facilitylocation.service.validation.FacilityPlanIssue.NonExistingFacilityReferenceIssue;
import org.junit.jupiter.api.Test;

// OpenAPI spec compliance is enforced by the Service module at the REST layer, so it's covered by
// org.acme.facilitylocation.rest.FacilityPlanOpenApiValidationTest instead. This class only covers the
// domain-specific checks FacilityPlanValidator implements itself.
class FacilityPlanValidatorTest {

    private static final FacilityInputDTO FACILITY = aFacilityDTO("f1").build();
    private static final List<ConsumerInputDTO> VALID_CONSUMERS = List.of(aConsumerDTO("c1").build());

    private final FacilityPlanValidator validator = new FacilityPlanValidator();

    @Test
    void validInputHasNoIssues() {
        ValidationResult<Issue> result = validate(createProblem());
        assertThat(result.issues()).isEmpty();
    }

    // A service that ships demo data its own validator rejects is broken out of the box.
    @Test
    void demoDataHasNoIssues() {
        ValidationResult<Issue> result = validate(DemoDataBuilder.builder().build());
        assertThat(result.issues()).isEmpty();
    }

    @Test
    void duplicateFacilityId() {
        FacilityPlanInput input = input(List.of(FACILITY, FACILITY), VALID_CONSUMERS);
        assertSingleIssue(validate(input), DuplicateFacilityIdIssue.class);
    }

    @Test
    void duplicateConsumerId() {
        ConsumerInputDTO consumer = aConsumerDTO("c1").build();
        FacilityPlanInput input = input(List.of(FACILITY), List.of(consumer, consumer));
        assertSingleIssue(validate(input), DuplicateConsumerIdIssue.class);
    }

    @Test
    void nonExistingFacilityReference() {
        ConsumerInputDTO consumer = aConsumerDTO("c1").facilityId("does-not-exist").build();
        FacilityPlanInput input = input(List.of(FACILITY), List.of(consumer));
        assertSingleIssue(validate(input), NonExistingFacilityReferenceIssue.class);
    }

    @Test
    void insufficientTotalCapacity() {
        FacilityInputDTO facility = aFacilityDTO("f1").capacity(10).build();
        ConsumerInputDTO consumer = aConsumerDTO("c1").demand(11).build();
        FacilityPlanInput input = input(List.of(facility), List.of(consumer));

        ValidationResult<Issue> result = validate(input);
        assertSingleIssue(result, InsufficientTotalCapacityIssue.class);
        InsufficientTotalCapacityIssue issue = (InsufficientTotalCapacityIssue) result.issues().iterator().next();
        assertThat(issue.getTotalCapacity()).isEqualTo(10);
        assertThat(issue.getTotalDemand()).isEqualTo(11);
    }

    @Test
    void mixedDatasetReportsEveryIssue() {
        FacilityInputDTO facility = aFacilityDTO("f1").capacity(10).build();
        ConsumerInputDTO duplicated = aConsumerDTO("c1").demand(11).build();
        ConsumerInputDTO dangling = aConsumerDTO("c2").facilityId("does-not-exist").build();
        FacilityPlanInput input = input(List.of(facility, facility), List.of(duplicated, duplicated, dangling));

        ValidationResult<Issue> result = validate(input);
        assertThat(result.issues()).hasAtLeastOneElementOfType(DuplicateFacilityIdIssue.class)
                .hasAtLeastOneElementOfType(DuplicateConsumerIdIssue.class)
                .hasAtLeastOneElementOfType(NonExistingFacilityReferenceIssue.class)
                .hasAtLeastOneElementOfType(InsufficientTotalCapacityIssue.class);
    }

    private ValidationResult<Issue> validate(FacilityPlanInput input) {
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
