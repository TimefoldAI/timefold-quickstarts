package org.acme.flightcrewscheduling.dto.output;

import java.util.List;

import ai.timefold.solver.service.definition.api.ModelOutput;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "The flight crew scheduling planning problem output.")
public record FlightCrewScheduleOutput(
        @Schema(description = "Crew seats with their assigned crew member, if any.", required = true,
                minItems = 1) List<FlightAssignmentOutputDTO> flightAssignments)
        implements
            ModelOutput {
}
