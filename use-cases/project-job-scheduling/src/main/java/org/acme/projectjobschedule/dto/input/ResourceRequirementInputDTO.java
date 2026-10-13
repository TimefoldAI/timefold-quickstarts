package org.acme.projectjobschedule.dto.input;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "How much of a resource an execution mode consumes.")
public record ResourceRequirementInputDTO(
        @Schema(description = "ID of the required resource.", required = true, minLength = 1) String resourceId,
        @Schema(description = "Units of that resource consumed while the job executes.", required = true,
                minimum = "1") Integer requirement) {
}
