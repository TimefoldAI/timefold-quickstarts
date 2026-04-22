package org.acme.conferencescheduling.solver;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.SequencedSet;
import java.util.Set;

import ai.timefold.solver.core.api.score.stream.test.ConstraintVerifier;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.acme.conferencescheduling.domain.ConferenceConstraintProperties;
import org.acme.conferencescheduling.domain.ConferenceSchedule;
import org.acme.conferencescheduling.domain.Room;
import org.acme.conferencescheduling.domain.Speaker;
import org.acme.conferencescheduling.domain.Talk;
import org.acme.conferencescheduling.domain.Timeslot;
import org.junit.jupiter.api.Test;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;

@QuarkusTest
class ConferenceSchedulingConstraintProviderTest {

    private static final LocalDateTime START = LocalDateTime.of(2000, 2, 1, 9, 0);

    private static final Timeslot MONDAY_9_TO_10 = new Timeslot("1", START, START.plusHours(1), emptySet(), sequencedSet("a"));
    private static final Timeslot MONDAY_10_05_TO_11 = new Timeslot("2", MONDAY_9_TO_10.getEndDateTime().plusMinutes(5),
            MONDAY_9_TO_10.getEndDateTime().plusHours(1), emptySet(), sequencedSet("b"));
    private static final Timeslot MONDAY_11_10_TO_12 = new Timeslot("3", MONDAY_10_05_TO_11.getEndDateTime().plusMinutes(10),
            MONDAY_10_05_TO_11.getEndDateTime().plusHours(1), emptySet(), sequencedSet("c"));
    private static final Timeslot TUESDAY_9_TO_10 =
            new Timeslot("4", START.plusDays(1), START.plusDays(1).plusHours(1), emptySet(), singleton("c"));

    private static final Timeslot WEDNESDAY_9_TO_10 =
            new Timeslot("5", START.plusDays(2), START.plusDays(1).plusHours(1), emptySet(), sequencedSet("c"));

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
        Room room1 = new Room("1", sequencedSet(MONDAY_9_TO_10));
        Room room2 = new Room("2", sequencedSet(MONDAY_10_05_TO_11));
        Talk talk1 = new Talk("1", MONDAY_9_TO_10, room1);
        Talk talk2 = new Talk("2", MONDAY_9_TO_10, room2);

