package org.acme.projectjobschedule.dto;

import java.util.List;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "The scheduling of a single job: its chosen execution mode, delay and computed dates.")
public record AllocationDTO(
        @Schema(description = "Unique identifier of the allocation.") String id,
        @Schema(description = "ID of the job scheduled by this allocation.") String jobId,
        @Schema(description = "ID of the source allocation of the same project.") String sourceAllocationId,
        @Schema(description = "ID of the sink allocation of the same project.") String sinkAllocationId,
        @Schema(description = "IDs of the allocations that must complete before this one starts.") List<String> predecessorAllocationIds,
        @Schema(description = "IDs of the allocations that start after this one completes.") List<String> successorAllocationIds,
        @Schema(description = "ID of the chosen execution mode. Blank when unscheduled.") String executionModeId,
        @Schema(description = "Delay, in days, applied before the allocation starts. Null when unscheduled.") Integer delay,
        @Schema(description = "The computed start date, in days. Null when unscheduled.") Integer startDate,
        @Schema(description = "The computed end date, in days. Null when unscheduled.") Integer endDate) {

    public AllocationDTO {
        id = id == null ? "" : id;
        jobId = jobId == null ? "" : jobId;
        sourceAllocationId = normalizeId(sourceAllocationId);
        sinkAllocationId = normalizeId(sinkAllocationId);
        predecessorAllocationIds = predecessorAllocationIds == null ? List.of() : List.copyOf(predecessorAllocationIds);
        successorAllocationIds = successorAllocationIds == null ? List.of() : List.copyOf(successorAllocationIds);
        executionModeId = normalizeId(executionModeId);
    }

    private static String normalizeId(String id) {
        return id != null && id.isBlank() ? null : id;
    }

    public AllocationDTO withId(String id) {
        return new AllocationDTO(id, jobId, sourceAllocationId, sinkAllocationId, predecessorAllocationIds,
                successorAllocationIds, executionModeId, delay, startDate, endDate);
    }

    public AllocationDTO withJobId(String jobId) {
        return new AllocationDTO(id, jobId, sourceAllocationId, sinkAllocationId, predecessorAllocationIds,
                successorAllocationIds, executionModeId, delay, startDate, endDate);
    }

    public AllocationDTO withSourceAllocationId(String sourceAllocationId) {
        return new AllocationDTO(id, jobId, sourceAllocationId, sinkAllocationId, predecessorAllocationIds,
                successorAllocationIds, executionModeId, delay, startDate, endDate);
    }

    public AllocationDTO withSinkAllocationId(String sinkAllocationId) {
        return new AllocationDTO(id, jobId, sourceAllocationId, sinkAllocationId, predecessorAllocationIds,
                successorAllocationIds, executionModeId, delay, startDate, endDate);
    }

    public AllocationDTO withPredecessorAllocationIds(List<String> predecessorAllocationIds) {
        return new AllocationDTO(id, jobId, sourceAllocationId, sinkAllocationId, predecessorAllocationIds,
                successorAllocationIds, executionModeId, delay, startDate, endDate);
    }

    public AllocationDTO withSuccessorAllocationIds(List<String> successorAllocationIds) {
        return new AllocationDTO(id, jobId, sourceAllocationId, sinkAllocationId, predecessorAllocationIds,
                successorAllocationIds, executionModeId, delay, startDate, endDate);
    }

    public AllocationDTO withExecutionModeId(String executionModeId) {
        return new AllocationDTO(id, jobId, sourceAllocationId, sinkAllocationId, predecessorAllocationIds,
                successorAllocationIds, executionModeId, delay, startDate, endDate);
    }

    public AllocationDTO withDelay(Integer delay) {
        return new AllocationDTO(id, jobId, sourceAllocationId, sinkAllocationId, predecessorAllocationIds,
                successorAllocationIds, executionModeId, delay, startDate, endDate);
    }

    public AllocationDTO withStartDate(Integer startDate) {
        return new AllocationDTO(id, jobId, sourceAllocationId, sinkAllocationId, predecessorAllocationIds,
                successorAllocationIds, executionModeId, delay, startDate, endDate);
    }

    public AllocationDTO withEndDate(Integer endDate) {
        return new AllocationDTO(id, jobId, sourceAllocationId, sinkAllocationId, predecessorAllocationIds,
                successorAllocationIds, executionModeId, delay, startDate, endDate);
    }
}
