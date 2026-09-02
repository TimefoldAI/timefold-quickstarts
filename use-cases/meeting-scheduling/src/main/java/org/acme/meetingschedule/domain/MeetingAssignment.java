package org.acme.meetingschedule.domain;

import java.time.OffsetDateTime;
import java.util.Objects;

import ai.timefold.solver.core.api.domain.common.PlanningId;
import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.entity.PlanningPin;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;

/**
 * The planning entity: it decides in which {@link Room} and at which {@link TimeGrain} its {@link Meeting} takes place.
 * There is exactly one assignment per meeting.
 */
@PlanningEntity
public class MeetingAssignment {

    @PlanningId
    private String id;
    private Meeting meeting;
    @PlanningPin
    private boolean pinned;

    // Planning variables: changes during planning, between score calculations.
    @PlanningVariable
    private TimeGrain startingTimeGrain;
    @PlanningVariable
    private Room room;

    public MeetingAssignment() {
    }

    public MeetingAssignment(String id, Meeting meeting) {
        this.id = id;
        this.meeting = meeting;
    }

    public MeetingAssignment(String id, Meeting meeting, TimeGrain startingTimeGrain, Room room, boolean pinned) {
        this(id, meeting);
        this.startingTimeGrain = startingTimeGrain;
        this.room = room;
        this.pinned = pinned;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

    public int getGrainIndex() {
        return startingTimeGrain.grainIndex();
    }

    /**
     * The time grains are an internal discretization of the office hours, so anything reported back to the client
     * (the model output, the constraint justifications) is expressed with these three instead.
     */
    public OffsetDateTime getStartDateTime() {
        return startingTimeGrain == null ? null : startingTimeGrain.startDateTime();
    }

    public OffsetDateTime getEndDateTime() {
        return startingTimeGrain == null ? null
                : startingTimeGrain.startDateTime().plusMinutes(getDurationInMinutes());
    }

    public int getDurationInMinutes() {
        return startingTimeGrain == null ? 0 : meeting.durationInGrains() * startingTimeGrain.lengthInMinutes();
    }

    /**
     * @return the number of minutes during which both assignments are running, 0 if they never overlap
     */
    public int calculateOverlapInMinutes(MeetingAssignment other) {
        return startingTimeGrain == null ? 0 : calculateOverlap(other) * startingTimeGrain.lengthInMinutes();
    }

    /**
     * @return the number of time grains during which both assignments are running, 0 if they never overlap
     */
    public int calculateOverlap(MeetingAssignment other) {
        if (startingTimeGrain == null || other.startingTimeGrain == null) {
            return 0;
        }
        // start is inclusive, end is exclusive
        int start = startingTimeGrain.grainIndex();
        int end = getLastTimeGrainIndex() + 1;
        int otherStart = other.startingTimeGrain.grainIndex();
        int otherEnd = other.getLastTimeGrainIndex() + 1;
        if (otherEnd < start) {
            return 0;
        }
        if (end < otherStart) {
            return 0;
        }
        return Math.min(end, otherEnd) - Math.max(start, otherStart);
    }

    /**
     * @return the index of the last grain this meeting occupies, null while it has no starting time grain yet
     */
    public Integer getLastTimeGrainIndex() {
        if (startingTimeGrain == null) {
            return null;
        }
        return startingTimeGrain.grainIndex() + meeting.durationInGrains() - 1;
    }

    public int getRoomCapacity() {
        if (room == null) {
            return 0;
        }
        return room.capacity();
    }

    public int getRequiredCapacity() {
        return meeting.requiredCapacity();
    }

    // ************************************************************************
    // Simple getters and setters
    // ************************************************************************

    public String getId() {
        return id;
    }

    public Meeting getMeeting() {
        return meeting;
    }

    public boolean isPinned() {
        return pinned;
    }

    public TimeGrain getStartingTimeGrain() {
        return startingTimeGrain;
    }

    public void setStartingTimeGrain(TimeGrain startingTimeGrain) {
        this.startingTimeGrain = startingTimeGrain;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MeetingAssignment that)) {
            return false;
        }
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return id;
    }
}
