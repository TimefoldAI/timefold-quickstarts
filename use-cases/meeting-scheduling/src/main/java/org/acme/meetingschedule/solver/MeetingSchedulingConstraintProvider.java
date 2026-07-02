package org.acme.meetingschedule.solver;

import static ai.timefold.solver.core.api.score.stream.Joiners.equal;
import static ai.timefold.solver.core.api.score.stream.Joiners.filtering;
import static ai.timefold.solver.core.api.score.stream.Joiners.greaterThan;
import static ai.timefold.solver.core.api.score.stream.Joiners.lessThan;
import static ai.timefold.solver.core.api.score.stream.Joiners.overlapping;

import ai.timefold.solver.core.api.score.HardMediumSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;

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
                equal(MeetingAssignment::getRoom),
                overlapping(MeetingAssignment::getGrainIndex, assignment -> assignment.getLastTimeGrainIndex() + 1))
                .penalize(HardMediumSoftScore.ONE_HARD,
                        (leftAssignment, rightAssignment) -> rightAssignment.calculateOverlap(leftAssignment))
                .asConstraint("Room conflict");
    }

    public Constraint avoidOvertime(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachIncludingUnassigned(MeetingAssignment.class)
                .filter(meetingAssignment -> meetingAssignment.getStartingTimeGrain() != null)
                .ifNotExists(TimeGrain.class,
                        equal(MeetingAssignment::getLastTimeGrainIndex, TimeGrain::getGrainIndex))
                .penalize(HardMediumSoftScore.ONE_HARD, MeetingAssignment::getLastTimeGrainIndex)
                .asConstraint("Don't go in overtime");
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
                .asConstraint("Required attendance conflict");
    }

    public Constraint requiredRoomCapacity(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachIncludingUnassigned(MeetingAssignment.class)
                .filter(meetingAssignment -> meetingAssignment.getRequiredCapacity() > meetingAssignment.getRoomCapacity())
                .penalize(HardMediumSoftScore.ONE_HARD,
                        meetingAssignment -> meetingAssignment.getRequiredCapacity() - meetingAssignment.getRoomCapacity())
                .asConstraint("Required room capacity");
    }

    public Constraint startAndEndOnSameDay(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachIncludingUnassigned(MeetingAssignment.class)
                .filter(meetingAssignment -> meetingAssignment.getStartingTimeGrain() != null)
                .join(TimeGrain.class,
                        equal(MeetingAssignment::getLastTimeGrainIndex, TimeGrain::getGrainIndex),
                        filtering((meetingAssignment,
                                timeGrain) -> !meetingAssignment.getStartingTimeGrain().getDayOfYear()
                                        .equals(timeGrain.getDayOfYear())))
                .penalize(HardMediumSoftScore.ONE_HARD)
                .asConstraint("Start and end on same day");
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
                .asConstraint("Required and preferred attendance conflict");
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
                .asConstraint("Preferred attendance conflict");
    }

    // ************************************************************************
    // Soft constraints
    // ************************************************************************

    public Constraint doMeetingsAsSoonAsPossible(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachIncludingUnassigned(MeetingAssignment.class)
                .filter(meetingAssignment -> meetingAssignment.getStartingTimeGrain() != null)
                .penalize(HardMediumSoftScore.ONE_SOFT, MeetingAssignment::getLastTimeGrainIndex)
                .asConstraint("Do all meetings as soon as possible");
    }

    public Constraint oneBreakBetweenConsecutiveMeetings(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachIncludingUnassigned(MeetingAssignment.class)
                .filter(meetingAssignment -> meetingAssignment.getStartingTimeGrain() != null)
                .join(constraintFactory.forEachIncludingUnassigned(MeetingAssignment.class)
                        .filter(assignment -> assignment.getStartingTimeGrain() != null),
                        equal(MeetingAssignment::getLastTimeGrainIndex,
                                rightAssignment -> rightAssignment.getGrainIndex() - 1))
                .penalize(HardMediumSoftScore.ofSoft(100))
                .asConstraint("One TimeGrain break between two consecutive meetings");
    }

    public Constraint overlappingMeetings(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachIncludingUnassigned(MeetingAssignment.class)
                .filter(meetingAssignment -> meetingAssignment.getStartingTimeGrain() != null)
                .join(constraintFactory.forEachIncludingUnassigned(MeetingAssignment.class)
                        .filter(meetingAssignment -> meetingAssignment.getStartingTimeGrain() != null),
                        greaterThan(leftAssignment -> leftAssignment.getMeeting().getId(),
                                rightAssignment -> rightAssignment.getMeeting().getId()),
                        overlapping(MeetingAssignment::getGrainIndex,
                                assignment -> assignment.getLastTimeGrainIndex() + 1))
                .penalize(HardMediumSoftScore.ofSoft(10), MeetingAssignment::calculateOverlap)
                .asConstraint("Overlapping meetings");
    }

    public Constraint assignLargerRoomsFirst(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachIncludingUnassigned(MeetingAssignment.class)
                .filter(meetingAssignment -> meetingAssignment.getRoom() != null)
                .join(Room.class,
                        lessThan(MeetingAssignment::getRoomCapacity, Room::getCapacity))
                .penalize(HardMediumSoftScore.ONE_SOFT,
                        (meetingAssignment, room) -> room.getCapacity() - meetingAssignment.getRoomCapacity())
                .asConstraint("Assign larger rooms first");
    }

    public Constraint roomStability(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Attendance.class)
                .join(Attendance.class,
                        equal(Attendance::getPerson),
                        filtering((leftAttendance,
                                rightAttendance) -> leftAttendance.getMeeting() != rightAttendance.getMeeting()))
                .join(MeetingAssignment.class,
                        equal((leftAttendance, rightAttendance) -> leftAttendance.getMeeting(),
                                MeetingAssignment::getMeeting))
                .join(MeetingAssignment.class,
                        equal((leftAttendance, rightAttendance, leftAssignment) -> rightAttendance.getMeeting(),
                                MeetingAssignment::getMeeting),
                        lessThan((leftAttendance, rightAttendance, leftAssignment) -> leftAssignment.getStartingTimeGrain(),
                                MeetingAssignment::getStartingTimeGrain),
                        filtering((leftAttendance, rightAttendance, leftAssignment,
                                rightAssignment) -> leftAssignment.getRoom() != rightAssignment.getRoom()),
                        filtering((leftAttendance, rightAttendance, leftAssignment,
                                rightAssignment) -> rightAssignment.getGrainIndex() -
                                        leftAttendance.getMeeting().getDurationInGrains() -
                                        leftAssignment.getGrainIndex() <= 2))
                .penalize(HardMediumSoftScore.ONE_SOFT)
                .asConstraint("Room stability");
    }

}
