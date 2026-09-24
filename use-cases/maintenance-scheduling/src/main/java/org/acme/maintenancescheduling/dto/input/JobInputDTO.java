package org.acme.maintenancescheduling.dto.input;

import static java.util.Collections.emptyList;

import java.time.LocalDate;
import java.util.List;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A maintenance job that must be assigned to a crew and a start date.")
public record JobInputDTO(
        @Schema(description = "Unique identifier of the job.", required = true, minLength = 1) String id,
        @Schema(description = "Display name of the job.", required = true, minLength = 1) String name,
        @Schema(description = "Number of workdays the job takes. Weekends do not count towards it.", required = true,
                minimum = "1") Integer durationInDays,
        @Schema(description = "First day the job may start on (inclusive), in ISO-8601 date format.",
                required = true) LocalDate minStartDate,
        @Schema(description = "Day by which the job must be finished (exclusive), in ISO-8601 date format.",
                required = true) LocalDate maxEndDate,
        @Schema(description = "Day the job ideally finishes on (exclusive), in ISO-8601 date format. "
                + "Finishing earlier repeats the maintenance sooner; finishing later risks the due date.",
                required = true) LocalDate idealEndDate,
        @Schema(description = "Tags of jobs that should not be worked on at the same time, "
                + "for example the area the job takes place in.") List<String> tags,
        @Schema(description = "ID of the crew this job is assigned to, or null if unassigned.",
                minLength = 1) String crewId,
        @Schema(description = "Day the job is scheduled to start on, or null if unscheduled.") LocalDate startDate) {

    public JobInputDTO {
        tags = tags != null ? tags : emptyList();
    }

    public JobInputDTO withAssignment(String crewId, LocalDate startDate) {
        return new JobInputDTO(id, name, durationInDays, minStartDate, maxEndDate, idealEndDate, tags, crewId,
                startDate);
    }
}
