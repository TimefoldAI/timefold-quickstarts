package org.acme.meetingschedule.domain;

import java.util.Objects;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.entity.PlanningPin;
import ai.timefold.solver.core.api.domain.common.PlanningId;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;

import com.fasterxml.jackson.annotation.JsonIgnore;

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

    public MeetingAssignment(String id) {
        this.id = id;
    }

    public MeetingAssignment(String id, Meeting meeting) {
        this(id);
        this.meeting = meeting;
    }

    public MeetingAssignment(String id, Meeting meeting, TimeGrain startingTimeGrain, Room room) {
        this(id, meeting);
        this.startingTimeGrain = startingTimeGrain;
        this.room = room;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Meeting getMeeting() {
        return meeting;
    }

    public void setMeeting(Meeting meeting) {
        this.meeting = meeting;
    }

    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
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

    // ************************************************************************
    // Complex methods
    // ************************************************************************

    @JsonIgnore
    public int getGrainIndex() {
        return getStartingTimeGrain().getGrainIndex();
    }

    @JsonIgnore
    public int calculateOverlap(MeetingAssignment other) {
        if (startingTimeGrain == null || other.getStartingTimeGrain() == null) {
            return 0;
        }
        // start is inclusive, end is exclusive
        int start = startingTimeGrain.getGrainIndex();
        int end = getLastTimeGrainIndex() + 1;
        int otherStart = other.startingTimeGrain.getGrainIndex();
        int otherEnd = other.getLastTimeGrainIndex() + 1;
        if (otherEnd < start) {
            return 0;
        }
        if (end < otherStart) {
            return 0;
        }
        return Math.min(end, otherEnd) - Math.max(start, otherStart);
    }

    @JsonIgnore
    public Integer getLastTimeGrainIndex() {
        if (startingTimeGrain == null) {
            return null;
        }
        return startingTimeGrain.getGrainIndex() + meeting.getDurationInGrains() - 1;
    }

    @JsonIgnore
    public int getRoomCapacity() {
        if (room == null) {
            return 0;
        }
        return room.getCapacity();
    }

    @JsonIgnore
    public int getRequiredCapacity() {
        return meeting.getRequiredCapacity();
    }

    @Override
    public String toString() {
        return meeting.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof MeetingAssignment that))
            return false;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }
}
