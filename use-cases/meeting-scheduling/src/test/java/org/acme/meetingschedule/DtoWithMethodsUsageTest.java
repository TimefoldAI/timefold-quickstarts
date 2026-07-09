package org.acme.meetingschedule;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.acme.meetingschedule.dto.MeetingAssignmentDTO;
import org.acme.meetingschedule.dto.MeetingAssignmentIdDetail;
import org.acme.meetingschedule.dto.MeetingDTO;
import org.acme.meetingschedule.dto.MeetingIdDetail;
import org.acme.meetingschedule.dto.MeetingScheduleConfigOverrides;
import org.acme.meetingschedule.dto.MeetingScheduleInput;
import org.acme.meetingschedule.dto.MeetingScheduleInputMetrics;
import org.acme.meetingschedule.dto.MeetingScheduleOutput;
import org.acme.meetingschedule.dto.MeetingScheduleOutputMetrics;
import org.acme.meetingschedule.dto.PersonDTO;
import org.acme.meetingschedule.dto.PersonIdDetail;
import org.acme.meetingschedule.dto.RoomDTO;
import org.acme.meetingschedule.dto.RoomIdDetail;
import org.acme.meetingschedule.dto.TimeGrainDTO;
import org.acme.meetingschedule.dto.TimeGrainIdDetail;
import org.junit.jupiter.api.Test;

class DtoWithMethodsUsageTest {

