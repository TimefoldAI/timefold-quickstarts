package org.acme.flightcrewscheduling.service;

import static org.acme.flightcrewscheduling.support.TestHelper.aFlightAssignmentDTO;
import static org.acme.flightcrewscheduling.support.TestHelper.aFlightDTO;
import static org.acme.flightcrewscheduling.support.TestHelper.airportDTO;
import static org.acme.flightcrewscheduling.support.TestHelper.anEmployeeDTO;
import static org.acme.flightcrewscheduling.support.TestHelper.input;
import static org.acme.flightcrewscheduling.support.TestHelper.offsetDateTime;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.List;

import ai.timefold.solver.service.definition.api.domain.ModelConfig;
import ai.timefold.solver.service.definition.api.validation.Issue;
import ai.timefold.solver.service.definition.api.validation.ValidationBuilder;
import ai.timefold.solver.service.definition.api.validation.dto.ValidationResult;

import org.acme.flightcrewscheduling.demo.DemoDataBuilder;
import org.acme.flightcrewscheduling.dto.input.AirportInputDTO;
import org.acme.flightcrewscheduling.dto.input.EmployeeInputDTO;
import org.acme.flightcrewscheduling.dto.input.FlightAssignmentInputDTO;
import org.acme.flightcrewscheduling.dto.input.FlightCrewScheduleInput;
import org.acme.flightcrewscheduling.dto.input.FlightInputDTO;
import org.acme.flightcrewscheduling.service.validation.FlightCrewScheduleIssue.DuplicateAirportCodeIssue;
import org.acme.flightcrewscheduling.service.validation.FlightCrewScheduleIssue.DuplicateEmployeeIdIssue;
import org.acme.flightcrewscheduling.service.validation.FlightCrewScheduleIssue.DuplicateFlightAssignmentIdIssue;
import org.acme.flightcrewscheduling.service.validation.FlightCrewScheduleIssue.DuplicateFlightNumberIssue;
import org.acme.flightcrewscheduling.service.validation.FlightCrewScheduleIssue.FlightArrivesBeforeItDepartsIssue;
import org.acme.flightcrewscheduling.service.validation.FlightCrewScheduleIssue.NonExistingAirportReferenceIssue;
import org.acme.flightcrewscheduling.service.validation.FlightCrewScheduleIssue.NonExistingEmployeeReferenceIssue;
import org.acme.flightcrewscheduling.service.validation.FlightCrewScheduleIssue.NonExistingFlightReferenceIssue;
import org.junit.jupiter.api.Test;

// OpenAPI spec compliance (Bean Validation) is enforced by the Service module at the REST layer, so it's
// covered by org.acme.flightcrewscheduling.rest.FlightCrewScheduleOpenApiValidationTest instead. This class
// only covers the domain-specific checks FlightCrewScheduleValidator implements itself.
class FlightCrewScheduleValidatorTest {

    private static final List<AirportInputDTO> AIRPORTS = List.of(airportDTO("LHR"), airportDTO("JFK"));
    private static final List<EmployeeInputDTO> EMPLOYEES = List.of(anEmployeeDTO("crew-1").build());
    private static final List<FlightInputDTO> FLIGHTS = List.of(aFlightDTO("TF1").build());
    private static final List<FlightAssignmentInputDTO> FLIGHT_ASSIGNMENTS =
            List.of(aFlightAssignmentDTO("seat-1", "TF1").build());

    private final FlightCrewScheduleValidator validator = new FlightCrewScheduleValidator();

    @Test
    void validInputHasNoIssues() {
        ValidationResult<Issue> result = validate(input(AIRPORTS, EMPLOYEES, FLIGHTS, FLIGHT_ASSIGNMENTS));
        assertThat(result.issues()).isEmpty();
    }

    @Test
    void demoDatasetHasNoIssues() {
        // The service must never ship demo data that its own validator would reject.
        ValidationResult<Issue> result = validate(DemoDataBuilder.basic());
        assertThat(result.issues()).isEmpty();
    }

    @Test
    void duplicateAirportCode() {
        FlightCrewScheduleInput input = input(List.of(airportDTO("LHR"), airportDTO("JFK"), airportDTO("LHR")),
                EMPLOYEES, FLIGHTS, FLIGHT_ASSIGNMENTS);
        assertSingleIssue(validate(input), DuplicateAirportCodeIssue.class);
    }

