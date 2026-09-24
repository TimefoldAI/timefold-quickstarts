package org.acme.meetingschedule.dto.output;

import java.util.List;

import ai.timefold.solver.service.definition.api.ModelOutput;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "The meeting scheduling planning problem output.")
public record MeetingScheduleOutput(
        @Schema(description = "Meetings with their assigned room and start, if any.",
                required = true) List<MeetingOutputDTO> meetings)
        implements
            ModelOutput {
}
