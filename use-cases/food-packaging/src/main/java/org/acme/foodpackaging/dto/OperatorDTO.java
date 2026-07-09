package org.acme.foodpackaging.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "An operator that can be assigned to a packaging line.")
public record OperatorDTO(
        @Schema(description = "Unique identifier of the operator.") String id) {

    @SuppressWarnings("PMD.NullAssignment")
    public OperatorDTO {
        id = id != null && id.isBlank() ? null : id;
    }

    public OperatorDTO withId(String id) {
        return new OperatorDTO(id);
    }
}
