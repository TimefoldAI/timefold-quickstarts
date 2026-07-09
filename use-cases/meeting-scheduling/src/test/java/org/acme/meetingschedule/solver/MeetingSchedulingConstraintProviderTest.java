package org.acme.meetingschedule.solver;

import ai.timefold.solver.core.api.score.stream.test.ConstraintVerifier;

import org.acme.meetingschedule.domain.Meeting;
import org.acme.meetingschedule.domain.MeetingAssignment;
import org.acme.meetingschedule.domain.MeetingSchedule;
import org.acme.meetingschedule.domain.Person;
import org.acme.meetingschedule.domain.PreferredAttendance;
import org.acme.meetingschedule.domain.RequiredAttendance;
import org.acme.meetingschedule.domain.Room;
import org.acme.meetingschedule.domain.TimeGrain;
import org.junit.jupiter.api.Test;

class MeetingSchedulingConstraintProviderTest {

    private static final Room ROOM1 = new Room("R1", "Room 1", 20);
    private static final Room ROOM2 = new Room("R2", "Room 2", 20);
    private static final Room SMALL_ROOM = new Room("R3", "Small", 1);
    private static final Room LARGE_ROOM = new Room("R4", "Large", 30);

    private static final TimeGrain GRAIN0 = new TimeGrain("0", 0, 100, 480);
    private static final TimeGrain GRAIN2 = new TimeGrain("2", 2, 100, 510);
    private static final TimeGrain GRAIN3 = new TimeGrain("3", 3, 100, 525);
    private static final TimeGrain GRAIN4 = new TimeGrain("4", 4, 100, 540);
    private static final TimeGrain NEXT_DAY_GRAIN = new TimeGrain("9", 1, 101, 480);

    private final ConstraintVerifier<MeetingSchedulingConstraintProvider, MeetingSchedule> constraintVerifier =
            ConstraintVerifier.build(new MeetingSchedulingConstraintProvider(), MeetingSchedule.class,
                    MeetingAssignment.class);

    private static Meeting meeting(String id, int duration) {
        return new Meeting(id, "Topic " + id, duration);
    }

    @Test
    void roomConflict() {
        Meeting first = meeting("1", 2);
        Meeting second = meeting("2", 2);
        MeetingAssignment left = new MeetingAssignment("1", first, GRAIN0, ROOM1);
        MeetingAssignment right = new MeetingAssignment("2", second, GRAIN0, ROOM1);
        MeetingAssignment nonConflicting = new MeetingAssignment("3", meeting("3", 2), GRAIN0, ROOM2);
        constraintVerifier.verifyThat(MeetingSchedulingConstraintProvider::roomConflict)
                .given(left, right, nonConflicting)
                .penalizesBy(2);
    }

    @Test
    void avoidOvertime() {
        Meeting first = meeting("1", 2);
        MeetingAssignment assignment = new MeetingAssignment("1", first, GRAIN3, ROOM1);
        constraintVerifier.verifyThat(MeetingSchedulingConstraintProvider::avoidOvertime)
                .given(assignment)
                .penalizesBy(4);
    }

    @Test
    void requiredAttendanceConflict() {
        Person person = new Person("p1", "Person 1");
        Meeting first = meeting("1", 2);
        Meeting second = meeting("2", 2);
        RequiredAttendance firstAttendance = new RequiredAttendance("a1", first, person);
        RequiredAttendance secondAttendance = new RequiredAttendance("a2", second, person);
        MeetingAssignment left = new MeetingAssignment("1", first, GRAIN0, ROOM1);
        MeetingAssignment right = new MeetingAssignment("2", second, GRAIN0, ROOM2);
        constraintVerifier.verifyThat(MeetingSchedulingConstraintProvider::requiredAttendanceConflict)
                .given(firstAttendance, secondAttendance, left, right)
                .penalizesBy(2);
    }

    @Test
    void requiredRoomCapacity() {
        Meeting first = meeting("1", 2);
        first.addRequiredAttendant(new Person("p1", "Person 1"));
        first.addRequiredAttendant(new Person("p2", "Person 2"));
        MeetingAssignment assignment = new MeetingAssignment("1", first, GRAIN0, SMALL_ROOM);
        constraintVerifier.verifyThat(MeetingSchedulingConstraintProvider::requiredRoomCapacity)
                .given(assignment)
                .penalizesBy(1);
    }

