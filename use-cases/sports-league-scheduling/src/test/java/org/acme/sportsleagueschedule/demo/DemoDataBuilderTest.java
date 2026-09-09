package org.acme.sportsleagueschedule.demo;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.IntStream;

import org.acme.sportsleagueschedule.dto.input.LeagueScheduleInput;
import org.acme.sportsleagueschedule.dto.input.MatchInputDTO;
import org.junit.jupiter.api.Test;

class DemoDataBuilderTest {

    @Test
    void shouldBuildData() {
        LeagueScheduleInput problem = DemoDataBuilder.basic();

        assertThat(problem.rounds()).hasSize(32);
        assertThat(problem.teams()).hasSize(14);
        // Every ordered pair of the 14 teams: 14 * 13 matches.
        assertThat(problem.matches()).hasSize(182);

        List<String> teamIds = problem.teams().stream().map(team -> team.id()).toList();
        problem.teams().forEach(team -> {
            assertThat(team.id()).isNotNull();
            assertThat(team.name()).isNotNull();
            // A distance to every other team, and none to itself.
            assertThat(team.distanceToTeam()).hasSize(teamIds.size() - 1);
            assertThat(team.distanceToTeam()).doesNotContainKey(team.id());
            assertThat(team.distanceToTeam().keySet()).isSubsetOf(teamIds);
            assertThat(team.distanceToTeam().values()).allMatch(distance -> distance > 0);
        });

        problem.matches().forEach(match -> {
            assertThat(match.homeTeamId()).isIn(teamIds);
            assertThat(match.awayTeamId()).isIn(teamIds);
            assertThat(match.homeTeamId()).isNotEqualTo(match.awayTeamId());
            // Unsolved: no round yet.
            assertThat(match.roundIndex()).isNull();
        });

        // Rounds are numbered from 0 upwards, without gaps.
        assertThat(problem.rounds().stream().map(round -> round.index()).toList())
                .containsExactlyElementsOf(IntStream.range(0, 32).boxed().toList());
    }

    @Test
    void shouldMarkBothDirectionsOfAClassicPairing() {
        LeagueScheduleInput problem = DemoDataBuilder.basic();
        List<MatchInputDTO> classicMatches = problem.matches().stream()
                .filter(match -> Boolean.TRUE.equals(match.classicMatch()))
                .toList();

        assertThat(classicMatches).isNotEmpty();
        classicMatches.forEach(match -> assertThat(problem.matches())
                .anyMatch(reverse -> reverse.homeTeamId().equals(match.awayTeamId())
                        && reverse.awayTeamId().equals(match.homeTeamId())
                        && Boolean.TRUE.equals(reverse.classicMatch())));
    }

    @Test
    void shouldBuildTheSameDataTwice() {
        // The dataset is seeded, so two builds only differ in the weekend rounds, which are anchored to today.
        assertThat(DemoDataBuilder.basic()).isEqualTo(DemoDataBuilder.basic());
    }
}
