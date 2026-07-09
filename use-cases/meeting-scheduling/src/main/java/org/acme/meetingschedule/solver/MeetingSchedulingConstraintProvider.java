package org.acme.meetingschedule.solver;

import java.util.Objects;

import ai.timefold.solver.core.api.score.HardMediumSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.core.api.score.stream.Joiners;
import ai.timefold.solver.service.definition.api.description.ConstraintInfo;

import org.acme.meetingschedule.domain.Attendance;
import org.acme.meetingschedule.domain.MeetingAssignment;
import org.acme.meetingschedule.domain.PreferredAttendance;
import org.acme.meetingschedule.domain.RequiredAttendance;
import org.acme.meetingschedule.domain.Room;
import org.acme.meetingschedule.domain.TimeGrain;

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
                Joiners.equal(MeetingAssignment::getRoom),
                Joiners.overlapping(MeetingAssignment::getGrainIndex,
                        assignment -> assignment.getLastTimeGrainIndex() + 1))
                .penalize(HardMediumSoftScore.ONE_HARD,
                        (leftAssignment, rightAssignment) -> rightAssignment.calculateOverlap(leftAssignment))
                .asConstraint(new ConstraintInfo(MeetingScheduleConstraintProperties.ROOM_CONFLICT,
                        MeetingScheduleConstraintProperties.ROOM_CONFLICT,
                        "A room can accommodate at most one meeting at the same time.",
                        MeetingScheduleConstraintGroup.CONFLICT_AVOIDANCE));
    }

    public Constraint avoidOvertime(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachIncludingUnassigned(MeetingAssignment.class)
                .filter(meetingAssignment -> meetingAssignment.getStartingTimeGrain() != null)
                .ifNotExists(TimeGrain.class,
                        Joiners.equal(MeetingAssignment::getLastTimeGrainIndex, TimeGrain::getGrainIndex))
                .penalize(HardMediumSoftScore.ONE_HARD, MeetingAssignment::getLastTimeGrainIndex)
                .asConstraint(new ConstraintInfo(MeetingScheduleConstraintProperties.DONT_GO_IN_OVERTIME,
                        MeetingScheduleConstraintProperties.DONT_GO_IN_OVERTIME,
                        "A meeting must end within the available time grains.",
                        MeetingScheduleConstraintGroup.CONFLICT_AVOIDANCE));
    }

    public Constraint requiredAttendanceConflict(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachUniquePair(RequiredAttendance.class,
                Joiners.equal(RequiredAttendance::getPerson))
                .join(MeetingAssignment.class,
                        Joiners.equal((leftRequiredAttendance, rightRequiredAttendance) -> leftRequiredAttendance.getMeeting(),
                                MeetingAssignment::getMeeting))
                .join(MeetingAssignment.class,
                        Joiners.equal(
                                (leftRequiredAttendance, rightRequiredAttendance, leftAssignment) -> rightRequiredAttendance
                                        .getMeeting(),
                                MeetingAssignment::getMeeting),
                        Joiners.overlapping((attendee1, attendee2, assignment) -> assignment.getGrainIndex(),
                                (attendee1, attendee2, assignment) -> assignment.getLastTimeGrainIndex() + 1,
                                MeetingAssignment::getGrainIndex,
                                assignment -> assignment.getLastTimeGrainIndex() + 1))
                .penalize(HardMediumSoftScore.ONE_HARD,
                        (leftRequiredAttendance, rightRequiredAttendance, leftAssignment, rightAssignment) -> rightAssignment
                                .calculateOverlap(leftAssignment))
                .asConstraint(new ConstraintInfo(MeetingScheduleConstraintProperties.REQUIRED_ATTENDANCE_CONFLICT,
                        MeetingScheduleConstraintProperties.REQUIRED_ATTENDANCE_CONFLICT,
                        "A required attendee cannot attend two overlapping meetings.",
                        MeetingScheduleConstraintGroup.CONFLICT_AVOIDANCE));
    }

    public Constraint requiredRoomCapacity(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachIncludingUnassigned(MeetingAssignment.class)
                .filter(meetingAssignment -> meetingAssignment.getRequiredCapacity() > meetingAssignment.getRoomCapacity())
                .penalize(HardMediumSoftScore.ONE_HARD,
                        meetingAssignment -> meetingAssignment.getRequiredCapacity() - meetingAssignment.getRoomCapacity())
                .asConstraint(new ConstraintInfo(MeetingScheduleConstraintProperties.REQUIRED_ROOM_CAPACITY,
                        MeetingScheduleConstraintProperties.REQUIRED_ROOM_CAPACITY,
                        "A room must have enough capacity for all required and preferred attendees.",
                        MeetingScheduleConstraintGroup.CONFLICT_AVOIDANCE));
    }

    public Constraint startAndEndOnSameDay(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachIncludingUnassigned(MeetingAssignment.class)
                .filter(meetingAssignment -> meetingAssignment.getStartingTimeGrain() != null)
                .join(TimeGrain.class,
                        Joiners.equal(MeetingAssignment::getLastTimeGrainIndex, TimeGrain::getGrainIndex),
                        Joiners.filtering((meetingAssignment,
                                timeGrain) -> !meetingAssignment.getStartingTimeGrain().getDayOfYear()
                                        .equals(timeGrain.getDayOfYear())))
                .penalize(HardMediumSoftScore.ONE_HARD)
                .asConstraint(new ConstraintInfo(MeetingScheduleConstraintProperties.START_AND_END_ON_SAME_DAY,
                        MeetingScheduleConstraintProperties.START_AND_END_ON_SAME_DAY,
                        "A meeting must start and end on the same day.",
                        MeetingScheduleConstraintGroup.CONFLICT_AVOIDANCE));
    }

    // ************************************************************************
    // Medium constraints
    // ************************************************************************

    public Constraint requiredAndPreferredAttendanceConflict(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(RequiredAttendance.class)
                .join(PreferredAttendance.class,
                        Joiners.equal(RequiredAttendance::getPerson, PreferredAttendance::getPerson))
                .join(constraintFactory.forEachIncludingUnassigned(MeetingAssignment.class)
                        .filter(assignment -> assignment.getStartingTimeGrain() != null),
                        Joiners.equal((requiredAttendance, preferredAttendance) -> requiredAttendance.getMeeting(),
                                MeetingAssignment::getMeeting))
                .join(constraintFactory.forEachIncludingUnassigned(MeetingAssignment.class)
                        .filter(assignment -> assignment.getStartingTimeGrain() != null),
                        Joiners.equal(
                                (requiredAttendance, preferredAttendance, leftAssignment) -> preferredAttendance.getMeeting(),
                                MeetingAssignment::getMeeting),
                        Joiners.overlapping((attendee1, attendee2, assignment) -> assignment.getGrainIndex(),
                                (attendee1, attendee2, assignment) -> assignment.getLastTimeGrainIndex() + 1,
                                MeetingAssignment::getGrainIndex,
                                assignment -> assignment.getLastTimeGrainIndex() + 1))
                .penalize(HardMediumSoftScore.ONE_MEDIUM,
                        (requiredAttendance, preferredAttendance, leftAssignment, rightAssignment) -> rightAssignment
                                .calculateOverlap(leftAssignment))
                .asConstraint(
                        new ConstraintInfo(MeetingScheduleConstraintProperties.REQUIRED_AND_PREFERRED_ATTENDANCE_CONFLICT,
                                MeetingScheduleConstraintProperties.REQUIRED_AND_PREFERRED_ATTENDANCE_CONFLICT,
                                "An attendee should not have a required and a preferred meeting overlapping.",
                                MeetingScheduleConstraintGroup.ATTENDANCE_PREFERENCES));
    }

    public Constraint preferredAttendanceConflict(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachUniquePair(PreferredAttendance.class,
                Joiners.equal(PreferredAttendance::getPerson))
                .join(constraintFactory.forEachIncludingUnassigned(MeetingAssignment.class)
                        .filter(assignment -> assignment.getStartingTimeGrain() != null),
                        Joiners.equal((leftAttendance, rightAttendance) -> leftAttendance.getMeeting(),
                                MeetingAssignment::getMeeting))
                .join(constraintFactory.forEachIncludingUnassigned(MeetingAssignment.class)
                        .filter(assignment -> assignment.getStartingTimeGrain() != null),
                        Joiners.equal((leftAttendance, rightAttendance, leftAssignment) -> rightAttendance.getMeeting(),
                                MeetingAssignment::getMeeting),
                        Joiners.overlapping((attendee1, attendee2, assignment) -> assignment.getGrainIndex(),
                                (attendee1, attendee2, assignment) -> assignment.getLastTimeGrainIndex() + 1,
                                MeetingAssignment::getGrainIndex,
                                assignment -> assignment.getLastTimeGrainIndex() + 1))
                .penalize(HardMediumSoftScore.ONE_MEDIUM,
                        (leftPreferredAttendance, rightPreferredAttendance, leftAssignment, rightAssignment) -> rightAssignment
                                .calculateOverlap(leftAssignment))
                .asConstraint(new ConstraintInfo(MeetingScheduleConstraintProperties.PREFERRED_ATTENDANCE_CONFLICT,
                        MeetingScheduleConstraintProperties.PREFERRED_ATTENDANCE_CONFLICT,
                        "A preferred attendee should not attend two overlapping meetings.",
                        MeetingScheduleConstraintGroup.ATTENDANCE_PREFERENCES));
    }

    // ************************************************************************
    // Soft constraints
    // ************************************************************************

    public Constraint doMeetingsAsSoonAsPossible(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachIncludingUnassigned(MeetingAssignment.class)
                .filter(meetingAssignment -> meetingAssignment.getStartingTimeGrain() != null)
                .penalize(HardMediumSoftScore.ONE_SOFT, MeetingAssignment::getLastTimeGrainIndex)
                .asConstraint(new ConstraintInfo(MeetingScheduleConstraintProperties.DO_ALL_MEETINGS_AS_SOON_AS_POSSIBLE,
                        MeetingScheduleConstraintProperties.DO_ALL_MEETINGS_AS_SOON_AS_POSSIBLE,
                        "Meetings are preferably scheduled as early as possible.",
                        MeetingScheduleConstraintGroup.SCHEDULE_QUALITY));
    }

    public Constraint oneBreakBetweenConsecutiveMeetings(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachIncludingUnassigned(MeetingAssignment.class)
                .filter(meetingAssignment -> meetingAssignment.getStartingTimeGrain() != null)
                .join(constraintFactory.forEachIncludingUnassigned(MeetingAssignment.class)
                        .filter(assignment -> assignment.getStartingTimeGrain() != null),
                        Joiners.equal(MeetingAssignment::getLastTimeGrainIndex,
                                rightAssignment -> rightAssignment.getGrainIndex() - 1))
                .penalize(HardMediumSoftScore.ofSoft(100))
                .asConstraint(new ConstraintInfo(
                        MeetingScheduleConstraintProperties.ONE_TIME_GRAIN_BREAK_BETWEEN_TWO_CONSECUTIVE_MEETINGS,
                        MeetingScheduleConstraintProperties.ONE_TIME_GRAIN_BREAK_BETWEEN_TWO_CONSECUTIVE_MEETINGS,
                        "There should be at least one time grain break between two consecutive meetings.",
                        MeetingScheduleConstraintGroup.SCHEDULE_QUALITY));
    }

    public Constraint overlappingMeetings(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachIncludingUnassigned(MeetingAssignment.class)
                .filter(meetingAssignment -> meetingAssignment.getStartingTimeGrain() != null)
                .join(constraintFactory.forEachIncludingUnassigned(MeetingAssignment.class)
                        .filter(meetingAssignment -> meetingAssignment.getStartingTimeGrain() != null),
                        Joiners.greaterThan(leftAssignment -> leftAssignment.getMeeting().getId(),
                                rightAssignment -> rightAssignment.getMeeting().getId()),
                        Joiners.overlapping(MeetingAssignment::getGrainIndex,
                                assignment -> assignment.getLastTimeGrainIndex() + 1))
                .penalize(HardMediumSoftScore.ofSoft(10), MeetingAssignment::calculateOverlap)
                .asConstraint(new ConstraintInfo(MeetingScheduleConstraintProperties.OVERLAPPING_MEETINGS,
                        MeetingScheduleConstraintProperties.OVERLAPPING_MEETINGS,
                        "Overlapping meetings are discouraged.",
                        MeetingScheduleConstraintGroup.SCHEDULE_QUALITY));
    }

    public Constraint assignLargerRoomsFirst(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachIncludingUnassigned(MeetingAssignment.class)
                .filter(meetingAssignment -> meetingAssignment.getRoom() != null)
                .join(Room.class,
                        Joiners.lessThan(MeetingAssignment::getRoomCapacity, Room::getCapacity))
                .penalize(HardMediumSoftScore.ONE_SOFT,
                        (meetingAssignment, room) -> room.getCapacity() - meetingAssignment.getRoomCapacity())
                .asConstraint(new ConstraintInfo(MeetingScheduleConstraintProperties.ASSIGN_LARGER_ROOMS_FIRST,
                        MeetingScheduleConstraintProperties.ASSIGN_LARGER_ROOMS_FIRST,
                        "Smaller rooms are preferably used before larger rooms.",
                        MeetingScheduleConstraintGroup.SCHEDULE_QUALITY));
    }

    public Constraint roomStability(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Attendance.class)
                .join(Attendance.class,
                        Joiners.equal(Attendance::getPerson),
                        Joiners.filtering((leftAttendance, rightAttendance) -> !Objects
                                .equals(leftAttendance.getMeeting().getId(), rightAttendance.getMeeting().getId())))
                .join(MeetingAssignment.class,
                        Joiners.equal((leftAttendance, rightAttendance) -> leftAttendance.getMeeting(),
                                MeetingAssignment::getMeeting))
                .join(MeetingAssignment.class,
                        Joiners.equal((leftAttendance, rightAttendance, leftAssignment) -> rightAttendance.getMeeting(),
                                MeetingAssignment::getMeeting),
                        Joiners.lessThan(
                                (leftAttendance, rightAttendance, leftAssignment) -> leftAssignment.getStartingTimeGrain(),
                                MeetingAssignment::getStartingTimeGrain),
                        Joiners.filtering((leftAttendance, rightAttendance, leftAssignment,
                                rightAssignment) -> !Objects.equals(leftAssignment.getRoom(), rightAssignment.getRoom())),
                        Joiners.filtering((leftAttendance, rightAttendance, leftAssignment,
                                rightAssignment) -> rightAssignment.getGrainIndex() -
                                        leftAttendance.getMeeting().getDurationInGrains() -
                                        leftAssignment.getGrainIndex() <= 2))
                .penalize(HardMediumSoftScore.ONE_SOFT)
                .asConstraint(new ConstraintInfo(MeetingScheduleConstraintProperties.ROOM_STABILITY,
                        MeetingScheduleConstraintProperties.ROOM_STABILITY,
                        "Attendees prefer to stay in the same room for consecutive meetings.",
                        MeetingScheduleConstraintGroup.SCHEDULE_QUALITY));
    }

}
