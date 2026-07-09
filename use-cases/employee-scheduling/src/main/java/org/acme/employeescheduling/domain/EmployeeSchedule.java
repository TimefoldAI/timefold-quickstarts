package org.acme.employeescheduling.domain;

import java.util.List;

import ai.timefold.solver.core.api.domain.solution.ConstraintWeightOverrides;
import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.HardMediumSoftScore;
import ai.timefold.solver.service.definition.api.SolverModel;
import ai.timefold.solver.service.definition.api.metrics.InputMetricsAware;
import ai.timefold.solver.service.definition.api.metrics.OutputMetricsAware;

import org.acme.employeescheduling.dto.EmployeeScheduleInputMetrics;
import org.acme.employeescheduling.dto.EmployeeScheduleOutputMetrics;

@PlanningSolution
public class EmployeeSchedule implements SolverModel<HardMediumSoftScore>,
        InputMetricsAware<EmployeeScheduleInputMetrics>, OutputMetricsAware<EmployeeScheduleOutputMetrics> {

    @ProblemFactCollectionProperty
    @ValueRangeProvider
    private List<Employee> employees;

    @PlanningEntityCollectionProperty
    private List<Shift> shifts;

    @PlanningScore
    private HardMediumSoftScore score;

    private ConstraintWeightOverrides<HardMediumSoftScore> constraintWeightOverrides = ConstraintWeightOverrides.none();

    public EmployeeSchedule() {
    }

    public EmployeeSchedule(List<Employee> employees, List<Shift> shifts) {
        this.employees = employees;
        this.shifts = shifts;
    }

    public List<Employee> getEmployees() {
        return employees;
    }

    public void setEmployees(List<Employee> employees) {
        this.employees = employees;
    }

    public List<Shift> getShifts() {
        return shifts;
    }

    public void setShifts(List<Shift> shifts) {
        this.shifts = shifts;
    }

    @Override
    public HardMediumSoftScore getScore() {
        return score;
    }

    public void setScore(HardMediumSoftScore score) {
        this.score = score;
    }

    @Override
    public ConstraintWeightOverrides<HardMediumSoftScore> getConstraintWeightOverrides() {
        return constraintWeightOverrides;
    }

    public void setConstraintWeightOverrides(ConstraintWeightOverrides<HardMediumSoftScore> constraintWeightOverrides) {
        this.constraintWeightOverrides = constraintWeightOverrides;
    }

    @Override
    public EmployeeScheduleInputMetrics getInputMetrics() {
        long locationCount = shifts.stream().map(Shift::getLocation).distinct().count();
        long skillCount = shifts.stream().map(Shift::getRequiredSkill).distinct().count();
        return new EmployeeScheduleInputMetrics(employees.size(), shifts.size(), (int) locationCount, (int) skillCount);
    }

    @Override
    public EmployeeScheduleOutputMetrics getOutputMetrics() {
        int assignedShifts = (int) shifts.stream().filter(Shift::isAssigned).count();
        int unassignedShifts = shifts.size() - assignedShifts;
        int usedEmployees = (int) shifts.stream().filter(Shift::isAssigned).map(Shift::getEmployee).distinct().count();
        return new EmployeeScheduleOutputMetrics(assignedShifts, unassignedShifts, usedEmployees);
    }

    @Override
    public String toString() {
        return "EmployeeSchedule{shifts: " + (shifts == null ? 0 : shifts.size()) + ", score: " + score + '}';
    }
}
