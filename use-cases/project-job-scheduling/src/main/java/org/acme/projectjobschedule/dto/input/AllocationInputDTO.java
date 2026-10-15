package org.acme.projectjobschedule.dto.input;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = """
        The scheduling of a single job: the execution mode it runs in and the delay it waits \
        on top of the date its predecessors are done.""")
public record AllocationInputDTO(
        @Schema(description = "Unique identifier of the allocation.", required = true, minLength = 1) String id,
        @Schema(description = "ID of the job this allocation schedules. Every job needs exactly one allocation.",
                required = true, minLength = 1) String jobId,
        @Schema(description = """
                ID of the execution mode the job runs in, or null if not decided yet. Must be one of \
                the job's own execution modes.""", minLength = 1) String executionModeId,
        @Schema(description = "Days this job waits after its predecessors are done, or null if not decided yet.",
                minimum = "0") Integer delay,
        @Schema(description = """
                Whether the solver must keep this allocation's execution mode and delay as \
                submitted. A pinned allocation needs both. Pinning does not fix the job's dates: the job still \
                starts its delay after its predecessors are done, so it moves if they do. The allocations of \
                SOURCE and SINK jobs are always pinned, whatever this says.""") Boolean pinned) {

    public AllocationInputDTO withAssignment(String executionModeId, Integer delay) {
        return new AllocationInputDTO(id, jobId, executionModeId, delay, pinned);
    }
}
