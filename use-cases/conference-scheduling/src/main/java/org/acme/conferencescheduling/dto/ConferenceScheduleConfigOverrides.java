package org.acme.conferencescheduling.dto;

import ai.timefold.solver.service.definition.api.ModelConfigOverrides;
import ai.timefold.solver.service.definition.api.domain.ConstraintReference;

import org.acme.conferencescheduling.domain.ConferenceConstraintProperties;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonInclude;

@Schema(description = "Soft constraint weights. Set a weight to 0 to disable the corresponding constraint. "
        + "A weight left unset (null) is not overridden here, so the value from the configuration profile "
        + "(or the constraint's default) applies. This makes it possible to override some weights via the "
        + "input while leaving others to the configuration profile.")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ConferenceScheduleConfigOverrides(
        @ConstraintReference(ConferenceConstraintProperties.THEME_TRACK_CONFLICT) @Schema(
                description = "Soft weight of the themeTrackConflict constraint.") Long themeTrackConflictWeight,
        @ConstraintReference(ConferenceConstraintProperties.THEME_TRACK_ROOM_STABILITY) @Schema(
                description = "Soft weight of the themeTrackRoomStability constraint.") Long themeTrackRoomStabilityWeight,
        @ConstraintReference(ConferenceConstraintProperties.SECTOR_CONFLICT) @Schema(
                description = "Soft weight of the sectorConflict constraint.") Long sectorConflictWeight,
        @ConstraintReference(ConferenceConstraintProperties.AUDIENCE_TYPE_DIVERSITY) @Schema(
                description = "Soft weight of the audienceTypeDiversity constraint.") Long audienceTypeDiversityWeight,
        @ConstraintReference(ConferenceConstraintProperties.AUDIENCE_TYPE_THEME_TRACK_CONFLICT) @Schema(
                description = "Soft weight of the audienceTypeThemeTrackConflict constraint.") Long audienceTypeThemeTrackConflictWeight,
        @ConstraintReference(ConferenceConstraintProperties.AUDIENCE_LEVEL_DIVERSITY) @Schema(
                description = "Soft weight of the audienceLevelDiversity constraint.") Long audienceLevelDiversityWeight,
        @ConstraintReference(ConferenceConstraintProperties.CONTENT_AUDIENCE_LEVEL_FLOW_VIOLATION) @Schema(
                description = "Soft weight of the contentAudienceLevelFlowViolation constraint.") Long contentAudienceLevelFlowViolationWeight,
        @ConstraintReference(ConferenceConstraintProperties.CONTENT_CONFLICT) @Schema(
                description = "Soft weight of the contentConflict constraint.") Long contentConflictWeight,
        @ConstraintReference(ConferenceConstraintProperties.LANGUAGE_DIVERSITY) @Schema(
                description = "Soft weight of the languageDiversity constraint.") Long languageDiversityWeight,
        @ConstraintReference(ConferenceConstraintProperties.SAME_DAY_TALKS) @Schema(
                description = "Soft weight of the sameDayTalks constraint.") Long sameDayTalksWeight,
        @ConstraintReference(ConferenceConstraintProperties.POPULAR_TALKS) @Schema(
                description = "Soft weight of the popularTalks constraint.") Long popularTalksWeight,
        @ConstraintReference(ConferenceConstraintProperties.SPEAKER_PREFERRED_TIMESLOT_TAGS) @Schema(
                description = "Soft weight of the speakerPreferredTimeslotTags constraint.") Long speakerPreferredTimeslotTagsWeight,
        @ConstraintReference(ConferenceConstraintProperties.SPEAKER_UNDESIRED_TIMESLOT_TAGS) @Schema(
                description = "Soft weight of the speakerUndesiredTimeslotTags constraint.") Long speakerUndesiredTimeslotTagsWeight,
        @ConstraintReference(ConferenceConstraintProperties.TALK_PREFERRED_TIMESLOT_TAGS) @Schema(
                description = "Soft weight of the talkPreferredTimeslotTags constraint.") Long talkPreferredTimeslotTagsWeight,
        @ConstraintReference(ConferenceConstraintProperties.TALK_UNDESIRED_TIMESLOT_TAGS) @Schema(
                description = "Soft weight of the talkUndesiredTimeslotTags constraint.") Long talkUndesiredTimeslotTagsWeight,
        @ConstraintReference(ConferenceConstraintProperties.SPEAKER_PREFERRED_ROOM_TAGS) @Schema(
                description = "Soft weight of the speakerPreferredRoomTags constraint.") Long speakerPreferredRoomTagsWeight,
        @ConstraintReference(ConferenceConstraintProperties.SPEAKER_UNDESIRED_ROOM_TAGS) @Schema(
                description = "Soft weight of the speakerUndesiredRoomTags constraint.") Long speakerUndesiredRoomTagsWeight,
        @ConstraintReference(ConferenceConstraintProperties.TALK_PREFERRED_ROOM_TAGS) @Schema(
                description = "Soft weight of the talkPreferredRoomTags constraint.") Long talkPreferredRoomTagsWeight,
        @ConstraintReference(ConferenceConstraintProperties.TALK_UNDESIRED_ROOM_TAGS) @Schema(
                description = "Soft weight of the talkUndesiredRoomTags constraint.") Long talkUndesiredRoomTagsWeight,
        @ConstraintReference(ConferenceConstraintProperties.SPEAKER_MAKESPAN) @Schema(
                description = "Soft weight of the speakerMakespan constraint.") Long speakerMakespanWeight)
        implements
            ModelConfigOverrides {

    /**
     * Creates an empty overrides instance: no weight is overridden, so the configuration profile
     * (or each constraint's default) applies. Required by the Service Module to generate the default config profile.
     */
    public ConferenceScheduleConfigOverrides() {
        this(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                null, null);
    }

    public ConferenceScheduleConfigOverrides {
        themeTrackConflictWeight = nonNegative(themeTrackConflictWeight);
        themeTrackRoomStabilityWeight = nonNegative(themeTrackRoomStabilityWeight);
        sectorConflictWeight = nonNegative(sectorConflictWeight);
        audienceTypeDiversityWeight = nonNegative(audienceTypeDiversityWeight);
        audienceTypeThemeTrackConflictWeight = nonNegative(audienceTypeThemeTrackConflictWeight);
        audienceLevelDiversityWeight = nonNegative(audienceLevelDiversityWeight);
        contentAudienceLevelFlowViolationWeight = nonNegative(contentAudienceLevelFlowViolationWeight);
        contentConflictWeight = nonNegative(contentConflictWeight);
        languageDiversityWeight = nonNegative(languageDiversityWeight);
        sameDayTalksWeight = nonNegative(sameDayTalksWeight);
        popularTalksWeight = nonNegative(popularTalksWeight);
        speakerPreferredTimeslotTagsWeight = nonNegative(speakerPreferredTimeslotTagsWeight);
        speakerUndesiredTimeslotTagsWeight = nonNegative(speakerUndesiredTimeslotTagsWeight);
        talkPreferredTimeslotTagsWeight = nonNegative(talkPreferredTimeslotTagsWeight);
        talkUndesiredTimeslotTagsWeight = nonNegative(talkUndesiredTimeslotTagsWeight);
        speakerPreferredRoomTagsWeight = nonNegative(speakerPreferredRoomTagsWeight);
        speakerUndesiredRoomTagsWeight = nonNegative(speakerUndesiredRoomTagsWeight);
        talkPreferredRoomTagsWeight = nonNegative(talkPreferredRoomTagsWeight);
        talkUndesiredRoomTagsWeight = nonNegative(talkUndesiredRoomTagsWeight);
        speakerMakespanWeight = nonNegative(speakerMakespanWeight);
    }

    /**
     * Clamps a negative weight to 0 (disabled) and keeps null as "not overridden".
     */
    private static Long nonNegative(Long weight) {
        if (weight == null) {
            return null;
        }
        return Math.max(weight, 0L);
    }
}
