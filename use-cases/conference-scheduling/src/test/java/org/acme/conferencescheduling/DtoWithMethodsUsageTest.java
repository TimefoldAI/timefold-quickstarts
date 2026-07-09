package org.acme.conferencescheduling;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.acme.conferencescheduling.dto.TalkTypeDTO;
import org.acme.conferencescheduling.dto.TimeslotDTO;
import org.acme.conferencescheduling.dto.RoomDTO;
import org.acme.conferencescheduling.dto.SpeakerDTO;
import org.acme.conferencescheduling.dto.TalkDTO;
import org.acme.conferencescheduling.dto.ConferenceScheduleInput;
import org.acme.conferencescheduling.dto.ConferenceScheduleOutput;
import org.acme.conferencescheduling.dto.ConferenceScheduleInputMetrics;
import org.acme.conferencescheduling.dto.ConferenceScheduleOutputMetrics;
import org.acme.conferencescheduling.dto.ConferenceScheduleConfigOverrides;
import org.acme.conferencescheduling.dto.TalkIdDetail;
import org.acme.conferencescheduling.dto.SpeakerIdDetail;
import org.acme.conferencescheduling.dto.RoomIdDetail;
import org.acme.conferencescheduling.dto.TimeslotIdDetail;
import org.junit.jupiter.api.Test;

class DtoWithMethodsUsageTest {

