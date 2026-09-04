package org.acme.sportsleagueschedule.solver;

import static ai.timefold.solver.core.api.score.stream.Joiners.equal;
import static ai.timefold.solver.core.api.score.stream.Joiners.filtering;

import java.util.function.Function;

import ai.timefold.solver.core.api.score.HardSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintCollectors;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.core.api.score.stream.common.SequenceChain;
import ai.timefold.solver.service.definition.api.description.ConstraintInfo;

import org.acme.sportsleagueschedule.domain.LeagueScheduleConstraintProperties;
import org.acme.sportsleagueschedule.domain.Match;
import org.acme.sportsleagueschedule.domain.Round;
import org.acme.sportsleagueschedule.domain.Team;
import org.acme.sportsleagueschedule.domain.justification.LeagueScheduleJustification.AwayToAwayHopJustification;
import org.acme.sportsleagueschedule.domain.justification.LeagueScheduleJustification.AwayToEndHopJustification;
import org.acme.sportsleagueschedule.domain.justification.LeagueScheduleJustification.AwayToHomeHopJustification;
import org.acme.sportsleagueschedule.domain.justification.LeagueScheduleJustification.ClassicMatchesJustification;
import org.acme.sportsleagueschedule.domain.justification.LeagueScheduleJustification.ConsecutiveAwayMatchesJustification;
import org.acme.sportsleagueschedule.domain.justification.LeagueScheduleJustification.ConsecutiveHomeMatchesJustification;
import org.acme.sportsleagueschedule.domain.justification.LeagueScheduleJustification.HomeToAwayHopJustification;
import org.acme.sportsleagueschedule.domain.justification.LeagueScheduleJustification.MatchesOnSameDayJustification;
import org.acme.sportsleagueschedule.domain.justification.LeagueScheduleJustification.RepeatMatchOnTheNextDayJustification;
import org.acme.sportsleagueschedule.domain.justification.LeagueScheduleJustification.StartToAwayHopJustification;

public class SportsLeagueSchedulingConstraintProvider implements ConstraintProvider {

    private static final int MAX_CONSECUTIVE_MATCHES = 4;

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
                // Hard constraints
                matchesOnSameDay(constraintFactory),
                multipleConsecutiveHomeMatches(constraintFactory),
                multipleConsecutiveAwayMatches(constraintFactory),
                repeatMatchOnTheNextDay(constraintFactory),

