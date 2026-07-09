package org.acme.orderpicking.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A location in the warehouse identified by a shelving, a side and a row.")
public record WarehouseLocationDTO(
        @Schema(description = "Identifier of the shelving where the location is placed.") String shelvingId,
        @Schema(description = "Side of the shelving (LEFT or RIGHT) where the location is placed.") String side,
        @Schema(description = "Row of the shelving where the location is placed.") int row) {

    public WarehouseLocationDTO {
        shelvingId = shelvingId == null ? "" : shelvingId;
        side = side == null ? "" : side;
    }

    public WarehouseLocationDTO withShelvingId(String shelvingId) {
        return new WarehouseLocationDTO(shelvingId, side, row);
    }

    public WarehouseLocationDTO withSide(String side) {
        return new WarehouseLocationDTO(shelvingId, side, row);
    }

    public WarehouseLocationDTO withRow(int row) {
        return new WarehouseLocationDTO(shelvingId, side, row);
    }
}
