package org.acme.foodpackaging.domain;

import java.time.Duration;
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

import org.acme.foodpackaging.dto.input.PackagingScheduleInputMetrics;
import org.acme.foodpackaging.dto.output.PackagingScheduleOutputMetrics;

@PlanningSolution
public class PackagingSchedule implements SolverModel<HardMediumSoftScore>,
        InputMetricsAware<PackagingScheduleInputMetrics>, OutputMetricsAware<PackagingScheduleOutputMetrics> {

    @ProblemFactProperty
    private WorkCalendar workCalendar;

    @ProblemFactCollectionProperty
    private List<Product> products;

    @ProblemFactCollectionProperty
    @ValueRangeProvider
    private List<Operator> operators;

    @PlanningEntityCollectionProperty
    private List<Line> lines;

    @PlanningEntityCollectionProperty
    @ValueRangeProvider
    private List<Job> jobs;

    @PlanningScore
    private HardMediumSoftScore score;

    private ConstraintWeightOverrides<HardMediumSoftScore> constraintWeightOverrides = ConstraintWeightOverrides.none();

    // No-arg constructor required for Timefold
    public PackagingSchedule() {
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

    public List<Product> getProducts() {
        return products;
    }

    public List<Operator> getOperators() {
        return operators;
    }

    public List<Line> getLines() {
        return lines;
    }

    public List<Job> getJobs() {
        return jobs;
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
    public PackagingScheduleInputMetrics getInputMetrics() {
        return new PackagingScheduleInputMetrics(jobs.size(), lines.size(), operators.size(), products.size());
    }

    @Override
    public PackagingScheduleOutputMetrics getOutputMetrics() {
        // Derived from the lines' job sequences rather than from Job.getLine(), so the metrics are also
        // correct on a solution whose inverse relation shadow variables were never initialized.
        int assignedJobs = lines.stream().mapToInt(line -> line.getJobs().size()).sum();
        int usedLines = (int) lines.stream().filter(line -> !line.getJobs().isEmpty()).count();
        return new PackagingScheduleOutputMetrics(assignedJobs, jobs.size() - assignedJobs, usedLines,
                totalCleaningMinutes());
    }

    /**
     * @return the changeover cleaning every line has to do between its consecutive jobs; the first job on
     *         a line needs no cleaning, since the line starts out clean
     */
    private long totalCleaningMinutes() {
        long totalMinutes = 0;
        for (Line line : lines) {
            List<Job> lineJobs = line.getJobs();
            for (int i = 1; i < lineJobs.size(); i++) {
                Duration cleaningDuration = lineJobs.get(i).getProduct()
                        .getCleanupDuration(lineJobs.get(i - 1).getProduct());
                totalMinutes += cleaningDuration.toMinutes();
            }
        }
        return totalMinutes;
    }
}
