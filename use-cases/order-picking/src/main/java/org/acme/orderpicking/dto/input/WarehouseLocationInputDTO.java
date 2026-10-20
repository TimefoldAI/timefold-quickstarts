package org.acme.orderpicking.dto.input;

import org.acme.orderpicking.domain.RackSide;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = """
        A place in the warehouse: one side of a rack, at one bay. The warehouse has five columns (A to E) and \
        three rows (1 to 3) of racks.""")
public record WarehouseLocationInputDTO(
        @Schema(description = """
                ID of the rack: its column letter followed by its row number, from A1 to E3.""", required = true,
                minLength = 1) String rackId,
        @Schema(description = "Side of the rack, which determines the aisle to walk.", required = true) RackSide side,
        @Schema(description = """
                Bay on that side of the rack, from 0 at the top to 10 at the bottom. A bay is one meter long.""",
                required = true, minimum = "0", maximum = "10") int bay) {
}
