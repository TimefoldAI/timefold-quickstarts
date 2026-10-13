package org.acme.projectjobschedule.dto.output;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.ALWAYS)
@Schema(description = "The schedule of a single job: the execution mode it runs in, the delay it waits and the "
        + "dates that follow from those.")
public record AllocationOutputDTO(
        @Schema(description = "Unique identifier of the allocation.", required = true, minLength = 1) String id,
        @Schema(description = "ID of the execution mode the job runs in, or null if still unassigned.") String executionModeId,
        @Schema(description = "Days the job waits after its predecessors are done, or null if still unassigned.") Integer delay,
        @Schema(description = "Day number the job starts on, counted from the start of the schedule.") Integer startDate,
        @Schema(description = "Day number the job ends on, counted from the start of the schedule.") Integer endDate) {
}
