package org.acme.flightcrewscheduling.dto.input;

import java.util.List;

import ai.timefold.solver.service.definition.api.ModelInput;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "The flight crew scheduling planning problem input.")
public record FlightCrewScheduleInput(
        @Schema(description = "Airports the flights depart from and arrive at.", required = true,
                minItems = 1) List<AirportInputDTO> airports,
        @Schema(description = "Crew members available for assignment.", required = true,
                minItems = 1) List<EmployeeInputDTO> employees,
        @Schema(description = "Flights that need a crew.", required = true, minItems = 1) List<FlightInputDTO> flights,
        @Schema(description = "Crew seats that must each be assigned to a crew member.", required = true,
                minItems = 1) List<FlightAssignmentInputDTO> flightAssignments)
        implements
            ModelInput {

    public FlightCrewScheduleInput withFlightAssignments(List<FlightAssignmentInputDTO> flightAssignments) {
        return new FlightCrewScheduleInput(airports, employees, flights, flightAssignments);
    }
}
