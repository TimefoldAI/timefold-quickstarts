package org.acme.tournamentschedule.demo;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Map;
import java.util.stream.Collectors;

import org.acme.tournamentschedule.dto.input.MatchInputDTO;
import org.acme.tournamentschedule.dto.input.TeamInputDTO;
import org.acme.tournamentschedule.dto.input.TournamentScheduleInput;
import org.acme.tournamentschedule.dto.input.UnavailabilityInputDTO;
import org.junit.jupiter.api.Test;

class DemoDataBuilderTest {

    @Test
    void shouldBuildData() {
        TournamentScheduleInput problem = DemoDataBuilder.basic();

        assertThat(problem.teams()).hasSize(7);
        assertThat(problem.teams()).extracting(TeamInputDTO::id).doesNotHaveDuplicates();
        assertThat(problem.matches()).hasSize(72);
        assertThat(problem.matches()).extracting(MatchInputDTO::id).doesNotHaveDuplicates();
        assertThat(problem.unavailabilities()).hasSize(12);
    }

    @Test
    void everyMatchStartsUnassignedOverEighteenDaysOfFourMatches() {
        TournamentScheduleInput problem = DemoDataBuilder.basic();

        for (MatchInputDTO match : problem.matches()) {
            assertThat(match.teamId()).isNull();
            assertThat(match.date()).isNotNull();
            assertThat(match.pinned()).isFalse();
        }
        Map<LocalDate, Long> matchesPerDay =
                problem.matches().stream().collect(Collectors.groupingBy(MatchInputDTO::date, Collectors.counting()));
        assertThat(matchesPerDay).hasSize(18);
        assertThat(matchesPerDay.values()).allMatch(count -> count == 4);
    }

    @Test
    void unavailabilitiesReferenceExistingTeamsAndDoNotRepeat() {
        TournamentScheduleInput problem = DemoDataBuilder.basic();
        var teamIds = problem.teams().stream().map(TeamInputDTO::id).collect(Collectors.toSet());

        for (UnavailabilityInputDTO unavailability : problem.unavailabilities()) {
            assertThat(teamIds).contains(unavailability.teamId());
        }
        assertThat(problem.unavailabilities().stream().map(u -> u.teamId() + '@' + u.date()).distinct())
                .hasSameSizeAs(problem.unavailabilities());
    }
}
