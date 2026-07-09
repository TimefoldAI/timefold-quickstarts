package org.acme.maintenancescheduling.dto;

import java.util.List;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A maintenance job that must be assigned to a crew and a start date.")
public record JobDTO(
        @Schema(description = "Unique identifier of the job.") String id,
        @Schema(description = "Display name of the job.") String name,
        @Schema(description = "Duration of the job expressed in business days.") int durationInDays,
        @Schema(description = "Earliest date the job may start, inclusive (yyyy-MM-dd).") String minStartDate,
        @Schema(description = "Latest date the job may end, exclusive (yyyy-MM-dd).") String maxEndDate,
        @Schema(description = "Preferred date the job should end, exclusive (yyyy-MM-dd).") String idealEndDate,
        @Schema(description = "Tags describing the job, e.g. its area.") List<String> tags,
        @Schema(description = "ID of the crew assigned to the job. Null when unassigned.") String crewId,
        @Schema(description = "Date the job is assigned to start, inclusive. Null when unassigned (yyyy-MM-dd).") String startDate,
        @Schema(description = "Date the job is assigned to end, exclusive. Null when unassigned (yyyy-MM-dd).") String endDate) {

    public JobDTO {
        name = name == null ? "" : name;
        tags = List.copyOf(tags);
        crewId = normalizeId(crewId);
        startDate = normalizeId(startDate);
    }

    private static String normalizeId(String value) {
        return value != null && value.isBlank() ? null : value;
    }

    public JobDTO withId(String id) {
        return new JobDTO(id, name, durationInDays, minStartDate, maxEndDate, idealEndDate, tags, crewId, startDate, endDate);
    }

    public JobDTO withName(String name) {
        return new JobDTO(id, name, durationInDays, minStartDate, maxEndDate, idealEndDate, tags, crewId, startDate, endDate);
    }

    public JobDTO withDurationInDays(int durationInDays) {
        return new JobDTO(id, name, durationInDays, minStartDate, maxEndDate, idealEndDate, tags, crewId, startDate, endDate);
    }

    public JobDTO withMinStartDate(String minStartDate) {
        return new JobDTO(id, name, durationInDays, minStartDate, maxEndDate, idealEndDate, tags, crewId, startDate, endDate);
    }

    public JobDTO withMaxEndDate(String maxEndDate) {
        return new JobDTO(id, name, durationInDays, minStartDate, maxEndDate, idealEndDate, tags, crewId, startDate, endDate);
    }

    public JobDTO withIdealEndDate(String idealEndDate) {
        return new JobDTO(id, name, durationInDays, minStartDate, maxEndDate, idealEndDate, tags, crewId, startDate, endDate);
    }

    public JobDTO withTags(List<String> tags) {
        return new JobDTO(id, name, durationInDays, minStartDate, maxEndDate, idealEndDate, tags, crewId, startDate, endDate);
    }

    public JobDTO withCrewId(String crewId) {
        return new JobDTO(id, name, durationInDays, minStartDate, maxEndDate, idealEndDate, tags, crewId, startDate, endDate);
    }

    public JobDTO withStartDate(String startDate) {
        return new JobDTO(id, name, durationInDays, minStartDate, maxEndDate, idealEndDate, tags, crewId, startDate, endDate);
    }

    public JobDTO withEndDate(String endDate) {
        return new JobDTO(id, name, durationInDays, minStartDate, maxEndDate, idealEndDate, tags, crewId, startDate, endDate);
    }
}
