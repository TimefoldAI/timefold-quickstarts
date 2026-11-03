package org.acme.tournamentschedule.domain;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

import org.acme.tournamentschedule.dto.input.TournamentScheduleInputMetrics;
import org.acme.tournamentschedule.dto.output.TournamentScheduleOutputMetrics;

@PlanningSolution
public class TournamentSchedule implements SolverModel<HardMediumSoftScore>,
        InputMetricsAware<TournamentScheduleInputMetrics>, OutputMetricsAware<TournamentScheduleOutputMetrics> {

    @ProblemFactCollectionProperty
    @ValueRangeProvider
    private List<Team> teams;
    @ProblemFactCollectionProperty
    private List<UnavailabilityPenalty> unavailabilityPenalties;

    @PlanningEntityCollectionProperty
    private List<TeamAssignment> teamAssignments;

    @PlanningScore
    private HardMediumSoftScore score;

    private ConstraintWeightOverrides<HardMediumSoftScore> constraintWeightOverrides = ConstraintWeightOverrides.none();

    public TournamentSchedule() {
    }

    public TournamentSchedule(List<Team> teams, List<UnavailabilityPenalty> unavailabilityPenalties,
            List<TeamAssignment> teamAssignments) {
        this.teams = teams;
        this.unavailabilityPenalties = unavailabilityPenalties;
        this.teamAssignments = teamAssignments;
    }

    public List<Team> getTeams() {
        return teams;
    }

    public List<UnavailabilityPenalty> getUnavailabilityPenalties() {
        return unavailabilityPenalties;
    }

    public List<TeamAssignment> getTeamAssignments() {
        return teamAssignments;
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
    public TournamentScheduleInputMetrics getInputMetrics() {
        return new TournamentScheduleInputMetrics(teams.size(), teamAssignments.size(), unavailabilityPenalties.size());
    }

    private static boolean isAssigned(TeamAssignment assignment) {
        return assignment.getTeam() != null;
    }

    @Override
    public TournamentScheduleOutputMetrics getOutputMetrics() {
        int assignedMatches = (int) teamAssignments.stream().filter(TournamentSchedule::isAssigned).count();
        int unassignedMatches = teamAssignments.size() - assignedMatches;
        Map<Team, Long> assignmentCountByTeam = teamAssignments.stream()
                .filter(TournamentSchedule::isAssigned)
                .collect(Collectors.groupingBy(TeamAssignment::getTeam, Collectors.counting()));
        teams.forEach(team -> assignmentCountByTeam.putIfAbsent(team, 0L));
        int assignmentCountRange = assignmentCountByTeam.isEmpty() ? 0
                : (int) (Collections.max(assignmentCountByTeam.values()) - Collections.min(assignmentCountByTeam.values()));
        return new TournamentScheduleOutputMetrics(assignedMatches, unassignedMatches, assignmentCountRange);
    }
}
