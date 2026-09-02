package org.acme.maintenancescheduling.dto.output;

import java.time.LocalDate;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.ALWAYS)
@Schema(description = "A maintenance job that is either scheduled on a crew and a start date, or not.")
public record JobOutputDTO(
        @Schema(description = "Unique identifier of the job.", required = true, minLength = 1) String id,
        @Schema(description = "ID of the crew this job is assigned to, or null if unassigned.") String crewId,
        @Schema(description = "Day the job starts on (inclusive), or null if unscheduled.") LocalDate startDate,
        @Schema(description = "Day the job ends on (exclusive), or null if unscheduled. Derived from the start date "
                + "by adding the job's duration in workdays.") LocalDate endDate) {
}
