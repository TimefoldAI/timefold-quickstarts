package org.acme.foodpackaging.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A packaging job that must be assigned to a line and sequenced among the other jobs on that line.")
public record JobDTO(
        @Schema(description = "Unique identifier of the job.") String id,
        @Schema(description = "Display name of the job.") String name,
        @Schema(description = "ID of the product packaged by this job.") String productId,
        @Schema(description = "Production duration of the job, in minutes.") long durationMinutes,
        @Schema(description = "Earliest start time of the job, in ISO-8601 format (yyyy-MM-ddTHH:mm:ss).") String minStartTime,
        @Schema(description = "Ideal end time of the job, in ISO-8601 format (yyyy-MM-ddTHH:mm:ss).") String idealEndTime,
        @Schema(description = "Maximum end time of the job, in ISO-8601 format (yyyy-MM-ddTHH:mm:ss).") String maxEndTime,
        @Schema(description = "Priority of the job; a higher number means a higher priority.") int priority,
        @Schema(description = "Whether the job assignment is pinned and must not be changed by the solver.") boolean pinned,
        @Schema(description = "ID of the line assigned to the job. Null when unassigned.") String lineId,
        @Schema(description = "Start of the cleaning window, in ISO-8601 format. Null when unassigned. Output only.") String startCleaningDateTime,
        @Schema(description = "Start of production, in ISO-8601 format. Null when unassigned. Output only.") String startProductionDateTime,
        @Schema(description = "End of production, in ISO-8601 format. Null when unassigned. Output only.") String endDateTime,
        @Schema(description = "ID of the operator on the assigned line. Null when unassigned. Output only.") String operatorId) {

    public JobDTO {
        lineId = normalizeId(lineId);
        operatorId = normalizeId(operatorId);
    }

    private static String normalizeId(String id) {
        return id != null && id.isBlank() ? null : id;
    }

    public JobDTO withId(String id) {
        return new JobDTO(id, name, productId, durationMinutes, minStartTime, idealEndTime, maxEndTime, priority,
                pinned, lineId, startCleaningDateTime, startProductionDateTime, endDateTime, operatorId);
    }

    public JobDTO withName(String name) {
        return new JobDTO(id, name, productId, durationMinutes, minStartTime, idealEndTime, maxEndTime, priority,
                pinned, lineId, startCleaningDateTime, startProductionDateTime, endDateTime, operatorId);
    }

    public JobDTO withProductId(String productId) {
        return new JobDTO(id, name, productId, durationMinutes, minStartTime, idealEndTime, maxEndTime, priority,
                pinned, lineId, startCleaningDateTime, startProductionDateTime, endDateTime, operatorId);
    }

    public JobDTO withDurationMinutes(long durationMinutes) {
        return new JobDTO(id, name, productId, durationMinutes, minStartTime, idealEndTime, maxEndTime, priority,
                pinned, lineId, startCleaningDateTime, startProductionDateTime, endDateTime, operatorId);
    }

    public JobDTO withMinStartTime(String minStartTime) {
        return new JobDTO(id, name, productId, durationMinutes, minStartTime, idealEndTime, maxEndTime, priority,
                pinned, lineId, startCleaningDateTime, startProductionDateTime, endDateTime, operatorId);
    }

    public JobDTO withIdealEndTime(String idealEndTime) {
        return new JobDTO(id, name, productId, durationMinutes, minStartTime, idealEndTime, maxEndTime, priority,
                pinned, lineId, startCleaningDateTime, startProductionDateTime, endDateTime, operatorId);
    }

    public JobDTO withMaxEndTime(String maxEndTime) {
        return new JobDTO(id, name, productId, durationMinutes, minStartTime, idealEndTime, maxEndTime, priority,
                pinned, lineId, startCleaningDateTime, startProductionDateTime, endDateTime, operatorId);
    }

    public JobDTO withPriority(int priority) {
        return new JobDTO(id, name, productId, durationMinutes, minStartTime, idealEndTime, maxEndTime, priority,
                pinned, lineId, startCleaningDateTime, startProductionDateTime, endDateTime, operatorId);
    }

    public JobDTO withPinned(boolean pinned) {
        return new JobDTO(id, name, productId, durationMinutes, minStartTime, idealEndTime, maxEndTime, priority,
                pinned, lineId, startCleaningDateTime, startProductionDateTime, endDateTime, operatorId);
    }

    public JobDTO withLineId(String lineId) {
        return new JobDTO(id, name, productId, durationMinutes, minStartTime, idealEndTime, maxEndTime, priority,
                pinned, lineId, startCleaningDateTime, startProductionDateTime, endDateTime, operatorId);
    }

    public JobDTO withStartCleaningDateTime(String startCleaningDateTime) {
        return new JobDTO(id, name, productId, durationMinutes, minStartTime, idealEndTime, maxEndTime, priority,
                pinned, lineId, startCleaningDateTime, startProductionDateTime, endDateTime, operatorId);
    }

    public JobDTO withStartProductionDateTime(String startProductionDateTime) {
        return new JobDTO(id, name, productId, durationMinutes, minStartTime, idealEndTime, maxEndTime, priority,
                pinned, lineId, startCleaningDateTime, startProductionDateTime, endDateTime, operatorId);
    }

    public JobDTO withEndDateTime(String endDateTime) {
        return new JobDTO(id, name, productId, durationMinutes, minStartTime, idealEndTime, maxEndTime, priority,
                pinned, lineId, startCleaningDateTime, startProductionDateTime, endDateTime, operatorId);
    }

    public JobDTO withOperatorId(String operatorId) {
        return new JobDTO(id, name, productId, durationMinutes, minStartTime, idealEndTime, maxEndTime, priority,
                pinned, lineId, startCleaningDateTime, startProductionDateTime, endDateTime, operatorId);
    }
}
