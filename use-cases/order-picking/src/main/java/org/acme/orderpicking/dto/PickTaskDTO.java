package org.acme.orderpicking.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A pick task represents the picking of a single order item from its warehouse location.")
public record PickTaskDTO(
        @Schema(description = "Unique identifier of the pick task.") String id,
        @Schema(description = "Identifier of the order the picked item belongs to.") String orderId,
        @Schema(description = "Identifier of the product to be picked.") String productId,
        @Schema(description = "Display name of the product to be picked.") String productName,
        @Schema(description = "Volume of the product in cm3.") int productVolume,
        @Schema(description = "Warehouse location where the product is stored.") WarehouseLocationDTO location) {

    public PickTaskDTO {
        id = id == null ? "" : id;
        orderId = orderId == null ? "" : orderId;
        productId = productId == null ? "" : productId;
        productName = productName == null ? "" : productName;
    }

    public PickTaskDTO withId(String id) {
        return new PickTaskDTO(id, orderId, productId, productName, productVolume, location);
    }

    public PickTaskDTO withOrderId(String orderId) {
        return new PickTaskDTO(id, orderId, productId, productName, productVolume, location);
    }

    public PickTaskDTO withProductId(String productId) {
        return new PickTaskDTO(id, orderId, productId, productName, productVolume, location);
    }

    public PickTaskDTO withProductName(String productName) {
        return new PickTaskDTO(id, orderId, productId, productName, productVolume, location);
    }

    public PickTaskDTO withProductVolume(int productVolume) {
        return new PickTaskDTO(id, orderId, productId, productName, productVolume, location);
    }

    public PickTaskDTO withLocation(WarehouseLocationDTO location) {
        return new PickTaskDTO(id, orderId, productId, productName, productVolume, location);
    }
}
