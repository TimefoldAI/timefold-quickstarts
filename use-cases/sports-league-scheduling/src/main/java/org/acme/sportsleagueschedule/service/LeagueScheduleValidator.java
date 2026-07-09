package org.acme.sportsleagueschedule.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;

import ai.timefold.solver.service.definition.api.domain.ModelConfig;
import ai.timefold.solver.service.definition.api.validation.ModelValidator;
import ai.timefold.solver.service.definition.api.validation.ValidationBuilder;

import org.acme.sportsleagueschedule.dto.LeagueScheduleConfigOverrides;
import org.acme.sportsleagueschedule.dto.LeagueScheduleInput;
import org.acme.sportsleagueschedule.dto.MatchDTO;
import org.acme.sportsleagueschedule.dto.MatchIdDetail;
import org.acme.sportsleagueschedule.dto.TeamDTO;
import org.acme.sportsleagueschedule.dto.TeamIdDetail;
import org.acme.sportsleagueschedule.service.LeagueScheduleIssues.DuplicateMatchIdIssue;
import org.acme.sportsleagueschedule.service.LeagueScheduleIssues.DuplicateTeamIdIssue;
import org.acme.sportsleagueschedule.service.LeagueScheduleIssues.MatchIdMissingIssue;
import org.acme.sportsleagueschedule.service.LeagueScheduleIssues.NonExistingTeamReferenceIssue;
import org.acme.sportsleagueschedule.service.LeagueScheduleIssues.TeamIdMissingIssue;

@ApplicationScoped
public class LeagueScheduleValidator
        implements ModelValidator<LeagueScheduleInput, LeagueScheduleConfigOverrides> {

    @Override
    public void validate(ValidationBuilder validationBuilder, LeagueScheduleInput modelInput,
            ModelConfig<LeagueScheduleConfigOverrides> modelConfig) {
        Set<String> teamIds = validateTeams(validationBuilder, modelInput.teams());
        validateMatches(validationBuilder, modelInput.matches(), teamIds);
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

    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private void validateMatches(ValidationBuilder validationBuilder, List<MatchDTO> matches, Set<String> teamIds) {
        Set<String> matchIds = new HashSet<>();
        for (MatchDTO match : matches) {
            if (match.id() == null || match.id().isBlank()) {
                validationBuilder.addIssue(new MatchIdMissingIssue());
            } else if (!matchIds.add(match.id())) {
                validationBuilder.addIssue(new DuplicateMatchIdIssue(new MatchIdDetail(match.id())));
            }
            if (match.homeTeamId() != null && !teamIds.contains(match.homeTeamId())) {
                validationBuilder.addIssue(new NonExistingTeamReferenceIssue(new MatchIdDetail(match.id())));
            }
            if (match.awayTeamId() != null && !teamIds.contains(match.awayTeamId())) {
                validationBuilder.addIssue(new NonExistingTeamReferenceIssue(new MatchIdDetail(match.id())));
            }
        }
    }
}
