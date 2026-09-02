package org.acme.maintenancescheduling.domain;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import ai.timefold.solver.core.api.domain.solution.ConstraintWeightOverrides;
import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.ProblemFactProperty;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.HardSoftScore;
import ai.timefold.solver.service.definition.api.SolverModel;
import ai.timefold.solver.service.definition.api.metrics.InputMetricsAware;
import ai.timefold.solver.service.definition.api.metrics.OutputMetricsAware;

import org.acme.maintenancescheduling.dto.input.MaintenanceScheduleInputMetrics;
import org.acme.maintenancescheduling.dto.output.MaintenanceScheduleOutputMetrics;

@PlanningSolution
public class MaintenanceSchedule implements SolverModel<HardSoftScore>,
        InputMetricsAware<MaintenanceScheduleInputMetrics>, OutputMetricsAware<MaintenanceScheduleOutputMetrics> {

    @ProblemFactProperty
    private WorkCalendar workCalendar;
    @ProblemFactCollectionProperty
    @ValueRangeProvider
    private List<Crew> crews;
    @PlanningEntityCollectionProperty
    private List<Job> jobs;

    @PlanningScore
    private HardSoftScore score;

    private ConstraintWeightOverrides<HardSoftScore> constraintWeightOverrides = ConstraintWeightOverrides.none();

    public MaintenanceSchedule() {
    }

    public MaintenanceSchedule(WorkCalendar workCalendar, List<Crew> crews, List<Job> jobs) {
        this.workCalendar = workCalendar;
        this.crews = crews;
        this.jobs = jobs;
    }

    @ValueRangeProvider
    public List<LocalDate> createStartDateList() {
        return workCalendar.fromDate().datesUntil(workCalendar.toDate())
                // Skip weekends. Does not work for holidays.
                // Keep in sync with Job.calculateEndDate().
                // To skip holidays too, cache all working days in WorkCalendar.
                .filter(date -> date.getDayOfWeek() != DayOfWeek.SATURDAY
                        && date.getDayOfWeek() != DayOfWeek.SUNDAY)
                .toList();
    }

    // ************************************************************************
    // Getters and setters
    // ************************************************************************

    public WorkCalendar getWorkCalendar() {
        return workCalendar;
    }

    public List<Crew> getCrews() {
        return crews;
    }

    public List<Job> getJobs() {
        return jobs;
    }

    @Override
    public HardSoftScore getScore() {
        return score;
    }

    public void setScore(HardSoftScore score) {
        this.score = score;
    }

    @Override
    public ConstraintWeightOverrides<HardSoftScore> getConstraintWeightOverrides() {
        return constraintWeightOverrides;
    }

    public void setConstraintWeightOverrides(ConstraintWeightOverrides<HardSoftScore> constraintWeightOverrides) {
        this.constraintWeightOverrides = constraintWeightOverrides;
    }

    @Override
    public MaintenanceScheduleInputMetrics getInputMetrics() {
        return new MaintenanceScheduleInputMetrics(jobs.size(), crews.size(), createStartDateList().size());
    }

    @Override
    public MaintenanceScheduleOutputMetrics getOutputMetrics() {
        int assignedJobs = (int) jobs.stream().filter(Job::isAssigned).count();
        int unassignedJobs = jobs.size() - assignedJobs;
        int usedCrews = (int) jobs.stream().map(Job::getCrew).filter(Objects::nonNull).distinct().count();
        int jobsAfterIdealEndDate = (int) jobs.stream().filter(Job::isAfterIdealEndDate).count();
        return new MaintenanceScheduleOutputMetrics(assignedJobs, unassignedJobs, usedCrews, jobsAfterIdealEndDate);
    }
}
