package org.acme.tournamentschedule.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.LongFunction;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;

import ai.timefold.solver.core.api.domain.solution.ConstraintWeightOverrides;
import ai.timefold.solver.core.api.score.HardMediumSoftScore;
import ai.timefold.solver.service.definition.api.ModelConvertor;
import ai.timefold.solver.service.definition.api.domain.ModelConfig;

import org.acme.tournamentschedule.domain.Day;
import org.acme.tournamentschedule.domain.Team;
import org.acme.tournamentschedule.domain.TeamAssignment;
import org.acme.tournamentschedule.domain.TournamentSchedule;
import org.acme.tournamentschedule.domain.UnavailabilityPenalty;
import org.acme.tournamentschedule.dto.DayDTO;
import org.acme.tournamentschedule.dto.TeamAssignmentDTO;
import org.acme.tournamentschedule.dto.TeamDTO;
import org.acme.tournamentschedule.dto.TournamentScheduleConfigOverrides;
import org.acme.tournamentschedule.dto.TournamentScheduleInput;
import org.acme.tournamentschedule.dto.TournamentScheduleOutput;
import org.acme.tournamentschedule.dto.UnavailabilityPenaltyDTO;
import org.acme.tournamentschedule.solver.TournamentScheduleConstraintProvider;

@ApplicationScoped
public class TournamentScheduleModelConvertor
        implements
        ModelConvertor<HardMediumSoftScore, TournamentScheduleInput, TournamentScheduleConfigOverrides, TournamentSchedule, TournamentScheduleOutput> {

    @Override
    public TournamentScheduleInput applyOutputToInput(TournamentScheduleInput modelInput,
            TournamentScheduleOutput modelOutput) {
        Map<String, TeamAssignmentDTO> outputAssignments = modelOutput.teamAssignments().stream()
                .collect(Collectors.toMap(TeamAssignmentDTO::id, assignment -> assignment));
        List<TeamAssignmentDTO> updatedAssignments = modelInput.teamAssignments().stream()
                .map(assignment -> {
                    TeamAssignmentDTO solved = outputAssignments.get(assignment.id());
                    if (solved == null) {
                        return assignment;
                    }
                    return assignment.withTeamId(solved.teamId() == null ? "" : solved.teamId());
                })
                .collect(Collectors.toList());
        return new TournamentScheduleInput(modelInput.teams(), modelInput.days(), modelInput.unavailabilityPenalties(),
                updatedAssignments);
    }

    @Override
    public TournamentSchedule toSolverModel(TournamentScheduleInput modelInput,
            ModelConfig<TournamentScheduleConfigOverrides> modelConfig,
            Optional<TournamentScheduleOutput> lastModelOutput) {
        Map<String, Team> teamMap = new HashMap<>();
        List<Team> teams = modelInput.teams().stream().map(dto -> {
            Team team = new Team(dto.id(), dto.name());
            teamMap.put(team.getId(), team);
            return team;
        }).collect(Collectors.toList());

        Map<Integer, Day> dayMap = new HashMap<>();
        List<Day> days = modelInput.days().stream().map(dto -> {
            Day day = new Day(dto.dateIndex());
            dayMap.put(day.getDateIndex(), day);
            return day;
        }).collect(Collectors.toList());

        List<UnavailabilityPenalty> unavailabilityPenalties = modelInput.unavailabilityPenalties().stream()
                .map(dto -> new UnavailabilityPenalty(teamMap.get(dto.teamId()), dayMap.get(dto.dateIndex())))
                .collect(Collectors.toList());

        List<TeamAssignment> teamAssignments = modelInput.teamAssignments().stream().map(dto -> {
            TeamAssignment assignment = new TeamAssignment(dto.id(), dayMap.get(dto.dateIndex()), dto.indexInDay());
            assignment.setPinned(dto.pinned());
            if (dto.teamId() != null) {
                assignment.setTeam(teamMap.get(dto.teamId()));
            }
            return assignment;
        }).collect(Collectors.toList());

        TournamentSchedule schedule =
                new TournamentSchedule(teams, days, unavailabilityPenalties, teamAssignments);
        applyConstraintWeightOverrides(schedule, modelConfig);
        applyLastOutput(teamAssignments, teamMap, lastModelOutput);
        return schedule;
    }

    private static void applyConstraintWeightOverrides(TournamentSchedule schedule,
            ModelConfig<TournamentScheduleConfigOverrides> modelConfig) {
        if (modelConfig == null || modelConfig.overrides() == null) {
            return;
        }
        TournamentScheduleConfigOverrides overrides = modelConfig.overrides();
        // Only apply weights that are actually set (non-null) in the merged overrides. A null weight means the
        // input did not override it, so the configuration profile value (or the constraint's default) is kept.
        Map<String, HardMediumSoftScore> weights = new HashMap<>();
        putIfPresent(weights, TournamentScheduleConstraintProvider.FAIR_ASSIGNMENT_COUNT_PER_TEAM,
                overrides.fairAssignmentCountPerTeamWeight(), HardMediumSoftScore::ofMedium);
        putIfPresent(weights, TournamentScheduleConstraintProvider.EVENLY_CONFRONTATION_COUNT,
                overrides.evenlyConfrontationCountWeight(), HardMediumSoftScore::ofSoft);
        if (!weights.isEmpty()) {
            schedule.setConstraintWeightOverrides(ConstraintWeightOverrides.of(weights));
        }
    }

    private static void putIfPresent(Map<String, HardMediumSoftScore> weights, String constraintName, Long weight,
            LongFunction<HardMediumSoftScore> scoreFactory) {
        if (weight != null) {
            weights.put(constraintName, scoreFactory.apply(weight));
        }
    }

    private static void applyLastOutput(List<TeamAssignment> teamAssignments, Map<String, Team> teamMap,
            Optional<TournamentScheduleOutput> lastModelOutput) {
        if (lastModelOutput.isEmpty()) {
            return;
        }
        Map<String, TeamAssignmentDTO> assignmentMap = lastModelOutput.get().teamAssignments().stream()
                .collect(Collectors.toMap(TeamAssignmentDTO::id, assignment -> assignment));
        for (TeamAssignment assignment : teamAssignments) {
            TeamAssignmentDTO solved = assignmentMap.get(assignment.getId());
            if (solved != null && solved.teamId() != null) {
                assignment.setTeam(teamMap.get(solved.teamId()));
            }
        }
    }

    @Override
    public TournamentScheduleOutput toModelOutput(TournamentSchedule solverModel) {
        List<TeamDTO> teams = solverModel.getTeams().stream().map(this::toDTO).collect(Collectors.toList());
        List<DayDTO> days = solverModel.getDays().stream().map(this::toDTO).collect(Collectors.toList());
        List<UnavailabilityPenaltyDTO> unavailabilityPenalties = solverModel.getUnavailabilityPenalties().stream()
                .map(this::toDTO).collect(Collectors.toList());
        List<TeamAssignmentDTO> teamAssignments =
                solverModel.getTeamAssignments().stream().map(this::toDTO).collect(Collectors.toList());
        String score = solverModel.getScore() == null ? "" : solverModel.getScore().toString();
        return new TournamentScheduleOutput(teams, days, unavailabilityPenalties, teamAssignments, score);
    }

    private TeamDTO toDTO(Team team) {
        return new TeamDTO(team.getId(), team.getName());
    }

    private DayDTO toDTO(Day day) {
        return new DayDTO(day.getDateIndex());
    }

    private UnavailabilityPenaltyDTO toDTO(UnavailabilityPenalty penalty) {
        return new UnavailabilityPenaltyDTO(penalty.getTeam().getId(), penalty.getDay().getDateIndex());
    }

    private TeamAssignmentDTO toDTO(TeamAssignment assignment) {
        String teamId = assignment.getTeam() == null ? null : assignment.getTeam().getId();
        return new TeamAssignmentDTO(assignment.getId(), assignment.getDay().getDateIndex(),
                assignment.getIndexInDay(), assignment.isPinned(), teamId);
    }
}
