package org.acme.flightcrewscheduling.dto;

import java.util.List;

import ai.timefold.solver.service.definition.api.ModelOutput;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "The flight crew scheduling planning problem output.")
public record FlightCrewScheduleOutput(
        @Schema(description = "List of airports flights depart from and arrive at.") List<AirportDTO> airports,
        @Schema(description = "List of employees that can be assigned as crew members.") List<EmployeeDTO> employees,
        @Schema(description = "List of flights to be crewed.") List<FlightDTO> flights,
        @Schema(description = "List of crew slots with their assigned employee.") //
        List<FlightAssignmentDTO> flightAssignments,
        @Schema(description = "The score of the solution.") String score) implements ModelOutput {

    public FlightCrewScheduleOutput {
        airports = List.copyOf(airports);
        employees = List.copyOf(employees);
        flights = List.copyOf(flights);
        flightAssignments = List.copyOf(flightAssignments);
    }

    public FlightCrewScheduleOutput withAirports(List<AirportDTO> airports) {
        return new FlightCrewScheduleOutput(airports, employees, flights, flightAssignments, score);
    }

    public FlightCrewScheduleOutput withEmployees(List<EmployeeDTO> employees) {
        return new FlightCrewScheduleOutput(airports, employees, flights, flightAssignments, score);
    }

    public FlightCrewScheduleOutput withFlights(List<FlightDTO> flights) {
        return new FlightCrewScheduleOutput(airports, employees, flights, flightAssignments, score);
    }

    public FlightCrewScheduleOutput withFlightAssignments(List<FlightAssignmentDTO> flightAssignments) {
        return new FlightCrewScheduleOutput(airports, employees, flights, flightAssignments, score);
    }

    public FlightCrewScheduleOutput withScore(String score) {
        return new FlightCrewScheduleOutput(airports, employees, flights, flightAssignments, score);
    }
}
