package org.acme.tournamentschedule.dto.output;

import java.util.List;

import ai.timefold.solver.service.definition.api.ModelOutput;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "The tournament scheduling planning problem output.")
public record TournamentScheduleOutput(
        @Schema(description = "Match slots with their assigned team, if any.",
                required = true) List<MatchOutputDTO> matches)
        implements
            ModelOutput {
}
