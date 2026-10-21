package org.acme.employeescheduling.domain;

import java.util.List;

import ai.timefold.solver.core.api.domain.solution.ConstraintWeightOverrides;
import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.HardSoftBigDecimalScore;
import ai.timefold.solver.service.definition.api.SolverModel;
import ai.timefold.solver.service.definition.api.metrics.InputMetricsAware;
import ai.timefold.solver.service.definition.api.metrics.OutputMetricsAware;

import org.acme.employeescheduling.dto.input.EmployeeScheduleInputMetrics;
import org.acme.employeescheduling.dto.output.EmployeeScheduleOutputMetrics;

/**
 * Scores with {@link HardSoftBigDecimalScore}, because the fairness constraint penalizes the
 * {@code LoadBalance} unfairness, which has a fractional part.
 */
@PlanningSolution
public class EmployeeSchedule implements SolverModel<HardSoftBigDecimalScore>,
        InputMetricsAware<EmployeeScheduleInputMetrics>, OutputMetricsAware<EmployeeScheduleOutputMetrics> {

    @ProblemFactCollectionProperty
    @ValueRangeProvider
    private List<Employee> employees;
    @PlanningEntityCollectionProperty
    private List<Shift> shifts;

    @PlanningScore
    private HardSoftBigDecimalScore score;

    private ConstraintWeightOverrides<HardSoftBigDecimalScore> constraintWeightOverrides =
            ConstraintWeightOverrides.none();

    public EmployeeSchedule() {
    }

    public EmployeeSchedule(List<Employee> employees, List<Shift> shifts) {
        this.employees = employees;
        this.shifts = shifts;
    }

    // ************************************************************************
    // Getters and setters
    // ************************************************************************

    public List<Employee> getEmployees() {
        return employees;
    }

    public List<Shift> getShifts() {
        return shifts;
    }

    @Override
    public HardSoftBigDecimalScore getScore() {
        return score;
    }

    public void setScore(HardSoftBigDecimalScore score) {
        this.score = score;
    }

    @Override
    public ConstraintWeightOverrides<HardSoftBigDecimalScore> getConstraintWeightOverrides() {
        return constraintWeightOverrides;
    }

    public void setConstraintWeightOverrides(
            ConstraintWeightOverrides<HardSoftBigDecimalScore> constraintWeightOverrides) {
        this.constraintWeightOverrides = constraintWeightOverrides;
    }

    @Override
    public EmployeeScheduleInputMetrics getInputMetrics() {
        int locations = (int) shifts.stream().map(Shift::getLocation).distinct().count();
        int days = (int) shifts.stream().map(Shift::getStartDate).distinct().count();
        return new EmployeeScheduleInputMetrics(employees.size(), shifts.size(), locations, days);
    }

    @Override
    public EmployeeScheduleOutputMetrics getOutputMetrics() {
        int assignedShifts = 0;
        int shiftsOnDesiredDays = 0;
        int shiftsOnUndesiredDays = 0;
        for (Shift shift : shifts) {
            Employee employee = shift.getEmployee();
            if (employee == null) {
                continue;
            }
            assignedShifts++;
            if (employee.desiredDates().stream().anyMatch(shift::isOverlappingWithDate)) {
                shiftsOnDesiredDays++;
            }
            if (employee.undesiredDates().stream().anyMatch(shift::isOverlappingWithDate)) {
                shiftsOnUndesiredDays++;
            }
        }
        return new EmployeeScheduleOutputMetrics(assignedShifts, shifts.size() - assignedShifts, shiftsOnDesiredDays,
                shiftsOnUndesiredDays);
    }
}
