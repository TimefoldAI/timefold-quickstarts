package org.acme.flightcrewscheduling.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;

import ai.timefold.solver.service.definition.api.domain.ModelConfig;
import ai.timefold.solver.service.definition.api.validation.ModelValidator;
import ai.timefold.solver.service.definition.api.validation.ValidationBuilder;

import org.acme.flightcrewscheduling.dto.input.AirportInputDTO;
import org.acme.flightcrewscheduling.dto.input.EmployeeInputDTO;
import org.acme.flightcrewscheduling.dto.input.FlightAssignmentInputDTO;
import org.acme.flightcrewscheduling.dto.input.FlightCrewScheduleConfigOverrides;
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

@ApplicationScoped
public class FlightCrewScheduleValidator
        implements ModelValidator<FlightCrewScheduleInput, FlightCrewScheduleConfigOverrides> {

    @Override
    public void validate(ValidationBuilder validationBuilder, FlightCrewScheduleInput modelInput,
            ModelConfig<FlightCrewScheduleConfigOverrides> modelConfig) {
        // OpenAPI spec (Bean Validation) compliance is enforced by the Service module at the REST layer,
        // before this validator ever runs; only domain-specific checks (duplicate ids, dangling references
        // and model invariants) belong here.
        Set<String> airportCodes = validateAirports(validationBuilder, orEmpty(modelInput.airports()));
        Set<String> employeeIds = validateEmployees(validationBuilder, orEmpty(modelInput.employees()), airportCodes);
        Set<String> flightNumbers = validateFlights(validationBuilder, orEmpty(modelInput.flights()), airportCodes);
        validateFlightAssignments(validationBuilder, orEmpty(modelInput.flightAssignments()), flightNumbers, employeeIds);
    }

    private static <T> List<T> orEmpty(List<T> list) {
        return list == null ? List.of() : list;
    }

    private static Set<String> validateAirports(ValidationBuilder validationBuilder, List<AirportInputDTO> airports) {
        Set<String> airportCodes = new HashSet<>();
        for (AirportInputDTO airport : airports) {
            if (hasId(airport.code()) && !airportCodes.add(airport.code())) {
                validationBuilder.addIssue(new DuplicateAirportCodeIssue(airport.code()));
            }
        }
        return airportCodes;
    }

    private static Set<String> validateEmployees(ValidationBuilder validationBuilder, List<EmployeeInputDTO> employees,
            Set<String> airportCodes) {
        Set<String> employeeIds = new HashSet<>();
        for (EmployeeInputDTO employee : employees) {
            if (hasId(employee.id()) && !employeeIds.add(employee.id())) {
                validationBuilder.addIssue(new DuplicateEmployeeIdIssue(employee.id()));
            }
            if (!airportCodes.contains(employee.homeAirportCode())) {
                validationBuilder.addIssue(new NonExistingAirportReferenceIssue(employee.homeAirportCode()));
            }
        }
        return employeeIds;
    }

    private static Set<String> validateFlights(ValidationBuilder validationBuilder, List<FlightInputDTO> flights,
            Set<String> airportCodes) {
        Set<String> flightNumbers = new HashSet<>();
        for (FlightInputDTO flight : flights) {
            if (hasId(flight.flightNumber()) && !flightNumbers.add(flight.flightNumber())) {
                validationBuilder.addIssue(new DuplicateFlightNumberIssue(flight.flightNumber()));
            }
            // Only the first unknown airport of a flight is reported: the second one adds no information the
            // first does not already give, and would double every issue for a flight between two typos.
            if (!airportCodes.contains(flight.departureAirportCode())) {
                validationBuilder.addIssue(new NonExistingAirportReferenceIssue(flight.departureAirportCode()));
            } else if (!airportCodes.contains(flight.arrivalAirportCode())) {
                validationBuilder.addIssue(new NonExistingAirportReferenceIssue(flight.arrivalAirportCode()));
            }
            if (!flight.arrivalUTCDateTime().isAfter(flight.departureUTCDateTime())) {
                validationBuilder.addIssue(new FlightArrivesBeforeItDepartsIssue(flight.flightNumber()));
            }
        }
        return flightNumbers;
    }

    private static void validateFlightAssignments(ValidationBuilder validationBuilder,
            List<FlightAssignmentInputDTO> flightAssignments, Set<String> flightNumbers, Set<String> employeeIds) {
        Set<String> flightAssignmentIds = new HashSet<>();
        for (FlightAssignmentInputDTO flightAssignment : flightAssignments) {
            if (hasId(flightAssignment.id()) && !flightAssignmentIds.add(flightAssignment.id())) {
                validationBuilder.addIssue(new DuplicateFlightAssignmentIdIssue(flightAssignment.id()));
            }
            if (!flightNumbers.contains(flightAssignment.flightNumber())) {
                validationBuilder.addIssue(new NonExistingFlightReferenceIssue(flightAssignment.id()));
            }
            if (flightAssignment.employeeId() != null && !employeeIds.contains(flightAssignment.employeeId())) {
                validationBuilder.addIssue(new NonExistingEmployeeReferenceIssue(flightAssignment.id()));
            }
        }
    }

    private static boolean hasId(String id) {
        return id != null && !id.isBlank();
    }
}
