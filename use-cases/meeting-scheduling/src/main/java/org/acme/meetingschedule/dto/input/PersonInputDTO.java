package org.acme.meetingschedule.dto.input;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A person who can attend meetings.")
public record PersonInputDTO(
        @Schema(description = "Unique identifier of the person.", required = true, minLength = 1) String id,
        @Schema(description = "Full name of the person.", required = true, minLength = 1) String fullName) {
}
