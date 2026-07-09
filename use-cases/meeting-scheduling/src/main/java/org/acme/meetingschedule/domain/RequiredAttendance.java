package org.acme.meetingschedule.domain;

public class RequiredAttendance extends Attendance {

    public RequiredAttendance() {
        super();
    }

    public RequiredAttendance(String id, Meeting meeting, Person person) {
        super(id, meeting, person);
    }

    @Override
    public boolean isRequired() {
        return true;
    }
}
