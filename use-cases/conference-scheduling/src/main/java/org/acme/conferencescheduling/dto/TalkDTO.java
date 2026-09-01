package org.acme.conferencescheduling.dto;

import static org.acme.conferencescheduling.support.ObjectHelper.immutableCopy;

import java.util.List;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A talk to be assigned to a timeslot and a room.")
public record TalkDTO(
        @Schema(description = "Unique code of the talk.", required = true) String code,
        @Schema(description = "Title of the talk.", required = true) String title,
        @Schema(description = "Name of the talk type of this talk.", required = true) String talkTypeName,
        @Schema(description = "IDs of the speakers presenting this talk.", required = true) List<String> speakerIds,
        @Schema(description = "Theme track tags of this talk.") List<String> themeTrackTags,
        @Schema(description = "Sector tags of this talk.") List<String> sectorTags,
        @Schema(description = "Audience types of this talk.") List<String> audienceTypes,
        @Schema(description = "Audience level of this talk, a low number means for beginners.") int audienceLevel,
        @Schema(description = "Content tags of this talk.") List<String> contentTags,
        @Schema(description = "Language in which this talk is presented.") String language,
        @Schema(description = "Timeslot tags required by this talk.") List<String> requiredTimeslotTags,
        @Schema(description = "Timeslot tags preferred by this talk.") List<String> preferredTimeslotTags,
        @Schema(description = "Timeslot tags prohibited by this talk.") List<String> prohibitedTimeslotTags,
        @Schema(description = "Timeslot tags undesired by this talk.") List<String> undesiredTimeslotTags,
        @Schema(description = "Room tags required by this talk.") List<String> requiredRoomTags,
        @Schema(description = "Room tags preferred by this talk.") List<String> preferredRoomTags,
        @Schema(description = "Room tags prohibited by this talk.") List<String> prohibitedRoomTags,
        @Schema(description = "Room tags undesired by this talk.") List<String> undesiredRoomTags,
        @Schema(description = "Tags shared by talks that must not be scheduled at overlapping times.") List<String> mutuallyExclusiveTalksTags,
        @Schema(description = "Codes of the talks that must be scheduled before this talk.") List<String> prerequisiteTalkCodes,
        @Schema(description = "Number of attendees who marked this talk as favorite.") int favoriteCount,
        @Schema(description = "Crowd control risk level of this talk.") int crowdControlRisk,
        @Schema(description = "ID of the timeslot this talk is assigned to, or null if unassigned.") String timeslotId,
        @Schema(description = "ID of the room this talk is assigned to, or null if unassigned.") String roomId) {

    public TalkDTO {
        speakerIds = immutableCopy(speakerIds);
        themeTrackTags = immutableCopy(themeTrackTags);
        sectorTags = immutableCopy(sectorTags);
        audienceTypes = immutableCopy(audienceTypes);
        contentTags = immutableCopy(contentTags);
        requiredTimeslotTags = immutableCopy(requiredTimeslotTags);
        preferredTimeslotTags = immutableCopy(preferredTimeslotTags);
        prohibitedTimeslotTags = immutableCopy(prohibitedTimeslotTags);
        undesiredTimeslotTags = immutableCopy(undesiredTimeslotTags);
        requiredRoomTags = immutableCopy(requiredRoomTags);
        preferredRoomTags = immutableCopy(preferredRoomTags);
        prohibitedRoomTags = immutableCopy(prohibitedRoomTags);
        undesiredRoomTags = immutableCopy(undesiredRoomTags);
        mutuallyExclusiveTalksTags = immutableCopy(mutuallyExclusiveTalksTags);
        prerequisiteTalkCodes = immutableCopy(prerequisiteTalkCodes);
        timeslotId = normalizeId(timeslotId);
        roomId = normalizeId(roomId);
    }

    private static String normalizeId(String id) {
        return id != null && id.isBlank() ? null : id;
    }

    public TalkDTO withTimeslotId(String timeslotId) {
        return toBuilder().timeslotId(timeslotId).build();
    }

    public TalkDTO withRoomId(String roomId) {
        return toBuilder().roomId(roomId).build();
    }

    public static Builder builder(String code, String title, String talkTypeName) {
        return new Builder(code, title, talkTypeName);
    }

    public Builder toBuilder() {
        return new Builder(code, title, talkTypeName)
                .speakerIds(speakerIds)
                .themeTrackTags(themeTrackTags)
                .sectorTags(sectorTags)
                .audienceTypes(audienceTypes)
                .audienceLevel(audienceLevel)
                .contentTags(contentTags)
                .language(language)
                .requiredTimeslotTags(requiredTimeslotTags)
                .preferredTimeslotTags(preferredTimeslotTags)
                .prohibitedTimeslotTags(prohibitedTimeslotTags)
                .undesiredTimeslotTags(undesiredTimeslotTags)
                .requiredRoomTags(requiredRoomTags)
                .preferredRoomTags(preferredRoomTags)
                .prohibitedRoomTags(prohibitedRoomTags)
                .undesiredRoomTags(undesiredRoomTags)
                .mutuallyExclusiveTalksTags(mutuallyExclusiveTalksTags)
                .prerequisiteTalkCodes(prerequisiteTalkCodes)
                .favoriteCount(favoriteCount)
                .crowdControlRisk(crowdControlRisk)
                .timeslotId(timeslotId)
                .roomId(roomId);
    }

    public static final class Builder {

        private final String code;
        private final String title;
        private final String talkTypeName;
        private List<String> speakerIds;
        private List<String> themeTrackTags;
        private List<String> sectorTags;
        private List<String> audienceTypes;
        private int audienceLevel;
        private List<String> contentTags;
        private String language;
        private List<String> requiredTimeslotTags;
        private List<String> preferredTimeslotTags;
        private List<String> prohibitedTimeslotTags;
        private List<String> undesiredTimeslotTags;
        private List<String> requiredRoomTags;
        private List<String> preferredRoomTags;
        private List<String> prohibitedRoomTags;
        private List<String> undesiredRoomTags;
        private List<String> mutuallyExclusiveTalksTags;
        private List<String> prerequisiteTalkCodes;
        private int favoriteCount;
        private int crowdControlRisk;
        private String timeslotId;
        private String roomId;

        private Builder(String code, String title, String talkTypeName) {
            this.code = code;
            this.title = title;
            this.talkTypeName = talkTypeName;
        }

        public Builder speakerIds(List<String> speakerIds) {
            this.speakerIds = speakerIds;
            return this;
        }

        public Builder themeTrackTags(List<String> themeTrackTags) {
            this.themeTrackTags = themeTrackTags;
            return this;
        }

        public Builder sectorTags(List<String> sectorTags) {
            this.sectorTags = sectorTags;
            return this;
        }

        public Builder audienceTypes(List<String> audienceTypes) {
            this.audienceTypes = audienceTypes;
            return this;
        }

        public Builder audienceLevel(int audienceLevel) {
            this.audienceLevel = audienceLevel;
            return this;
        }

        public Builder contentTags(List<String> contentTags) {
            this.contentTags = contentTags;
            return this;
        }

        public Builder language(String language) {
            this.language = language;
            return this;
        }

        public Builder requiredTimeslotTags(List<String> requiredTimeslotTags) {
            this.requiredTimeslotTags = requiredTimeslotTags;
            return this;
        }

        public Builder preferredTimeslotTags(List<String> preferredTimeslotTags) {
            this.preferredTimeslotTags = preferredTimeslotTags;
            return this;
        }

        public Builder prohibitedTimeslotTags(List<String> prohibitedTimeslotTags) {
            this.prohibitedTimeslotTags = prohibitedTimeslotTags;
            return this;
        }

        public Builder undesiredTimeslotTags(List<String> undesiredTimeslotTags) {
            this.undesiredTimeslotTags = undesiredTimeslotTags;
            return this;
        }

        public Builder requiredRoomTags(List<String> requiredRoomTags) {
            this.requiredRoomTags = requiredRoomTags;
            return this;
        }

        public Builder preferredRoomTags(List<String> preferredRoomTags) {
            this.preferredRoomTags = preferredRoomTags;
            return this;
        }

        public Builder prohibitedRoomTags(List<String> prohibitedRoomTags) {
            this.prohibitedRoomTags = prohibitedRoomTags;
            return this;
        }

        public Builder undesiredRoomTags(List<String> undesiredRoomTags) {
            this.undesiredRoomTags = undesiredRoomTags;
            return this;
        }

        public Builder mutuallyExclusiveTalksTags(List<String> mutuallyExclusiveTalksTags) {
            this.mutuallyExclusiveTalksTags = mutuallyExclusiveTalksTags;
            return this;
        }

        public Builder prerequisiteTalkCodes(List<String> prerequisiteTalkCodes) {
            this.prerequisiteTalkCodes = prerequisiteTalkCodes;
            return this;
        }

        public Builder favoriteCount(int favoriteCount) {
            this.favoriteCount = favoriteCount;
            return this;
        }

        public Builder crowdControlRisk(int crowdControlRisk) {
            this.crowdControlRisk = crowdControlRisk;
            return this;
        }

        public Builder timeslotId(String timeslotId) {
            this.timeslotId = timeslotId;
            return this;
        }

        public Builder roomId(String roomId) {
            this.roomId = roomId;
            return this;
        }

        public TalkDTO build() {
            return new TalkDTO(code, title, talkTypeName, speakerIds, themeTrackTags, sectorTags, audienceTypes,
                    audienceLevel, contentTags, language, requiredTimeslotTags, preferredTimeslotTags,
                    prohibitedTimeslotTags, undesiredTimeslotTags, requiredRoomTags, preferredRoomTags,
                    prohibitedRoomTags, undesiredRoomTags, mutuallyExclusiveTalksTags, prerequisiteTalkCodes,
                    favoriteCount, crowdControlRisk, timeslotId, roomId);
        }
    }
}
