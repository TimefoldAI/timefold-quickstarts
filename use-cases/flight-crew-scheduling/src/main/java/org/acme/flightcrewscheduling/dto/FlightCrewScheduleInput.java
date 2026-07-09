package org.acme.flightcrewscheduling.dto;

import java.util.List;

import ai.timefold.solver.service.definition.api.ModelInput;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "The flight crew scheduling planning problem input.")
public record FlightCrewScheduleInput(
        @Schema(description = "List of airports flights depart from and arrive at.") List<AirportDTO> airports,
        @Schema(description = "List of employees that can be assigned as crew members.") List<EmployeeDTO> employees,
        @Schema(description = "List of flights to be crewed.") List<FlightDTO> flights,
        @Schema(description = "List of crew slots that must each be assigned to an employee.") //
        List<FlightAssignmentDTO> flightAssignments) implements ModelInput {

    public FlightCrewScheduleInput {
        airports = List.copyOf(airports);
        employees = List.copyOf(employees);
        flights = List.copyOf(flights);
        flightAssignments = List.copyOf(flightAssignments);
    }

    public FlightCrewScheduleInput withAirports(List<AirportDTO> airports) {
        return new FlightCrewScheduleInput(airports, employees, flights, flightAssignments);
    }

    public FlightCrewScheduleInput withEmployees(List<EmployeeDTO> employees) {
        return new FlightCrewScheduleInput(airports, employees, flights, flightAssignments);
    }

    public FlightCrewScheduleInput withFlights(List<FlightDTO> flights) {
        return new FlightCrewScheduleInput(airports, employees, flights, flightAssignments);
    }

    public FlightCrewScheduleInput withFlightAssignments(List<FlightAssignmentDTO> flightAssignments) {
        return new FlightCrewScheduleInput(airports, employees, flights, flightAssignments);
    }
}
