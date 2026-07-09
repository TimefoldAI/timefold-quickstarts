package org.acme.tournamentschedule.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;

import ai.timefold.solver.service.definition.api.domain.ModelConfig;
import ai.timefold.solver.service.definition.api.validation.ModelValidator;
import ai.timefold.solver.service.definition.api.validation.ValidationBuilder;

import org.acme.tournamentschedule.dto.DayDTO;
import org.acme.tournamentschedule.dto.TeamAssignmentDTO;
import org.acme.tournamentschedule.dto.TeamAssignmentIdDetail;
import org.acme.tournamentschedule.dto.TeamDTO;
import org.acme.tournamentschedule.dto.TeamIdDetail;
import org.acme.tournamentschedule.dto.TournamentScheduleConfigOverrides;
import org.acme.tournamentschedule.dto.TournamentScheduleInput;
import org.acme.tournamentschedule.service.TournamentScheduleIssues.DuplicateTeamAssignmentIdIssue;
import org.acme.tournamentschedule.service.TournamentScheduleIssues.DuplicateTeamIdIssue;
import org.acme.tournamentschedule.service.TournamentScheduleIssues.NonExistingDayReferenceIssue;
import org.acme.tournamentschedule.service.TournamentScheduleIssues.NonExistingTeamReferenceIssue;
import org.acme.tournamentschedule.service.TournamentScheduleIssues.TeamAssignmentIdMissingIssue;
import org.acme.tournamentschedule.service.TournamentScheduleIssues.TeamIdMissingIssue;

@ApplicationScoped
public class TournamentScheduleValidator
        implements ModelValidator<TournamentScheduleInput, TournamentScheduleConfigOverrides> {

    @Override
    public void validate(ValidationBuilder validationBuilder, TournamentScheduleInput modelInput,
            ModelConfig<TournamentScheduleConfigOverrides> modelConfig) {
        Set<String> teamIds = validateTeams(validationBuilder, modelInput.teams());
        Set<Integer> dayIndexes = collectDayIndexes(modelInput.days());
        validateTeamAssignments(validationBuilder, modelInput.teamAssignments(), teamIds, dayIndexes);
    }

    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private Set<String> validateTeams(ValidationBuilder validationBuilder, List<TeamDTO> teams) {
        Set<String> teamIds = new HashSet<>();
        for (TeamDTO team : teams) {
            if (team.id() == null || team.id().isBlank()) {
                validationBuilder.addIssue(new TeamIdMissingIssue());
            } else if (!teamIds.add(team.id())) {
                validationBuilder.addIssue(new DuplicateTeamIdIssue(new TeamIdDetail(team.id())));
            }
        }
        return teamIds;
    }

    private Set<Integer> collectDayIndexes(List<DayDTO> days) {
        Set<Integer> dayIndexes = new HashSet<>();
        for (DayDTO day : days) {
            dayIndexes.add(day.dateIndex());
        }
        return dayIndexes;
    }

    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private void validateTeamAssignments(ValidationBuilder validationBuilder, List<TeamAssignmentDTO> teamAssignments,
            Set<String> teamIds, Set<Integer> dayIndexes) {
        Set<String> assignmentIds = new HashSet<>();
        for (TeamAssignmentDTO assignment : teamAssignments) {
            if (assignment.id() == null || assignment.id().isBlank()) {
                validationBuilder.addIssue(new TeamAssignmentIdMissingIssue());
            } else if (!assignmentIds.add(assignment.id())) {
                validationBuilder
                        .addIssue(new DuplicateTeamAssignmentIdIssue(new TeamAssignmentIdDetail(assignment.id())));
            }
            if (assignment.teamId() != null && !teamIds.contains(assignment.teamId())) {
                validationBuilder
                        .addIssue(new NonExistingTeamReferenceIssue(new TeamAssignmentIdDetail(assignment.id())));
            }
            if (!dayIndexes.contains(assignment.dateIndex())) {
                validationBuilder
                        .addIssue(new NonExistingDayReferenceIssue(new TeamAssignmentIdDetail(assignment.id())));
            }
        }
    }
}
