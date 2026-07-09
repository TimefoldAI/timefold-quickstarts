package org.acme.employeescheduling.dto;

import java.util.List;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "An employee who can be assigned to shifts.")
public record EmployeeDTO(
        @Schema(description = "Unique identifier of the employee.") String id,
        @Schema(description = "Set of skills the employee possesses.") List<String> skills,
        @Schema(description = "Dates on which the employee is unavailable, in ISO-8601 format (yyyy-MM-dd).") List<String> unavailableDates,
        @Schema(description = "Dates on which the employee prefers not to work, in ISO-8601 format (yyyy-MM-dd).") List<String> undesiredDates,
        @Schema(description = "Dates on which the employee prefers to work, in ISO-8601 format (yyyy-MM-dd).") List<String> desiredDates) {

    public EmployeeDTO {
        id = id == null ? "" : id;
        skills = skills == null ? List.of() : List.copyOf(skills);
        unavailableDates = unavailableDates == null ? List.of() : List.copyOf(unavailableDates);
        undesiredDates = undesiredDates == null ? List.of() : List.copyOf(undesiredDates);
        desiredDates = desiredDates == null ? List.of() : List.copyOf(desiredDates);
    }

    public EmployeeDTO withId(String id) {
        return new EmployeeDTO(id, skills, unavailableDates, undesiredDates, desiredDates);
    }

    public EmployeeDTO withSkills(List<String> skills) {
        return new EmployeeDTO(id, skills, unavailableDates, undesiredDates, desiredDates);
    }

    public EmployeeDTO withUnavailableDates(List<String> unavailableDates) {
        return new EmployeeDTO(id, skills, unavailableDates, undesiredDates, desiredDates);
    }

    public EmployeeDTO withUndesiredDates(List<String> undesiredDates) {
        return new EmployeeDTO(id, skills, unavailableDates, undesiredDates, desiredDates);
    }

    public EmployeeDTO withDesiredDates(List<String> desiredDates) {
        return new EmployeeDTO(id, skills, unavailableDates, undesiredDates, desiredDates);
    }
}
