package org.acme.conferencescheduling.domain;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;

import ai.timefold.solver.core.api.domain.lookup.PlanningId;

public class Timeslot {

    @PlanningId
    private String id;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private Set<TalkType> talkTypes;
    private Set<String> tags;

    // Cached
    private int durationInMinutes;

    public Timeslot() {
    }

    public Timeslot(String id, LocalDateTime startDateTime, LocalDateTime endDateTime, Set<TalkType> talkTypes,
            Set<String> tags) {
        this.id = id;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.talkTypes = talkTypes;
        this.tags = tags;
        durationInMinutes = (startDateTime == null || endDateTime == null) ? 0
                : (int) Duration.between(startDateTime, endDateTime).toMinutes();
        // Update compatible timeslots
        talkTypes.forEach(t -> t.addCompatibleTimeslot(this));
    }

    public boolean overlapsTime(Timeslot other) {
        if (this == other) {
            return true;
        }
        return startDateTime.isBefore(other.endDateTime) && other.startDateTime.isBefore(endDateTime);
    }

    public int getOverlapInMinutes(Timeslot other) {
        if (this == other) {
            return durationInMinutes;
        }
        LocalDateTime startMaximum = (startDateTime.isBefore(other.startDateTime)) ? other.startDateTime : startDateTime;
        LocalDateTime endMinimum = (endDateTime.isBefore(other.endDateTime)) ? endDateTime : other.endDateTime;
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

    public void setId(String id) {
        this.id = id;
    }

    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(LocalDateTime startDateTime) {
        this.startDateTime = startDateTime;
    }

    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(LocalDateTime endDateTime) {
        this.endDateTime = endDateTime;
    }

    public Set<TalkType> getTalkTypes() {
        return talkTypes;
    }

    public void setTalkTypes(Set<TalkType> talkTypes) {
        this.talkTypes = talkTypes;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public int getDurationInMinutes() {
        return durationInMinutes;
    }

    public void setDurationInMinutes(int durationInMinutes) {
        this.durationInMinutes = durationInMinutes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Timeslot timeslot))
            return false;
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
