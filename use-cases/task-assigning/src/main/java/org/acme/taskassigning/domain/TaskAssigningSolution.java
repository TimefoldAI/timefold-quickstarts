package org.acme.taskassigning.domain;

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

import org.acme.taskassigning.dto.input.TaskAssigningInputMetrics;
import org.acme.taskassigning.dto.output.TaskAssigningOutputMetrics;

@PlanningSolution
public class TaskAssigningSolution implements SolverModel<HardMediumSoftScore>,
        InputMetricsAware<TaskAssigningInputMetrics>, OutputMetricsAware<TaskAssigningOutputMetrics> {

    @ProblemFactCollectionProperty
    private List<TaskType> taskTypes;

    @ProblemFactCollectionProperty
    private List<Customer> customers;

    @ValueRangeProvider
    @PlanningEntityCollectionProperty
    private List<Task> tasks;

    @PlanningEntityCollectionProperty
    private List<Employee> employees;

    @PlanningScore
    private HardMediumSoftScore score;

    private ConstraintWeightOverrides<HardMediumSoftScore> constraintWeightOverrides = ConstraintWeightOverrides.none();

    public TaskAssigningSolution() {
    }

    public TaskAssigningSolution(List<TaskType> taskTypes, List<Customer> customers, List<Task> tasks,
            List<Employee> employees) {
        this.taskTypes = taskTypes;
        this.customers = customers;
        this.tasks = tasks;
        this.employees = employees;
    }

    public List<TaskType> getTaskTypes() {
        return taskTypes;
    }

    public List<Customer> getCustomers() {
        return customers;
    }

    public List<Employee> getEmployees() {
        return employees;
    }

    public List<Task> getTasks() {
        return tasks;
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
    public TaskAssigningInputMetrics getInputMetrics() {
        return new TaskAssigningInputMetrics(tasks.size(), employees.size(), customers.size(), taskTypes.size());
    }

    @Override
    public TaskAssigningOutputMetrics getOutputMetrics() {
        int assignedTasks = (int) tasks.stream().filter(task -> task.getEmployee() != null).count();
        int unassignedTasks = tasks.size() - assignedTasks;
        int usedEmployees = (int) employees.stream().filter(employee -> !employee.getTasks().isEmpty()).count();
        long makespan = employees.stream().mapToLong(Employee::getEndTime).max().orElse(0L);
        return new TaskAssigningOutputMetrics(assignedTasks, unassignedTasks, usedEmployees, makespan);
    }
}
