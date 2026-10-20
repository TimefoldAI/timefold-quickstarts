package org.acme.orderpicking.dto.input;

import static java.util.Collections.emptyList;

import java.util.List;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = """
        A picking cart with buckets that an employee pushes through the warehouse. It starts at its location, walks past \
        its pick tasks in order and returns to its location. A bucket never holds products of two different orders.""")
public record PickingCartInputDTO(
        @Schema(description = "Unique identifier of the picking cart.", required = true, minLength = 1) String id,
        @Schema(description = "Number of buckets on the picking cart.", required = true, minimum = "1") int bucketCount,
        @Schema(description = "Volume of each bucket in cm3.", required = true, minimum = "1") int bucketCapacity,
        @Schema(description = "Place in the warehouse the picking cart starts from and returns to.",
                required = true) WarehouseLocationInputDTO location,
        @Schema(description = """
                IDs of the pick tasks assigned to this picking cart, in the order it picks them. Empty when the picking cart \
                has no path yet. The solver assigns every pick task that appears in no picking cart's path.""") List<String> pickTaskIds) {

    public PickingCartInputDTO {
        pickTaskIds = pickTaskIds != null ? pickTaskIds : emptyList();
    }

    public PickingCartInputDTO withPickTaskIds(List<String> pickTaskIds) {
        return new PickingCartInputDTO(id, bucketCount, bucketCapacity, location, pickTaskIds);
    }
}
