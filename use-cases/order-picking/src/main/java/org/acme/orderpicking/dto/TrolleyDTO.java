package org.acme.orderpicking.dto;

import java.util.List;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A trolley that travels the warehouse picking the assigned order items.")
public record TrolleyDTO(
        @Schema(description = "Unique identifier of the trolley.") String id,
        @Schema(description = "Number of buckets available on the trolley.") int bucketCount,
        @Schema(description = "Capacity in cm3 of each bucket of the trolley.") int bucketCapacity,
        @Schema(description = "Warehouse location where the trolley starts and ends its path.") WarehouseLocationDTO location,
        @Schema(description = "Ordered list of pick task IDs assigned to this trolley.") List<String> pickTaskIds) {

    public TrolleyDTO {
        id = id == null ? "" : id;
        pickTaskIds = List.copyOf(pickTaskIds);
    }

    public TrolleyDTO withId(String id) {
        return new TrolleyDTO(id, bucketCount, bucketCapacity, location, pickTaskIds);
    }

    public TrolleyDTO withBucketCount(int bucketCount) {
        return new TrolleyDTO(id, bucketCount, bucketCapacity, location, pickTaskIds);
    }

    public TrolleyDTO withBucketCapacity(int bucketCapacity) {
        return new TrolleyDTO(id, bucketCount, bucketCapacity, location, pickTaskIds);
    }

    public TrolleyDTO withLocation(WarehouseLocationDTO location) {
        return new TrolleyDTO(id, bucketCount, bucketCapacity, location, pickTaskIds);
    }

    public TrolleyDTO withPickTaskIds(List<String> pickTaskIds) {
        return new TrolleyDTO(id, bucketCount, bucketCapacity, location, pickTaskIds);
    }
}