        constraintVerifier.verifyThat(ConferenceSchedulingConstraintProvider::roomUnavailableTimeslot)
                .given(talk1, talk2)
                .penalizesBy(MONDAY_9_TO_10.getDurationInMinutes()); // room1 is in an unavailable timeslot.
    }

    @Test
    void roomConflict() {
        Room room1 = new Room("1", sequencedSet(MONDAY_9_TO_10));
        Talk talk1 = new Talk("1", MONDAY_9_TO_10, room1);
        Talk talk2 = new Talk("2", MONDAY_9_TO_10, room1);
        Talk talk3 = new Talk("3", MONDAY_10_05_TO_11, room1);

        constraintVerifier.verifyThat(ConferenceSchedulingConstraintProvider::roomConflict)
                .given(talk1, talk2, talk3)
                .penalizesBy(MONDAY_9_TO_10.getDurationInMinutes()); // talk1 and talk2 are in conflict.
    }

    @Test
    void speakerUnavailableTimeslot() {
        Room room = new Room("0");
        Speaker speaker1 = new Speaker("1");
        speaker1.setUnavailableTimeslots(sequencedSet(MONDAY_9_TO_10));
        Speaker speaker2 = new Speaker("2");
        speaker2.setUnavailableTimeslots(sequencedSet(MONDAY_10_05_TO_11));
        Talk talk1 = new Talk("1", MONDAY_9_TO_10, room, List.of(speaker1));
        Talk talk2 = new Talk("2", MONDAY_9_TO_10, room, List.of(speaker2));

        constraintVerifier.verifyThat(ConferenceSchedulingConstraintProvider::speakerUnavailableTimeslot)
                .given(talk1, talk2, speaker1, speaker2)
                .penalizesBy(MONDAY_9_TO_10.getDurationInMinutes()); // speaker1 is in an unavailable timeslot.
    }

    @Test
    void speakerConflict() {
        Room room = new Room("0");
        Speaker speaker = new Speaker("1");
        Talk talk1 = new Talk("1", MONDAY_9_TO_10, room, List.of(speaker));
        Talk talk2 = new Talk("2", MONDAY_9_TO_10, room, List.of(speaker));
        Talk talk3 = new Talk("3", MONDAY_10_05_TO_11, room, List.of(speaker));

        constraintVerifier.verifyThat(ConferenceSchedulingConstraintProvider::speakerConflict)
                .given(speaker, talk1, talk2, talk3)
                .penalizesBy(MONDAY_9_TO_10.getDurationInMinutes()); // talk1 and talk2 are in conflict.
    }

    @Test
    void talkPrerequisiteTalks() {
        Room room = new Room("0");
        Talk talk1 = new Talk("1", MONDAY_9_TO_10, room);
        Talk talk2 = new Talk("2", MONDAY_9_TO_10, room);
        talk2.setPrerequisiteTalks(sequencedSet(talk1));
        Talk talk3 = new Talk("3", MONDAY_10_05_TO_11, room);
        talk3.setPrerequisiteTalks(sequencedSet(talk1));

        constraintVerifier.verifyThat(ConferenceSchedulingConstraintProvider::talkPrerequisiteTalks)
                .given(talk1, talk2, talk3)
                .penalizesBy(MONDAY_9_TO_10.getDurationInMinutes() * 2); // talk2 is not after talk1.
    }

    @Test
    void talkMutuallyExclusiveTalksTags() {
        Room room = new Room("0");
        Talk talk1 = new Talk("1", MONDAY_9_TO_10, room);
        Talk talk2 = new Talk("2", MONDAY_9_TO_10, room);
        talk2.setMutuallyExclusiveTalksTags(sequencedSet("a", "b"));
        Talk talk3 = new Talk("3", MONDAY_9_TO_10, room);
        talk3.setMutuallyExclusiveTalksTags(sequencedSet("a", "b", "c"));

        constraintVerifier.verifyThat(ConferenceSchedulingConstraintProvider::talkMutuallyExclusiveTalksTags)
                .given(talk1, talk2, talk3)
                .penalizesBy(MONDAY_9_TO_10.getDurationInMinutes() * 2); // talk2 and talk3 excluded twice.
    }

    @Test
    void consecutiveTalksPause() {
        Room room = new Room("0");
        Speaker speaker1 = new Speaker("1");
        Speaker speaker2 = new Speaker("2");
        Talk talk1 = new Talk("1", MONDAY_9_TO_10, room, List.of(speaker1));
        Talk talk2 = new Talk("2", MONDAY_10_05_TO_11, room, List.of(speaker1));
        Talk talk3 = new Talk("3", MONDAY_11_10_TO_12, room, List.of(speaker1));
        Talk talk4 = new Talk("4", MONDAY_9_TO_10, room, List.of(speaker2));
        ConferenceConstraintProperties configuration = new ConferenceConstraintProperties();
        configuration.setMinimumConsecutiveTalksPauseInMinutes(11);

        constraintVerifier.verifyThat(ConferenceSchedulingConstraintProvider::consecutiveTalksPause)
                .given(configuration, talk1, talk2, talk3, talk4)
                .penalizesBy(MONDAY_9_TO_10.getDurationInMinutes() + MONDAY_10_05_TO_11.getDurationInMinutes()
                        + MONDAY_10_05_TO_11.getDurationInMinutes() + MONDAY_11_10_TO_12.getDurationInMinutes()); // talk1+talk2 , talk2+talk3.
    }

    @Test
    void crowdControl() {
        Room room = new Room("0");
        Talk talk1 = new Talk("1", MONDAY_9_TO_10, room);
        talk1.setCrowdControlRisk(1);
        Talk talk2 = new Talk("2", MONDAY_9_TO_10, room);
        talk2.setCrowdControlRisk(1);
        Talk talk3 = new Talk("3", MONDAY_9_TO_10, room);
        talk3.setCrowdControlRisk(1);
        Talk talk4 = new Talk("4", MONDAY_10_05_TO_11, room);
        talk4.setCrowdControlRisk(1);
        Talk talk5 = new Talk("5", MONDAY_10_05_TO_11, room);
        talk5.setCrowdControlRisk(1);
        Talk noRiskTalk = new Talk("6", MONDAY_10_05_TO_11, room);

        constraintVerifier.verifyThat(ConferenceSchedulingConstraintProvider::crowdControl)
                .given(talk1, talk2, talk3, talk4, talk5, noRiskTalk)
                .penalizesBy(MONDAY_9_TO_10.getDurationInMinutes() * 3); // talk1, talk2, talk3.
    }

    @Test
    void speakerRequiredTimeslotTags() {
        Room room = new Room("0");
        Speaker speaker1 = new Speaker("1");
        speaker1.setRequiredTimeslotTags(sequencedSet("a"));
        Speaker speaker2 = new Speaker("2");
        speaker2.setRequiredTimeslotTags(sequencedSet("x"));
        Talk talk1 = new Talk("1", MONDAY_9_TO_10, room, List.of(speaker1));
        talk1.setRequiredTimeslotTags(sequencedSet("a", "b"));
        Talk talk2 = new Talk("2", MONDAY_10_05_TO_11, room, List.of(speaker2));

        constraintVerifier.verifyThat(ConferenceSchedulingConstraintProvider::speakerRequiredTimeslotTags)
                .given(talk1, talk2)
                .penalizesBy(MONDAY_10_05_TO_11.getDurationInMinutes());
    }

    @Test
    void speakerProhibitedTimeslotTags() {
        Room room = new Room("0");
        Speaker speaker1 = new Speaker("1");
        speaker1.setProhibitedTimeslotTags(sequencedSet("a"));
        Speaker speaker2 = new Speaker("2");
        speaker2.setProhibitedTimeslotTags(sequencedSet("x"));
        Talk talk1 = new Talk("1", MONDAY_9_TO_10, room, List.of(speaker1));
        talk1.setProhibitedTimeslotTags(sequencedSet("a", "b"));
        Talk talk2 = new Talk("2", MONDAY_10_05_TO_11, room, List.of(speaker2));

        constraintVerifier.verifyThat(ConferenceSchedulingConstraintProvider::speakerProhibitedTimeslotTags)
                .given(talk1, talk2)
                .penalizesBy(MONDAY_9_TO_10.getDurationInMinutes());
    }

    @Test
    void talkRequiredTimeslotTags() {
        Room room = new Room("0");
        Talk talk1 = new Talk("1", MONDAY_9_TO_10, room);
        talk1.setRequiredTimeslotTags(sequencedSet("a", "b"));
        Talk talk2 = new Talk("2", MONDAY_10_05_TO_11, room);

        constraintVerifier.verifyThat(ConferenceSchedulingConstraintProvider::talkRequiredTimeslotTags)
                .given(talk1, talk2)
                .penalizesBy(MONDAY_9_TO_10.getDurationInMinutes());
    }

    @Test
    void talkProhibitedTimeslotTags() {
        Room room = new Room("0");
        Talk talk1 = new Talk("1", MONDAY_9_TO_10, room);
        talk1.setProhibitedTimeslotTags(sequencedSet("a", "b"));
        Talk talk2 = new Talk("2", MONDAY_10_05_TO_11, room);

        constraintVerifier.verifyThat(ConferenceSchedulingConstraintProvider::talkProhibitedTimeslotTags)
                .given(talk1, talk2)
                .penalizesBy(MONDAY_9_TO_10.getDurationInMinutes());
    }

    @Test
    void speakerRequiredRoomTags() {
        Room room = new Room("0");
        room.setTags(sequencedSet("a"));
        Speaker speaker1 = new Speaker("1");
        speaker1.setRequiredRoomTags(sequencedSet("a"));
        Speaker speaker2 = new Speaker("2");
        speaker2.setRequiredRoomTags(sequencedSet("x"));
        Talk talk1 = new Talk("1", MONDAY_9_TO_10, room, List.of(speaker1));
        Talk talk2 = new Talk("2", MONDAY_10_05_TO_11, room, List.of(speaker2));

        constraintVerifier.verifyThat(ConferenceSchedulingConstraintProvider::speakerRequiredRoomTags)
                .given(talk1, talk2)
                .penalizesBy(MONDAY_10_05_TO_11.getDurationInMinutes());
    }

    @Test
    void speakerProhibitedRoomTags() {
        Room room = new Room("0");
        room.setTags(sequencedSet("a"));
        Speaker speaker1 = new Speaker("1");
        speaker1.setProhibitedRoomTags(sequencedSet("a"));
        Speaker speaker2 = new Speaker("2");
        speaker2.setProhibitedRoomTags(sequencedSet("x"));
        Talk talk1 = new Talk("1", MONDAY_9_TO_10, room, List.of(speaker1));
        Talk talk2 = new Talk("2", MONDAY_10_05_TO_11, room, List.of(speaker2));

        constraintVerifier.verifyThat(ConferenceSchedulingConstraintProvider::speakerProhibitedRoomTags)
                .given(talk1, talk2)
                .penalizesBy(MONDAY_9_TO_10.getDurationInMinutes());
    }

    @Test
    void talkRequiredRoomTags() {
        Room room = new Room("0");
        room.setTags(sequencedSet("a"));
        Talk talk1 = new Talk("1", MONDAY_9_TO_10, room);
        talk1.setRequiredRoomTags(sequencedSet("a", "b"));
        Talk talk2 = new Talk("2", MONDAY_10_05_TO_11, room);

        constraintVerifier.verifyThat(ConferenceSchedulingConstraintProvider::talkRequiredRoomTags)
                .given(talk1, talk2)
                .penalizesBy(MONDAY_9_TO_10.getDurationInMinutes());
    }

    @Test
    void talkProhibitedRoomTags() {
        Room room = new Room("0");
        room.setTags(sequencedSet("a"));
        Talk talk1 = new Talk("1", MONDAY_9_TO_10, room);
        talk1.setProhibitedRoomTags(sequencedSet("a", "b"));
        Talk talk2 = new Talk("2", MONDAY_10_05_TO_11, room);

        constraintVerifier.verifyThat(ConferenceSchedulingConstraintProvider::talkProhibitedRoomTags)
                .given(talk1, talk2)
                .penalizesBy(MONDAY_9_TO_10.getDurationInMinutes());
    }

    // ************************************************************************
    // Soft constraints
    // ************************************************************************

    @Test
    void themeTrackConflict() {
        Room room = new Room("0");
        Talk talk1 = new Talk("1", MONDAY_9_TO_10, room);
        talk1.setThemeTrackTags(sequencedSet("a"));
        Talk talk2 = new Talk("2", MONDAY_9_TO_10, room);
        talk2.setThemeTrackTags(sequencedSet("a"));
        Talk talk3 = new Talk("3", MONDAY_9_TO_10, room);
        talk3.setThemeTrackTags(sequencedSet("b"));
        Talk talk4 = new Talk("4", MONDAY_10_05_TO_11, room);
        talk4.setThemeTrackTags(sequencedSet("a"));

        constraintVerifier.verifyThat(ConferenceSchedulingConstraintProvider::themeTrackConflict)
                .given(talk1, talk2, talk3, talk4)
                .penalizesBy(MONDAY_9_TO_10.getDurationInMinutes()); // overlap(talk1, talk2).
    }

    @Test
    void themeTrackRoomStability() {
        Room room1 = new Room("0");
        Room room2 = new Room("1");
        Talk talk1 = new Talk("1", MONDAY_9_TO_10, room1);
        talk1.setThemeTrackTags(sequencedSet("a"));
        Talk talk2 = new Talk("2", MONDAY_10_05_TO_11, room2);
        talk2.setThemeTrackTags(sequencedSet("a"));
        Talk talk3 = new Talk("3", MONDAY_11_10_TO_12, room1);
        talk3.setThemeTrackTags(sequencedSet("b"));
        Talk talk4 = new Talk("4", TUESDAY_9_TO_10, room2);
        talk4.setThemeTrackTags(sequencedSet("a"));

        constraintVerifier.verifyThat(ConferenceSchedulingConstraintProvider::themeTrackRoomStability)
                .given(talk1, talk2, talk3, talk4)
                .penalizesBy(MONDAY_9_TO_10.getDurationInMinutes() + MONDAY_10_05_TO_11.getDurationInMinutes()); // talk1 + talk2.
    }

    @Test
    void sectorConflict() {
        Room room = new Room("0");
        Talk talk1 = new Talk("1", MONDAY_9_TO_10, room);
        talk1.setSectorTags(sequencedSet("a"));
        Talk talk2 = new Talk("2", MONDAY_9_TO_10, room);
        talk2.setSectorTags(sequencedSet("a"));
        Talk talk3 = new Talk("2", MONDAY_9_TO_10, room);
        talk3.setSectorTags(sequencedSet("b"));
        Talk talk4 = new Talk("2", MONDAY_10_05_TO_11, room);
        talk4.setSectorTags(sequencedSet("a"));

        constraintVerifier.verifyThat(ConferenceSchedulingConstraintProvider::sectorConflict)
                .given(talk1, talk2, talk3, talk4)
                .penalizesBy(MONDAY_9_TO_10.getDurationInMinutes()); // talk1 + talk2.
    }

    @Test
    void audienceTypeDiversity() {
        Room room = new Room("0");
        Talk talk1 = new Talk("1", MONDAY_9_TO_10, room);
        talk1.setAudienceTypes(sequencedSet("a"));
        Talk talk2 = new Talk("2", MONDAY_9_TO_10, room);
        talk2.setAudienceTypes(sequencedSet("a"));
        Talk talk3 = new Talk("3", MONDAY_9_TO_10, room);
        talk3.setAudienceTypes(sequencedSet("b"));
        Talk talk4 = new Talk("4", MONDAY_10_05_TO_11, room);
        talk4.setAudienceTypes(sequencedSet("b"));

        constraintVerifier.verifyThat(ConferenceSchedulingConstraintProvider::audienceTypeDiversity)
                .given(talk1, talk2, talk3, talk4)
                .rewardsWith(MONDAY_9_TO_10.getDurationInMinutes()); // talk1 + talk2.
    }

    @Test
    void audienceTypeThemeTrackConflict() {
        Room room = new Room("0");
        Talk talk1 = new Talk("1", MONDAY_9_TO_10, room);
        talk1.setAudienceTypes(sequencedSet("a"));
        talk1.setThemeTrackTags(sequencedSet("b"));
        Talk talk2 = new Talk("2", MONDAY_9_TO_10, room);
        talk2.setAudienceTypes(sequencedSet("a"));
        talk2.setThemeTrackTags(sequencedSet("a"));
        Talk talk3 = new Talk("3", MONDAY_9_TO_10, room);
        talk3.setAudienceTypes(sequencedSet("b"));
        talk3.setThemeTrackTags(sequencedSet("a"));
        Talk talk4 = new Talk("4", MONDAY_10_05_TO_11, room);
        talk4.setAudienceTypes(sequencedSet("a"));
        talk4.setThemeTrackTags(sequencedSet("a"));
        Talk talk5 = new Talk("5", MONDAY_9_TO_10, room);
        talk5.setAudienceTypes(sequencedSet("a"));
        talk5.setThemeTrackTags(sequencedSet("b"));
        Talk talk6 = new Talk("6", MONDAY_9_TO_10, room);
        talk6.setAudienceTypes(sequencedSet("a"));
        talk6.setThemeTrackTags(sequencedSet("c"));

        constraintVerifier.verifyThat(ConferenceSchedulingConstraintProvider::audienceTypeThemeTrackConflict)
                .given(talk1, talk2, talk3, talk4, talk5, talk6)
                .penalizesBy(MONDAY_9_TO_10.getDurationInMinutes()); // talk1 + talk5.
    }

    @Test
    void audienceLevelDiversity() {
        Room room = new Room("0");
        Talk talk1 = new Talk("1", MONDAY_9_TO_10, room);
        talk1.setAudienceLevel(1);
        Talk talk2 = new Talk("2", MONDAY_9_TO_10, room);
        talk2.setAudienceLevel(1);
        Talk talk3 = new Talk("3", MONDAY_9_TO_10, room);
        talk3.setAudienceLevel(2);
        Talk talk4 = new Talk("4", MONDAY_11_10_TO_12, room);
        talk4.setAudienceLevel(1);

        constraintVerifier.verifyThat(ConferenceSchedulingConstraintProvider::audienceLevelDiversity)
                .given(talk1, talk2, talk3, talk4)
                .rewardsWith(MONDAY_9_TO_10.getDurationInMinutes() * 2); // talk1/talk3 + talk2/talk3
    }

    @Test
    void contentAudienceLevelFlowViolation() {
        Room room = new Room("0");
        Talk talk1 = new Talk("1", MONDAY_9_TO_10, room);
        talk1.setAudienceLevel(1);
        talk1.setContentTags(sequencedSet("a"));
        Talk talk2 = new Talk("2", MONDAY_9_TO_10, room);
        talk2.setAudienceLevel(2);
        talk2.setContentTags(sequencedSet("a"));
        Talk talk3 = new Talk("3", MONDAY_9_TO_10, room);
        talk3.setAudienceLevel(3);
        talk3.setContentTags(sequencedSet("b"));
        Talk talk4 = new Talk("4", MONDAY_10_05_TO_11, room);
        talk4.setAudienceLevel(1);
        talk4.setContentTags(sequencedSet("a"));

        constraintVerifier.verifyThat(ConferenceSchedulingConstraintProvider::contentAudienceLevelFlowViolation)
                .given(talk1, talk2, talk3, talk4)
                .penalizesBy(MONDAY_9_TO_10.getDurationInMinutes() + MONDAY_10_05_TO_11.getDurationInMinutes()
                        + MONDAY_9_TO_10.getDurationInMinutes() * 2);
    }

    @Test
    void contentConflict() {
        Room room = new Room("0");
        Talk talk1 = new Talk("1", MONDAY_9_TO_10, room);
        talk1.setContentTags(sequencedSet("a"));
        Talk talk2 = new Talk("2", MONDAY_9_TO_10, room);
        talk2.setContentTags(sequencedSet("a"));
        Talk talk3 = new Talk("3", MONDAY_9_TO_10, room);
        talk3.setContentTags(sequencedSet("b"));
        Talk talk4 = new Talk("4", MONDAY_10_05_TO_11, room);
        talk4.setContentTags(sequencedSet("a"));

        constraintVerifier.verifyThat(ConferenceSchedulingConstraintProvider::contentConflict)
                .given(talk1, talk2, talk3, talk4)
                .penalizesBy(MONDAY_9_TO_10.getDurationInMinutes());
    }

    @Test
    void languageDiversity() {
        Room room = new Room("0");
        Talk talk1 = new Talk("1", MONDAY_9_TO_10, room);
        talk1.setLanguage("a");
        Talk talk2 = new Talk("2", MONDAY_9_TO_10, room);
        talk2.setLanguage("a");
        Talk talk3 = new Talk("3", MONDAY_9_TO_10, room);
        talk3.setLanguage("b");
        Talk talk4 = new Talk("4", MONDAY_10_05_TO_11, room);
        talk4.setLanguage("a");

        constraintVerifier.verifyThat(ConferenceSchedulingConstraintProvider::languageDiversity)
                .given(talk1, talk2, talk3, talk4)
                .rewardsWith(MONDAY_9_TO_10.getDurationInMinutes() * 2);
    }

    @Test
    void sameDayTalks() {
        Room room = new Room("0");
        Talk talk1 = new Talk("1", MONDAY_9_TO_10, room);
        talk1.setContentTags(sequencedSet("a"));
        talk1.setThemeTrackTags(sequencedSet("a"));
        Talk talk2 = new Talk("2", TUESDAY_9_TO_10, room);
        talk2.setContentTags(sequencedSet("b"));
        talk2.setThemeTrackTags(sequencedSet("a"));
        Talk talk3 = new Talk("3", TUESDAY_9_TO_10, room);
        talk3.setContentTags(sequencedSet("a"));
        talk3.setThemeTrackTags(sequencedSet("a"));
        Talk talk4 = new Talk("4", MONDAY_9_TO_10, room);
        talk4.setContentTags(sequencedSet("a"));
        talk4.setThemeTrackTags(sequencedSet("b"));
        Talk talk5 = new Talk("5", TUESDAY_9_TO_10, room);
        talk5.setContentTags(sequencedSet("b"));
        talk5.setThemeTrackTags(sequencedSet("b"));
        Talk talk6 = new Talk("6", TUESDAY_9_TO_10, room);
        talk6.setContentTags(sequencedSet("a"));
        talk6.setThemeTrackTags(sequencedSet("b"));

        constraintVerifier.verifyThat(ConferenceSchedulingConstraintProvider::sameDayTalks)
                .given(talk1, talk2, talk3, talk4, talk5, talk6)
                .penalizesBy(960); // talk1/talk2 + talk1/talk3 + talk1/talk6*2 + talk4/talk3 + talk4/talk5 + talk4/talk6*2
    }

    @Test
    void popularTalks() {
        Room smallerRoom = new Room("0");
        smallerRoom.setCapacity(10);
        Room biggerRoom = new Room("1");
        biggerRoom.setCapacity(20);
        Talk talk1 = new Talk("1", MONDAY_9_TO_10, smallerRoom);
        talk1.setFavoriteCount(2);
        Talk talk2 = new Talk("2", MONDAY_9_TO_10, biggerRoom);
        talk2.setFavoriteCount(2);
        Talk talk3 = new Talk("3", MONDAY_9_TO_10, biggerRoom);
        talk3.setFavoriteCount(1);

        constraintVerifier.verifyThat(ConferenceSchedulingConstraintProvider::popularTalks)
                .given(talk1, talk2, talk3)
                .penalizesBy(MONDAY_9_TO_10.getDurationInMinutes() * 2);
    }

    @Test
    void speakerPreferredTimeslotTags() {
        Room room = new Room("0");
        Speaker speaker1 = new Speaker("1");
        speaker1.setPreferredTimeslotTags(sequencedSet("a"));
        Speaker speaker2 = new Speaker("1");
        speaker2.setPreferredTimeslotTags(sequencedSet("x"));

        Talk talk1 = new Talk("1", MONDAY_9_TO_10, room, List.of(speaker1));
        talk1.setPreferredTimeslotTags(sequencedSet("a", "b"));
        Talk talk2 = new Talk("2", MONDAY_10_05_TO_11, room, List.of(speaker2));

        constraintVerifier.verifyThat(ConferenceSchedulingConstraintProvider::speakerPreferredTimeslotTags)
                .given(talk1, talk2)
                .penalizesBy(MONDAY_10_05_TO_11.getDurationInMinutes());
    }

    @Test
    void speakerUndesiredTimeslotTags() {
        Room room = new Room("0");
        Speaker speaker1 = new Speaker("1");
        speaker1.setUndesiredTimeslotTags(sequencedSet("a"));
        Speaker speaker2 = new Speaker("1");
        speaker2.setUndesiredTimeslotTags(sequencedSet("x"));

        Talk talk1 = new Talk("1", MONDAY_9_TO_10, room, List.of(speaker1));
        talk1.setUndesiredRoomTags(sequencedSet("a", "b"));
        Talk talk2 = new Talk("2", MONDAY_10_05_TO_11, room, List.of(speaker2));

        constraintVerifier.verifyThat(ConferenceSchedulingConstraintProvider::speakerUndesiredTimeslotTags)
                .given(talk1, talk2)
                .penalizesBy(MONDAY_9_TO_10.getDurationInMinutes());
    }

    @Test
    void talkPreferredTimeslotTags() {
        Room room = new Room("0");
        Talk talk1 = new Talk("1", MONDAY_9_TO_10, room);
        talk1.setPreferredTimeslotTags(sequencedSet("a", "b"));
        Talk talk2 = new Talk("2", MONDAY_10_05_TO_11, room);

        constraintVerifier.verifyThat(ConferenceSchedulingConstraintProvider::talkPreferredTimeslotTags)
                .given(talk1, talk2)
                .penalizesBy(MONDAY_9_TO_10.getDurationInMinutes());
    }

    @Test
    void talkUndesiredTimeslotTags() {
        Room room = new Room("0");
        Talk talk1 = new Talk("1", MONDAY_9_TO_10, room);
        talk1.setUndesiredTimeslotTags(sequencedSet("a", "b"));
        Talk talk2 = new Talk("2", MONDAY_10_05_TO_11, room);

        constraintVerifier.verifyThat(ConferenceSchedulingConstraintProvider::talkUndesiredTimeslotTags)
                .given(talk1, talk2)
                .penalizesBy(MONDAY_9_TO_10.getDurationInMinutes());
    }

    @Test
    void speakerPreferredRoomTags() {
        Room room = new Room("0");
        room.setTags(sequencedSet("a"));
        Speaker speaker1 = new Speaker("1");
        speaker1.setPreferredRoomTags(sequencedSet("a"));
        Speaker speaker2 = new Speaker("2");
        speaker2.setPreferredRoomTags(sequencedSet("x"));

        Talk talk1 = new Talk("1", MONDAY_9_TO_10, room, List.of(speaker1));
        Talk talk2 = new Talk("2", MONDAY_10_05_TO_11, room, List.of(speaker2));

        constraintVerifier.verifyThat(ConferenceSchedulingConstraintProvider::speakerPreferredRoomTags)
                .given(talk1, talk2)
                .penalizesBy(MONDAY_10_05_TO_11.getDurationInMinutes());
    }

    @Test
    void speakerUndesiredRoomTags() {
        Room room = new Room("0");
        room.setTags(sequencedSet("a"));
        Speaker speaker1 = new Speaker("1");
        speaker1.setUndesiredRoomTags(sequencedSet("a"));
        Speaker speaker2 = new Speaker("2");
        speaker2.setUndesiredRoomTags(sequencedSet("x"));

        Talk talk1 = new Talk("1", MONDAY_9_TO_10, room, List.of(speaker1));
        Talk talk2 = new Talk("2", MONDAY_10_05_TO_11, room, List.of(speaker2));

        constraintVerifier.verifyThat(ConferenceSchedulingConstraintProvider::speakerUndesiredRoomTags)
                .given(talk1, talk2)
                .penalizesBy(MONDAY_9_TO_10.getDurationInMinutes());
    }

    @Test
    void talkPreferredRoomTags() {
        Room room = new Room("0");
        room.setTags(sequencedSet("a"));
        Talk talk1 = new Talk("1", MONDAY_9_TO_10, room);
        talk1.setPreferredRoomTags(sequencedSet("a", "b"));
        Talk talk2 = new Talk("2", MONDAY_10_05_TO_11, room);

        constraintVerifier.verifyThat(ConferenceSchedulingConstraintProvider::talkPreferredRoomTags)
                .given(talk1, talk2)
                .penalizesBy(MONDAY_9_TO_10.getDurationInMinutes());
    }

    @Test
    void talkUndesiredRoomTags() {
        Room room = new Room("0");
        room.setTags(sequencedSet("a"));
        Talk talk1 = new Talk("1", MONDAY_9_TO_10, room);
        talk1.setUndesiredRoomTags(sequencedSet("a", "b"));
        Talk talk2 = new Talk("2", MONDAY_10_05_TO_11, room);

        constraintVerifier.verifyThat(ConferenceSchedulingConstraintProvider::talkUndesiredRoomTags)
                .given(talk1, talk2)
                .penalizesBy(MONDAY_9_TO_10.getDurationInMinutes());
    }

    @Test
    void speakerMakespan() {
        Room room = new Room("0");
        room.setTags(sequencedSet("a"));
        Speaker speaker1 = new Speaker("1");
        speaker1.setUnavailableTimeslots(sequencedSet(MONDAY_9_TO_10));
        Speaker speaker2 = new Speaker("2");
        speaker2.setUnavailableTimeslots(sequencedSet(MONDAY_10_05_TO_11));

        Talk talk1 = new Talk("1", MONDAY_9_TO_10, room, List.of(speaker1, speaker2));
        Talk talk2 = new Talk("2", TUESDAY_9_TO_10, room, List.of(speaker1, speaker2));
        Talk talk3 = new Talk("3", WEDNESDAY_9_TO_10, room, List.of(speaker1));

        constraintVerifier.verifyThat(ConferenceSchedulingConstraintProvider::speakerMakespan)
                .given(speaker1, speaker2, talk1, talk2, talk3)
                .penalizesBy(8 * 60);
    }
}
