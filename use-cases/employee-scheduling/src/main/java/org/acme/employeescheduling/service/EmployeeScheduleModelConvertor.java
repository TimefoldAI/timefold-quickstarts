package org.acme.employeescheduling.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;

import ai.timefold.solver.core.api.domain.solution.ConstraintWeightOverrides;
import ai.timefold.solver.core.api.score.HardMediumSoftScore;
import ai.timefold.solver.service.definition.api.ModelConvertor;
import ai.timefold.solver.service.definition.api.domain.ModelConfig;

import org.acme.employeescheduling.domain.Employee;
import org.acme.employeescheduling.domain.EmployeeSchedule;
import org.acme.employeescheduling.domain.Shift;
import org.acme.employeescheduling.dto.EmployeeDTO;
import org.acme.employeescheduling.dto.EmployeeScheduleConfigOverrides;
import org.acme.employeescheduling.dto.EmployeeScheduleInput;
import org.acme.employeescheduling.dto.EmployeeScheduleOutput;
import org.acme.employeescheduling.dto.ShiftDTO;
import org.acme.employeescheduling.solver.EmployeeSchedulingConstraintProvider;

@ApplicationScoped
public class EmployeeScheduleModelConvertor
        implements
        ModelConvertor<HardMediumSoftScore, EmployeeScheduleInput, EmployeeScheduleConfigOverrides, EmployeeSchedule, EmployeeScheduleOutput> {

    @Override
    public EmployeeScheduleInput applyOutputToInput(EmployeeScheduleInput modelInput,
            EmployeeScheduleOutput modelOutput) {
        Map<String, ShiftDTO> outputShifts = modelOutput.shifts().stream()
                .collect(Collectors.toMap(ShiftDTO::id, shift -> shift));
        List<ShiftDTO> updatedShifts = modelInput.shifts().stream()
                .map(shift -> {
                    ShiftDTO solved = outputShifts.get(shift.id());
                    if (solved == null) {
                        return shift;
                    }
                    return shift.withEmployeeId(solved.employeeId());
                })
                .collect(Collectors.toList());
        return new EmployeeScheduleInput(modelInput.employees(), updatedShifts);
    }

    @Override
    public EmployeeSchedule toSolverModel(EmployeeScheduleInput modelInput,
            ModelConfig<EmployeeScheduleConfigOverrides> modelConfig, Optional<EmployeeScheduleOutput> lastModelOutput) {
        Map<String, Employee> employeeMap = new HashMap<>();
        List<Employee> employees = modelInput.employees().stream().map(dto -> {
            Employee employee = new Employee(
                    dto.id(),
                    new LinkedHashSet<>(dto.skills()),
                    dto.unavailableDates().stream().map(LocalDate::parse).collect(Collectors.toCollection(LinkedHashSet::new)),
                    dto.undesiredDates().stream().map(LocalDate::parse).collect(Collectors.toCollection(LinkedHashSet::new)),
                    dto.desiredDates().stream().map(LocalDate::parse).collect(Collectors.toCollection(LinkedHashSet::new)));
            employeeMap.put(employee.getName(), employee);
            return employee;
        }).collect(Collectors.toList());

        List<Shift> shifts = modelInput.shifts().stream().map(dto -> {
            Shift shift = new Shift(dto.id(), LocalDateTime.parse(dto.start()), LocalDateTime.parse(dto.end()),
                    dto.location(), dto.requiredSkill(), null);
            if (dto.employeeId() != null) {
                shift.setEmployee(employeeMap.get(dto.employeeId()));
            }
            return shift;
        }).collect(Collectors.toList());

        EmployeeSchedule schedule = new EmployeeSchedule(employees, shifts);
        applyConstraintWeightOverrides(schedule, modelConfig);
        applyLastOutput(shifts, employeeMap, lastModelOutput);
        return schedule;
    }

    private static void applyConstraintWeightOverrides(EmployeeSchedule schedule,
            ModelConfig<EmployeeScheduleConfigOverrides> modelConfig) {
        if (modelConfig == null || modelConfig.overrides() == null) {
            return;
        }
        EmployeeScheduleConfigOverrides overrides = modelConfig.overrides();
        // Only apply weights that are actually set (non-null) in the merged overrides. A null weight means the
        // input did not override it, so the configuration profile value (or the constraint's default) is kept.
        Map<String, HardMediumSoftScore> weights = new HashMap<>();
        putIfPresent(weights, EmployeeSchedulingConstraintProvider.UNDESIRED_DAY_FOR_EMPLOYEE,
                overrides.undesiredDayForEmployeeWeight());
        putIfPresent(weights, EmployeeSchedulingConstraintProvider.DESIRED_DAY_FOR_EMPLOYEE,
                overrides.desiredDayForEmployeeWeight());
        putIfPresent(weights, EmployeeSchedulingConstraintProvider.BALANCE_EMPLOYEE_SHIFT_ASSIGNMENTS,
                overrides.balanceEmployeeShiftAssignmentsWeight());
        if (!weights.isEmpty()) {
            schedule.setConstraintWeightOverrides(ConstraintWeightOverrides.of(weights));
        }
    }

    private static void putIfPresent(Map<String, HardMediumSoftScore> weights, String constraintName, Long weight) {
        if (weight != null) {
            weights.put(constraintName, HardMediumSoftScore.ofSoft(weight));
        }
    }

    private static void applyLastOutput(List<Shift> shifts, Map<String, Employee> employeeMap,
            Optional<EmployeeScheduleOutput> lastModelOutput) {
        if (lastModelOutput.isEmpty()) {
            return;
        }
        Map<String, ShiftDTO> assignmentMap = lastModelOutput.get().shifts().stream()
                .collect(Collectors.toMap(ShiftDTO::id, shift -> shift));
        for (Shift shift : shifts) {
            ShiftDTO solved = assignmentMap.get(shift.getId());
            if (solved == null) {
                continue;
            }
            if (solved.employeeId() != null) {
                shift.setEmployee(employeeMap.get(solved.employeeId()));
            }
        }
    }

    @Override
    public EmployeeScheduleOutput toModelOutput(EmployeeSchedule solverModel) {
        List<EmployeeDTO> employees = solverModel.getEmployees().stream().map(this::toDTO).collect(Collectors.toList());
        List<ShiftDTO> shifts = solverModel.getShifts().stream().map(this::toDTO).collect(Collectors.toList());
        String score = solverModel.getScore() == null ? "" : solverModel.getScore().toString();
        return new EmployeeScheduleOutput(employees, shifts, score);
    }

    private EmployeeDTO toDTO(Employee employee) {
        List<String> unavailableDates = employee.getUnavailableDates().stream()
                .map(LocalDate::toString).collect(Collectors.toList());
        List<String> undesiredDates = employee.getUndesiredDates().stream()
                .map(LocalDate::toString).collect(Collectors.toList());
        List<String> desiredDates = employee.getDesiredDates().stream()
                .map(LocalDate::toString).collect(Collectors.toList());
        return new EmployeeDTO(employee.getName(), employee.getSkills().stream().sorted().collect(Collectors.toList()),
                unavailableDates, undesiredDates, desiredDates);
    }

    private ShiftDTO toDTO(Shift shift) {
        String employeeId = shift.getEmployee() == null ? null : shift.getEmployee().getName();
        return new ShiftDTO(shift.getId(), shift.getStart().toString(), shift.getEnd().toString(),
                shift.getLocation(), shift.getRequiredSkill(), employeeId);
    }
}
