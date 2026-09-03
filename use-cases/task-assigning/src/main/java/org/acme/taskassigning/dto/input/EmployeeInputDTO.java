package org.acme.taskassigning.dto.input;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;

import java.util.List;
import java.util.Map;

import org.acme.taskassigning.domain.Affinity;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "An employee who can be assigned tasks.")
public record EmployeeInputDTO(
        @Schema(description = "Unique identifier of the employee.", required = true, minLength = 1) String id,
        @Schema(description = "Full name of the employee.", required = true, minLength = 1) String fullName,
        @Schema(description = "The skills this employee has.") List<String> skills,
        @Schema(description = "How well this employee gets along with each customer, keyed by customer ID. A "
                + "customer that is not a key defaults to no affinity.") Map<String, Affinity> customerAffinities,
        @Schema(description = "IDs of the tasks assigned to this employee, in the order they will be worked on, "
                + "or null if unassigned.") List<String> taskIds) {

    public EmployeeInputDTO {
        skills = skills != null ? skills : emptyList();
        customerAffinities = customerAffinities != null ? customerAffinities : emptyMap();
        taskIds = taskIds != null ? taskIds : emptyList();
    }

    public EmployeeInputDTO withTaskIds(List<String> taskIds) {
        return new EmployeeInputDTO(id, fullName, skills, customerAffinities, taskIds);
    }
}
