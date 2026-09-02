package org.acme.meetingschedule.domain;

import java.util.Objects;

import ai.timefold.solver.core.api.domain.common.PlanningId;

/**
 * One person attending one meeting. The subtype ({@link RequiredAttendance} or {@link PreferredAttendance}) is what the
 * constraints match on, so both live in the solution's single attendance fact collection.
 */
public abstract class Attendance {

    @PlanningId
    private final String id;
    private final Meeting meeting;
    private final Person person;

    protected Attendance(String id, Meeting meeting, Person person) {
        this.id = id;
        this.meeting = meeting;
        this.person = person;
    }

    public String getId() {
        return id;
    }

    public Meeting getMeeting() {
        return meeting;
    }

    public Person getPerson() {
        return person;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Attendance attendance)) {
            return false;
        }
        return Objects.equals(id, attendance.id);
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
