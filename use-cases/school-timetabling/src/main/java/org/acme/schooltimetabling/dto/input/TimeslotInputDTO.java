package org.acme.schooltimetabling.dto.input;

import java.time.DayOfWeek;
import java.time.LocalTime;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A recurring period in the weekly timetable that lessons can be scheduled in.")
public record TimeslotInputDTO(
        @Schema(description = "Unique identifier of the timeslot.", required = true, minLength = 1) String id,
        @Schema(description = "Day of the week the timeslot takes place on.", required = true) DayOfWeek dayOfWeek,
        @Schema(description = "Time of day the timeslot starts, e.g. 08:30.", required = true) LocalTime startTime,
        @Schema(description = "Time of day the timeslot ends, e.g. 09:30. Must be after the start time.",
                required = true) LocalTime endTime) {
}
