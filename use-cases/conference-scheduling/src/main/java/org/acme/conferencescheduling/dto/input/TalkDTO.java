package org.acme.conferencescheduling.dto.input;

import static java.util.Collections.emptyList;

import java.util.List;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A talk to be assigned to a timeslot and a room.")
public record TalkDTO(
        @Schema(description = "Unique code of the talk.", required = true, minLength = 1) String code,
        @Schema(description = "Title of the talk.", required = true, minLength = 1) String title,
        @Schema(description = "Name of the talk type of this talk.", required = true, minLength = 1) String talkTypeName,
        @Schema(description = "IDs of the speakers presenting this talk.", required = true,
                minItems = 1) List<String> speakerIds,
        @Schema(description = "Theme track tags of this talk.") List<String> themeTrackTags,
        @Schema(description = "Sector tags of this talk.") List<String> sectorTags,
        @Schema(description = "Content tags of this talk.") List<String> contentTags,
        @Schema(description = "Audience types of this talk.") List<String> audienceTypes,
        @Schema(description = "Audience level of this talk, a low number means for beginners.",
                minimum = "0") int audienceLevel,
        @Schema(description = "Language in which this talk is presented.") String language,
        @Schema(description = "Timeslot tag preferences of this talk.", required = true) TagPreferencesDTO timeslotTags,
        @Schema(description = "Room tag preferences of this talk.", required = true) TagPreferencesDTO roomTags,
        @Schema(description = "Tags shared by talks that must not be scheduled at overlapping times.") List<String> mutuallyExclusiveTalksTags,
        @Schema(description = "Codes of the talks that must be scheduled before this talk.") List<String> prerequisiteTalkCodes,
        @Schema(description = "Number of attendees who marked this talk as favorite.", minimum = "0") int favoriteCount,
        @Schema(description = "Crowd control risk level of this talk.", minimum = "0") int crowdControlRisk,
        @Schema(description = "ID of the timeslot this talk is assigned to, or null if unassigned.",
                minLength = 1) String timeslotId,
        @Schema(description = "ID of the room this talk is assigned to, or null if unassigned.", minLength = 1) String roomId) {

    public TalkDTO {
        themeTrackTags = themeTrackTags != null ? themeTrackTags : emptyList();
        sectorTags = sectorTags != null ? sectorTags : emptyList();
        contentTags = contentTags != null ? contentTags : emptyList();
        audienceTypes = audienceTypes != null ? audienceTypes : emptyList();
        mutuallyExclusiveTalksTags = mutuallyExclusiveTalksTags != null ? mutuallyExclusiveTalksTags : emptyList();
        prerequisiteTalkCodes = prerequisiteTalkCodes != null ? prerequisiteTalkCodes : emptyList();
    }

    public TalkDTO withAssignment(String roomId, String timeslotId) {
        return new TalkDTO(code, title, talkTypeName, speakerIds, themeTrackTags, sectorTags, contentTags,
                audienceTypes, audienceLevel, language, timeslotTags, roomTags, mutuallyExclusiveTalksTags,
                prerequisiteTalkCodes, favoriteCount, crowdControlRisk, timeslotId, roomId);
    }
}
