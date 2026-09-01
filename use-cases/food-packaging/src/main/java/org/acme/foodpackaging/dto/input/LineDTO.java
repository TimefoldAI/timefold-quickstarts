package org.acme.foodpackaging.dto.input;

import static java.util.Collections.emptyList;

import java.time.OffsetDateTime;
import java.util.List;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A production line, which produces the jobs assigned to it one after the other.")
public record LineDTO(
        @Schema(description = "Unique identifier of the line.", required = true, minLength = 1) String id,
        @Schema(description = "Display name of the line.", required = true, minLength = 1) String name,
        @Schema(description = "Time at which this line starts producing, in ISO-8601 date-time format.",
                required = true) OffsetDateTime startDateTime,
        @Schema(description = "ID of the operator running this line, or null if no operator is assigned yet.",
                minLength = 1) String operatorId,
        @Schema(description = "IDs of the jobs produced on this line, in production order. Empty if nothing is "
                + "scheduled on this line yet.") List<String> jobIds) {

    public LineDTO {
        jobIds = jobIds != null ? jobIds : emptyList();
    }

    public LineDTO withAssignment(String operatorId, List<String> jobIds) {
        return new LineDTO(id, name, startDateTime, operatorId, jobIds);
    }
}
