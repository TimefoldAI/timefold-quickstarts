package org.acme.meetingschedule.domain.justification;

import ai.timefold.solver.service.definition.api.ModelConstraintJustification;

import org.acme.meetingschedule.domain.Attendance;
import org.acme.meetingschedule.domain.MeetingAssignment;
import org.acme.meetingschedule.domain.Room;
import org.acme.meetingschedule.domain.TimeGrain;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Common contract for every meeting scheduling justification.
 * <p>
 * Each implementation is a record dedicated to exactly one thing that is being justified, so that the Timefold Platform can
 * both render a human-readable {@link #getDescription() description} and expose the individual facts behind it through the
 * OpenAPI schema.
 * <p>
 * Every implementation must be listed in the {@link Schema#oneOf()} below, otherwise it does not show up in the generated
 * OpenAPI schema.
 */
@Schema(description = "Explains why a meeting scheduling constraint was matched.",
        oneOf = {
                // Hard constraints
                MeetingScheduleJustification.MeetingsOverlappingInSameRoomJustification.class,
                MeetingScheduleJustification.MeetingRunningPastTheHorizonJustification.class,
                MeetingScheduleJustification.RequiredAttendeeInOverlappingMeetingsJustification.class,
                MeetingScheduleJustification.RoomTooSmallForMeetingJustification.class,
                MeetingScheduleJustification.MeetingSpanningTwoDaysJustification.class,

                // Medium constraints
                MeetingScheduleJustification.RequiredAndPreferredAttendeeInOverlappingMeetingsJustification.class,
                MeetingScheduleJustification.PreferredAttendeeInOverlappingMeetingsJustification.class,

                // Soft constraints
                MeetingScheduleJustification.MeetingScheduledLateJustification.class,
                MeetingScheduleJustification.MeetingsWithoutBreakInBetweenJustification.class,
                MeetingScheduleJustification.MeetingsOverlappingInTimeJustification.class,
                MeetingScheduleJustification.LargerRoomAvailableJustification.class,
                MeetingScheduleJustification.AttendeeChangingRoomJustification.class
        })
public interface MeetingScheduleJustification extends ModelConstraintJustification {

    /**
     * @return never null, a human-readable explanation of the constraint match
     */
    String getDescription();

    /**
     * Exposes the description as the {@code description} property of {@link ModelConstraintJustification}.
     */
    default String description() {
        return getDescription();
    }

    // ************************************************************************
    // Hard constraints
    // ************************************************************************

    @Schema(description = "Two meetings share the same room while their time slots overlap.",
            allOf = { MeetingScheduleJustification.class })
    record MeetingsOverlappingInSameRoomJustification(
            @Schema(description = "The topic of the first meeting.") String meeting,
            @Schema(description = "The topic of the second meeting.") String otherMeeting,
            @Schema(description = "The name of the room both meetings are assigned to.") String room,
            @Schema(description = "The number of minutes during which both meetings overlap.") int overlapInMinutes)
            implements
                MeetingScheduleJustification {

        public static MeetingsOverlappingInSameRoomJustification of(MeetingAssignment assignment,
                MeetingAssignment otherAssignment) {
            return new MeetingsOverlappingInSameRoomJustification(assignment.getMeeting().topic(),
                    otherAssignment.getMeeting().topic(), assignment.getRoom().name(),
                    assignment.calculateOverlapInMinutes(otherAssignment));
        }

        @Override
        public String getDescription() {
            return "Meetings '%s' and '%s' share room '%s' and overlap for %d minute(s)."
                    .formatted(meeting, otherMeeting, room, overlapInMinutes);
        }
    }

    @Schema(description = "A meeting is scheduled so late that it does not finish within the scheduling horizon.",
            allOf = { MeetingScheduleJustification.class })
    record MeetingRunningPastTheHorizonJustification(
            @Schema(description = "The topic of the meeting.") String meeting,
            @Schema(description = "When the meeting starts, in ISO-8601 date and time format with an offset.") String startDateTime,
            @Schema(description = "The duration of the meeting, in minutes.") int durationInMinutes)
            implements
                MeetingScheduleJustification {

        public static MeetingRunningPastTheHorizonJustification of(MeetingAssignment assignment) {
            return new MeetingRunningPastTheHorizonJustification(assignment.getMeeting().topic(),
                    assignment.getStartDateTime().toString(), assignment.getDurationInMinutes());
        }

        @Override
        public String getDescription() {
            return "Meeting '%s' starts at %s and lasts %d minute(s), which runs past the end of the office hours."
                    .formatted(meeting, startDateTime, durationInMinutes);
        }
    }

    @Schema(description = "A person is a required attendee of two meetings whose time slots overlap.",
            allOf = { MeetingScheduleJustification.class })
    record RequiredAttendeeInOverlappingMeetingsJustification(
            @Schema(description = "The name of the double-booked attendee.") String attendee,
            @Schema(description = "The topic of the first meeting.") String meeting,
            @Schema(description = "The topic of the second meeting.") String otherMeeting,
            @Schema(description = "The number of minutes during which both meetings overlap.") int overlapInMinutes)
            implements
                MeetingScheduleJustification {

        public static RequiredAttendeeInOverlappingMeetingsJustification of(Attendance attendance,
                MeetingAssignment assignment, MeetingAssignment otherAssignment) {
            return new RequiredAttendeeInOverlappingMeetingsJustification(attendance.getPerson().fullName(),
                    assignment.getMeeting().topic(), otherAssignment.getMeeting().topic(),
                    assignment.calculateOverlapInMinutes(otherAssignment));
        }

        @Override
        public String getDescription() {
            return "'%s' is required in meetings '%s' and '%s', which overlap for %d minute(s)."
                    .formatted(attendee, meeting, otherMeeting, overlapInMinutes);
        }
    }

    @Schema(description = "A meeting is assigned a room that seats fewer people than are attending it.",
            allOf = { MeetingScheduleJustification.class })
    record RoomTooSmallForMeetingJustification(
            @Schema(description = "The topic of the meeting.") String meeting,
            @Schema(description = "The name of the room the meeting is assigned to, or null if unassigned.") String room,
            @Schema(description = "The number of people attending the meeting.") int requiredCapacity,
            @Schema(description = "The number of people the assigned room seats.") int roomCapacity)
            implements
                MeetingScheduleJustification {

        public static RoomTooSmallForMeetingJustification of(MeetingAssignment assignment) {
            Room room = assignment.getRoom();
            return new RoomTooSmallForMeetingJustification(assignment.getMeeting().topic(),
                    room == null ? null : room.name(), assignment.getRequiredCapacity(), assignment.getRoomCapacity());
        }

        @Override
        public String getDescription() {
            return "Meeting '%s' needs seats for %d attendee(s), but room '%s' only seats %d."
                    .formatted(meeting, requiredCapacity, room, roomCapacity);
        }
    }

    @Schema(description = "A meeting starts on one day and would only finish on the next.",
            allOf = { MeetingScheduleJustification.class })
    record MeetingSpanningTwoDaysJustification(
            @Schema(description = "The topic of the meeting.") String meeting,
            @Schema(description = "The date the meeting starts on, in ISO-8601 date format.") String startDate,
            @Schema(description = "The date the meeting would end on, in ISO-8601 date format.") String endDate)
            implements
                MeetingScheduleJustification {

        public static MeetingSpanningTwoDaysJustification of(MeetingAssignment assignment, TimeGrain lastTimeGrain) {
            return new MeetingSpanningTwoDaysJustification(assignment.getMeeting().topic(),
                    assignment.getStartingTimeGrain().getDate().toString(), lastTimeGrain.getDate().toString());
        }

        @Override
        public String getDescription() {
            return "Meeting '%s' starts on %s but would only end on %s.".formatted(meeting, startDate, endDate);
        }
    }

    // ************************************************************************
    // Medium constraints
    // ************************************************************************

    @Schema(description = "A person is required in one meeting and would prefer to attend another that overlaps it.",
            allOf = { MeetingScheduleJustification.class })
    record RequiredAndPreferredAttendeeInOverlappingMeetingsJustification(
            @Schema(description = "The name of the attendee.") String attendee,
            @Schema(description = "The topic of the meeting the attendee is required in.") String requiredMeeting,
            @Schema(description = "The topic of the meeting the attendee prefers to attend.") String preferredMeeting,
            @Schema(description = "The number of minutes during which both meetings overlap.") int overlapInMinutes)
            implements
                MeetingScheduleJustification {

        public static RequiredAndPreferredAttendeeInOverlappingMeetingsJustification of(Attendance attendance,
                MeetingAssignment requiredAssignment, MeetingAssignment preferredAssignment) {
            return new RequiredAndPreferredAttendeeInOverlappingMeetingsJustification(
                    attendance.getPerson().fullName(), requiredAssignment.getMeeting().topic(),
                    preferredAssignment.getMeeting().topic(),
                    requiredAssignment.calculateOverlapInMinutes(preferredAssignment));
        }

        @Override
        public String getDescription() {
            return "'%s' is required in meeting '%s' and prefers meeting '%s', which overlap for %d minute(s)."
                    .formatted(attendee, requiredMeeting, preferredMeeting, overlapInMinutes);
        }
    }

    @Schema(description = "A person prefers to attend two meetings whose time slots overlap.",
            allOf = { MeetingScheduleJustification.class })
    record PreferredAttendeeInOverlappingMeetingsJustification(
            @Schema(description = "The name of the attendee.") String attendee,
            @Schema(description = "The topic of the first meeting.") String meeting,
            @Schema(description = "The topic of the second meeting.") String otherMeeting,
            @Schema(description = "The number of minutes during which both meetings overlap.") int overlapInMinutes)
            implements
                MeetingScheduleJustification {

        public static PreferredAttendeeInOverlappingMeetingsJustification of(Attendance attendance,
                MeetingAssignment assignment, MeetingAssignment otherAssignment) {
            return new PreferredAttendeeInOverlappingMeetingsJustification(attendance.getPerson().fullName(),
                    assignment.getMeeting().topic(), otherAssignment.getMeeting().topic(),
                    assignment.calculateOverlapInMinutes(otherAssignment));
        }

        @Override
        public String getDescription() {
            return "'%s' prefers both meetings '%s' and '%s', which overlap for %d minute(s)."
                    .formatted(attendee, meeting, otherMeeting, overlapInMinutes);
        }
    }

    // ************************************************************************
    // Soft constraints
    // ************************************************************************

    @Schema(description = "A meeting could have been held earlier in the scheduling horizon.",
            allOf = { MeetingScheduleJustification.class })
    record MeetingScheduledLateJustification(
            @Schema(description = "The topic of the meeting.") String meeting,
            @Schema(description = "When the meeting ends, in ISO-8601 date and time format with an offset.") String endDateTime)
            implements
                MeetingScheduleJustification {

        public static MeetingScheduledLateJustification of(MeetingAssignment assignment) {
            return new MeetingScheduledLateJustification(assignment.getMeeting().topic(),
                    assignment.getEndDateTime().toString());
        }

        @Override
        public String getDescription() {
            return "Meeting '%s' only ends at %s, later in the schedule than necessary."
                    .formatted(meeting, endDateTime);
        }
    }

    @Schema(description = "Two meetings follow each other without a single free time slot in between.",
            allOf = { MeetingScheduleJustification.class })
    record MeetingsWithoutBreakInBetweenJustification(
            @Schema(description = "The topic of the meeting that ends first.") String meeting,
            @Schema(description = "The topic of the meeting that starts right after it.") String nextMeeting)
            implements
                MeetingScheduleJustification {

        public static MeetingsWithoutBreakInBetweenJustification of(MeetingAssignment assignment,
                MeetingAssignment nextAssignment) {
            return new MeetingsWithoutBreakInBetweenJustification(assignment.getMeeting().topic(),
                    nextAssignment.getMeeting().topic());
        }

        @Override
        public String getDescription() {
            return "Meeting '%s' is immediately followed by meeting '%s', without a free time slot in between."
                    .formatted(meeting, nextMeeting);
        }
    }

    @Schema(description = "Two meetings run in parallel.", allOf = { MeetingScheduleJustification.class })
    record MeetingsOverlappingInTimeJustification(
            @Schema(description = "The topic of the first meeting.") String meeting,
            @Schema(description = "The topic of the second meeting.") String otherMeeting,
            @Schema(description = "The number of minutes during which both meetings overlap.") int overlapInMinutes)
            implements
                MeetingScheduleJustification {

        public static MeetingsOverlappingInTimeJustification of(MeetingAssignment assignment,
                MeetingAssignment otherAssignment) {
            return new MeetingsOverlappingInTimeJustification(assignment.getMeeting().topic(),
                    otherAssignment.getMeeting().topic(), assignment.calculateOverlapInMinutes(otherAssignment));
        }

        @Override
        public String getDescription() {
            return "Meetings '%s' and '%s' run in parallel for %d minute(s)."
                    .formatted(meeting, otherMeeting, overlapInMinutes);
        }
    }

    @Schema(description = "A meeting is held in a smaller room while a larger one exists.",
            allOf = { MeetingScheduleJustification.class })
    record LargerRoomAvailableJustification(
            @Schema(description = "The topic of the meeting.") String meeting,
            @Schema(description = "The name of the room the meeting is assigned to.") String room,
            @Schema(description = "The number of people the assigned room seats.") int roomCapacity,
            @Schema(description = "The name of the larger room.") String largerRoom,
            @Schema(description = "The number of people the larger room seats.") int largerRoomCapacity)
            implements
                MeetingScheduleJustification {

        public static LargerRoomAvailableJustification of(MeetingAssignment assignment, Room largerRoom) {
            return new LargerRoomAvailableJustification(assignment.getMeeting().topic(), assignment.getRoom().name(),
                    assignment.getRoomCapacity(), largerRoom.name(), largerRoom.capacity());
        }

        @Override
        public String getDescription() {
            return "Meeting '%s' is held in room '%s' (seats %d), while the larger room '%s' (seats %d) exists."
                    .formatted(meeting, room, roomCapacity, largerRoom, largerRoomCapacity);
        }
    }

    @Schema(description = "An attendee has to change room between two meetings that follow each other closely.",
            allOf = { MeetingScheduleJustification.class })
    record AttendeeChangingRoomJustification(
            @Schema(description = "The name of the attendee.") String attendee,
            @Schema(description = "The topic of the meeting that ends first.") String meeting,
            @Schema(description = "The name of the room that meeting is held in.") String room,
            @Schema(description = "The topic of the meeting that follows it.") String nextMeeting,
            @Schema(description = "The name of the room the following meeting is held in.") String nextRoom)
            implements
                MeetingScheduleJustification {

        public static AttendeeChangingRoomJustification of(Attendance attendance, MeetingAssignment assignment,
                MeetingAssignment nextAssignment) {
            return new AttendeeChangingRoomJustification(attendance.getPerson().fullName(),
                    assignment.getMeeting().topic(), assignment.getRoom().name(),
                    nextAssignment.getMeeting().topic(), nextAssignment.getRoom().name());
        }

        @Override
        public String getDescription() {
            return "'%s' has to move from room '%s' after meeting '%s' to room '%s' for meeting '%s'."
                    .formatted(attendee, room, meeting, nextRoom, nextMeeting);
        }
    }
}
