package org.acme.maintenancescheduling.domain;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

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
    private List<Crew> crewList;
    @PlanningEntityCollectionProperty
    private List<Job> jobList;

    @PlanningScore
    private HardSoftLongScore score;

    // Ignored by Timefold, used by the UI to display solve or stop solving button
    private SolverStatus solverStatus;

    // No-arg constructor required for Timefold
    public MaintenanceSchedule() {
    }

    public MaintenanceSchedule(WorkCalendar workCalendar,
            List<Crew> crewList, List<Job> jobList) {
        this.workCalendar = workCalendar;
        this.crewList = crewList;
        this.jobList = jobList;
    }

    @ValueRangeProvider
    public List<LocalDate> createStartDateList() {
        return workCalendar.getFromDate().datesUntil(workCalendar.getToDate())
                // Skip weekends. Does not work for holidays.
                // Keep in sync with EndDateUpdatingVariableListener.updateEndDate().
                // To skip holidays too, cache all working days in WorkCalendar.
                .filter(date -> date.getDayOfWeek() != DayOfWeek.SATURDAY
                        && date.getDayOfWeek() != DayOfWeek.SUNDAY)
                .collect(Collectors.toList());
    }

    // ************************************************************************
    // Getters and setters
    // ************************************************************************

    public WorkCalendar getWorkCalendar() {
        return workCalendar;
    }

    public List<Crew> getCrewList() {
        return crewList;
    }

    public List<Job> getJobList() {
        return jobList;
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
