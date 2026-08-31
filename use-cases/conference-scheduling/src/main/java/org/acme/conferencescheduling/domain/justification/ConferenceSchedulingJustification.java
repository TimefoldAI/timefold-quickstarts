package org.acme.conferencescheduling.domain.justification;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import ai.timefold.solver.service.definition.api.ModelConstraintJustification;

import org.acme.conferencescheduling.domain.Speaker;
import org.acme.conferencescheduling.domain.Talk;
import org.acme.conferencescheduling.domain.Timeslot;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Common contract for every conference scheduling justification.
 * <p>
 * Each implementation is a record dedicated to exactly one thing that is being justified, so that the Timefold Platform can
 * both render a human-readable {@link #getDescription() description} and expose the individual facts behind it through the
 * OpenAPI schema.
 * <p>
 * Every implementation must be listed in the {@link Schema#oneOf()} below, otherwise it does not show up in the generated
 * OpenAPI schema.
 */
@Schema(description = "Explains why a conference scheduling constraint was matched.",
        oneOf = {
                // Hard constraints
                ConferenceSchedulingJustification.RoomUnavailableAtTalkTimeslotJustification.class,
                ConferenceSchedulingJustification.TalksOverlappingInSameRoomJustification.class,
                ConferenceSchedulingJustification.SpeakerUnavailableAtTalkTimeslotJustification.class,
                ConferenceSchedulingJustification.SpeakerAssignedToOverlappingTalksJustification.class,
                ConferenceSchedulingJustification.TalkScheduledBeforePrerequisiteTalkJustification.class,
                ConferenceSchedulingJustification.MutuallyExclusiveTalksOverlappingJustification.class,
                ConferenceSchedulingJustification.SpeakerConsecutiveTalksPauseTooShortJustification.class,
                ConferenceSchedulingJustification.CrowdControlTalkNotPairedJustification.class,
                ConferenceSchedulingJustification.MissingRequiredTimeslotTagsForSpeakersJustification.class,
                ConferenceSchedulingJustification.ProhibitedTimeslotTagsForSpeakersJustification.class,
                ConferenceSchedulingJustification.MissingRequiredTimeslotTagsForTalkJustification.class,
                ConferenceSchedulingJustification.ProhibitedTimeslotTagsForTalkJustification.class,
                ConferenceSchedulingJustification.MissingRequiredRoomTagsForSpeakersJustification.class,
                ConferenceSchedulingJustification.ProhibitedRoomTagsForSpeakersJustification.class,
                ConferenceSchedulingJustification.MissingRequiredRoomTagsForTalkJustification.class,
                ConferenceSchedulingJustification.ProhibitedRoomTagsForTalkJustification.class,

                // Soft constraints
                ConferenceSchedulingJustification.TalksWithSameThemeTrackOverlappingJustification.class,
                ConferenceSchedulingJustification.TalksWithSameThemeTrackInDifferentRoomsJustification.class,
                ConferenceSchedulingJustification.TalksWithSameSectorOverlappingJustification.class,
                ConferenceSchedulingJustification.TalksWithSameAudienceTypeInSameTimeslotJustification.class,
                ConferenceSchedulingJustification.TalksWithSameThemeTrackAndAudienceTypeOverlappingJustification.class,
                ConferenceSchedulingJustification.TalksWithDifferentAudienceLevelInSameTimeslotJustification.class,
                ConferenceSchedulingJustification.SharedContentAudienceLevelFlowViolationJustification.class,
                ConferenceSchedulingJustification.TalksWithSameContentOverlappingJustification.class,
                ConferenceSchedulingJustification.TalksWithSameLanguageInSameTimeslotJustification.class,
                ConferenceSchedulingJustification.RelatedTalksNotOnSameDayJustification.class,
                ConferenceSchedulingJustification.PopularTalkInSmallerRoomJustification.class,
                ConferenceSchedulingJustification.MissingPreferredTimeslotTagsForSpeakersJustification.class,
                ConferenceSchedulingJustification.UndesiredTimeslotTagsForSpeakersJustification.class,
                ConferenceSchedulingJustification.MissingPreferredTimeslotTagsForTalkJustification.class,
                ConferenceSchedulingJustification.UndesiredTimeslotTagsForTalkJustification.class,
                ConferenceSchedulingJustification.MissingPreferredRoomTagsForSpeakersJustification.class,
                ConferenceSchedulingJustification.UndesiredRoomTagsForSpeakersJustification.class,
                ConferenceSchedulingJustification.MissingPreferredRoomTagsForTalkJustification.class,
                ConferenceSchedulingJustification.UndesiredRoomTagsForTalkJustification.class,
                ConferenceSchedulingJustification.SpeakerMakespanTooLongJustification.class
        })
public interface ConferenceSchedulingJustification extends ModelConstraintJustification {

    /**
     * @return never null, a human-readable explanation of the constraint match
     */
    String getDescription();

    /**
     * Exposes the description as the {@code description} property of {@link ModelConstraintJustification}.
     */
    default String description() {
        return getDescription();
    }

    /**
     * @return the tags present in both collections, in the iteration order of {@code left}
     */
    private static List<String> shared(Collection<String> left, Collection<String> right) {
        return left.stream()
                .filter(right::contains)
                .toList();
    }

    /**
     * @return the expected tags that are absent from the actual tags, in the iteration order of {@code expected}
     */
    private static List<String> missing(Collection<String> expected, Collection<String> actual) {
        return expected.stream()
                .filter(tag -> !actual.contains(tag))
                .toList();
    }

    private static List<String> speakerNames(Collection<Speaker> speakers) {
        return speakers.stream()
                .map(Speaker::name)
                .toList();
    }

    private static List<String> timeslotIds(Collection<Timeslot> timeslots) {
        return timeslots.stream()
                .map(Timeslot::getId)
                .toList();
    }

    /**
     * @return the distinct tags of all speakers, as selected by {@code tagExtractor}
     */
    private static List<String> speakerTags(Collection<Speaker> speakers,
            Function<Speaker, Collection<String>> tagExtractor) {
        return speakers.stream()
                .flatMap(speaker -> tagExtractor.apply(speaker).stream())
                .distinct()
                .toList();
    }

    // ************************************************************************
    // Hard constraints
    // ************************************************************************

    @Schema(description = "A talk occupies a room during a timeslot in which that room is unavailable.",
            allOf = { ConferenceSchedulingJustification.class })
    record RoomUnavailableAtTalkTimeslotJustification(
            @Schema(description = "The talk code.") String talk,
            @Schema(description = "The id of the room the talk is assigned to.") String room,
            @Schema(description = "The id of the timeslot the talk is assigned to.") String timeslot,
            @Schema(description = "The ids of all timeslots during which the room is unavailable.") List<String> unavailableTimeslots)
            implements
                ConferenceSchedulingJustification {

        public static RoomUnavailableAtTalkTimeslotJustification of(Talk talk) {
            return new RoomUnavailableAtTalkTimeslotJustification(talk.getCode(), talk.getRoom().id(),
                    talk.getTimeslot().getId(), timeslotIds(talk.getRoom().unavailableTimeslots()));
        }

        @Override
        public String getDescription() {
            return "Timeslot '%s' of talk '%s' is marked as unavailable for room '%s' [%s]."
                    .formatted(timeslot, talk, room, String.join(", ", unavailableTimeslots));
        }
    }

    @Schema(description = "Two talks share the same room while their timeslots overlap.",
            allOf = { ConferenceSchedulingJustification.class })
    record TalksOverlappingInSameRoomJustification(
            @Schema(description = "The code of the first talk.") String talk,
            @Schema(description = "The code of the second talk.") String otherTalk,
            @Schema(description = "The id of the room both talks are assigned to.") String room,
            @Schema(description = "The number of minutes during which both talks overlap.") int overlapInMinutes)
            implements
                ConferenceSchedulingJustification {

        public static TalksOverlappingInSameRoomJustification of(Talk talk, Talk otherTalk) {
            return new TalksOverlappingInSameRoomJustification(talk.getCode(), otherTalk.getCode(), talk.getRoom().id(),
                    talk.overlappingDurationInMinutes(otherTalk));
        }

        @Override
        public String getDescription() {
            return "Talks '%s' and '%s' share room '%s' and overlap for %d minutes."
                    .formatted(talk, otherTalk, room, overlapInMinutes);
        }
    }

    @Schema(description = "A talk is placed in a timeslot during which one of its speakers is unavailable.",
            allOf = { ConferenceSchedulingJustification.class })
    record SpeakerUnavailableAtTalkTimeslotJustification(
            @Schema(description = "The talk code.") String talk,
            @Schema(description = "The id of the unavailable speaker.") String speaker,
            @Schema(description = "The id of the timeslot the talk is assigned to.") String timeslot,
            @Schema(description = "The ids of all timeslots during which the speaker is unavailable.") List<String> unavailableTimeslots)
            implements
                ConferenceSchedulingJustification {

        public static SpeakerUnavailableAtTalkTimeslotJustification of(Talk talk, Speaker speaker) {
            return new SpeakerUnavailableAtTalkTimeslotJustification(talk.getCode(), speaker.id(),
                    talk.getTimeslot().getId(), timeslotIds(speaker.unavailableTimeslots()));
        }

        @Override
        public String getDescription() {
            return "Timeslot '%s' of talk '%s' is marked as unavailable for speaker '%s' [%s]."
                    .formatted(timeslot, talk, speaker, String.join(", ", unavailableTimeslots));
        }
    }

    @Schema(description = "A speaker is assigned to two talks whose timeslots overlap.",
            allOf = { ConferenceSchedulingJustification.class })
    record SpeakerAssignedToOverlappingTalksJustification(
            @Schema(description = "The id of the double-booked speaker.") String speaker,
            @Schema(description = "The code of the first talk.") String talk,
            @Schema(description = "The code of the second talk.") String otherTalk,
            @Schema(description = "The number of minutes during which both talks overlap.") int overlapInMinutes)
            implements
                ConferenceSchedulingJustification {

        public static SpeakerAssignedToOverlappingTalksJustification of(Talk talk, Talk otherTalk, Speaker speaker) {
            return new SpeakerAssignedToOverlappingTalksJustification(speaker.id(), talk.getCode(), otherTalk.getCode(),
                    otherTalk.overlappingDurationInMinutes(talk));
        }

        @Override
        public String getDescription() {
            return "Speaker '%s' is assigned to talks '%s' and '%s', which overlap for %d minutes."
                    .formatted(speaker, talk, otherTalk, overlapInMinutes);
        }
    }

    @Schema(description = "A talk starts before one of its prerequisite talks has finished.",
            allOf = { ConferenceSchedulingJustification.class })
    record TalkScheduledBeforePrerequisiteTalkJustification(
            @Schema(description = "The code of the talk that depends on the prerequisite.") String talk,
            @Schema(description = "The id of the timeslot the dependent talk is assigned to.") String timeslot,
            @Schema(description = "The code of the prerequisite talk.") String prerequisiteTalk,
            @Schema(description = "The id of the timeslot the prerequisite talk is assigned to.") String prerequisiteTimeslot)
            implements
                ConferenceSchedulingJustification {

        public static TalkScheduledBeforePrerequisiteTalkJustification of(Talk prerequisiteTalk, Talk talk) {
            return new TalkScheduledBeforePrerequisiteTalkJustification(talk.getCode(), talk.getTimeslot().getId(),
                    prerequisiteTalk.getCode(), prerequisiteTalk.getTimeslot().getId());
        }

        @Override
        public String getDescription() {
            return "Talk '%s' in timeslot '%s' must be scheduled after its prerequisite talk '%s' in timeslot '%s'."
                    .formatted(talk, timeslot, prerequisiteTalk, prerequisiteTimeslot);
        }
    }

    @Schema(description = "Two talks sharing a mutually-exclusive-talks tag overlap in time.",
            allOf = { ConferenceSchedulingJustification.class })
    record MutuallyExclusiveTalksOverlappingJustification(
            @Schema(description = "The code of the first talk.") String talk,
            @Schema(description = "The code of the second talk.") String otherTalk,
            @Schema(description = "The mutually-exclusive-talks tags both talks have in common.") List<String> sharedTags,
            @Schema(description = "The number of minutes during which both talks overlap.") int overlapInMinutes)
            implements
                ConferenceSchedulingJustification {

        public static MutuallyExclusiveTalksOverlappingJustification of(Talk talk, Talk otherTalk) {
            return new MutuallyExclusiveTalksOverlappingJustification(talk.getCode(), otherTalk.getCode(),
                    shared(talk.getMutuallyExclusiveTalksTags(), otherTalk.getMutuallyExclusiveTalksTags()),
                    talk.overlappingDurationInMinutes(otherTalk));
        }

        @Override
        public String getDescription() {
            return "Talks '%s' and '%s' share the mutually-exclusive-talks tags [%s] and overlap for %d minutes."
                    .formatted(talk, otherTalk, String.join(", ", sharedTags), overlapInMinutes);
        }
    }

    @Schema(description = "Two consecutive talks of the same speaker are not separated by the minimum required pause.",
            allOf = { ConferenceSchedulingJustification.class })
    record SpeakerConsecutiveTalksPauseTooShortJustification(
            @Schema(description = "The ids of the speakers giving both talks.") List<String> speakers,
            @Schema(description = "The code of the first talk.") String talk,
            @Schema(description = "The id of the timeslot the first talk is assigned to.") String timeslot,
            @Schema(description = "The code of the second talk.") String otherTalk,
            @Schema(description = "The id of the timeslot the second talk is assigned to.") String otherTimeslot)
            implements
                ConferenceSchedulingJustification {

        public static SpeakerConsecutiveTalksPauseTooShortJustification of(Talk talk, Talk otherTalk) {
            List<Speaker> sharedSpeakers = talk.getSpeakers().stream()
                    .filter(otherTalk.getSpeakers()::contains)
                    .toList();
            return new SpeakerConsecutiveTalksPauseTooShortJustification(speakerNames(sharedSpeakers),
                    talk.getCode(),
                    talk.getTimeslot().getId(), otherTalk.getCode(), otherTalk.getTimeslot().getId());
        }

        @Override
        public String getDescription() {
            return "Speakers [%s] do not get the minimum pause between their consecutive talks '%s' (timeslot '%s') and '%s' (timeslot '%s')."
                    .formatted(String.join(", ", speakers), talk, timeslot, otherTalk, otherTimeslot);
        }
    }

    @Schema(description = "A talk that needs crowd control is not paired with exactly one other crowd-control talk "
            + "in the same timeslot.",
            allOf = { ConferenceSchedulingJustification.class })
    record CrowdControlTalkNotPairedJustification(
            @Schema(description = "The talk code.") String talk,
            @Schema(description = "The id of the timeslot the talk is assigned to.") String timeslot,
            @Schema(description = "The crowd control risk of the talk.") int crowdControlRisk,
            @Schema(description = "The number of other crowd-control talks in the same timeslot; exactly one is required.") long pairedTalkCount)
            implements
                ConferenceSchedulingJustification {

        public static CrowdControlTalkNotPairedJustification of(Talk talk, long pairedTalkCount) {
            return new CrowdControlTalkNotPairedJustification(talk.getCode(), talk.getTimeslot().getId(),
                    talk.getCrowdControlRisk(), pairedTalkCount);
        }

        @Override
        public String getDescription() {
            return "Talk '%s' with crowd control risk %d is paired with %d other crowd-control talks in timeslot '%s', but exactly 1 is required."
                    .formatted(talk, crowdControlRisk, pairedTalkCount, timeslot);
        }
    }

    @Schema(description = "The timeslot of a talk does not carry every timeslot tag required by its speakers.",
            allOf = { ConferenceSchedulingJustification.class })
    record MissingRequiredTimeslotTagsForSpeakersJustification(
            @Schema(description = "The talk code.") String talk,
            @Schema(description = "The ids of the speakers presenting the talk.") List<String> speakers,
            @Schema(description = "The id of the timeslot the talk is assigned to.") String timeslot,
            @Schema(description = "The required timeslot tags the timeslot does not carry.") List<String> missingTags,
            @Schema(description = "The timeslot tags the timeslot actually carries.") List<String> timeslotTags)
            implements
                ConferenceSchedulingJustification {

        public static MissingRequiredTimeslotTagsForSpeakersJustification of(Talk talk) {
            List<String> requiredTags = speakerTags(talk.getSpeakers(), Speaker::requiredTimeslotTags);
            return new MissingRequiredTimeslotTagsForSpeakersJustification(talk.getCode(),
                    speakerNames(talk.getSpeakers()),
                    talk.getTimeslot().getId(), missing(requiredTags, talk.getTimeslot().getTags()),
                    talk.getTimeslot().getTags());
        }

        @Override
        public String getDescription() {
            return "Timeslot '%s' of talk '%s' is missing the timeslot tags [%s] required by speakers [%s]."
                    .formatted(timeslot, talk, String.join(", ", missingTags), String.join(", ", speakers));
        }
    }

    @Schema(description = "The timeslot of a talk carries a timeslot tag prohibited by its speakers.",
            allOf = { ConferenceSchedulingJustification.class })
    record ProhibitedTimeslotTagsForSpeakersJustification(
            @Schema(description = "The talk code.") String talk,
            @Schema(description = "The ids of the speakers presenting the talk.") List<String> speakers,
            @Schema(description = "The id of the timeslot the talk is assigned to.") String timeslot,
            @Schema(description = "The prohibited timeslot tags the timeslot carries.") List<String> prohibitedTags,
            @Schema(description = "The timeslot tags the timeslot actually carries.") List<String> timeslotTags)
            implements
                ConferenceSchedulingJustification {

        public static ProhibitedTimeslotTagsForSpeakersJustification of(Talk talk) {
            List<String> prohibitedTags = speakerTags(talk.getSpeakers(), Speaker::prohibitedTimeslotTags);
            return new ProhibitedTimeslotTagsForSpeakersJustification(talk.getCode(),
                    speakerNames(talk.getSpeakers()),
                    talk.getTimeslot().getId(), shared(prohibitedTags, talk.getTimeslot().getTags()),
                    talk.getTimeslot().getTags());
        }

        @Override
        public String getDescription() {
            return "Timeslot '%s' of talk '%s' carries the timeslot tags [%s] prohibited by speakers [%s]."
                    .formatted(timeslot, talk, String.join(", ", prohibitedTags), String.join(", ", speakers));
        }
    }

    @Schema(description = "The timeslot of a talk does not carry every timeslot tag the talk itself requires.",
            allOf = { ConferenceSchedulingJustification.class })
    record MissingRequiredTimeslotTagsForTalkJustification(
            @Schema(description = "The talk code.") String talk,
            @Schema(description = "The id of the timeslot the talk is assigned to.") String timeslot,
            @Schema(description = "The required timeslot tags the timeslot does not carry.") List<String> missingTags,
            @Schema(description = "The timeslot tags the timeslot actually carries.") List<String> timeslotTags)
            implements
                ConferenceSchedulingJustification {

        public static MissingRequiredTimeslotTagsForTalkJustification of(Talk talk) {
            return new MissingRequiredTimeslotTagsForTalkJustification(talk.getCode(), talk.getTimeslot().getId(),
                    missing(talk.getRequiredTimeslotTags(), talk.getTimeslot().getTags()),
                    talk.getTimeslot().getTags());
        }

        @Override
        public String getDescription() {
            return "Timeslot '%s' of talk '%s' is missing the required timeslot tags [%s]."
                    .formatted(timeslot, talk, String.join(", ", missingTags));
        }
    }

    @Schema(description = "The timeslot of a talk carries a timeslot tag the talk itself prohibits.",
            allOf = { ConferenceSchedulingJustification.class })
    record ProhibitedTimeslotTagsForTalkJustification(
            @Schema(description = "The talk code.") String talk,
            @Schema(description = "The id of the timeslot the talk is assigned to.") String timeslot,
            @Schema(description = "The prohibited timeslot tags the timeslot carries.") List<String> prohibitedTags,
            @Schema(description = "The timeslot tags the timeslot actually carries.") List<String> timeslotTags)
            implements
                ConferenceSchedulingJustification {

        public static ProhibitedTimeslotTagsForTalkJustification of(Talk talk) {
            return new ProhibitedTimeslotTagsForTalkJustification(talk.getCode(), talk.getTimeslot().getId(),
                    shared(talk.getProhibitedTimeslotTags(), talk.getTimeslot().getTags()),
                    talk.getTimeslot().getTags());
        }

        @Override
        public String getDescription() {
            return "Timeslot '%s' of talk '%s' carries the prohibited timeslot tags [%s]."
                    .formatted(timeslot, talk, String.join(", ", prohibitedTags));
        }
    }

    @Schema(description = "The room of a talk does not carry every room tag required by its speakers.",
            allOf = { ConferenceSchedulingJustification.class })
    record MissingRequiredRoomTagsForSpeakersJustification(
            @Schema(description = "The talk code.") String talk,
            @Schema(description = "The ids of the speakers presenting the talk.") List<String> speakers,
            @Schema(description = "The id of the room the talk is assigned to.") String room,
            @Schema(description = "The required room tags the room does not carry.") List<String> missingTags,
            @Schema(description = "The room tags the room actually carries.") List<String> roomTags)
            implements
                ConferenceSchedulingJustification {

        public static MissingRequiredRoomTagsForSpeakersJustification of(Talk talk) {
            List<String> requiredTags = speakerTags(talk.getSpeakers(), Speaker::requiredRoomTags);
            return new MissingRequiredRoomTagsForSpeakersJustification(talk.getCode(),
                    speakerNames(talk.getSpeakers()),
                    talk.getRoom().id(), missing(requiredTags, talk.getRoom().tags()),
                    talk.getRoom().tags());
        }

        @Override
        public String getDescription() {
            return "Room '%s' of talk '%s' is missing the room tags [%s] required by speakers [%s]."
                    .formatted(room, talk, String.join(", ", missingTags), String.join(", ", speakers));
        }
    }

    @Schema(description = "The room of a talk carries a room tag prohibited by its speakers.",
            allOf = { ConferenceSchedulingJustification.class })
    record ProhibitedRoomTagsForSpeakersJustification(
            @Schema(description = "The talk code.") String talk,
            @Schema(description = "The ids of the speakers presenting the talk.") List<String> speakers,
            @Schema(description = "The id of the room the talk is assigned to.") String room,
            @Schema(description = "The prohibited room tags the room carries.") List<String> prohibitedTags,
            @Schema(description = "The room tags the room actually carries.") List<String> roomTags)
            implements
                ConferenceSchedulingJustification {

        public static ProhibitedRoomTagsForSpeakersJustification of(Talk talk) {
            List<String> prohibitedTags = speakerTags(talk.getSpeakers(), Speaker::prohibitedRoomTags);
            return new ProhibitedRoomTagsForSpeakersJustification(talk.getCode(),
                    speakerNames(talk.getSpeakers()),
                    talk.getRoom().id(), shared(prohibitedTags, talk.getRoom().tags()),
                    talk.getRoom().tags());
        }

        @Override
        public String getDescription() {
            return "Room '%s' of talk '%s' carries the room tags [%s] prohibited by speakers [%s]."
                    .formatted(room, talk, String.join(", ", prohibitedTags), String.join(", ", speakers));
        }
    }

    @Schema(description = "The room of a talk does not carry every room tag the talk itself requires.",
            allOf = { ConferenceSchedulingJustification.class })
    record MissingRequiredRoomTagsForTalkJustification(
            @Schema(description = "The talk code.") String talk,
            @Schema(description = "The id of the room the talk is assigned to.") String room,
            @Schema(description = "The required room tags the room does not carry.") List<String> missingTags,
            @Schema(description = "The room tags the room actually carries.") List<String> roomTags)
            implements
                ConferenceSchedulingJustification {

        public static MissingRequiredRoomTagsForTalkJustification of(Talk talk) {
            return new MissingRequiredRoomTagsForTalkJustification(talk.getCode(), talk.getRoom().id(),
                    missing(talk.getRequiredRoomTags(), talk.getRoom().tags()),
                    talk.getRoom().tags());
        }

        @Override
        public String getDescription() {
            return "Room '%s' of talk '%s' is missing the required room tags [%s]."
                    .formatted(room, talk, String.join(", ", missingTags));
        }
    }

    @Schema(description = "The room of a talk carries a room tag the talk itself prohibits.",
            allOf = { ConferenceSchedulingJustification.class })
    record ProhibitedRoomTagsForTalkJustification(
            @Schema(description = "The talk code.") String talk,
            @Schema(description = "The id of the room the talk is assigned to.") String room,
            @Schema(description = "The prohibited room tags the room carries.") List<String> prohibitedTags,
            @Schema(description = "The room tags the room actually carries.") List<String> roomTags)
            implements
                ConferenceSchedulingJustification {

        public static ProhibitedRoomTagsForTalkJustification of(Talk talk) {
            return new ProhibitedRoomTagsForTalkJustification(talk.getCode(), talk.getRoom().id(),
                    shared(talk.getProhibitedRoomTags(), talk.getRoom().tags()),
                    talk.getRoom().tags());
        }

        @Override
        public String getDescription() {
            return "Room '%s' of talk '%s' carries the prohibited room tags [%s]."
                    .formatted(room, talk, String.join(", ", prohibitedTags));
        }
    }

    // ************************************************************************
    // Soft constraints
    // ************************************************************************

    @Schema(description = "Two talks sharing a theme track overlap in time.",
            allOf = { ConferenceSchedulingJustification.class })
    record TalksWithSameThemeTrackOverlappingJustification(
            @Schema(description = "The code of the first talk.") String talk,
            @Schema(description = "The code of the second talk.") String otherTalk,
            @Schema(description = "The theme track tags both talks have in common.") List<String> sharedThemeTrackTags,
            @Schema(description = "The number of minutes during which both talks overlap.") int overlapInMinutes)
            implements
                ConferenceSchedulingJustification {

        public static TalksWithSameThemeTrackOverlappingJustification of(Talk talk, Talk otherTalk) {
            return new TalksWithSameThemeTrackOverlappingJustification(talk.getCode(), otherTalk.getCode(),
                    shared(talk.getThemeTrackTags(), otherTalk.getThemeTrackTags()),
                    talk.overlappingDurationInMinutes(otherTalk));
        }

        @Override
        public String getDescription() {
            return "Talks '%s' and '%s' share the theme track tags [%s] and overlap for %d minutes."
                    .formatted(talk, otherTalk, String.join(", ", sharedThemeTrackTags), overlapInMinutes);
        }
    }

    @Schema(description = "Two talks sharing a theme track on the same day are scheduled in different rooms.",
            allOf = { ConferenceSchedulingJustification.class })
    record TalksWithSameThemeTrackInDifferentRoomsJustification(
            @Schema(description = "The day on which both talks are scheduled.") LocalDate date,
            @Schema(description = "The code of the first talk.") String talk,
            @Schema(description = "The id of the room the first talk is assigned to.") String room,
            @Schema(description = "The code of the second talk.") String otherTalk,
            @Schema(description = "The id of the room the second talk is assigned to.") String otherRoom,
            @Schema(description = "The theme track tags both talks have in common.") List<String> sharedThemeTrackTags)
            implements
                ConferenceSchedulingJustification {

        public static TalksWithSameThemeTrackInDifferentRoomsJustification of(Talk talk, Talk otherTalk) {
            return new TalksWithSameThemeTrackInDifferentRoomsJustification(
                    talk.getTimeslot().getStartDateTime().toLocalDate(), talk.getCode(), talk.getRoom().id(),
                    otherTalk.getCode(), otherTalk.getRoom().id(),
                    shared(talk.getThemeTrackTags(), otherTalk.getThemeTrackTags()));
        }

        @Override
        public String getDescription() {
            return "Talks '%s' and '%s' on %s share the theme track tags [%s] but are scheduled in different rooms '%s' and '%s'."
                    .formatted(talk, otherTalk, date, String.join(", ", sharedThemeTrackTags), room, otherRoom);
        }
    }

    @Schema(description = "Two talks sharing a sector overlap in time.",
            allOf = { ConferenceSchedulingJustification.class })
    record TalksWithSameSectorOverlappingJustification(
            @Schema(description = "The code of the first talk.") String talk,
            @Schema(description = "The code of the second talk.") String otherTalk,
            @Schema(description = "The sector tags both talks have in common.") List<String> sharedSectorTags,
            @Schema(description = "The number of minutes during which both talks overlap.") int overlapInMinutes)
            implements
                ConferenceSchedulingJustification {

        public static TalksWithSameSectorOverlappingJustification of(Talk talk, Talk otherTalk) {
            return new TalksWithSameSectorOverlappingJustification(talk.getCode(), otherTalk.getCode(),
                    shared(talk.getSectorTags(), otherTalk.getSectorTags()),
                    talk.overlappingDurationInMinutes(otherTalk));
        }

        @Override
        public String getDescription() {
            return "Talks '%s' and '%s' share the sector tags [%s] and overlap for %d minutes."
                    .formatted(talk, otherTalk, String.join(", ", sharedSectorTags), overlapInMinutes);
        }
    }

    @Schema(description = "Two talks in the same timeslot share an audience type, which is rewarded.",
            allOf = { ConferenceSchedulingJustification.class })
    record TalksWithSameAudienceTypeInSameTimeslotJustification(
            @Schema(description = "The id of the timeslot both talks are assigned to.") String timeslot,
            @Schema(description = "The code of the first talk.") String talk,
            @Schema(description = "The code of the second talk.") String otherTalk,
            @Schema(description = "The audience types both talks have in common.") List<String> sharedAudienceTypes)
            implements
                ConferenceSchedulingJustification {

        public static TalksWithSameAudienceTypeInSameTimeslotJustification of(Talk talk, Talk otherTalk) {
            return new TalksWithSameAudienceTypeInSameTimeslotJustification(talk.getTimeslot().getId(), talk.getCode(),
                    otherTalk.getCode(), shared(talk.getAudienceTypes(), otherTalk.getAudienceTypes()));
        }

        @Override
        public String getDescription() {
            return "Talks '%s' and '%s' in timeslot '%s' share the audience types [%s]."
                    .formatted(talk, otherTalk, timeslot, String.join(", ", sharedAudienceTypes));
        }
    }

    @Schema(description = "Two talks sharing both a theme track and an audience type overlap in time.",
            allOf = { ConferenceSchedulingJustification.class })
    record TalksWithSameThemeTrackAndAudienceTypeOverlappingJustification(
            @Schema(description = "The code of the first talk.") String talk,
            @Schema(description = "The code of the second talk.") String otherTalk,
            @Schema(description = "The theme track tags both talks have in common.") List<String> sharedThemeTrackTags,
            @Schema(description = "The audience types both talks have in common.") List<String> sharedAudienceTypes,
            @Schema(description = "The number of minutes during which both talks overlap.") int overlapInMinutes)
            implements
                ConferenceSchedulingJustification {

        public static TalksWithSameThemeTrackAndAudienceTypeOverlappingJustification of(Talk talk, Talk otherTalk) {
            return new TalksWithSameThemeTrackAndAudienceTypeOverlappingJustification(talk.getCode(), otherTalk.getCode(),
                    shared(talk.getThemeTrackTags(), otherTalk.getThemeTrackTags()),
                    shared(talk.getAudienceTypes(), otherTalk.getAudienceTypes()),
                    talk.overlappingDurationInMinutes(otherTalk));
        }

        @Override
        public String getDescription() {
            return "Talks '%s' and '%s' share the theme track tags [%s] and the audience types [%s], and overlap for %d minutes."
                    .formatted(talk, otherTalk, String.join(", ", sharedThemeTrackTags),
                            String.join(", ", sharedAudienceTypes), overlapInMinutes);
        }
    }

    @Schema(description = "Two talks in the same timeslot target a different audience level, which is rewarded.",
            allOf = { ConferenceSchedulingJustification.class })
    record TalksWithDifferentAudienceLevelInSameTimeslotJustification(
            @Schema(description = "The id of the timeslot both talks are assigned to.") String timeslot,
            @Schema(description = "The code of the first talk.") String talk,
            @Schema(description = "The audience level of the first talk.") int audienceLevel,
            @Schema(description = "The code of the second talk.") String otherTalk,
            @Schema(description = "The audience level of the second talk.") int otherAudienceLevel)
            implements
                ConferenceSchedulingJustification {

        public static TalksWithDifferentAudienceLevelInSameTimeslotJustification of(Talk talk, Talk otherTalk) {
            return new TalksWithDifferentAudienceLevelInSameTimeslotJustification(talk.getTimeslot().getId(), talk.getCode(),
                    talk.getAudienceLevel(), otherTalk.getCode(), otherTalk.getAudienceLevel());
        }

        @Override
        public String getDescription() {
            return "Talks '%s' and '%s' in timeslot '%s' have the different audience levels %d and %d."
                    .formatted(talk, otherTalk, timeslot, audienceLevel, otherAudienceLevel);
        }
    }

    @Schema(description = "Two talks share content, but the talk with the higher audience level is not scheduled after the talk "
            + "with the lower audience level, breaking the rising audience level flow.",
            allOf = { ConferenceSchedulingJustification.class })
    record SharedContentAudienceLevelFlowViolationJustification(
            @Schema(description = "The code of the talk with the lower audience level.") String talk,
            @Schema(description = "The audience level of that talk.") int audienceLevel,
            @Schema(description = "The id of the timeslot that talk is assigned to.") String timeslot,
            @Schema(description = "The code of the talk with the higher audience level.") String higherLevelTalk,
            @Schema(description = "The audience level of that talk.") int higherAudienceLevel,
            @Schema(description = "The id of the timeslot that talk is assigned to.") String higherLevelTimeslot,
            @Schema(description = "The content tags both talks have in common.") List<String> sharedContentTags)
            implements
                ConferenceSchedulingJustification {

        public static SharedContentAudienceLevelFlowViolationJustification of(Talk talk, Talk higherLevelTalk) {
            return new SharedContentAudienceLevelFlowViolationJustification(talk.getCode(), talk.getAudienceLevel(),
                    talk.getTimeslot().getId(), higherLevelTalk.getCode(), higherLevelTalk.getAudienceLevel(),
                    higherLevelTalk.getTimeslot().getId(),
                    shared(talk.getContentTags(), higherLevelTalk.getContentTags()));
        }

        @Override
        public String getDescription() {
            return "Talks '%s' (audience level %d, timeslot '%s') and '%s' (audience level %d, timeslot '%s') share the content tags [%s], but the higher audience level talk is not scheduled after the lower one."
                    .formatted(talk, audienceLevel, timeslot, higherLevelTalk, higherAudienceLevel, higherLevelTimeslot,
                            String.join(", ", sharedContentTags));
        }
    }

    @Schema(description = "Two talks sharing content overlap in time.",
            allOf = { ConferenceSchedulingJustification.class })
    record TalksWithSameContentOverlappingJustification(
            @Schema(description = "The code of the first talk.") String talk,
            @Schema(description = "The code of the second talk.") String otherTalk,
            @Schema(description = "The content tags both talks have in common.") List<String> sharedContentTags,
            @Schema(description = "The number of minutes during which both talks overlap.") int overlapInMinutes)
            implements
                ConferenceSchedulingJustification {

        public static TalksWithSameContentOverlappingJustification of(Talk talk, Talk otherTalk) {
            return new TalksWithSameContentOverlappingJustification(talk.getCode(), otherTalk.getCode(),
                    shared(talk.getContentTags(), otherTalk.getContentTags()),
                    talk.overlappingDurationInMinutes(otherTalk));
        }

        @Override
        public String getDescription() {
            return "Talks '%s' and '%s' share the content tags [%s] and overlap for %d minutes."
                    .formatted(talk, otherTalk, String.join(", ", sharedContentTags), overlapInMinutes);
        }
    }

    @Schema(description = "Two talks in the same timeslot are given in the same language.",
            allOf = { ConferenceSchedulingJustification.class })
    record TalksWithSameLanguageInSameTimeslotJustification(
            @Schema(description = "The id of the timeslot both talks are assigned to.") String timeslot,
            @Schema(description = "The code of the first talk.") String talk,
            @Schema(description = "The code of the second talk.") String otherTalk,
            @Schema(description = "The language both talks are given in.") String language)
            implements
                ConferenceSchedulingJustification {

        public static TalksWithSameLanguageInSameTimeslotJustification of(Talk talk, Talk otherTalk) {
            return new TalksWithSameLanguageInSameTimeslotJustification(talk.getTimeslot().getId(), talk.getCode(),
                    otherTalk.getCode(), talk.getLanguage());
        }

        @Override
        public String getDescription() {
            return "Talks '%s' and '%s' in timeslot '%s' are both given in language '%s'."
                    .formatted(talk, otherTalk, timeslot, language);
        }
    }

    @Schema(description = "Two talks sharing content or a theme track are scheduled on different days.",
            allOf = { ConferenceSchedulingJustification.class })
    record RelatedTalksNotOnSameDayJustification(
            @Schema(description = "The code of the first talk.") String talk,
            @Schema(description = "The day on which the first talk is scheduled.") LocalDate date,
            @Schema(description = "The code of the second talk.") String otherTalk,
            @Schema(description = "The day on which the second talk is scheduled.") LocalDate otherDate,
            @Schema(description = "The content tags both talks have in common.") List<String> sharedContentTags,
            @Schema(description = "The theme track tags both talks have in common.") List<String> sharedThemeTrackTags)
            implements
                ConferenceSchedulingJustification {

        public static RelatedTalksNotOnSameDayJustification of(Talk talk, Talk otherTalk) {
            return new RelatedTalksNotOnSameDayJustification(talk.getCode(),
                    talk.getTimeslot().getStartDateTime().toLocalDate(), otherTalk.getCode(),
                    otherTalk.getTimeslot().getStartDateTime().toLocalDate(),
                    shared(talk.getContentTags(), otherTalk.getContentTags()),
                    shared(talk.getThemeTrackTags(), otherTalk.getThemeTrackTags()));
        }

        @Override
        public String getDescription() {
            return "Talks '%s' (%s) and '%s' (%s) share the content tags [%s] and the theme track tags [%s], but are not scheduled on the same day."
                    .formatted(talk, date, otherTalk, otherDate, String.join(", ", sharedContentTags),
                            String.join(", ", sharedThemeTrackTags));
        }
    }

    @Schema(description = "A more popular talk is placed in a smaller room than a less popular one.",
            allOf = { ConferenceSchedulingJustification.class })
    record PopularTalkInSmallerRoomJustification(
            @Schema(description = "The code of the more popular talk.") String talk,
            @Schema(description = "The favorite count of the more popular talk.") int favoriteCount,
            @Schema(description = "The id of the room the more popular talk is assigned to.") String room,
            @Schema(description = "The capacity of that room.") int roomCapacity,
            @Schema(description = "The code of the less popular talk.") String lessPopularTalk,
            @Schema(description = "The favorite count of the less popular talk.") int lessPopularFavoriteCount,
            @Schema(description = "The id of the room the less popular talk is assigned to.") String lessPopularRoom,
            @Schema(description = "The capacity of that room.") int lessPopularRoomCapacity)
            implements
                ConferenceSchedulingJustification {

        public static PopularTalkInSmallerRoomJustification of(Talk lessPopularTalk, Talk talk) {
            return new PopularTalkInSmallerRoomJustification(talk.getCode(), talk.getFavoriteCount(), talk.getRoom().id(),
                    talk.getRoom().capacity(), lessPopularTalk.getCode(), lessPopularTalk.getFavoriteCount(),
                    lessPopularTalk.getRoom().id(), lessPopularTalk.getRoom().capacity());
        }

        @Override
        public String getDescription() {
            return "Talk '%s' with %d favorites is scheduled in room '%s' with capacity %d, while the less popular talk '%s' with %d favorites is scheduled in the larger room '%s' with capacity %d."
                    .formatted(talk, favoriteCount, room, roomCapacity, lessPopularTalk, lessPopularFavoriteCount,
                            lessPopularRoom, lessPopularRoomCapacity);
        }
    }

    @Schema(description = "The timeslot of a talk does not carry every timeslot tag preferred by its speakers.",
            allOf = { ConferenceSchedulingJustification.class })
    record MissingPreferredTimeslotTagsForSpeakersJustification(
            @Schema(description = "The talk code.") String talk,
            @Schema(description = "The ids of the speakers presenting the talk.") List<String> speakers,
            @Schema(description = "The id of the timeslot the talk is assigned to.") String timeslot,
            @Schema(description = "The preferred timeslot tags the timeslot does not carry.") List<String> missingTags,
            @Schema(description = "The timeslot tags the timeslot actually carries.") List<String> timeslotTags)
            implements
                ConferenceSchedulingJustification {

        public static MissingPreferredTimeslotTagsForSpeakersJustification of(Talk talk) {
            List<String> preferredTags = speakerTags(talk.getSpeakers(), Speaker::preferredTimeslotTags);
            return new MissingPreferredTimeslotTagsForSpeakersJustification(talk.getCode(),
                    speakerNames(talk.getSpeakers()),
                    talk.getTimeslot().getId(), missing(preferredTags, talk.getTimeslot().getTags()),
                    talk.getTimeslot().getTags());
        }

        @Override
        public String getDescription() {
            return "Timeslot '%s' of talk '%s' is missing the timeslot tags [%s] preferred by speakers [%s]."
                    .formatted(timeslot, talk, String.join(", ", missingTags), String.join(", ", speakers));
        }
    }

    @Schema(description = "The timeslot of a talk carries a timeslot tag its speakers find undesired.",
            allOf = { ConferenceSchedulingJustification.class })
    record UndesiredTimeslotTagsForSpeakersJustification(
            @Schema(description = "The talk code.") String talk,
            @Schema(description = "The ids of the speakers presenting the talk.") List<String> speakers,
            @Schema(description = "The id of the timeslot the talk is assigned to.") String timeslot,
            @Schema(description = "The undesired timeslot tags the timeslot carries.") List<String> undesiredTags,
            @Schema(description = "The timeslot tags the timeslot actually carries.") List<String> timeslotTags)
            implements
                ConferenceSchedulingJustification {

        public static UndesiredTimeslotTagsForSpeakersJustification of(Talk talk) {
            List<String> undesiredTags = speakerTags(talk.getSpeakers(), Speaker::undesiredTimeslotTags);
            return new UndesiredTimeslotTagsForSpeakersJustification(talk.getCode(),
                    speakerNames(talk.getSpeakers()),
                    talk.getTimeslot().getId(), shared(undesiredTags, talk.getTimeslot().getTags()),
                    talk.getTimeslot().getTags());
        }

        @Override
        public String getDescription() {
            return "Timeslot '%s' of talk '%s' carries the timeslot tags [%s] undesired by speakers [%s]."
                    .formatted(timeslot, talk, String.join(", ", undesiredTags), String.join(", ", speakers));
        }
    }

    @Schema(description = "The timeslot of a talk does not carry every timeslot tag the talk itself prefers.",
            allOf = { ConferenceSchedulingJustification.class })
    record MissingPreferredTimeslotTagsForTalkJustification(
            @Schema(description = "The talk code.") String talk,
            @Schema(description = "The id of the timeslot the talk is assigned to.") String timeslot,
            @Schema(description = "The preferred timeslot tags the timeslot does not carry.") List<String> missingTags,
            @Schema(description = "The timeslot tags the timeslot actually carries.") List<String> timeslotTags)
            implements
                ConferenceSchedulingJustification {

        public static MissingPreferredTimeslotTagsForTalkJustification of(Talk talk) {
            return new MissingPreferredTimeslotTagsForTalkJustification(talk.getCode(), talk.getTimeslot().getId(),
                    missing(talk.getPreferredTimeslotTags(), talk.getTimeslot().getTags()),
                    talk.getTimeslot().getTags());
        }

        @Override
        public String getDescription() {
            return "Timeslot '%s' of talk '%s' is missing the preferred timeslot tags [%s]."
                    .formatted(timeslot, talk, String.join(", ", missingTags));
        }
    }

    @Schema(description = "The timeslot of a talk carries a timeslot tag the talk itself finds undesired.",
            allOf = { ConferenceSchedulingJustification.class })
    record UndesiredTimeslotTagsForTalkJustification(
            @Schema(description = "The talk code.") String talk,
            @Schema(description = "The id of the timeslot the talk is assigned to.") String timeslot,
            @Schema(description = "The undesired timeslot tags the timeslot carries.") List<String> undesiredTags,
            @Schema(description = "The timeslot tags the timeslot actually carries.") List<String> timeslotTags)
            implements
                ConferenceSchedulingJustification {

        public static UndesiredTimeslotTagsForTalkJustification of(Talk talk) {
            return new UndesiredTimeslotTagsForTalkJustification(talk.getCode(), talk.getTimeslot().getId(),
                    shared(talk.getUndesiredTimeslotTags(), talk.getTimeslot().getTags()),
                    talk.getTimeslot().getTags());
        }

        @Override
        public String getDescription() {
            return "Timeslot '%s' of talk '%s' carries the undesired timeslot tags [%s]."
                    .formatted(timeslot, talk, String.join(", ", undesiredTags));
        }
    }

    @Schema(description = "The room of a talk does not carry every room tag preferred by its speakers.",
            allOf = { ConferenceSchedulingJustification.class })
    record MissingPreferredRoomTagsForSpeakersJustification(
            @Schema(description = "The talk code.") String talk,
            @Schema(description = "The ids of the speakers presenting the talk.") List<String> speakers,
            @Schema(description = "The id of the room the talk is assigned to.") String room,
            @Schema(description = "The preferred room tags the room does not carry.") List<String> missingTags,
            @Schema(description = "The room tags the room actually carries.") List<String> roomTags)
            implements
                ConferenceSchedulingJustification {

        public static MissingPreferredRoomTagsForSpeakersJustification of(Talk talk) {
            List<String> preferredTags = speakerTags(talk.getSpeakers(), Speaker::preferredRoomTags);
            return new MissingPreferredRoomTagsForSpeakersJustification(talk.getCode(),
                    speakerNames(talk.getSpeakers()),
                    talk.getRoom().id(), missing(preferredTags, talk.getRoom().tags()),
                    talk.getRoom().tags());
        }

        @Override
        public String getDescription() {
            return "Room '%s' of talk '%s' is missing the room tags [%s] preferred by speakers [%s]."
                    .formatted(room, talk, String.join(", ", missingTags), String.join(", ", speakers));
        }
    }

    @Schema(description = "The room of a talk carries a room tag its speakers find undesired.",
            allOf = { ConferenceSchedulingJustification.class })
    record UndesiredRoomTagsForSpeakersJustification(
            @Schema(description = "The talk code.") String talk,
            @Schema(description = "The ids of the speakers presenting the talk.") List<String> speakers,
            @Schema(description = "The id of the room the talk is assigned to.") String room,
            @Schema(description = "The undesired room tags the room carries.") List<String> undesiredTags,
            @Schema(description = "The room tags the room actually carries.") List<String> roomTags)
            implements
                ConferenceSchedulingJustification {

        public static UndesiredRoomTagsForSpeakersJustification of(Talk talk) {
            List<String> undesiredTags = speakerTags(talk.getSpeakers(), Speaker::undesiredRoomTags);
            return new UndesiredRoomTagsForSpeakersJustification(talk.getCode(), speakerNames(talk.getSpeakers()),
                    talk.getRoom().id(), shared(undesiredTags, talk.getRoom().tags()),
                    talk.getRoom().tags());
        }

        @Override
        public String getDescription() {
            return "Room '%s' of talk '%s' carries the room tags [%s] undesired by speakers [%s]."
                    .formatted(room, talk, String.join(", ", undesiredTags), String.join(", ", speakers));
        }
    }

    @Schema(description = "The room of a talk does not carry every room tag the talk itself prefers.",
            allOf = { ConferenceSchedulingJustification.class })
    record MissingPreferredRoomTagsForTalkJustification(
            @Schema(description = "The talk code.") String talk,
            @Schema(description = "The id of the room the talk is assigned to.") String room,
            @Schema(description = "The preferred room tags the room does not carry.") List<String> missingTags,
            @Schema(description = "The room tags the room actually carries.") List<String> roomTags)
            implements
                ConferenceSchedulingJustification {

        public static MissingPreferredRoomTagsForTalkJustification of(Talk talk) {
            return new MissingPreferredRoomTagsForTalkJustification(talk.getCode(), talk.getRoom().id(),
                    missing(talk.getPreferredRoomTags(), talk.getRoom().tags()),
                    talk.getRoom().tags());
        }

        @Override
        public String getDescription() {
            return "Room '%s' of talk '%s' is missing the preferred room tags [%s]."
                    .formatted(room, talk, String.join(", ", missingTags));
        }
    }

    @Schema(description = "The room of a talk carries a room tag the talk itself finds undesired.",
            allOf = { ConferenceSchedulingJustification.class })
    record UndesiredRoomTagsForTalkJustification(
            @Schema(description = "The talk code.") String talk,
            @Schema(description = "The id of the room the talk is assigned to.") String room,
            @Schema(description = "The undesired room tags the room carries.") List<String> undesiredTags,
            @Schema(description = "The room tags the room actually carries.") List<String> roomTags)
            implements
                ConferenceSchedulingJustification {

        public static UndesiredRoomTagsForTalkJustification of(Talk talk) {
            return new UndesiredRoomTagsForTalkJustification(talk.getCode(), talk.getRoom().id(),
                    shared(talk.getUndesiredRoomTags(), talk.getRoom().tags()),
                    talk.getRoom().tags());
        }

        @Override
        public String getDescription() {
            return "Room '%s' of talk '%s' carries the undesired room tags [%s]."
                    .formatted(room, talk, String.join(", ", undesiredTags));
        }
    }

    @Schema(description = "The talks of a speaker are spread over more than two days.",
            allOf = { ConferenceSchedulingJustification.class })
    record SpeakerMakespanTooLongJustification(
            @Schema(description = "The id of the speaker.") String speaker,
            @Schema(description = "The number of days between the speaker's first and last talk.") int daysBetweenTalks)
            implements
                ConferenceSchedulingJustification {

        public static SpeakerMakespanTooLongJustification of(Speaker speaker, int daysBetweenTalks) {
            return new SpeakerMakespanTooLongJustification(speaker.id(), daysBetweenTalks);
        }

        @Override
        public String getDescription() {
            return "The talks of speaker '%s' are spread over %d days."
                    .formatted(speaker, daysBetweenTalks);
        }
    }
}
