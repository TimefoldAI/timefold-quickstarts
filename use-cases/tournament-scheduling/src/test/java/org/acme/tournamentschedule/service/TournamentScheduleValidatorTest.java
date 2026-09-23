package org.acme.tournamentschedule.service;

import static org.acme.tournamentschedule.support.TestHelper.aMatchDTO;
import static org.acme.tournamentschedule.support.TestHelper.aTeamDTO;
import static org.acme.tournamentschedule.support.TestHelper.anUnavailabilityDTO;
import static org.acme.tournamentschedule.support.TestHelper.createProblem;
import static org.acme.tournamentschedule.support.TestHelper.input;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import ai.timefold.solver.service.definition.api.domain.ModelConfig;
import ai.timefold.solver.service.definition.api.validation.Issue;
import ai.timefold.solver.service.definition.api.validation.ValidationBuilder;
import ai.timefold.solver.service.definition.api.validation.dto.ValidationResult;

import org.acme.tournamentschedule.demo.DemoDataBuilder;
import org.acme.tournamentschedule.dto.input.MatchInputDTO;
import org.acme.tournamentschedule.dto.input.TeamInputDTO;
import org.acme.tournamentschedule.dto.input.TournamentScheduleInput;
import org.acme.tournamentschedule.dto.input.UnavailabilityInputDTO;
import org.acme.tournamentschedule.service.validation.TournamentScheduleIssue.DuplicateMatchIdIssue;
import org.acme.tournamentschedule.service.validation.TournamentScheduleIssue.DuplicateTeamIdIssue;
import org.acme.tournamentschedule.service.validation.TournamentScheduleIssue.DuplicateUnavailabilityIssue;
import org.acme.tournamentschedule.service.validation.TournamentScheduleIssue.NonExistingTeamReferenceInMatchIssue;
import org.acme.tournamentschedule.service.validation.TournamentScheduleIssue.NonExistingTeamReferenceInUnavailabilityIssue;
import org.junit.jupiter.api.Test;

// OpenAPI spec compliance (Bean Validation) is enforced by the Service module at the REST layer, so it's
// covered by org.acme.tournamentschedule.rest.TournamentScheduleOpenApiValidationTest instead. This class only
// covers the domain-specific checks TournamentScheduleValidator implements itself.
class TournamentScheduleValidatorTest {

    private static final List<TeamInputDTO> TEAMS = List.of(aTeamDTO("T1").build(), aTeamDTO("T2").build());
    private static final List<MatchInputDTO> VALID_MATCHES = List.of(aMatchDTO("M1").teamId("T1").build());

    private final TournamentScheduleValidator validator = new TournamentScheduleValidator();

    @Test
    void validInputHasNoIssues() {
        ValidationResult<Issue> result = validate(createProblem());
        assertThat(result.issues()).isEmpty();
    }

    @Test
    void demoDataHasNoIssues() {
        // The service must never ship demo data it would reject itself.
        ValidationResult<Issue> result = validate(DemoDataBuilder.basic());
        assertThat(result.issues()).isEmpty();
    }

    @Test
    void duplicateTeamId() {
        TeamInputDTO team = aTeamDTO("T1").build();
        TournamentScheduleInput problem = input(List.of(team, team), List.of(), VALID_MATCHES);
        assertSingleIssue(validate(problem), DuplicateTeamIdIssue.class);
    }

    @Test
    void duplicateMatchId() {
        MatchInputDTO match = aMatchDTO("M1").build();
        TournamentScheduleInput problem = input(TEAMS, List.of(), List.of(match, match));
        assertSingleIssue(validate(problem), DuplicateMatchIdIssue.class);
    }

    @Test
    void nonExistingTeamReferenceInMatch() {
        MatchInputDTO match = aMatchDTO("M1").teamId("does-not-exist").build();
        TournamentScheduleInput problem = input(TEAMS, List.of(), List.of(match));
        assertSingleIssue(validate(problem), NonExistingTeamReferenceInMatchIssue.class);
    }

    @Test
    void nonExistingTeamReferenceInUnavailability() {
        UnavailabilityInputDTO unavailability = anUnavailabilityDTO("does-not-exist").build();
        TournamentScheduleInput problem = input(TEAMS, List.of(unavailability), VALID_MATCHES);
        assertSingleIssue(validate(problem), NonExistingTeamReferenceInUnavailabilityIssue.class);
    }

    @Test
    void duplicateUnavailability() {
        UnavailabilityInputDTO unavailability = anUnavailabilityDTO("T1").date(LocalDate.of(2024, 1, 1)).build();
        TournamentScheduleInput problem = input(TEAMS, List.of(unavailability, unavailability), VALID_MATCHES);
        assertSingleIssue(validate(problem), DuplicateUnavailabilityIssue.class);
    }

    @Test
    void mixedDatasetReportsEveryIssue() {
        TeamInputDTO team = aTeamDTO("T1").build();
        MatchInputDTO match = aMatchDTO("M1").build();
        TournamentScheduleInput problem = input(List.of(team, team), List.of(anUnavailabilityDTO("nobody").build()),
                List.of(match, match, aMatchDTO("M2").teamId("nobody").build()));

        ValidationResult<Issue> result = validate(problem);
        assertThat(result.issues()).hasSize(4);
        assertThat(result.issues()).hasAtLeastOneElementOfType(DuplicateTeamIdIssue.class);
        assertThat(result.issues()).hasAtLeastOneElementOfType(DuplicateMatchIdIssue.class);
        assertThat(result.issues()).hasAtLeastOneElementOfType(NonExistingTeamReferenceInUnavailabilityIssue.class);
        assertThat(result.issues()).hasAtLeastOneElementOfType(NonExistingTeamReferenceInMatchIssue.class);
    }

    private ValidationResult<Issue> validate(TournamentScheduleInput input) {
        ValidationBuilder validationBuilder = new ValidationBuilder();
        validator.validate(validationBuilder, input, ModelConfig.empty());
        return validationBuilder.build();
    }

    private static <T extends Issue> void assertSingleIssue(ValidationResult<Issue> result, Class<T> expectedType) {
        Collection<Issue> issues = result.issues();
        assertThat(issues).hasSize(1);
        Issue issue = issues.iterator().next();
        assertThat(issue).isInstanceOf(expectedType);
    }
}
