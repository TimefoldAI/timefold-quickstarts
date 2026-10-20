package org.acme.orderpicking.dto.output;

import java.util.List;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.ALWAYS)
@Schema(description = "A picking cart with the path it walks through the warehouse.")
public record PickingCartOutputDTO(
        @Schema(description = "Unique identifier of the picking cart.", required = true, minLength = 1) String id,
        @Schema(description = """
                IDs of the pick tasks assigned to this picking cart, in the order it picks them. Empty when the picking cart \
                is not used.""", required = true) List<String> pickTaskIds,
        @Schema(description = """
                The distance in meters the picking cart walks: from its start location past every pick task, and back \
                again.""", required = true) int travelDistance) {
}
