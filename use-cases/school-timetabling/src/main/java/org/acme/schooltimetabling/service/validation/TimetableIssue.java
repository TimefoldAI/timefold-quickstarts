package org.acme.schooltimetabling.service.validation;

import java.util.List;

import ai.timefold.solver.service.definition.api.validation.AbstractIssue;
import ai.timefold.solver.service.definition.api.validation.IssueCode;
import ai.timefold.solver.service.definition.api.validation.IssueMetadata;
import ai.timefold.solver.service.definition.api.validation.IssueSeverity;
import ai.timefold.solver.service.definition.api.validation.metadata.IssueMessage;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A dataset validation issue reported for a school timetabling input.",
        oneOf = {
                TimetableIssue.DuplicateTimeslotIdIssue.class,
                TimetableIssue.DuplicateRoomIdIssue.class,
                TimetableIssue.DuplicateLessonIdIssue.class,
                TimetableIssue.InvalidTimeslotTimeRangeIssue.class,
                TimetableIssue.NonExistingTimeslotReferenceIssue.class,
                TimetableIssue.NonExistingRoomReferenceIssue.class,
                TimetableIssue.InsufficientRoomCapacityIssue.class,
                TimetableIssue.OverloadedTeacherIssue.class,
                TimetableIssue.OverloadedStudentGroupIssue.class
        })
public abstract class TimetableIssue extends AbstractIssue {

    protected TimetableIssue(IssueCode code, IssueSeverity severity, List<IssueMetadata> metadata) {
        super(code, severity, metadata);
    }

    @Schema(allOf = { TimetableIssue.class })
    public static class DuplicateTimeslotIdIssue extends TimetableIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("DUPLICATE_TIMESLOT_ID");
        public static final IssueMessage ISSUE_MESSAGE = new IssueMessage("Duplicate timeslot ID found.");

        @Schema(description = "The ID of the duplicated timeslot.")
        private String timeslotId;

        public DuplicateTimeslotIdIssue() {
            this(null);
        }

