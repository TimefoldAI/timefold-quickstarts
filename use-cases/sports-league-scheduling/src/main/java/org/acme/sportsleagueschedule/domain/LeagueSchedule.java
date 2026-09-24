package org.acme.sportsleagueschedule.domain;

import java.util.List;

import ai.timefold.solver.core.api.domain.solution.ConstraintWeightOverrides;
import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.HardSoftScore;
import ai.timefold.solver.service.definition.api.SolverModel;
import ai.timefold.solver.service.definition.api.metrics.InputMetricsAware;
import ai.timefold.solver.service.definition.api.metrics.OutputMetricsAware;

import org.acme.sportsleagueschedule.dto.input.LeagueScheduleInputMetrics;
import org.acme.sportsleagueschedule.dto.output.LeagueScheduleOutputMetrics;

@PlanningSolution
public class LeagueSchedule implements SolverModel<HardSoftScore>,
        InputMetricsAware<LeagueScheduleInputMetrics>, OutputMetricsAware<LeagueScheduleOutputMetrics> {

    @ValueRangeProvider
    @ProblemFactCollectionProperty
    private List<Round> rounds;
    @ProblemFactCollectionProperty
    private List<Team> teams;
    @PlanningEntityCollectionProperty
    private List<Match> matches;

    @PlanningScore
    private HardSoftScore score;

    private ConstraintWeightOverrides<HardSoftScore> constraintWeightOverrides = ConstraintWeightOverrides.none();

    public LeagueSchedule() {
    }

    public LeagueSchedule(List<Round> rounds, List<Team> teams, List<Match> matches) {
        this.rounds = rounds;
        this.teams = teams;
        this.matches = matches;
    }

    // ************************************************************************
    // Getters and setters
    // ************************************************************************

    public List<Round> getRounds() {
        return rounds;
    }

    public List<Team> getTeams() {
        return teams;
    }

    public List<Match> getMatches() {
        return matches;
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
    public LeagueScheduleInputMetrics getInputMetrics() {
        return new LeagueScheduleInputMetrics(matches.size(), rounds.size(), teams.size());
    }

    @Override
    public LeagueScheduleOutputMetrics getOutputMetrics() {
        // Reads the planning variable itself rather than anything derived from it, so an
        // unassigned match simply does not count instead of blowing up.
        int assignedMatches = (int) matches.stream().filter(Match::isAssigned).count();
        int unassignedMatches = matches.size() - assignedMatches;
        int usedRounds = (int) matches.stream()
                .filter(Match::isAssigned)
                .map(Match::getRound)
                .distinct()
                .count();
        int classicMatchesOffPeak = (int) matches.stream()
                .filter(match -> match.isClassicMatch() && match.isAssigned())
                .filter(match -> !match.getRound().isWeekendOrHoliday())
                .count();
        return new LeagueScheduleOutputMetrics(assignedMatches, unassignedMatches, usedRounds, classicMatchesOffPeak);
    }
}
