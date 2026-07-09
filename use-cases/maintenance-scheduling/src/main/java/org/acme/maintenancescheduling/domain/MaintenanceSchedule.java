package org.acme.maintenancescheduling.domain;

import java.time.DayOfWeek;
import java.time.LocalDate;
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

import org.acme.maintenancescheduling.dto.MaintenanceScheduleInputMetrics;
import org.acme.maintenancescheduling.dto.MaintenanceScheduleOutputMetrics;

@PlanningSolution
public class MaintenanceSchedule implements SolverModel<HardMediumSoftScore>,
        InputMetricsAware<MaintenanceScheduleInputMetrics>, OutputMetricsAware<MaintenanceScheduleOutputMetrics> {

    @ProblemFactProperty
    private WorkCalendar workCalendar;
    @ProblemFactCollectionProperty
    @ValueRangeProvider
    private List<Crew> crews;
    @PlanningEntityCollectionProperty
    private List<Job> jobs;

    @PlanningScore
    private HardMediumSoftScore score;

    private ConstraintWeightOverrides<HardMediumSoftScore> constraintWeightOverrides = ConstraintWeightOverrides.none();

    public MaintenanceSchedule() {
    }

    public MaintenanceSchedule(WorkCalendar workCalendar, List<Crew> crews, List<Job> jobs) {
        this.workCalendar = workCalendar;
        this.crews = crews;
        this.jobs = jobs;
    }

    @ValueRangeProvider
    public List<LocalDate> createStartDateList() {
        return workCalendar.getFromDate().datesUntil(workCalendar.getToDate())
                // Skip weekends. Does not work for holidays.
                // Keep in sync with Job.calculateEndDate().
                // To skip holidays too, cache all working days in WorkCalendar.
                .filter(date -> date.getDayOfWeek() != DayOfWeek.SATURDAY
                        && date.getDayOfWeek() != DayOfWeek.SUNDAY)
                .toList();
    }

    public WorkCalendar getWorkCalendar() {
        return workCalendar;
    }

    public void setWorkCalendar(WorkCalendar workCalendar) {
        this.workCalendar = workCalendar;
    }

    public List<Crew> getCrews() {
        return crews;
    }

    public void setCrews(List<Crew> crews) {
        this.crews = crews;
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

    public void setConstraintWeightOverrides(ConstraintWeightOverrides<HardMediumSoftScore> constraintWeightOverrides) {
        this.constraintWeightOverrides = constraintWeightOverrides;
    }

    @Override
    public MaintenanceScheduleInputMetrics getInputMetrics() {
        long tags = jobs.stream().flatMap(job -> job.getTags().stream()).distinct().count();
        return new MaintenanceScheduleInputMetrics(jobs.size(), crews.size(), (int) tags);
    }

    @Override
    public MaintenanceScheduleOutputMetrics getOutputMetrics() {
        int assignedJobs = (int) jobs.stream().filter(Job::isAssigned).count();
        int unassignedJobs = jobs.size() - assignedJobs;
        int usedCrews = (int) jobs.stream().filter(Job::isAssigned).map(Job::getCrew).distinct().count();
        return new MaintenanceScheduleOutputMetrics(assignedJobs, unassignedJobs, usedCrews);
    }

    @Override
    public String toString() {
        return "MaintenanceSchedule{jobs: " + jobs.size() + ", score: " + score + '}';
    }
}
