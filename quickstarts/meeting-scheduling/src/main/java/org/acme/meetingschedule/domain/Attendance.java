package org.acme.meetingschedule.domain;

import ai.timefold.solver.core.api.domain.common.PlanningId;

public abstract class Attendance {

    @PlanningId
    private String id;
    private Person person;
    private Meeting meeting;

    protected Attendance() {
    }

    protected Attendance(String id) {
        this.id = id;
    }

    protected Attendance(String id, Meeting meeting) {
        this(id);
        this.meeting = meeting;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public Meeting getMeeting() {
        return meeting;
    }

    public void setMeeting(Meeting meeting) {
        this.meeting = meeting;
    }

    @Override
    public String toString() {
        return person + "-" + meeting;
    }
}
