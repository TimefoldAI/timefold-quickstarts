package org.acme.vehiclerouting.service;

import static org.acme.vehiclerouting.support.TestHelper.aVehicleDTO;
import static org.acme.vehiclerouting.support.TestHelper.aVisitDTO;
import static org.acme.vehiclerouting.support.TestHelper.at;
import static org.acme.vehiclerouting.support.TestHelper.input;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.List;

import ai.timefold.solver.service.definition.api.domain.ModelConfig;
import ai.timefold.solver.service.definition.api.validation.Issue;
import ai.timefold.solver.service.definition.api.validation.ValidationBuilder;
import ai.timefold.solver.service.definition.api.validation.dto.ValidationResult;

import org.acme.vehiclerouting.demo.DemoDataBuilder;
import org.acme.vehiclerouting.dto.input.VehicleInputDTO;
import org.acme.vehiclerouting.dto.input.VehicleRoutePlanInput;
import org.acme.vehiclerouting.dto.input.VisitInputDTO;
import org.acme.vehiclerouting.service.validation.VehicleRoutePlanIssue.DuplicateVehicleIdIssue;
import org.acme.vehiclerouting.service.validation.VehicleRoutePlanIssue.DuplicateVisitIdIssue;
import org.acme.vehiclerouting.service.validation.VehicleRoutePlanIssue.NonExistingVisitReferenceIssue;
import org.acme.vehiclerouting.service.validation.VehicleRoutePlanIssue.VisitAssignedMoreThanOnceIssue;
import org.acme.vehiclerouting.service.validation.VehicleRoutePlanIssue.VisitWindowTooShortIssue;
import org.junit.jupiter.api.Test;

// OpenAPI spec compliance (Bean Validation) is enforced by the Service module at the REST layer, so it's
// covered by org.acme.vehiclerouting.rest.VehicleRoutePlanOpenApiValidationTest instead. This class only
// covers the domain-specific checks VehicleRoutePlanValidator implements itself.
class VehicleRoutePlanValidatorTest {

    private static final List<VehicleInputDTO> VEHICLES = List.of(aVehicleDTO("1").build(), aVehicleDTO("2").build());
    private static final List<VisitInputDTO> VALID_VISITS = List.of(aVisitDTO("1").build());

    private final VehicleRoutePlanValidator validator = new VehicleRoutePlanValidator();

    @Test
    void validInputHasNoIssues() {
        VehicleRoutePlanInput routePlan = input(
                List.of(aVehicleDTO("1").visitIds(List.of("2", "3")).build(), aVehicleDTO("2").build()),
                List.of(aVisitDTO("1").build(), aVisitDTO("2").build(), aVisitDTO("3").build()));

        assertThat(validate(routePlan).issues()).isEmpty();
    }

    @Test
    void demoDatasetsHaveNoIssues() {
        // Otherwise the service would ship demo data that its own validator rejects.
        assertThat(validate(DemoDataBuilder.philadelphia()).issues()).isEmpty();
        assertThat(validate(DemoDataBuilder.ghent()).issues()).isEmpty();
        assertThat(validate(DemoDataBuilder.hartfort()).issues()).isEmpty();
        assertThat(validate(DemoDataBuilder.firenze()).issues()).isEmpty();
    }

    @Test
    void duplicateVehicleId() {
        VehicleInputDTO vehicle = aVehicleDTO("1").build();
        assertSingleIssue(validate(input(List.of(vehicle, vehicle), VALID_VISITS)), DuplicateVehicleIdIssue.class);
    }

    @Test
    void duplicateVisitId() {
        VisitInputDTO visit = aVisitDTO("1").build();
        assertSingleIssue(validate(input(VEHICLES, List.of(visit, visit))), DuplicateVisitIdIssue.class);
    }

    @Test
    void nonExistingVisitReference() {
        VehicleInputDTO vehicle = aVehicleDTO("1").visitIds(List.of("does-not-exist")).build();
        assertSingleIssue(validate(input(List.of(vehicle), VALID_VISITS)), NonExistingVisitReferenceIssue.class);
    }

    @Test
    void onlyTheFirstUnknownVisitReferencePerVehicleIsReported() {
        VehicleInputDTO vehicle = aVehicleDTO("1").visitIds(List.of("nope", "also-nope", "1")).build();
        assertSingleIssue(validate(input(List.of(vehicle), VALID_VISITS)), NonExistingVisitReferenceIssue.class);
    }

    @Test
    void visitAssignedToTwoVehicles() {
        VehicleRoutePlanInput routePlan = input(
                List.of(aVehicleDTO("1").visitIds(List.of("1")).build(),
                        aVehicleDTO("2").visitIds(List.of("1")).build()),
                VALID_VISITS);
        assertSingleIssue(validate(routePlan), VisitAssignedMoreThanOnceIssue.class);
    }

    @Test
    void visitAssignedTwiceToTheSameVehicle() {
        VehicleRoutePlanInput routePlan = input(
                List.of(aVehicleDTO("1").visitIds(List.of("1", "1")).build()), VALID_VISITS);
        assertSingleIssue(validate(routePlan), VisitAssignedMoreThanOnceIssue.class);
    }

    @Test
    void visitWindowTooShort() {
        // Forty minutes of servicing to be done within half an hour.
        VisitInputDTO visit = aVisitDTO("1")
                .minStartTime(at(8, 0))
                .maxEndTime(at(8, 30))
                .serviceDurationMinutes(40)
                .build();
        assertSingleIssue(validate(input(VEHICLES, List.of(visit))), VisitWindowTooShortIssue.class);
    }

    @Test
    void mixedDatasetReportsEveryIssue() {
        VehicleInputDTO duplicatedVehicle = aVehicleDTO("1").build();
        VisitInputDTO duplicatedVisit = aVisitDTO("1").build();
        VehicleRoutePlanInput routePlan = input(
                List.of(duplicatedVehicle, duplicatedVehicle,
                        aVehicleDTO("2").visitIds(List.of("does-not-exist")).build(),
                        aVehicleDTO("3").visitIds(List.of("2")).build(),
                        aVehicleDTO("4").visitIds(List.of("2")).build()),
                List.of(duplicatedVisit, duplicatedVisit, aVisitDTO("2").build(),
                        aVisitDTO("3").minStartTime(at(8, 0)).maxEndTime(at(8, 30)).serviceDurationMinutes(40)
                                .build()));

        Collection<Issue> issues = validate(routePlan).issues();
        assertThat(issues).hasSize(5);
        assertThat(issues).hasAtLeastOneElementOfType(DuplicateVehicleIdIssue.class)
                .hasAtLeastOneElementOfType(DuplicateVisitIdIssue.class)
                .hasAtLeastOneElementOfType(NonExistingVisitReferenceIssue.class)
                .hasAtLeastOneElementOfType(VisitAssignedMoreThanOnceIssue.class)
                .hasAtLeastOneElementOfType(VisitWindowTooShortIssue.class);
    }

    private ValidationResult<Issue> validate(VehicleRoutePlanInput routePlan) {
        ValidationBuilder validationBuilder = new ValidationBuilder();
        validator.validate(validationBuilder, routePlan, ModelConfig.empty());
        return validationBuilder.build();
    }

    private static <T extends Issue> void assertSingleIssue(ValidationResult<Issue> result, Class<T> expectedType) {
        Collection<Issue> issues = result.issues();
        assertThat(issues).hasSize(1);
        Issue issue = issues.iterator().next();
        assertThat(issue).isInstanceOf(expectedType);
    }
}
