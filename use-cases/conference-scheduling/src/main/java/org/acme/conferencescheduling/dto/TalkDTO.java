package org.acme.conferencescheduling.dto;

import java.util.List;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A talk to be assigned to a timeslot and a room.")
public record TalkDTO(
        @Schema(description = "code") String code,
        @Schema(description = "title") String title,
        @Schema(description = "talkTypeName") String talkTypeName,
        @Schema(description = "speakerIds") List<String> speakerIds,
        @Schema(description = "themeTrackTags") List<String> themeTrackTags,
        @Schema(description = "sectorTags") List<String> sectorTags,
        @Schema(description = "audienceTypes") List<String> audienceTypes,
        @Schema(description = "audienceLevel") int audienceLevel,
        @Schema(description = "contentTags") List<String> contentTags,
        @Schema(description = "language") String language,
        @Schema(description = "requiredTimeslotTags") List<String> requiredTimeslotTags,
        @Schema(description = "preferredTimeslotTags") List<String> preferredTimeslotTags,
        @Schema(description = "prohibitedTimeslotTags") List<String> prohibitedTimeslotTags,
        @Schema(description = "undesiredTimeslotTags") List<String> undesiredTimeslotTags,
        @Schema(description = "requiredRoomTags") List<String> requiredRoomTags,
        @Schema(description = "preferredRoomTags") List<String> preferredRoomTags,
        @Schema(description = "prohibitedRoomTags") List<String> prohibitedRoomTags,
        @Schema(description = "undesiredRoomTags") List<String> undesiredRoomTags,
        @Schema(description = "mutuallyExclusiveTalksTags") List<String> mutuallyExclusiveTalksTags,
        @Schema(description = "prerequisiteTalkCodes") List<String> prerequisiteTalkCodes,
        @Schema(description = "favoriteCount") int favoriteCount,
        @Schema(description = "crowdControlRisk") int crowdControlRisk,
        @Schema(description = "timeslotId") String timeslotId,
        @Schema(description = "roomId") String roomId) {

    public TalkDTO {
        code = code == null ? "" : code;
        title = title == null ? "" : title;
        talkTypeName = talkTypeName == null ? "" : talkTypeName;
        speakerIds = speakerIds == null ? List.of() : List.copyOf(speakerIds);
        themeTrackTags = themeTrackTags == null ? List.of() : List.copyOf(themeTrackTags);
        sectorTags = sectorTags == null ? List.of() : List.copyOf(sectorTags);
        audienceTypes = audienceTypes == null ? List.of() : List.copyOf(audienceTypes);
        contentTags = contentTags == null ? List.of() : List.copyOf(contentTags);
        language = language == null ? "" : language;
        requiredTimeslotTags = requiredTimeslotTags == null ? List.of() : List.copyOf(requiredTimeslotTags);
        preferredTimeslotTags = preferredTimeslotTags == null ? List.of() : List.copyOf(preferredTimeslotTags);
        prohibitedTimeslotTags = prohibitedTimeslotTags == null ? List.of() : List.copyOf(prohibitedTimeslotTags);
        undesiredTimeslotTags = undesiredTimeslotTags == null ? List.of() : List.copyOf(undesiredTimeslotTags);
        requiredRoomTags = requiredRoomTags == null ? List.of() : List.copyOf(requiredRoomTags);
        preferredRoomTags = preferredRoomTags == null ? List.of() : List.copyOf(preferredRoomTags);
        prohibitedRoomTags = prohibitedRoomTags == null ? List.of() : List.copyOf(prohibitedRoomTags);
        undesiredRoomTags = undesiredRoomTags == null ? List.of() : List.copyOf(undesiredRoomTags);
        mutuallyExclusiveTalksTags = mutuallyExclusiveTalksTags == null ? List.of() : List.copyOf(mutuallyExclusiveTalksTags);
        prerequisiteTalkCodes = prerequisiteTalkCodes == null ? List.of() : List.copyOf(prerequisiteTalkCodes);
        timeslotId = normalizeId(timeslotId);
        roomId = normalizeId(roomId);
    }

    private static String normalizeId(String id) {
        return id != null && id.isBlank() ? null : id;
    }

    public TalkDTO withCode(String code) {
        return new TalkDTO(code, title, talkTypeName, speakerIds, themeTrackTags, sectorTags, audienceTypes, audienceLevel,
                contentTags, language, requiredTimeslotTags, preferredTimeslotTags, prohibitedTimeslotTags,
                undesiredTimeslotTags, requiredRoomTags, preferredRoomTags, prohibitedRoomTags, undesiredRoomTags,
                mutuallyExclusiveTalksTags, prerequisiteTalkCodes, favoriteCount, crowdControlRisk, timeslotId, roomId);
    }

    public TalkDTO withTitle(String title) {
        return new TalkDTO(code, title, talkTypeName, speakerIds, themeTrackTags, sectorTags, audienceTypes, audienceLevel,
                contentTags, language, requiredTimeslotTags, preferredTimeslotTags, prohibitedTimeslotTags,
                undesiredTimeslotTags, requiredRoomTags, preferredRoomTags, prohibitedRoomTags, undesiredRoomTags,
                mutuallyExclusiveTalksTags, prerequisiteTalkCodes, favoriteCount, crowdControlRisk, timeslotId, roomId);
    }

    public TalkDTO withTalkTypeName(String talkTypeName) {
        return new TalkDTO(code, title, talkTypeName, speakerIds, themeTrackTags, sectorTags, audienceTypes, audienceLevel,
                contentTags, language, requiredTimeslotTags, preferredTimeslotTags, prohibitedTimeslotTags,
                undesiredTimeslotTags, requiredRoomTags, preferredRoomTags, prohibitedRoomTags, undesiredRoomTags,
                mutuallyExclusiveTalksTags, prerequisiteTalkCodes, favoriteCount, crowdControlRisk, timeslotId, roomId);
    }

    public TalkDTO withSpeakerIds(List<String> speakerIds) {
        return new TalkDTO(code, title, talkTypeName, speakerIds, themeTrackTags, sectorTags, audienceTypes, audienceLevel,
                contentTags, language, requiredTimeslotTags, preferredTimeslotTags, prohibitedTimeslotTags,
                undesiredTimeslotTags, requiredRoomTags, preferredRoomTags, prohibitedRoomTags, undesiredRoomTags,
                mutuallyExclusiveTalksTags, prerequisiteTalkCodes, favoriteCount, crowdControlRisk, timeslotId, roomId);
    }

    public TalkDTO withThemeTrackTags(List<String> themeTrackTags) {
        return new TalkDTO(code, title, talkTypeName, speakerIds, themeTrackTags, sectorTags, audienceTypes, audienceLevel,
                contentTags, language, requiredTimeslotTags, preferredTimeslotTags, prohibitedTimeslotTags,
                undesiredTimeslotTags, requiredRoomTags, preferredRoomTags, prohibitedRoomTags, undesiredRoomTags,
                mutuallyExclusiveTalksTags, prerequisiteTalkCodes, favoriteCount, crowdControlRisk, timeslotId, roomId);
    }

    public TalkDTO withSectorTags(List<String> sectorTags) {
        return new TalkDTO(code, title, talkTypeName, speakerIds, themeTrackTags, sectorTags, audienceTypes, audienceLevel,
                contentTags, language, requiredTimeslotTags, preferredTimeslotTags, prohibitedTimeslotTags,
                undesiredTimeslotTags, requiredRoomTags, preferredRoomTags, prohibitedRoomTags, undesiredRoomTags,
                mutuallyExclusiveTalksTags, prerequisiteTalkCodes, favoriteCount, crowdControlRisk, timeslotId, roomId);
    }

    public TalkDTO withAudienceTypes(List<String> audienceTypes) {
        return new TalkDTO(code, title, talkTypeName, speakerIds, themeTrackTags, sectorTags, audienceTypes, audienceLevel,
                contentTags, language, requiredTimeslotTags, preferredTimeslotTags, prohibitedTimeslotTags,
                undesiredTimeslotTags, requiredRoomTags, preferredRoomTags, prohibitedRoomTags, undesiredRoomTags,
                mutuallyExclusiveTalksTags, prerequisiteTalkCodes, favoriteCount, crowdControlRisk, timeslotId, roomId);
    }

    public TalkDTO withAudienceLevel(int audienceLevel) {
        return new TalkDTO(code, title, talkTypeName, speakerIds, themeTrackTags, sectorTags, audienceTypes, audienceLevel,
                contentTags, language, requiredTimeslotTags, preferredTimeslotTags, prohibitedTimeslotTags,
                undesiredTimeslotTags, requiredRoomTags, preferredRoomTags, prohibitedRoomTags, undesiredRoomTags,
                mutuallyExclusiveTalksTags, prerequisiteTalkCodes, favoriteCount, crowdControlRisk, timeslotId, roomId);
    }

    public TalkDTO withContentTags(List<String> contentTags) {
        return new TalkDTO(code, title, talkTypeName, speakerIds, themeTrackTags, sectorTags, audienceTypes, audienceLevel,
                contentTags, language, requiredTimeslotTags, preferredTimeslotTags, prohibitedTimeslotTags,
                undesiredTimeslotTags, requiredRoomTags, preferredRoomTags, prohibitedRoomTags, undesiredRoomTags,
                mutuallyExclusiveTalksTags, prerequisiteTalkCodes, favoriteCount, crowdControlRisk, timeslotId, roomId);
    }

    public TalkDTO withLanguage(String language) {
        return new TalkDTO(code, title, talkTypeName, speakerIds, themeTrackTags, sectorTags, audienceTypes, audienceLevel,
                contentTags, language, requiredTimeslotTags, preferredTimeslotTags, prohibitedTimeslotTags,
                undesiredTimeslotTags, requiredRoomTags, preferredRoomTags, prohibitedRoomTags, undesiredRoomTags,
                mutuallyExclusiveTalksTags, prerequisiteTalkCodes, favoriteCount, crowdControlRisk, timeslotId, roomId);
    }

    public TalkDTO withRequiredTimeslotTags(List<String> requiredTimeslotTags) {
        return new TalkDTO(code, title, talkTypeName, speakerIds, themeTrackTags, sectorTags, audienceTypes, audienceLevel,
                contentTags, language, requiredTimeslotTags, preferredTimeslotTags, prohibitedTimeslotTags,
                undesiredTimeslotTags, requiredRoomTags, preferredRoomTags, prohibitedRoomTags, undesiredRoomTags,
                mutuallyExclusiveTalksTags, prerequisiteTalkCodes, favoriteCount, crowdControlRisk, timeslotId, roomId);
    }

    public TalkDTO withPreferredTimeslotTags(List<String> preferredTimeslotTags) {
        return new TalkDTO(code, title, talkTypeName, speakerIds, themeTrackTags, sectorTags, audienceTypes, audienceLevel,
                contentTags, language, requiredTimeslotTags, preferredTimeslotTags, prohibitedTimeslotTags,
                undesiredTimeslotTags, requiredRoomTags, preferredRoomTags, prohibitedRoomTags, undesiredRoomTags,
                mutuallyExclusiveTalksTags, prerequisiteTalkCodes, favoriteCount, crowdControlRisk, timeslotId, roomId);
    }

    public TalkDTO withProhibitedTimeslotTags(List<String> prohibitedTimeslotTags) {
        return new TalkDTO(code, title, talkTypeName, speakerIds, themeTrackTags, sectorTags, audienceTypes, audienceLevel,
                contentTags, language, requiredTimeslotTags, preferredTimeslotTags, prohibitedTimeslotTags,
                undesiredTimeslotTags, requiredRoomTags, preferredRoomTags, prohibitedRoomTags, undesiredRoomTags,
                mutuallyExclusiveTalksTags, prerequisiteTalkCodes, favoriteCount, crowdControlRisk, timeslotId, roomId);
    }

    public TalkDTO withUndesiredTimeslotTags(List<String> undesiredTimeslotTags) {
        return new TalkDTO(code, title, talkTypeName, speakerIds, themeTrackTags, sectorTags, audienceTypes, audienceLevel,
                contentTags, language, requiredTimeslotTags, preferredTimeslotTags, prohibitedTimeslotTags,
                undesiredTimeslotTags, requiredRoomTags, preferredRoomTags, prohibitedRoomTags, undesiredRoomTags,
                mutuallyExclusiveTalksTags, prerequisiteTalkCodes, favoriteCount, crowdControlRisk, timeslotId, roomId);
    }

    public TalkDTO withRequiredRoomTags(List<String> requiredRoomTags) {
        return new TalkDTO(code, title, talkTypeName, speakerIds, themeTrackTags, sectorTags, audienceTypes, audienceLevel,
                contentTags, language, requiredTimeslotTags, preferredTimeslotTags, prohibitedTimeslotTags,
                undesiredTimeslotTags, requiredRoomTags, preferredRoomTags, prohibitedRoomTags, undesiredRoomTags,
                mutuallyExclusiveTalksTags, prerequisiteTalkCodes, favoriteCount, crowdControlRisk, timeslotId, roomId);
    }

    public TalkDTO withPreferredRoomTags(List<String> preferredRoomTags) {
        return new TalkDTO(code, title, talkTypeName, speakerIds, themeTrackTags, sectorTags, audienceTypes, audienceLevel,
                contentTags, language, requiredTimeslotTags, preferredTimeslotTags, prohibitedTimeslotTags,
                undesiredTimeslotTags, requiredRoomTags, preferredRoomTags, prohibitedRoomTags, undesiredRoomTags,
                mutuallyExclusiveTalksTags, prerequisiteTalkCodes, favoriteCount, crowdControlRisk, timeslotId, roomId);
    }

    public TalkDTO withProhibitedRoomTags(List<String> prohibitedRoomTags) {
        return new TalkDTO(code, title, talkTypeName, speakerIds, themeTrackTags, sectorTags, audienceTypes, audienceLevel,
                contentTags, language, requiredTimeslotTags, preferredTimeslotTags, prohibitedTimeslotTags,
                undesiredTimeslotTags, requiredRoomTags, preferredRoomTags, prohibitedRoomTags, undesiredRoomTags,
                mutuallyExclusiveTalksTags, prerequisiteTalkCodes, favoriteCount, crowdControlRisk, timeslotId, roomId);
    }

    public TalkDTO withUndesiredRoomTags(List<String> undesiredRoomTags) {
        return new TalkDTO(code, title, talkTypeName, speakerIds, themeTrackTags, sectorTags, audienceTypes, audienceLevel,
                contentTags, language, requiredTimeslotTags, preferredTimeslotTags, prohibitedTimeslotTags,
                undesiredTimeslotTags, requiredRoomTags, preferredRoomTags, prohibitedRoomTags, undesiredRoomTags,
                mutuallyExclusiveTalksTags, prerequisiteTalkCodes, favoriteCount, crowdControlRisk, timeslotId, roomId);
    }

    public TalkDTO withMutuallyExclusiveTalksTags(List<String> mutuallyExclusiveTalksTags) {
        return new TalkDTO(code, title, talkTypeName, speakerIds, themeTrackTags, sectorTags, audienceTypes, audienceLevel,
                contentTags, language, requiredTimeslotTags, preferredTimeslotTags, prohibitedTimeslotTags,
                undesiredTimeslotTags, requiredRoomTags, preferredRoomTags, prohibitedRoomTags, undesiredRoomTags,
                mutuallyExclusiveTalksTags, prerequisiteTalkCodes, favoriteCount, crowdControlRisk, timeslotId, roomId);
    }

    public TalkDTO withPrerequisiteTalkCodes(List<String> prerequisiteTalkCodes) {
        return new TalkDTO(code, title, talkTypeName, speakerIds, themeTrackTags, sectorTags, audienceTypes, audienceLevel,
                contentTags, language, requiredTimeslotTags, preferredTimeslotTags, prohibitedTimeslotTags,
                undesiredTimeslotTags, requiredRoomTags, preferredRoomTags, prohibitedRoomTags, undesiredRoomTags,
                mutuallyExclusiveTalksTags, prerequisiteTalkCodes, favoriteCount, crowdControlRisk, timeslotId, roomId);
    }

    public TalkDTO withFavoriteCount(int favoriteCount) {
        return new TalkDTO(code, title, talkTypeName, speakerIds, themeTrackTags, sectorTags, audienceTypes, audienceLevel,
                contentTags, language, requiredTimeslotTags, preferredTimeslotTags, prohibitedTimeslotTags,
                undesiredTimeslotTags, requiredRoomTags, preferredRoomTags, prohibitedRoomTags, undesiredRoomTags,
                mutuallyExclusiveTalksTags, prerequisiteTalkCodes, favoriteCount, crowdControlRisk, timeslotId, roomId);
    }

    public TalkDTO withCrowdControlRisk(int crowdControlRisk) {
        return new TalkDTO(code, title, talkTypeName, speakerIds, themeTrackTags, sectorTags, audienceTypes, audienceLevel,
                contentTags, language, requiredTimeslotTags, preferredTimeslotTags, prohibitedTimeslotTags,
                undesiredTimeslotTags, requiredRoomTags, preferredRoomTags, prohibitedRoomTags, undesiredRoomTags,
                mutuallyExclusiveTalksTags, prerequisiteTalkCodes, favoriteCount, crowdControlRisk, timeslotId, roomId);
    }

    public TalkDTO withTimeslotId(String timeslotId) {
        return new TalkDTO(code, title, talkTypeName, speakerIds, themeTrackTags, sectorTags, audienceTypes, audienceLevel,
                contentTags, language, requiredTimeslotTags, preferredTimeslotTags, prohibitedTimeslotTags,
                undesiredTimeslotTags, requiredRoomTags, preferredRoomTags, prohibitedRoomTags, undesiredRoomTags,
                mutuallyExclusiveTalksTags, prerequisiteTalkCodes, favoriteCount, crowdControlRisk, timeslotId, roomId);
    }

    public TalkDTO withRoomId(String roomId) {
        return new TalkDTO(code, title, talkTypeName, speakerIds, themeTrackTags, sectorTags, audienceTypes, audienceLevel,
                contentTags, language, requiredTimeslotTags, preferredTimeslotTags, prohibitedTimeslotTags,
                undesiredTimeslotTags, requiredRoomTags, preferredRoomTags, prohibitedRoomTags, undesiredRoomTags,
                mutuallyExclusiveTalksTags, prerequisiteTalkCodes, favoriteCount, crowdControlRisk, timeslotId, roomId);
    }
}
