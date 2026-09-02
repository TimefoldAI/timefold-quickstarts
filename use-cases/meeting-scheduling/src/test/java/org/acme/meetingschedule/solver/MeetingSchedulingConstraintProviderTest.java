package org.acme.meetingschedule.solver;

import static org.acme.meetingschedule.support.TestHelper.aMeeting;
import static org.acme.meetingschedule.support.TestHelper.aPerson;
import static org.acme.meetingschedule.support.TestHelper.aPreferredAttendance;
import static org.acme.meetingschedule.support.TestHelper.aRequiredAttendance;
import static org.acme.meetingschedule.support.TestHelper.aRoom;
import static org.acme.meetingschedule.support.TestHelper.aTimeGrain;
import static org.acme.meetingschedule.support.TestHelper.anAssignment;

import java.time.LocalDate;

import jakarta.inject.Inject;

import ai.timefold.solver.core.api.score.stream.test.ConstraintVerifier;

import org.acme.meetingschedule.domain.MeetingSchedule;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class MeetingSchedulingConstraintProviderTest {

    @Inject
    ConstraintVerifier<MeetingSchedulingConstraintProvider, MeetingSchedule> constraintVerifier;

    @Test
    void roomConflictUnpenalized() {
        var room = aRoom("R1");
        var leftAssignment = anAssignment("0", aMeeting("M1").durationInGrains(4))
                .startingTimeGrain(aTimeGrain("TG0").grainIndex(0)).room(room).build();
        var rightAssignment = anAssignment("1", aMeeting("M2").durationInGrains(4))
                .startingTimeGrain(aTimeGrain("TG4").grainIndex(4)).room(room).build();

        constraintVerifier.verifyThat(MeetingSchedulingConstraintProvider::roomConflict)
                .given(leftAssignment, rightAssignment)
                .penalizesBy(0);
    }

    @Test
    void roomConflictPenalized() {
        var room = aRoom("R1");
        var leftAssignment = anAssignment("0", aMeeting("M1").durationInGrains(4))
                .startingTimeGrain(aTimeGrain("TG0").grainIndex(0)).room(room).build();
        var rightAssignment = anAssignment("1", aMeeting("M2").durationInGrains(4))
                .startingTimeGrain(aTimeGrain("TG2").grainIndex(2)).room(room).build();

        constraintVerifier.verifyThat(MeetingSchedulingConstraintProvider::roomConflict)
                .given(leftAssignment, rightAssignment)
                .penalizesBy(2);
    }

    @Test
    void avoidOvertimeUnpenalized() {
        var lastTimeGrain = aTimeGrain("TG3").grainIndex(3).build();
        var meetingAssignment = anAssignment("0", aMeeting("M1").durationInGrains(4))
                .startingTimeGrain(aTimeGrain("TG0").grainIndex(0)).room(aRoom("R1")).build();

        constraintVerifier.verifyThat(MeetingSchedulingConstraintProvider::avoidOvertime)
                .given(meetingAssignment, lastTimeGrain)
                .penalizesBy(0);
    }

    @Test
    void avoidOvertimePenalized() {
        // The schedule has no time grain 3, so a four grain meeting starting at grain 0 runs past its end.
        var meetingAssignment = anAssignment("0", aMeeting("M1").durationInGrains(4))
                .startingTimeGrain(aTimeGrain("TG0").grainIndex(0)).room(aRoom("R1")).build();

        constraintVerifier.verifyThat(MeetingSchedulingConstraintProvider::avoidOvertime)
                .given(meetingAssignment)
                .penalizesBy(3);
    }

    @Test
    void requiredAttendanceConflictUnpenalized() {
        var person = aPerson("P1");
        var leftMeeting = aMeeting("M1").durationInGrains(4);
        var rightMeeting = aMeeting("M2").durationInGrains(4);
        var leftAttendance = aRequiredAttendance("M1-required-P1", leftMeeting, person);
        var rightAttendance = aRequiredAttendance("M2-required-P1", rightMeeting, person);
        var leftAssignment = anAssignment("0", leftMeeting)
                .startingTimeGrain(aTimeGrain("TG0").grainIndex(0)).room(aRoom("R1")).build();
        var rightAssignment = anAssignment("1", rightMeeting)
                .startingTimeGrain(aTimeGrain("TG4").grainIndex(4)).room(aRoom("R1")).build();

        constraintVerifier.verifyThat(MeetingSchedulingConstraintProvider::requiredAttendanceConflict)
                .given(leftAttendance, rightAttendance, leftAssignment, rightAssignment)
                .penalizesBy(0);
    }

    @Test
    void requiredAttendanceConflictPenalized() {
        var person = aPerson("P1");
        var leftMeeting = aMeeting("M1").durationInGrains(4);
        var rightMeeting = aMeeting("M2").durationInGrains(4);
        var leftAttendance = aRequiredAttendance("M1-required-P1", leftMeeting, person);
        var rightAttendance = aRequiredAttendance("M2-required-P1", rightMeeting, person);
        var leftAssignment = anAssignment("0", leftMeeting)
                .startingTimeGrain(aTimeGrain("TG0").grainIndex(0)).room(aRoom("R1")).build();
        var rightAssignment = anAssignment("1", rightMeeting)
                .startingTimeGrain(aTimeGrain("TG2").grainIndex(2)).room(aRoom("R1")).build();

        constraintVerifier.verifyThat(MeetingSchedulingConstraintProvider::requiredAttendanceConflict)
                .given(leftAttendance, rightAttendance, leftAssignment, rightAssignment)
                .penalizesBy(2);
    }

    @Test
    void requiredRoomCapacityUnpenalized() {
        var meetingAssignment = anAssignment("0", aMeeting("M1").requiredCapacity(2))
                .startingTimeGrain(aTimeGrain("TG0").grainIndex(0)).room(aRoom("R1").capacity(2)).build();

        constraintVerifier.verifyThat(MeetingSchedulingConstraintProvider::requiredRoomCapacity)
                .given(meetingAssignment)
                .penalizesBy(0);
    }

    @Test
    void requiredRoomCapacityPenalized() {
        var meetingAssignment = anAssignment("0", aMeeting("M1").requiredCapacity(2))
                .startingTimeGrain(aTimeGrain("TG0").grainIndex(0)).room(aRoom("R1").capacity(1)).build();

        constraintVerifier.verifyThat(MeetingSchedulingConstraintProvider::requiredRoomCapacity)
                .given(meetingAssignment)
                .penalizesBy(1);
    }

    @Test
    void startAndEndOnSameDayUnpenalized() {
        var meetingAssignment = anAssignment("0", aMeeting("M1").durationInGrains(4))
                .startingTimeGrain(aTimeGrain("TG0").grainIndex(0)).room(aRoom("R1")).build();
        var lastTimeGrain = aTimeGrain("TG3").grainIndex(3).build();

        constraintVerifier.verifyThat(MeetingSchedulingConstraintProvider::startAndEndOnSameDay)
                .given(meetingAssignment, lastTimeGrain)
                .penalizesBy(0);
    }

    @Test
    void startAndEndOnSameDayPenalized() {
        var meetingAssignment = anAssignment("0", aMeeting("M1").durationInGrains(4))
                .startingTimeGrain(aTimeGrain("TG0").grainIndex(0)).room(aRoom("R1")).build();
        // The meeting's last grain is already on the next day.
        var lastTimeGrain = aTimeGrain("TG3").grainIndex(3).date(LocalDate.of(2024, 1, 2)).build();

        constraintVerifier.verifyThat(MeetingSchedulingConstraintProvider::startAndEndOnSameDay)
                .given(meetingAssignment, lastTimeGrain)
                .penalizesBy(1);
    }

    @Test
    void requiredAndPreferredAttendanceConflictUnpenalized() {
        var person = aPerson("P1");
        var requiredMeeting = aMeeting("M1").durationInGrains(4);
        var preferredMeeting = aMeeting("M2").durationInGrains(4);
        var requiredAttendance = aRequiredAttendance("M1-required-P1", requiredMeeting, person);
        var preferredAttendance = aPreferredAttendance("M2-preferred-P1", preferredMeeting, person);
        var leftAssignment = anAssignment("0", requiredMeeting)
                .startingTimeGrain(aTimeGrain("TG0").grainIndex(0)).room(aRoom("R1")).build();
        var rightAssignment = anAssignment("1", preferredMeeting)
                .startingTimeGrain(aTimeGrain("TG4").grainIndex(4)).room(aRoom("R1")).build();

        constraintVerifier.verifyThat(MeetingSchedulingConstraintProvider::requiredAndPreferredAttendanceConflict)
                .given(requiredAttendance, preferredAttendance, leftAssignment, rightAssignment)
                .penalizesBy(0);
    }

    @Test
    void requiredAndPreferredAttendanceConflictPenalized() {
        var person = aPerson("P1");
        var requiredMeeting = aMeeting("M1").durationInGrains(4);
        var preferredMeeting = aMeeting("M2").durationInGrains(4);
        var requiredAttendance = aRequiredAttendance("M1-required-P1", requiredMeeting, person);
        var preferredAttendance = aPreferredAttendance("M2-preferred-P1", preferredMeeting, person);
        var leftAssignment = anAssignment("0", requiredMeeting)
                .startingTimeGrain(aTimeGrain("TG0").grainIndex(0)).room(aRoom("R1")).build();
        var rightAssignment = anAssignment("1", preferredMeeting)
                .startingTimeGrain(aTimeGrain("TG0").grainIndex(0)).room(aRoom("R1")).build();

        constraintVerifier.verifyThat(MeetingSchedulingConstraintProvider::requiredAndPreferredAttendanceConflict)
                .given(requiredAttendance, preferredAttendance, leftAssignment, rightAssignment)
                .penalizesBy(4);
    }

    @Test
    void preferredAttendanceConflictUnpenalized() {
        var person = aPerson("P1");
        var leftMeeting = aMeeting("M1").durationInGrains(4);
        var rightMeeting = aMeeting("M2").durationInGrains(4);
        var leftAttendance = aPreferredAttendance("M1-preferred-P1", leftMeeting, person);
        var rightAttendance = aPreferredAttendance("M2-preferred-P1", rightMeeting, person);
        var leftAssignment = anAssignment("0", leftMeeting)
                .startingTimeGrain(aTimeGrain("TG0").grainIndex(0)).room(aRoom("R1")).build();
        var rightAssignment = anAssignment("1", rightMeeting)
                .startingTimeGrain(aTimeGrain("TG4").grainIndex(4)).room(aRoom("R1")).build();

        constraintVerifier.verifyThat(MeetingSchedulingConstraintProvider::preferredAttendanceConflict)
                .given(leftAttendance, rightAttendance, leftAssignment, rightAssignment)
                .penalizesBy(0);
    }

    @Test
    void preferredAttendanceConflictPenalized() {
        var person = aPerson("P1");
        var leftMeeting = aMeeting("M1").durationInGrains(4);
        var rightMeeting = aMeeting("M2").durationInGrains(4);
        var leftAttendance = aPreferredAttendance("M1-preferred-P1", leftMeeting, person);
        var rightAttendance = aPreferredAttendance("M2-preferred-P1", rightMeeting, person);
        var leftAssignment = anAssignment("0", leftMeeting)
                .startingTimeGrain(aTimeGrain("TG0").grainIndex(0)).room(aRoom("R1")).build();
        var rightAssignment = anAssignment("1", rightMeeting)
                .startingTimeGrain(aTimeGrain("TG0").grainIndex(0)).room(aRoom("R1")).build();

        constraintVerifier.verifyThat(MeetingSchedulingConstraintProvider::preferredAttendanceConflict)
                .given(leftAttendance, rightAttendance, leftAssignment, rightAssignment)
                .penalizesBy(4);
    }

    @Test
    void doMeetingsAsSoonAsPossibleUnpenalized() {
        var meetingAssignment = anAssignment("0", aMeeting("M1").durationInGrains(1))
                .startingTimeGrain(aTimeGrain("TG0").grainIndex(0)).room(aRoom("R1")).build();

        constraintVerifier.verifyThat(MeetingSchedulingConstraintProvider::doMeetingsAsSoonAsPossible)
                .given(meetingAssignment)
                .penalizesBy(0);
    }

    @Test
    void doMeetingsAsSoonAsPossiblePenalized() {
        var meetingAssignment = anAssignment("0", aMeeting("M1").durationInGrains(4))
                .startingTimeGrain(aTimeGrain("TG0").grainIndex(0)).room(aRoom("R1")).build();

        constraintVerifier.verifyThat(MeetingSchedulingConstraintProvider::doMeetingsAsSoonAsPossible)
                .given(meetingAssignment)
                .penalizesBy(3);
    }

    @Test
    void oneBreakBetweenConsecutiveMeetingsUnpenalized() {
        var leftAssignment = anAssignment("0", aMeeting("M1").durationInGrains(4))
                .startingTimeGrain(aTimeGrain("TG0").grainIndex(0)).room(aRoom("R1")).build();
        // Starts at grain 5, so grain 4 stays free as a break.
        var rightAssignment = anAssignment("1", aMeeting("M2").durationInGrains(4))
                .startingTimeGrain(aTimeGrain("TG5").grainIndex(5)).room(aRoom("R1")).build();

        constraintVerifier.verifyThat(MeetingSchedulingConstraintProvider::oneBreakBetweenConsecutiveMeetings)
                .given(leftAssignment, rightAssignment)
                .penalizesBy(0);
    }

    @Test
    void oneBreakBetweenConsecutiveMeetingsPenalized() {
        var leftAssignment = anAssignment("0", aMeeting("M1").durationInGrains(4))
                .startingTimeGrain(aTimeGrain("TG0").grainIndex(0)).room(aRoom("R1")).build();
        // Starts at grain 4, right after the left meeting's last grain 3.
        var rightAssignment = anAssignment("1", aMeeting("M2").durationInGrains(4))
                .startingTimeGrain(aTimeGrain("TG4").grainIndex(4)).room(aRoom("R1")).build();

        constraintVerifier.verifyThat(MeetingSchedulingConstraintProvider::oneBreakBetweenConsecutiveMeetings)
                .given(leftAssignment, rightAssignment)
                .penalizesBy(1);
    }

    @Test
    void overlappingMeetingsUnpenalized() {
        var leftAssignment = anAssignment("0", aMeeting("1").durationInGrains(4))
                .startingTimeGrain(aTimeGrain("TG0").grainIndex(0)).room(aRoom("R1")).build();
        var rightAssignment = anAssignment("1", aMeeting("0").durationInGrains(4))
                .startingTimeGrain(aTimeGrain("TG4").grainIndex(4)).room(aRoom("R1")).build();

        constraintVerifier.verifyThat(MeetingSchedulingConstraintProvider::overlappingMeetings)
                .given(leftAssignment, rightAssignment)
                .penalizesBy(0);
    }

    @Test
    void overlappingMeetingsPenalized() {
        var leftAssignment = anAssignment("0", aMeeting("1").durationInGrains(3))
                .startingTimeGrain(aTimeGrain("TG1").grainIndex(1)).room(aRoom("R1")).build();
        var rightAssignment = anAssignment("1", aMeeting("0").durationInGrains(3))
                .startingTimeGrain(aTimeGrain("TG0").grainIndex(0)).room(aRoom("R1")).build();

        constraintVerifier.verifyThat(MeetingSchedulingConstraintProvider::overlappingMeetings)
                .given(leftAssignment, rightAssignment)
                .penalizesBy(2);
    }

    @Test
    void assignLargerRoomsFirstUnpenalized() {
        var room = aRoom("R1").capacity(1);
        var meetingAssignment = anAssignment("0", aMeeting("M1"))
                .startingTimeGrain(aTimeGrain("TG0").grainIndex(0)).room(room).build();

        // The only room in the schedule is the one the meeting is already in, so nothing larger exists.
        constraintVerifier.verifyThat(MeetingSchedulingConstraintProvider::assignLargerRoomsFirst)
                .given(meetingAssignment, room.build())
                .penalizesBy(0);
    }

    @Test
    void assignLargerRoomsFirstPenalized() {
        var meetingAssignment = anAssignment("0", aMeeting("M1"))
                .startingTimeGrain(aTimeGrain("TG0").grainIndex(0)).room(aRoom("R1").capacity(1)).build();
        var largerRoom = aRoom("R2").capacity(2).build();

        constraintVerifier.verifyThat(MeetingSchedulingConstraintProvider::assignLargerRoomsFirst)
                .given(meetingAssignment, largerRoom)
                .penalizesBy(1);
    }

    @Test
    void roomStabilityUnpenalized() {
        var person = aPerson("P1");
        var leftMeeting = aMeeting("M1").durationInGrains(4);
        var rightMeeting = aMeeting("M2").durationInGrains(4);
        var leftAttendance = aRequiredAttendance("M1-required-P1", leftMeeting, person);
        var rightAttendance = aRequiredAttendance("M2-required-P1", rightMeeting, person);
        var leftAssignment = anAssignment("0", leftMeeting)
                .startingTimeGrain(aTimeGrain("TG0").grainIndex(0)).room(aRoom("R1")).build();
        // Four grains of slack after the left meeting ends, so changing room is not held against the schedule.
        var rightAssignment = anAssignment("1", rightMeeting)
                .startingTimeGrain(aTimeGrain("TG8").grainIndex(8)).room(aRoom("R2")).build();

        constraintVerifier.verifyThat(MeetingSchedulingConstraintProvider::roomStability)
                .given(leftAttendance, rightAttendance, leftAssignment, rightAssignment)
                .penalizesBy(0);
    }

    @Test
    void roomStabilityPenalized() {
        var person = aPerson("P1");
        var leftMeeting = aMeeting("M1").durationInGrains(4);
        var rightMeeting = aMeeting("M2").durationInGrains(4);
        var leftAttendance = aRequiredAttendance("M1-required-P1", leftMeeting, person);
        var rightAttendance = aRequiredAttendance("M2-required-P1", rightMeeting, person);
        var leftAssignment = anAssignment("0", leftMeeting)
                .startingTimeGrain(aTimeGrain("TG0").grainIndex(0)).room(aRoom("R1")).build();
        // Starts right after the left meeting ends, in a different room, so the attendee has to move.
        var rightAssignment = anAssignment("1", rightMeeting)
                .startingTimeGrain(aTimeGrain("TG4").grainIndex(4)).room(aRoom("R2")).build();

        constraintVerifier.verifyThat(MeetingSchedulingConstraintProvider::roomStability)
                .given(leftAttendance, rightAttendance, leftAssignment, rightAssignment)
                .penalizesBy(1);
    }
}
