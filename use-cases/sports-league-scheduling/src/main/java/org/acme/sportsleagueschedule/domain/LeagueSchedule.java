package org.acme.sportsleagueschedule.domain;

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

import org.acme.sportsleagueschedule.dto.LeagueScheduleInputMetrics;
import org.acme.sportsleagueschedule.dto.LeagueScheduleOutputMetrics;

@PlanningSolution
public class LeagueSchedule implements SolverModel<HardMediumSoftScore>,
        InputMetricsAware<LeagueScheduleInputMetrics>, OutputMetricsAware<LeagueScheduleOutputMetrics> {

    @ValueRangeProvider
    @ProblemFactCollectionProperty
    private List<Round> rounds;
    @ProblemFactCollectionProperty
    private List<Team> teams;
    @PlanningEntityCollectionProperty
    private List<Match> matches;
    @PlanningScore
    private HardMediumSoftScore score;

    private ConstraintWeightOverrides<HardMediumSoftScore> constraintWeightOverrides = ConstraintWeightOverrides.none();

    public LeagueSchedule() {
    }

    public LeagueSchedule(List<Round> rounds, List<Team> teams, List<Match> matches) {
        this.rounds = rounds;
        this.teams = teams;
        this.matches = matches;
    }

    public List<Round> getRounds() {
        return rounds;
    }

    public void setRounds(List<Round> rounds) {
        this.rounds = rounds;
    }

    public List<Team> getTeams() {
        return teams;
    }

    public void setTeams(List<Team> teams) {
        this.teams = teams;
    }

    public List<Match> getMatches() {
        return matches;
    }

    public void setMatches(List<Match> matches) {
        this.matches = matches;
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
    public LeagueScheduleInputMetrics getInputMetrics() {
        return new LeagueScheduleInputMetrics(matches.size(), teams.size(), rounds.size());
    }

    @Override
    public LeagueScheduleOutputMetrics getOutputMetrics() {
        int assignedMatches = (int) matches.stream().filter(match -> match.getRound() != null).count();
        int unassignedMatches = matches.size() - assignedMatches;
        int usedRounds = (int) matches.stream()
                .filter(match -> match.getRound() != null)
                .map(Match::getRound)
                .distinct()
                .count();
        return new LeagueScheduleOutputMetrics(assignedMatches, unassignedMatches, usedRounds);
    }

    @Override
    public String toString() {
        return "LeagueSchedule{matches: " + matches.size() + ", score: " + score + '}';
    }
}
