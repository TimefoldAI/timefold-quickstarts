package org.acme.bedallocation.dto;

import java.util.List;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A room containing one or more beds.")
public record RoomDTO(
        @Schema(description = "Unique identifier of the room.", required = true) String id,
        @Schema(description = "Display name of the room.", required = true) String name,
        @Schema(description = "Number of beds in the room.", required = true) int capacity,
        @Schema(description = "Gender restriction of the room: ANY_GENDER, MALE_ONLY, FEMALE_ONLY or SAME_GENDER.",
                required = true) String genderLimitation,
        @Schema(description = "Medical equipment available in this room.") List<String> equipments,
        @Schema(description = "Beds in this room.", required = true) List<BedDTO> beds) {

    public RoomDTO {
        beds = beds == null ? List.of() : beds;
    }
}
