package org.acme.sportsleagueschedule;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.acme.sportsleagueschedule.dto.LeagueScheduleConfigOverrides;
import org.acme.sportsleagueschedule.dto.LeagueScheduleInput;
import org.acme.sportsleagueschedule.dto.LeagueScheduleInputMetrics;
import org.acme.sportsleagueschedule.dto.LeagueScheduleOutput;
import org.acme.sportsleagueschedule.dto.LeagueScheduleOutputMetrics;
import org.acme.sportsleagueschedule.dto.MatchDTO;
import org.acme.sportsleagueschedule.dto.MatchIdDetail;
import org.acme.sportsleagueschedule.dto.RoundDTO;
import org.acme.sportsleagueschedule.dto.TeamDTO;
import org.acme.sportsleagueschedule.dto.TeamIdDetail;
import org.junit.jupiter.api.Test;

class DtoWithMethodsUsageTest {

    @Test
    void allWithMethodsProduceUpdatedCopies() {
        var baseRound = new RoundDTO(0, false);
        var updatedRound = baseRound.withIndex(5).withWeekendOrHoliday(true);

        var baseTeam = new TeamDTO("t1", "Team 1", Map.of("t2", 100));
        var updatedTeam = baseTeam
                .withId("t2")
                .withName("Team 2")
                .withDistanceToTeamId(Map.of("t1", 200));

        Integer unassignedRound = null;
        var baseMatch = new MatchDTO("m1", "t1", "t2", false, unassignedRound);
        var updatedMatch = baseMatch
                .withId("m2")
                .withHomeTeamId("t3")
                .withAwayTeamId("t4")
                .withClassicMatch(true)
                .withRoundIndex(7);

        var updatedMatchIdDetail = new MatchIdDetail("m1").withMatchId("m2");
        var updatedTeamIdDetail = new TeamIdDetail("t1").withTeamId("t2");

        var updatedOverrides = new LeagueScheduleConfigOverrides()
                .withStartToAwayHopWeight(11L)
                .withHomeToAwayHopWeight(22L)
                .withAwayToAwayHopWeight(33L)
                .withAwayToHomeHopWeight(44L)
                .withAwayToEndHopWeight(55L)
                .withClassicMatchesWeight(66L);

        var updatedInput = new LeagueScheduleInput(List.of(baseRound), List.of(baseTeam), List.of(baseMatch))
                .withRounds(List.of(updatedRound))
                .withTeams(List.of(updatedTeam))
                .withMatches(List.of(updatedMatch));

        var updatedOutput = new LeagueScheduleOutput(List.of(baseRound), List.of(baseTeam), List.of(baseMatch), "0hard/0soft")
                .withRounds(List.of(updatedRound))
                .withTeams(List.of(updatedTeam))
                .withMatches(List.of(updatedMatch))
                .withScore("1hard/0soft");

        var updatedInputMetrics = new LeagueScheduleInputMetrics(1, 2, 3)
                .withMatches(10)
                .withTeams(20)
                .withRounds(30);

        var updatedOutputMetrics = new LeagueScheduleOutputMetrics(1, 2, 3)
                .withTotalAssignedMatches(10)
                .withTotalUnassignedMatches(20)
                .withTotalUsedRounds(30);

        assertThat(updatedRound.index()).isEqualTo(5);
        assertThat(updatedRound.weekendOrHoliday()).isTrue();
        assertThat(updatedTeam.id()).isEqualTo("t2");
        assertThat(updatedTeam.name()).isEqualTo("Team 2");
        assertThat(updatedTeam.distanceToTeamId()).containsEntry("t1", 200);
        assertThat(updatedMatch.id()).isEqualTo("m2");
        assertThat(updatedMatch.homeTeamId()).isEqualTo("t3");
        assertThat(updatedMatch.awayTeamId()).isEqualTo("t4");
        assertThat(updatedMatch.classicMatch()).isTrue();
        assertThat(updatedMatch.roundIndex()).isEqualTo(7);
        assertThat(updatedMatchIdDetail.matchId()).isEqualTo("m2");
        assertThat(updatedTeamIdDetail.teamId()).isEqualTo("t2");
        assertThat(updatedOverrides.startToAwayHopWeight()).isEqualTo(11L);
        assertThat(updatedOverrides.homeToAwayHopWeight()).isEqualTo(22L);
        assertThat(updatedOverrides.awayToAwayHopWeight()).isEqualTo(33L);
        assertThat(updatedOverrides.awayToHomeHopWeight()).isEqualTo(44L);
        assertThat(updatedOverrides.awayToEndHopWeight()).isEqualTo(55L);
        assertThat(updatedOverrides.classicMatchesWeight()).isEqualTo(66L);
        assertThat(updatedInput.rounds()).containsExactly(updatedRound);
        assertThat(updatedInput.teams()).containsExactly(updatedTeam);
        assertThat(updatedInput.matches()).containsExactly(updatedMatch);
        assertThat(updatedOutput.rounds()).containsExactly(updatedRound);
        assertThat(updatedOutput.teams()).containsExactly(updatedTeam);
        assertThat(updatedOutput.matches()).containsExactly(updatedMatch);
        assertThat(updatedOutput.score()).isEqualTo("1hard/0soft");
        assertThat(updatedInputMetrics.matches()).isEqualTo(10);
        assertThat(updatedInputMetrics.teams()).isEqualTo(20);
        assertThat(updatedInputMetrics.rounds()).isEqualTo(30);
        assertThat(updatedOutputMetrics.totalAssignedMatches()).isEqualTo(10);
        assertThat(updatedOutputMetrics.totalUnassignedMatches()).isEqualTo(20);
        assertThat(updatedOutputMetrics.totalUsedRounds()).isEqualTo(30);
    }
}
