package org.acme.sportsleagueschedule.solver;

import static org.acme.sportsleagueschedule.support.TestHelper.aMatch;
import static org.acme.sportsleagueschedule.support.TestHelper.aRound;
import static org.acme.sportsleagueschedule.support.TestHelper.aTeam;

import jakarta.inject.Inject;

import ai.timefold.solver.core.api.score.stream.test.ConstraintVerifier;

import org.acme.sportsleagueschedule.domain.LeagueSchedule;
import org.acme.sportsleagueschedule.support.TestHelper.RoundBuilder;
import org.acme.sportsleagueschedule.support.TestHelper.TeamBuilder;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class SportsLeagueSchedulingConstraintProviderTest {

    private static final TeamBuilder FIRST_TEAM = aTeam("1").name("Cruzeiro");
    private static final TeamBuilder SECOND_TEAM = aTeam("2").name("Boca Juniors");
    private static final TeamBuilder THIRD_TEAM = aTeam("3").name("Flamengo");

    private static final RoundBuilder ROUND_0 = aRound(0);
    private static final RoundBuilder ROUND_1 = aRound(1);
    private static final RoundBuilder ROUND_2 = aRound(2);
    private static final RoundBuilder ROUND_3 = aRound(3);

    @Inject
    ConstraintVerifier<SportsLeagueSchedulingConstraintProvider, LeagueSchedule> constraintVerifier;

    @Test
    void matchesOnSameDay() {
        // Two matches on the same round with the same home team.
        constraintVerifier.verifyThat(SportsLeagueSchedulingConstraintProvider::matchesOnSameDay)
                .given(aMatch("1").homeTeam(FIRST_TEAM).awayTeam(SECOND_TEAM).round(ROUND_0).build(),
                        aMatch("2").homeTeam(FIRST_TEAM).awayTeam(THIRD_TEAM).round(ROUND_0).build())
                .penalizesBy(1);
        // A team playing at home in one match and away in the other, still on the same round.
        constraintVerifier.verifyThat(SportsLeagueSchedulingConstraintProvider::matchesOnSameDay)
                .given(aMatch("1").homeTeam(FIRST_TEAM).awayTeam(SECOND_TEAM).round(ROUND_0).build(),
                        aMatch("2").homeTeam(SECOND_TEAM).awayTeam(THIRD_TEAM).round(ROUND_0).build())
                .penalizesBy(1);
        // The same two matches on different rounds are fine.
        constraintVerifier.verifyThat(SportsLeagueSchedulingConstraintProvider::matchesOnSameDay)
                .given(aMatch("1").homeTeam(FIRST_TEAM).awayTeam(SECOND_TEAM).round(ROUND_0).build(),
                        aMatch("2").homeTeam(FIRST_TEAM).awayTeam(THIRD_TEAM).round(ROUND_1).build())
                .penalizesBy(0);
    }

    @Test
    void multipleConsecutiveHomeMatches() {
        constraintVerifier.verifyThat(SportsLeagueSchedulingConstraintProvider::multipleConsecutiveHomeMatches)
                .given(FIRST_TEAM.build(), SECOND_TEAM.build(),
                        aMatch("1").homeTeam(FIRST_TEAM).awayTeam(SECOND_TEAM).round(ROUND_0).build(),
                        aMatch("2").homeTeam(FIRST_TEAM).awayTeam(SECOND_TEAM).round(ROUND_1).build(),
                        aMatch("3").homeTeam(FIRST_TEAM).awayTeam(SECOND_TEAM).round(ROUND_2).build(),
                        aMatch("4").homeTeam(FIRST_TEAM).awayTeam(SECOND_TEAM).round(ROUND_3).build())
                .penalizesBy(4);
        // Three in a row is still allowed.
        constraintVerifier.verifyThat(SportsLeagueSchedulingConstraintProvider::multipleConsecutiveHomeMatches)
                .given(FIRST_TEAM.build(), SECOND_TEAM.build(),
                        aMatch("1").homeTeam(FIRST_TEAM).awayTeam(SECOND_TEAM).round(ROUND_0).build(),
                        aMatch("2").homeTeam(FIRST_TEAM).awayTeam(SECOND_TEAM).round(ROUND_1).build(),
                        aMatch("3").homeTeam(FIRST_TEAM).awayTeam(SECOND_TEAM).round(ROUND_2).build())
                .penalizesBy(0);
    }

    @Test
    void multipleConsecutiveAwayMatches() {
        constraintVerifier.verifyThat(SportsLeagueSchedulingConstraintProvider::multipleConsecutiveAwayMatches)
                .given(FIRST_TEAM.build(), SECOND_TEAM.build(),
                        aMatch("1").homeTeam(FIRST_TEAM).awayTeam(SECOND_TEAM).round(ROUND_0).build(),
                        aMatch("2").homeTeam(FIRST_TEAM).awayTeam(SECOND_TEAM).round(ROUND_1).build(),
                        aMatch("3").homeTeam(FIRST_TEAM).awayTeam(SECOND_TEAM).round(ROUND_2).build(),
                        aMatch("4").homeTeam(FIRST_TEAM).awayTeam(SECOND_TEAM).round(ROUND_3).build())
                .penalizesBy(4);
        constraintVerifier.verifyThat(SportsLeagueSchedulingConstraintProvider::multipleConsecutiveAwayMatches)
                .given(FIRST_TEAM.build(), SECOND_TEAM.build(),
                        aMatch("1").homeTeam(FIRST_TEAM).awayTeam(SECOND_TEAM).round(ROUND_0).build(),
                        aMatch("2").homeTeam(FIRST_TEAM).awayTeam(SECOND_TEAM).round(ROUND_1).build(),
                        aMatch("3").homeTeam(FIRST_TEAM).awayTeam(SECOND_TEAM).round(ROUND_2).build())
                .penalizesBy(0);
    }

    @Test
    void repeatMatchOnTheNextDay() {
        // The reverse fixture right on the next round.
        constraintVerifier.verifyThat(SportsLeagueSchedulingConstraintProvider::repeatMatchOnTheNextDay)
                .given(aMatch("1").homeTeam(FIRST_TEAM).awayTeam(SECOND_TEAM).round(ROUND_0).build(),
                        aMatch("2").homeTeam(SECOND_TEAM).awayTeam(FIRST_TEAM).round(ROUND_1).build())
                .penalizesBy(1);
        // Two rounds apart is fine.
        constraintVerifier.verifyThat(SportsLeagueSchedulingConstraintProvider::repeatMatchOnTheNextDay)
                .given(aMatch("1").homeTeam(FIRST_TEAM).awayTeam(SECOND_TEAM).round(ROUND_0).build(),
                        aMatch("2").homeTeam(SECOND_TEAM).awayTeam(FIRST_TEAM).round(ROUND_2).build())
                .penalizesBy(0);
    }

    @Test
    void startToAwayHop() {
        // Only the match on the opening round counts: its away team travels there from home.
        TeamBuilder secondTeam = aTeam("2").distanceTo(FIRST_TEAM, 5);
        constraintVerifier.verifyThat(SportsLeagueSchedulingConstraintProvider::startToAwayHop)
                .given(ROUND_0.build(), ROUND_1.build(),
                        aMatch("1").homeTeam(FIRST_TEAM).awayTeam(secondTeam).round(ROUND_0).build(),
                        aMatch("2").homeTeam(FIRST_TEAM).awayTeam(THIRD_TEAM).round(ROUND_1).build())
                .penalizesBy(5);
    }

    @Test
    void homeToAwayHop() {
        TeamBuilder firstTeam = aTeam("1").distanceTo(THIRD_TEAM, 7);
        constraintVerifier.verifyThat(SportsLeagueSchedulingConstraintProvider::homeToAwayHop)
                .given(ROUND_0.build(), ROUND_1.build(),
                        aMatch("1").homeTeam(firstTeam).awayTeam(SECOND_TEAM).round(ROUND_0).build(),
                        aMatch("2").homeTeam(THIRD_TEAM).awayTeam(firstTeam).round(ROUND_1).build())
                .penalizesBy(7);
    }

    @Test
    void awayToAwayHop() {
        TeamBuilder secondTeam = aTeam("2").distanceTo(THIRD_TEAM, 2);
        constraintVerifier.verifyThat(SportsLeagueSchedulingConstraintProvider::awayToAwayHop)
                .given(ROUND_0.build(), ROUND_1.build(),
                        aMatch("1").homeTeam(secondTeam).awayTeam(FIRST_TEAM).round(ROUND_0).build(),
                        aMatch("2").homeTeam(THIRD_TEAM).awayTeam(FIRST_TEAM).round(ROUND_1).build())
                .penalizesBy(2);
    }

    @Test
    void awayToHomeHop() {
        TeamBuilder secondTeam = aTeam("2").distanceTo(FIRST_TEAM, 20);
        constraintVerifier.verifyThat(SportsLeagueSchedulingConstraintProvider::awayToHomeHop)
                .given(ROUND_0.build(), ROUND_1.build(),
                        aMatch("1").homeTeam(secondTeam).awayTeam(FIRST_TEAM).round(ROUND_0).build(),
                        aMatch("2").homeTeam(FIRST_TEAM).awayTeam(THIRD_TEAM).round(ROUND_1).build())
                .penalizesBy(20);
    }

    @Test
    void awayToEndHop() {
        // Only the match on the closing round counts: its away team travels back home afterwards.
        TeamBuilder thirdTeam = aTeam("3").distanceTo(FIRST_TEAM, 15);
        constraintVerifier.verifyThat(SportsLeagueSchedulingConstraintProvider::awayToEndHop)
                .given(ROUND_0.build(), ROUND_1.build(),
                        aMatch("1").homeTeam(FIRST_TEAM).awayTeam(SECOND_TEAM).round(ROUND_0).build(),
                        aMatch("2").homeTeam(thirdTeam).awayTeam(FIRST_TEAM).round(ROUND_1).build())
                .penalizesBy(15);
    }

    @Test
    void classicMatches() {
        constraintVerifier.verifyThat(SportsLeagueSchedulingConstraintProvider::classicMatches)
                .given(aMatch("1").homeTeam(FIRST_TEAM).awayTeam(SECOND_TEAM).classicMatch(true).round(ROUND_0).build(),
                        aMatch("2").homeTeam(SECOND_TEAM).awayTeam(FIRST_TEAM).round(ROUND_1).build(),
                        aMatch("3").homeTeam(FIRST_TEAM).awayTeam(THIRD_TEAM).classicMatch(true)
                                .round(aRound(2).weekendOrHoliday(true)).build())
                // Only the classic match on a regular round is penalized, once per match.
                .penalizesBy(1);
    }
}
