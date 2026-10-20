package org.acme.orderpicking.dto.input;

import org.acme.orderpicking.domain.ShelvingSide;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = """
        A place in the warehouse: one side of a shelving, at one row. The warehouse has five columns (A to E) and \
        three rows (1 to 3) of shelvings.""")
public record WarehouseLocationInputDTO(
        @Schema(description = """
                ID of the shelving: its column letter followed by its row number, from A1 to E3.""", required = true,
                minLength = 1) String shelvingId,
        @Schema(description = "Side of the shelving, which determines the aisle to walk.", required = true) ShelvingSide side,
        @Schema(description = """
                Row on that side of the shelving, from 0 at the top to 10 at the bottom. A row is one meter high.""",
                required = true, minimum = "0", maximum = "10") int row) {
}
