package org.acme.bedallocation.dto;

import java.util.List;
import java.util.Set;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.acme.bedallocation.domain.GenderLimitation;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A room containing one or more beds.")
public record RoomDTO(
        @Schema(description = "Unique identifier of the room.") @NotBlank String id,
        @Schema(description = "Display name of the room.") @NotBlank String name,
        @Schema(description = "Number of beds in the room.") @NotNull @Min(1) Integer capacity,
        @Schema(description = "Gender restriction of the room: ANY_GENDER, MALE_ONLY, FEMALE_ONLY or SAME_GENDER.") @NotNull @Valid GenderLimitation genderLimitation,
        @Schema(description = "Medical equipment available in this room.") Set<String> equipments,
        @Schema(description = "Beds in this room.", required = true) List<@Valid BedDTO> beds) {
}
