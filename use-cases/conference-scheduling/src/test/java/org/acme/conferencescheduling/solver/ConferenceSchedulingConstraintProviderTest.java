package org.acme.conferencescheduling.solver;

import static java.util.Collections.singleton;
import static org.acme.conferencescheduling.support.TestHelper.aConfiguration;
import static org.acme.conferencescheduling.support.TestHelper.aRoom;
import static org.acme.conferencescheduling.support.TestHelper.aSpeaker;
import static org.acme.conferencescheduling.support.TestHelper.aTalk;
import static org.acme.conferencescheduling.support.TestHelper.aTimeslot;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.SequencedSet;
import java.util.Set;

import jakarta.inject.Inject;

import ai.timefold.solver.core.api.score.stream.test.ConstraintVerifier;

import org.acme.conferencescheduling.domain.ConferenceConstraintProperties;
import org.acme.conferencescheduling.domain.ConferenceSchedule;
import org.acme.conferencescheduling.domain.Talk;
import org.acme.conferencescheduling.domain.Timeslot;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class ConferenceSchedulingConstraintProviderTest {

    private static final OffsetDateTime START = OffsetDateTime.of(2000, 2, 1, 9, 0, 0, 0, ZoneOffset.UTC);

    private static final Timeslot MONDAY_9_TO_10 =
            aTimeslot("1").startDateTime(START).endDateTime(START.plusHours(1)).tags(sequencedSet("a")).build();
    private static final Timeslot MONDAY_10_05_TO_11 = aTimeslot("2")
            .startDateTime(MONDAY_9_TO_10.getEndDateTime().plusMinutes(5))
            .endDateTime(MONDAY_9_TO_10.getEndDateTime().plusHours(1))
            .tags(sequencedSet("b"))
            .build();
    private static final Timeslot MONDAY_11_10_TO_12 = aTimeslot("3")
            .startDateTime(MONDAY_10_05_TO_11.getEndDateTime().plusMinutes(10))
            .endDateTime(MONDAY_10_05_TO_11.getEndDateTime().plusHours(1))
            .tags(sequencedSet("c"))
            .build();
    private static final Timeslot TUESDAY_9_TO_10 = aTimeslot("4")
            .startDateTime(START.plusDays(1))
            .endDateTime(START.plusDays(1).plusHours(1))
            .tags(singleton("c"))
            .build();

    private static final Timeslot WEDNESDAY_9_TO_10 = aTimeslot("5")
            .startDateTime(START.plusDays(2))
            .endDateTime(START.plusDays(1).plusHours(1))
            .tags(sequencedSet("c"))
            .build();

    private final ConstraintVerifier<ConferenceSchedulingConstraintProvider, ConferenceSchedule> constraintVerifier;

    @Inject
    public ConferenceSchedulingConstraintProviderTest(
            ConstraintVerifier<ConferenceSchedulingConstraintProvider, ConferenceSchedule> constraintVerifier) {
        this.constraintVerifier = constraintVerifier;
    }

    @SafeVarargs
    private static <T> SequencedSet<T> sequencedSet(T... values) {
        return new LinkedHashSet<>(Set.of(values));
    }

    // ************************************************************************
    // Hard constraints
    // ************************************************************************

    @Test
    void roomUnavailableTimeslot() {
        var room1 = aRoom("1").unavailableTimeslots(sequencedSet(MONDAY_9_TO_10));
        var room2 = aRoom("2").unavailableTimeslots(sequencedSet(MONDAY_10_05_TO_11));
        Talk talk1 = aTalk("1").timeslot(MONDAY_9_TO_10).room(room1).build();
        Talk talk2 = aTalk("2").timeslot(MONDAY_9_TO_10).room(room2).build();

        constraintVerifier.verifyThat(ConferenceSchedulingConstraintProvider::roomUnavailableTimeslot)
                .given(talk1, talk2)
                .penalizesBy(MONDAY_9_TO_10.getDurationInMinutes()); // room1 is in an unavailable timeslot.
    }

    @Test
    void roomConflict() {
        var room1 = aRoom("1").unavailableTimeslots(sequencedSet(MONDAY_9_TO_10));
        Talk talk1 = aTalk("1").timeslot(MONDAY_9_TO_10).room(room1).build();
        Talk talk2 = aTalk("2").timeslot(MONDAY_9_TO_10).room(room1).build();
        Talk talk3 = aTalk("3").timeslot(MONDAY_10_05_TO_11).room(room1).build();

        constraintVerifier.verifyThat(ConferenceSchedulingConstraintProvider::roomConflict)
                .given(talk1, talk2, talk3)
                .penalizesBy(MONDAY_9_TO_10.getDurationInMinutes()); // talk1 and talk2 are in conflict.
    }

    @Test
    void speakerUnavailableTimeslot() {
        var room = aRoom("0");
        var speaker1 = aSpeaker("1").unavailableTimeslots(sequencedSet(MONDAY_9_TO_10));
        var speaker2 = aSpeaker("2").unavailableTimeslots(sequencedSet(MONDAY_10_05_TO_11));
        Talk talk1 = aTalk("1").timeslot(MONDAY_9_TO_10).room(room).speakers(List.of(speaker1)).build();
        Talk talk2 = aTalk("2").timeslot(MONDAY_9_TO_10).room(room).speakers(List.of(speaker2)).build();

        constraintVerifier.verifyThat(ConferenceSchedulingConstraintProvider::speakerUnavailableTimeslot)
                .given(talk1, talk2, speaker1.build(), speaker2.build())
                .penalizesBy(MONDAY_9_TO_10.getDurationInMinutes()); // speaker1 is in an unavailable timeslot.
    }

    @Test
    void speakerConflict() {
        var room = aRoom("0");
        var speaker = aSpeaker("1");
        Talk talk1 = aTalk("1").timeslot(MONDAY_9_TO_10).room(room).speakers(List.of(speaker)).build();
        Talk talk2 = aTalk("2").timeslot(MONDAY_9_TO_10).room(room).speakers(List.of(speaker)).build();
        Talk talk3 = aTalk("3").timeslot(MONDAY_10_05_TO_11).room(room).speakers(List.of(speaker)).build();

        constraintVerifier.verifyThat(ConferenceSchedulingConstraintProvider::speakerConflict)
                .given(speaker.build(), talk1, talk2, talk3)
                .penalizesBy(MONDAY_9_TO_10.getDurationInMinutes()); // talk1 and talk2 are in conflict.
    }

    @Test
    void talkPrerequisiteTalks() {
        var room = aRoom("0");
        Talk talk1 = aTalk("1").timeslot(MONDAY_9_TO_10).room(room).build();
        Talk talk2 = aTalk("2").timeslot(MONDAY_9_TO_10).room(room).prerequisiteTalks(sequencedSet(talk1)).build();
        Talk talk3 = aTalk("3").timeslot(MONDAY_10_05_TO_11).room(room).prerequisiteTalks(sequencedSet(talk1)).build();

        constraintVerifier.verifyThat(ConferenceSchedulingConstraintProvider::talkPrerequisiteTalks)
                .given(talk1, talk2, talk3)
                .penalizesBy(MONDAY_9_TO_10.getDurationInMinutes() * 2); // talk2 is not after talk1.
    }

    @Test
    void talkMutuallyExclusiveTalksTags() {
        var room = aRoom("0");
        Talk talk1 = aTalk("1").timeslot(MONDAY_9_TO_10).room(room).build();
        Talk talk2 = aTalk("2").timeslot(MONDAY_9_TO_10).room(room).mutuallyExclusiveTalksTags(sequencedSet("a", "b"))
                .build();
        Talk talk3 = aTalk("3").timeslot(MONDAY_9_TO_10).room(room)
                .mutuallyExclusiveTalksTags(sequencedSet("a", "b", "c")).build();

        constraintVerifier.verifyThat(ConferenceSchedulingConstraintProvider::talkMutuallyExclusiveTalksTags)
                .given(talk1, talk2, talk3)
                .penalizesBy(MONDAY_9_TO_10.getDurationInMinutes() * 2); // talk2 and talk3 excluded twice.
    }

    @Test
    void consecutiveTalksPause() {
        var room = aRoom("0");
        var speaker1 = aSpeaker("1");
        var speaker2 = aSpeaker("2");
        Talk talk1 = aTalk("1").timeslot(MONDAY_9_TO_10).room(room).speakers(List.of(speaker1)).build();
        Talk talk2 = aTalk("2").timeslot(MONDAY_10_05_TO_11).room(room).speakers(List.of(speaker1)).build();
        Talk talk3 = aTalk("3").timeslot(MONDAY_11_10_TO_12).room(room).speakers(List.of(speaker1)).build();
        Talk talk4 = aTalk("4").timeslot(MONDAY_9_TO_10).room(room).speakers(List.of(speaker2)).build();
        ConferenceConstraintProperties configuration = aConfiguration().minimumConsecutiveTalksPauseInMinutes(11).build();

        constraintVerifier.verifyThat(ConferenceSchedulingConstraintProvider::consecutiveTalksPause)
                .given(configuration, talk1, talk2, talk3, talk4)
                .penalizesBy(MONDAY_9_TO_10.getDurationInMinutes() + MONDAY_10_05_TO_11.getDurationInMinutes()
                        + MONDAY_10_05_TO_11.getDurationInMinutes() + MONDAY_11_10_TO_12.getDurationInMinutes()); // talk1+talk2 , talk2+talk3.
    }

    @Test
    void crowdControl() {
        var room = aRoom("0");
        Talk talk1 = aTalk("1").timeslot(MONDAY_9_TO_10).room(room).crowdControlRisk(1).build();
        Talk talk2 = aTalk("2").timeslot(MONDAY_9_TO_10).room(room).crowdControlRisk(1).build();
        Talk talk3 = aTalk("3").timeslot(MONDAY_9_TO_10).room(room).crowdControlRisk(1).build();
        Talk talk4 = aTalk("4").timeslot(MONDAY_10_05_TO_11).room(room).crowdControlRisk(1).build();
        Talk talk5 = aTalk("5").timeslot(MONDAY_10_05_TO_11).room(room).crowdControlRisk(1).build();
        Talk noRiskTalk = aTalk("6").timeslot(MONDAY_10_05_TO_11).room(room).build();

        constraintVerifier.verifyThat(ConferenceSchedulingConstraintProvider::crowdControl)
                .given(talk1, talk2, talk3, talk4, talk5, noRiskTalk)
                .penalizesBy(MONDAY_9_TO_10.getDurationInMinutes() * 3); // talk1, talk2, talk3.
    }

    @Test
    void speakerRequiredTimeslotTags() {
        var room = aRoom("0");
        var speaker1 = aSpeaker("1").requiredTimeslotTags(sequencedSet("a"));
        var speaker2 = aSpeaker("2").requiredTimeslotTags(sequencedSet("x"));
        Talk talk1 = aTalk("1").timeslot(MONDAY_9_TO_10).room(room).speakers(List.of(speaker1))
                .requiredTimeslotTags(sequencedSet("a", "b")).build();
        Talk talk2 = aTalk("2").timeslot(MONDAY_10_05_TO_11).room(room).speakers(List.of(speaker2)).build();

        constraintVerifier.verifyThat(ConferenceSchedulingConstraintProvider::speakerRequiredTimeslotTags)
                .given(talk1, talk2)
                .penalizesBy(MONDAY_10_05_TO_11.getDurationInMinutes());
    }

    @Test
    void speakerProhibitedTimeslotTags() {
        var room = aRoom("0");
        var speaker1 = aSpeaker("1").prohibitedTimeslotTags(sequencedSet("a"));
        var speaker2 = aSpeaker("2").prohibitedTimeslotTags(sequencedSet("x"));
        Talk talk1 = aTalk("1").timeslot(MONDAY_9_TO_10).room(room).speakers(List.of(speaker1))
                .prohibitedTimeslotTags(sequencedSet("a", "b")).build();
        Talk talk2 = aTalk("2").timeslot(MONDAY_10_05_TO_11).room(room).speakers(List.of(speaker2)).build();

        constraintVerifier.verifyThat(ConferenceSchedulingConstraintProvider::speakerProhibitedTimeslotTags)
                .given(talk1, talk2)
                .penalizesBy(MONDAY_9_TO_10.getDurationInMinutes());
    }

    @Test
    void talkRequiredTimeslotTags() {
        var room = aRoom("0");
        Talk talk1 = aTalk("1").timeslot(MONDAY_9_TO_10).room(room).requiredTimeslotTags(sequencedSet("a", "b")).build();
        Talk talk2 = aTalk("2").timeslot(MONDAY_10_05_TO_11).room(room).build();

        constraintVerifier.verifyThat(ConferenceSchedulingConstraintProvider::talkRequiredTimeslotTags)
                .given(talk1, talk2)
                .penalizesBy(MONDAY_9_TO_10.getDurationInMinutes());
    }

    @Test
    void talkProhibitedTimeslotTags() {
        var room = aRoom("0");
        Talk talk1 =
                aTalk("1").timeslot(MONDAY_9_TO_10).room(room).prohibitedTimeslotTags(sequencedSet("a", "b")).build();
        Talk talk2 = aTalk("2").timeslot(MONDAY_10_05_TO_11).room(room).build();

        constraintVerifier.verifyThat(ConferenceSchedulingConstraintProvider::talkProhibitedTimeslotTags)
                .given(talk1, talk2)
                .penalizesBy(MONDAY_9_TO_10.getDurationInMinutes());
    }

    @Test
    void speakerRequiredRoomTags() {
        var room = aRoom("0").tags(sequencedSet("a"));
        var speaker1 = aSpeaker("1").requiredRoomTags(sequencedSet("a"));
        var speaker2 = aSpeaker("2").requiredRoomTags(sequencedSet("x"));
        Talk talk1 = aTalk("1").timeslot(MONDAY_9_TO_10).room(room).speakers(List.of(speaker1)).build();
        Talk talk2 = aTalk("2").timeslot(MONDAY_10_05_TO_11).room(room).speakers(List.of(speaker2)).build();

        constraintVerifier.verifyThat(ConferenceSchedulingConstraintProvider::speakerRequiredRoomTags)
                .given(talk1, talk2)
                .penalizesBy(MONDAY_10_05_TO_11.getDurationInMinutes());
    }

    @Test
    void speakerProhibitedRoomTags() {
        var room = aRoom("0").tags(sequencedSet("a"));
        var speaker1 = aSpeaker("1").prohibitedRoomTags(sequencedSet("a"));
        var speaker2 = aSpeaker("2").prohibitedRoomTags(sequencedSet("x"));
        Talk talk1 = aTalk("1").timeslot(MONDAY_9_TO_10).room(room).speakers(List.of(speaker1)).build();
        Talk talk2 = aTalk("2").timeslot(MONDAY_10_05_TO_11).room(room).speakers(List.of(speaker2)).build();

        constraintVerifier.verifyThat(ConferenceSchedulingConstraintProvider::speakerProhibitedRoomTags)
                .given(talk1, talk2)
                .penalizesBy(MONDAY_9_TO_10.getDurationInMinutes());
    }

    @Test
    void talkRequiredRoomTags() {
        var room = aRoom("0").tags(sequencedSet("a"));
        Talk talk1 = aTalk("1").timeslot(MONDAY_9_TO_10).room(room).requiredRoomTags(sequencedSet("a", "b")).build();
        Talk talk2 = aTalk("2").timeslot(MONDAY_10_05_TO_11).room(room).build();

        constraintVerifier.verifyThat(ConferenceSchedulingConstraintProvider::talkRequiredRoomTags)
                .given(talk1, talk2)
                .penalizesBy(MONDAY_9_TO_10.getDurationInMinutes());
    }

    @Test
    void talkProhibitedRoomTags() {
        var room = aRoom("0").tags(sequencedSet("a"));
        Talk talk1 = aTalk("1").timeslot(MONDAY_9_TO_10).room(room).prohibitedRoomTags(sequencedSet("a", "b")).build();
        Talk talk2 = aTalk("2").timeslot(MONDAY_10_05_TO_11).room(room).build();

        constraintVerifier.verifyThat(ConferenceSchedulingConstraintProvider::talkProhibitedRoomTags)
                .given(talk1, talk2)
                .penalizesBy(MONDAY_9_TO_10.getDurationInMinutes());
    }

    // ************************************************************************
    // Soft constraints
    // ************************************************************************

    @Test
    void themeTrackConflict() {
        var room = aRoom("0");
        Talk talk1 = aTalk("1").timeslot(MONDAY_9_TO_10).room(room).themeTrackTags(sequencedSet("a")).build();
        Talk talk2 = aTalk("2").timeslot(MONDAY_9_TO_10).room(room).themeTrackTags(sequencedSet("a")).build();
        Talk talk3 = aTalk("3").timeslot(MONDAY_9_TO_10).room(room).themeTrackTags(sequencedSet("b")).build();
        Talk talk4 = aTalk("4").timeslot(MONDAY_10_05_TO_11).room(room).themeTrackTags(sequencedSet("a")).build();

        constraintVerifier.verifyThat(ConferenceSchedulingConstraintProvider::themeTrackConflict)
                .given(talk1, talk2, talk3, talk4)
                .penalizesBy(MONDAY_9_TO_10.getDurationInMinutes()); // overlap(talk1, talk2).
    }

    @Test
    void themeTrackRoomStability() {
        var room1 = aRoom("0");
        var room2 = aRoom("1");
        Talk talk1 = aTalk("1").timeslot(MONDAY_9_TO_10).room(room1).themeTrackTags(sequencedSet("a")).build();
        Talk talk2 = aTalk("2").timeslot(MONDAY_10_05_TO_11).room(room2).themeTrackTags(sequencedSet("a")).build();
        Talk talk3 = aTalk("3").timeslot(MONDAY_11_10_TO_12).room(room1).themeTrackTags(sequencedSet("b")).build();
        Talk talk4 = aTalk("4").timeslot(TUESDAY_9_TO_10).room(room2).themeTrackTags(sequencedSet("a")).build();

        constraintVerifier.verifyThat(ConferenceSchedulingConstraintProvider::themeTrackRoomStability)
                .given(talk1, talk2, talk3, talk4)
                .penalizesBy(MONDAY_9_TO_10.getDurationInMinutes() + MONDAY_10_05_TO_11.getDurationInMinutes()); // talk1 + talk2.
    }

    @Test
    void sectorConflict() {
        var room = aRoom("0");
        Talk talk1 = aTalk("1").timeslot(MONDAY_9_TO_10).room(room).sectorTags(sequencedSet("a")).build();
        Talk talk2 = aTalk("2").timeslot(MONDAY_9_TO_10).room(room).sectorTags(sequencedSet("a")).build();
        Talk talk3 = aTalk("3").timeslot(MONDAY_9_TO_10).room(room).sectorTags(sequencedSet("b")).build();
        Talk talk4 = aTalk("4").timeslot(MONDAY_10_05_TO_11).room(room).sectorTags(sequencedSet("a")).build();

        constraintVerifier.verifyThat(ConferenceSchedulingConstraintProvider::sectorConflict)
                .given(talk1, talk2, talk3, talk4)
                .penalizesBy(MONDAY_9_TO_10.getDurationInMinutes()); // talk1 + talk2.
    }

    @Test
    void audienceTypeDiversity() {
        var room = aRoom("0");
        Talk talk1 = aTalk("1").timeslot(MONDAY_9_TO_10).room(room).audienceTypes(sequencedSet("a")).build();
        Talk talk2 = aTalk("2").timeslot(MONDAY_9_TO_10).room(room).audienceTypes(sequencedSet("a")).build();
        Talk talk3 = aTalk("3").timeslot(MONDAY_9_TO_10).room(room).audienceTypes(sequencedSet("b")).build();
        Talk talk4 = aTalk("4").timeslot(MONDAY_10_05_TO_11).room(room).audienceTypes(sequencedSet("b")).build();

        constraintVerifier.verifyThat(ConferenceSchedulingConstraintProvider::audienceTypeDiversity)
                .given(talk1, talk2, talk3, talk4)
                .rewardsWith(MONDAY_9_TO_10.getDurationInMinutes()); // talk1 + talk2.
    }

    @Test
    void audienceTypeThemeTrackConflict() {
        var room = aRoom("0");
        Talk talk1 = aTalk("1").timeslot(MONDAY_9_TO_10).room(room).audienceTypes(sequencedSet("a"))
                .themeTrackTags(sequencedSet("b")).build();
        Talk talk2 = aTalk("2").timeslot(MONDAY_9_TO_10).room(room).audienceTypes(sequencedSet("a"))
                .themeTrackTags(sequencedSet("a")).build();
        Talk talk3 = aTalk("3").timeslot(MONDAY_9_TO_10).room(room).audienceTypes(sequencedSet("b"))
                .themeTrackTags(sequencedSet("a")).build();
        Talk talk4 = aTalk("4").timeslot(MONDAY_10_05_TO_11).room(room).audienceTypes(sequencedSet("a"))
                .themeTrackTags(sequencedSet("a")).build();
        Talk talk5 = aTalk("5").timeslot(MONDAY_9_TO_10).room(room).audienceTypes(sequencedSet("a"))
                .themeTrackTags(sequencedSet("b")).build();
        Talk talk6 = aTalk("6").timeslot(MONDAY_9_TO_10).room(room).audienceTypes(sequencedSet("a"))
                .themeTrackTags(sequencedSet("c")).build();

        constraintVerifier.verifyThat(ConferenceSchedulingConstraintProvider::audienceTypeThemeTrackConflict)
                .given(talk1, talk2, talk3, talk4, talk5, talk6)
                .penalizesBy(MONDAY_9_TO_10.getDurationInMinutes()); // talk1 + talk5.
    }

    @Test
    void audienceLevelDiversity() {
        var room = aRoom("0");
        Talk talk1 = aTalk("1").timeslot(MONDAY_9_TO_10).room(room).audienceLevel(1).build();
        Talk talk2 = aTalk("2").timeslot(MONDAY_9_TO_10).room(room).audienceLevel(1).build();
        Talk talk3 = aTalk("3").timeslot(MONDAY_9_TO_10).room(room).audienceLevel(2).build();
        Talk talk4 = aTalk("4").timeslot(MONDAY_11_10_TO_12).room(room).audienceLevel(1).build();

        constraintVerifier.verifyThat(ConferenceSchedulingConstraintProvider::audienceLevelDiversity)
                .given(talk1, talk2, talk3, talk4)
                .rewardsWith(MONDAY_9_TO_10.getDurationInMinutes() * 2); // talk1/talk3 + talk2/talk3
    }

    @Test
    void contentAudienceLevelFlowViolation() {
        var room = aRoom("0");
        Talk talk1 =
                aTalk("1").timeslot(MONDAY_9_TO_10).room(room).audienceLevel(1).contentTags(sequencedSet("a")).build();
        Talk talk2 =
                aTalk("2").timeslot(MONDAY_9_TO_10).room(room).audienceLevel(2).contentTags(sequencedSet("a")).build();
        Talk talk3 =
                aTalk("3").timeslot(MONDAY_9_TO_10).room(room).audienceLevel(3).contentTags(sequencedSet("b")).build();
        Talk talk4 = aTalk("4").timeslot(MONDAY_10_05_TO_11).room(room).audienceLevel(1).contentTags(sequencedSet("a"))
                .build();

        constraintVerifier.verifyThat(ConferenceSchedulingConstraintProvider::contentAudienceLevelFlowViolation)
                .given(talk1, talk2, talk3, talk4)
                .penalizesBy(MONDAY_9_TO_10.getDurationInMinutes() + MONDAY_10_05_TO_11.getDurationInMinutes()
                        + MONDAY_9_TO_10.getDurationInMinutes() * 2);
    }

    @Test
    void contentConflict() {
        var room = aRoom("0");
        Talk talk1 = aTalk("1").timeslot(MONDAY_9_TO_10).room(room).contentTags(sequencedSet("a")).build();
        Talk talk2 = aTalk("2").timeslot(MONDAY_9_TO_10).room(room).contentTags(sequencedSet("a")).build();
        Talk talk3 = aTalk("3").timeslot(MONDAY_9_TO_10).room(room).contentTags(sequencedSet("b")).build();
        Talk talk4 = aTalk("4").timeslot(MONDAY_10_05_TO_11).room(room).contentTags(sequencedSet("a")).build();

        constraintVerifier.verifyThat(ConferenceSchedulingConstraintProvider::contentConflict)
                .given(talk1, talk2, talk3, talk4)
                .penalizesBy(MONDAY_9_TO_10.getDurationInMinutes());
    }

    @Test
    void languageDiversity() {
        var room = aRoom("0");
        Talk talk1 = aTalk("1").timeslot(MONDAY_9_TO_10).room(room).language("a").build();
        Talk talk2 = aTalk("2").timeslot(MONDAY_9_TO_10).room(room).language("a").build();
        Talk talk3 = aTalk("3").timeslot(MONDAY_9_TO_10).room(room).language("b").build();
        Talk talk4 = aTalk("4").timeslot(MONDAY_10_05_TO_11).room(room).language("a").build();

        constraintVerifier.verifyThat(ConferenceSchedulingConstraintProvider::languageDiversity)
                .given(talk1, talk2, talk3, talk4)
                .rewardsWith(MONDAY_9_TO_10.getDurationInMinutes() * 2);
    }

    @Test
    void sameDayTalks() {
        var room = aRoom("0");
        Talk talk1 = aTalk("1").timeslot(MONDAY_9_TO_10).room(room).contentTags(sequencedSet("a"))
                .themeTrackTags(sequencedSet("a")).build();
        Talk talk2 = aTalk("2").timeslot(TUESDAY_9_TO_10).room(room).contentTags(sequencedSet("b"))
                .themeTrackTags(sequencedSet("a")).build();
        Talk talk3 = aTalk("3").timeslot(TUESDAY_9_TO_10).room(room).contentTags(sequencedSet("a"))
                .themeTrackTags(sequencedSet("a")).build();
        Talk talk4 = aTalk("4").timeslot(MONDAY_9_TO_10).room(room).contentTags(sequencedSet("a"))
                .themeTrackTags(sequencedSet("b")).build();
        Talk talk5 = aTalk("5").timeslot(TUESDAY_9_TO_10).room(room).contentTags(sequencedSet("b"))
                .themeTrackTags(sequencedSet("b")).build();
        Talk talk6 = aTalk("6").timeslot(TUESDAY_9_TO_10).room(room).contentTags(sequencedSet("a"))
                .themeTrackTags(sequencedSet("b")).build();

        constraintVerifier.verifyThat(ConferenceSchedulingConstraintProvider::sameDayTalks)
                .given(talk1, talk2, talk3, talk4, talk5, talk6)
                .penalizesBy(960); // talk1/talk2 + talk1/talk3 + talk1/talk6*2 + talk4/talk3 + talk4/talk5 + talk4/talk6*2
    }

    @Test
    void popularTalks() {
        var smallerRoom = aRoom("0").capacity(10);
        var biggerRoom = aRoom("1").capacity(20);
        Talk talk1 = aTalk("1").timeslot(MONDAY_9_TO_10).room(smallerRoom).favoriteCount(2).build();
        Talk talk2 = aTalk("2").timeslot(MONDAY_9_TO_10).room(biggerRoom).favoriteCount(2).build();
        Talk talk3 = aTalk("3").timeslot(MONDAY_9_TO_10).room(biggerRoom).favoriteCount(1).build();

        constraintVerifier.verifyThat(ConferenceSchedulingConstraintProvider::popularTalks)
                .given(talk1, talk2, talk3)
                .penalizesBy(MONDAY_9_TO_10.getDurationInMinutes() * 2);
    }

    @Test
    void speakerPreferredTimeslotTags() {
        var room = aRoom("0");
        var speaker1 = aSpeaker("1").preferredTimeslotTags(sequencedSet("a"));
        var speaker2 = aSpeaker("1").preferredTimeslotTags(sequencedSet("x"));

        Talk talk1 = aTalk("1").timeslot(MONDAY_9_TO_10).room(room).speakers(List.of(speaker1))
                .preferredTimeslotTags(sequencedSet("a", "b")).build();
        Talk talk2 = aTalk("2").timeslot(MONDAY_10_05_TO_11).room(room).speakers(List.of(speaker2)).build();

        constraintVerifier.verifyThat(ConferenceSchedulingConstraintProvider::speakerPreferredTimeslotTags)
                .given(talk1, talk2)
                .penalizesBy(MONDAY_10_05_TO_11.getDurationInMinutes());
    }

    @Test
    void speakerUndesiredTimeslotTags() {
        var room = aRoom("0");
        var speaker1 = aSpeaker("1").undesiredTimeslotTags(sequencedSet("a"));
        var speaker2 = aSpeaker("1").undesiredTimeslotTags(sequencedSet("x"));

        Talk talk1 = aTalk("1").timeslot(MONDAY_9_TO_10).room(room).speakers(List.of(speaker1))
                .undesiredRoomTags(sequencedSet("a", "b")).build();
        Talk talk2 = aTalk("2").timeslot(MONDAY_10_05_TO_11).room(room).speakers(List.of(speaker2)).build();

        constraintVerifier.verifyThat(ConferenceSchedulingConstraintProvider::speakerUndesiredTimeslotTags)
                .given(talk1, talk2)
                .penalizesBy(MONDAY_9_TO_10.getDurationInMinutes());
    }

    @Test
    void talkPreferredTimeslotTags() {
        var room = aRoom("0");
        Talk talk1 =
                aTalk("1").timeslot(MONDAY_9_TO_10).room(room).preferredTimeslotTags(sequencedSet("a", "b")).build();
        Talk talk2 = aTalk("2").timeslot(MONDAY_10_05_TO_11).room(room).build();

        constraintVerifier.verifyThat(ConferenceSchedulingConstraintProvider::talkPreferredTimeslotTags)
                .given(talk1, talk2)
                .penalizesBy(MONDAY_9_TO_10.getDurationInMinutes());
    }

    @Test
    void talkUndesiredTimeslotTags() {
        var room = aRoom("0");
        Talk talk1 =
                aTalk("1").timeslot(MONDAY_9_TO_10).room(room).undesiredTimeslotTags(sequencedSet("a", "b")).build();
        Talk talk2 = aTalk("2").timeslot(MONDAY_10_05_TO_11).room(room).build();

        constraintVerifier.verifyThat(ConferenceSchedulingConstraintProvider::talkUndesiredTimeslotTags)
                .given(talk1, talk2)
                .penalizesBy(MONDAY_9_TO_10.getDurationInMinutes());
    }

    @Test
    void speakerPreferredRoomTags() {
        var room = aRoom("0").tags(sequencedSet("a"));
        var speaker1 = aSpeaker("1").preferredRoomTags(sequencedSet("a"));
        var speaker2 = aSpeaker("2").preferredRoomTags(sequencedSet("x"));

        Talk talk1 = aTalk("1").timeslot(MONDAY_9_TO_10).room(room).speakers(List.of(speaker1)).build();
        Talk talk2 = aTalk("2").timeslot(MONDAY_10_05_TO_11).room(room).speakers(List.of(speaker2)).build();

        constraintVerifier.verifyThat(ConferenceSchedulingConstraintProvider::speakerPreferredRoomTags)
                .given(talk1, talk2)
                .penalizesBy(MONDAY_10_05_TO_11.getDurationInMinutes());
    }

    @Test
    void speakerUndesiredRoomTags() {
        var room = aRoom("0").tags(sequencedSet("a"));
        var speaker1 = aSpeaker("1").undesiredRoomTags(sequencedSet("a"));
        var speaker2 = aSpeaker("2").undesiredRoomTags(sequencedSet("x"));

        Talk talk1 = aTalk("1").timeslot(MONDAY_9_TO_10).room(room).speakers(List.of(speaker1)).build();
        Talk talk2 = aTalk("2").timeslot(MONDAY_10_05_TO_11).room(room).speakers(List.of(speaker2)).build();

        constraintVerifier.verifyThat(ConferenceSchedulingConstraintProvider::speakerUndesiredRoomTags)
                .given(talk1, talk2)
                .penalizesBy(MONDAY_9_TO_10.getDurationInMinutes());
    }

    @Test
    void talkPreferredRoomTags() {
        var room = aRoom("0").tags(sequencedSet("a"));
        Talk talk1 = aTalk("1").timeslot(MONDAY_9_TO_10).room(room).preferredRoomTags(sequencedSet("a", "b")).build();
        Talk talk2 = aTalk("2").timeslot(MONDAY_10_05_TO_11).room(room).build();

        constraintVerifier.verifyThat(ConferenceSchedulingConstraintProvider::talkPreferredRoomTags)
                .given(talk1, talk2)
                .penalizesBy(MONDAY_9_TO_10.getDurationInMinutes());
    }

    @Test
    void talkUndesiredRoomTags() {
        var room = aRoom("0").tags(sequencedSet("a"));
        Talk talk1 = aTalk("1").timeslot(MONDAY_9_TO_10).room(room).undesiredRoomTags(sequencedSet("a", "b")).build();
        Talk talk2 = aTalk("2").timeslot(MONDAY_10_05_TO_11).room(room).build();

        constraintVerifier.verifyThat(ConferenceSchedulingConstraintProvider::talkUndesiredRoomTags)
                .given(talk1, talk2)
                .penalizesBy(MONDAY_9_TO_10.getDurationInMinutes());
    }

    @Test
    void speakerMakespan() {
        var room = aRoom("0").tags(sequencedSet("a"));
        var speaker1 = aSpeaker("1").unavailableTimeslots(sequencedSet(MONDAY_9_TO_10));
        var speaker2 = aSpeaker("2").unavailableTimeslots(sequencedSet(MONDAY_10_05_TO_11));

        Talk talk1 = aTalk("1").timeslot(MONDAY_9_TO_10).room(room).speakers(List.of(speaker1, speaker2)).build();
        Talk talk2 = aTalk("2").timeslot(TUESDAY_9_TO_10).room(room).speakers(List.of(speaker1, speaker2)).build();
        Talk talk3 = aTalk("3").timeslot(WEDNESDAY_9_TO_10).room(room).speakers(List.of(speaker1)).build();

        constraintVerifier.verifyThat(ConferenceSchedulingConstraintProvider::speakerMakespan)
                .given(speaker1.build(), speaker2.build(), talk1, talk2, talk3)
                .penalizesBy(8 * 60);
    }
}
