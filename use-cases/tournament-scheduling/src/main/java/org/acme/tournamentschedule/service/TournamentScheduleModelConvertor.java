package org.acme.tournamentschedule.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;

import ai.timefold.solver.core.api.domain.solution.ConstraintWeightOverrides;
import ai.timefold.solver.core.api.score.HardMediumSoftScore;
import ai.timefold.solver.service.definition.api.ModelConvertor;
import ai.timefold.solver.service.definition.api.domain.ModelConfig;

import org.acme.tournamentschedule.domain.Team;
import org.acme.tournamentschedule.domain.TeamAssignment;
import org.acme.tournamentschedule.domain.TournamentSchedule;
import org.acme.tournamentschedule.domain.TournamentScheduleConstraintProperties;
import org.acme.tournamentschedule.domain.UnavailabilityPenalty;
import org.acme.tournamentschedule.dto.input.MatchInputDTO;
import org.acme.tournamentschedule.dto.input.TournamentScheduleConfigOverrides;
import org.acme.tournamentschedule.dto.input.TournamentScheduleInput;
import org.acme.tournamentschedule.dto.output.MatchOutputDTO;
import org.acme.tournamentschedule.dto.output.TournamentScheduleOutput;

@ApplicationScoped
public class TournamentScheduleModelConvertor implements
        ModelConvertor<HardMediumSoftScore, TournamentScheduleInput, TournamentScheduleConfigOverrides, TournamentSchedule, TournamentScheduleOutput> {

    @Override
    public TournamentSchedule toSolverModel(TournamentScheduleInput modelInput,
            ModelConfig<TournamentScheduleConfigOverrides> modelConfig,
            Optional<TournamentScheduleOutput> lastModelOutput) {
        Map<String, Team> teamMap = modelInput.teams().stream()
                .map(dto -> new Team(dto.id(), dto.name()))
                .collect(Collectors.toMap(Team::id, team -> team, (left, right) -> left, LinkedHashMap::new));

        List<UnavailabilityPenalty> unavailabilityPenalties = modelInput.unavailabilities().stream()
                .map(dto -> new UnavailabilityPenalty(require(teamMap, dto.teamId(), "team"), dto.date()))
                .toList();

        List<TeamAssignment> teamAssignments = new ArrayList<>(modelInput.matches().size());
        for (MatchInputDTO dto : modelInput.matches()) {
            teamAssignments.add(new TeamAssignment(dto.id(), dto.date(),
                    dto.teamId() == null ? null : require(teamMap, dto.teamId(), "team"),
                    Boolean.TRUE.equals(dto.pinned())));
        }

        var tournamentSchedule = new TournamentSchedule(List.copyOf(teamMap.values()), unavailabilityPenalties,
                teamAssignments);
        applyConstraintWeightOverrides(tournamentSchedule, modelConfig);
        applyLastOutput(teamAssignments, teamMap, lastModelOutput);
        return tournamentSchedule;
    }

    @Override
    public TournamentScheduleOutput toModelOutput(TournamentSchedule solverModel) {
        var matches = solverModel.getTeamAssignments().stream()
                .map(assignment -> new MatchOutputDTO(assignment.getId(),
                        assignment.getTeam() == null ? null : assignment.getTeam().id()))
                .toList();
        return new TournamentScheduleOutput(matches);
    }

    @Override
    public TournamentScheduleInput applyOutputToInput(TournamentScheduleInput modelInput,
            TournamentScheduleOutput modelOutput) {
        Map<String, MatchOutputDTO> outputMatches = modelOutput.matches().stream()
                .collect(Collectors.toMap(MatchOutputDTO::id, match -> match));
        List<MatchInputDTO> updatedMatches = modelInput.matches().stream()
                .map(match -> {
                    MatchOutputDTO solved = outputMatches.get(match.id());
                    return solved == null ? match : match.withTeamId(solved.teamId());
                })
                .toList();
        return modelInput.withMatches(updatedMatches);
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

    private static void applyConstraintWeightOverrides(TournamentSchedule tournamentSchedule,
            ModelConfig<TournamentScheduleConfigOverrides> modelConfig) {
        if (modelConfig == null || modelConfig.overrides() == null) {
            return;
        }
        var overrides = modelConfig.overrides();
        // Only apply weights that are actually set (non-null) in the merged overrides. A null weight means the
        // input did not override it, so the configuration profile value (or the constraint's default) is kept.
        Map<String, HardMediumSoftScore> weights = new HashMap<>();
        putIfPresent(weights, TournamentScheduleConstraintProperties.EVEN_CONFRONTATION_COUNT,
                overrides.evenConfrontationCountWeight());
        if (!weights.isEmpty()) {
            tournamentSchedule.setConstraintWeightOverrides(ConstraintWeightOverrides.of(weights));
        }
    }

    private static void putIfPresent(Map<String, HardMediumSoftScore> weights, String constraintName, Long weight) {
        if (weight != null) {
            weights.put(constraintName, HardMediumSoftScore.ofSoft(weight));
        }
    }

    // lastModelOutput is used to recover a run that stopped halfway, so it fully overrides the input assignment of
    // every match it reports. A match it does not report keeps the assignment the input gave it.
    private static void applyLastOutput(List<TeamAssignment> teamAssignments, Map<String, Team> teamMap,
            Optional<TournamentScheduleOutput> lastModelOutput) {
        if (lastModelOutput.isEmpty()) {
            return;
        }
        var assignmentMap = teamAssignments.stream()
                .collect(Collectors.toMap(TeamAssignment::getId, assignment -> assignment));
        for (var solved : lastModelOutput.get().matches()) {
            TeamAssignment assignment = assignmentMap.get(solved.id());
            if (assignment == null) {
                continue;
            }
            // Set unconditionally: a match the last run left unassigned has to end up unassigned here as well,
            // rather than falling back on whatever the input came in with.
            assignment.setTeam(solved.teamId() == null ? null : require(teamMap, solved.teamId(), "team"));
        }
    }
}
