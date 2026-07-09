package org.acme.foodpackaging.domain;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import ai.timefold.solver.core.api.domain.solution.ConstraintWeightOverrides;
import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.ProblemFactProperty;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.HardMediumSoftScore;
import ai.timefold.solver.service.definition.api.SolverModel;
import ai.timefold.solver.service.definition.api.metrics.InputMetricsAware;
import ai.timefold.solver.service.definition.api.metrics.OutputMetricsAware;

import org.acme.foodpackaging.dto.PackagingScheduleInputMetrics;
import org.acme.foodpackaging.dto.PackagingScheduleOutputMetrics;

@PlanningSolution
public class PackagingSchedule implements SolverModel<HardMediumSoftScore>,
        InputMetricsAware<PackagingScheduleInputMetrics>, OutputMetricsAware<PackagingScheduleOutputMetrics> {

    @ProblemFactProperty
    private WorkCalendar workCalendar;

    @ProblemFactCollectionProperty
    private List<Product> products;

    @PlanningEntityCollectionProperty
    @ValueRangeProvider
    private List<Operator> operators;

    @PlanningEntityCollectionProperty
    private List<Line> lines;

    @PlanningEntityCollectionProperty
    @ValueRangeProvider
    private List<Job> jobs;

    @PlanningScore
    private HardMediumSoftScore score;

    private ConstraintWeightOverrides<HardMediumSoftScore> constraintWeightOverrides =
            ConstraintWeightOverrides.none();

    public PackagingSchedule() {
        // No-arg constructor required for Timefold
    }

    public PackagingSchedule(WorkCalendar workCalendar, List<Product> products, List<Operator> operators,
            List<Line> lines, List<Job> jobs) {
        this.workCalendar = workCalendar;
        this.products = products;
        this.operators = operators;
        this.lines = lines;
        this.jobs = jobs;
    }

    // ************************************************************************
    // Getters and setters
    // ************************************************************************

    public WorkCalendar getWorkCalendar() {
        return workCalendar;
    }

    public void setWorkCalendar(WorkCalendar workCalendar) {
        this.workCalendar = workCalendar;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public List<Operator> getOperators() {
        return operators;
    }

    public void setOperators(List<Operator> operators) {
        this.operators = operators;
    }

    public List<Line> getLines() {
        return lines;
    }

    public void setLines(List<Line> lines) {
        this.lines = lines;
    }

    public List<Job> getJobs() {
        return jobs;
    }

    public void setJobs(List<Job> jobs) {
        this.jobs = jobs;
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

    public void setConstraintWeightOverrides(
            ConstraintWeightOverrides<HardMediumSoftScore> constraintWeightOverrides) {
        this.constraintWeightOverrides = constraintWeightOverrides;
    }

    @Override
    public PackagingScheduleInputMetrics getInputMetrics() {
        return new PackagingScheduleInputMetrics(jobs.size(), lines.size(), operators.size(), products.size());
    }

    @Override
    public PackagingScheduleOutputMetrics getOutputMetrics() {
        int assignedJobs = (int) jobs.stream().filter(job -> job.getLine() != null).count();
        int unassignedJobs = jobs.size() - assignedJobs;
        int usedLines = (int) lines.stream().filter(line -> !line.getJobs().isEmpty()).count();
        LocalDateTime earliestStart = lines.stream()
                .map(Line::getStartDateTime)
                .filter(start -> start != null)
                .min(LocalDateTime::compareTo)
                .orElse(null);
        LocalDateTime latestEnd = jobs.stream()
                .filter(job -> job.getLine() != null && job.getEndDateTime() != null)
                .map(Job::getEndDateTime)
                .max(LocalDateTime::compareTo)
                .orElse(null);
        int makespanMinutes = earliestStart == null || latestEnd == null ? 0
                : (int) Duration.between(earliestStart, latestEnd).toMinutes();
        return new PackagingScheduleOutputMetrics(assignedJobs, unassignedJobs, usedLines, Math.max(0, makespanMinutes));
    }

    @Override
    public String toString() {
        return "PackagingSchedule{jobs: " + jobs.size() + ", score: " + score + '}';
    }
}
