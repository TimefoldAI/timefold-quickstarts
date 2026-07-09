package org.acme.sportsleagueschedule.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;

import ai.timefold.solver.core.api.domain.solution.ConstraintWeightOverrides;
import ai.timefold.solver.core.api.score.HardMediumSoftScore;
import ai.timefold.solver.service.definition.api.ModelConvertor;
import ai.timefold.solver.service.definition.api.domain.ModelConfig;

import org.acme.sportsleagueschedule.domain.LeagueSchedule;
import org.acme.sportsleagueschedule.domain.Match;
import org.acme.sportsleagueschedule.domain.Round;
import org.acme.sportsleagueschedule.domain.Team;
import org.acme.sportsleagueschedule.dto.LeagueScheduleConfigOverrides;
import org.acme.sportsleagueschedule.dto.LeagueScheduleInput;
import org.acme.sportsleagueschedule.dto.LeagueScheduleOutput;
import org.acme.sportsleagueschedule.dto.MatchDTO;
import org.acme.sportsleagueschedule.dto.RoundDTO;
import org.acme.sportsleagueschedule.dto.TeamDTO;
import org.acme.sportsleagueschedule.solver.SportsLeagueSchedulingConstraintProvider;

@ApplicationScoped
public class LeagueScheduleModelConvertor
        implements
        ModelConvertor<HardMediumSoftScore, LeagueScheduleInput, LeagueScheduleConfigOverrides, LeagueSchedule, LeagueScheduleOutput> {

    @Override
    public LeagueScheduleInput applyOutputToInput(LeagueScheduleInput modelInput, LeagueScheduleOutput modelOutput) {
        Map<String, MatchDTO> outputMatches = modelOutput.matches().stream()
                .collect(Collectors.toMap(MatchDTO::id, match -> match));
        List<MatchDTO> updatedMatches = modelInput.matches().stream()
                .map(match -> {
                    MatchDTO solved = outputMatches.get(match.id());
                    return solved == null ? match : match.withRoundIndex(solved.roundIndex());
                })
                .collect(Collectors.toList());
        return new LeagueScheduleInput(modelInput.rounds(), modelInput.teams(), updatedMatches);
    }

    @Override
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public LeagueSchedule toSolverModel(LeagueScheduleInput modelInput,
            ModelConfig<LeagueScheduleConfigOverrides> modelConfig,
            Optional<LeagueScheduleOutput> lastModelOutput) {
        Map<Integer, Round> roundMap = new HashMap<>();
        List<Round> rounds = modelInput.rounds().stream().map(dto -> {
            Round round = new Round(dto.index(), dto.weekendOrHoliday());
            roundMap.put(round.getIndex(), round);
            return round;
        }).collect(Collectors.toList());

        Map<String, Team> teamMap = new HashMap<>();
        List<Team> teams = modelInput.teams().stream().map(dto -> {
            Team team = new Team(dto.id(), dto.name());
            teamMap.put(team.getId(), team);
            return team;
        }).collect(Collectors.toList());
        // Second pass: wire the distance graph now that every team exists.
        for (TeamDTO dto : modelInput.teams()) {
            Team team = teamMap.get(dto.id());
            Map<Team, Integer> distances = new HashMap<>();
            dto.distanceToTeamId().forEach((otherId, distance) -> {
                Team other = teamMap.get(otherId);
                if (other != null) {
                    distances.put(other, distance);
                }
            });
            team.setDistanceToTeam(distances);
        }

        List<Match> matches = modelInput.matches().stream()
                .map(dto -> toMatch(dto, teamMap, roundMap))
                .collect(Collectors.toList());

        LeagueSchedule schedule = new LeagueSchedule(rounds, teams, matches);
        applyConstraintWeightOverrides(schedule, modelConfig);
        applyLastOutput(matches, roundMap, lastModelOutput);
        return schedule;
    }

    private static Match toMatch(MatchDTO dto, Map<String, Team> teamMap, Map<Integer, Round> roundMap) {
        Team homeTeam = dto.homeTeamId() == null ? null : teamMap.get(dto.homeTeamId());
        Team awayTeam = dto.awayTeamId() == null ? null : teamMap.get(dto.awayTeamId());
        Match match = new Match(dto.id(), homeTeam, awayTeam, dto.classicMatch());
        if (dto.roundIndex() != null) {
            match.setRound(roundMap.get(dto.roundIndex()));
        }
        return match;
    }

    private static void applyConstraintWeightOverrides(LeagueSchedule schedule,
            ModelConfig<LeagueScheduleConfigOverrides> modelConfig) {
        if (modelConfig == null || modelConfig.overrides() == null) {
            return;
        }
        LeagueScheduleConfigOverrides overrides = modelConfig.overrides();
        // Only apply weights that are actually set (non-null) in the merged overrides. A null weight means the
        // input did not override it, so the configuration profile value (or the constraint's default) is kept.
        Map<String, HardMediumSoftScore> weights = new HashMap<>();
        putIfPresent(weights, SportsLeagueSchedulingConstraintProvider.START_TO_AWAY_HOP,
                overrides.startToAwayHopWeight());
        putIfPresent(weights, SportsLeagueSchedulingConstraintProvider.HOME_TO_AWAY_HOP,
                overrides.homeToAwayHopWeight());
        putIfPresent(weights, SportsLeagueSchedulingConstraintProvider.AWAY_TO_AWAY_HOP,
                overrides.awayToAwayHopWeight());
        putIfPresent(weights, SportsLeagueSchedulingConstraintProvider.AWAY_TO_HOME_HOP,
                overrides.awayToHomeHopWeight());
        putIfPresent(weights, SportsLeagueSchedulingConstraintProvider.AWAY_TO_END_HOP,
                overrides.awayToEndHopWeight());
        putIfPresent(weights, SportsLeagueSchedulingConstraintProvider.CLASSIC_MATCHES,
                overrides.classicMatchesWeight());
        if (!weights.isEmpty()) {
            schedule.setConstraintWeightOverrides(ConstraintWeightOverrides.of(weights));
        }
    }

    private static void putIfPresent(Map<String, HardMediumSoftScore> weights, String constraintName, Long weight) {
        if (weight != null) {
            weights.put(constraintName, HardMediumSoftScore.ofSoft(weight));
        }
    }

    private static void applyLastOutput(List<Match> matches, Map<Integer, Round> roundMap,
            Optional<LeagueScheduleOutput> lastModelOutput) {
        if (lastModelOutput.isEmpty()) {
            return;
        }
        Map<String, MatchDTO> assignmentMap = lastModelOutput.get().matches().stream()
                .collect(Collectors.toMap(MatchDTO::id, match -> match));
        for (Match match : matches) {
            MatchDTO solved = assignmentMap.get(match.getId());
            if (solved != null && solved.roundIndex() != null) {
                match.setRound(roundMap.get(solved.roundIndex()));
            }
        }
    }

    @Override
    public LeagueScheduleOutput toModelOutput(LeagueSchedule solverModel) {
        List<RoundDTO> rounds = solverModel.getRounds().stream().map(this::toDTO).collect(Collectors.toList());
        List<TeamDTO> teams = solverModel.getTeams().stream().map(this::toDTO).collect(Collectors.toList());
        List<MatchDTO> matches = solverModel.getMatches().stream().map(this::toDTO).collect(Collectors.toList());
        String score = solverModel.getScore() == null ? "" : solverModel.getScore().toString();
        return new LeagueScheduleOutput(rounds, teams, matches, score);
    }

    private RoundDTO toDTO(Round round) {
        return new RoundDTO(round.getIndex(), round.isWeekendOrHoliday());
    }

    private TeamDTO toDTO(Team team) {
        Map<String, Integer> distanceToTeamId = new HashMap<>();
        if (team.getDistanceToTeam() != null) {
            team.getDistanceToTeam().forEach((other, distance) -> distanceToTeamId.put(other.getId(), distance));
        }
        return new TeamDTO(team.getId(), team.getName(), distanceToTeamId);
    }

    private MatchDTO toDTO(Match match) {
        String homeTeamId = match.getHomeTeam() == null ? null : match.getHomeTeam().getId();
        String awayTeamId = match.getAwayTeam() == null ? null : match.getAwayTeam().getId();
        Integer roundIndex = match.getRound() == null ? null : match.getRound().getIndex();
        return new MatchDTO(match.getId(), homeTeamId, awayTeamId, match.isClassicMatch(), roundIndex);
    }
}
