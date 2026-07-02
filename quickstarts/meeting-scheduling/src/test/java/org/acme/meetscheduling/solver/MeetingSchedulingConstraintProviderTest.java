package org.acme.meetscheduling.solver;

import java.util.ArrayList;
import java.util.List;

import jakarta.inject.Inject;

import ai.timefold.solver.core.api.score.stream.test.ConstraintVerifier;

import org.acme.meetingschedule.domain.Meeting;
import org.acme.meetingschedule.domain.MeetingAssignment;
import org.acme.meetingschedule.domain.MeetingSchedule;
import org.acme.meetingschedule.domain.Person;
import org.acme.meetingschedule.domain.PreferredAttendance;
import org.acme.meetingschedule.domain.RequiredAttendance;
import org.acme.meetingschedule.domain.Room;
import org.acme.meetingschedule.domain.TimeGrain;
import org.acme.meetingschedule.solver.MeetingSchedulingConstraintProvider;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class MeetingSchedulingConstraintProviderTest {

    @Inject
    ConstraintVerifier<MeetingSchedulingConstraintProvider, MeetingSchedule> constraintVerifier;

    @Test
    void roomConflictUnpenalized() {
        Room room = new Room("1");

        TimeGrain timeGrain1 = new TimeGrain();
        timeGrain1.setGrainIndex(0);

        Meeting meeting1 = new Meeting();
        meeting1.setDurationInGrains(4);

        MeetingAssignment leftAssignment = new MeetingAssignment("0", meeting1, timeGrain1, room);

        TimeGrain timeGrain2 = new TimeGrain();
        timeGrain2.setGrainIndex(4);

        Meeting meeting2 = new Meeting();
        meeting2.setDurationInGrains(4);

        MeetingAssignment rightAssignment = new MeetingAssignment("1", meeting2, timeGrain2, room);

        constraintVerifier.verifyThat(MeetingSchedulingConstraintProvider::roomConflict)
                .given(leftAssignment, rightAssignment)
                .penalizesBy(0);
    }

    @Test
    void roomConflictPenalized() {
        Room room = new Room("1");

        TimeGrain timeGrain1 = new TimeGrain();
        timeGrain1.setGrainIndex(0);

        Meeting meeting1 = new Meeting();
        meeting1.setDurationInGrains(4);

        MeetingAssignment leftAssignment = new MeetingAssignment("0", meeting1, timeGrain1, room);

        TimeGrain timeGrain2 = new TimeGrain();
        timeGrain2.setGrainIndex(2);

        Meeting meeting2 = new Meeting();
        meeting2.setDurationInGrains(4);

        MeetingAssignment rightAssignment = new MeetingAssignment("1", meeting2, timeGrain2, room);

        constraintVerifier.verifyThat(MeetingSchedulingConstraintProvider::roomConflict)
                .given(leftAssignment, rightAssignment)
                .penalizesBy(2);
    }

    @Test
    void avoidOvertimeUnpenalized() {
        TimeGrain timeGrain = new TimeGrain();
        timeGrain.setGrainIndex(3);

        TimeGrain assignmentTimeGrain = new TimeGrain();
        assignmentTimeGrain.setGrainIndex(0);

        Meeting meeting = new Meeting();
        meeting.setDurationInGrains(4);

        Room room = new Room();

        MeetingAssignment meetingAssignment = new MeetingAssignment("0", meeting, assignmentTimeGrain, room);

        constraintVerifier.verifyThat(MeetingSchedulingConstraintProvider::avoidOvertime)
                .given(meetingAssignment, timeGrain)
                .penalizesBy(0);
    }

    @Test
    void avoidOvertimePenalized() {
        TimeGrain assignmentTimeGrain = new TimeGrain();
        assignmentTimeGrain.setGrainIndex(0);

        Meeting meeting = new Meeting();
        meeting.setDurationInGrains(4);

        Room room = new Room();

        MeetingAssignment meetingAssignment = new MeetingAssignment("0", meeting, assignmentTimeGrain, room);

        constraintVerifier.verifyThat(MeetingSchedulingConstraintProvider::avoidOvertime)
                .given(meetingAssignment)
                .penalizesBy(3);
    }

    @Test
    void requiredAttendanceConflictUnpenalized() {
        Person person = new Person("1");
        Meeting leftMeeting = new Meeting();
        leftMeeting.setDurationInGrains(4);

        RequiredAttendance requiredAttendance1 = new RequiredAttendance("0", leftMeeting);
        requiredAttendance1.setPerson(person);

        TimeGrain leftTimeGrain = new TimeGrain();
        leftTimeGrain.setGrainIndex(0);

        Room room = new Room();

        MeetingAssignment leftAssignment = new MeetingAssignment("0", leftMeeting, leftTimeGrain, room);

        Meeting rightMeeting = new Meeting();
        rightMeeting.setDurationInGrains(4);

        RequiredAttendance requiredAttendance2 = new RequiredAttendance("1", rightMeeting);
        requiredAttendance2.setPerson(person);

        TimeGrain rightTimeGrain = new TimeGrain();
        rightTimeGrain.setGrainIndex(4);

        MeetingAssignment rightAssignment = new MeetingAssignment("1", rightMeeting, rightTimeGrain, room);

        constraintVerifier.verifyThat(MeetingSchedulingConstraintProvider::requiredAttendanceConflict)
                .given(requiredAttendance1, requiredAttendance2, leftAssignment, rightAssignment)
                .penalizesBy(0);
    }

    @Test
    void requiredAttendanceConflictPenalized() {
        Person person = new Person("1");
        Meeting leftMeeting = new Meeting();
        leftMeeting.setDurationInGrains(4);

        RequiredAttendance requiredAttendance1 = new RequiredAttendance("0", leftMeeting);
        requiredAttendance1.setPerson(person);

        TimeGrain leftTimeGrain = new TimeGrain();
        leftTimeGrain.setGrainIndex(0);

        Room room = new Room();

        MeetingAssignment leftAssignment = new MeetingAssignment("0", leftMeeting, leftTimeGrain, room);

        Meeting rightMeeting = new Meeting();
        rightMeeting.setDurationInGrains(4);

        RequiredAttendance requiredAttendance2 = new RequiredAttendance("1", rightMeeting);
        requiredAttendance2.setPerson(person);

        TimeGrain rightTimeGrain = new TimeGrain();
        rightTimeGrain.setGrainIndex(2);

        MeetingAssignment rightAssignment = new MeetingAssignment("1", rightMeeting, rightTimeGrain, room);

        constraintVerifier.verifyThat(MeetingSchedulingConstraintProvider::requiredAttendanceConflict)
                .given(requiredAttendance1, requiredAttendance2, leftAssignment, rightAssignment)
                .penalizesBy(2);
    }

    @Test
    void requiredRoomCapacityUnpenalized() {
        Room room = new Room();
        room.setCapacity(2);

        List<RequiredAttendance> requiredAttendanceList = new ArrayList<>(1);
        RequiredAttendance requiredAttendance = new RequiredAttendance();
        requiredAttendanceList.add(requiredAttendance);

        List<PreferredAttendance> preferredAttendanceList = new ArrayList<>(1);
        PreferredAttendance preferredAttendance = new PreferredAttendance();
        preferredAttendanceList.add(preferredAttendance);

        Meeting meeting = new Meeting();
        meeting.setRequiredAttendances(requiredAttendanceList);
        meeting.setPreferredAttendances(preferredAttendanceList);

        TimeGrain startingTimeGrain = new TimeGrain();

        MeetingAssignment meetingAssignment = new MeetingAssignment("0", meeting, startingTimeGrain, room);

        constraintVerifier.verifyThat(MeetingSchedulingConstraintProvider::requiredRoomCapacity)
                .given(meetingAssignment)
                .penalizesBy(0);
    }

    @Test
    void requiredRoomCapacityPenalized() {
        Room room = new Room();
        room.setCapacity(1);

        List<RequiredAttendance> requiredAttendanceList = new ArrayList<>(1);
        RequiredAttendance requiredAttendance = new RequiredAttendance();
        requiredAttendanceList.add(requiredAttendance);

        List<PreferredAttendance> preferredAttendanceList = new ArrayList<>(1);
        PreferredAttendance preferredAttendance = new PreferredAttendance();
        preferredAttendanceList.add(preferredAttendance);

        Meeting meeting = new Meeting();
        meeting.setRequiredAttendances(requiredAttendanceList);
        meeting.setPreferredAttendances(preferredAttendanceList);

        TimeGrain startingTimeGrain = new TimeGrain();

        MeetingAssignment meetingAssignment = new MeetingAssignment("0", meeting, startingTimeGrain, room);

        constraintVerifier.verifyThat(MeetingSchedulingConstraintProvider::requiredRoomCapacity)
                .given(meetingAssignment)
                .penalizesBy(1);
    }

    @Test
    void startAndEndOnSameDayUnpenalized() {
        TimeGrain startingTimeGrain = new TimeGrain();
        startingTimeGrain.setGrainIndex(0);
        startingTimeGrain.setDayOfYear(0);

        Meeting meeting = new Meeting();
        meeting.setDurationInGrains(4);

        Room room = new Room();

        MeetingAssignment meetingAssignment = new MeetingAssignment("0", meeting, startingTimeGrain, room);

        TimeGrain timeGrain = new TimeGrain();
        timeGrain.setGrainIndex(3);
        timeGrain.setDayOfYear(0);

        constraintVerifier.verifyThat(MeetingSchedulingConstraintProvider::startAndEndOnSameDay)
                .given(meetingAssignment, timeGrain)
                .penalizesBy(0);
    }

    @Test
    void startAndEndOnSameDayPenalized() {
        TimeGrain startingTimeGrain = new TimeGrain();
        startingTimeGrain.setGrainIndex(0);
        startingTimeGrain.setDayOfYear(0);

        Meeting meeting = new Meeting();
        meeting.setDurationInGrains(4);

        Room room = new Room();

        MeetingAssignment meetingAssignment = new MeetingAssignment("0", meeting, startingTimeGrain, room);

        TimeGrain timeGrain = new TimeGrain();
        timeGrain.setGrainIndex(3);

        constraintVerifier.verifyThat(MeetingSchedulingConstraintProvider::startAndEndOnSameDay)
                .given(meetingAssignment, timeGrain)
                .penalizesBy(1);
    }

    @Test
    void requiredAndPreferredAttendanceConflictUnpenalized() {
        Person person = new Person("1");

        Meeting leftMeeting = new Meeting();
        leftMeeting.setDurationInGrains(4);

        RequiredAttendance requiredAttendance = new RequiredAttendance();
        requiredAttendance.setPerson(person);
        requiredAttendance.setMeeting(leftMeeting);

        Meeting rightMeeting = new Meeting();
        rightMeeting.setDurationInGrains(4);

        PreferredAttendance preferredAttendance = new PreferredAttendance();
        preferredAttendance.setPerson(person);
        preferredAttendance.setMeeting(rightMeeting);

        TimeGrain leftTimeGrain = new TimeGrain();
        leftTimeGrain.setGrainIndex(0);

        Room room = new Room();

        MeetingAssignment leftAssignment = new MeetingAssignment("0", leftMeeting, leftTimeGrain, room);

        TimeGrain rightTimeGrain = new TimeGrain();
        rightTimeGrain.setGrainIndex(4);

        MeetingAssignment rightAssignment = new MeetingAssignment("1", rightMeeting, rightTimeGrain, room);

        constraintVerifier.verifyThat(MeetingSchedulingConstraintProvider::requiredAndPreferredAttendanceConflict)
                .given(requiredAttendance, preferredAttendance, leftAssignment, rightAssignment)
                .penalizesBy(0);
    }

    @Test
    void requiredAndPreferredAttendanceConflictPenalized() {
        Person person = new Person("1");

        Meeting leftMeeting = new Meeting();
        leftMeeting.setDurationInGrains(4);

        RequiredAttendance requiredAttendance = new RequiredAttendance();
        requiredAttendance.setPerson(person);
        requiredAttendance.setMeeting(leftMeeting);

        Meeting rightMeeting = new Meeting();
        rightMeeting.setDurationInGrains(4);

        PreferredAttendance preferredAttendance = new PreferredAttendance();
        preferredAttendance.setPerson(person);
        preferredAttendance.setMeeting(rightMeeting);

        TimeGrain leftTimeGrain = new TimeGrain();
        leftTimeGrain.setGrainIndex(0);

        Room room = new Room();

        MeetingAssignment leftAssignment = new MeetingAssignment("0", leftMeeting, leftTimeGrain, room);

        TimeGrain rightTimeGrain = new TimeGrain();
        rightTimeGrain.setGrainIndex(0);

        MeetingAssignment rightAssignment = new MeetingAssignment("1", rightMeeting, rightTimeGrain, room);

        constraintVerifier.verifyThat(MeetingSchedulingConstraintProvider::requiredAndPreferredAttendanceConflict)
                .given(requiredAttendance, preferredAttendance, leftAssignment, rightAssignment)
                .penalizesBy(4);
    }

    @Test
    void preferredAttendanceConflictUnpenalized() {
        Person person = new Person("1");

        Meeting leftMeeting = new Meeting();
        leftMeeting.setDurationInGrains(4);

        PreferredAttendance leftAttendance = new PreferredAttendance("0", leftMeeting);
        leftAttendance.setPerson(person);

        Meeting rightMeeting = new Meeting();
        rightMeeting.setDurationInGrains(4);

        PreferredAttendance rightAttendance = new PreferredAttendance("1", rightMeeting);
        rightAttendance.setPerson(person);

        TimeGrain leftTimeGrain = new TimeGrain();
        leftTimeGrain.setGrainIndex(0);

        Room room = new Room();

        MeetingAssignment leftAssignment = new MeetingAssignment("0", leftMeeting, leftTimeGrain, room);

        TimeGrain rightTimeGrain = new TimeGrain();
        rightTimeGrain.setGrainIndex(4);

        MeetingAssignment rightAssignment = new MeetingAssignment("1", rightMeeting, rightTimeGrain, room);

        constraintVerifier.verifyThat(MeetingSchedulingConstraintProvider::preferredAttendanceConflict)
                .given(leftAttendance, rightAttendance, leftAssignment, rightAssignment)
                .penalizesBy(0);
    }

    @Test
    void preferredAttendanceConflictPenalized() {
        Person person = new Person("1");

        Meeting leftMeeting = new Meeting();
        leftMeeting.setDurationInGrains(4);

        PreferredAttendance leftAttendance = new PreferredAttendance("0", leftMeeting);
        leftAttendance.setPerson(person);

        Meeting rightMeeting = new Meeting();
        rightMeeting.setDurationInGrains(4);

        PreferredAttendance rightAttendance = new PreferredAttendance("1", rightMeeting);
        rightAttendance.setPerson(person);

        TimeGrain leftTimeGrain = new TimeGrain();
        leftTimeGrain.setGrainIndex(0);

        Room room = new Room();

        MeetingAssignment leftAssignment = new MeetingAssignment("0", leftMeeting, leftTimeGrain, room);

        TimeGrain rightTimeGrain = new TimeGrain();
        rightTimeGrain.setGrainIndex(0);

        MeetingAssignment rightAssignment = new MeetingAssignment("1", rightMeeting, rightTimeGrain, room);

        constraintVerifier.verifyThat(MeetingSchedulingConstraintProvider::preferredAttendanceConflict)
                .given(leftAttendance, rightAttendance, leftAssignment, rightAssignment)
                .penalizesBy(4);
    }

    @Test
    void doMeetingsAsSoonAsPossibleUnpenalized() {
        TimeGrain timeGrain = new TimeGrain();
        timeGrain.setGrainIndex(0);

        Meeting meeting = new Meeting();
        meeting.setDurationInGrains(1);

        Room room = new Room();

        MeetingAssignment meetingAssignment = new MeetingAssignment("0", meeting, timeGrain, room);

        constraintVerifier.verifyThat(MeetingSchedulingConstraintProvider::doMeetingsAsSoonAsPossible)
                .given(meetingAssignment)
                .penalizesBy(0);
    }

    @Test
    void doMeetingsAsSoonAsPossiblePenalized() {
        TimeGrain timeGrain = new TimeGrain();
        timeGrain.setGrainIndex(0);

        Meeting meeting = new Meeting();
        meeting.setDurationInGrains(4);

        Room room = new Room();

        MeetingAssignment meetingAssignment = new MeetingAssignment("0", meeting, timeGrain, room);

        constraintVerifier.verifyThat(MeetingSchedulingConstraintProvider::doMeetingsAsSoonAsPossible)
                .given(meetingAssignment)
                .penalizesBy(3);
    }

    @Test
    void oneBreakBetweenConsecutiveMeetingsUnpenalized() {
        TimeGrain leftTimeGrain = new TimeGrain();
        leftTimeGrain.setGrainIndex(0);

        Meeting meeting = new Meeting();
        meeting.setDurationInGrains(4);

        Room room = new Room();

        MeetingAssignment leftAssignment = new MeetingAssignment("0", meeting, leftTimeGrain, room);

        TimeGrain rightTimeGrain = new TimeGrain();
        rightTimeGrain.setGrainIndex(0);

        MeetingAssignment rightAssignment = new MeetingAssignment("1", meeting, rightTimeGrain, room);

        constraintVerifier.verifyThat(MeetingSchedulingConstraintProvider::oneBreakBetweenConsecutiveMeetings)
                .given(leftAssignment, rightAssignment)
                .penalizesBy(0);
    }

    @Test
    void oneBreakBetweenConsecutiveMeetingsPenalized() {
        TimeGrain leftTimeGrain = new TimeGrain();
        leftTimeGrain.setGrainIndex(0);

        Meeting meeting = new Meeting();
        meeting.setDurationInGrains(4);

        Room room = new Room();

        MeetingAssignment leftAssignment = new MeetingAssignment("0", meeting, leftTimeGrain, room);

        TimeGrain rightTimeGrain = new TimeGrain();
        rightTimeGrain.setGrainIndex(4);

        MeetingAssignment rightAssignment = new MeetingAssignment("1", meeting, rightTimeGrain, room);

        constraintVerifier.verifyThat(MeetingSchedulingConstraintProvider::oneBreakBetweenConsecutiveMeetings)
                .given(leftAssignment, rightAssignment)
                .penalizesBy(1);
    }

    @Test
    void overlappingMeetingsUnpenalized() {
        TimeGrain leftTimeGrain = new TimeGrain();
        leftTimeGrain.setGrainIndex(0);

        Meeting leftMeeting = new Meeting("1");
        leftMeeting.setDurationInGrains(4);

        Room room = new Room();

        MeetingAssignment leftAssignment = new MeetingAssignment("0", leftMeeting, leftTimeGrain, room);

        TimeGrain rightTimeGrain = new TimeGrain();
        rightTimeGrain.setGrainIndex(4);

        Meeting rightMeeting = new Meeting("0");
        rightMeeting.setDurationInGrains(4);

        MeetingAssignment rightAssignment = new MeetingAssignment("1", rightMeeting, rightTimeGrain, room);

        constraintVerifier.verifyThat(MeetingSchedulingConstraintProvider::overlappingMeetings)
                .given(leftAssignment, rightAssignment)
                .penalizesBy(0);
    }

    @Test
    void overlappingMeetingsPenalized() {
        TimeGrain leftTimeGrain = new TimeGrain();
        leftTimeGrain.setGrainIndex(1);

        Meeting leftMeeting = new Meeting("1");
        leftMeeting.setDurationInGrains(3);

        Room room = new Room();

        MeetingAssignment leftAssignment = new MeetingAssignment("0", leftMeeting, leftTimeGrain, room);

        TimeGrain rightTimeGrain = new TimeGrain();
        rightTimeGrain.setGrainIndex(0);

        Meeting rightMeeting = new Meeting("0");
        rightMeeting.setDurationInGrains(3);

        MeetingAssignment rightAssignment = new MeetingAssignment("1", rightMeeting, rightTimeGrain, room);

        constraintVerifier.verifyThat(MeetingSchedulingConstraintProvider::overlappingMeetings)
                .given(leftAssignment, rightAssignment)
                .penalizesBy(2);
    }

    @Test
    void assignLargerRoomsFirstUnpenalized() {
        Room meetingRoom = new Room();
        meetingRoom.setCapacity(1);

        Meeting meeting = new Meeting();

        TimeGrain startingTimeGrain = new TimeGrain();

        MeetingAssignment meetingAssignment = new MeetingAssignment("0", meeting, startingTimeGrain, meetingRoom);
        meetingAssignment.setRoom(meetingRoom);

        constraintVerifier.verifyThat(MeetingSchedulingConstraintProvider::assignLargerRoomsFirst)
                .given(meetingAssignment)
                .penalizesBy(0);
    }

    @Test
    void assignLargerRoomsFirstPenalized() {
        Room meetingRoom = new Room("1");
        meetingRoom.setCapacity(1);

        Meeting meeting = new Meeting();

        TimeGrain startingTimeGrain = new TimeGrain();

        MeetingAssignment meetingAssignment = new MeetingAssignment("0", meeting, startingTimeGrain, meetingRoom);

        Room largerRoom = new Room("2");
        largerRoom.setCapacity(2);

        constraintVerifier.verifyThat(MeetingSchedulingConstraintProvider::assignLargerRoomsFirst)
                .given(meetingAssignment, largerRoom)
                .penalizesBy(1);
    }

    @Test
    void roomStabilityUnpenalized() {
        Person person = new Person("1");

        Meeting leftMeeting = new Meeting();
        leftMeeting.setDurationInGrains(4);

        RequiredAttendance leftAttendance = new RequiredAttendance();
        leftAttendance.setMeeting(leftMeeting);
        leftAttendance.setPerson(person);

        Meeting rightMeeting = new Meeting();
        rightMeeting.setDurationInGrains(4);

        RequiredAttendance rightAttendance = new RequiredAttendance();
        rightAttendance.setMeeting(rightMeeting);
        rightAttendance.setPerson(person);

        TimeGrain leftStartTimeGrain = new TimeGrain();
        leftStartTimeGrain.setDayOfYear(1);
        leftStartTimeGrain.setStartingMinuteOfDay(0);
        leftStartTimeGrain.setGrainIndex(0);

        Room leftRoom = new Room();

        MeetingAssignment leftAssignment = new MeetingAssignment("0", leftMeeting, leftStartTimeGrain, leftRoom);

        TimeGrain rightStartTimeGrain = new TimeGrain();
        rightStartTimeGrain.setDayOfYear(1);
        rightStartTimeGrain.setStartingMinuteOfDay(8 * TimeGrain.GRAIN_LENGTH_IN_MINUTES);
        rightStartTimeGrain.setGrainIndex(8);

        Room rightRoom = new Room();

        MeetingAssignment rightAssignment = new MeetingAssignment("1", rightMeeting, rightStartTimeGrain, rightRoom);

        constraintVerifier.verifyThat(MeetingSchedulingConstraintProvider::roomStability)
                .given(leftAttendance, rightAttendance, leftAssignment, rightAssignment)
                .penalizesBy(0);
    }

    @Test
    void roomStabilityPenalized() {
        Person person = new Person("1");

        Meeting leftMeeting = new Meeting();
        leftMeeting.setDurationInGrains(4);

        RequiredAttendance leftAttendance = new RequiredAttendance();
        leftAttendance.setMeeting(leftMeeting);
        leftAttendance.setPerson(person);

        Meeting rightMeeting = new Meeting();
        rightMeeting.setDurationInGrains(4);

        RequiredAttendance rightAttendance = new RequiredAttendance();
        rightAttendance.setMeeting(rightMeeting);
        rightAttendance.setPerson(person);

        TimeGrain leftStartTimeGrain = new TimeGrain();
        leftStartTimeGrain.setDayOfYear(1);
        leftStartTimeGrain.setStartingMinuteOfDay(0);
        leftStartTimeGrain.setGrainIndex(0);

        Room leftRoom = new Room();

        MeetingAssignment leftAssignment = new MeetingAssignment("0", leftMeeting, leftStartTimeGrain, leftRoom);

        TimeGrain rightStartTimeGrain = new TimeGrain();
        rightStartTimeGrain.setDayOfYear(1);
        rightStartTimeGrain.setStartingMinuteOfDay(4 * TimeGrain.GRAIN_LENGTH_IN_MINUTES);
        rightStartTimeGrain.setGrainIndex(4);

        Room rightRoom = new Room();

        MeetingAssignment rightAssignment = new MeetingAssignment("1", rightMeeting, rightStartTimeGrain, rightRoom);

        constraintVerifier.verifyThat(MeetingSchedulingConstraintProvider::roomStability)
                .given(leftAttendance, rightAttendance, leftAssignment, rightAssignment)
                .penalizesBy(1);
    }
}
