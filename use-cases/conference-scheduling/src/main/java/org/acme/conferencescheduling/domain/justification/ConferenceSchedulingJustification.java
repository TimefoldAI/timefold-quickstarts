package org.acme.conferencescheduling.domain.justification;

import ai.timefold.solver.service.definition.api.ModelConstraintJustification;

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
                RoomUnavailableAtTalkTimeslotJustification.class,
                TalksOverlappingInSameRoomJustification.class,
                SpeakerUnavailableAtTalkTimeslotJustification.class,
                SpeakerAssignedToOverlappingTalksJustification.class,
                TalkScheduledBeforePrerequisiteTalkJustification.class,
                MutuallyExclusiveTalksOverlappingJustification.class,
                SpeakerConsecutiveTalksPauseTooShortJustification.class,
                CrowdControlTalkNotPairedJustification.class,
                MissingRequiredTimeslotTagsForSpeakersJustification.class,
                ProhibitedTimeslotTagsForSpeakersJustification.class,
                MissingRequiredTimeslotTagsForTalkJustification.class,
                ProhibitedTimeslotTagsForTalkJustification.class,
                MissingRequiredRoomTagsForSpeakersJustification.class,
                ProhibitedRoomTagsForSpeakersJustification.class,
                MissingRequiredRoomTagsForTalkJustification.class,
                ProhibitedRoomTagsForTalkJustification.class,

                // Soft constraints
                TalksWithSameThemeTrackOverlappingJustification.class,
                TalksWithSameThemeTrackInDifferentRoomsJustification.class,
                TalksWithSameSectorOverlappingJustification.class,
                TalksWithSameAudienceTypeInSameTimeslotJustification.class,
                TalksWithSameThemeTrackAndAudienceTypeOverlappingJustification.class,
                TalksWithDifferentAudienceLevelInSameTimeslotJustification.class,
                SharedContentAudienceLevelFlowViolationJustification.class,
                TalksWithSameContentOverlappingJustification.class,
                TalksWithSameLanguageInSameTimeslotJustification.class,
                RelatedTalksNotOnSameDayJustification.class,
                PopularTalkInSmallerRoomJustification.class,
                MissingPreferredTimeslotTagsForSpeakersJustification.class,
                UndesiredTimeslotTagsForSpeakersJustification.class,
                MissingPreferredTimeslotTagsForTalkJustification.class,
                UndesiredTimeslotTagsForTalkJustification.class,
                MissingPreferredRoomTagsForSpeakersJustification.class,
                UndesiredRoomTagsForSpeakersJustification.class,
                MissingPreferredRoomTagsForTalkJustification.class,
                UndesiredRoomTagsForTalkJustification.class,
                SpeakerMakespanTooLongJustification.class })
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
}