                // Soft constraints
                startToAwayHop(constraintFactory),
                homeToAwayHop(constraintFactory),
                awayToAwayHop(constraintFactory),
                awayToHomeHop(constraintFactory),
                awayToEndHop(constraintFactory),
                classicMatches(constraintFactory)
        };
    }

    // ************************************************************************
    // Hard constraints
    // ************************************************************************

    protected Constraint matchesOnSameDay(ConstraintFactory constraintFactory) {
        // A team can play at most one match per matchday, at home or away.
        return constraintFactory
                .forEachUniquePair(Match.class,
                        equal(Match::getRoundIndex),
                        filtering((match1, match2) -> match1.getHomeTeam().equals(match2.getHomeTeam())
                                || match1.getHomeTeam().equals(match2.getAwayTeam())
                                || match1.getAwayTeam().equals(match2.getHomeTeam())
                                || match1.getAwayTeam().equals(match2.getAwayTeam())))
                .penalize(HardSoftScore.ONE_HARD)
                .justifyWith((match, otherMatch, score) -> MatchesOnSameDayJustification.of(match, otherMatch))
                .asConstraint(new ConstraintInfo(LeagueScheduleConstraintProperties.MATCHES_ON_SAME_DAY,
                        LeagueScheduleConstraintProperties.MATCHES_ON_SAME_DAY,
                        "A team must not play two matches on the same matchday.",
                        LeagueScheduleConstraintGroup.SCHEDULE_CONFLICTS));
    }

    protected Constraint multipleConsecutiveHomeMatches(ConstraintFactory constraintFactory) {
        // Playing at home for too many matchdays in a row is unfair to the opponents' supporters.
        return constraintFactory.forEach(Match.class)
                .join(Team.class, equal(Match::getHomeTeam, Function.identity()))
                .groupBy((match, team) -> team,
                        ConstraintCollectors.toConsecutiveSequences((match, team) -> match.getRound(), Round::getIndex))
                .flattenLast(SequenceChain::getConsecutiveSequences)
                .filter((team, rounds) -> rounds.getCount() >= MAX_CONSECUTIVE_MATCHES)
                .penalize(HardSoftScore.ONE_HARD, (team, rounds) -> rounds.getCount())
                .justifyWith((team, rounds, score) -> ConsecutiveHomeMatchesJustification.of(team, rounds))
                .asConstraint(new ConstraintInfo(LeagueScheduleConstraintProperties.FOUR_CONSECUTIVE_HOME_MATCHES,
                        LeagueScheduleConstraintProperties.FOUR_CONSECUTIVE_HOME_MATCHES,
                        "A team must not play four or more consecutive matchdays at its own venue.",
                        LeagueScheduleConstraintGroup.TEAM_FAIRNESS));
    }

    protected Constraint multipleConsecutiveAwayMatches(ConstraintFactory constraintFactory) {
        // Being on the road for too many matchdays in a row wears a team out.
        return constraintFactory.forEach(Match.class)
                .join(Team.class, equal(Match::getAwayTeam, Function.identity()))
                .groupBy((match, team) -> team,
                        ConstraintCollectors.toConsecutiveSequences((match, team) -> match.getRound(), Round::getIndex))
                .flattenLast(SequenceChain::getConsecutiveSequences)
                .filter((team, rounds) -> rounds.getCount() >= MAX_CONSECUTIVE_MATCHES)
                .penalize(HardSoftScore.ONE_HARD, (team, rounds) -> rounds.getCount())
                .justifyWith((team, rounds, score) -> ConsecutiveAwayMatchesJustification.of(team, rounds))
                .asConstraint(new ConstraintInfo(LeagueScheduleConstraintProperties.FOUR_CONSECUTIVE_AWAY_MATCHES,
                        LeagueScheduleConstraintProperties.FOUR_CONSECUTIVE_AWAY_MATCHES,
                        "A team must not play four or more consecutive matchdays away from its own venue.",
                        LeagueScheduleConstraintGroup.TEAM_FAIRNESS));
    }

    protected Constraint repeatMatchOnTheNextDay(ConstraintFactory constraintFactory) {
        // The reverse fixture must not be played on the very next matchday.
        return constraintFactory.forEach(Match.class)
                .ifExists(Match.class, equal(Match::getHomeTeam, Match::getAwayTeam),
                        equal(Match::getAwayTeam, Match::getHomeTeam),
                        equal(match -> match.getRoundIndex() + 1, Match::getRoundIndex))
                .penalize(HardSoftScore.ONE_HARD)
                .justifyWith((match, score) -> RepeatMatchOnTheNextDayJustification.of(match))
                .asConstraint(new ConstraintInfo(LeagueScheduleConstraintProperties.REPEAT_MATCH_ON_THE_NEXT_DAY,
                        LeagueScheduleConstraintProperties.REPEAT_MATCH_ON_THE_NEXT_DAY,
                        "The same two teams must not meet again on the matchday right after they played.",
                        LeagueScheduleConstraintGroup.SCHEDULE_CONFLICTS));
    }

    // ************************************************************************
    // Soft constraints
    // ************************************************************************

    protected Constraint startToAwayHop(ConstraintFactory constraintFactory) {
        // The away team of an opening match travels there from its own venue.
        return constraintFactory.forEach(Match.class)
                .ifNotExists(Round.class,
                        equal(match -> match.getRoundIndex() - 1, Round::getIndex))
                .penalize(HardSoftScore.ONE_SOFT,
                        match -> match.getAwayTeam().getDistance(match.getHomeTeam()))
                .justifyWith((match, score) -> StartToAwayHopJustification.of(match))
                .asConstraint(new ConstraintInfo(LeagueScheduleConstraintProperties.START_TO_AWAY_HOP,
                        LeagueScheduleConstraintProperties.START_TO_AWAY_HOP,
                        "Minimise the distance a team travels from home to the season's opening match.",
                        LeagueScheduleConstraintGroup.TRAVEL_DISTANCE));
    }

    protected Constraint homeToAwayHop(ConstraintFactory constraintFactory) {
        // A team playing at home and away on the next matchday travels from its own venue to the other one.
        return constraintFactory.forEach(Match.class)
                .join(Match.class, equal(Match::getHomeTeam, Match::getAwayTeam),
                        equal(match -> match.getRoundIndex() + 1, Match::getRoundIndex))
                .penalize(HardSoftScore.ONE_SOFT,
                        (match, otherMatch) -> match.getHomeTeam().getDistance(otherMatch.getHomeTeam()))
                .justifyWith((match, otherMatch, score) -> HomeToAwayHopJustification.of(match, otherMatch))
                .asConstraint(new ConstraintInfo(LeagueScheduleConstraintProperties.HOME_TO_AWAY_HOP,
                        LeagueScheduleConstraintProperties.HOME_TO_AWAY_HOP,
                        "Minimise the distance a team travels from a home match to an away match on the next matchday.",
                        LeagueScheduleConstraintGroup.TRAVEL_DISTANCE));
    }

    protected Constraint awayToAwayHop(ConstraintFactory constraintFactory) {
        // A team playing away twice in a row travels straight from one venue to the next.
        return constraintFactory.forEach(Match.class)
                .join(Match.class, equal(Match::getAwayTeam, Match::getAwayTeam),
                        equal(match -> match.getRoundIndex() + 1, Match::getRoundIndex))
                .penalize(HardSoftScore.ONE_SOFT,
                        (match, otherMatch) -> match.getHomeTeam().getDistance(otherMatch.getHomeTeam()))
                .justifyWith((match, otherMatch, score) -> AwayToAwayHopJustification.of(match, otherMatch))
                .asConstraint(new ConstraintInfo(LeagueScheduleConstraintProperties.AWAY_TO_AWAY_HOP,
                        LeagueScheduleConstraintProperties.AWAY_TO_AWAY_HOP,
                        "Minimise the distance a team travels between two away matches on consecutive matchdays.",
                        LeagueScheduleConstraintGroup.TRAVEL_DISTANCE));
    }

    protected Constraint awayToHomeHop(ConstraintFactory constraintFactory) {
        // A team playing away and at home on the next matchday travels back to its own venue.
        return constraintFactory.forEach(Match.class)
                .join(Match.class, equal(Match::getAwayTeam, Match::getHomeTeam),
                        equal(match -> match.getRoundIndex() + 1, Match::getRoundIndex))
                .penalize(HardSoftScore.ONE_SOFT,
                        (match, otherMatch) -> match.getHomeTeam().getDistance(match.getAwayTeam()))
                .justifyWith((match, otherMatch, score) -> AwayToHomeHopJustification.of(match, otherMatch))
                .asConstraint(new ConstraintInfo(LeagueScheduleConstraintProperties.AWAY_TO_HOME_HOP,
                        LeagueScheduleConstraintProperties.AWAY_TO_HOME_HOP,
                        "Minimise the distance a team travels back home from an away match to a home match on the "
                                + "next matchday.",
                        LeagueScheduleConstraintGroup.TRAVEL_DISTANCE));
    }

    protected Constraint awayToEndHop(ConstraintFactory constraintFactory) {
        // The away team of a closing match travels back to its own venue afterwards.
        return constraintFactory.forEach(Match.class)
                .ifNotExists(Round.class, equal(match -> match.getRoundIndex() + 1, Round::getIndex))
                .penalize(HardSoftScore.ONE_SOFT,
                        match -> match.getHomeTeam().getDistance(match.getAwayTeam()))
                .justifyWith((match, score) -> AwayToEndHopJustification.of(match))
                .asConstraint(new ConstraintInfo(LeagueScheduleConstraintProperties.AWAY_TO_END_HOP,
                        LeagueScheduleConstraintProperties.AWAY_TO_END_HOP,
                        "Minimise the distance a team travels home after the season's closing match.",
                        LeagueScheduleConstraintGroup.TRAVEL_DISTANCE));
    }

    protected Constraint classicMatches(ConstraintFactory constraintFactory) {
        // A classic match, such as a derby, draws the biggest crowd on a weekend or holiday.
        return constraintFactory.forEach(Match.class)
                .filter(match -> match.isClassicMatch() && !match.getRound().isWeekendOrHoliday())
                .penalize(HardSoftScore.ofSoft(1000))
                .justifyWith((match, score) -> ClassicMatchesJustification.of(match))
                .asConstraint(new ConstraintInfo(LeagueScheduleConstraintProperties.CLASSIC_MATCHES,
                        LeagueScheduleConstraintProperties.CLASSIC_MATCHES,
                        "A classic match should be played on a weekend or holiday round.",
                        LeagueScheduleConstraintGroup.MATCH_ATTRACTIVENESS));
    }
}
