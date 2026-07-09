package org.acme.foodpackaging.dto;

import java.util.List;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A packaging line that processes an ordered list of jobs and is staffed by an operator.")
public record LineDTO(
        @Schema(description = "Unique identifier of the line.") String id,
        @Schema(description = "Display name of the line.") String name,
        @Schema(description = "Time at which the line becomes available, in ISO-8601 format (yyyy-MM-ddTHH:mm:ss).") String startDateTime,
        @Schema(description = "ID of the operator assigned to the line. Null when unassigned.") String operatorId,
        @Schema(description = "Ordered IDs of the jobs assigned to this line.") List<String> jobIds) {

    @SuppressWarnings("PMD.NullAssignment")
    public LineDTO {
        operatorId = operatorId != null && operatorId.isBlank() ? null : operatorId;
        jobIds = jobIds == null ? List.of() : List.copyOf(jobIds);
    }

    public LineDTO withId(String id) {
        return new LineDTO(id, name, startDateTime, operatorId, jobIds);
    }

    public LineDTO withName(String name) {
        return new LineDTO(id, name, startDateTime, operatorId, jobIds);
    }

    public LineDTO withStartDateTime(String startDateTime) {
        return new LineDTO(id, name, startDateTime, operatorId, jobIds);
    }

    public LineDTO withOperatorId(String operatorId) {
        return new LineDTO(id, name, startDateTime, operatorId, jobIds);
    }

    public LineDTO withJobIds(List<String> jobIds) {
        return new LineDTO(id, name, startDateTime, operatorId, jobIds);
    }
}
