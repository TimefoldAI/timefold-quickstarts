package org.acme.meetingschedule.domain;

public class PreferredAttendance extends Attendance {

    public PreferredAttendance(String id, Meeting meeting, Person person) {
        super(id, meeting, person);
    }
}
