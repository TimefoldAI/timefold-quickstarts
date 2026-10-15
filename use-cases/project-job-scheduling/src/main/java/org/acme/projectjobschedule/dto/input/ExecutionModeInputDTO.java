package org.acme.projectjobschedule.dto.input;

import static java.util.Collections.emptyList;

import java.util.List;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "One way of executing a job: how long it takes and what it consumes while it runs.")
public record ExecutionModeInputDTO(
        @Schema(description = "Unique identifier of the execution mode.", required = true, minLength = 1) String id,
        @Schema(description = "Number of days the job takes in this execution mode.", required = true,
                minimum = "0") Integer duration,
        @Schema(description = "Resources this execution mode consumes; empty when it consumes none.") List<ResourceRequirementInputDTO> resourceRequirements) {

    public ExecutionModeInputDTO {
        resourceRequirements = resourceRequirements != null ? resourceRequirements : emptyList();
    }
}
