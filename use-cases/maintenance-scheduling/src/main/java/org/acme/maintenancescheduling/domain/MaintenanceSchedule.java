package org.acme.maintenancescheduling.domain;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.ProblemFactProperty;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
import ai.timefold.solver.core.api.solver.SolverStatus;

@PlanningSolution
public class MaintenanceSchedule {

    @ProblemFactProperty
    private WorkCalendar workCalendar;
    @ProblemFactCollectionProperty
    @ValueRangeProvider
    private List<Crew> crews;
    @PlanningEntityCollectionProperty
    private List<Job> jobs;

    @PlanningScore
    private HardSoftLongScore score;

    // Ignored by Timefold, used by the UI to display solve or stop solving button
    private SolverStatus solverStatus;

    // No-arg constructor required for Timefold
    public MaintenanceSchedule() {
    }

    public MaintenanceSchedule(WorkCalendar workCalendar,
            List<Crew> crews, List<Job> jobs) {
        this.workCalendar = workCalendar;
        this.crews = crews;
        this.jobs = jobs;
    }

    public MaintenanceSchedule(HardSoftLongScore score, SolverStatus solverStatus) {
        this.score = score;
        this.solverStatus = solverStatus;
    }

    @ValueRangeProvider
    public List<LocalDate> createStartDateList() {
        return workCalendar.getFromDate().datesUntil(workCalendar.getToDate())
                // Skip weekends. Does not work for holidays.
                // Keep in sync with EndDateUpdatingVariableListener.updateEndDate().
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

    public HardSoftLongScore getScore() {
        return score;
    }

    public void setScore(HardSoftLongScore score) {
        this.score = score;
    }

    public SolverStatus getSolverStatus() {
        return solverStatus;
    }

    public void setSolverStatus(SolverStatus solverStatus) {
        this.solverStatus = solverStatus;
    }
}
