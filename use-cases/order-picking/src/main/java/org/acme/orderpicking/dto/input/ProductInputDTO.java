package org.acme.orderpicking.dto.input;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "An indivisible store article that can be included in an order, e.g. a pack of 6 milk bricks.")
public record ProductInputDTO(
        @Schema(description = "Unique identifier of the product.", required = true, minLength = 1) String id,
        @Schema(description = "Human-readable name of the product.", required = true, minLength = 1) String name,
        @Schema(description = """
                Volume of the product in cm3. It has to fit in a single picking cart bucket.""", required = true,
                minimum = "1") int volume,
        @Schema(description = "Place in the warehouse the product is stored.",
                required = true) WarehouseLocationInputDTO location) {
}
