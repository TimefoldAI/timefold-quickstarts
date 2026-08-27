package org.acme.conferencescheduling.domain;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;

import ai.timefold.solver.core.api.domain.common.PlanningId;

public class Timeslot {
    private @PlanningId String id;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private Set<String> tags;

    // Cached
    private int durationInMinutes;

    public Timeslot(String id, LocalDateTime startDateTime, LocalDateTime endDateTime, Set<String> tags) {
        this.id = id;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.tags = tags;
        durationInMinutes = (startDateTime == null || endDateTime == null) ? 0
                : (int) Duration.between(startDateTime, endDateTime).toMinutes();
    }

    public boolean overlapsTime(Timeslot other) {
        if (this.equals(other)) {
            return true;
        }
        return startDateTime.isBefore(other.endDateTime) && other.startDateTime.isBefore(endDateTime);
    }

    public int getOverlapInMinutes(Timeslot other) {
        if (this.equals(other)) {
            return durationInMinutes;
        }
        LocalDateTime startMaximum = startDateTime.isBefore(other.startDateTime) ? other.startDateTime : startDateTime;
        LocalDateTime endMinimum = endDateTime.isBefore(other.endDateTime) ? endDateTime : other.endDateTime;
        return (int) Duration.between(startMaximum, endMinimum).toMinutes();
    }

    public boolean startsAfter(Timeslot other) {
        return !other.endDateTime.isAfter(startDateTime);
    }

    public boolean endsBefore(Timeslot other) {
        return !endDateTime.isAfter(other.startDateTime);
    }

    public boolean isOnSameDayAs(Timeslot other) {
        return startDateTime.toLocalDate().equals(other.getStartDateTime().toLocalDate());
    }

    public boolean pauseExists(Timeslot other, int pauseInMinutes) {
        if (this.overlapsTime(other)) {
            return false;
        }
        if (!this.isOnSameDayAs(other)) {
            return true;
        }
        Duration pause = startsAfter(other) ? Duration.between(other.getEndDateTime(), getStartDateTime())
                : Duration.between(getEndDateTime(), other.getStartDateTime());
        return pause.toMinutes() >= pauseInMinutes;
    }

    public String getId() {
        return id;
    }

    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }

    public Set<String> getTags() {
        return tags;
    }

    public int getDurationInMinutes() {
        return durationInMinutes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Timeslot timeslot)) {
            return false;
        }
        return Objects.equals(id, timeslot.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "%s - %s - %s".formatted(id, startDateTime, endDateTime);
    }
}
