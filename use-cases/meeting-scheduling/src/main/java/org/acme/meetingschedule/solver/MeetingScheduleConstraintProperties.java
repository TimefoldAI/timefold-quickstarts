package org.acme.meetingschedule.solver;

public final class MeetingScheduleConstraintProperties {

    public static final String ROOM_CONFLICT = "Room conflict";
    public static final String DONT_GO_IN_OVERTIME = "Don't go in overtime";
    public static final String REQUIRED_ATTENDANCE_CONFLICT = "Required attendance conflict";
    public static final String REQUIRED_ROOM_CAPACITY = "Required room capacity";
    public static final String START_AND_END_ON_SAME_DAY = "Start and end on same day";
    public static final String REQUIRED_AND_PREFERRED_ATTENDANCE_CONFLICT = "Required and preferred attendance conflict";
    public static final String PREFERRED_ATTENDANCE_CONFLICT = "Preferred attendance conflict";
    public static final String DO_ALL_MEETINGS_AS_SOON_AS_POSSIBLE = "Do all meetings as soon as possible";
    public static final String ONE_TIME_GRAIN_BREAK_BETWEEN_TWO_CONSECUTIVE_MEETINGS =
            "One TimeGrain break between two consecutive meetings";
    public static final String OVERLAPPING_MEETINGS = "Overlapping meetings";
    public static final String ASSIGN_LARGER_ROOMS_FIRST = "Assign larger rooms first";
    public static final String ROOM_STABILITY = "Room stability";

    private MeetingScheduleConstraintProperties() {
    }
}
