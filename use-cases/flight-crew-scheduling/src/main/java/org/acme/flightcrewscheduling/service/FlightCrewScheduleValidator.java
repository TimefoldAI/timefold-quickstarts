package org.acme.flightcrewscheduling.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;

import ai.timefold.solver.service.definition.api.domain.ModelConfig;
import ai.timefold.solver.service.definition.api.validation.ModelValidator;
import ai.timefold.solver.service.definition.api.validation.ValidationBuilder;

import org.acme.flightcrewscheduling.dto.AirportDTO;
import org.acme.flightcrewscheduling.dto.AirportIdDetail;
import org.acme.flightcrewscheduling.dto.EmployeeDTO;
import org.acme.flightcrewscheduling.dto.EmployeeIdDetail;
import org.acme.flightcrewscheduling.dto.FlightAssignmentDTO;
import org.acme.flightcrewscheduling.dto.FlightAssignmentIdDetail;
import org.acme.flightcrewscheduling.dto.FlightCrewScheduleConfigOverrides;
import org.acme.flightcrewscheduling.dto.FlightCrewScheduleInput;
import org.acme.flightcrewscheduling.dto.FlightDTO;
import org.acme.flightcrewscheduling.dto.FlightIdDetail;
import org.acme.flightcrewscheduling.service.FlightCrewScheduleIssues.AirportIdMissingIssue;
import org.acme.flightcrewscheduling.service.FlightCrewScheduleIssues.DuplicateAirportIdIssue;
import org.acme.flightcrewscheduling.service.FlightCrewScheduleIssues.DuplicateEmployeeIdIssue;
import org.acme.flightcrewscheduling.service.FlightCrewScheduleIssues.DuplicateFlightAssignmentIdIssue;
import org.acme.flightcrewscheduling.service.FlightCrewScheduleIssues.DuplicateFlightNumberIssue;
import org.acme.flightcrewscheduling.service.FlightCrewScheduleIssues.EmployeeIdMissingIssue;
import org.acme.flightcrewscheduling.service.FlightCrewScheduleIssues.FlightAssignmentIdMissingIssue;
import org.acme.flightcrewscheduling.service.FlightCrewScheduleIssues.FlightNumberMissingIssue;
import org.acme.flightcrewscheduling.service.FlightCrewScheduleIssues.NonExistingArrivalAirportReferenceIssue;
import org.acme.flightcrewscheduling.service.FlightCrewScheduleIssues.NonExistingDepartureAirportReferenceIssue;
import org.acme.flightcrewscheduling.service.FlightCrewScheduleIssues.NonExistingEmployeeReferenceIssue;
import org.acme.flightcrewscheduling.service.FlightCrewScheduleIssues.NonExistingFlightReferenceIssue;
import org.acme.flightcrewscheduling.service.FlightCrewScheduleIssues.NonExistingHomeAirportReferenceIssue;

