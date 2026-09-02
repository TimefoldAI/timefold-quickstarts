package org.acme.meetingschedule.solver;

import ai.timefold.solver.service.definition.api.description.ConstraintGroupInfo;

public final class MeetingScheduleConstraintGroup {

    public static final ConstraintGroupInfo ROOM_CONFLICTS = new ConstraintGroupInfo("roomConflicts",
            "Room conflicts",
            "Avoid double-booking a single room with two meetings that overlap in time.",
            "IconDoor",
            new String[] { "room conflicts" });

    public static final ConstraintGroupInfo SCHEDULING_WINDOW = new ConstraintGroupInfo("schedulingWindow",
            "Scheduling window",
            "Keep every meeting inside the scheduling horizon and finishing on the day it starts.",
            "IconClock",
            new String[] { "scheduling window" });

    public static final ConstraintGroupInfo ATTENDANCE_CONFLICTS = new ConstraintGroupInfo("attendanceConflicts",
            "Attendance conflicts",
            "Avoid asking a person to be in two overlapping meetings at once, whether their attendance is "
                    + "required or merely preferred.",
            "IconUsers",
            new String[] { "attendance conflicts" });

    public static final ConstraintGroupInfo ROOM_CAPACITY = new ConstraintGroupInfo("roomCapacity",
            "Room capacity",
            "Match each meeting to a room that seats everyone attending it, using the larger rooms first.",
            "IconArmchair",
            new String[] { "room capacity" });

    public static final ConstraintGroupInfo SCHEDULE_QUALITY = new ConstraintGroupInfo("scheduleQuality",
            "Schedule quality",
            "Hold the meetings early, spread them out in time, leave a break between back-to-back ones and keep "
                    + "an attendee in the same room.",
            "IconCalendarTime",
            new String[] { "schedule quality" });

    private MeetingScheduleConstraintGroup() {
    }
}
