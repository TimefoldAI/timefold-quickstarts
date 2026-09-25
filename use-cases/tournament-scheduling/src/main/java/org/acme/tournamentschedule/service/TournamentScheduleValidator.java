package org.acme.tournamentschedule.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import jakarta.enterprise.context.ApplicationScoped;

import ai.timefold.solver.service.definition.api.domain.ModelConfig;
import ai.timefold.solver.service.definition.api.validation.ModelValidator;
import ai.timefold.solver.service.definition.api.validation.ValidationBuilder;

import org.acme.tournamentschedule.dto.input.MatchInputDTO;
import org.acme.tournamentschedule.dto.input.TeamInputDTO;
import org.acme.tournamentschedule.dto.input.TournamentScheduleConfigOverrides;
import org.acme.tournamentschedule.dto.input.TournamentScheduleInput;
import org.acme.tournamentschedule.dto.input.UnavailabilityInputDTO;
import org.acme.tournamentschedule.service.validation.TournamentScheduleIssue;
import org.acme.tournamentschedule.service.validation.TournamentScheduleIssue.DuplicateMatchIdIssue;
import org.acme.tournamentschedule.service.validation.TournamentScheduleIssue.DuplicateTeamIdIssue;
import org.acme.tournamentschedule.service.validation.TournamentScheduleIssue.DuplicateUnavailabilityIssue;
import org.acme.tournamentschedule.service.validation.TournamentScheduleIssue.NonExistingTeamReferenceInMatchIssue;
import org.acme.tournamentschedule.service.validation.TournamentScheduleIssue.NonExistingTeamReferenceInUnavailabilityIssue;

@ApplicationScoped
public class TournamentScheduleValidator implements ModelValidator<TournamentScheduleInput, TournamentScheduleConfigOverrides> {

    @Override
    public void validate(ValidationBuilder validationBuilder, TournamentScheduleInput modelInput,
            ModelConfig<TournamentScheduleConfigOverrides> modelConfig) {
        // OpenAPI spec (Bean Validation) compliance is enforced by the Service module at the REST layer,
        // before this validator ever runs; only domain-specific checks belong here.
        Set<String> teamIds = collectIds(validationBuilder, orEmpty(modelInput.teams()), TeamInputDTO::id,
                DuplicateTeamIdIssue::new);
        validateUnavailabilities(validationBuilder, orEmpty(modelInput.unavailabilities()), teamIds);
        validateMatches(validationBuilder, orEmpty(modelInput.matches()), teamIds);
    }

    /**
     * Collects the ids of {@code elements}, reporting one duplicate issue per repeated id.
     *
     * @return the distinct, non-blank ids, so the reference checks below can resolve against them
     */
    private static <T> Set<String> collectIds(ValidationBuilder validationBuilder, List<T> elements,
            Function<T, String> idExtractor, Function<String, TournamentScheduleIssue> duplicateIssueFactory) {
        Set<String> ids = new HashSet<>();
        for (T element : elements) {
            String id = idExtractor.apply(element);
            if (hasId(id) && !ids.add(id)) {
                validationBuilder.addIssue(duplicateIssueFactory.apply(id));
            }
        }
        return ids;
    }

    private static void validateUnavailabilities(ValidationBuilder validationBuilder,
            List<UnavailabilityInputDTO> unavailabilities, Set<String> teamIds) {
        Set<String> seen = new HashSet<>();
        for (UnavailabilityInputDTO unavailability : unavailabilities) {
            if (unavailability.teamId() != null && !teamIds.contains(unavailability.teamId())) {
                validationBuilder.addIssue(new NonExistingTeamReferenceInUnavailabilityIssue(unavailability.teamId()));
                continue;
            }
            if (unavailability.teamId() == null || unavailability.date() == null) {
                continue;
            }
            String key = unavailability.teamId() + '@' + unavailability.date();
            if (!seen.add(key)) {
                validationBuilder
                        .addIssue(new DuplicateUnavailabilityIssue(unavailability.teamId(), unavailability.date()));
            }
        }
    }

    private static void validateMatches(ValidationBuilder validationBuilder, List<MatchInputDTO> matches,
            Set<String> teamIds) {
        Set<String> matchIds = new HashSet<>();
        for (MatchInputDTO match : matches) {
            if (hasId(match.id()) && !matchIds.add(match.id())) {
                validationBuilder.addIssue(new DuplicateMatchIdIssue(match.id()));
                continue;
            }
            if (match.teamId() != null && !teamIds.contains(match.teamId())) {
                validationBuilder.addIssue(new NonExistingTeamReferenceInMatchIssue(match.id()));
            }
        }
    }

    private static <T> List<T> orEmpty(List<T> list) {
        return list == null ? List.of() : list;
    }

    private static boolean hasId(String id) {
        return id != null && !id.isBlank();
    }
}
