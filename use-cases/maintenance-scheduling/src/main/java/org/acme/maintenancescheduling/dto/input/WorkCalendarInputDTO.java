package org.acme.maintenancescheduling.dto.input;

import java.time.LocalDate;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "The window of workdays that jobs can be scheduled in. Weekends inside the window are skipped.")
public record WorkCalendarInputDTO(
        @Schema(description = "Unique identifier of the work calendar.", required = true, minLength = 1) String id,
        @Schema(description = "First day of the planning window (inclusive), in ISO-8601 date format.",
                required = true) LocalDate fromDate,
        @Schema(description = "Day after the last day of the planning window (exclusive), in ISO-8601 date format.",
                required = true) LocalDate toDate) {
}
