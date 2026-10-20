package org.acme.schooltimetabling.domain;

public final class TimetableConstraintProperties {

    public static final String ROOM_CONFLICT = "Room conflict";
    public static final String TEACHER_CONFLICT = "Teacher conflict";
    public static final String STUDENT_GROUP_CONFLICT = "Student group conflict";

    public static final String TEACHER_ROOM_STABILITY = "Teacher room stability";
    public static final String TEACHER_TIME_EFFICIENCY = "Teacher time efficiency";
    public static final String STUDENT_GROUP_SUBJECT_VARIETY = "Student group subject variety";

    private TimetableConstraintProperties() {
    }
}
