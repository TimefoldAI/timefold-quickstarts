package org.acme.conferencescheduling.dto.input;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;

@Schema(description = "A talk to be assigned to a timeslot and a room.")
public record TalkDTO(
        @Schema(description = "Unique code of the talk.") @NotBlank String code,
        @Schema(description = "Title of the talk.") @NotBlank String title,
        @Schema(description = "Name of the talk type of this talk.") @NotBlank String talkTypeName,
        @Schema(description = "IDs of the speakers presenting this talk.") @NotEmpty List<String> speakerIds,
        @Schema(description = "Theme track tags of this talk.") @JsonSetter(nulls = Nulls.AS_EMPTY) List<String> themeTrackTags,
        @Schema(description = "Sector tags of this talk.") @JsonSetter(nulls = Nulls.AS_EMPTY) List<String> sectorTags,
        @Schema(description = "Content tags of this talk.") @JsonSetter(nulls = Nulls.AS_EMPTY) List<String> contentTags,
        @Schema(description = "Audience types of this talk.") @JsonSetter(nulls = Nulls.AS_EMPTY) List<String> audienceTypes,
        @Schema(description = "Audience level of this talk, a low number means for beginners.") @Min(0) int audienceLevel,
        @Schema(description = "Language in which this talk is presented.") String language,
        @Schema(description = "Timeslot tag preferences of this talk.") @NotNull @Valid TagPreferencesDTO timeslotTags,
        @Schema(description = "Room tag preferences of this talk.") @NotNull @Valid TagPreferencesDTO roomTags,
        @Schema(description = "Tags shared by talks that must not be scheduled at overlapping times.") @JsonSetter(
                nulls = Nulls.AS_EMPTY) List<String> mutuallyExclusiveTalksTags,
        @Schema(description = "Codes of the talks that must be scheduled before this talk.") @JsonSetter(
                nulls = Nulls.AS_EMPTY) List<String> prerequisiteTalkCodes,
        @Schema(description = "Number of attendees who marked this talk as favorite.") @Min(0) int favoriteCount,
        @Schema(description = "Crowd control risk level of this talk.") @Min(0) int crowdControlRisk,
        @Schema(description = "ID of the timeslot this talk is assigned to, or null if unassigned.") String timeslotId,
        @Schema(description = "ID of the room this talk is assigned to, or null if unassigned.") String roomId) {

    public TalkDTO withAssignment(String roomId, String timeslotId) {
        return new TalkDTO(code, title, talkTypeName, speakerIds, themeTrackTags, sectorTags, contentTags,
                audienceTypes, audienceLevel, language, timeslotTags, roomTags, mutuallyExclusiveTalksTags,
                prerequisiteTalkCodes, favoriteCount, crowdControlRisk, timeslotId, roomId);
    }
}
