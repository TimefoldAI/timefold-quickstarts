package org.acme.sportsleagueschedule.solver;

import static ai.timefold.solver.core.api.score.stream.Joiners.equal;
import static ai.timefold.solver.core.api.score.stream.Joiners.filtering;

import java.util.function.Function;

import ai.timefold.solver.core.api.score.HardMediumSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintCollectors;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.core.api.score.stream.common.SequenceChain;
import ai.timefold.solver.service.definition.api.description.ConstraintInfo;

import org.acme.sportsleagueschedule.domain.Match;
import org.acme.sportsleagueschedule.domain.Round;
import org.acme.sportsleagueschedule.domain.Team;

public class SportsLeagueSchedulingConstraintProvider implements ConstraintProvider {

    private static final int MAX_CONSECUTIVE_MATCHES = 4;

    public static final String MATCHES_ON_SAME_DAY = "Matches on the same day";
    public static final String CONSECUTIVE_HOME_MATCHES = "4 or more consecutive home matches";
    public static final String CONSECUTIVE_AWAY_MATCHES = "4 or more consecutive away matches";
    public static final String REPEAT_MATCH_ON_THE_NEXT_DAY = "Repeat match on the next day";
    public static final String START_TO_AWAY_HOP = "Start to away hop";
    public static final String HOME_TO_AWAY_HOP = "Home to away hop";
    public static final String AWAY_TO_AWAY_HOP = "Away to away hop";
    public static final String AWAY_TO_HOME_HOP = "Away to home hop";
    public static final String AWAY_TO_END_HOP = "Away to end hop";
    public static final String CLASSIC_MATCHES = "Classic matches played on weekends or holidays";

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

    protected Constraint matchesOnSameDay(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEachUniquePair(Match.class,
                        equal(Match::getRoundIndex),
                        filtering((match1, match2) -> match1.getHomeTeam().equals(match2.getHomeTeam())
                                || match1.getHomeTeam().equals(match2.getAwayTeam())
                                || match1.getAwayTeam().equals(match2.getHomeTeam())
                                || match1.getAwayTeam().equals(match2.getAwayTeam())))
                .penalize(HardMediumSoftScore.ONE_HARD)
                .asConstraint(new ConstraintInfo(MATCHES_ON_SAME_DAY, MATCHES_ON_SAME_DAY,
                        "A team must not play two matches on the same day.",
                        LeagueScheduleConstraintGroup.SCHEDULE_FEASIBILITY));
    }

