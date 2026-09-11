package org.acme.taskassigning.dto.input;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A customer whose tasks need to be handled.")
public record CustomerInputDTO(
        @Schema(description = "Unique identifier of the customer.", required = true, minLength = 1) String id,
        @Schema(description = "Name of the customer.", required = true, minLength = 1) String name) {
}
