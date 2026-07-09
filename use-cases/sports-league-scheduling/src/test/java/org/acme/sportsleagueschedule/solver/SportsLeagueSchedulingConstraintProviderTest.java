package org.acme.sportsleagueschedule.solver;

import java.util.Map;

import ai.timefold.solver.core.api.score.stream.test.ConstraintVerifier;

import org.acme.sportsleagueschedule.domain.LeagueSchedule;
import org.acme.sportsleagueschedule.domain.Match;
import org.acme.sportsleagueschedule.domain.Round;
import org.acme.sportsleagueschedule.domain.Team;
import org.junit.jupiter.api.Test;

class SportsLeagueSchedulingConstraintProviderTest {

    private final ConstraintVerifier<SportsLeagueSchedulingConstraintProvider, LeagueSchedule> constraintVerifier =
            ConstraintVerifier.build(new SportsLeagueSchedulingConstraintProvider(), LeagueSchedule.class, Match.class);

    private static Team team(String id) {
        return new Team(id, "Team " + id);
    }

    private static Match match(String id, Team home, Team away, Round round) {
        Match match = new Match(id, home, away);
        match.setRound(round);
        return match;
    }

    @Test
    void matchesOnSameDay() {
        Team a = team("1");
        Team b = team("2");
        Team c = team("3");
        Round round = new Round(0);
        constraintVerifier.verifyThat(SportsLeagueSchedulingConstraintProvider::matchesOnSameDay)
                .given(match("1", a, b, round), match("2", a, c, round))
                .penalizesBy(1);
        constraintVerifier.verifyThat(SportsLeagueSchedulingConstraintProvider::matchesOnSameDay)
                .given(match("1", a, b, round), match("2", c, team("4"), round))
                .penalizes(0);
    }

    @Test
    void multipleConsecutiveHomeMatches() {
        Team a = team("1");
        Team b = team("2");
        constraintVerifier.verifyThat(SportsLeagueSchedulingConstraintProvider::multipleConsecutiveHomeMatches)
                .given(a, b,
                        match("1", a, b, new Round(0)),
                        match("2", a, b, new Round(1)),
                        match("3", a, b, new Round(2)),
                        match("4", a, b, new Round(3)))
                .penalizesBy(4);
    }

    @Test
    void multipleConsecutiveAwayMatches() {
        Team a = team("1");
        Team b = team("2");
        constraintVerifier.verifyThat(SportsLeagueSchedulingConstraintProvider::multipleConsecutiveAwayMatches)
                .given(a, b,
                        match("1", b, a, new Round(0)),
                        match("2", b, a, new Round(1)),
                        match("3", b, a, new Round(2)),
                        match("4", b, a, new Round(3)))
                .penalizesBy(4);
    }

    @Test
    void repeatMatchOnTheNextDay() {
        Team a = team("1");
        Team b = team("2");
        constraintVerifier.verifyThat(SportsLeagueSchedulingConstraintProvider::repeatMatchOnTheNextDay)
                .given(match("1", a, b, new Round(0)), match("2", b, a, new Round(1)))
                .penalizesBy(1);
        constraintVerifier.verifyThat(SportsLeagueSchedulingConstraintProvider::repeatMatchOnTheNextDay)
                .given(match("1", a, b, new Round(0)), match("2", b, a, new Round(3)))
                .penalizes(0);
    }

    @Test
    void startToAwayHop() {
        Team home = team("1");
        Team away = team("2");
        away.setDistanceToTeam(Map.of(home, 500));
        // No round at index -1 exists, so the away team travels from its base.
        constraintVerifier.verifyThat(SportsLeagueSchedulingConstraintProvider::startToAwayHop)
                .given(match("1", home, away, new Round(0)))
                .penalizesBy(500);
    }

    @Test
    void homeToAwayHop() {
        Team a = team("1");
        Team b = team("2");
        a.setDistanceToTeam(Map.of(b, 300));
        // a is home in round 0 and away in round 1: travel from a's venue to b's venue.
        constraintVerifier.verifyThat(SportsLeagueSchedulingConstraintProvider::homeToAwayHop)
                .given(match("1", a, b, new Round(0)), match("2", b, a, new Round(1)))
                .penalizesBy(300);
    }

    @Test
    void awayToAwayHop() {
        Team homeA = team("1");
        Team homeB = team("2");
        Team away = team("9");
        homeA.setDistanceToTeam(Map.of(homeB, 250));
        // away plays away at homeA (round 0) then at homeB (round 1).
        constraintVerifier.verifyThat(SportsLeagueSchedulingConstraintProvider::awayToAwayHop)
                .given(match("1", homeA, away, new Round(0)), match("2", homeB, away, new Round(1)))
                .penalizesBy(250);
    }

    @Test
    void awayToHomeHop() {
        Team opponentHome = team("1");
        Team t = team("2");
        opponentHome.setDistanceToTeam(Map.of(t, 400));
        // t plays away in round 0 and home in round 1: travel back to t's venue.
        constraintVerifier.verifyThat(SportsLeagueSchedulingConstraintProvider::awayToHomeHop)
                .given(match("1", opponentHome, t, new Round(0)), match("2", t, opponentHome, new Round(1)))
                .penalizesBy(400);
    }

    @Test
    void awayToEndHop() {
        Team home = team("1");
        Team away = team("2");
        home.setDistanceToTeam(Map.of(away, 150));
        // No round after this one, so the away team travels back from this venue.
        constraintVerifier.verifyThat(SportsLeagueSchedulingConstraintProvider::awayToEndHop)
                .given(match("1", home, away, new Round(0)))
                .penalizesBy(150);
    }

    @Test
    void classicMatches() {
        Team a = team("1");
        Team b = team("2");
        Match classicOnWeekday = new Match("1", a, b, true);
        classicOnWeekday.setRound(new Round(0, false));
        constraintVerifier.verifyThat(SportsLeagueSchedulingConstraintProvider::classicMatches)
                .given(classicOnWeekday)
                .penalizesBy(1);
        Match classicOnWeekend = new Match("2", a, b, true);
        classicOnWeekend.setRound(new Round(1, true));
        constraintVerifier.verifyThat(SportsLeagueSchedulingConstraintProvider::classicMatches)
                .given(classicOnWeekend)
                .penalizes(0);
    }
}
