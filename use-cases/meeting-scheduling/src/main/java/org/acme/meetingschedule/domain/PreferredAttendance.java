package org.acme.meetingschedule.domain;

public class PreferredAttendance extends Attendance {

    public PreferredAttendance() {
        super();
    }

    public PreferredAttendance(String id, Meeting meeting, Person person) {
        super(id, meeting, person);
    }

    @Override
    public boolean isRequired() {
        return false;
    }
}
