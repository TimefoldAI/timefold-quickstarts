package org.acme.conferencescheduling.dto.output;

import java.util.List;

import ai.timefold.solver.service.definition.api.ModelOutput;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "The conference scheduling planning problem output.")
public record ConferenceScheduleOutput(
        @Schema(description = "Talks with their assigned timeslot and room, if any.", required = true,
                minItems = 1) List<TalkAssignmentDTO> talks)
        implements
            ModelOutput {
}