    protected Constraint multipleConsecutiveHomeMatches(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Match.class)
                .join(Team.class, equal(Match::getHomeTeam, Function.identity()))
                .groupBy((match, team) -> team,
                        ConstraintCollectors.toConsecutiveSequences((match, team) -> match.getRound(), Round::getIndex))
                .flattenLast(SequenceChain::getConsecutiveSequences)
                .filter((team, matches) -> matches.getCount() >= MAX_CONSECUTIVE_MATCHES)
                .penalize(HardMediumSoftScore.ONE_HARD, (team, matches) -> matches.getCount())
                .asConstraint(new ConstraintInfo(CONSECUTIVE_HOME_MATCHES, CONSECUTIVE_HOME_MATCHES,
                        "A team must not play four or more consecutive home matches.",
                        LeagueScheduleConstraintGroup.SCHEDULE_FEASIBILITY));
    }

    protected Constraint multipleConsecutiveAwayMatches(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Match.class)
                .join(Team.class, equal(Match::getAwayTeam, Function.identity()))
                .groupBy((match, team) -> team,
                        ConstraintCollectors.toConsecutiveSequences((match, team) -> match.getRound(), Round::getIndex))
                .flattenLast(SequenceChain::getConsecutiveSequences)
                .filter((team, matches) -> matches.getCount() >= MAX_CONSECUTIVE_MATCHES)
                .penalize(HardMediumSoftScore.ONE_HARD, (team, matches) -> matches.getCount())
                .asConstraint(new ConstraintInfo(CONSECUTIVE_AWAY_MATCHES, CONSECUTIVE_AWAY_MATCHES,
                        "A team must not play four or more consecutive away matches.",
                        LeagueScheduleConstraintGroup.SCHEDULE_FEASIBILITY));
    }

    protected Constraint repeatMatchOnTheNextDay(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Match.class)
                .ifExists(Match.class, equal(Match::getHomeTeam, Match::getAwayTeam),
                        equal(Match::getAwayTeam, Match::getHomeTeam),
                        equal(match -> match.getRoundIndex() + 1, Match::getRoundIndex))
                .penalize(HardMediumSoftScore.ONE_HARD)
                .asConstraint(new ConstraintInfo(REPEAT_MATCH_ON_THE_NEXT_DAY, REPEAT_MATCH_ON_THE_NEXT_DAY,
                        "The reverse fixture must not be played on the day after a match.",
                        LeagueScheduleConstraintGroup.SCHEDULE_FEASIBILITY));
    }

    protected Constraint startToAwayHop(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Match.class)
                .ifNotExists(Round.class,
                        equal(match -> match.getRoundIndex() - 1, Round::getIndex))
                .penalize(HardMediumSoftScore.ONE_SOFT,
                        match -> match.getAwayTeam().getDistance(match.getHomeTeam()))
                .asConstraint(new ConstraintInfo(START_TO_AWAY_HOP, START_TO_AWAY_HOP,
                        "Minimize travel from a team's base to its first away match.",
                        LeagueScheduleConstraintGroup.TRAVEL_DISTANCE));
    }

    protected Constraint homeToAwayHop(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Match.class)
                .join(Match.class, equal(Match::getHomeTeam, Match::getAwayTeam),
                        equal(match -> match.getRoundIndex() + 1, Match::getRoundIndex))
                .penalize(HardMediumSoftScore.ONE_SOFT,
                        (match, otherMatch) -> match.getHomeTeam().getDistance(otherMatch.getHomeTeam()))
                .asConstraint(new ConstraintInfo(HOME_TO_AWAY_HOP, HOME_TO_AWAY_HOP,
                        "Minimize travel from a home match to the next away match.",
                        LeagueScheduleConstraintGroup.TRAVEL_DISTANCE));
    }

    protected Constraint awayToAwayHop(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Match.class)
                .join(Match.class, equal(Match::getAwayTeam, Match::getAwayTeam),
                        equal(match -> match.getRoundIndex() + 1, Match::getRoundIndex))
                .penalize(HardMediumSoftScore.ONE_SOFT,
                        (match, otherMatch) -> match.getHomeTeam().getDistance(otherMatch.getHomeTeam()))
                .asConstraint(new ConstraintInfo(AWAY_TO_AWAY_HOP, AWAY_TO_AWAY_HOP,
                        "Minimize travel between two consecutive away matches.",
                        LeagueScheduleConstraintGroup.TRAVEL_DISTANCE));
    }

    protected Constraint awayToHomeHop(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Match.class)
                .join(Match.class, equal(Match::getAwayTeam, Match::getHomeTeam),
                        equal(match -> match.getRoundIndex() + 1, Match::getRoundIndex))
                .penalize(HardMediumSoftScore.ONE_SOFT,
                        (match, otherMatch) -> match.getHomeTeam().getDistance(match.getAwayTeam()))
                .asConstraint(new ConstraintInfo(AWAY_TO_HOME_HOP, AWAY_TO_HOME_HOP,
                        "Minimize travel from an away match back to a home match.",
                        LeagueScheduleConstraintGroup.TRAVEL_DISTANCE));
    }

    protected Constraint awayToEndHop(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Match.class)
                .ifNotExists(Round.class, equal(match -> match.getRoundIndex() + 1, Round::getIndex))
                .penalize(HardMediumSoftScore.ONE_SOFT,
                        match -> match.getHomeTeam().getDistance(match.getAwayTeam()))
                .asConstraint(new ConstraintInfo(AWAY_TO_END_HOP, AWAY_TO_END_HOP,
                        "Minimize travel from a team's last away match back to its base.",
                        LeagueScheduleConstraintGroup.TRAVEL_DISTANCE));
    }

    protected Constraint classicMatches(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Match.class)
                .filter(match -> match.isClassicMatch() && !match.getRound().isWeekendOrHoliday())
                .penalize(HardMediumSoftScore.ofSoft(1000))
                .asConstraint(new ConstraintInfo(CLASSIC_MATCHES, CLASSIC_MATCHES,
                        "Classic matches should be played on a weekend or holiday.",
                        LeagueScheduleConstraintGroup.MATCH_IMPORTANCE));
    }
}
