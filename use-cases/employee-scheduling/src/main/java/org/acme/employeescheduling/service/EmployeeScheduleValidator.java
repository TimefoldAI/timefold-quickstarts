package org.acme.employeescheduling.service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;

import ai.timefold.solver.service.definition.api.domain.ModelConfig;
import ai.timefold.solver.service.definition.api.validation.ModelValidator;
import ai.timefold.solver.service.definition.api.validation.ValidationBuilder;

import org.acme.employeescheduling.dto.input.EmployeeInputDTO;
import org.acme.employeescheduling.dto.input.EmployeeScheduleConfigOverrides;
import org.acme.employeescheduling.dto.input.EmployeeScheduleInput;
import org.acme.employeescheduling.dto.input.ShiftInputDTO;
import org.acme.employeescheduling.service.validation.EmployeeScheduleIssue.DuplicateEmployeeNameIssue;
import org.acme.employeescheduling.service.validation.EmployeeScheduleIssue.DuplicateShiftIdIssue;
import org.acme.employeescheduling.service.validation.EmployeeScheduleIssue.InsufficientEmployeesIssue;
import org.acme.employeescheduling.service.validation.EmployeeScheduleIssue.InvalidShiftTimeRangeIssue;
import org.acme.employeescheduling.service.validation.EmployeeScheduleIssue.NoQualifiedEmployeeIssue;
import org.acme.employeescheduling.service.validation.EmployeeScheduleIssue.NonExistingEmployeeReferenceIssue;

@ApplicationScoped
public class EmployeeScheduleValidator
        implements ModelValidator<EmployeeScheduleInput, EmployeeScheduleConfigOverrides> {

    @Override
    public void validate(ValidationBuilder validationBuilder, EmployeeScheduleInput modelInput,
            ModelConfig<EmployeeScheduleConfigOverrides> modelConfig) {
        // OpenAPI spec compliance is enforced by the Service module at the REST layer, before this validator ever
        // runs; only domain-specific checks belong here.
        List<EmployeeInputDTO> employees = orEmpty(modelInput.employees());
        List<ShiftInputDTO> shifts = orEmpty(modelInput.shifts());
        Set<String> employeeNames = validateEmployees(validationBuilder, employees);
        validateShifts(validationBuilder, shifts, employeeNames);
        validateSkills(validationBuilder, employees, shifts);
        validateCapacity(validationBuilder, shifts, employees.size());
    }

    private static Set<String> validateEmployees(ValidationBuilder validationBuilder,
            List<EmployeeInputDTO> employees) {
        Set<String> employeeNames = new HashSet<>();
        for (EmployeeInputDTO employee : employees) {
            if (hasId(employee.name()) && !employeeNames.add(employee.name())) {
                validationBuilder.addIssue(new DuplicateEmployeeNameIssue(employee.name()));
            }
        }
        return employeeNames;
    }

    private static void validateShifts(ValidationBuilder validationBuilder, List<ShiftInputDTO> shifts,
            Set<String> employeeNames) {
        Set<String> shiftIds = new HashSet<>();
        for (ShiftInputDTO shift : shifts) {
            if (hasId(shift.id()) && !shiftIds.add(shift.id())) {
                validationBuilder.addIssue(new DuplicateShiftIdIssue(shift.id()));
            }
            if (shift.start() != null && shift.end() != null && !shift.end().isAfter(shift.start())) {
                validationBuilder.addIssue(new InvalidShiftTimeRangeIssue(shift.id()));
            }
            if (shift.employeeName() != null && !employeeNames.contains(shift.employeeName())) {
                validationBuilder.addIssue(new NonExistingEmployeeReferenceIssue(shift.id()));
            }
        }
    }

    /**
     * One issue per required skill that no employee has, so the issue list stays bounded by the number of distinct
     * skills rather than growing with the number of shifts.
     */
    private static void validateSkills(ValidationBuilder validationBuilder, List<EmployeeInputDTO> employees,
            List<ShiftInputDTO> shifts) {
        Set<String> employeeSkills = new HashSet<>();
        for (EmployeeInputDTO employee : employees) {
            employeeSkills.addAll(orEmpty(employee.skills()));
        }
        Map<String, Integer> shiftCountBySkill = new LinkedHashMap<>();
        for (ShiftInputDTO shift : shifts) {
            if (shift.requiredSkill() != null && !employeeSkills.contains(shift.requiredSkill())) {
                shiftCountBySkill.merge(shift.requiredSkill(), 1, Integer::sum);
            }
        }
        shiftCountBySkill.forEach((skill, shiftCount) -> validationBuilder
                .addIssue(new NoQualifiedEmployeeIssue(skill, shiftCount)));
    }

    /**
     * An employee works at most one shift a day, so a day with more shifts than employees can never be feasible.
     * One issue per such day.
     */
    private static void validateCapacity(ValidationBuilder validationBuilder, List<ShiftInputDTO> shifts,
            int employeeCount) {
        Map<LocalDate, Integer> shiftCountByDate = new LinkedHashMap<>();
        for (ShiftInputDTO shift : shifts) {
            if (shift.start() != null) {
                shiftCountByDate.merge(shift.start().toLocalDate(), 1, Integer::sum);
            }
        }
        shiftCountByDate.forEach((date, shiftCount) -> {
            if (shiftCount > employeeCount) {
                validationBuilder.addIssue(new InsufficientEmployeesIssue(date.toString(), shiftCount, employeeCount));
            }
        });
    }

    private static <T> List<T> orEmpty(List<T> list) {
        return list == null ? List.of() : list;
    }

    private static boolean hasId(String id) {
        return id != null && !id.isEmpty();
    }
}
