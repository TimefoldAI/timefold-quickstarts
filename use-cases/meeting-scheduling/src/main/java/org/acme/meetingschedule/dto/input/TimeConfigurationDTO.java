package org.acme.meetingschedule.dto.input;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "When meetings may be held: the office hours of every day in the scheduling horizon, and how "
        + "finely those hours are divided into the slots a meeting can start in.")
public record TimeConfigurationDTO(
        @Schema(description = "Length in minutes of the slots the office hours are divided into. A meeting starts at "
                + "the beginning of a slot, and its duration has to be a whole number of slots. Smaller slots give "
                + "the solver more freedom, at the cost of a larger search space.",
                required = true, minimum = "1") Integer granularityInMinutes,
        @Schema(description = "Office hours per day. Two entries may share a date, to carve a break out of that day.",
                required = true, minItems = 1) List<OfficeHoursDTO> days) {

    /**
     * Divides the office hours into slots of {@link #granularityInMinutes()} minutes, dropping any trailing remainder
     * that is too short to hold a whole slot.
     *
     * @return every moment a meeting may start at, distinct and in chronological order, empty if this configuration
     *         does not describe any usable office hours
     */
    public List<OffsetDateTime> slotStartDateTimes() {
        if (granularityInMinutes == null || granularityInMinutes <= 0 || days == null) {
            return List.of();
        }
        // Keyed by instant rather than by OffsetDateTime, so two days written down in a different
        // offset do not each contribute their own copy of the same moment.
        Map<Instant, OffsetDateTime> startsByInstant = new TreeMap<>();
        for (OfficeHoursDTO day : days) {
            if (day == null || day.startDateTime() == null || day.endDateTime() == null) {
                continue;
            }
            for (OffsetDateTime slotStart = day.startDateTime(); !slotStart.plusMinutes(granularityInMinutes)
                    .isAfter(day.endDateTime()); slotStart = slotStart.plusMinutes(granularityInMinutes)) {
                startsByInstant.putIfAbsent(slotStart.toInstant(), slotStart);
            }
        }
        return List.copyOf(startsByInstant.values());
    }
}
