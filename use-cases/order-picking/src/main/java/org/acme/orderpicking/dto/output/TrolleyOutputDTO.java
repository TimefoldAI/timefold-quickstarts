package org.acme.orderpicking.dto.output;

import java.util.List;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.ALWAYS)
@Schema(description = "A trolley with the path it walks through the warehouse.")
public record TrolleyOutputDTO(
        @Schema(description = "Unique identifier of the trolley.", required = true, minLength = 1) String id,
        @Schema(description = """
                IDs of the pick tasks assigned to this trolley, in the order it picks them. Empty when the trolley \
                is not used.""", required = true) List<String> pickTaskIds,
        @Schema(description = """
                The distance in meters the trolley walks: from its start location past every pick task, and back \
                again.""", required = true) int travelDistance) {
}
