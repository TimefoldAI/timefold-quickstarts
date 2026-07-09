package org.acme.foodpackaging.dto;

import java.util.Map;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A product that can be packaged, with the cleaning time required after each possible previous product.")
public record ProductDTO(
        @Schema(description = "Unique identifier of the product.") String id,
        @Schema(description = "Display name of the product.") String name,
        @Schema(description = "Cleaning duration in minutes, keyed by the previous product ID on the line.") Map<String, Long> cleaningDurations) {

    public ProductDTO {
        cleaningDurations = cleaningDurations == null ? Map.of() : Map.copyOf(cleaningDurations);
    }

    public ProductDTO withId(String id) {
        return new ProductDTO(id, name, cleaningDurations);
    }

    public ProductDTO withName(String name) {
        return new ProductDTO(id, name, cleaningDurations);
    }

    public ProductDTO withCleaningDurations(Map<String, Long> cleaningDurations) {
        return new ProductDTO(id, name, cleaningDurations);
    }
}
