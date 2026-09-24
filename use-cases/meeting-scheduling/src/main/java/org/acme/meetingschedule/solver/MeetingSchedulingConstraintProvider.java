package org.acme.meetingschedule.solver;

import static ai.timefold.solver.core.api.score.stream.Joiners.equal;
import static ai.timefold.solver.core.api.score.stream.Joiners.filtering;
import static ai.timefold.solver.core.api.score.stream.Joiners.greaterThan;
import static ai.timefold.solver.core.api.score.stream.Joiners.lessThan;
import static ai.timefold.solver.core.api.score.stream.Joiners.overlapping;

import java.util.Objects;

import ai.timefold.solver.core.api.score.HardMediumSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.service.definition.api.description.ConstraintInfo;

import org.acme.meetingschedule.domain.Attendance;
import org.acme.meetingschedule.domain.MeetingAssignment;
import org.acme.meetingschedule.domain.MeetingScheduleConstraintProperties;
import org.acme.meetingschedule.domain.PreferredAttendance;
import org.acme.meetingschedule.domain.RequiredAttendance;
import org.acme.meetingschedule.domain.Room;
import org.acme.meetingschedule.domain.TimeGrain;
import org.acme.meetingschedule.domain.justification.MeetingScheduleJustification.AttendeeChangingRoomJustification;
import org.acme.meetingschedule.domain.justification.MeetingScheduleJustification.LargerRoomAvailableJustification;
import org.acme.meetingschedule.domain.justification.MeetingScheduleJustification.MeetingRunningPastTheHorizonJustification;
import org.acme.meetingschedule.domain.justification.MeetingScheduleJustification.MeetingScheduledLateJustification;
import org.acme.meetingschedule.domain.justification.MeetingScheduleJustification.MeetingSpanningTwoDaysJustification;
import org.acme.meetingschedule.domain.justification.MeetingScheduleJustification.MeetingsOverlappingInSameRoomJustification;
import org.acme.meetingschedule.domain.justification.MeetingScheduleJustification.MeetingsOverlappingInTimeJustification;
import org.acme.meetingschedule.domain.justification.MeetingScheduleJustification.MeetingsWithoutBreakInBetweenJustification;
import org.acme.meetingschedule.domain.justification.MeetingScheduleJustification.PreferredAttendeeInOverlappingMeetingsJustification;
import org.acme.meetingschedule.domain.justification.MeetingScheduleJustification.RequiredAndPreferredAttendeeInOverlappingMeetingsJustification;
import org.acme.meetingschedule.domain.justification.MeetingScheduleJustification.RequiredAttendeeInOverlappingMeetingsJustification;
import org.acme.meetingschedule.domain.justification.MeetingScheduleJustification.RoomTooSmallForMeetingJustification;

