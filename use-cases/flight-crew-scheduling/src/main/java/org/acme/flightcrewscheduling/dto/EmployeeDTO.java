package org.acme.flightcrewscheduling.dto;

import java.util.List;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "An employee who can be assigned to flights as a crew member.")
public record EmployeeDTO(
        @Schema(description = "Unique identifier of the employee.") String id,
        @Schema(description = "Display name of the employee.") String name,
        @Schema(description = "ID of the airport the employee is based at.") String homeAirportId,
        @Schema(description = "Skills the employee holds, e.g. Pilot or Flight attendant.") List<String> skills,
        @Schema(description = "Days, in ISO-8601 format (yyyy-MM-dd), on which the employee is unavailable.") //
        List<String> unavailableDays) {

    public EmployeeDTO {
        homeAirportId = normalizeId(homeAirportId);
        name = name == null ? "" : name;
        skills = skills == null ? List.of() : List.copyOf(skills);
        unavailableDays = unavailableDays == null ? List.of() : List.copyOf(unavailableDays);
    }

    private static String normalizeId(String id) {
        return id != null && id.isBlank() ? null : id;
    }

    public EmployeeDTO withId(String id) {
        return new EmployeeDTO(id, name, homeAirportId, skills, unavailableDays);
    }

    public EmployeeDTO withName(String name) {
        return new EmployeeDTO(id, name, homeAirportId, skills, unavailableDays);
    }

    public EmployeeDTO withHomeAirportId(String homeAirportId) {
        return new EmployeeDTO(id, name, homeAirportId, skills, unavailableDays);
    }

    public EmployeeDTO withSkills(List<String> skills) {
        return new EmployeeDTO(id, name, homeAirportId, skills, unavailableDays);
    }

    public EmployeeDTO withUnavailableDays(List<String> unavailableDays) {
        return new EmployeeDTO(id, name, homeAirportId, skills, unavailableDays);
    }
}
