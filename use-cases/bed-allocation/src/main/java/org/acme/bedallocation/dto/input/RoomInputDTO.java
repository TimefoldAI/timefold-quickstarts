package org.acme.bedallocation.dto.input;

import static java.util.Collections.emptyList;

import java.util.List;

import org.acme.bedallocation.domain.GenderLimitation;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A room containing one or more beds.")
public record RoomInputDTO(
        @Schema(description = "Unique identifier of the room.", required = true, minLength = 1) String id,
        @Schema(description = "Display name of the room.", required = true, minLength = 1) String name,
        @Schema(description = "Number of beds in the room.", required = true, minimum = "1") Integer capacity,
        @Schema(description = "Gender restriction of the room.", required = true) GenderLimitation genderLimitation,
        @Schema(description = "Medical equipment available in this room.") List<String> equipments,
        @Schema(description = "Beds in this room.", required = true, minItems = 1) List<BedInputDTO> beds) {

    public RoomInputDTO {
        equipments = equipments != null ? equipments : emptyList();
    }
}
