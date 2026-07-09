package org.acme.taskassigning.dto;

import java.util.List;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "An employee that can be assigned an ordered list of tasks.")
public record EmployeeDTO(
        @Schema(description = "Unique identifier of the employee.") String id,
        @Schema(description = "Full name of the employee.") String fullName,
        @Schema(description = "Skills the employee possesses.") List<String> skills,
        @Schema(description = "Affinities of the employee with the customers.") List<CustomerAffinityDTO> affinities,
        @Schema(description = "Ordered list of task IDs assigned to this employee.") List<String> taskIds) {

    public EmployeeDTO {
        id = id == null ? "" : id;
        fullName = fullName == null ? "" : fullName;
        skills = List.copyOf(skills);
        affinities = List.copyOf(affinities);
        taskIds = List.copyOf(taskIds);
    }

    public EmployeeDTO withId(String id) {
        return new EmployeeDTO(id, fullName, skills, affinities, taskIds);
    }

    public EmployeeDTO withFullName(String fullName) {
        return new EmployeeDTO(id, fullName, skills, affinities, taskIds);
    }

    public EmployeeDTO withSkills(List<String> skills) {
        return new EmployeeDTO(id, fullName, skills, affinities, taskIds);
    }

    public EmployeeDTO withAffinities(List<CustomerAffinityDTO> affinities) {
        return new EmployeeDTO(id, fullName, skills, affinities, taskIds);
    }

    public EmployeeDTO withTaskIds(List<String> taskIds) {
        return new EmployeeDTO(id, fullName, skills, affinities, taskIds);
    }
}