    @Test
    void allWithMethodsProduceUpdatedCopies() {
        var talkTypeDTO = new TalkTypeDTO("t")
                .withName("n");
        assertThat(talkTypeDTO).isNotNull();
        var timeslotDTO = new TimeslotDTO("t1", "2024-01-01T09:00", "2024-01-01T10:00", List.of("Lab"), List.of("a"))
                .withId("t2")
                .withStartDateTime("2024-01-01T11:00")
                .withEndDateTime("2024-01-01T12:00")
                .withTalkTypeNames(List.of("Breakout"))
                .withTags(List.of("b"));
        assertThat(timeslotDTO).isNotNull();
        var roomDTO = new RoomDTO("r1", "Room A", 10, List.of("Lab"), List.of("t1"), List.of("a"))
                .withId("r2")
                .withName("Room B")
                .withCapacity(20)
                .withTalkTypeNames(List.of("Breakout"))
                .withUnavailableTimeslotIds(List.of("t2"))
                .withTags(List.of("b"));
        assertThat(roomDTO).isNotNull();
        var speakerDTO = new SpeakerDTO("s1", "Amy", List.of(), List.of(), List.of(), List.of(), List.of(), List.of(),
                List.of(), List.of(), List.of())
                .withId("s2")
                .withName("Bob")
                .withUnavailableTimeslotIds(List.of("x"))
                .withRequiredTimeslotTags(List.of("x"))
                .withPreferredTimeslotTags(List.of("x"))
                .withProhibitedTimeslotTags(List.of("x"))
                .withUndesiredTimeslotTags(List.of("x"))
                .withRequiredRoomTags(List.of("x"))
                .withPreferredRoomTags(List.of("x"))
                .withProhibitedRoomTags(List.of("x"))
                .withUndesiredRoomTags(List.of("x"));
        assertThat(speakerDTO).isNotNull();
        var talkDTO = new TalkDTO("S01", "Title", "Lab", List.of("s1"), List.of(), List.of(), List.of(), 1, List.of(), "en",
                List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), 5,
                0, "t1", "r1")
                .withCode("S02")
                .withTitle("T2")
                .withTalkTypeName("Breakout")
                .withLanguage("fr")
                .withSpeakerIds(List.of("x"))
                .withThemeTrackTags(List.of("x"))
                .withSectorTags(List.of("x"))
                .withAudienceTypes(List.of("x"))
                .withContentTags(List.of("x"))
                .withRequiredTimeslotTags(List.of("x"))
                .withPreferredTimeslotTags(List.of("x"))
                .withProhibitedTimeslotTags(List.of("x"))
                .withUndesiredTimeslotTags(List.of("x"))
                .withRequiredRoomTags(List.of("x"))
                .withPreferredRoomTags(List.of("x"))
                .withProhibitedRoomTags(List.of("x"))
                .withUndesiredRoomTags(List.of("x"))
                .withMutuallyExclusiveTalksTags(List.of("x"))
                .withPrerequisiteTalkCodes(List.of("x"))
                .withAudienceLevel(2)
                .withFavoriteCount(9)
                .withCrowdControlRisk(1)
                .withTimeslotId("t2")
                .withRoomId("r2");
        assertThat(talkDTO).isNotNull();
        var conferenceScheduleInput = new ConferenceScheduleInput("Conf", List.of(), List.of(), List.of(), List.of(), List.of())
                .withName("C2")
                .withTalkTypes(List.of())
                .withTimeslots(List.of())
                .withRooms(List.of())
                .withSpeakers(List.of())
                .withTalks(List.of());
        assertThat(conferenceScheduleInput).isNotNull();
        var conferenceScheduleOutput =
                new ConferenceScheduleOutput("Conf", List.of(), List.of(), List.of(), List.of(), List.of(), "0hard/0soft")
                        .withName("C2")
                        .withTalkTypes(List.of())
                        .withTimeslots(List.of())
                        .withRooms(List.of())
                        .withSpeakers(List.of())
                        .withTalks(List.of())
                        .withScore("1hard/1soft");
        assertThat(conferenceScheduleOutput).isNotNull();
        var conferenceScheduleInputMetrics = new ConferenceScheduleInputMetrics(1, 2, 3, 4, 5)
                .withTalks(10)
                .withSpeakers(20)
                .withRooms(30)
                .withTimeslots(40)
                .withTalkTypes(50);
        assertThat(conferenceScheduleInputMetrics).isNotNull();
        var conferenceScheduleOutputMetrics = new ConferenceScheduleOutputMetrics(1, 2, 3, 4)
                .withTotalScheduledTalks(10)
                .withTotalUnscheduledTalks(20)
                .withTotalUsedRooms(30)
                .withTotalUsedTimeslots(40);
        assertThat(conferenceScheduleOutputMetrics).isNotNull();
        var conferenceScheduleConfigOverrides = new ConferenceScheduleConfigOverrides()
                .withThemeTrackConflictWeight(5L)
                .withThemeTrackRoomStabilityWeight(5L)
                .withSectorConflictWeight(5L)
                .withAudienceTypeDiversityWeight(5L)
                .withAudienceTypeThemeTrackConflictWeight(5L)
                .withAudienceLevelDiversityWeight(5L)
                .withContentAudienceLevelFlowViolationWeight(5L)
                .withContentConflictWeight(5L)
                .withLanguageDiversityWeight(5L)
                .withSameDayTalksWeight(5L)
                .withPopularTalksWeight(5L)
                .withSpeakerPreferredTimeslotTagsWeight(5L)
                .withSpeakerUndesiredTimeslotTagsWeight(5L)
                .withTalkPreferredTimeslotTagsWeight(5L)
                .withTalkUndesiredTimeslotTagsWeight(5L)
                .withSpeakerPreferredRoomTagsWeight(5L)
                .withSpeakerUndesiredRoomTagsWeight(5L)
                .withTalkPreferredRoomTagsWeight(5L)
                .withTalkUndesiredRoomTagsWeight(5L)
                .withSpeakerMakespanWeight(5L);
        assertThat(conferenceScheduleConfigOverrides).isNotNull();
        var talkIdDetail = new TalkIdDetail("S01")
                .withTalkId("S02");
        assertThat(talkIdDetail).isNotNull();
        var speakerIdDetail = new SpeakerIdDetail("s1")
                .withSpeakerId("s2");
        assertThat(speakerIdDetail).isNotNull();
        var roomIdDetail = new RoomIdDetail("r1")
                .withRoomId("r2");
        assertThat(roomIdDetail).isNotNull();
        var timeslotIdDetail = new TimeslotIdDetail("t1")
                .withTimeslotId("t2");
        assertThat(timeslotIdDetail).isNotNull();
    }
}
