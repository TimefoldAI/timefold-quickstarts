package org.acme.sportsleagueschedule.service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;

import ai.timefold.solver.service.definition.api.domain.ModelConfig;
import ai.timefold.solver.service.definition.api.validation.ModelValidator;
import ai.timefold.solver.service.definition.api.validation.ValidationBuilder;

import org.acme.sportsleagueschedule.dto.input.LeagueScheduleConfigOverrides;
import org.acme.sportsleagueschedule.dto.input.LeagueScheduleInput;
import org.acme.sportsleagueschedule.dto.input.MatchInputDTO;
import org.acme.sportsleagueschedule.dto.input.RoundInputDTO;
import org.acme.sportsleagueschedule.dto.input.TeamInputDTO;
import org.acme.sportsleagueschedule.service.validation.LeagueScheduleIssue.DuplicateMatchIdIssue;
import org.acme.sportsleagueschedule.service.validation.LeagueScheduleIssue.DuplicateRoundIndexIssue;
import org.acme.sportsleagueschedule.service.validation.LeagueScheduleIssue.DuplicateTeamIdIssue;
import org.acme.sportsleagueschedule.service.validation.LeagueScheduleIssue.MissingDistanceIssue;
import org.acme.sportsleagueschedule.service.validation.LeagueScheduleIssue.NonExistingRoundReferenceIssue;
import org.acme.sportsleagueschedule.service.validation.LeagueScheduleIssue.NonExistingTeamReferenceIssue;
import org.acme.sportsleagueschedule.service.validation.LeagueScheduleIssue.SameHomeAndAwayTeamIssue;

@ApplicationScoped
public class LeagueScheduleValidator implements ModelValidator<LeagueScheduleInput, LeagueScheduleConfigOverrides> {

    @Override
    public void validate(ValidationBuilder validationBuilder, LeagueScheduleInput modelInput,
            ModelConfig<LeagueScheduleConfigOverrides> modelConfig) {
        // OpenAPI spec (Bean Validation) compliance is enforced by the Service module at the REST layer,
        // before this validator ever runs; only domain-specific checks belong here.
        Set<Integer> roundIndexes = validateRounds(validationBuilder, orEmpty(modelInput.rounds()));
        Set<String> teamIds = validateTeams(validationBuilder, orEmpty(modelInput.teams()));
        validateMatches(validationBuilder, orEmpty(modelInput.matches()), teamIds, roundIndexes);
    }

    private Set<Integer> validateRounds(ValidationBuilder validationBuilder, List<RoundInputDTO> rounds) {
        Set<Integer> roundIndexes = new HashSet<>();
        for (var round : rounds) {
            if (round.index() != null && !roundIndexes.add(round.index())) {
                validationBuilder.addIssue(new DuplicateRoundIndexIssue(round.index()));
            }
        }
        return roundIndexes;
    }

    private Set<String> validateTeams(ValidationBuilder validationBuilder, List<TeamInputDTO> teams) {
        Set<String> teamIds = new HashSet<>();
        for (var team : teams) {
            if (hasId(team.id()) && !teamIds.add(team.id())) {
                validationBuilder.addIssue(new DuplicateTeamIdIssue(team.id()));
            }
        }
        // The travel constraints look up a distance for every pair of teams, so a gap there
        // silently turns into a free hop. Reported per team, not per missing pair.
        for (var team : teams) {
            if (hasId(team.id()) && isMissingADistance(team, teamIds)) {
                validationBuilder.addIssue(new MissingDistanceIssue(team.id()));
            }
        }
        return teamIds;
    }

    private void validateMatches(ValidationBuilder validationBuilder, List<MatchInputDTO> matches, Set<String> teamIds,
            Set<Integer> roundIndexes) {
        Set<String> matchIds = new HashSet<>();
        for (var match : matches) {
            // At most one issue per match, so a single misconfigured match cannot flood the report.
            if (hasId(match.id()) && !matchIds.add(match.id())) {
                validationBuilder.addIssue(new DuplicateMatchIdIssue(match.id()));
            } else if (!teamIds.contains(match.homeTeamId()) || !teamIds.contains(match.awayTeamId())) {
                validationBuilder.addIssue(new NonExistingTeamReferenceIssue(match.id()));
            } else if (match.homeTeamId().equals(match.awayTeamId())) {
                validationBuilder.addIssue(new SameHomeAndAwayTeamIssue(match.id()));
            } else if (match.roundIndex() != null && !roundIndexes.contains(match.roundIndex())) {
                validationBuilder.addIssue(new NonExistingRoundReferenceIssue(match.id()));
            }
        }
    }

    private static boolean isMissingADistance(TeamInputDTO team, Set<String> teamIds) {
        Map<String, Integer> distances = team.distanceToTeam();
        if (distances == null) {
            return true;
        }
        return teamIds.stream()
                .filter(otherTeamId -> !otherTeamId.equals(team.id()))
                .anyMatch(otherTeamId -> distances.get(otherTeamId) == null);
    }

    private static <T> List<T> orEmpty(List<T> list) {
        return list == null ? List.of() : list;
    }

    private static boolean hasId(String id) {
        return id != null && !id.isBlank();
    }
}
