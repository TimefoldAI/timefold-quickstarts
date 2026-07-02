package org.acme.sportsleagueschedule.solver;

import java.util.Map;

import jakarta.inject.Inject;

import ai.timefold.solver.core.api.score.stream.test.ConstraintVerifier;

import org.acme.sportsleagueschedule.domain.LeagueSchedule;
import org.acme.sportsleagueschedule.domain.Match;
import org.acme.sportsleagueschedule.domain.Round;
import org.acme.sportsleagueschedule.domain.Team;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class SportsLeagueSchedulingConstraintProviderTest {

    @Inject
    ConstraintVerifier<SportsLeagueSchedulingConstraintProvider, LeagueSchedule> constraintVerifier;

    @Test
    void matchesSameDay() {
        // Two matches for the home team
        Team homeTeam = new Team("1");
        Team rivalTeam = new Team("2");
        Match firstMatch = new Match("1", homeTeam, rivalTeam);
        firstMatch.setRound(new Round(0));
        Match secondMatch = new Match("2", homeTeam, rivalTeam);
        secondMatch.setRound(new Round(0));
        Match thirdMatch = new Match("3", homeTeam, rivalTeam);

        constraintVerifier.verifyThat(SportsLeagueSchedulingConstraintProvider::matchesOnSameDay)
                .given(firstMatch, secondMatch, thirdMatch)
                .penalizesBy(1);

        // Two matches, one for home and another for away match
        Team otherTeam = new Team("3");
        firstMatch = new Match("1", homeTeam, rivalTeam);
        firstMatch.setRound(new Round(0));
        secondMatch = new Match("2", rivalTeam, otherTeam);
        secondMatch.setRound(new Round(0));
        constraintVerifier.verifyThat(SportsLeagueSchedulingConstraintProvider::matchesOnSameDay)
                .given(firstMatch, secondMatch, thirdMatch)
                .penalizesBy(1);
    }

    @Test
    void multipleConsecutiveHomeMatches() {
        Team homeTeam = new Team("1");
        Team rivalTeam = new Team("2");
        Match firstMatch = new Match("1", homeTeam, rivalTeam);
        firstMatch.setRound(new Round(0));
        Match secondMatch = new Match("2", homeTeam, rivalTeam);
        secondMatch.setRound(new Round(1));
        Match thirdMatch = new Match("3", homeTeam, rivalTeam);
        thirdMatch.setRound(new Round(2));
        Match fourthMatch = new Match("4", homeTeam, rivalTeam);
        fourthMatch.setRound(new Round(3));
        Match fifthMatch = new Match("5", new Team("3"), homeTeam);

        constraintVerifier.verifyThat(SportsLeagueSchedulingConstraintProvider::multipleConsecutiveHomeMatches)
                .given(firstMatch, secondMatch, thirdMatch, fourthMatch, fifthMatch, homeTeam, rivalTeam)
                .penalizesBy(4); // four consecutive matches for homeTeam
    }

    @Test
    void multipleConsecutiveAwayMatches() {
        Team homeTeam = new Team("1");
        Team rivalTeam = new Team("2");
        Match firstMatch = new Match("1", homeTeam, rivalTeam);
        firstMatch.setRound(new Round(0));
        Match secondMatch = new Match("2", homeTeam, rivalTeam);
        secondMatch.setRound(new Round(1));
        Match thirdMatch = new Match("3", homeTeam, rivalTeam);
        thirdMatch.setRound(new Round(2));
        Match fourthMatch = new Match("4", homeTeam, rivalTeam);
        fourthMatch.setRound(new Round(3));
        Match fifthMatch = new Match("5", new Team("3"), homeTeam);

        constraintVerifier.verifyThat(SportsLeagueSchedulingConstraintProvider::multipleConsecutiveAwayMatches)
                .given(firstMatch, secondMatch, thirdMatch, fourthMatch, fifthMatch, homeTeam, rivalTeam)
                .penalizesBy(4); // four consecutive away matches for homeTeam
    }

    @Test
    void repeatMatchOnTheNextDay() {
        Team homeTeam = new Team("1");
        Team rivalTeam = new Team("2");
        Match firstMatch = new Match("1", homeTeam, rivalTeam);
        firstMatch.setRound(new Round(0));
        Match secondMatch = new Match("2", rivalTeam, homeTeam);
        secondMatch.setRound(new Round(1));
        Match thirdMatch = new Match("3", homeTeam, rivalTeam);
        thirdMatch.setRound(new Round(4));
        Match fourthMatch = new Match("4", rivalTeam, homeTeam);
        fourthMatch.setRound(new Round(6));

        constraintVerifier.verifyThat(SportsLeagueSchedulingConstraintProvider::repeatMatchOnTheNextDay)
                .given(firstMatch, secondMatch, thirdMatch, fourthMatch)
                .penalizesBy(1); // one match repeating on the next day
    }

    @Test
    void startToAwayHop() {
        Team homeTeam = new Team("1");
        Team secondTeam = new Team("2");
        secondTeam.setDistanceToTeam(Map.of(homeTeam, 5));
        Team thirdTeam = new Team("3");
        Match firstMatch = new Match("1", homeTeam, secondTeam);
        Round firstRound = new Round(0);
        firstMatch.setRound(firstRound);
        Match secondMatch = new Match("2", homeTeam, thirdTeam);
        Round secondRound = new Round(1);
        secondMatch.setRound(secondRound);

        constraintVerifier.verifyThat(SportsLeagueSchedulingConstraintProvider::startToAwayHop)
                .given(firstMatch, secondMatch, firstRound, secondRound)
                .penalizesBy(5); // match with the second team
    }

    @Test
    void homeToAwayHop() {
        Team homeTeam = new Team("1");
        Team secondTeam = new Team("2");
        Team thirdTeam = new Team("3");
        homeTeam.setDistanceToTeam(Map.of(thirdTeam, 7));
        Match firstMatch = new Match("1", homeTeam, secondTeam);
        Round firstRound = new Round(0);
        firstMatch.setRound(firstRound);
        Match secondMatch = new Match("2", thirdTeam, homeTeam);
        Round secondRound = new Round(1);
        secondMatch.setRound(secondRound);

        constraintVerifier.verifyThat(SportsLeagueSchedulingConstraintProvider::homeToAwayHop)
                .given(firstMatch, secondMatch, firstRound, secondRound)
                .penalizesBy(7); // match with the home team
    }

    @Test
    void awayToAwayHop() {
        Team homeTeam = new Team("1");
        Team secondTeam = new Team("2");
        Team thirdTeam = new Team("3");
        secondTeam.setDistanceToTeam(Map.of(thirdTeam, 2));
        Match firstMatch = new Match("1", secondTeam, homeTeam);
        Round firstRound = new Round(0);
        firstMatch.setRound(firstRound);
        Match secondMatch = new Match("2", thirdTeam, homeTeam);
        Round secondRound = new Round(1);
        secondMatch.setRound(secondRound);

        constraintVerifier.verifyThat(SportsLeagueSchedulingConstraintProvider::awayToAwayHop)
                .given(firstMatch, secondMatch, firstRound, secondRound)
                .penalizesBy(2); // match with the home team
    }

    @Test
    void awayToHomeHop() {
        Team homeTeam = new Team("1");
        Team secondTeam = new Team("2");
        Team thirdTeam = new Team("3");
        secondTeam.setDistanceToTeam(Map.of(homeTeam, 20));
        Match firstMatch = new Match("1", secondTeam, homeTeam);
        Round firstRound = new Round(0);
        firstMatch.setRound(firstRound);
        Match secondMatch = new Match("2", homeTeam, thirdTeam);
        Round secondRound = new Round(1);
        secondMatch.setRound(secondRound);

        constraintVerifier.verifyThat(SportsLeagueSchedulingConstraintProvider::awayToHomeHop)
                .given(firstMatch, secondMatch, firstRound, secondRound)
                .penalizesBy(20); // match with the home team
    }

    @Test
    void awayToEndHop() {
        Team homeTeam = new Team("1");
        Team secondTeam = new Team("2");
        Team thirdTeam = new Team("3");
        thirdTeam.setDistanceToTeam(Map.of(homeTeam, 15));
        Match firstMatch = new Match("1", homeTeam, secondTeam);
        Round firstRound = new Round(0);
        firstMatch.setRound(firstRound);
        Match secondMatch = new Match("2", thirdTeam, homeTeam);
        Round secondRound = new Round(1);
        secondMatch.setRound(secondRound);

        constraintVerifier.verifyThat(SportsLeagueSchedulingConstraintProvider::awayToEndHop)
                .given(firstMatch, secondMatch, firstRound, secondRound)
                .penalizesBy(15); // match with the home team
    }

    @Test
    void classicMatches() {
        Team homeTeam = new Team("1");
        Team rivalTeam = new Team("2");
        Match firstMatch = new Match("1", homeTeam, rivalTeam, true);
        firstMatch.setRound(new Round(0, false));
        Match secondMatch = new Match("2", rivalTeam, homeTeam, false);
        secondMatch.setRound(new Round(1, false));
        Match thirdMatch = new Match("3", homeTeam, rivalTeam, true);
        thirdMatch.setRound(new Round(4, false));

        constraintVerifier.verifyThat(SportsLeagueSchedulingConstraintProvider::classicMatches)
                .given(firstMatch, secondMatch, thirdMatch)
                .penalizesBy(2); // two classic matches
    }
}