    @Test
    void allWithMethodsProduceUpdatedCopies() {
        var basePerson = new PersonDTO("p1", "Amy Cole");
        var updatedPerson = basePerson.withId("p2").withFullName("Beth Fox");

        var baseRoom = new RoomDTO("r1", "Room 1", 30);
        var updatedRoom = baseRoom.withId("r2").withName("Room 2").withCapacity(20);

        var baseTimeGrain = new TimeGrainDTO("t1", 1, 100, 480);
        var updatedTimeGrain = baseTimeGrain.withId("t2").withGrainIndex(2).withDayOfYear(101).withStartingMinuteOfDay(495);

        var baseMeeting = new MeetingDTO("m1", "Topic", 8, List.of("p1"), List.of("p2"));
        var updatedMeeting = baseMeeting.withId("m2")
                .withTopic("Other topic")
                .withDurationInGrains(12)
                .withRequiredAttendancePersonIds(List.of("p3"))
                .withPreferredAttendancePersonIds(List.of("p4"));

        var baseAssignment = new MeetingAssignmentDTO("a1", "m1", "", "", false);
        var updatedAssignment = baseAssignment.withId("a2")
                .withMeetingId("m2")
                .withStartingTimeGrainId("t2")
                .withRoomId("r2")
                .withPinned(true);

        var updatedMeetingIdDetail = new MeetingIdDetail("m1").withMeetingId("m2");
        var updatedRoomIdDetail = new RoomIdDetail("r1").withRoomId("r2");
        var updatedTimeGrainIdDetail = new TimeGrainIdDetail("t1").withTimeGrainId("t2");
        var updatedPersonIdDetail = new PersonIdDetail("p1").withPersonId("p2");
        var updatedAssignmentIdDetail = new MeetingAssignmentIdDetail("a1").withMeetingAssignmentId("a2");

        var updatedOverrides = new MeetingScheduleConfigOverrides()
                .withDoMeetingsAsSoonAsPossibleWeight(10L)
                .withOneBreakBetweenConsecutiveMeetingsWeight(20L)
                .withOverlappingMeetingsWeight(30L)
                .withAssignLargerRoomsFirstWeight(40L)
                .withRoomStabilityWeight(50L);

        var updatedInput = new MeetingScheduleInput(List.of(basePerson), List.of(baseTimeGrain), List.of(baseRoom),
                List.of(baseMeeting), List.of(baseAssignment))
                .withPeople(List.of(updatedPerson))
                .withTimeGrains(List.of(updatedTimeGrain))
                .withRooms(List.of(updatedRoom))
                .withMeetings(List.of(updatedMeeting))
                .withMeetingAssignments(List.of(updatedAssignment));

        var updatedOutput = new MeetingScheduleOutput(List.of(basePerson), List.of(baseTimeGrain), List.of(baseRoom),
                List.of(baseMeeting), List.of(baseAssignment), "0hard/0medium/0soft")
                .withPeople(List.of(updatedPerson))
                .withTimeGrains(List.of(updatedTimeGrain))
                .withRooms(List.of(updatedRoom))
                .withMeetings(List.of(updatedMeeting))
                .withMeetingAssignments(List.of(updatedAssignment))
                .withScore("1hard/0medium/0soft");

        var updatedInputMetrics = new MeetingScheduleInputMetrics(1, 2, 3, 4, 5)
                .withMeetings(10)
                .withMeetingAssignments(20)
                .withPeople(30)
                .withRooms(40)
                .withTimeGrains(50);

        var updatedOutputMetrics = new MeetingScheduleOutputMetrics(1, 2, 3)
                .withTotalAssignedMeetings(10)
                .withTotalUnassignedMeetings(20)
                .withTotalUsedRooms(30);

        assertThat(updatedPerson.id()).isEqualTo("p2");
        assertThat(updatedPerson.fullName()).isEqualTo("Beth Fox");
        assertThat(updatedRoom.id()).isEqualTo("r2");
        assertThat(updatedRoom.name()).isEqualTo("Room 2");
        assertThat(updatedRoom.capacity()).isEqualTo(20);
        assertThat(updatedTimeGrain.id()).isEqualTo("t2");
        assertThat(updatedTimeGrain.grainIndex()).isEqualTo(2);
        assertThat(updatedTimeGrain.dayOfYear()).isEqualTo(101);
        assertThat(updatedTimeGrain.startingMinuteOfDay()).isEqualTo(495);
        assertThat(updatedMeeting.id()).isEqualTo("m2");
        assertThat(updatedMeeting.topic()).isEqualTo("Other topic");
        assertThat(updatedMeeting.durationInGrains()).isEqualTo(12);
        assertThat(updatedMeeting.requiredAttendancePersonIds()).containsExactly("p3");
        assertThat(updatedMeeting.preferredAttendancePersonIds()).containsExactly("p4");
        assertThat(updatedAssignment.id()).isEqualTo("a2");
        assertThat(updatedAssignment.meetingId()).isEqualTo("m2");
        assertThat(updatedAssignment.startingTimeGrainId()).isEqualTo("t2");
        assertThat(updatedAssignment.roomId()).isEqualTo("r2");
        assertThat(updatedAssignment.pinned()).isTrue();
        assertThat(updatedMeetingIdDetail.meetingId()).isEqualTo("m2");
        assertThat(updatedRoomIdDetail.roomId()).isEqualTo("r2");
        assertThat(updatedTimeGrainIdDetail.timeGrainId()).isEqualTo("t2");
        assertThat(updatedPersonIdDetail.personId()).isEqualTo("p2");
        assertThat(updatedAssignmentIdDetail.meetingAssignmentId()).isEqualTo("a2");
        assertThat(updatedOverrides.doMeetingsAsSoonAsPossibleWeight()).isEqualTo(10L);
        assertThat(updatedOverrides.oneBreakBetweenConsecutiveMeetingsWeight()).isEqualTo(20L);
        assertThat(updatedOverrides.overlappingMeetingsWeight()).isEqualTo(30L);
        assertThat(updatedOverrides.assignLargerRoomsFirstWeight()).isEqualTo(40L);
        assertThat(updatedOverrides.roomStabilityWeight()).isEqualTo(50L);
        assertThat(updatedInput.people()).containsExactly(updatedPerson);
        assertThat(updatedInput.timeGrains()).containsExactly(updatedTimeGrain);
        assertThat(updatedInput.rooms()).containsExactly(updatedRoom);
        assertThat(updatedInput.meetings()).containsExactly(updatedMeeting);
        assertThat(updatedInput.meetingAssignments()).containsExactly(updatedAssignment);
        assertThat(updatedOutput.people()).containsExactly(updatedPerson);
        assertThat(updatedOutput.timeGrains()).containsExactly(updatedTimeGrain);
        assertThat(updatedOutput.rooms()).containsExactly(updatedRoom);
        assertThat(updatedOutput.meetings()).containsExactly(updatedMeeting);
        assertThat(updatedOutput.meetingAssignments()).containsExactly(updatedAssignment);
        assertThat(updatedOutput.score()).isEqualTo("1hard/0medium/0soft");
        assertThat(updatedInputMetrics.meetings()).isEqualTo(10);
        assertThat(updatedInputMetrics.meetingAssignments()).isEqualTo(20);
        assertThat(updatedInputMetrics.people()).isEqualTo(30);
        assertThat(updatedInputMetrics.rooms()).isEqualTo(40);
        assertThat(updatedInputMetrics.timeGrains()).isEqualTo(50);
        assertThat(updatedOutputMetrics.totalAssignedMeetings()).isEqualTo(10);
        assertThat(updatedOutputMetrics.totalUnassignedMeetings()).isEqualTo(20);
        assertThat(updatedOutputMetrics.totalUsedRooms()).isEqualTo(30);
    }
}
