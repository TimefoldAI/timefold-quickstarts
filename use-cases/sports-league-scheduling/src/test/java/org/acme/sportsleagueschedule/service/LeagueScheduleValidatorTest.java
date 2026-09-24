package org.acme.sportsleagueschedule.service;

import static org.acme.sportsleagueschedule.support.TestHelper.aMatchDTO;
import static org.acme.sportsleagueschedule.support.TestHelper.aRoundDTO;
import static org.acme.sportsleagueschedule.support.TestHelper.aTeamDTO;
import static org.acme.sportsleagueschedule.support.TestHelper.input;
import static org.acme.sportsleagueschedule.support.TestHelper.rounds;
import static org.acme.sportsleagueschedule.support.TestHelper.teams;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.List;

import ai.timefold.solver.service.definition.api.domain.ModelConfig;
import ai.timefold.solver.service.definition.api.validation.Issue;
import ai.timefold.solver.service.definition.api.validation.ValidationBuilder;
import ai.timefold.solver.service.definition.api.validation.dto.ValidationResult;

import org.acme.sportsleagueschedule.demo.DemoDataBuilder;
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
import org.junit.jupiter.api.Test;

// OpenAPI spec compliance (Bean Validation) is enforced by the Service module at the REST layer, so it's
// covered by org.acme.sportsleagueschedule.rest.LeagueScheduleOpenApiValidationTest instead. This class
// only covers the domain-specific checks LeagueScheduleValidator implements itself.
class LeagueScheduleValidatorTest {

    private static final List<RoundInputDTO> ROUNDS = rounds(4);
    private static final List<TeamInputDTO> TEAMS = teams(2);
    private static final List<MatchInputDTO> VALID_MATCHES = List.of(aMatchDTO("1-2").build());

    private final LeagueScheduleValidator validator = new LeagueScheduleValidator();

    @Test
    void validInputHasNoIssues() {
        LeagueScheduleInput schedule = input(ROUNDS, TEAMS, List.of(
                aMatchDTO("1-2").build(),
                aMatchDTO("2-1").homeTeamId("2").awayTeamId("1").roundIndex(2).build()));

        assertThat(validate(schedule).issues()).isEmpty();
    }

    @Test
    void demoDatasetHasNoIssues() {
        // Otherwise the service would ship demo data that its own validator rejects.
        assertThat(validate(DemoDataBuilder.basic()).issues()).isEmpty();
    }

    @Test
    void duplicateTeamId() {
        TeamInputDTO team = aTeamDTO("1").distanceTo("2", 10).build();
        LeagueScheduleInput schedule = input(ROUNDS, List.of(team, team, aTeamDTO("2").distanceTo("1", 10).build()),
                VALID_MATCHES);
        assertSingleIssue(validate(schedule), DuplicateTeamIdIssue.class);
    }

    @Test
    void duplicateMatchId() {
        MatchInputDTO match = aMatchDTO("1-2").build();
        LeagueScheduleInput schedule = input(ROUNDS, TEAMS, List.of(match, match));
        assertSingleIssue(validate(schedule), DuplicateMatchIdIssue.class);
    }

    @Test
    void duplicateRoundIndex() {
        RoundInputDTO round = aRoundDTO(0).build();
        LeagueScheduleInput schedule = input(List.of(round, round), TEAMS, VALID_MATCHES);
        assertSingleIssue(validate(schedule), DuplicateRoundIndexIssue.class);
    }

    @Test
    void nonExistingTeamReference() {
        MatchInputDTO match = aMatchDTO("1-9").awayTeamId("does-not-exist").build();
        LeagueScheduleInput schedule = input(ROUNDS, TEAMS, List.of(match));
        assertSingleIssue(validate(schedule), NonExistingTeamReferenceIssue.class);
    }

    @Test
    void sameHomeAndAwayTeam() {
        MatchInputDTO match = aMatchDTO("1-1").awayTeamId("1").build();
        LeagueScheduleInput schedule = input(ROUNDS, TEAMS, List.of(match));
        assertSingleIssue(validate(schedule), SameHomeAndAwayTeamIssue.class);
    }

    @Test
    void nonExistingRoundReference() {
        MatchInputDTO match = aMatchDTO("1-2").roundIndex(99).build();
        LeagueScheduleInput schedule = input(ROUNDS, TEAMS, List.of(match));
        assertSingleIssue(validate(schedule), NonExistingRoundReferenceIssue.class);
    }

    @Test
    void missingDistance() {
        // Team 2 knows how far team 1 is, but not the other way around.
        LeagueScheduleInput schedule = input(ROUNDS, List.of(
                aTeamDTO("1").build(),
                aTeamDTO("2").distanceTo("1", 10).build()), VALID_MATCHES);
        assertSingleIssue(validate(schedule), MissingDistanceIssue.class);
    }

    @Test
    void mixedDatasetReportsEveryIssue() {
        RoundInputDTO duplicatedRound = aRoundDTO(0).build();
        TeamInputDTO duplicatedTeam = aTeamDTO("1").distanceTo("2", 10).build();
        MatchInputDTO duplicatedMatch = aMatchDTO("1-2").build();

        LeagueScheduleInput schedule = input(
                List.of(duplicatedRound, duplicatedRound, aRoundDTO(1).build()),
                List.of(duplicatedTeam, duplicatedTeam, aTeamDTO("2").build()),
                List.of(duplicatedMatch,
                        duplicatedMatch,
                        aMatchDTO("1-9").awayTeamId("does-not-exist").build(),
                        aMatchDTO("1-1").awayTeamId("1").build(),
                        aMatchDTO("2-1").homeTeamId("2").awayTeamId("1").roundIndex(99).build()));

        Collection<Issue> issues = validate(schedule).issues();
        assertThat(issues).hasSize(7);
        assertThat(issues).hasAtLeastOneElementOfType(DuplicateRoundIndexIssue.class)
                .hasAtLeastOneElementOfType(DuplicateTeamIdIssue.class)
                .hasAtLeastOneElementOfType(MissingDistanceIssue.class)
                .hasAtLeastOneElementOfType(DuplicateMatchIdIssue.class)
                .hasAtLeastOneElementOfType(NonExistingTeamReferenceIssue.class)
                .hasAtLeastOneElementOfType(SameHomeAndAwayTeamIssue.class)
                .hasAtLeastOneElementOfType(NonExistingRoundReferenceIssue.class);
    }

    private ValidationResult<Issue> validate(LeagueScheduleInput schedule) {
        ValidationBuilder validationBuilder = new ValidationBuilder();
        validator.validate(validationBuilder, schedule, ModelConfig.empty());
        return validationBuilder.build();
    }

    private static <T extends Issue> void assertSingleIssue(ValidationResult<Issue> result, Class<T> expectedType) {
        Collection<Issue> issues = result.issues();
        assertThat(issues).hasSize(1);
        Issue issue = issues.iterator().next();
        assertThat(issue).isInstanceOf(expectedType);
    }
}