@ApplicationScoped
public class FlightCrewScheduleValidator
        implements ModelValidator<FlightCrewScheduleInput, FlightCrewScheduleConfigOverrides> {

    private static final String AVOID_NEW_IN_LOOP = "PMD.AvoidInstantiatingObjectsInLoops";

    @Override
    public void validate(ValidationBuilder validationBuilder, FlightCrewScheduleInput modelInput,
            ModelConfig<FlightCrewScheduleConfigOverrides> modelConfig) {
        Set<String> airportIds = validateAirports(validationBuilder, modelInput.airports());
        Set<String> employeeIds = validateEmployees(validationBuilder, modelInput.employees(), airportIds);
        Set<String> flightNumbers = validateFlights(validationBuilder, modelInput.flights(), airportIds);
        validateFlightAssignments(validationBuilder, modelInput.flightAssignments(), flightNumbers, employeeIds);
    }

    @SuppressWarnings(AVOID_NEW_IN_LOOP)
    private Set<String> validateAirports(ValidationBuilder validationBuilder, List<AirportDTO> airports) {
        Set<String> airportIds = new HashSet<>();
        for (AirportDTO airport : airports) {
            if (airport.id() == null || airport.id().isBlank()) {
                validationBuilder.addIssue(new AirportIdMissingIssue());
            } else if (!airportIds.add(airport.id())) {
                validationBuilder.addIssue(new DuplicateAirportIdIssue(new AirportIdDetail(airport.id())));
            }
        }
        return airportIds;
    }

    @SuppressWarnings(AVOID_NEW_IN_LOOP)
    private Set<String> validateEmployees(ValidationBuilder validationBuilder, List<EmployeeDTO> employees,
            Set<String> airportIds) {
        Set<String> employeeIds = new HashSet<>();
        for (EmployeeDTO employee : employees) {
            if (employee.id() == null || employee.id().isBlank()) {
                validationBuilder.addIssue(new EmployeeIdMissingIssue());
            } else if (!employeeIds.add(employee.id())) {
                validationBuilder.addIssue(new DuplicateEmployeeIdIssue(new EmployeeIdDetail(employee.id())));
            }
            if (employee.homeAirportId() != null && !airportIds.contains(employee.homeAirportId())) {
                validationBuilder.addIssue(new NonExistingHomeAirportReferenceIssue(new EmployeeIdDetail(employee.id())));
            }
        }
        return employeeIds;
    }

    @SuppressWarnings(AVOID_NEW_IN_LOOP)
    private Set<String> validateFlights(ValidationBuilder validationBuilder, List<FlightDTO> flights,
            Set<String> airportIds) {
        Set<String> flightNumbers = new HashSet<>();
        for (FlightDTO flight : flights) {
            if (flight.flightNumber() == null || flight.flightNumber().isBlank()) {
                validationBuilder.addIssue(new FlightNumberMissingIssue());
            } else if (!flightNumbers.add(flight.flightNumber())) {
                validationBuilder.addIssue(new DuplicateFlightNumberIssue(new FlightIdDetail(flight.flightNumber())));
            }
            if (flight.departureAirportId() != null && !airportIds.contains(flight.departureAirportId())) {
                validationBuilder
                        .addIssue(new NonExistingDepartureAirportReferenceIssue(new FlightIdDetail(flight.flightNumber())));
            }
            if (flight.arrivalAirportId() != null && !airportIds.contains(flight.arrivalAirportId())) {
                validationBuilder
                        .addIssue(new NonExistingArrivalAirportReferenceIssue(new FlightIdDetail(flight.flightNumber())));
            }
        }
        return flightNumbers;
    }

    @SuppressWarnings(AVOID_NEW_IN_LOOP)
    private void validateFlightAssignments(ValidationBuilder validationBuilder, List<FlightAssignmentDTO> assignments,
            Set<String> flightNumbers, Set<String> employeeIds) {
        Set<String> assignmentIds = new HashSet<>();
        for (FlightAssignmentDTO assignment : assignments) {
            if (assignment.id() == null || assignment.id().isBlank()) {
                validationBuilder.addIssue(new FlightAssignmentIdMissingIssue());
            } else if (!assignmentIds.add(assignment.id())) {
                validationBuilder
                        .addIssue(new DuplicateFlightAssignmentIdIssue(new FlightAssignmentIdDetail(assignment.id())));
            }
            if (assignment.flightNumber() != null && !flightNumbers.contains(assignment.flightNumber())) {
                validationBuilder
                        .addIssue(new NonExistingFlightReferenceIssue(new FlightAssignmentIdDetail(assignment.id())));
            }
            if (assignment.employeeId() != null && !employeeIds.contains(assignment.employeeId())) {
                validationBuilder
                        .addIssue(new NonExistingEmployeeReferenceIssue(new FlightAssignmentIdDetail(assignment.id())));
            }
        }
    }
}