    @Test
    void startAndEndOnSameDay() {
        Meeting first = meeting("1", 2);
        MeetingAssignment assignment = new MeetingAssignment("1", first, GRAIN0, ROOM1);
        constraintVerifier.verifyThat(MeetingSchedulingConstraintProvider::startAndEndOnSameDay)
                .given(assignment, NEXT_DAY_GRAIN)
                .penalizesBy(1);
    }

    @Test
    void requiredAndPreferredAttendanceConflict() {
        Person person = new Person("p1", "Person 1");
        Meeting first = meeting("1", 2);
        Meeting second = meeting("2", 2);
        RequiredAttendance required = new RequiredAttendance("a1", first, person);
        PreferredAttendance preferred = new PreferredAttendance("a2", second, person);
        MeetingAssignment left = new MeetingAssignment("1", first, GRAIN0, ROOM1);
        MeetingAssignment right = new MeetingAssignment("2", second, GRAIN0, ROOM2);
        constraintVerifier.verifyThat(MeetingSchedulingConstraintProvider::requiredAndPreferredAttendanceConflict)
                .given(required, preferred, left, right)
                .penalizesBy(2);
    }

    @Test
    void preferredAttendanceConflict() {
        Person person = new Person("p1", "Person 1");
        Meeting first = meeting("1", 2);
        Meeting second = meeting("2", 2);
        PreferredAttendance firstAttendance = new PreferredAttendance("a1", first, person);
        PreferredAttendance secondAttendance = new PreferredAttendance("a2", second, person);
        MeetingAssignment left = new MeetingAssignment("1", first, GRAIN0, ROOM1);
        MeetingAssignment right = new MeetingAssignment("2", second, GRAIN0, ROOM2);
        constraintVerifier.verifyThat(MeetingSchedulingConstraintProvider::preferredAttendanceConflict)
                .given(firstAttendance, secondAttendance, left, right)
                .penalizesBy(2);
    }

    @Test
    void doMeetingsAsSoonAsPossible() {
        Meeting first = meeting("1", 2);
        MeetingAssignment assignment = new MeetingAssignment("1", first, GRAIN2, ROOM1);
        constraintVerifier.verifyThat(MeetingSchedulingConstraintProvider::doMeetingsAsSoonAsPossible)
                .given(assignment)
                .penalizesBy(3);
    }

    @Test
    void oneBreakBetweenConsecutiveMeetings() {
        Meeting first = meeting("1", 2);
        Meeting second = meeting("2", 2);
        // first occupies grains 0..1, so getLastTimeGrainIndex()==1; second starts at grain 2 => 2-1==1 matches.
        MeetingAssignment left = new MeetingAssignment("1", first, GRAIN0, ROOM1);
        MeetingAssignment right = new MeetingAssignment("2", second, GRAIN2, ROOM2);
        constraintVerifier.verifyThat(MeetingSchedulingConstraintProvider::oneBreakBetweenConsecutiveMeetings)
                .given(left, right)
                .penalizesBy(1);
    }

    @Test
    void overlappingMeetings() {
        Meeting first = meeting("1", 3);
        Meeting second = meeting("2", 3);
        MeetingAssignment left = new MeetingAssignment("1", first, GRAIN0, ROOM1);
        MeetingAssignment right = new MeetingAssignment("2", second, GRAIN2, ROOM2);
        constraintVerifier.verifyThat(MeetingSchedulingConstraintProvider::overlappingMeetings)
                .given(left, right)
                .penalizesByMoreThan(0);
    }

    @Test
    void assignLargerRoomsFirst() {
        Meeting first = meeting("1", 2);
        MeetingAssignment assignment = new MeetingAssignment("1", first, GRAIN0, SMALL_ROOM);
        constraintVerifier.verifyThat(MeetingSchedulingConstraintProvider::assignLargerRoomsFirst)
                .given(assignment, LARGE_ROOM)
                .penalizesByMoreThan(0);
    }

    @Test
    void roomStability() {
        Person person = new Person("p1", "Person 1");
        Meeting first = meeting("1", 2);
        Meeting second = meeting("2", 2);
        RequiredAttendance firstAttendance = new RequiredAttendance("a1", first, person);
        RequiredAttendance secondAttendance = new RequiredAttendance("a2", second, person);
        MeetingAssignment left = new MeetingAssignment("1", first, GRAIN0, ROOM1);
        MeetingAssignment right = new MeetingAssignment("2", second, GRAIN4, ROOM2);
        constraintVerifier.verifyThat(MeetingSchedulingConstraintProvider::roomStability)
                .given(firstAttendance, secondAttendance, left, right)
                .penalizesBy(1);
    }
}