    @Test
    void duplicateEmployeeId() {
        EmployeeInputDTO employee = anEmployeeDTO("crew-1").build();
        FlightCrewScheduleInput input = input(AIRPORTS, List.of(employee, employee), FLIGHTS, FLIGHT_ASSIGNMENTS);
        assertSingleIssue(validate(input), DuplicateEmployeeIdIssue.class);
    }

    @Test
    void duplicateFlightNumber() {
        FlightInputDTO flight = aFlightDTO("TF1").build();
        FlightCrewScheduleInput input = input(AIRPORTS, EMPLOYEES, List.of(flight, flight), FLIGHT_ASSIGNMENTS);
        assertSingleIssue(validate(input), DuplicateFlightNumberIssue.class);
    }

    @Test
    void duplicateFlightAssignmentId() {
        FlightAssignmentInputDTO flightAssignment = aFlightAssignmentDTO("seat-1", "TF1").build();
        FlightCrewScheduleInput input =
                input(AIRPORTS, EMPLOYEES, FLIGHTS, List.of(flightAssignment, flightAssignment));
        assertSingleIssue(validate(input), DuplicateFlightAssignmentIdIssue.class);
    }

    @Test
    void nonExistingHomeAirportReference() {
        FlightCrewScheduleInput input = input(AIRPORTS,
                List.of(anEmployeeDTO("crew-1").homeAirportCode("XXX").build()), FLIGHTS, FLIGHT_ASSIGNMENTS);
        assertSingleIssue(validate(input), NonExistingAirportReferenceIssue.class);
    }

    @Test
    void nonExistingFlightAirportReference() {
        FlightCrewScheduleInput input = input(AIRPORTS, EMPLOYEES,
                List.of(aFlightDTO("TF1").arrival("XXX", offsetDateTime(0, 14)).build()), FLIGHT_ASSIGNMENTS);
        assertSingleIssue(validate(input), NonExistingAirportReferenceIssue.class);
    }

    @Test
    void nonExistingFlightReference() {
        FlightCrewScheduleInput input = input(AIRPORTS, EMPLOYEES, FLIGHTS,
                List.of(aFlightAssignmentDTO("seat-1", "does-not-exist").build()));
        assertSingleIssue(validate(input), NonExistingFlightReferenceIssue.class);
    }

    @Test
    void nonExistingEmployeeReference() {
        FlightCrewScheduleInput input = input(AIRPORTS, EMPLOYEES, FLIGHTS,
                List.of(aFlightAssignmentDTO("seat-1", "TF1").employeeId("does-not-exist").build()));
        assertSingleIssue(validate(input), NonExistingEmployeeReferenceIssue.class);
    }

    @Test
    void flightArrivesBeforeItDeparts() {
        FlightCrewScheduleInput input = input(AIRPORTS, EMPLOYEES,
                List.of(aFlightDTO("TF1")
                        .departure("LHR", offsetDateTime(0, 14))
                        .arrival("JFK", offsetDateTime(0, 6))
                        .build()),
                FLIGHT_ASSIGNMENTS);
        assertSingleIssue(validate(input), FlightArrivesBeforeItDepartsIssue.class);
    }

    @Test
    void mixedDatasetReportsEveryIssue() {
        FlightCrewScheduleInput input = input(
                List.of(airportDTO("LHR"), airportDTO("LHR")),
                List.of(anEmployeeDTO("crew-1").homeAirportCode("XXX").build()),
                List.of(aFlightDTO("TF1")
                        .departure("LHR", offsetDateTime(0, 14))
                        .arrival("LHR", offsetDateTime(0, 6))
                        .build()),
                List.of(aFlightAssignmentDTO("seat-1", "TF2").employeeId("does-not-exist").build()));

        Collection<Issue> issues = validate(input).issues();
        assertThat(issues).hasSize(5);
        assertThat(issues).hasAtLeastOneElementOfType(DuplicateAirportCodeIssue.class)
                .hasAtLeastOneElementOfType(NonExistingAirportReferenceIssue.class)
                .hasAtLeastOneElementOfType(FlightArrivesBeforeItDepartsIssue.class)
                .hasAtLeastOneElementOfType(NonExistingFlightReferenceIssue.class)
                .hasAtLeastOneElementOfType(NonExistingEmployeeReferenceIssue.class);
    }

    private ValidationResult<Issue> validate(FlightCrewScheduleInput input) {
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
