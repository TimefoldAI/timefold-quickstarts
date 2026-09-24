package org.acme.meetingschedule.dto.input;

import java.time.OffsetDateTime;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "The office hours of one day: no meeting may start before the start, and every meeting of that "
        + "day must be finished by the end.")
public record OfficeHoursDTO(
        @Schema(description = "First moment of the day a meeting may start, in ISO-8601 date and time format with an "
                + "offset. Its date is the day these office hours belong to.",
                required = true) OffsetDateTime startDateTime,
        @Schema(description = "Moment by which every meeting of that day must be finished, in ISO-8601 date and time "
                + "format with an offset.", required = true) OffsetDateTime endDateTime) {
}
