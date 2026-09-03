package org.acme.sportsleagueschedule.service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;

import ai.timefold.solver.core.api.domain.solution.ConstraintWeightOverrides;
import ai.timefold.solver.core.api.score.HardSoftScore;
import ai.timefold.solver.service.definition.api.ModelConvertor;
import ai.timefold.solver.service.definition.api.domain.ModelConfig;

import org.acme.sportsleagueschedule.domain.LeagueSchedule;
import org.acme.sportsleagueschedule.domain.LeagueScheduleConstraintProperties;
import org.acme.sportsleagueschedule.domain.Match;
import org.acme.sportsleagueschedule.domain.Round;
import org.acme.sportsleagueschedule.domain.Team;
import org.acme.sportsleagueschedule.dto.input.LeagueScheduleConfigOverrides;
import org.acme.sportsleagueschedule.dto.input.LeagueScheduleInput;
import org.acme.sportsleagueschedule.dto.input.MatchInputDTO;
import org.acme.sportsleagueschedule.dto.input.RoundInputDTO;
import org.acme.sportsleagueschedule.dto.input.TeamInputDTO;
import org.acme.sportsleagueschedule.dto.output.LeagueScheduleOutput;
import org.acme.sportsleagueschedule.dto.output.MatchAssignmentDTO;

