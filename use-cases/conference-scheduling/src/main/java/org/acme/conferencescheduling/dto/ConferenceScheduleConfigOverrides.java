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

    public ConferenceScheduleConfigOverrides {
        themeTrackConflictWeight =
                themeTrackConflictWeight != null && themeTrackConflictWeight < 0L ? 0L : themeTrackConflictWeight;
        themeTrackRoomStabilityWeight = themeTrackRoomStabilityWeight != null && themeTrackRoomStabilityWeight < 0L ? 0L
                : themeTrackRoomStabilityWeight;
        sectorConflictWeight = sectorConflictWeight != null && sectorConflictWeight < 0L ? 0L : sectorConflictWeight;
        audienceTypeDiversityWeight =
                audienceTypeDiversityWeight != null && audienceTypeDiversityWeight < 0L ? 0L : audienceTypeDiversityWeight;
        audienceTypeThemeTrackConflictWeight =
                audienceTypeThemeTrackConflictWeight != null && audienceTypeThemeTrackConflictWeight < 0L ? 0L
                        : audienceTypeThemeTrackConflictWeight;
        audienceLevelDiversityWeight =
                audienceLevelDiversityWeight != null && audienceLevelDiversityWeight < 0L ? 0L : audienceLevelDiversityWeight;
        contentAudienceLevelFlowViolationWeight =
                contentAudienceLevelFlowViolationWeight != null && contentAudienceLevelFlowViolationWeight < 0L ? 0L
                        : contentAudienceLevelFlowViolationWeight;
        contentConflictWeight = contentConflictWeight != null && contentConflictWeight < 0L ? 0L : contentConflictWeight;
        languageDiversityWeight =
                languageDiversityWeight != null && languageDiversityWeight < 0L ? 0L : languageDiversityWeight;
        sameDayTalksWeight = sameDayTalksWeight != null && sameDayTalksWeight < 0L ? 0L : sameDayTalksWeight;
        popularTalksWeight = popularTalksWeight != null && popularTalksWeight < 0L ? 0L : popularTalksWeight;
        speakerPreferredTimeslotTagsWeight =
                speakerPreferredTimeslotTagsWeight != null && speakerPreferredTimeslotTagsWeight < 0L ? 0L
                        : speakerPreferredTimeslotTagsWeight;
        speakerUndesiredTimeslotTagsWeight =
                speakerUndesiredTimeslotTagsWeight != null && speakerUndesiredTimeslotTagsWeight < 0L ? 0L
                        : speakerUndesiredTimeslotTagsWeight;
        talkPreferredTimeslotTagsWeight = talkPreferredTimeslotTagsWeight != null && talkPreferredTimeslotTagsWeight < 0L ? 0L
                : talkPreferredTimeslotTagsWeight;
        talkUndesiredTimeslotTagsWeight = talkUndesiredTimeslotTagsWeight != null && talkUndesiredTimeslotTagsWeight < 0L ? 0L
                : talkUndesiredTimeslotTagsWeight;
        speakerPreferredRoomTagsWeight = speakerPreferredRoomTagsWeight != null && speakerPreferredRoomTagsWeight < 0L ? 0L
                : speakerPreferredRoomTagsWeight;
        speakerUndesiredRoomTagsWeight = speakerUndesiredRoomTagsWeight != null && speakerUndesiredRoomTagsWeight < 0L ? 0L
                : speakerUndesiredRoomTagsWeight;
        talkPreferredRoomTagsWeight =
                talkPreferredRoomTagsWeight != null && talkPreferredRoomTagsWeight < 0L ? 0L : talkPreferredRoomTagsWeight;
        talkUndesiredRoomTagsWeight =
                talkUndesiredRoomTagsWeight != null && talkUndesiredRoomTagsWeight < 0L ? 0L : talkUndesiredRoomTagsWeight;
        speakerMakespanWeight = speakerMakespanWeight != null && speakerMakespanWeight < 0L ? 0L : speakerMakespanWeight;
    }

    public ConferenceScheduleConfigOverrides() {
        this(1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L);
    }

    public ConferenceScheduleConfigOverrides withThemeTrackConflictWeight(Long themeTrackConflictWeight) {
        return new ConferenceScheduleConfigOverrides(themeTrackConflictWeight, themeTrackRoomStabilityWeight,
                sectorConflictWeight, audienceTypeDiversityWeight, audienceTypeThemeTrackConflictWeight,
                audienceLevelDiversityWeight, contentAudienceLevelFlowViolationWeight, contentConflictWeight,
                languageDiversityWeight, sameDayTalksWeight, popularTalksWeight, speakerPreferredTimeslotTagsWeight,
                speakerUndesiredTimeslotTagsWeight, talkPreferredTimeslotTagsWeight, talkUndesiredTimeslotTagsWeight,
                speakerPreferredRoomTagsWeight, speakerUndesiredRoomTagsWeight, talkPreferredRoomTagsWeight,
                talkUndesiredRoomTagsWeight, speakerMakespanWeight);
    }

    public ConferenceScheduleConfigOverrides withThemeTrackRoomStabilityWeight(Long themeTrackRoomStabilityWeight) {
        return new ConferenceScheduleConfigOverrides(themeTrackConflictWeight, themeTrackRoomStabilityWeight,
                sectorConflictWeight, audienceTypeDiversityWeight, audienceTypeThemeTrackConflictWeight,
                audienceLevelDiversityWeight, contentAudienceLevelFlowViolationWeight, contentConflictWeight,
                languageDiversityWeight, sameDayTalksWeight, popularTalksWeight, speakerPreferredTimeslotTagsWeight,
                speakerUndesiredTimeslotTagsWeight, talkPreferredTimeslotTagsWeight, talkUndesiredTimeslotTagsWeight,
                speakerPreferredRoomTagsWeight, speakerUndesiredRoomTagsWeight, talkPreferredRoomTagsWeight,
                talkUndesiredRoomTagsWeight, speakerMakespanWeight);
    }

    public ConferenceScheduleConfigOverrides withSectorConflictWeight(Long sectorConflictWeight) {
        return new ConferenceScheduleConfigOverrides(themeTrackConflictWeight, themeTrackRoomStabilityWeight,
                sectorConflictWeight, audienceTypeDiversityWeight, audienceTypeThemeTrackConflictWeight,
                audienceLevelDiversityWeight, contentAudienceLevelFlowViolationWeight, contentConflictWeight,
                languageDiversityWeight, sameDayTalksWeight, popularTalksWeight, speakerPreferredTimeslotTagsWeight,
                speakerUndesiredTimeslotTagsWeight, talkPreferredTimeslotTagsWeight, talkUndesiredTimeslotTagsWeight,
                speakerPreferredRoomTagsWeight, speakerUndesiredRoomTagsWeight, talkPreferredRoomTagsWeight,
                talkUndesiredRoomTagsWeight, speakerMakespanWeight);
    }

    public ConferenceScheduleConfigOverrides withAudienceTypeDiversityWeight(Long audienceTypeDiversityWeight) {
        return new ConferenceScheduleConfigOverrides(themeTrackConflictWeight, themeTrackRoomStabilityWeight,
                sectorConflictWeight, audienceTypeDiversityWeight, audienceTypeThemeTrackConflictWeight,
                audienceLevelDiversityWeight, contentAudienceLevelFlowViolationWeight, contentConflictWeight,
                languageDiversityWeight, sameDayTalksWeight, popularTalksWeight, speakerPreferredTimeslotTagsWeight,
                speakerUndesiredTimeslotTagsWeight, talkPreferredTimeslotTagsWeight, talkUndesiredTimeslotTagsWeight,
                speakerPreferredRoomTagsWeight, speakerUndesiredRoomTagsWeight, talkPreferredRoomTagsWeight,
                talkUndesiredRoomTagsWeight, speakerMakespanWeight);
    }

    public ConferenceScheduleConfigOverrides
            withAudienceTypeThemeTrackConflictWeight(Long audienceTypeThemeTrackConflictWeight) {
        return new ConferenceScheduleConfigOverrides(themeTrackConflictWeight, themeTrackRoomStabilityWeight,
                sectorConflictWeight, audienceTypeDiversityWeight, audienceTypeThemeTrackConflictWeight,
                audienceLevelDiversityWeight, contentAudienceLevelFlowViolationWeight, contentConflictWeight,
                languageDiversityWeight, sameDayTalksWeight, popularTalksWeight, speakerPreferredTimeslotTagsWeight,
                speakerUndesiredTimeslotTagsWeight, talkPreferredTimeslotTagsWeight, talkUndesiredTimeslotTagsWeight,
                speakerPreferredRoomTagsWeight, speakerUndesiredRoomTagsWeight, talkPreferredRoomTagsWeight,
                talkUndesiredRoomTagsWeight, speakerMakespanWeight);
    }

    public ConferenceScheduleConfigOverrides withAudienceLevelDiversityWeight(Long audienceLevelDiversityWeight) {
        return new ConferenceScheduleConfigOverrides(themeTrackConflictWeight, themeTrackRoomStabilityWeight,
                sectorConflictWeight, audienceTypeDiversityWeight, audienceTypeThemeTrackConflictWeight,
                audienceLevelDiversityWeight, contentAudienceLevelFlowViolationWeight, contentConflictWeight,
                languageDiversityWeight, sameDayTalksWeight, popularTalksWeight, speakerPreferredTimeslotTagsWeight,
                speakerUndesiredTimeslotTagsWeight, talkPreferredTimeslotTagsWeight, talkUndesiredTimeslotTagsWeight,
                speakerPreferredRoomTagsWeight, speakerUndesiredRoomTagsWeight, talkPreferredRoomTagsWeight,
                talkUndesiredRoomTagsWeight, speakerMakespanWeight);
    }

    public ConferenceScheduleConfigOverrides
            withContentAudienceLevelFlowViolationWeight(Long contentAudienceLevelFlowViolationWeight) {
        return new ConferenceScheduleConfigOverrides(themeTrackConflictWeight, themeTrackRoomStabilityWeight,
                sectorConflictWeight, audienceTypeDiversityWeight, audienceTypeThemeTrackConflictWeight,
                audienceLevelDiversityWeight, contentAudienceLevelFlowViolationWeight, contentConflictWeight,
                languageDiversityWeight, sameDayTalksWeight, popularTalksWeight, speakerPreferredTimeslotTagsWeight,
                speakerUndesiredTimeslotTagsWeight, talkPreferredTimeslotTagsWeight, talkUndesiredTimeslotTagsWeight,
                speakerPreferredRoomTagsWeight, speakerUndesiredRoomTagsWeight, talkPreferredRoomTagsWeight,
                talkUndesiredRoomTagsWeight, speakerMakespanWeight);
    }

    public ConferenceScheduleConfigOverrides withContentConflictWeight(Long contentConflictWeight) {
        return new ConferenceScheduleConfigOverrides(themeTrackConflictWeight, themeTrackRoomStabilityWeight,
                sectorConflictWeight, audienceTypeDiversityWeight, audienceTypeThemeTrackConflictWeight,
                audienceLevelDiversityWeight, contentAudienceLevelFlowViolationWeight, contentConflictWeight,
                languageDiversityWeight, sameDayTalksWeight, popularTalksWeight, speakerPreferredTimeslotTagsWeight,
                speakerUndesiredTimeslotTagsWeight, talkPreferredTimeslotTagsWeight, talkUndesiredTimeslotTagsWeight,
                speakerPreferredRoomTagsWeight, speakerUndesiredRoomTagsWeight, talkPreferredRoomTagsWeight,
                talkUndesiredRoomTagsWeight, speakerMakespanWeight);
    }

    public ConferenceScheduleConfigOverrides withLanguageDiversityWeight(Long languageDiversityWeight) {
        return new ConferenceScheduleConfigOverrides(themeTrackConflictWeight, themeTrackRoomStabilityWeight,
                sectorConflictWeight, audienceTypeDiversityWeight, audienceTypeThemeTrackConflictWeight,
                audienceLevelDiversityWeight, contentAudienceLevelFlowViolationWeight, contentConflictWeight,
                languageDiversityWeight, sameDayTalksWeight, popularTalksWeight, speakerPreferredTimeslotTagsWeight,
                speakerUndesiredTimeslotTagsWeight, talkPreferredTimeslotTagsWeight, talkUndesiredTimeslotTagsWeight,
                speakerPreferredRoomTagsWeight, speakerUndesiredRoomTagsWeight, talkPreferredRoomTagsWeight,
                talkUndesiredRoomTagsWeight, speakerMakespanWeight);
    }

    public ConferenceScheduleConfigOverrides withSameDayTalksWeight(Long sameDayTalksWeight) {
        return new ConferenceScheduleConfigOverrides(themeTrackConflictWeight, themeTrackRoomStabilityWeight,
                sectorConflictWeight, audienceTypeDiversityWeight, audienceTypeThemeTrackConflictWeight,
                audienceLevelDiversityWeight, contentAudienceLevelFlowViolationWeight, contentConflictWeight,
                languageDiversityWeight, sameDayTalksWeight, popularTalksWeight, speakerPreferredTimeslotTagsWeight,
                speakerUndesiredTimeslotTagsWeight, talkPreferredTimeslotTagsWeight, talkUndesiredTimeslotTagsWeight,
                speakerPreferredRoomTagsWeight, speakerUndesiredRoomTagsWeight, talkPreferredRoomTagsWeight,
                talkUndesiredRoomTagsWeight, speakerMakespanWeight);
    }

    public ConferenceScheduleConfigOverrides withPopularTalksWeight(Long popularTalksWeight) {
        return new ConferenceScheduleConfigOverrides(themeTrackConflictWeight, themeTrackRoomStabilityWeight,
                sectorConflictWeight, audienceTypeDiversityWeight, audienceTypeThemeTrackConflictWeight,
                audienceLevelDiversityWeight, contentAudienceLevelFlowViolationWeight, contentConflictWeight,
                languageDiversityWeight, sameDayTalksWeight, popularTalksWeight, speakerPreferredTimeslotTagsWeight,
                speakerUndesiredTimeslotTagsWeight, talkPreferredTimeslotTagsWeight, talkUndesiredTimeslotTagsWeight,
                speakerPreferredRoomTagsWeight, speakerUndesiredRoomTagsWeight, talkPreferredRoomTagsWeight,
                talkUndesiredRoomTagsWeight, speakerMakespanWeight);
    }

    public ConferenceScheduleConfigOverrides withSpeakerPreferredTimeslotTagsWeight(Long speakerPreferredTimeslotTagsWeight) {
        return new ConferenceScheduleConfigOverrides(themeTrackConflictWeight, themeTrackRoomStabilityWeight,
                sectorConflictWeight, audienceTypeDiversityWeight, audienceTypeThemeTrackConflictWeight,
                audienceLevelDiversityWeight, contentAudienceLevelFlowViolationWeight, contentConflictWeight,
                languageDiversityWeight, sameDayTalksWeight, popularTalksWeight, speakerPreferredTimeslotTagsWeight,
                speakerUndesiredTimeslotTagsWeight, talkPreferredTimeslotTagsWeight, talkUndesiredTimeslotTagsWeight,
                speakerPreferredRoomTagsWeight, speakerUndesiredRoomTagsWeight, talkPreferredRoomTagsWeight,
                talkUndesiredRoomTagsWeight, speakerMakespanWeight);
    }

    public ConferenceScheduleConfigOverrides withSpeakerUndesiredTimeslotTagsWeight(Long speakerUndesiredTimeslotTagsWeight) {
        return new ConferenceScheduleConfigOverrides(themeTrackConflictWeight, themeTrackRoomStabilityWeight,
                sectorConflictWeight, audienceTypeDiversityWeight, audienceTypeThemeTrackConflictWeight,
                audienceLevelDiversityWeight, contentAudienceLevelFlowViolationWeight, contentConflictWeight,
                languageDiversityWeight, sameDayTalksWeight, popularTalksWeight, speakerPreferredTimeslotTagsWeight,
                speakerUndesiredTimeslotTagsWeight, talkPreferredTimeslotTagsWeight, talkUndesiredTimeslotTagsWeight,
                speakerPreferredRoomTagsWeight, speakerUndesiredRoomTagsWeight, talkPreferredRoomTagsWeight,
                talkUndesiredRoomTagsWeight, speakerMakespanWeight);
    }

    public ConferenceScheduleConfigOverrides withTalkPreferredTimeslotTagsWeight(Long talkPreferredTimeslotTagsWeight) {
        return new ConferenceScheduleConfigOverrides(themeTrackConflictWeight, themeTrackRoomStabilityWeight,
                sectorConflictWeight, audienceTypeDiversityWeight, audienceTypeThemeTrackConflictWeight,
                audienceLevelDiversityWeight, contentAudienceLevelFlowViolationWeight, contentConflictWeight,
                languageDiversityWeight, sameDayTalksWeight, popularTalksWeight, speakerPreferredTimeslotTagsWeight,
                speakerUndesiredTimeslotTagsWeight, talkPreferredTimeslotTagsWeight, talkUndesiredTimeslotTagsWeight,
                speakerPreferredRoomTagsWeight, speakerUndesiredRoomTagsWeight, talkPreferredRoomTagsWeight,
                talkUndesiredRoomTagsWeight, speakerMakespanWeight);
    }

    public ConferenceScheduleConfigOverrides withTalkUndesiredTimeslotTagsWeight(Long talkUndesiredTimeslotTagsWeight) {
        return new ConferenceScheduleConfigOverrides(themeTrackConflictWeight, themeTrackRoomStabilityWeight,
                sectorConflictWeight, audienceTypeDiversityWeight, audienceTypeThemeTrackConflictWeight,
                audienceLevelDiversityWeight, contentAudienceLevelFlowViolationWeight, contentConflictWeight,
                languageDiversityWeight, sameDayTalksWeight, popularTalksWeight, speakerPreferredTimeslotTagsWeight,
                speakerUndesiredTimeslotTagsWeight, talkPreferredTimeslotTagsWeight, talkUndesiredTimeslotTagsWeight,
                speakerPreferredRoomTagsWeight, speakerUndesiredRoomTagsWeight, talkPreferredRoomTagsWeight,
                talkUndesiredRoomTagsWeight, speakerMakespanWeight);
    }

    public ConferenceScheduleConfigOverrides withSpeakerPreferredRoomTagsWeight(Long speakerPreferredRoomTagsWeight) {
        return new ConferenceScheduleConfigOverrides(themeTrackConflictWeight, themeTrackRoomStabilityWeight,
                sectorConflictWeight, audienceTypeDiversityWeight, audienceTypeThemeTrackConflictWeight,
                audienceLevelDiversityWeight, contentAudienceLevelFlowViolationWeight, contentConflictWeight,
                languageDiversityWeight, sameDayTalksWeight, popularTalksWeight, speakerPreferredTimeslotTagsWeight,
                speakerUndesiredTimeslotTagsWeight, talkPreferredTimeslotTagsWeight, talkUndesiredTimeslotTagsWeight,
                speakerPreferredRoomTagsWeight, speakerUndesiredRoomTagsWeight, talkPreferredRoomTagsWeight,
                talkUndesiredRoomTagsWeight, speakerMakespanWeight);
    }

    public ConferenceScheduleConfigOverrides withSpeakerUndesiredRoomTagsWeight(Long speakerUndesiredRoomTagsWeight) {
        return new ConferenceScheduleConfigOverrides(themeTrackConflictWeight, themeTrackRoomStabilityWeight,
                sectorConflictWeight, audienceTypeDiversityWeight, audienceTypeThemeTrackConflictWeight,
                audienceLevelDiversityWeight, contentAudienceLevelFlowViolationWeight, contentConflictWeight,
                languageDiversityWeight, sameDayTalksWeight, popularTalksWeight, speakerPreferredTimeslotTagsWeight,
                speakerUndesiredTimeslotTagsWeight, talkPreferredTimeslotTagsWeight, talkUndesiredTimeslotTagsWeight,
                speakerPreferredRoomTagsWeight, speakerUndesiredRoomTagsWeight, talkPreferredRoomTagsWeight,
                talkUndesiredRoomTagsWeight, speakerMakespanWeight);
    }

    public ConferenceScheduleConfigOverrides withTalkPreferredRoomTagsWeight(Long talkPreferredRoomTagsWeight) {
        return new ConferenceScheduleConfigOverrides(themeTrackConflictWeight, themeTrackRoomStabilityWeight,
                sectorConflictWeight, audienceTypeDiversityWeight, audienceTypeThemeTrackConflictWeight,
                audienceLevelDiversityWeight, contentAudienceLevelFlowViolationWeight, contentConflictWeight,
                languageDiversityWeight, sameDayTalksWeight, popularTalksWeight, speakerPreferredTimeslotTagsWeight,
                speakerUndesiredTimeslotTagsWeight, talkPreferredTimeslotTagsWeight, talkUndesiredTimeslotTagsWeight,
                speakerPreferredRoomTagsWeight, speakerUndesiredRoomTagsWeight, talkPreferredRoomTagsWeight,
                talkUndesiredRoomTagsWeight, speakerMakespanWeight);
    }

    public ConferenceScheduleConfigOverrides withTalkUndesiredRoomTagsWeight(Long talkUndesiredRoomTagsWeight) {
        return new ConferenceScheduleConfigOverrides(themeTrackConflictWeight, themeTrackRoomStabilityWeight,
                sectorConflictWeight, audienceTypeDiversityWeight, audienceTypeThemeTrackConflictWeight,
                audienceLevelDiversityWeight, contentAudienceLevelFlowViolationWeight, contentConflictWeight,
                languageDiversityWeight, sameDayTalksWeight, popularTalksWeight, speakerPreferredTimeslotTagsWeight,
                speakerUndesiredTimeslotTagsWeight, talkPreferredTimeslotTagsWeight, talkUndesiredTimeslotTagsWeight,
                speakerPreferredRoomTagsWeight, speakerUndesiredRoomTagsWeight, talkPreferredRoomTagsWeight,
                talkUndesiredRoomTagsWeight, speakerMakespanWeight);
    }

    public ConferenceScheduleConfigOverrides withSpeakerMakespanWeight(Long speakerMakespanWeight) {
        return new ConferenceScheduleConfigOverrides(themeTrackConflictWeight, themeTrackRoomStabilityWeight,
                sectorConflictWeight, audienceTypeDiversityWeight, audienceTypeThemeTrackConflictWeight,
                audienceLevelDiversityWeight, contentAudienceLevelFlowViolationWeight, contentConflictWeight,
                languageDiversityWeight, sameDayTalksWeight, popularTalksWeight, speakerPreferredTimeslotTagsWeight,
                speakerUndesiredTimeslotTagsWeight, talkPreferredTimeslotTagsWeight, talkUndesiredTimeslotTagsWeight,
                speakerPreferredRoomTagsWeight, speakerUndesiredRoomTagsWeight, talkPreferredRoomTagsWeight,
                talkUndesiredRoomTagsWeight, speakerMakespanWeight);
    }
}