        public DuplicateTimeslotIdIssue(String timeslotId) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.timeslotId = timeslotId;
        }

        public String getTimeslotId() {
            return timeslotId;
        }
    }

    @Schema(allOf = { TimetableIssue.class })
    public static class DuplicateRoomIdIssue extends TimetableIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("DUPLICATE_ROOM_ID");
        public static final IssueMessage ISSUE_MESSAGE = new IssueMessage("Duplicate room ID found.");

        @Schema(description = "The ID of the duplicated room.")
        private String roomId;

        public DuplicateRoomIdIssue() {
            this(null);
        }

        public DuplicateRoomIdIssue(String roomId) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.roomId = roomId;
        }

        public String getRoomId() {
            return roomId;
        }
    }

    @Schema(allOf = { TimetableIssue.class })
    public static class DuplicateLessonIdIssue extends TimetableIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("DUPLICATE_LESSON_ID");
        public static final IssueMessage ISSUE_MESSAGE = new IssueMessage("Duplicate lesson ID found.");

        @Schema(description = "The ID of the duplicated lesson.")
        private String lessonId;

        public DuplicateLessonIdIssue() {
            this(null);
        }

        public DuplicateLessonIdIssue(String lessonId) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.lessonId = lessonId;
        }

        public String getLessonId() {
            return lessonId;
        }
    }

    /**
     * The constraints that look for consecutive lessons measure the break from one timeslot's end to the next
     * one's start, which is meaningless for a timeslot that ends before it starts.
     */
    @Schema(allOf = { TimetableIssue.class })
    public static class InvalidTimeslotTimeRangeIssue extends TimetableIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("INVALID_TIMESLOT_TIME_RANGE");
        public static final IssueMessage ISSUE_MESSAGE =
                new IssueMessage("Timeslot does not end after it starts.");

        @Schema(description = "The ID of the timeslot whose end time is not after its start time.")
        private String timeslotId;

        public InvalidTimeslotTimeRangeIssue() {
            this(null);
        }

        public InvalidTimeslotTimeRangeIssue(String timeslotId) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.timeslotId = timeslotId;
        }

        public String getTimeslotId() {
            return timeslotId;
        }
    }

    @Schema(allOf = { TimetableIssue.class })
    public static class NonExistingTimeslotReferenceIssue extends TimetableIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("NON_EXISTING_TIMESLOT_REFERENCE");
        public static final IssueMessage ISSUE_MESSAGE =
                new IssueMessage("Lesson refers to a timeslot ID that does not exist.");

        @Schema(description = "The ID of the lesson with the unknown timeslot reference.")
        private String lessonId;

        public NonExistingTimeslotReferenceIssue() {
            this(null);
        }

        public NonExistingTimeslotReferenceIssue(String lessonId) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.lessonId = lessonId;
        }

        public String getLessonId() {
            return lessonId;
        }
    }

    @Schema(allOf = { TimetableIssue.class })
    public static class NonExistingRoomReferenceIssue extends TimetableIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("NON_EXISTING_ROOM_REFERENCE");
        public static final IssueMessage ISSUE_MESSAGE =
                new IssueMessage("Lesson refers to a room ID that does not exist.");

        @Schema(description = "The ID of the lesson with the unknown room reference.")
        private String lessonId;

        public NonExistingRoomReferenceIssue() {
            this(null);
        }

        public NonExistingRoomReferenceIssue(String lessonId) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.lessonId = lessonId;
        }

        public String getLessonId() {
            return lessonId;
        }
    }

    /**
     * Every room holds one lesson per timeslot, so a dataset with more lessons than room-timeslot combinations
     * can never be scheduled without a room conflict. Reporting that up front beats letting the solver spend its
     * entire time budget on a problem that can never reach a feasible score.
     */
    @Schema(allOf = { TimetableIssue.class })
    public static class InsufficientRoomCapacityIssue extends TimetableIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("INSUFFICIENT_ROOM_CAPACITY");
        public static final IssueMessage ISSUE_MESSAGE =
                new IssueMessage("There are more lessons than room and timeslot combinations to hold them.");

        @Schema(description = "The number of lessons in the dataset.")
        private int lessonCount;
        @Schema(description = "The number of rooms times the number of timeslots in the dataset.")
        private int capacity;

        public InsufficientRoomCapacityIssue() {
            this(0, 0);
        }

        public InsufficientRoomCapacityIssue(int lessonCount, int capacity) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.lessonCount = lessonCount;
            this.capacity = capacity;
        }

        public int getLessonCount() {
            return lessonCount;
        }

        public int getCapacity() {
            return capacity;
        }
    }

    /**
     * A teacher gives one lesson per timeslot at most, so one with more lessons than there are timeslots can never
     * be scheduled without a teacher conflict.
     */
    @Schema(allOf = { TimetableIssue.class })
    public static class OverloadedTeacherIssue extends TimetableIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("OVERLOADED_TEACHER");
        public static final IssueMessage ISSUE_MESSAGE =
                new IssueMessage("Teacher has more lessons than there are timeslots.");

        @Schema(description = "The overloaded teacher.")
        private String teacher;
        @Schema(description = "The number of lessons the teacher gives.")
        private int lessonCount;

        public OverloadedTeacherIssue() {
            this(null, 0);
        }

        public OverloadedTeacherIssue(String teacher, int lessonCount) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.teacher = teacher;
            this.lessonCount = lessonCount;
        }

        public String getTeacher() {
            return teacher;
        }

        public int getLessonCount() {
            return lessonCount;
        }
    }

    /**
     * A student group attends one lesson per timeslot at most, so one with more lessons than there are timeslots
     * can never be scheduled without a student group conflict.
     */
    @Schema(allOf = { TimetableIssue.class })
    public static class OverloadedStudentGroupIssue extends TimetableIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("OVERLOADED_STUDENT_GROUP");
        public static final IssueMessage ISSUE_MESSAGE =
                new IssueMessage("Student group has more lessons than there are timeslots.");

        @Schema(description = "The overloaded student group.")
        private String studentGroup;
        @Schema(description = "The number of lessons the student group attends.")
        private int lessonCount;

        public OverloadedStudentGroupIssue() {
            this(null, 0);
        }

        public OverloadedStudentGroupIssue(String studentGroup, int lessonCount) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.studentGroup = studentGroup;
            this.lessonCount = lessonCount;
        }

        public String getStudentGroup() {
            return studentGroup;
        }

        public int getLessonCount() {
            return lessonCount;
        }
    }
}
