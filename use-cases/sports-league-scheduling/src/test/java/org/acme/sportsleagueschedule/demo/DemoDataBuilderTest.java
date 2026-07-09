package org.acme.sportsleagueschedule.demo;

import static org.assertj.core.api.Assertions.assertThat;

import org.acme.sportsleagueschedule.dto.LeagueScheduleInput;
import org.junit.jupiter.api.Test;

class DemoDataBuilderTest {

    @Test
    void buildsConsistentProblem() {
        LeagueScheduleInput problem = DemoDataBuilder.builder()
                .setRoundCount(20)
                .build();

        assertThat(problem.rounds()).hasSize(20);
        assertThat(problem.teams()).hasSize(14);
        // Full double round-robin between 14 teams = 14 * 13 matches.
        assertThat(problem.matches()).hasSize(14 * 13);
        assertThat(problem.matches()).allSatisfy(match -> {
            assertThat(match.id()).isNotBlank();
            assertThat(match.homeTeamId()).isNotBlank();
            assertThat(match.awayTeamId()).isNotBlank();
            assertThat(match.roundIndex()).isNull();
        });
        assertThat(problem.teams()).allSatisfy(team -> assertThat(team.distanceToTeamId()).hasSize(13));
    }
}
