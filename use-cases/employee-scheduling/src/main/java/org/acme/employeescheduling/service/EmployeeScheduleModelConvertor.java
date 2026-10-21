package org.acme.employeescheduling.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;

import ai.timefold.solver.core.api.domain.solution.ConstraintWeightOverrides;
import ai.timefold.solver.core.api.score.HardSoftBigDecimalScore;
import ai.timefold.solver.service.definition.api.ModelConvertor;
import ai.timefold.solver.service.definition.api.domain.ModelConfig;

import org.acme.employeescheduling.domain.Employee;
import org.acme.employeescheduling.domain.EmployeeSchedule;
import org.acme.employeescheduling.domain.EmployeeScheduleConstraintProperties;
import org.acme.employeescheduling.domain.Shift;
import org.acme.employeescheduling.dto.input.EmployeeInputDTO;
import org.acme.employeescheduling.dto.input.EmployeeScheduleConfigOverrides;
import org.acme.employeescheduling.dto.input.EmployeeScheduleInput;
import org.acme.employeescheduling.dto.input.ShiftInputDTO;
import org.acme.employeescheduling.dto.output.EmployeeScheduleOutput;
import org.acme.employeescheduling.dto.output.ShiftOutputDTO;

@ApplicationScoped
public class EmployeeScheduleModelConvertor implements
        ModelConvertor<HardSoftBigDecimalScore, EmployeeScheduleInput, EmployeeScheduleConfigOverrides, EmployeeSchedule, EmployeeScheduleOutput> {

    @Override
    public EmployeeScheduleInput applyOutputToInput(EmployeeScheduleInput modelInput,
            EmployeeScheduleOutput modelOutput) {
        Map<String, ShiftOutputDTO> outputShifts = new LinkedHashMap<>();
        for (ShiftOutputDTO shift : modelOutput.shifts()) {
            outputShifts.put(shift.id(), shift);
        }
        List<ShiftInputDTO> updatedShifts = modelInput.shifts().stream()
                .map(shift -> {
                    ShiftOutputDTO solved = outputShifts.get(shift.id());
                    return solved == null ? shift : shift.withEmployeeName(solved.employeeName());
                })
                .toList();
        return modelInput.withShifts(updatedShifts);
    }

    @Override
    public EmployeeSchedule toSolverModel(EmployeeScheduleInput modelInput,
            ModelConfig<EmployeeScheduleConfigOverrides> modelConfig,
            Optional<EmployeeScheduleOutput> lastModelOutput) {
        Map<String, Employee> employeeMap = new LinkedHashMap<>();
        for (EmployeeInputDTO dto : modelInput.employees()) {
            employeeMap.put(dto.name(), new Employee(dto.name(), new LinkedHashSet<>(dto.skills()),
                    new LinkedHashSet<>(dto.unavailableDates()), new LinkedHashSet<>(dto.undesiredDates()),
                    new LinkedHashSet<>(dto.desiredDates())));
        }
        List<Shift> shifts = modelInput.shifts().stream()
                .map(dto -> new Shift(dto.id(), dto.start(), dto.end(), dto.location(), dto.requiredSkill(),
                        dto.employeeName() == null ? null : require(employeeMap, dto.employeeName(), "employee")))
                .toList();

        EmployeeSchedule schedule = new EmployeeSchedule(List.copyOf(employeeMap.values()), shifts);
        applyLastOutput(shifts, employeeMap, lastModelOutput);
        applyConstraintWeightOverrides(schedule, modelConfig);
        return schedule;
    }

    @Override
    public EmployeeScheduleOutput toModelOutput(EmployeeSchedule solverModel) {
        List<ShiftOutputDTO> shifts = solverModel.getShifts().stream()
                .map(shift -> new ShiftOutputDTO(shift.getId(),
                        shift.getEmployee() == null ? null : shift.getEmployee().name()))
                .toList();
        return new EmployeeScheduleOutput(shifts);
    }

    /**
     * Fails fast with an actionable message instead of letting an unknown reference
     * turn into a null in the solver model and a delayed NullPointerException.
     */
    private static <T> T require(Map<String, T> map, String key, String kind) {
        T value = map.get(key);
        if (value == null) {
            throw new IllegalArgumentException("Unknown %s '%s'.".formatted(kind, key));
        }
        return value;
    }

    private static void applyConstraintWeightOverrides(EmployeeSchedule schedule,
            ModelConfig<EmployeeScheduleConfigOverrides> modelConfig) {
        if (modelConfig == null || modelConfig.overrides() == null) {
            return;
        }
        EmployeeScheduleConfigOverrides overrides = modelConfig.overrides();
        // Only apply weights that are actually set (non-null) in the merged overrides. A null weight means the
        // input did not override it, so the configuration profile value (or the constraint's default) is kept.
        Map<String, HardSoftBigDecimalScore> weights = new HashMap<>();
        if (overrides.undesiredDayForEmployeeWeight() != null) {
            weights.put(EmployeeScheduleConstraintProperties.UNDESIRED_DAY_FOR_EMPLOYEE,
                    softWeight(overrides.undesiredDayForEmployeeWeight()));
        }
        if (overrides.desiredDayForEmployeeWeight() != null) {
            weights.put(EmployeeScheduleConstraintProperties.DESIRED_DAY_FOR_EMPLOYEE,
                    softWeight(overrides.desiredDayForEmployeeWeight()));
        }
        if (overrides.balanceEmployeeShiftAssignmentsWeight() != null) {
            weights.put(EmployeeScheduleConstraintProperties.BALANCE_EMPLOYEE_SHIFT_ASSIGNMENTS,
                    softWeight(overrides.balanceEmployeeShiftAssignmentsWeight()));
        }
        if (!weights.isEmpty()) {
            schedule.setConstraintWeightOverrides(ConstraintWeightOverrides.of(weights));
        }
    }

    private static HardSoftBigDecimalScore softWeight(long weight) {
        return HardSoftBigDecimalScore.ofSoft(BigDecimal.valueOf(weight));
    }

    // lastModelOutput is used to recover a run that stopped halfway; it overrides the input assignment.
    private static void applyLastOutput(List<Shift> shifts, Map<String, Employee> employeeMap,
            Optional<EmployeeScheduleOutput> lastModelOutput) {
        if (lastModelOutput.isEmpty()) {
            return;
        }
        Map<String, Shift> shiftMap = new LinkedHashMap<>();
        for (Shift shift : shifts) {
            shiftMap.put(shift.getId(), shift);
        }
        for (ShiftOutputDTO solved : lastModelOutput.get().shifts()) {
            Shift shift = shiftMap.get(solved.id());
            if (shift == null || solved.employeeName() == null) {
                continue;
            }
            Employee employee = employeeMap.get(solved.employeeName());
            if (employee != null) {
                shift.setEmployee(employee);
            }
        }
    }
}
