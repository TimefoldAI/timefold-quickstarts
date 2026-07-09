package org.acme.employeescheduling.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;

import ai.timefold.solver.service.definition.api.domain.ModelConfig;
import ai.timefold.solver.service.definition.api.validation.ModelValidator;
import ai.timefold.solver.service.definition.api.validation.ValidationBuilder;

import org.acme.employeescheduling.dto.EmployeeDTO;
import org.acme.employeescheduling.dto.EmployeeIdDetail;
import org.acme.employeescheduling.dto.EmployeeScheduleConfigOverrides;
import org.acme.employeescheduling.dto.EmployeeScheduleInput;
import org.acme.employeescheduling.dto.ShiftDTO;
import org.acme.employeescheduling.dto.ShiftIdDetail;
import org.acme.employeescheduling.service.EmployeeScheduleIssues.DuplicateEmployeeIdIssue;
import org.acme.employeescheduling.service.EmployeeScheduleIssues.DuplicateShiftIdIssue;
import org.acme.employeescheduling.service.EmployeeScheduleIssues.EmployeeIdMissingIssue;
import org.acme.employeescheduling.service.EmployeeScheduleIssues.NonExistingEmployeeReferenceIssue;
import org.acme.employeescheduling.service.EmployeeScheduleIssues.ShiftIdMissingIssue;

@ApplicationScoped
public class EmployeeScheduleValidator
        implements ModelValidator<EmployeeScheduleInput, EmployeeScheduleConfigOverrides> {

    @Override
    public void validate(ValidationBuilder validationBuilder, EmployeeScheduleInput modelInput,
            ModelConfig<EmployeeScheduleConfigOverrides> modelConfig) {
        Set<String> employeeIds = validateEmployees(validationBuilder, modelInput.employees());
        validateShifts(validationBuilder, modelInput.shifts(), employeeIds);
    }

    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private Set<String> validateEmployees(ValidationBuilder validationBuilder, List<EmployeeDTO> employees) {
        Set<String> employeeIds = new HashSet<>();
        for (EmployeeDTO employee : employees) {
            if (employee.id() == null || employee.id().isBlank()) {
                validationBuilder.addIssue(new EmployeeIdMissingIssue());
            } else if (!employeeIds.add(employee.id())) {
                validationBuilder.addIssue(new DuplicateEmployeeIdIssue(new EmployeeIdDetail(employee.id())));
            }
        }
        return employeeIds;
    }

    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private void validateShifts(ValidationBuilder validationBuilder, List<ShiftDTO> shifts,
            Set<String> employeeIds) {
        Set<String> shiftIds = new HashSet<>();
        for (ShiftDTO shift : shifts) {
            if (shift.id() == null || shift.id().isBlank()) {
                validationBuilder.addIssue(new ShiftIdMissingIssue());
            } else if (!shiftIds.add(shift.id())) {
                validationBuilder.addIssue(new DuplicateShiftIdIssue(new ShiftIdDetail(shift.id())));
            }
            if (shift.employeeId() != null && !employeeIds.contains(shift.employeeId())) {
                validationBuilder.addIssue(new NonExistingEmployeeReferenceIssue(new ShiftIdDetail(shift.id())));
            }
        }
    }
}
