package org.acme.taskassigning.domain;

import java.util.List;

import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.BendableScore;
import ai.timefold.solver.core.api.solver.SolverStatus;

@PlanningSolution
public class TaskAssigningSolution {

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

    private SolverStatus solverStatus;

    public TaskAssigningSolution() {
    }

    public TaskAssigningSolution(BendableScore score, SolverStatus solverStatus) {
        this.score = score;
        this.solverStatus = solverStatus;
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

    public BendableScore getScore() {
        return score;
    }

    public void setScore(BendableScore score) {
        this.score = score;
    }

    public SolverStatus getSolverStatus() {
        return solverStatus;
    }

    public void setSolverStatus(SolverStatus solverStatus) {
        this.solverStatus = solverStatus;
    }
}
