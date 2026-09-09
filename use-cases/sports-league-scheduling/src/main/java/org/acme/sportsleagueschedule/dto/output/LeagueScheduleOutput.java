package org.acme.sportsleagueschedule.dto.output;

import java.util.List;

import ai.timefold.solver.service.definition.api.ModelOutput;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "The sports league scheduling problem output.")
public record LeagueScheduleOutput(
        @Schema(description = "Matches with the round they are played in, if any.",
                required = true) List<MatchAssignmentDTO> matches)
        implements
            ModelOutput {
}
