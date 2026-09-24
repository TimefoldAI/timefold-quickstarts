package org.acme.foodpackaging.dto.input;

import java.time.LocalDate;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "The window the schedule covers.")
public record WorkCalendarDTO(
        @Schema(description = "First day of the schedule, in ISO-8601 date format.", required = true) LocalDate fromDate,
        @Schema(description = "Day after the last day of the schedule, in ISO-8601 date format.",
                required = true) LocalDate toDate) {
}
