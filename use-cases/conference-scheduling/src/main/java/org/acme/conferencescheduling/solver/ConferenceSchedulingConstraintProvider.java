package org.acme.conferencescheduling.solver;

import static ai.timefold.solver.core.api.score.stream.ConstraintCollectors.compose;
import static ai.timefold.solver.core.api.score.stream.ConstraintCollectors.countBi;
import static ai.timefold.solver.core.api.score.stream.ConstraintCollectors.max;
import static ai.timefold.solver.core.api.score.stream.ConstraintCollectors.min;
import static ai.timefold.solver.core.api.score.stream.Joiners.containedIn;
import static ai.timefold.solver.core.api.score.stream.Joiners.containing;
import static ai.timefold.solver.core.api.score.stream.Joiners.containingAnyOf;
import static ai.timefold.solver.core.api.score.stream.Joiners.equal;
import static ai.timefold.solver.core.api.score.stream.Joiners.filtering;
import static ai.timefold.solver.core.api.score.stream.Joiners.greaterThan;
import static ai.timefold.solver.core.api.score.stream.Joiners.lessThan;
import static ai.timefold.solver.core.api.score.stream.Joiners.overlapping;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

import ai.timefold.solver.core.api.score.HardMediumSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.service.definition.api.description.ConstraintInfo;

import org.acme.conferencescheduling.domain.ConferenceConstraintProperties;
import org.acme.conferencescheduling.domain.Speaker;
import org.acme.conferencescheduling.domain.Talk;
import org.acme.conferencescheduling.domain.justification.CrowdControlTalkNotPairedJustification;
import org.acme.conferencescheduling.domain.justification.MissingPreferredRoomTagsForSpeakersJustification;
import org.acme.conferencescheduling.domain.justification.MissingPreferredRoomTagsForTalkJustification;
import org.acme.conferencescheduling.domain.justification.MissingPreferredTimeslotTagsForSpeakersJustification;
import org.acme.conferencescheduling.domain.justification.MissingPreferredTimeslotTagsForTalkJustification;
import org.acme.conferencescheduling.domain.justification.MissingRequiredRoomTagsForSpeakersJustification;
import org.acme.conferencescheduling.domain.justification.MissingRequiredRoomTagsForTalkJustification;
import org.acme.conferencescheduling.domain.justification.MissingRequiredTimeslotTagsForSpeakersJustification;
import org.acme.conferencescheduling.domain.justification.MissingRequiredTimeslotTagsForTalkJustification;
import org.acme.conferencescheduling.domain.justification.MutuallyExclusiveTalksOverlappingJustification;
import org.acme.conferencescheduling.domain.justification.PopularTalkInSmallerRoomJustification;
import org.acme.conferencescheduling.domain.justification.ProhibitedRoomTagsForSpeakersJustification;
import org.acme.conferencescheduling.domain.justification.ProhibitedRoomTagsForTalkJustification;
import org.acme.conferencescheduling.domain.justification.ProhibitedTimeslotTagsForSpeakersJustification;
import org.acme.conferencescheduling.domain.justification.ProhibitedTimeslotTagsForTalkJustification;
import org.acme.conferencescheduling.domain.justification.RelatedTalksNotOnSameDayJustification;
import org.acme.conferencescheduling.domain.justification.RoomUnavailableAtTalkTimeslotJustification;
import org.acme.conferencescheduling.domain.justification.SharedContentAudienceLevelFlowViolationJustification;
import org.acme.conferencescheduling.domain.justification.SpeakerAssignedToOverlappingTalksJustification;
import org.acme.conferencescheduling.domain.justification.SpeakerConsecutiveTalksPauseTooShortJustification;
import org.acme.conferencescheduling.domain.justification.SpeakerMakespanTooLongJustification;
import org.acme.conferencescheduling.domain.justification.SpeakerUnavailableAtTalkTimeslotJustification;
import org.acme.conferencescheduling.domain.justification.TalkScheduledBeforePrerequisiteTalkJustification;
import org.acme.conferencescheduling.domain.justification.TalksOverlappingInSameRoomJustification;
import org.acme.conferencescheduling.domain.justification.TalksWithDifferentAudienceLevelInSameTimeslotJustification;
import org.acme.conferencescheduling.domain.justification.TalksWithSameAudienceTypeInSameTimeslotJustification;
import org.acme.conferencescheduling.domain.justification.TalksWithSameContentOverlappingJustification;
import org.acme.conferencescheduling.domain.justification.TalksWithSameLanguageInSameTimeslotJustification;
import org.acme.conferencescheduling.domain.justification.TalksWithSameSectorOverlappingJustification;
import org.acme.conferencescheduling.domain.justification.TalksWithSameThemeTrackAndAudienceTypeOverlappingJustification;
import org.acme.conferencescheduling.domain.justification.TalksWithSameThemeTrackInDifferentRoomsJustification;
import org.acme.conferencescheduling.domain.justification.TalksWithSameThemeTrackOverlappingJustification;
import org.acme.conferencescheduling.domain.justification.UndesiredRoomTagsForSpeakersJustification;
import org.acme.conferencescheduling.domain.justification.UndesiredRoomTagsForTalkJustification;
import org.acme.conferencescheduling.domain.justification.UndesiredTimeslotTagsForSpeakersJustification;
import org.acme.conferencescheduling.domain.justification.UndesiredTimeslotTagsForTalkJustification;

/**
 * Provides the constraints for the conference scheduling problem.
 * <p>
 * Makes heavy use of CS expand() functionality to cache computation results,
 * except in cases where doing so less is efficient than recomputing the result.
 * That is the case in filtering joiners.
 * In this case, it is better to reduce the size of the joins even at the expense of duplicating some calculations.
 * In other words, time saved by caching those calculations is far outweighed by the time spent in unrestricted joins.
 * <p>
 * Every constraint carries a {@link ConstraintInfo} describing it and assigning it to a
 * {@link ConferenceScheduleConstraintGroup}, so the Timefold Platform can present the constraints grouped and explained.
 */
