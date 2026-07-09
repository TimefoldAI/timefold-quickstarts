package org.acme.tournamentschedule.domain;

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

import org.acme.tournamentschedule.dto.TournamentScheduleInputMetrics;
import org.acme.tournamentschedule.dto.TournamentScheduleOutputMetrics;

@PlanningSolution
public class TournamentSchedule implements SolverModel<HardMediumSoftScore>,
        InputMetricsAware<TournamentScheduleInputMetrics>, OutputMetricsAware<TournamentScheduleOutputMetrics> {

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
    private HardMediumSoftScore score;

    private ConstraintWeightOverrides<HardMediumSoftScore> constraintWeightOverrides =
            ConstraintWeightOverrides.none();

    public TournamentSchedule() {
    }

    public TournamentSchedule(List<Team> teams, List<Day> days, List<UnavailabilityPenalty> unavailabilityPenalties,
            List<TeamAssignment> teamAssignments) {
        this.teams = teams;
        this.days = days;
        this.unavailabilityPenalties = unavailabilityPenalties;
        this.teamAssignments = teamAssignments;
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
    public TournamentScheduleInputMetrics getInputMetrics() {
        return new TournamentScheduleInputMetrics(teams.size(), days.size(), teamAssignments.size(),
                unavailabilityPenalties.size());
    }

    @Override
    public TournamentScheduleOutputMetrics getOutputMetrics() {
        int assigned = (int) teamAssignments.stream().filter(TeamAssignment::isAssigned).count();
        int unassigned = teamAssignments.size() - assigned;
        int usedTeams = (int) teamAssignments.stream().filter(TeamAssignment::isAssigned)
                .map(TeamAssignment::getTeam).distinct().count();
        int usedDays = (int) teamAssignments.stream().filter(TeamAssignment::isAssigned)
                .map(TeamAssignment::getDay).distinct().count();
        return new TournamentScheduleOutputMetrics(assigned, unassigned, usedTeams, usedDays);
    }

    @Override
    public String toString() {
        return "TournamentSchedule{teamAssignments: " + teamAssignments.size() + ", score: " + score + '}';
    }
}
