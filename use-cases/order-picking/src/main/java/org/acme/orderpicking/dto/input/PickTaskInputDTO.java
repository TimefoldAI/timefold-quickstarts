package org.acme.orderpicking.dto.input;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = """
        The picking of one order item: one product that was added to an order. The solver assigns it to a trolley, \
        at a position in that trolley's path.""")
public record PickTaskInputDTO(
        @Schema(description = "Unique identifier of the pick task.", required = true, minLength = 1) String id,
        @Schema(description = """
                ID of the order the item belongs to. Pick tasks with the same order ID are preferably picked by the \
                same trolley, and share its buckets.""", required = true, minLength = 1) String orderId,
        @Schema(description = "ID of the product to pick.", required = true, minLength = 1) String productId) {
}