public class ConferenceSchedulingConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory factory) {
        return new Constraint[] {
                // Hard constraints
                roomUnavailableTimeslot(factory),
                roomConflict(factory),
                speakerUnavailableTimeslot(factory),
                speakerConflict(factory),
                talkPrerequisiteTalks(factory),
                talkMutuallyExclusiveTalksTags(factory),
                consecutiveTalksPause(factory),
                crowdControl(factory),
                speakerRequiredTimeslotTags(factory),
                speakerProhibitedTimeslotTags(factory),
                talkRequiredTimeslotTags(factory),
                talkProhibitedTimeslotTags(factory),
                speakerRequiredRoomTags(factory),
                speakerProhibitedRoomTags(factory),
                talkRequiredRoomTags(factory),
                talkProhibitedRoomTags(factory),

                // Soft constraints
                themeTrackConflict(factory),
                themeTrackRoomStability(factory),
                sectorConflict(factory),
                audienceTypeDiversity(factory),
                audienceTypeThemeTrackConflict(factory),
                audienceLevelDiversity(factory),
                contentAudienceLevelFlowViolation(factory),
                contentConflict(factory),
                languageDiversity(factory),
                sameDayTalks(factory),
                popularTalks(factory),
                speakerPreferredTimeslotTags(factory),
                speakerUndesiredTimeslotTags(factory),
                talkPreferredTimeslotTags(factory),
                talkUndesiredTimeslotTags(factory),
                speakerPreferredRoomTags(factory),
                speakerUndesiredRoomTags(factory),
                talkPreferredRoomTags(factory),
                talkUndesiredRoomTags(factory),
                speakerMakespan(factory)
        };
    }

    // ************************************************************************
    // Hard constraints
    // ************************************************************************

    // A talk must not occupy a room during a timeslot in which that room is marked unavailable.
    Constraint roomUnavailableTimeslot(ConstraintFactory factory) {
        return factory.forEach(Talk.class)
                .filter(Talk::hasUnavailableRoom)
                .penalize(HardMediumSoftScore.ofHard(100_000), Talk::getDurationInMinutes)
                .justifyWith((talk, score) -> RoomUnavailableAtTalkTimeslotJustification.of(talk))
                .asConstraint(new ConstraintInfo(ConferenceConstraintProperties.ROOM_UNAVAILABLE_TIMESLOT,
                        ConferenceConstraintProperties.ROOM_UNAVAILABLE_TIMESLOT,
                        "A talk must not be scheduled in a room during a timeslot when that room is unavailable.",
                        ConferenceScheduleConstraintGroup.CONFLICT_AVOIDANCE));
    }

    // Two talks must not share the same room while their timeslots overlap.
    Constraint roomConflict(ConstraintFactory factory) {
        return factory.forEachUniquePair(Talk.class,
                equal(Talk::getRoom),
                overlapping(t -> t.getTimeslot().getStartDateTime(), t -> t.getTimeslot().getEndDateTime()))
                .penalize(HardMediumSoftScore.ofHard(1_000), Talk::overlappingDurationInMinutes)
                .justifyWith((talk, talk2, score) -> TalksOverlappingInSameRoomJustification.of(talk, talk2))
                .asConstraint(new ConstraintInfo(ConferenceConstraintProperties.ROOM_CONFLICT,
                        ConferenceConstraintProperties.ROOM_CONFLICT,
                        "Two talks must not share the same room at overlapping times.",
                        ConferenceScheduleConstraintGroup.CONFLICT_AVOIDANCE));
    }

    // A talk must not be placed in a timeslot during which one of its speakers is unavailable.
    Constraint speakerUnavailableTimeslot(ConstraintFactory factory) {
        return factory.forEachIncludingUnassigned(Talk.class)
                .filter(talk -> talk.getTimeslot() != null)
                .join(Speaker.class,
                        containing(Talk::getSpeakers, speaker -> speaker),
                        containedIn(Talk::getTimeslot, Speaker::unavailableTimeslots))
                .penalize(HardMediumSoftScore.ofHard(100), (talk, speaker) -> talk.getDurationInMinutes())
                .justifyWith(
                        (talk, speaker, score) -> SpeakerUnavailableAtTalkTimeslotJustification.of(talk, speaker))
                .asConstraint(new ConstraintInfo(ConferenceConstraintProperties.SPEAKER_UNAVAILABLE_TIMESLOT,
                        ConferenceConstraintProperties.SPEAKER_UNAVAILABLE_TIMESLOT,
                        "A talk must not be scheduled in a timeslot when one of its speakers is unavailable.",
                        ConferenceScheduleConstraintGroup.CONFLICT_AVOIDANCE));
    }

    // A speaker must not be assigned to two talks whose timeslots overlap.
    Constraint speakerConflict(ConstraintFactory factory) {
        return factory.forEachUniquePair(Talk.class,
                overlapping(t -> t.getTimeslot().getStartDateTime(), t -> t.getTimeslot().getEndDateTime()))
                .join(Speaker.class,
                        containing((talk1, talk2) -> talk1.getSpeakers(), speaker -> speaker),
                        containing((talk1, talk2) -> talk2.getSpeakers(), speaker -> speaker))
                .penalize(HardMediumSoftScore.ofHard(10), (talk1, talk2, speaker) -> talk2.overlappingDurationInMinutes(talk1))
                .justifyWith((talk, talk2, speaker, score) -> SpeakerAssignedToOverlappingTalksJustification.of(talk, talk2,
                        speaker))
                .asConstraint(new ConstraintInfo(ConferenceConstraintProperties.SPEAKER_CONFLICT,
                        ConferenceConstraintProperties.SPEAKER_CONFLICT,
                        "A speaker must not be assigned to two talks that overlap in time.",
                        ConferenceScheduleConstraintGroup.CONFLICT_AVOIDANCE));
    }

    // A talk must start only after all of its prerequisite talks have finished.
    Constraint talkPrerequisiteTalks(ConstraintFactory factory) {
        return factory.forEach(Talk.class)
                .join(Talk.class,
                        greaterThan(t -> t.getTimeslot().getEndDateTime(), t -> t.getTimeslot().getStartDateTime()),
                        containedIn(talk -> talk, Talk::getPrerequisiteTalks))
                .penalize(HardMediumSoftScore.ofHard(10), Talk::combinedDurationInMinutes)
                .justifyWith(
                        (talk, talk2, score) -> TalkScheduledBeforePrerequisiteTalkJustification.of(talk, talk2))
                .asConstraint(new ConstraintInfo(ConferenceConstraintProperties.TALK_PREREQUISITE_TALKS,
                        ConferenceConstraintProperties.TALK_PREREQUISITE_TALKS,
                        "A talk must be scheduled after all of its prerequisite talks have finished.",
                        ConferenceScheduleConstraintGroup.CONFLICT_AVOIDANCE));
    }

    // Talks that share a mutually-exclusive tag must not overlap in time.
    Constraint talkMutuallyExclusiveTalksTags(ConstraintFactory factory) {
        return factory.forEachUniquePair(Talk.class,
                overlapping(t -> t.getTimeslot().getStartDateTime(), t -> t.getTimeslot().getEndDateTime()),
                containingAnyOf(Talk::getMutuallyExclusiveTalksTags))
                .penalize(HardMediumSoftScore.ofHard(1),
                        (talk1, talk2) -> talk1.overlappingMutuallyExclusiveTalksTagCount(talk2) *
                                talk1.overlappingDurationInMinutes(talk2))
                .justifyWith((talk, talk2, score) -> MutuallyExclusiveTalksOverlappingJustification.of(talk, talk2))
                .asConstraint(new ConstraintInfo(ConferenceConstraintProperties.TALK_MUTUALLY_EXCLUSIVE_TALKS_TAGS,
                        ConferenceConstraintProperties.TALK_MUTUALLY_EXCLUSIVE_TALKS_TAGS,
                        "Talks sharing a mutually-exclusive tag must not be scheduled at overlapping times.",
                        ConferenceScheduleConstraintGroup.CONFLICT_AVOIDANCE));
    }

    // A speaker must get at least the configured minimum pause between two of their consecutive talks.
    Constraint consecutiveTalksPause(ConstraintFactory factory) {
        return factory.forEachUniquePair(Talk.class, containingAnyOf(Talk::getSpeakers))
                .ifExists(ConferenceConstraintProperties.class,
                        filtering((talk1, talk2, config) -> !talk1.getTimeslot().pauseExists(talk2.getTimeslot(),
                                config.getMinimumConsecutiveTalksPauseInMinutes())))
                .penalize(HardMediumSoftScore.ofHard(1), Talk::combinedDurationInMinutes)
                .justifyWith(
                        (talk, talk2, score) -> SpeakerConsecutiveTalksPauseTooShortJustification.of(talk, talk2))
                .asConstraint(new ConstraintInfo(ConferenceConstraintProperties.CONSECUTIVE_TALKS_PAUSE,
                        ConferenceConstraintProperties.CONSECUTIVE_TALKS_PAUSE,
                        "A speaker must get the minimum pause between two of their consecutive talks.",
                        ConferenceScheduleConstraintGroup.CONFLICT_AVOIDANCE));
    }

    // A talk needing crowd control must be paired with exactly one other crowd-control talk in the same timeslot.
    Constraint crowdControl(ConstraintFactory factory) {
        return factory.forEach(Talk.class)
                .filter(talk -> talk.getCrowdControlRisk() > 0)
                .join(factory.forEach(Talk.class)
                        .filter(talk -> talk.getCrowdControlRisk() > 0),
                        equal(Talk::getTimeslot))
                .filter((talk1, talk2) -> !Objects.equals(talk1, talk2))
                .groupBy((talk1, talk2) -> talk1, countBi())
                .filter((talk, count) -> count != 1)
                .penalize(HardMediumSoftScore.ofHard(1), (talk, count) -> talk.getDurationInMinutes())
                .justifyWith((talk, count, score) -> CrowdControlTalkNotPairedJustification.of(talk, count))
                .asConstraint(new ConstraintInfo(ConferenceConstraintProperties.CROWD_CONTROL,
                        ConferenceConstraintProperties.CROWD_CONTROL,
                        "A talk that needs crowd control must be paired with exactly one other such talk in the same timeslot.",
                        ConferenceScheduleConstraintGroup.CONFLICT_AVOIDANCE));
    }

    // The talk's timeslot must carry every timeslot tag required by its speakers.
    Constraint speakerRequiredTimeslotTags(ConstraintFactory factory) {
        return factory.forEach(Talk.class)
                .expand(Talk::missingSpeakerRequiredTimeslotTagCount)
                .filter((talk, missingTagCount) -> missingTagCount > 0)
                .penalize(HardMediumSoftScore.ofHard(1),
                        (talk, missingTagCount) -> missingTagCount * talk.getDurationInMinutes())
                .justifyWith(
                        (talk, missingTagCount, score) -> MissingRequiredTimeslotTagsForSpeakersJustification.of(talk))
                .asConstraint(new ConstraintInfo(ConferenceConstraintProperties.SPEAKER_REQUIRED_TIMESLOT_TAGS,
                        ConferenceConstraintProperties.SPEAKER_REQUIRED_TIMESLOT_TAGS,
                        "The talk's timeslot must carry every timeslot tag required by its speakers.",
                        ConferenceScheduleConstraintGroup.TAG_REQUIREMENTS));
    }

    // The talk's timeslot must not carry any timeslot tag prohibited by its speakers.
    Constraint speakerProhibitedTimeslotTags(ConstraintFactory factory) {
        return factory.forEach(Talk.class)
                .expand(Talk::prevailingSpeakerProhibitedTimeslotTagCount)
                .filter((talk, prohibitedTagCount) -> prohibitedTagCount > 0)
                .penalize(HardMediumSoftScore.ofHard(1),
                        (talk, prohibitedTagCount) -> prohibitedTagCount * talk.getDurationInMinutes())
                .justifyWith(
                        (talk, prohibitedTagCount, score) -> ProhibitedTimeslotTagsForSpeakersJustification.of(talk))
                .asConstraint(new ConstraintInfo(ConferenceConstraintProperties.SPEAKER_PROHIBITED_TIMESLOT_TAGS,
                        ConferenceConstraintProperties.SPEAKER_PROHIBITED_TIMESLOT_TAGS,
                        "The talk's timeslot must not carry any timeslot tag prohibited by its speakers.",
                        ConferenceScheduleConstraintGroup.TAG_REQUIREMENTS));
    }

    // The talk's timeslot must carry every timeslot tag the talk itself requires.
    Constraint talkRequiredTimeslotTags(ConstraintFactory factory) {
        return factory.forEach(Talk.class)
                .expand(Talk::missingRequiredTimeslotTagCount)
                .filter((talk, missingTagCount) -> missingTagCount > 0)
                .penalize(HardMediumSoftScore.ofHard(1),
                        (talk, missingTagCount) -> missingTagCount * talk.getDurationInMinutes())
                .justifyWith((talk, missingTagCount, score) -> MissingRequiredTimeslotTagsForTalkJustification.of(talk))
                .asConstraint(new ConstraintInfo(ConferenceConstraintProperties.TALK_REQUIRED_TIMESLOT_TAGS,
                        ConferenceConstraintProperties.TALK_REQUIRED_TIMESLOT_TAGS,
                        "The talk's timeslot must carry every timeslot tag the talk requires.",
                        ConferenceScheduleConstraintGroup.TAG_REQUIREMENTS));
    }

    // The talk's timeslot must not carry any timeslot tag the talk itself prohibits.
    Constraint talkProhibitedTimeslotTags(ConstraintFactory factory) {
        return factory.forEach(Talk.class)
                .expand(Talk::prevailingProhibitedTimeslotTagCount)
                .filter((talk, prohibitedTagCount) -> prohibitedTagCount > 0)
                .penalize(HardMediumSoftScore.ofHard(1),
                        (talk, prohibitedTagCount) -> prohibitedTagCount * talk.getDurationInMinutes())
                .justifyWith((talk, prohibitedTagCount, score) -> ProhibitedTimeslotTagsForTalkJustification.of(talk))
                .asConstraint(new ConstraintInfo(ConferenceConstraintProperties.TALK_PROHIBITED_TIMESLOT_TAGS,
                        ConferenceConstraintProperties.TALK_PROHIBITED_TIMESLOT_TAGS,
                        "The talk's timeslot must not carry any timeslot tag the talk prohibits.",
                        ConferenceScheduleConstraintGroup.TAG_REQUIREMENTS));
    }

    // The talk's room must carry every room tag required by its speakers.
    Constraint speakerRequiredRoomTags(ConstraintFactory factory) {
        return factory.forEach(Talk.class)
                .expand(Talk::missingSpeakerRequiredRoomTagCount)
                .filter((talk, missingTagCount) -> missingTagCount > 0)
                .penalize(HardMediumSoftScore.ofHard(1),
                        (talk, missingTagCount) -> missingTagCount * talk.getDurationInMinutes())
                .justifyWith((talk, missingTagCount, score) -> MissingRequiredRoomTagsForSpeakersJustification.of(talk))
                .asConstraint(new ConstraintInfo(ConferenceConstraintProperties.SPEAKER_REQUIRED_ROOM_TAGS,
                        ConferenceConstraintProperties.SPEAKER_REQUIRED_ROOM_TAGS,
                        "The talk's room must carry every room tag required by its speakers.",
                        ConferenceScheduleConstraintGroup.TAG_REQUIREMENTS));
    }

    // The talk's room must not carry any room tag prohibited by its speakers.
    Constraint speakerProhibitedRoomTags(ConstraintFactory factory) {
        return factory.forEach(Talk.class)
                .expand(Talk::prevailingSpeakerProhibitedRoomTagCount)
                .filter((talk, prohibitedTagCount) -> prohibitedTagCount > 0)
                .penalize(HardMediumSoftScore.ofHard(1),
                        (talk, prohibitedTagCount) -> prohibitedTagCount * talk.getDurationInMinutes())
                .justifyWith((talk, prohibitedTagCount, score) -> ProhibitedRoomTagsForSpeakersJustification.of(talk))
                .asConstraint(new ConstraintInfo(ConferenceConstraintProperties.SPEAKER_PROHIBITED_ROOM_TAGS,
                        ConferenceConstraintProperties.SPEAKER_PROHIBITED_ROOM_TAGS,
                        "The talk's room must not carry any room tag prohibited by its speakers.",
                        ConferenceScheduleConstraintGroup.TAG_REQUIREMENTS));
    }

    // The talk's room must carry every room tag the talk itself requires.
    Constraint talkRequiredRoomTags(ConstraintFactory factory) {
        return factory.forEach(Talk.class)
                .expand(Talk::missingRequiredRoomTagCount)
                .filter((talk, missingTagCount) -> missingTagCount > 0)
                .penalize(HardMediumSoftScore.ofHard(1),
                        (talk, missingTagCount) -> missingTagCount * talk.getDurationInMinutes())
                .justifyWith((talk, missingTagCount, score) -> MissingRequiredRoomTagsForTalkJustification.of(talk))
                .asConstraint(new ConstraintInfo(ConferenceConstraintProperties.TALK_REQUIRED_ROOM_TAGS,
                        ConferenceConstraintProperties.TALK_REQUIRED_ROOM_TAGS,
                        "The talk's room must carry every room tag the talk requires.",
                        ConferenceScheduleConstraintGroup.TAG_REQUIREMENTS));
    }

    // The talk's room must not carry any room tag the talk itself prohibits.
    Constraint talkProhibitedRoomTags(ConstraintFactory factory) {
        return factory.forEach(Talk.class)
                .expand(Talk::prevailingProhibitedRoomTagCount)
                .filter((talk, prohibitedTagCount) -> prohibitedTagCount > 0)
                .penalize(HardMediumSoftScore.ofHard(1),
                        (talk, prohibitedTagCount) -> prohibitedTagCount * talk.getDurationInMinutes())
                .justifyWith((talk, prohibitedTagCount, score) -> ProhibitedRoomTagsForTalkJustification.of(talk))
                .asConstraint(new ConstraintInfo(ConferenceConstraintProperties.TALK_PROHIBITED_ROOM_TAGS,
                        ConferenceConstraintProperties.TALK_PROHIBITED_ROOM_TAGS,
                        "The talk's room must not carry any room tag the talk prohibits.",
                        ConferenceScheduleConstraintGroup.TAG_REQUIREMENTS));
    }

    // ************************************************************************
    // Soft constraints
    // ************************************************************************

    // Talks that share a theme track should not be scheduled at overlapping times.
    Constraint themeTrackConflict(ConstraintFactory factory) {
        return factory.forEachUniquePair(Talk.class,
                overlapping(t -> t.getTimeslot().getStartDateTime(), t -> t.getTimeslot().getEndDateTime()),
                containingAnyOf(Talk::getThemeTrackTags))
                .penalize(HardMediumSoftScore.ofSoft(10), (talk1, talk2) -> talk1.overlappingThemeTrackCount(talk2) *
                        talk1.overlappingDurationInMinutes(talk2))
                .justifyWith(
                        (talk, talk2, score) -> TalksWithSameThemeTrackOverlappingJustification.of(talk, talk2))
                .asConstraint(new ConstraintInfo(ConferenceConstraintProperties.THEME_TRACK_CONFLICT,
                        ConferenceConstraintProperties.THEME_TRACK_CONFLICT,
                        "Talks sharing a theme track should not overlap in time.",
                        ConferenceScheduleConstraintGroup.PROGRAM_QUALITY));
    }

    // Talks sharing a theme track on the same day should stay in the same room.
    Constraint themeTrackRoomStability(ConstraintFactory factory) {
        return factory.forEachUniquePair(Talk.class,
                equal(talk -> talk.getTimeslot().getStartDateTime().toLocalDate()),
                containingAnyOf(Talk::getThemeTrackTags))
                .filter((talk1, talk2) -> !talk1.getRoom().equals(talk2.getRoom()))
                .penalize(HardMediumSoftScore.ofSoft(10), (talk1, talk2) -> talk1.overlappingThemeTrackCount(talk2) *
                        talk1.combinedDurationInMinutes(talk2))
                .justifyWith(
                        (talk, talk2, score) -> TalksWithSameThemeTrackInDifferentRoomsJustification.of(talk, talk2))
                .asConstraint(new ConstraintInfo(ConferenceConstraintProperties.THEME_TRACK_ROOM_STABILITY,
                        ConferenceConstraintProperties.THEME_TRACK_ROOM_STABILITY,
                        "Talks sharing a theme track on the same day should stay in the same room.",
                        ConferenceScheduleConstraintGroup.PROGRAM_QUALITY));
    }

    // Talks that share a sector should not be scheduled at overlapping times.
    Constraint sectorConflict(ConstraintFactory factory) {
        return factory.forEachUniquePair(Talk.class,
                overlapping(t -> t.getTimeslot().getStartDateTime(), t -> t.getTimeslot().getEndDateTime()),
                containingAnyOf(Talk::getSectorTags))
                .penalize(HardMediumSoftScore.ofSoft(10), (talk1, talk2) -> talk1.overlappingSectorCount(talk2)
                        * talk1.overlappingDurationInMinutes(talk2))
                .justifyWith((talk, talk2, score) -> TalksWithSameSectorOverlappingJustification.of(talk, talk2))
                .asConstraint(new ConstraintInfo(ConferenceConstraintProperties.SECTOR_CONFLICT,
                        ConferenceConstraintProperties.SECTOR_CONFLICT,
                        "Talks sharing a sector should not overlap in time.",
                        ConferenceScheduleConstraintGroup.PROGRAM_QUALITY));
    }

    // Reward talks in the same timeslot for offering several audience types (a diverse slot).
    Constraint audienceTypeDiversity(ConstraintFactory factory) {
        return factory.forEachUniquePair(Talk.class,
                equal(Talk::getTimeslot),
                containingAnyOf(Talk::getAudienceTypes))
                .reward(HardMediumSoftScore.ofSoft(1), (talk1, talk2) -> talk1.overlappingAudienceTypeCount(talk2)
                        * talk1.getTimeslot().getDurationInMinutes())
                .justifyWith((talk, talk2, score) -> TalksWithSameAudienceTypeInSameTimeslotJustification.of(talk, talk2))
                .asConstraint(new ConstraintInfo(ConferenceConstraintProperties.AUDIENCE_TYPE_DIVERSITY,
                        ConferenceConstraintProperties.AUDIENCE_TYPE_DIVERSITY,
                        "Talks in the same timeslot are rewarded for sharing an audience type.",
                        ConferenceScheduleConstraintGroup.PROGRAM_QUALITY));
    }

    // Overlapping talks that share both a theme track and an audience type are penalized.
    Constraint audienceTypeThemeTrackConflict(ConstraintFactory factory) {
        return factory.forEachUniquePair(Talk.class,
                overlapping(t -> t.getTimeslot().getStartDateTime(), t -> t.getTimeslot().getEndDateTime()),
                containingAnyOf(Talk::getThemeTrackTags),
                containingAnyOf(Talk::getAudienceTypes))
                .penalize(HardMediumSoftScore.ofSoft(1), (talk1, talk2) -> talk1.overlappingThemeTrackCount(talk2)
                        * talk1.overlappingAudienceTypeCount(talk2)
                        * talk1.overlappingDurationInMinutes(talk2))
                .justifyWith((talk, talk2, score) -> TalksWithSameThemeTrackAndAudienceTypeOverlappingJustification.of(talk,
                        talk2))
                .asConstraint(new ConstraintInfo(ConferenceConstraintProperties.AUDIENCE_TYPE_THEME_TRACK_CONFLICT,
                        ConferenceConstraintProperties.AUDIENCE_TYPE_THEME_TRACK_CONFLICT,
                        "Overlapping talks that share both a theme track and an audience type should be avoided.",
                        ConferenceScheduleConstraintGroup.PROGRAM_QUALITY));
    }

    // Reward talks in the same timeslot for targeting different audience levels.
    Constraint audienceLevelDiversity(ConstraintFactory factory) {
        return factory.forEachUniquePair(Talk.class,
                equal(Talk::getTimeslot))
                .filter((talk1, talk2) -> talk1.getAudienceLevel() != talk2.getAudienceLevel())
                .reward(HardMediumSoftScore.ofSoft(1), (talk1, talk2) -> talk1.getTimeslot().getDurationInMinutes())
                .justifyWith((talk, talk2, score) -> TalksWithDifferentAudienceLevelInSameTimeslotJustification.of(talk,
                        talk2))
                .asConstraint(new ConstraintInfo(ConferenceConstraintProperties.AUDIENCE_LEVEL_DIVERSITY,
                        ConferenceConstraintProperties.AUDIENCE_LEVEL_DIVERSITY,
                        "Talks in the same timeslot are rewarded for having different audience levels.",
                        ConferenceScheduleConstraintGroup.PROGRAM_QUALITY));
    }

    // A talk on shared content should not precede a lower-audience-level talk on that content (keep the level flow rising).
    Constraint contentAudienceLevelFlowViolation(ConstraintFactory factory) {
        return factory.forEach(Talk.class)
                .join(Talk.class,
                        lessThan(Talk::getAudienceLevel),
                        greaterThan(talk1 -> talk1.getTimeslot().getEndDateTime(),
                                talk2 -> talk2.getTimeslot().getStartDateTime()),
                        containingAnyOf(Talk::getContentTags))
                .penalize(HardMediumSoftScore.ofSoft(10), (talk1, talk2) -> talk1.overlappingContentCount(talk2)
                        * talk1.combinedDurationInMinutes(talk2))
                .justifyWith((talk, talk2, score) -> SharedContentAudienceLevelFlowViolationJustification.of(talk, talk2))
                .asConstraint(new ConstraintInfo(ConferenceConstraintProperties.CONTENT_AUDIENCE_LEVEL_FLOW_VIOLATION,
                        ConferenceConstraintProperties.CONTENT_AUDIENCE_LEVEL_FLOW_VIOLATION,
                        "A talk on shared content should not be scheduled before a lower-audience-level talk on that content.",
                        ConferenceScheduleConstraintGroup.PROGRAM_QUALITY));
    }

    // Talks that share content should not be scheduled at overlapping times.
    Constraint contentConflict(ConstraintFactory factory) {
        return factory.forEachUniquePair(Talk.class,
                overlapping(t -> t.getTimeslot().getStartDateTime(), t -> t.getTimeslot().getEndDateTime()),
                containingAnyOf(Talk::getContentTags))
                .penalize(HardMediumSoftScore.ofSoft(100), (talk1, talk2) -> talk1.overlappingContentCount(talk2)
                        * talk1.overlappingDurationInMinutes(talk2))
                .justifyWith(
                        (talk, talk2, score) -> TalksWithSameContentOverlappingJustification.of(talk, talk2))
                .asConstraint(new ConstraintInfo(ConferenceConstraintProperties.CONTENT_CONFLICT,
                        ConferenceConstraintProperties.CONTENT_CONFLICT,
                        "Talks sharing content should not overlap in time.",
                        ConferenceScheduleConstraintGroup.PROGRAM_QUALITY));
    }

    // Reward talks in the same timeslot for being given in different languages.
    Constraint languageDiversity(ConstraintFactory factory) {
        return factory.forEachUniquePair(Talk.class,
                equal(Talk::getTimeslot))
                .filter((talk1, talk2) -> !Objects.equals(talk1.getLanguage(), talk2.getLanguage()))
                .reward(HardMediumSoftScore.ofSoft(10), (talk1, talk2) -> talk1.getTimeslot().getDurationInMinutes())
                .justifyWith((talk, talk2, score) -> TalksWithSameLanguageInSameTimeslotJustification.of(talk, talk2))
                .asConstraint(new ConstraintInfo(ConferenceConstraintProperties.LANGUAGE_DIVERSITY,
                        ConferenceConstraintProperties.LANGUAGE_DIVERSITY,
                        "Talks in the same timeslot are rewarded for using different languages.",
                        ConferenceScheduleConstraintGroup.PROGRAM_QUALITY));
    }

    // Talks that share content or a theme track should be scheduled on the same day.
    Constraint sameDayTalks(ConstraintFactory factory) {
        return factory.forEachUniquePair(Talk.class)
                .filter((talk1, talk2) -> !talk1.getTimeslot().isOnSameDayAs(talk2.getTimeslot()) &&
                        (talk1.overlappingContentCount(talk2) > 0 || talk1.overlappingThemeTrackCount(talk2) > 0))
                .penalize(HardMediumSoftScore.ofSoft(10),
                        (talk1, talk2) -> (talk2.overlappingThemeTrackCount(talk1) + talk2.overlappingContentCount(talk1))
                                * talk1.combinedDurationInMinutes(talk2))
                .justifyWith((talk, talk2, score) -> RelatedTalksNotOnSameDayJustification.of(talk, talk2))
                .asConstraint(new ConstraintInfo(ConferenceConstraintProperties.SAME_DAY_TALKS,
                        ConferenceConstraintProperties.SAME_DAY_TALKS,
                        "Talks sharing content or a theme track should be scheduled on the same day.",
                        ConferenceScheduleConstraintGroup.PROGRAM_QUALITY));
    }

    // A more popular talk should not be placed in a smaller room than a less popular one.
    Constraint popularTalks(ConstraintFactory factory) {
        return factory.forEach(Talk.class)
                .join(Talk.class,
                        lessThan(Talk::getFavoriteCount),
                        greaterThan(talk -> talk.getRoom().capacity()))
                .penalize(HardMediumSoftScore.ofSoft(10), Talk::combinedDurationInMinutes)
                .justifyWith((talk, talk2, score) -> PopularTalkInSmallerRoomJustification.of(talk, talk2))
                .asConstraint(new ConstraintInfo(ConferenceConstraintProperties.POPULAR_TALKS,
                        ConferenceConstraintProperties.POPULAR_TALKS,
                        "A more popular talk should not be placed in a smaller room than a less popular one.",
                        ConferenceScheduleConstraintGroup.PROGRAM_QUALITY));
    }

    // The talk's timeslot should carry the timeslot tags preferred by its speakers.
    Constraint speakerPreferredTimeslotTags(ConstraintFactory factory) {
        return factory.forEach(Talk.class)
                .expand(Talk::missingSpeakerPreferredTimeslotTagCount)
                .filter((talk, missingTagCount) -> missingTagCount > 0)
                .penalize(HardMediumSoftScore.ofSoft(20),
                        (talk, missingTagCount) -> missingTagCount * talk.getDurationInMinutes())
                .justifyWith(
                        (talk, missingTagCount, score) -> MissingPreferredTimeslotTagsForSpeakersJustification.of(talk))
                .asConstraint(new ConstraintInfo(ConferenceConstraintProperties.SPEAKER_PREFERRED_TIMESLOT_TAGS,
                        ConferenceConstraintProperties.SPEAKER_PREFERRED_TIMESLOT_TAGS,
                        "The talk's timeslot should carry the timeslot tags preferred by its speakers.",
                        ConferenceScheduleConstraintGroup.TAG_PREFERENCES));
    }

    // The talk's timeslot should avoid the timeslot tags undesired by its speakers.
    Constraint speakerUndesiredTimeslotTags(ConstraintFactory factory) {
        return factory.forEach(Talk.class)
                .expand(Talk::prevailingSpeakerUndesiredTimeslotTagCount)
                .filter((talk, undesiredTagCount) -> undesiredTagCount > 0)
                .penalize(HardMediumSoftScore.ofSoft(20),
                        (talk, undesiredTagCount) -> undesiredTagCount * talk.getDurationInMinutes())
                .justifyWith(
                        (talk, undesiredTagCount, score) -> UndesiredTimeslotTagsForSpeakersJustification.of(talk))
                .asConstraint(new ConstraintInfo(ConferenceConstraintProperties.SPEAKER_UNDESIRED_TIMESLOT_TAGS,
                        ConferenceConstraintProperties.SPEAKER_UNDESIRED_TIMESLOT_TAGS,
                        "The talk's timeslot should avoid the timeslot tags undesired by its speakers.",
                        ConferenceScheduleConstraintGroup.TAG_PREFERENCES));
    }

    // The talk's timeslot should carry the timeslot tags the talk itself prefers.
    Constraint talkPreferredTimeslotTags(ConstraintFactory factory) {
        return factory.forEach(Talk.class)
                .expand(Talk::missingPreferredTimeslotTagCount)
                .filter((talk, missingTagCount) -> missingTagCount > 0)
                .penalize(HardMediumSoftScore.ofSoft(20),
                        (talk, missingTagCount) -> missingTagCount * talk.getDurationInMinutes())
                .justifyWith((talk, missingTagCount, score) -> MissingPreferredTimeslotTagsForTalkJustification.of(talk))
                .asConstraint(new ConstraintInfo(ConferenceConstraintProperties.TALK_PREFERRED_TIMESLOT_TAGS,
                        ConferenceConstraintProperties.TALK_PREFERRED_TIMESLOT_TAGS,
                        "The talk's timeslot should carry the timeslot tags the talk prefers.",
                        ConferenceScheduleConstraintGroup.TAG_PREFERENCES));
    }

    // The talk's timeslot should avoid the timeslot tags the talk itself finds undesired.
    Constraint talkUndesiredTimeslotTags(ConstraintFactory factory) {
        return factory.forEach(Talk.class)
                .expand(Talk::prevailingUndesiredTimeslotTagCount)
                .filter((talk, undesiredTagCount) -> undesiredTagCount > 0)
                .penalize(HardMediumSoftScore.ofSoft(20),
                        (talk, undesiredTagCount) -> undesiredTagCount * talk.getDurationInMinutes())
                .justifyWith((talk, undesiredTagCount, score) -> UndesiredTimeslotTagsForTalkJustification.of(talk))
                .asConstraint(new ConstraintInfo(ConferenceConstraintProperties.TALK_UNDESIRED_TIMESLOT_TAGS,
                        ConferenceConstraintProperties.TALK_UNDESIRED_TIMESLOT_TAGS,
                        "The talk's timeslot should avoid the timeslot tags the talk finds undesired.",
                        ConferenceScheduleConstraintGroup.TAG_PREFERENCES));
    }

    // The talk's room should carry the room tags preferred by its speakers.
    Constraint speakerPreferredRoomTags(ConstraintFactory factory) {
        return factory.forEach(Talk.class)
                .expand(Talk::missingSpeakerPreferredRoomTagCount)
                .filter((talk, missingTagCount) -> missingTagCount > 0)
                .penalize(HardMediumSoftScore.ofSoft(20),
                        (talk, missingTagCount) -> missingTagCount * talk.getDurationInMinutes())
                .justifyWith((talk, missingTagCount, score) -> MissingPreferredRoomTagsForSpeakersJustification.of(talk))
                .asConstraint(new ConstraintInfo(ConferenceConstraintProperties.SPEAKER_PREFERRED_ROOM_TAGS,
                        ConferenceConstraintProperties.SPEAKER_PREFERRED_ROOM_TAGS,
                        "The talk's room should carry the room tags preferred by its speakers.",
                        ConferenceScheduleConstraintGroup.TAG_PREFERENCES));
    }

    // The talk's room should avoid the room tags undesired by its speakers.
    Constraint speakerUndesiredRoomTags(ConstraintFactory factory) {
        return factory.forEach(Talk.class)
                .expand(Talk::prevailingSpeakerUndesiredRoomTagCount)
                .filter((talk, undesiredTagCount) -> undesiredTagCount > 0)
                .penalize(HardMediumSoftScore.ofSoft(20),
                        (talk, undesiredTagCount) -> undesiredTagCount * talk.getDurationInMinutes())
                .justifyWith((talk, undesiredTagCount, score) -> UndesiredRoomTagsForSpeakersJustification.of(talk))
                .asConstraint(new ConstraintInfo(ConferenceConstraintProperties.SPEAKER_UNDESIRED_ROOM_TAGS,
                        ConferenceConstraintProperties.SPEAKER_UNDESIRED_ROOM_TAGS,
                        "The talk's room should avoid the room tags undesired by its speakers.",
                        ConferenceScheduleConstraintGroup.TAG_PREFERENCES));
    }

    // The talk's room should carry the room tags the talk itself prefers.
    Constraint talkPreferredRoomTags(ConstraintFactory factory) {
        return factory.forEach(Talk.class)
                .expand(Talk::missingPreferredRoomTagCount)
                .filter((talk, missingTagCount) -> missingTagCount > 0)
                .penalize(HardMediumSoftScore.ofSoft(20),
                        (talk, missingTagCount) -> missingTagCount * talk.getDurationInMinutes())
                .justifyWith((talk, missingTagCount, score) -> MissingPreferredRoomTagsForTalkJustification.of(talk))
                .asConstraint(new ConstraintInfo(ConferenceConstraintProperties.TALK_PREFERRED_ROOM_TAGS,
                        ConferenceConstraintProperties.TALK_PREFERRED_ROOM_TAGS,
                        "The talk's room should carry the room tags the talk prefers.",
                        ConferenceScheduleConstraintGroup.TAG_PREFERENCES));
    }

    // The talk's room should avoid the room tags the talk itself finds undesired.
    Constraint talkUndesiredRoomTags(ConstraintFactory factory) {
        return factory.forEach(Talk.class)
                .expand(Talk::prevailingUndesiredRoomTagCount)
                .filter((talk, undesiredTagCount) -> undesiredTagCount > 0)
                .penalize(HardMediumSoftScore.ofSoft(20),
                        (talk, undesiredTagCount) -> undesiredTagCount * talk.getDurationInMinutes())
                .justifyWith((talk, undesiredTagCount, score) -> UndesiredRoomTagsForTalkJustification.of(talk))
                .asConstraint(new ConstraintInfo(ConferenceConstraintProperties.TALK_UNDESIRED_ROOM_TAGS,
                        ConferenceConstraintProperties.TALK_UNDESIRED_ROOM_TAGS,
                        "The talk's room should avoid the room tags the talk finds undesired.",
                        ConferenceScheduleConstraintGroup.TAG_PREFERENCES));
    }

    // A speaker's talks should be packed into as few days as possible (minimize their makespan).
    Constraint speakerMakespan(ConstraintFactory factory) {
        return factory.forEach(Speaker.class)
                .join(Talk.class,
                        containedIn(speaker -> speaker, Talk::getSpeakers))
                .groupBy((speaker, talk) -> speaker,
                        compose(
                                min((Speaker speaker, Talk talk) -> talk,
                                        talk -> talk.getTimeslot().getStartDateTime()),
                                max((Speaker speaker, Talk talk) -> talk,
                                        talk -> talk.getTimeslot().getStartDateTime()),
                                (firstTalk, lastTalk) -> {
                                    LocalDate firstDate = firstTalk.getTimeslot().getStartDateTime().toLocalDate();
                                    LocalDate lastDate = lastTalk.getTimeslot().getStartDateTime().toLocalDate();
                                    return (int) Math.abs(ChronoUnit.DAYS.between(firstDate, lastDate));
                                }))
                .filter((speaker, daysBetweenTalks) -> daysBetweenTalks > 1)
                // Each such day counts for 8 hours.
                .penalize(HardMediumSoftScore.ofSoft(20), (speaker, daysBetweenTalks) -> (daysBetweenTalks - 1) * 8 * 60)
                .justifyWith(
                        (speaker, daysBetweenTalks, score) -> SpeakerMakespanTooLongJustification.of(speaker,
                                daysBetweenTalks))
                .asConstraint(new ConstraintInfo(ConferenceConstraintProperties.SPEAKER_MAKESPAN,
                        ConferenceConstraintProperties.SPEAKER_MAKESPAN,
                        "A speaker's talks should be packed into as few days as possible.",
                        ConferenceScheduleConstraintGroup.TAG_PREFERENCES));
    }

}
