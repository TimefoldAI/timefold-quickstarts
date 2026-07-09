package org.acme.taskassigning.domain;

import java.util.List;

import ai.timefold.solver.core.api.domain.solution.ConstraintWeightOverrides;
import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.BendableScore;
import ai.timefold.solver.service.definition.api.SolverModel;
import ai.timefold.solver.service.definition.api.metrics.InputMetricsAware;
import ai.timefold.solver.service.definition.api.metrics.OutputMetricsAware;

import org.acme.taskassigning.dto.TaskAssigningInputMetrics;
import org.acme.taskassigning.dto.TaskAssigningOutputMetrics;

@PlanningSolution
public class TaskAssigningSolution implements SolverModel<BendableScore>,
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

    @PlanningScore(bendableHardLevelsSize = 1, bendableSoftLevelsSize = 3)
    private BendableScore score;

    private ConstraintWeightOverrides<BendableScore> constraintWeightOverrides = ConstraintWeightOverrides.none();

    public TaskAssigningSolution() {
        // Marshalling constructor
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

    public void setTaskTypes(List<TaskType> taskTypes) {
        this.taskTypes = taskTypes;
    }

    public List<Customer> getCustomers() {
        return customers;
    }

    public void setCustomers(List<Customer> customers) {
        this.customers = customers;
    }

    public List<Employee> getEmployees() {
        return employees;
    }

    public void setEmployees(List<Employee> employees) {
        this.employees = employees;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    @Override
    public BendableScore getScore() {
        return score;
    }

    public void setScore(BendableScore score) {
        this.score = score;
    }

    @Override
    public ConstraintWeightOverrides<BendableScore> getConstraintWeightOverrides() {
        return constraintWeightOverrides;
    }

    public void setConstraintWeightOverrides(ConstraintWeightOverrides<BendableScore> constraintWeightOverrides) {
        this.constraintWeightOverrides = constraintWeightOverrides;
    }

    @Override
    public TaskAssigningInputMetrics getInputMetrics() {
        return new TaskAssigningInputMetrics(employees.size(), tasks.size(), taskTypes.size(), customers.size());
    }

    @Override
    public TaskAssigningOutputMetrics getOutputMetrics() {
        int assignedTasks = (int) tasks.stream().filter(task -> task.getEmployee() != null).count();
        int unassignedTasks = tasks.size() - assignedTasks;
        int usedEmployees = (int) employees.stream().filter(employee -> !employee.getTasks().isEmpty()).count();
        long makespan = employees.stream().mapToLong(Employee::getEndTime).max().orElse(0L);
        return new TaskAssigningOutputMetrics(assignedTasks, unassignedTasks, usedEmployees, makespan);
    }

    @Override
    public String toString() {
        return "TaskAssigningSolution{tasks: " + tasks.size() + ", score: " + score + '}';
    }
}
