package org.acme.tournamentschedule.domain;

import java.util.List;

import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.HardMediumSoftBigDecimalScore;
import ai.timefold.solver.core.api.solver.SolverStatus;

@PlanningSolution
public class TournamentSchedule {

    @ProblemFactCollectionProperty
    @ValueRangeProvider
    private List<Team> teams;
    @ProblemFactCollectionProperty
    private List<Day> days;
    @ProblemFactCollectionProperty
    private List<UnavailabilityPenalty> unavailabilityPenalties;

    @PlanningEntityCollectionProperty
    private List<TeamAssignment> teamAssignments;

    @PlanningScore
    private HardMediumSoftBigDecimalScore score;
    private SolverStatus solverStatus;

    public TournamentSchedule() {
    }

    public TournamentSchedule(HardMediumSoftBigDecimalScore score, SolverStatus solverStatus) {
        this.score = score;
        this.solverStatus = solverStatus;
    }

    public List<Team> getTeams() {
        return teams;
    }

    public void setTeams(List<Team> teams) {
        this.teams = teams;
    }

    public List<Day> getDays() {
        return days;
    }

    public void setDays(List<Day> days) {
        this.days = days;
    }

    public List<UnavailabilityPenalty> getUnavailabilityPenalties() {
        return unavailabilityPenalties;
    }

    public void setUnavailabilityPenalties(List<UnavailabilityPenalty> unavailabilityPenalties) {
        this.unavailabilityPenalties = unavailabilityPenalties;
    }

    public List<TeamAssignment> getTeamAssignments() {
        return teamAssignments;
    }

    public void setTeamAssignments(List<TeamAssignment> teamAssignments) {
        this.teamAssignments = teamAssignments;
    }

    public HardMediumSoftBigDecimalScore getScore() {
        return score;
    }

    public void setScore(HardMediumSoftBigDecimalScore score) {
        this.score = score;
    }

    public SolverStatus getSolverStatus() {
        return solverStatus;
    }

    public void setSolverStatus(SolverStatus solverStatus) {
        this.solverStatus = solverStatus;
    }
}
