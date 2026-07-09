package org.acme.schooltimetabling.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A teaching slot on a particular day and time range.")
public record TimeslotDTO(
        @Schema(description = "Unique identifier of the timeslot.") String id,
        @Schema(description = "Day of the week on which the timeslot occurs, e.g. MONDAY.") String dayOfWeek,
        @Schema(description = "Local time at which the timeslot starts, in ISO-8601 format (HH:mm).") String startTime,
        @Schema(description = "Local time at which the timeslot ends, in ISO-8601 format (HH:mm).") String endTime) {

    public TimeslotDTO {
        // no-op compact constructor required by repository rules
    }

    public TimeslotDTO withId(String id) {
        return new TimeslotDTO(id, dayOfWeek, startTime, endTime);
    }

    public TimeslotDTO withDayOfWeek(String dayOfWeek) {
        return new TimeslotDTO(id, dayOfWeek, startTime, endTime);
    }

    public TimeslotDTO withStartTime(String startTime) {
        return new TimeslotDTO(id, dayOfWeek, startTime, endTime);
    }

    public TimeslotDTO withEndTime(String endTime) {
        return new TimeslotDTO(id, dayOfWeek, startTime, endTime);
    }
}