public class MeetingSchedulingConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
                // Hard constraints
                roomConflict(constraintFactory),
                avoidOvertime(constraintFactory),
                requiredAttendanceConflict(constraintFactory),
                requiredRoomCapacity(constraintFactory),
                startAndEndOnSameDay(constraintFactory),

                // Medium constraints
                requiredAndPreferredAttendanceConflict(constraintFactory),
                preferredAttendanceConflict(constraintFactory),

                // Soft constraints
                doMeetingsAsSoonAsPossible(constraintFactory),
                oneBreakBetweenConsecutiveMeetings(constraintFactory),
                overlappingMeetings(constraintFactory),
                assignLargerRoomsFirst(constraintFactory),
                roomStability(constraintFactory)
        };
    }

    // ************************************************************************
    // Hard constraints
    // ************************************************************************

    public Constraint roomConflict(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachUniquePair(MeetingAssignment.class,
                equal(MeetingAssignment::getRoom),
                overlapping(MeetingAssignment::getGrainIndex, assignment -> assignment.getLastTimeGrainIndex() + 1))
                .penalize(HardMediumSoftScore.ONE_HARD,
                        (leftAssignment, rightAssignment) -> rightAssignment.calculateOverlap(leftAssignment))
                .justifyWith((leftAssignment, rightAssignment, score) -> MeetingsOverlappingInSameRoomJustification
                        .of(leftAssignment, rightAssignment))
                .asConstraint(new ConstraintInfo(MeetingScheduleConstraintProperties.ROOM_CONFLICT,
                        MeetingScheduleConstraintProperties.ROOM_CONFLICT,
                        "Two meetings must not be held in the same room at the same time.",
                        MeetingScheduleConstraintGroup.ROOM_CONFLICTS));
    }

    public Constraint avoidOvertime(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachIncludingUnassigned(MeetingAssignment.class)
                .filter(meetingAssignment -> meetingAssignment.getStartingTimeGrain() != null)
                .ifNotExists(TimeGrain.class,
                        equal(MeetingAssignment::getLastTimeGrainIndex, TimeGrain::grainIndex))
                .penalize(HardMediumSoftScore.ONE_HARD, MeetingAssignment::getLastTimeGrainIndex)
                .justifyWith((meetingAssignment, score) -> MeetingRunningPastTheHorizonJustification
                        .of(meetingAssignment))
                .asConstraint(new ConstraintInfo(MeetingScheduleConstraintProperties.DONT_GO_IN_OVERTIME,
                        MeetingScheduleConstraintProperties.DONT_GO_IN_OVERTIME,
                        "A meeting must finish within the office hours of its day.",
                        MeetingScheduleConstraintGroup.SCHEDULING_WINDOW));
    }

    public Constraint requiredAttendanceConflict(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachUniquePair(RequiredAttendance.class,
                equal(RequiredAttendance::getPerson))
                .join(MeetingAssignment.class,
                        equal((leftRequiredAttendance, rightRequiredAttendance) -> leftRequiredAttendance.getMeeting(),
                                MeetingAssignment::getMeeting))
                .join(MeetingAssignment.class,
                        equal((leftRequiredAttendance, rightRequiredAttendance, leftAssignment) -> rightRequiredAttendance
                                .getMeeting(),
                                MeetingAssignment::getMeeting),
                        overlapping((attendee1, attendee2, assignment) -> assignment.getGrainIndex(),
                                (attendee1, attendee2, assignment) -> assignment.getLastTimeGrainIndex() + 1,
                                MeetingAssignment::getGrainIndex,
                                assignment -> assignment.getLastTimeGrainIndex() + 1))
                .penalize(HardMediumSoftScore.ONE_HARD,
                        (leftRequiredAttendance, rightRequiredAttendance, leftAssignment, rightAssignment) -> rightAssignment
                                .calculateOverlap(leftAssignment))
                .justifyWith((leftRequiredAttendance, rightRequiredAttendance, leftAssignment, rightAssignment,
                        score) -> RequiredAttendeeInOverlappingMeetingsJustification.of(leftRequiredAttendance,
                                leftAssignment, rightAssignment))
                .asConstraint(new ConstraintInfo(MeetingScheduleConstraintProperties.REQUIRED_ATTENDANCE_CONFLICT,
                        MeetingScheduleConstraintProperties.REQUIRED_ATTENDANCE_CONFLICT,
                        "A required attendee must not be expected in two meetings at the same time.",
                        MeetingScheduleConstraintGroup.ATTENDANCE_CONFLICTS));
    }

    public Constraint requiredRoomCapacity(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachIncludingUnassigned(MeetingAssignment.class)
                .filter(meetingAssignment -> meetingAssignment.getRequiredCapacity() > meetingAssignment.getRoomCapacity())
                .penalize(HardMediumSoftScore.ONE_HARD,
                        meetingAssignment -> meetingAssignment.getRequiredCapacity() - meetingAssignment.getRoomCapacity())
                .justifyWith((meetingAssignment, score) -> RoomTooSmallForMeetingJustification.of(meetingAssignment))
                .asConstraint(new ConstraintInfo(MeetingScheduleConstraintProperties.REQUIRED_ROOM_CAPACITY,
                        MeetingScheduleConstraintProperties.REQUIRED_ROOM_CAPACITY,
                        "A meeting's room must seat every attendee of that meeting.",
                        MeetingScheduleConstraintGroup.ROOM_CAPACITY));
    }

    public Constraint startAndEndOnSameDay(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachIncludingUnassigned(MeetingAssignment.class)
                .filter(meetingAssignment -> meetingAssignment.getStartingTimeGrain() != null)
                .join(TimeGrain.class,
                        equal(MeetingAssignment::getLastTimeGrainIndex, TimeGrain::grainIndex),
                        filtering((meetingAssignment,
                                timeGrain) -> !meetingAssignment.getStartingTimeGrain().getDate()
                                        .equals(timeGrain.getDate())))
                .penalize(HardMediumSoftScore.ONE_HARD)
                .justifyWith((meetingAssignment, timeGrain, score) -> MeetingSpanningTwoDaysJustification
                        .of(meetingAssignment, timeGrain))
                .asConstraint(new ConstraintInfo(MeetingScheduleConstraintProperties.START_AND_END_ON_SAME_DAY,
                        MeetingScheduleConstraintProperties.START_AND_END_ON_SAME_DAY,
                        "A meeting must start and end on the same day.",
                        MeetingScheduleConstraintGroup.SCHEDULING_WINDOW));
    }

    // ************************************************************************
    // Medium constraints
    // ************************************************************************

    public Constraint requiredAndPreferredAttendanceConflict(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(RequiredAttendance.class)
                .join(PreferredAttendance.class,
                        equal(RequiredAttendance::getPerson, PreferredAttendance::getPerson))
                .join(constraintFactory.forEachIncludingUnassigned(MeetingAssignment.class)
                        .filter(assignment -> assignment.getStartingTimeGrain() != null),
                        equal((requiredAttendance, preferredAttendance) -> requiredAttendance.getMeeting(),
                                MeetingAssignment::getMeeting))
                .join(constraintFactory.forEachIncludingUnassigned(MeetingAssignment.class)
                        .filter(assignment -> assignment.getStartingTimeGrain() != null),
                        equal((requiredAttendance, preferredAttendance, leftAssignment) -> preferredAttendance.getMeeting(),
                                MeetingAssignment::getMeeting),
                        overlapping((attendee1, attendee2, assignment) -> assignment.getGrainIndex(),
                                (attendee1, attendee2, assignment) -> assignment.getLastTimeGrainIndex() + 1,
                                MeetingAssignment::getGrainIndex,
                                assignment -> assignment.getLastTimeGrainIndex() + 1))
                .penalize(HardMediumSoftScore.ONE_MEDIUM,
                        (requiredAttendance, preferredAttendance, leftAssignment, rightAssignment) -> rightAssignment
                                .calculateOverlap(leftAssignment))
                .justifyWith((requiredAttendance, preferredAttendance, leftAssignment, rightAssignment,
                        score) -> RequiredAndPreferredAttendeeInOverlappingMeetingsJustification.of(requiredAttendance,
                                leftAssignment, rightAssignment))
                .asConstraint(
                        new ConstraintInfo(
                                MeetingScheduleConstraintProperties.REQUIRED_AND_PREFERRED_ATTENDANCE_CONFLICT,
                                MeetingScheduleConstraintProperties.REQUIRED_AND_PREFERRED_ATTENDANCE_CONFLICT,
                                "A person required in one meeting should not have to skip another meeting they would "
                                        + "prefer to attend.",
                                MeetingScheduleConstraintGroup.ATTENDANCE_CONFLICTS));
    }

    public Constraint preferredAttendanceConflict(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachUniquePair(PreferredAttendance.class,
                equal(PreferredAttendance::getPerson))
                .join(constraintFactory.forEachIncludingUnassigned(MeetingAssignment.class)
                        .filter(assignment -> assignment.getStartingTimeGrain() != null),
                        equal((leftAttendance, rightAttendance) -> leftAttendance.getMeeting(),
                                MeetingAssignment::getMeeting))
                .join(constraintFactory.forEachIncludingUnassigned(MeetingAssignment.class)
                        .filter(assignment -> assignment.getStartingTimeGrain() != null),
                        equal((leftAttendance, rightAttendance, leftAssignment) -> rightAttendance.getMeeting(),
                                MeetingAssignment::getMeeting),
                        overlapping((attendee1, attendee2, assignment) -> assignment.getGrainIndex(),
                                (attendee1, attendee2, assignment) -> assignment.getLastTimeGrainIndex() + 1,
                                MeetingAssignment::getGrainIndex,
                                assignment -> assignment.getLastTimeGrainIndex() + 1))
                .penalize(HardMediumSoftScore.ONE_MEDIUM,
                        (leftPreferredAttendance, rightPreferredAttendance, leftAssignment, rightAssignment) -> rightAssignment
                                .calculateOverlap(leftAssignment))
                .justifyWith((leftPreferredAttendance, rightPreferredAttendance, leftAssignment, rightAssignment,
                        score) -> PreferredAttendeeInOverlappingMeetingsJustification.of(leftPreferredAttendance,
                                leftAssignment, rightAssignment))
                .asConstraint(new ConstraintInfo(MeetingScheduleConstraintProperties.PREFERRED_ATTENDANCE_CONFLICT,
                        MeetingScheduleConstraintProperties.PREFERRED_ATTENDANCE_CONFLICT,
                        "A person should not have to pick between two meetings they would both prefer to attend.",
                        MeetingScheduleConstraintGroup.ATTENDANCE_CONFLICTS));
    }

    // ************************************************************************
    // Soft constraints
    // ************************************************************************

    public Constraint doMeetingsAsSoonAsPossible(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachIncludingUnassigned(MeetingAssignment.class)
                .filter(meetingAssignment -> meetingAssignment.getStartingTimeGrain() != null)
                .penalize(HardMediumSoftScore.ONE_SOFT, MeetingAssignment::getLastTimeGrainIndex)
                .justifyWith((meetingAssignment, score) -> MeetingScheduledLateJustification.of(meetingAssignment))
                .asConstraint(
                        new ConstraintInfo(MeetingScheduleConstraintProperties.DO_ALL_MEETINGS_AS_SOON_AS_POSSIBLE,
                                MeetingScheduleConstraintProperties.DO_ALL_MEETINGS_AS_SOON_AS_POSSIBLE,
                                "A meeting should be held as early in the scheduling horizon as possible.",
                                MeetingScheduleConstraintGroup.SCHEDULE_QUALITY));
    }

    public Constraint oneBreakBetweenConsecutiveMeetings(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachIncludingUnassigned(MeetingAssignment.class)
                .filter(meetingAssignment -> meetingAssignment.getStartingTimeGrain() != null)
                .join(constraintFactory.forEachIncludingUnassigned(MeetingAssignment.class)
                        .filter(assignment -> assignment.getStartingTimeGrain() != null),
                        equal(MeetingAssignment::getLastTimeGrainIndex,
                                rightAssignment -> rightAssignment.getGrainIndex() - 1))
                .penalize(HardMediumSoftScore.ofSoft(100))
                .justifyWith((leftAssignment, rightAssignment, score) -> MeetingsWithoutBreakInBetweenJustification
                        .of(leftAssignment, rightAssignment))
                .asConstraint(
                        new ConstraintInfo(
                                MeetingScheduleConstraintProperties.ONE_BREAK_BETWEEN_CONSECUTIVE_MEETINGS,
                                MeetingScheduleConstraintProperties.ONE_BREAK_BETWEEN_CONSECUTIVE_MEETINGS,
                                "Two consecutive meetings should be separated by at least one free time slot.",
                                MeetingScheduleConstraintGroup.SCHEDULE_QUALITY));
    }

    public Constraint overlappingMeetings(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachIncludingUnassigned(MeetingAssignment.class)
                .filter(meetingAssignment -> meetingAssignment.getStartingTimeGrain() != null)
                .join(constraintFactory.forEachIncludingUnassigned(MeetingAssignment.class)
                        .filter(meetingAssignment -> meetingAssignment.getStartingTimeGrain() != null),
                        greaterThan(leftAssignment -> leftAssignment.getMeeting().id(),
                                rightAssignment -> rightAssignment.getMeeting().id()),
                        overlapping(MeetingAssignment::getGrainIndex,
                                assignment -> assignment.getLastTimeGrainIndex() + 1))
                .penalize(HardMediumSoftScore.ofSoft(10), MeetingAssignment::calculateOverlap)
                .justifyWith((leftAssignment, rightAssignment, score) -> MeetingsOverlappingInTimeJustification
                        .of(leftAssignment, rightAssignment))
                .asConstraint(new ConstraintInfo(MeetingScheduleConstraintProperties.OVERLAPPING_MEETINGS,
                        MeetingScheduleConstraintProperties.OVERLAPPING_MEETINGS,
                        "Two meetings should not run in parallel.",
                        MeetingScheduleConstraintGroup.SCHEDULE_QUALITY));
    }

    public Constraint assignLargerRoomsFirst(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachIncludingUnassigned(MeetingAssignment.class)
                .filter(meetingAssignment -> meetingAssignment.getRoom() != null)
                .join(Room.class,
                        lessThan(MeetingAssignment::getRoomCapacity, Room::capacity))
                .penalize(HardMediumSoftScore.ONE_SOFT,
                        (meetingAssignment, room) -> room.capacity() - meetingAssignment.getRoomCapacity())
                .justifyWith((meetingAssignment, room, score) -> LargerRoomAvailableJustification.of(meetingAssignment,
                        room))
                .asConstraint(new ConstraintInfo(MeetingScheduleConstraintProperties.ASSIGN_LARGER_ROOMS_FIRST,
                        MeetingScheduleConstraintProperties.ASSIGN_LARGER_ROOMS_FIRST,
                        "A meeting should be held in the largest room available, so smaller rooms stay free.",
                        MeetingScheduleConstraintGroup.ROOM_CAPACITY));
    }

    public Constraint roomStability(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Attendance.class)
                .join(Attendance.class,
                        equal(Attendance::getPerson),
                        filtering((leftAttendance, rightAttendance) -> !leftAttendance.getMeeting()
                                .equals(rightAttendance.getMeeting())))
                .join(MeetingAssignment.class,
                        equal((leftAttendance, rightAttendance) -> leftAttendance.getMeeting(),
                                MeetingAssignment::getMeeting))
                .join(MeetingAssignment.class,
                        equal((leftAttendance, rightAttendance, leftAssignment) -> rightAttendance.getMeeting(),
                                MeetingAssignment::getMeeting),
                        lessThan((leftAttendance, rightAttendance, leftAssignment) -> leftAssignment.getStartingTimeGrain(),
                                MeetingAssignment::getStartingTimeGrain),
                        filtering((leftAttendance, rightAttendance, leftAssignment,
                                rightAssignment) -> !Objects.equals(leftAssignment.getRoom(),
                                        rightAssignment.getRoom())),
                        filtering((leftAttendance, rightAttendance, leftAssignment,
                                rightAssignment) -> rightAssignment.getGrainIndex() -
                                        leftAttendance.getMeeting().durationInGrains() -
                                        leftAssignment.getGrainIndex() <= 2))
                .penalize(HardMediumSoftScore.ONE_SOFT)
                .justifyWith((leftAttendance, rightAttendance, leftAssignment, rightAssignment,
                        score) -> AttendeeChangingRoomJustification.of(leftAttendance, leftAssignment, rightAssignment))
                .asConstraint(new ConstraintInfo(MeetingScheduleConstraintProperties.ROOM_STABILITY,
                        MeetingScheduleConstraintProperties.ROOM_STABILITY,
                        "An attendee's consecutive meetings should be held in the same room, so they do not have to move.",
                        MeetingScheduleConstraintGroup.SCHEDULE_QUALITY));
    }
}
