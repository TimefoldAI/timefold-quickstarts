package org.acme.meetingschedule.domain;

public class PreferredAttendance extends Attendance {

    public PreferredAttendance() {
    }

    public PreferredAttendance(String id, Meeting meeting) {
        super(id, meeting);
    }

    public PreferredAttendance(String id, Meeting meeting, Person person) {
        super(id, meeting);
        setPerson(person);
    }
}
