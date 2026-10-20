package org.acme.orderpicking.dto.input;

import static java.util.Collections.emptyList;

import java.util.List;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = """
        A trolley with buckets that an employee pushes through the warehouse. It starts at its location, walks past \
        its pick tasks in order and returns to its location. A bucket never holds products of two different orders.""")
public record TrolleyInputDTO(
        @Schema(description = "Unique identifier of the trolley.", required = true, minLength = 1) String id,
        @Schema(description = "Number of buckets on the trolley.", required = true, minimum = "1") int bucketCount,
        @Schema(description = "Volume of each bucket in cm3.", required = true, minimum = "1") int bucketCapacity,
        @Schema(description = "Place in the warehouse the trolley starts from and returns to.",
                required = true) WarehouseLocationInputDTO location,
        @Schema(description = """
                IDs of the pick tasks assigned to this trolley, in the order it picks them. Empty when the trolley \
                has no path yet. The solver assigns every pick task that appears in no trolley's path.""") List<String> pickTaskIds) {

    public TrolleyInputDTO {
        pickTaskIds = pickTaskIds != null ? pickTaskIds : emptyList();
    }

    public TrolleyInputDTO withPickTaskIds(List<String> pickTaskIds) {
        return new TrolleyInputDTO(id, bucketCount, bucketCapacity, location, pickTaskIds);
    }
}