@ApplicationScoped
public class LeagueScheduleModelConvertor implements
        ModelConvertor<HardSoftScore, LeagueScheduleInput, LeagueScheduleConfigOverrides, LeagueSchedule, LeagueScheduleOutput> {

    @Override
    public LeagueSchedule toSolverModel(LeagueScheduleInput modelInput,
            ModelConfig<LeagueScheduleConfigOverrides> modelConfig,
            Optional<LeagueScheduleOutput> lastModelOutput) {
        Map<Integer, Round> roundMap = modelInput.rounds().stream()
                .map(LeagueScheduleModelConvertor::toRound)
                .collect(Collectors.toMap(Round::getIndex, round -> round, (first, second) -> first,
                        LinkedHashMap::new));
        Map<String, Team> teamMap = modelInput.teams().stream()
                .map(LeagueScheduleModelConvertor::toTeam)
                .collect(Collectors.toMap(Team::getId, team -> team, (first, second) -> first, LinkedHashMap::new));
        // Distances are resolved in a second pass, because they reference the very teams being built.
        modelInput.teams().forEach(dto -> applyDistances(dto, teamMap));

        List<Match> matches = modelInput.matches().stream()
                .map(dto -> toMatch(dto, teamMap, roundMap))
                .toList();

        LeagueSchedule schedule = new LeagueSchedule(List.copyOf(roundMap.values()), List.copyOf(teamMap.values()),
                matches);
        applyConstraintWeightOverrides(schedule, modelConfig);
        applyLastOutput(matches, roundMap, lastModelOutput);
        return schedule;
    }

    @Override
    public LeagueScheduleOutput toModelOutput(LeagueSchedule solverModel) {
        List<MatchAssignmentDTO> matches = solverModel.getMatches().stream()
                .map(match -> new MatchAssignmentDTO(match.getId(),
                        match.getRound() == null ? null : match.getRound().getIndex()))
                .toList();
        return new LeagueScheduleOutput(matches);
    }

    @Override
    public LeagueScheduleInput applyOutputToInput(LeagueScheduleInput modelInput, LeagueScheduleOutput modelOutput) {
        Map<String, MatchAssignmentDTO> outputMatches = modelOutput.matches().stream()
                .collect(Collectors.toMap(MatchAssignmentDTO::id, match -> match));
        List<MatchInputDTO> updatedMatches = modelInput.matches().stream()
                .map(match -> {
                    MatchAssignmentDTO solved = outputMatches.get(match.id());
                    return solved == null ? match : match.withRoundIndex(solved.roundIndex());
                })
                .toList();
        return modelInput.withMatches(updatedMatches);
    }

    private static Round toRound(RoundInputDTO dto) {
        return new Round(dto.index(), Boolean.TRUE.equals(dto.weekendOrHoliday()));
    }

    private static Team toTeam(TeamInputDTO dto) {
        return new Team(dto.id(), dto.name());
    }

    private static void applyDistances(TeamInputDTO dto, Map<String, Team> teamMap) {
        Team team = require(teamMap, dto.id(), "team");
        Map<Team, Integer> distances = new LinkedHashMap<>();
        dto.distanceToTeam()
                .forEach((otherTeamId, distance) -> distances.put(require(teamMap, otherTeamId, "team"), distance));
        team.setDistanceToTeam(distances);
    }

    private static Match toMatch(MatchInputDTO dto, Map<String, Team> teamMap, Map<Integer, Round> roundMap) {
        Match match = new Match(dto.id(), require(teamMap, dto.homeTeamId(), "team"),
                require(teamMap, dto.awayTeamId(), "team"), Boolean.TRUE.equals(dto.classicMatch()));
        if (dto.roundIndex() != null) {
            match.setRound(require(roundMap, dto.roundIndex(), "round"));
        }
        return match;
    }

    /**
     * Fails fast with an actionable message instead of letting an unknown reference
     * turn into a null in the solver model and a delayed NullPointerException.
     */
    private static <K, T> T require(Map<K, T> map, K key, String kind) {
        T value = map.get(key);
        if (value == null) {
            throw new IllegalArgumentException("Unknown %s '%s'.".formatted(kind, key));
        }
        return value;
    }

    private static void applyConstraintWeightOverrides(LeagueSchedule schedule,
            ModelConfig<LeagueScheduleConfigOverrides> modelConfig) {
        if (modelConfig == null || modelConfig.overrides() == null) {
            return;
        }
        var overrides = modelConfig.overrides();
        // Only apply weights that are actually set (non-null) in the merged overrides. A null weight means the
        // input did not override it, so the configuration profile value (or the constraint's default) is kept.
        Map<String, HardSoftScore> weights = new HashMap<>();
        putIfPresent(weights, LeagueScheduleConstraintProperties.START_TO_AWAY_HOP, overrides.startToAwayHopWeight());
        putIfPresent(weights, LeagueScheduleConstraintProperties.HOME_TO_AWAY_HOP, overrides.homeToAwayHopWeight());
        putIfPresent(weights, LeagueScheduleConstraintProperties.AWAY_TO_AWAY_HOP, overrides.awayToAwayHopWeight());
        putIfPresent(weights, LeagueScheduleConstraintProperties.AWAY_TO_HOME_HOP, overrides.awayToHomeHopWeight());
        putIfPresent(weights, LeagueScheduleConstraintProperties.AWAY_TO_END_HOP, overrides.awayToEndHopWeight());
        putIfPresent(weights, LeagueScheduleConstraintProperties.CLASSIC_MATCHES, overrides.classicMatchesWeight());
        if (!weights.isEmpty()) {
            schedule.setConstraintWeightOverrides(ConstraintWeightOverrides.of(weights));
        }
    }

    private static void putIfPresent(Map<String, HardSoftScore> weights, String constraintName, Long weight) {
        if (weight != null) {
            weights.put(constraintName, HardSoftScore.ofSoft(weight));
        }
    }

    // lastModelOutput is used to recover a run that stopped halfway, so it overrides the input assignment.
    private static void applyLastOutput(List<Match> matches, Map<Integer, Round> roundMap,
            Optional<LeagueScheduleOutput> lastModelOutput) {
        if (lastModelOutput.isEmpty()) {
            return;
        }
        Map<String, Match> matchMap = matches.stream().collect(Collectors.toMap(Match::getId, match -> match));
        for (var solved : lastModelOutput.get().matches()) {
            Match match = matchMap.get(solved.id());
            if (match == null || solved.roundIndex() == null) {
                continue;
            }
            Round round = roundMap.get(solved.roundIndex());
            if (round != null) {
                match.setRound(round);
            }
        }
    }
}
