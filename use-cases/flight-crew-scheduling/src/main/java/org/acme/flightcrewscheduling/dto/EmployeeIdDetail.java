package org.acme.flightcrewscheduling.dto;

import ai.timefold.solver.service.definition.api.validation.IssueMetadata;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Details about an employee ID validation issue.")
public record EmployeeIdDetail(
        @Schema(description = "The ID of the employee.") String employeeId) implements IssueMetadata {

    public EmployeeIdDetail {
        employeeId = employeeId == null ? "" : employeeId;
    }

    public EmployeeIdDetail withEmployeeId(String employeeId) {
        return new EmployeeIdDetail(employeeId);
    }

    @Override
    public String getType() {
        return "EmployeeId";
    }
}
