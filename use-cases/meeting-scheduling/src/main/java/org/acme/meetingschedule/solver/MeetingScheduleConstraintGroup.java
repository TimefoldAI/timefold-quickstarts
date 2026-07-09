package org.acme.meetingschedule.solver;

import ai.timefold.solver.service.definition.api.description.ConstraintGroupInfo;

public final class MeetingScheduleConstraintGroup {
    public static final ConstraintGroupInfo CONFLICT_AVOIDANCE = new ConstraintGroupInfo("conflictAvoidance",
            "Conflict avoidance",
            "Ensure no room or attendee is double-booked and meetings fit within a single day.",
            "IconDiamond",
            new String[] { ConstraintGroupTag.CONFLICT_FREE_PLANNING.getTag() });
    public static final ConstraintGroupInfo ATTENDANCE_PREFERENCES = new ConstraintGroupInfo("attendancePreferences",
            "Attendance preferences",
            "Avoid clashes for required and preferred attendees.",
            "IconUser",
            new String[] { ConstraintGroupTag.ATTENDANCE_SATISFACTION.getTag() });
    public static final ConstraintGroupInfo SCHEDULE_QUALITY = new ConstraintGroupInfo("scheduleQuality",
            "Schedule quality",
            "Schedule meetings early, avoid overlaps and keep attendees in stable rooms.",
            "IconClock",
            new String[] { ConstraintGroupTag.SCHEDULE_EFFICIENCY.getTag() });

    private MeetingScheduleConstraintGroup() {
    }
}
