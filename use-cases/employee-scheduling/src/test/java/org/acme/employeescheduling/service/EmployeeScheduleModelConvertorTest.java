package org.acme.employeescheduling.service;

import static org.acme.employeescheduling.support.TestHelper.aShiftDTO;
import static org.acme.employeescheduling.support.TestHelper.configOverrides;
import static org.acme.employeescheduling.support.TestHelper.createProblem;
import static org.acme.employeescheduling.support.TestHelper.output;
import static org.acme.employeescheduling.support.TestHelper.shiftOutputDTO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import ai.timefold.solver.core.api.score.HardSoftBigDecimalScore;
import ai.timefold.solver.service.definition.api.domain.ModelConfig;

import org.acme.employeescheduling.domain.EmployeeSchedule;
import org.acme.employeescheduling.domain.EmployeeScheduleConstraintProperties;
import org.acme.employeescheduling.domain.Shift;
import org.acme.employeescheduling.dto.input.EmployeeScheduleConfigOverrides;
import org.acme.employeescheduling.dto.input.EmployeeScheduleInput;
import org.acme.employeescheduling.dto.input.ShiftInputDTO;
import org.acme.employeescheduling.dto.output.EmployeeScheduleOutput;
import org.acme.employeescheduling.dto.output.ShiftOutputDTO;
import org.junit.jupiter.api.Test;

class EmployeeScheduleModelConvertorTest {

    private static final EmployeeScheduleInput PROBLEM = createProblem();

    private final EmployeeScheduleModelConvertor convertor = new EmployeeScheduleModelConvertor();

    @Test
    void unassignedInputLeavesEveryShiftToTheSolver() {
        EmployeeSchedule schedule = toSolverModel(PROBLEM, Optional.empty());

        assertThat(schedule.getEmployees()).hasSize(3);
        assertThat(schedule.getShifts()).hasSize(4).noneMatch(Shift::isAssigned);
        assertThat(schedule.getEmployees().getFirst().unavailableDates()).hasSize(1);
    }

    @Test
    void assignedInputIsKept() {
        EmployeeScheduleInput input = withFirstShift(aShiftDTO("s1").employeeName("Beth").build());

        Shift shift = shiftById(toSolverModel(input, Optional.empty()), "s1");
        assertThat(shift.getEmployee().name()).isEqualTo("Beth");
    }

    @Test
    void lastModelOutputOverridesTheInputAssignment() {
        EmployeeScheduleInput input = withFirstShift(aShiftDTO("s1").employeeName("Beth").build());
        EmployeeScheduleOutput lastOutput = output(List.of(shiftOutputDTO("s1", "Carl")));

        Shift shift = shiftById(toSolverModel(input, Optional.of(lastOutput)), "s1");
        assertThat(shift.getEmployee().name()).isEqualTo("Carl");
    }

    @Test
    void unknownReferenceFailsFast() {
        EmployeeScheduleInput input = withFirstShift(aShiftDTO("s1").employeeName("does-not-exist").build());

        assertThatIllegalArgumentException()
                .isThrownBy(() -> toSolverModel(input, Optional.empty()))
                .withMessageContaining("employee 'does-not-exist'");
    }

    @Test
    void outputRoundTripsThroughTheInput() {
        EmployeeSchedule schedule = toSolverModel(PROBLEM, Optional.empty());
        for (Shift shift : schedule.getShifts()) {
            shift.setEmployee(schedule.getEmployees().getLast());
        }

        EmployeeScheduleOutput output = convertor.toModelOutput(schedule);
        assertThat(output.shifts()).extracting(ShiftOutputDTO::employeeName).containsOnly("Carl");

        EmployeeScheduleInput updatedInput = convertor.applyOutputToInput(PROBLEM, output);
        assertThat(updatedInput.shifts()).extracting(ShiftInputDTO::employeeName).containsOnly("Carl");
        assertThat(updatedInput.shifts()).extracting(ShiftInputDTO::requiredSkill)
                .containsExactlyElementsOf(PROBLEM.shifts().stream().map(ShiftInputDTO::requiredSkill).toList());
    }

    @Test
    void onlySetWeightsAreOverridden() {
        ModelConfig<EmployeeScheduleConfigOverrides> modelConfig = new ModelConfig<>(configOverrides(5L, null, 0L));

        EmployeeSchedule schedule = convertor.toSolverModel(PROBLEM, modelConfig, Optional.empty());
        var overrides = schedule.getConstraintWeightOverrides();
        assertThat(overrides.getKnownConstraintIds()).containsExactlyInAnyOrder(
                EmployeeScheduleConstraintProperties.UNDESIRED_DAY_FOR_EMPLOYEE,
                EmployeeScheduleConstraintProperties.BALANCE_EMPLOYEE_SHIFT_ASSIGNMENTS);
        assertThat(overrides.getConstraintWeight(EmployeeScheduleConstraintProperties.UNDESIRED_DAY_FOR_EMPLOYEE))
                .isEqualTo(HardSoftBigDecimalScore.ofSoft(BigDecimal.valueOf(5)));
        assertThat(overrides.getConstraintWeight(EmployeeScheduleConstraintProperties.BALANCE_EMPLOYEE_SHIFT_ASSIGNMENTS))
                .isEqualTo(HardSoftBigDecimalScore.ZERO);
    }

    private EmployeeSchedule toSolverModel(EmployeeScheduleInput input, Optional<EmployeeScheduleOutput> lastOutput) {
        return convertor.toSolverModel(input, ModelConfig.empty(), lastOutput);
    }

    private static EmployeeScheduleInput withFirstShift(ShiftInputDTO shift) {
        List<ShiftInputDTO> shifts = new ArrayList<>(PROBLEM.shifts());
        shifts.set(0, shift);
        return PROBLEM.withShifts(shifts);
    }

    private static Shift shiftById(EmployeeSchedule schedule, String id) {
        return schedule.getShifts().stream().filter(shift -> shift.getId().equals(id)).findFirst().orElseThrow();
    }
}
