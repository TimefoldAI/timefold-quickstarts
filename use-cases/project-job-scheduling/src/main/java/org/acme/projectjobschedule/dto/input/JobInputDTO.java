package org.acme.projectjobschedule.dto.input;

import static java.util.Collections.emptyList;

import java.util.List;

import org.acme.projectjobschedule.domain.JobType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A unit of work within a project, executed in exactly one of its execution modes.")
public record JobInputDTO(
        @Schema(description = "Unique identifier of the job.", required = true, minLength = 1) String id,
        @Schema(description = "ID of the project this job belongs to.", required = true, minLength = 1) String projectId,
        @Schema(description = "Whether this job starts its project (SOURCE), ends it (SINK) or is real work "
                + "(STANDARD). Every project needs exactly one SOURCE and one SINK job.",
                required = true) JobType jobType,
        @Schema(description = "The ways this job can be executed; the solver picks one of them. A SOURCE or SINK "
                + "job has exactly one, of zero duration.", required = true,
                minItems = 1) List<ExecutionModeInputDTO> executionModes,
        @Schema(description = "IDs of the jobs that can only start once this job has finished.") List<String> successorJobIds) {

    public JobInputDTO {
        successorJobIds = successorJobIds != null ? successorJobIds : emptyList();
    }
}
