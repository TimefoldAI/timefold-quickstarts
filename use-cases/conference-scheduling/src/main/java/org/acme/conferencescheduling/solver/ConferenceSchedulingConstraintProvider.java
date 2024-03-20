package org.acme.conferencescheduling.solver;

import static ai.timefold.solver.core.api.score.stream.ConstraintCollectors.compose;
import static ai.timefold.solver.core.api.score.stream.ConstraintCollectors.countBi;
import static ai.timefold.solver.core.api.score.stream.ConstraintCollectors.max;
import static ai.timefold.solver.core.api.score.stream.ConstraintCollectors.min;
import static ai.timefold.solver.core.api.score.stream.Joiners.equal;
import static ai.timefold.solver.core.api.score.stream.Joiners.filtering;
import static ai.timefold.solver.core.api.score.stream.Joiners.greaterThan;
import static ai.timefold.solver.core.api.score.stream.Joiners.lessThan;
import static ai.timefold.solver.core.api.score.stream.Joiners.overlapping;
import static java.util.stream.Collectors.joining;
import static org.acme.conferencescheduling.domain.ConferenceConstraintConfiguration.AUDIENCE_LEVEL_DIVERSITY;
import static org.acme.conferencescheduling.domain.ConferenceConstraintConfiguration.AUDIENCE_TYPE_DIVERSITY;
import static org.acme.conferencescheduling.domain.ConferenceConstraintConfiguration.AUDIENCE_TYPE_THEME_TRACK_CONFLICT;
import static org.acme.conferencescheduling.domain.ConferenceConstraintConfiguration.CONSECUTIVE_TALKS_PAUSE;
import static org.acme.conferencescheduling.domain.ConferenceConstraintConfiguration.CONTENT_AUDIENCE_LEVEL_FLOW_VIOLATION;
import static org.acme.conferencescheduling.domain.ConferenceConstraintConfiguration.CONTENT_CONFLICT;
import static org.acme.conferencescheduling.domain.ConferenceConstraintConfiguration.CROWD_CONTROL;
import static org.acme.conferencescheduling.domain.ConferenceConstraintConfiguration.LANGUAGE_DIVERSITY;
import static org.acme.conferencescheduling.domain.ConferenceConstraintConfiguration.POPULAR_TALKS;
import static org.acme.conferencescheduling.domain.ConferenceConstraintConfiguration.PUBLISHED_ROOM;
import static org.acme.conferencescheduling.domain.ConferenceConstraintConfiguration.PUBLISHED_TIMESLOT;
import static org.acme.conferencescheduling.domain.ConferenceConstraintConfiguration.ROOM_CONFLICT;
import static org.acme.conferencescheduling.domain.ConferenceConstraintConfiguration.ROOM_UNAVAILABLE_TIMESLOT;
import static org.acme.conferencescheduling.domain.ConferenceConstraintConfiguration.SAME_DAY_TALKS;
import static org.acme.conferencescheduling.domain.ConferenceConstraintConfiguration.SECTOR_CONFLICT;
import static org.acme.conferencescheduling.domain.ConferenceConstraintConfiguration.SPEAKER_CONFLICT;
import static org.acme.conferencescheduling.domain.ConferenceConstraintConfiguration.SPEAKER_MAKESPAN;
import static org.acme.conferencescheduling.domain.ConferenceConstraintConfiguration.SPEAKER_PREFERRED_ROOM_TAGS;
import static org.acme.conferencescheduling.domain.ConferenceConstraintConfiguration.SPEAKER_PREFERRED_TIMESLOT_TAGS;
import static org.acme.conferencescheduling.domain.ConferenceConstraintConfiguration.SPEAKER_PROHIBITED_ROOM_TAGS;
import static org.acme.conferencescheduling.domain.ConferenceConstraintConfiguration.SPEAKER_PROHIBITED_TIMESLOT_TAGS;
import static org.acme.conferencescheduling.domain.ConferenceConstraintConfiguration.SPEAKER_REQUIRED_ROOM_TAGS;
import static org.acme.conferencescheduling.domain.ConferenceConstraintConfiguration.SPEAKER_REQUIRED_TIMESLOT_TAGS;
import static org.acme.conferencescheduling.domain.ConferenceConstraintConfiguration.SPEAKER_UNAVAILABLE_TIMESLOT;
import static org.acme.conferencescheduling.domain.ConferenceConstraintConfiguration.SPEAKER_UNDESIRED_ROOM_TAGS;
import static org.acme.conferencescheduling.domain.ConferenceConstraintConfiguration.SPEAKER_UNDESIRED_TIMESLOT_TAGS;
import static org.acme.conferencescheduling.domain.ConferenceConstraintConfiguration.TALK_MUTUALLY_EXCLUSIVE_TALKS_TAGS;
import static org.acme.conferencescheduling.domain.ConferenceConstraintConfiguration.TALK_PREFERRED_ROOM_TAGS;
import static org.acme.conferencescheduling.domain.ConferenceConstraintConfiguration.TALK_PREFERRED_TIMESLOT_TAGS;
import static org.acme.conferencescheduling.domain.ConferenceConstraintConfiguration.TALK_PREREQUISITE_TALKS;
import static org.acme.conferencescheduling.domain.ConferenceConstraintConfiguration.TALK_PROHIBITED_ROOM_TAGS;
import static org.acme.conferencescheduling.domain.ConferenceConstraintConfiguration.TALK_PROHIBITED_TIMESLOT_TAGS;
import static org.acme.conferencescheduling.domain.ConferenceConstraintConfiguration.TALK_REQUIRED_ROOM_TAGS;
import static org.acme.conferencescheduling.domain.ConferenceConstraintConfiguration.TALK_REQUIRED_TIMESLOT_TAGS;
import static org.acme.conferencescheduling.domain.ConferenceConstraintConfiguration.TALK_UNDESIRED_ROOM_TAGS;
import static org.acme.conferencescheduling.domain.ConferenceConstraintConfiguration.TALK_UNDESIRED_TIMESLOT_TAGS;
import static org.acme.conferencescheduling.domain.ConferenceConstraintConfiguration.THEME_TRACK_CONFLICT;
import static org.acme.conferencescheduling.domain.ConferenceConstraintConfiguration.THEME_TRACK_ROOM_STABILITY;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;

