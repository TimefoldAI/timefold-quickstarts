package org.acme.meetingschedule.solver;

public enum ConstraintGroupTag {
    CONFLICT_FREE_PLANNING("conflict-free planning"),
    ATTENDANCE_SATISFACTION("attendance satisfaction"),
    SCHEDULE_EFFICIENCY("schedule efficiency");

    private final String tag;

    ConstraintGroupTag(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }
}