import org.acme.conferencescheduling.domain.ConferenceConstraintConfiguration;
import org.acme.conferencescheduling.domain.Speaker;
import org.acme.conferencescheduling.domain.Talk;
import org.acme.conferencescheduling.solver.justifications.ConferenceSchedulingJustification;
import org.acme.conferencescheduling.solver.justifications.ConflictTalkJustification;
import org.acme.conferencescheduling.solver.justifications.DiversityTalkJustification;
import org.acme.conferencescheduling.solver.justifications.PreferredTagsJustification;
import org.acme.conferencescheduling.solver.justifications.ProhibitedTagsJustification;
import org.acme.conferencescheduling.solver.justifications.PublishedTimeslotJustification;
import org.acme.conferencescheduling.solver.justifications.RequiredTagsJustification;
import org.acme.conferencescheduling.solver.justifications.UnavailableTimeslotJustification;
import org.acme.conferencescheduling.solver.justifications.UndesiredTagsJustification;

/**
 * Provides the constraints for the conference scheduling problem.
 * <p>
 * Makes heavy use of CS expand() functionality to cache computation results,
 * except in cases where doing so less is efficient than recomputing the result.
 * That is the case in filtering joiners.
 * In this case, it is better to reduce the size of the joins even at the expense of duplicating some calculations.
 * In other words, time saved by caching those calculations is far outweighed by the time spent in unrestricted joins.
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
                // Medium constraints
                publishedTimeslot(factory),
                // Soft constraints
                publishedRoom(factory),
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

    Constraint roomUnavailableTimeslot(ConstraintFactory factory) {
        return factory.forEach(Talk.class)
                .filter(Talk::hasUnavailableRoom)
                .penalizeConfigurable(Talk::getDurationInMinutes)
                .justifyWith((talk, score) -> new UnavailableTimeslotJustification(talk))
                .asConstraint(ROOM_UNAVAILABLE_TIMESLOT);
    }

    Constraint roomConflict(ConstraintFactory factory) {
        return factory.forEachUniquePair(Talk.class,
                equal(Talk::getRoom),
                overlapping(t -> t.getTimeslot().getStartDateTime(), t -> t.getTimeslot().getEndDateTime()))
                .penalizeConfigurable(Talk::overlappingDurationInMinutes)
                .justifyWith((talk, talk2, score) -> new ConflictTalkJustification("room", talk,
                        List.of(talk.getRoom().getId()), talk2, List.of(talk2.getRoom().getId())))
                .asConstraint(ROOM_CONFLICT);
    }

    Constraint speakerUnavailableTimeslot(ConstraintFactory factory) {
        return factory.forEachIncludingUnassigned(Talk.class)
                .filter(talk -> talk.getTimeslot() != null)
                .join(Speaker.class,
                        filtering((talk, speaker) -> talk.hasSpeaker(speaker)
                                && speaker.getUnavailableTimeslots().contains(talk.getTimeslot())))
                .penalizeConfigurable((talk, speaker) -> talk.getDurationInMinutes())
                .justifyWith(
                        (talk, speaker, score) -> new UnavailableTimeslotJustification(talk, speaker))
                .asConstraint(SPEAKER_UNAVAILABLE_TIMESLOT);
    }

    Constraint speakerConflict(ConstraintFactory factory) {
        return factory.forEachUniquePair(Talk.class,
                overlapping(t -> t.getTimeslot().getStartDateTime(), t -> t.getTimeslot().getEndDateTime()))
                .join(Speaker.class,
                        filtering((talk1, talk2, speaker) -> talk1.hasSpeaker(speaker) && talk2.hasSpeaker(speaker)))
                .penalizeConfigurable((talk1, talk2, speaker) -> talk2.overlappingDurationInMinutes(talk1))
                .justifyWith((talk, talk2, speaker, score) -> new ConflictTalkJustification(talk, talk2, speaker))
                .asConstraint(SPEAKER_CONFLICT);
    }

    Constraint talkPrerequisiteTalks(ConstraintFactory factory) {
        return factory.forEach(Talk.class)
                .join(Talk.class,
                        greaterThan(t -> t.getTimeslot().getEndDateTime(), t -> t.getTimeslot().getStartDateTime()),
                        filtering((talk1, talk2) -> talk2.getPrerequisiteTalks().contains(talk1)))
                .penalizeConfigurable(Talk::combinedDurationInMinutes)
                .justifyWith(
                        (talk, talk2, score) -> new ConferenceSchedulingJustification(
                                "Talk %s must be scheduled after talk %s.".formatted(talk2.getCode(), talk.getCode())))
                .asConstraint(TALK_PREREQUISITE_TALKS);
    }

    Constraint talkMutuallyExclusiveTalksTags(ConstraintFactory factory) {
        return factory.forEachUniquePair(Talk.class,
                overlapping(t -> t.getTimeslot().getStartDateTime(), t -> t.getTimeslot().getEndDateTime()),
                filtering((talk1, talk2) -> talk2.overlappingMutuallyExclusiveTalksTagCount(talk1) > 0))
                .penalizeConfigurable((talk1, talk2) -> talk1.overlappingMutuallyExclusiveTalksTagCount(talk2) *
                        talk1.overlappingDurationInMinutes(talk2))
                .justifyWith((talk, talk2, score) -> new ConflictTalkJustification("mutually-exclusive-talks tags", talk,
                        talk.getMutuallyExclusiveTalksTags(), talk2, talk2.getMutuallyExclusiveTalksTags()))
                .asConstraint(TALK_MUTUALLY_EXCLUSIVE_TALKS_TAGS);
    }

    Constraint consecutiveTalksPause(ConstraintFactory factory) {
        return factory.forEachUniquePair(Talk.class,
                filtering((talk1, talk2) -> talk2.hasMutualSpeaker(talk1)))
                .ifExists(ConferenceConstraintConfiguration.class,
                        filtering((talk1, talk2, config) -> !talk1.getTimeslot().pauseExists(talk2.getTimeslot(),
                                config.getMinimumConsecutiveTalksPauseInMinutes())))
                .penalizeConfigurable(Talk::combinedDurationInMinutes)
                .justifyWith(
                        (talk, talk2, score) -> new ConferenceSchedulingJustification(
                                "Required minimum consecutive pauses between talks [%s, %s].".formatted(talk.getCode(),
                                        talk2.getCode())))
                .asConstraint(CONSECUTIVE_TALKS_PAUSE);
    }

    Constraint crowdControl(ConstraintFactory factory) {
        return factory.forEach(Talk.class)
                .filter(talk -> talk.getCrowdControlRisk() > 0)
                .join(factory.forEach(Talk.class)
                        .filter(talk -> talk.getCrowdControlRisk() > 0),
                        equal(Talk::getTimeslot))
                .filter((talk1, talk2) -> !Objects.equals(talk1, talk2))
                .groupBy((talk1, talk2) -> talk1, countBi())
                .filter((talk, count) -> count != 1)
                .penalizeConfigurable((talk, count) -> talk.getDurationInMinutes())
                .justifyWith((talk, integer, score) -> new ConferenceSchedulingJustification(
                        "Required crowd control for talk %s".formatted(talk.getCode())))
                .asConstraint(CROWD_CONTROL);
    }

    Constraint speakerRequiredTimeslotTags(ConstraintFactory factory) {
        return factory.forEach(Talk.class)
                .expand(Talk::missingSpeakerRequiredTimeslotTagCount)
                .filter((talk, missingTagCount) -> missingTagCount > 0)
                .penalizeConfigurable((talk, missingTagCount) -> missingTagCount * talk.getDurationInMinutes())
                .indictWith((talk, missingTagCount) -> Collections.singleton(talk))
                .justifyWith(
                        (talk, integer, score) -> new RequiredTagsJustification("timeslot", talk.getSpeakers(),
                                talk.getSpeakers().stream()
                                        .flatMap(s -> s.getRequiredTimeslotTags().stream())
                                        .distinct()
                                        .toList(),
                                talk.getTimeslot().getTags()))
                .asConstraint(SPEAKER_REQUIRED_TIMESLOT_TAGS);
    }

    Constraint speakerProhibitedTimeslotTags(ConstraintFactory factory) {
        return factory.forEach(Talk.class)
                .expand(Talk::prevailingSpeakerProhibitedTimeslotTagCount)
                .filter((talk, prohibitedTagCount) -> prohibitedTagCount > 0)
                .penalizeConfigurable((talk, prohibitedTagCount) -> prohibitedTagCount * talk.getDurationInMinutes())
                .indictWith((talk, prohibitedTagCount) -> Collections.singleton(talk))
                .justifyWith((talk, integer, score) -> new ProhibitedTagsJustification("timeslot", talk.getSpeakers(),
                        talk.getSpeakers().stream()
                                .flatMap(s -> s.getProhibitedTimeslotTags().stream())
                                .distinct()
                                .toList(),
                        talk.getTimeslot().getTags()))
                .asConstraint(SPEAKER_PROHIBITED_TIMESLOT_TAGS);
    }

    Constraint talkRequiredTimeslotTags(ConstraintFactory factory) {
        return factory.forEach(Talk.class)
                .expand(Talk::missingRequiredTimeslotTagCount)
                .filter((talk, missingTagCount) -> missingTagCount > 0)
                .penalizeConfigurable((talk, missingTagCount) -> missingTagCount * talk.getDurationInMinutes())
                .indictWith((talk, missingTagCount) -> Collections.singleton(talk))
                .justifyWith((talk, integer, score) -> new RequiredTagsJustification("timeslot", talk,
                        talk.getRequiredTimeslotTags(),
                        talk.getTimeslot().getTags()))
                .asConstraint(TALK_REQUIRED_TIMESLOT_TAGS);
    }

    Constraint talkProhibitedTimeslotTags(ConstraintFactory factory) {
        return factory.forEach(Talk.class)
                .expand(Talk::prevailingProhibitedTimeslotTagCount)
                .filter((talk, prohibitedTagCount) -> prohibitedTagCount > 0)
                .penalizeConfigurable((talk, prohibitedTagCount) -> prohibitedTagCount * talk.getDurationInMinutes())
                .indictWith((talk, prohibitedTagCount) -> Collections.singleton(talk))
                .justifyWith((talk, integer, score) -> new ProhibitedTagsJustification("timeslot", talk,
                        talk.getProhibitedTimeslotTags(),
                        talk.getTimeslot().getTags()))
                .asConstraint(TALK_PROHIBITED_TIMESLOT_TAGS);
    }

    Constraint speakerRequiredRoomTags(ConstraintFactory factory) {
        return factory.forEach(Talk.class)
                .expand(Talk::missingSpeakerRequiredRoomTagCount)
                .filter((talk, missingTagCount) -> missingTagCount > 0)
                .penalizeConfigurable((talk, missingTagCount) -> missingTagCount * talk.getDurationInMinutes())
                .indictWith((talk, missingTagCount) -> Collections.singleton(talk))
                .justifyWith((talk, integer, score) -> new RequiredTagsJustification("room", talk.getSpeakers(),
                        talk.getSpeakers().stream()
                                .flatMap(s -> s.getRequiredRoomTags().stream())
                                .distinct()
                                .toList(),
                        talk.getRoom().getTags()))
                .asConstraint(SPEAKER_REQUIRED_ROOM_TAGS);
    }

    Constraint speakerProhibitedRoomTags(ConstraintFactory factory) {
        return factory.forEach(Talk.class)
                .expand(Talk::prevailingSpeakerProhibitedRoomTagCount)
                .filter((talk, prohibitedTagCount) -> prohibitedTagCount > 0)
                .penalizeConfigurable((talk, prohibitedTagCount) -> prohibitedTagCount * talk.getDurationInMinutes())
                .indictWith((talk, prohibitedTagCount) -> Collections.singleton(talk))
                .justifyWith((talk, integer, score) -> new ProhibitedTagsJustification("room", talk.getSpeakers(),
                        talk.getSpeakers().stream()
                                .flatMap(s -> s.getPreferredRoomTags().stream())
                                .distinct()
                                .toList(),
                        talk.getRoom().getTags()))
                .asConstraint(SPEAKER_PROHIBITED_ROOM_TAGS);
    }

    Constraint talkRequiredRoomTags(ConstraintFactory factory) {
        return factory.forEach(Talk.class)
                .expand(Talk::missingRequiredRoomTagCount)
                .filter((talk, missingTagCount) -> missingTagCount > 0)
                .penalizeConfigurable((talk, missingTagCount) -> missingTagCount * talk.getDurationInMinutes())
                .indictWith((talk, missingTagCount) -> Collections.singleton(talk))
                .justifyWith((talk, integer, score) -> new RequiredTagsJustification("room", talk,
                        talk.getRequiredRoomTags(),
                        talk.getRoom().getTags()))
                .asConstraint(TALK_REQUIRED_ROOM_TAGS);
    }

    Constraint talkProhibitedRoomTags(ConstraintFactory factory) {
        return factory.forEach(Talk.class)
                .expand(Talk::prevailingProhibitedRoomTagCount)
                .filter((talk, prohibitedTagCount) -> prohibitedTagCount > 0)
                .penalizeConfigurable((talk, prohibitedTagCount) -> prohibitedTagCount * talk.getDurationInMinutes())
                .indictWith((talk, prohibitedTagCount) -> Collections.singleton(talk))
                .justifyWith((talk, integer, score) -> new ProhibitedTagsJustification("room", talk,
                        talk.getProhibitedRoomTags(),
                        talk.getRoom().getTags()))
                .asConstraint(TALK_PROHIBITED_ROOM_TAGS);
    }

    // ************************************************************************
    // Medium constraints
    // ************************************************************************

    Constraint publishedTimeslot(ConstraintFactory factory) {
        return factory.forEach(Talk.class)
                .filter(talk -> talk.getPublishedTimeslot() != null
                        && !talk.getTimeslot().equals(talk.getPublishedTimeslot()))
                .penalizeConfigurable()
                .justifyWith((talk, score) -> new PublishedTimeslotJustification(talk, true))
                .asConstraint(PUBLISHED_TIMESLOT);
    }

    // ************************************************************************
    // Soft constraints
    // ************************************************************************

    Constraint publishedRoom(ConstraintFactory factory) {
        return factory.forEach(Talk.class)
                .filter(talk -> talk.getPublishedRoom() != null && !talk.getRoom().equals(talk.getPublishedRoom()))
                .penalizeConfigurable()
                .justifyWith((talk, score) -> new PublishedTimeslotJustification(talk, false))
                .asConstraint(PUBLISHED_ROOM);
    }

    Constraint themeTrackConflict(ConstraintFactory factory) {
        return factory.forEachUniquePair(Talk.class,
                overlapping(t -> t.getTimeslot().getStartDateTime(), t -> t.getTimeslot().getEndDateTime()),
                filtering((talk1, talk2) -> talk2.overlappingThemeTrackCount(talk1) > 0))
                .penalizeConfigurable((talk1, talk2) -> talk1.overlappingThemeTrackCount(talk2) *
                        talk1.overlappingDurationInMinutes(talk2))
                .justifyWith(
                        (talk, talk2, score) -> new ConflictTalkJustification("theme", talk, talk.getThemeTrackTags(), talk2,
                                talk2.getThemeTrackTags()))
                .asConstraint(THEME_TRACK_CONFLICT);
    }

    Constraint themeTrackRoomStability(ConstraintFactory factory) {
        return factory.forEachUniquePair(Talk.class,
                equal(talk -> talk.getTimeslot().getStartDateTime().toLocalDate()),
                filtering((talk1, talk2) -> talk2.overlappingThemeTrackCount(talk1) > 0))
                .filter((talk1, talk2) -> !talk1.getRoom().equals(talk2.getRoom()))
                .penalizeConfigurable((talk1, talk2) -> talk1.overlappingThemeTrackCount(talk2) *
                        talk1.combinedDurationInMinutes(talk2))
                .justifyWith(
                        (talk, talk2, score) -> new ConferenceSchedulingJustification(
                                "Talks [%s, %s] with matching themes [%s] were scheduled for different rooms [%s, %s]."
                                        .formatted(talk.getCode(), talk2.getCode(),
                                                talk.getThemeTrackTags().stream()
                                                        .filter(t -> talk2.getThemeTrackTags().contains(t))
                                                        .collect(joining(", ")),
                                                talk.getRoom().getId(), talk2.getRoom().getId())))
                .asConstraint(THEME_TRACK_ROOM_STABILITY);
    }

    Constraint sectorConflict(ConstraintFactory factory) {
        return factory.forEachUniquePair(Talk.class,
                overlapping(t -> t.getTimeslot().getStartDateTime(), t -> t.getTimeslot().getEndDateTime()),
                filtering((talk1, talk2) -> talk2.overlappingSectorCount(talk1) > 0))
                .penalizeConfigurable((talk1, talk2) -> talk1.overlappingSectorCount(talk2)
                        * talk1.overlappingDurationInMinutes(talk2))
                .justifyWith((talk, talk2, score) -> new ConflictTalkJustification("sector", talk, talk.getSectorTags(), talk2,
                        talk2.getSectorTags()))
                .asConstraint(SECTOR_CONFLICT);
    }

    Constraint audienceTypeDiversity(ConstraintFactory factory) {
        return factory.forEachUniquePair(Talk.class,
                equal(Talk::getTimeslot),
                filtering((talk1, talk2) -> talk2.overlappingAudienceTypeCount(talk1) > 0))
                .rewardConfigurable((talk1, talk2) -> talk1.overlappingAudienceTypeCount(talk2)
                        * talk1.getTimeslot().getDurationInMinutes())
                .justifyWith((talk, talk2, score) -> new DiversityTalkJustification("audience types", talk,
                        talk.getAudienceTypes(), talk2, talk2.getAudienceTypes()))
                .asConstraint(AUDIENCE_TYPE_DIVERSITY);
    }

    Constraint audienceTypeThemeTrackConflict(ConstraintFactory factory) {
        return factory.forEachUniquePair(Talk.class,
                overlapping(t -> t.getTimeslot().getStartDateTime(), t -> t.getTimeslot().getEndDateTime()),
                filtering((talk1, talk2) -> talk2.overlappingThemeTrackCount(talk1) > 0),
                filtering((talk1, talk2) -> talk2.overlappingAudienceTypeCount(talk1) > 0))
                .penalizeConfigurable((talk1, talk2) -> talk1.overlappingThemeTrackCount(talk2)
                        * talk1.overlappingAudienceTypeCount(talk2)
                        * talk1.overlappingDurationInMinutes(talk2))
                .justifyWith((talk, talk2, score) -> new ConflictTalkJustification("theme", "audience type", talk,
                        talk.getThemeTrackTags(), talk.getAudienceTypes(), talk2, talk2.getThemeTrackTags(),
                        talk2.getAudienceTypes()))
                .asConstraint(AUDIENCE_TYPE_THEME_TRACK_CONFLICT);
    }

    Constraint audienceLevelDiversity(ConstraintFactory factory) {
        return factory.forEachUniquePair(Talk.class,
                equal(Talk::getTimeslot))
                .filter((talk1, talk2) -> talk1.getAudienceLevel() != talk2.getAudienceLevel())
                .rewardConfigurable((talk1, talk2) -> talk1.getTimeslot().getDurationInMinutes())
                .justifyWith((talk, talk2, score) -> new DiversityTalkJustification("audience level", talk,
                        String.valueOf(talk.getAudienceLevel()), talk2, String.valueOf(talk2.getAudienceLevel())))
                .asConstraint(AUDIENCE_LEVEL_DIVERSITY);
    }

    Constraint contentAudienceLevelFlowViolation(ConstraintFactory factory) {
        return factory.forEach(Talk.class)
                .join(Talk.class,
                        lessThan(Talk::getAudienceLevel),
                        greaterThan(talk1 -> talk1.getTimeslot().getEndDateTime(),
                                talk2 -> talk2.getTimeslot().getStartDateTime()),
                        filtering((talk1, talk2) -> talk2.overlappingContentCount(talk1) > 0))
                .penalizeConfigurable((talk1, talk2) -> talk1.overlappingContentCount(talk2)
                        * talk1.combinedDurationInMinutes(talk2))
                .justifyWith((talk, talk2, score) -> new ConferenceSchedulingJustification(
                        "Two talks [%s, %s] with the audience level [%s, %s] and matching content [%s] have a flow violation."
                                .formatted(talk.getCode(), talk2.getCode(), String.valueOf(talk.getAudienceLevel()),
                                        String.valueOf(talk2.getAudienceLevel()),
                                        talk.getContentTags().stream().filter(c -> talk2.getContentTags().contains(c))
                                                .collect(joining(", ")))))
                .asConstraint(CONTENT_AUDIENCE_LEVEL_FLOW_VIOLATION);
    }

    Constraint contentConflict(ConstraintFactory factory) {
        return factory.forEachUniquePair(Talk.class,
                overlapping(t -> t.getTimeslot().getStartDateTime(), t -> t.getTimeslot().getEndDateTime()),
                filtering((talk1, talk2) -> talk2.overlappingContentCount(talk1) > 0))
                .penalizeConfigurable((talk1, talk2) -> talk1.overlappingContentCount(talk2)
                        * talk1.overlappingDurationInMinutes(talk2))
                .justifyWith(
                        (talk, talk2, score) -> new ConflictTalkJustification("content", talk, talk.getContentTags(), talk2,
                                talk2.getContentTags()))
                .asConstraint(CONTENT_CONFLICT);
    }

    Constraint languageDiversity(ConstraintFactory factory) {
        return factory.forEachUniquePair(Talk.class,
                equal(Talk::getTimeslot))
                .filter((talk1, talk2) -> !talk1.getLanguage().equals(talk2.getLanguage()))
                .rewardConfigurable((talk1, talk2) -> talk1.getTimeslot().getDurationInMinutes())
                .justifyWith((talk, talk2, score) -> new DiversityTalkJustification("language", talk, talk.getLanguage(), talk2,
                        talk2.getLanguage()))
                .asConstraint(LANGUAGE_DIVERSITY);
    }

    Constraint sameDayTalks(ConstraintFactory factory) {
        return factory.forEachUniquePair(Talk.class)
                .filter((talk1, talk2) -> !talk1.getTimeslot().isOnSameDayAs(talk2.getTimeslot()) &&
                        (talk1.overlappingContentCount(talk2) > 0 || talk1.overlappingThemeTrackCount(talk2) > 0))
                .penalizeConfigurable(
                        (talk1, talk2) -> (talk2.overlappingThemeTrackCount(talk1) + talk2.overlappingContentCount(talk1))
                                * talk1.combinedDurationInMinutes(talk2))
                .justifyWith((talk, talk2, score) -> new ConferenceSchedulingJustification(
                        "Two talks [%s, %s] with matching content [%s] or matching theme [%s] not scheduled at the same day."
                                .formatted(
                                        talk.getCode(), talk2.getCode(),
                                        talk.getContentTags().stream().filter(c -> talk2.getContentTags().contains(c))
                                                .collect(joining(", ")),
                                        talk.getThemeTrackTags().stream().filter(t -> talk2.getThemeTrackTags().contains(t))
                                                .collect(joining(", ")))))
                .asConstraint(SAME_DAY_TALKS);
    }

    Constraint popularTalks(ConstraintFactory factory) {
        return factory.forEach(Talk.class)
                .join(Talk.class,
                        lessThan(Talk::getFavoriteCount),
                        greaterThan(talk -> talk.getRoom().getCapacity()))
                .penalizeConfigurable(Talk::combinedDurationInMinutes)
                .justifyWith((talk, talk2, score) -> new ConferenceSchedulingJustification(
                        "Two talks [%s, %s] with popularity [%d, %d] scheduled to rooms [%s, %s] with capacity [%d, %d]."
                                .formatted(talk.getCode(), talk2.getCode(), talk.getFavoriteCount(), talk2.getFavoriteCount(),
                                        talk.getRoom().getId(), talk2.getRoom().getId(), talk.getRoom().getCapacity(),
                                        talk2.getRoom().getCapacity())))
                .asConstraint(POPULAR_TALKS);
    }

    Constraint speakerPreferredTimeslotTags(ConstraintFactory factory) {
        return factory.forEach(Talk.class)
                .expand(Talk::missingSpeakerPreferredTimeslotTagCount)
                .filter((talk, missingTagCount) -> missingTagCount > 0)
                .penalizeConfigurable((talk, missingTagCount) -> missingTagCount * talk.getDurationInMinutes())
                .indictWith((talk, missingTagCount) -> Collections.singleton(talk))
                .justifyWith((talk, integer, score) -> new PreferredTagsJustification("timeslot", talk.getSpeakers(),
                        talk.getSpeakers().stream()
                                .flatMap(s -> s.getPreferredTimeslotTags().stream())
                                .distinct()
                                .toList(),
                        talk.getTimeslot().getTags()))
                .asConstraint(SPEAKER_PREFERRED_TIMESLOT_TAGS);
    }

    Constraint speakerUndesiredTimeslotTags(ConstraintFactory factory) {
        return factory.forEach(Talk.class)
                .expand(Talk::prevailingSpeakerUndesiredTimeslotTagCount)
                .filter((talk, undesiredTagCount) -> undesiredTagCount > 0)
                .penalizeConfigurable((talk, undesiredTagCount) -> undesiredTagCount * talk.getDurationInMinutes())
                .indictWith((talk, undesiredTagCount) -> Collections.singleton(talk))
                .justifyWith((talk, integer, score) -> new UndesiredTagsJustification("timeslot", talk.getSpeakers(),
                        talk.getSpeakers().stream()
                                .flatMap(s -> s.getUndesiredTimeslotTags().stream())
                                .distinct()
                                .toList(),
                        talk.getTimeslot().getTags()))
                .asConstraint(SPEAKER_UNDESIRED_TIMESLOT_TAGS);
    }

    Constraint talkPreferredTimeslotTags(ConstraintFactory factory) {
        return factory.forEach(Talk.class)
                .expand(Talk::missingPreferredTimeslotTagCount)
                .filter((talk, missingTagCount) -> missingTagCount > 0)
                .penalizeConfigurable((talk, missingTagCount) -> missingTagCount * talk.getDurationInMinutes())
                .indictWith((talk, missingTagCount) -> Collections.singleton(talk))
                .justifyWith((talk, integer, score) -> new PreferredTagsJustification("timeslot", talk,
                        talk.getPreferredTimeslotTags(),
                        talk.getTimeslot().getTags()))
                .asConstraint(TALK_PREFERRED_TIMESLOT_TAGS);
    }

    Constraint talkUndesiredTimeslotTags(ConstraintFactory factory) {
        return factory.forEach(Talk.class)
                .expand(Talk::prevailingUndesiredTimeslotTagCount)
                .filter((talk, undesiredTagCount) -> undesiredTagCount > 0)
                .penalizeConfigurable((talk, undesiredTagCount) -> undesiredTagCount * talk.getDurationInMinutes())
                .indictWith((talk, undesiredTagCount) -> Collections.singleton(talk))
                .justifyWith((talk, integer, score) -> new UndesiredTagsJustification("timeslot", talk,
                        talk.getPreferredTimeslotTags(),
                        talk.getTimeslot().getTags()))
                .asConstraint(TALK_UNDESIRED_TIMESLOT_TAGS);
    }

    Constraint speakerPreferredRoomTags(ConstraintFactory factory) {
        return factory.forEach(Talk.class)
                .expand(Talk::missingSpeakerPreferredRoomTagCount)
                .filter((talk, missingTagCount) -> missingTagCount > 0)
                .penalizeConfigurable((talk, missingTagCount) -> missingTagCount * talk.getDurationInMinutes())
                .indictWith((talk, missingTagCount) -> Collections.singleton(talk))
                .justifyWith((talk, integer, score) -> new PreferredTagsJustification("room", talk.getSpeakers(),
                        talk.getSpeakers().stream()
                                .flatMap(s -> s.getPreferredRoomTags().stream())
                                .distinct()
                                .toList(),
                        talk.getRoom().getTags()))
                .asConstraint(SPEAKER_PREFERRED_ROOM_TAGS);
    }

    Constraint speakerUndesiredRoomTags(ConstraintFactory factory) {
        return factory.forEach(Talk.class)
                .expand(Talk::prevailingSpeakerUndesiredRoomTagCount)
                .filter((talk, undesiredTagCount) -> undesiredTagCount > 0)
                .penalizeConfigurable((talk, undesiredTagCount) -> undesiredTagCount * talk.getDurationInMinutes())
                .indictWith((talk, undesiredTagCount) -> Collections.singleton(talk))
                .justifyWith((talk, integer, score) -> new UndesiredTagsJustification("room", talk.getSpeakers(),
                        talk.getSpeakers().stream()
                                .flatMap(s -> s.getUndesiredRoomTags().stream())
                                .distinct()
                                .toList(),
                        talk.getRoom().getTags()))
                .asConstraint(SPEAKER_UNDESIRED_ROOM_TAGS);
    }

    Constraint talkPreferredRoomTags(ConstraintFactory factory) {
        return factory.forEach(Talk.class)
                .expand(Talk::missingPreferredRoomTagCount)
                .filter((talk, missingTagCount) -> missingTagCount > 0)
                .penalizeConfigurable((talk, missingTagCount) -> missingTagCount * talk.getDurationInMinutes())
                .indictWith((talk, missingTagCount) -> Collections.singleton(talk))
                .justifyWith((talk, integer, score) -> new PreferredTagsJustification("room", talk,
                        talk.getPreferredRoomTags(),
                        talk.getRoom().getTags()))
                .asConstraint(TALK_PREFERRED_ROOM_TAGS);
    }

    Constraint talkUndesiredRoomTags(ConstraintFactory factory) {
        return factory.forEach(Talk.class)
                .expand(Talk::prevailingUndesiredRoomTagCount)
                .filter((talk, undesiredTagCount) -> undesiredTagCount > 0)
                .penalizeConfigurable((talk, undesiredTagCount) -> undesiredTagCount * talk.getDurationInMinutes())
                .indictWith((talk, undesiredTagCount) -> Collections.singleton(talk))
                .justifyWith((talk, integer, score) -> new UndesiredTagsJustification("room", talk,
                        talk.getUndesiredRoomTags(),
                        talk.getRoom().getTags()))
                .asConstraint(TALK_UNDESIRED_ROOM_TAGS);
    }

    Constraint speakerMakespan(ConstraintFactory factory) {
        return factory.forEach(Speaker.class)
                .join(Talk.class,
                        filtering((speaker, talk) -> talk.hasSpeaker(speaker)))
                .groupBy((speaker, talk) -> speaker,
                        compose(
                                min((Speaker speaker, Talk talk) -> talk, talk -> talk.getTimeslot().getStartDateTime()),
                                max((Speaker speaker, Talk talk) -> talk, talk -> talk.getTimeslot().getStartDateTime()),
                                (firstTalk, lastTalk) -> {
                                    LocalDate firstDate = firstTalk.getTimeslot().getStartDateTime().toLocalDate();
                                    LocalDate lastDate = lastTalk.getTimeslot().getStartDateTime().toLocalDate();
                                    return (int) Math.abs(ChronoUnit.DAYS.between(firstDate, lastDate));
                                }))
                .filter((speaker, daysBetweenTalks) -> daysBetweenTalks > 1)
                // Each such day counts for 8 hours.
                .penalizeConfigurable((speaker, daysBetweenTalks) -> (daysBetweenTalks - 1) * 8 * 60)
                .indictWith((speaker, daysBetweenTalks) -> Collections.singleton(speaker))
                .justifyWith(
                        (speaker, integer, score) -> new ConferenceSchedulingJustification(
                                "Required makespan for speaker %s".formatted(speaker.getName())))
                .asConstraint(SPEAKER_MAKESPAN);
    }

}
