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

import org.acme.sportsleagueschedule.domain.Match;
import org.acme.sportsleagueschedule.domain.Round;
import org.acme.sportsleagueschedule.domain.Team;

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

    protected Constraint matchesOnSameDay(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEachUniquePair(Match.class,
                        equal(Match::getRoundIndex),
                        filtering((match1, match2) -> match1.getHomeTeam().equals(match2.getHomeTeam())
                                || match1.getHomeTeam().equals(match2.getAwayTeam())
                                || match1.getAwayTeam().equals(match2.getHomeTeam())
                                || match1.getAwayTeam().equals(match2.getAwayTeam())))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Matches on the same day");
    }

    protected Constraint multipleConsecutiveHomeMatches(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Match.class)
                .join(Team.class, equal(Match::getHomeTeam, Function.identity()))
                .groupBy((match, team) -> team,
                        ConstraintCollectors.toConsecutiveSequences((match, team) -> match.getRound(), Round::getIndex))
                .flattenLast(SequenceChain::getConsecutiveSequences)
                .filter((team, matches) -> matches.getCount() >= MAX_CONSECUTIVE_MATCHES)
                .penalize(HardSoftScore.ONE_HARD, (team, matches) -> matches.getCount())
                .asConstraint("4 or more consecutive home matches");
    }

    protected Constraint multipleConsecutiveAwayMatches(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Match.class)
                .join(Team.class, equal(Match::getAwayTeam, Function.identity()))
                .groupBy((match, team) -> team,
                        ConstraintCollectors.toConsecutiveSequences((match, team) -> match.getRound(), Round::getIndex))
                .flattenLast(SequenceChain::getConsecutiveSequences)
                .filter((team, matches) -> matches.getCount() >= MAX_CONSECUTIVE_MATCHES)
                .penalize(HardSoftScore.ONE_HARD, (team, matches) -> matches.getCount())
                .asConstraint("4 or more consecutive away matches");
    }

    protected Constraint repeatMatchOnTheNextDay(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Match.class)
                .ifExists(Match.class, equal(Match::getHomeTeam, Match::getAwayTeam),
                        equal(Match::getAwayTeam, Match::getHomeTeam),
                        equal(match -> match.getRoundIndex() + 1, Match::getRoundIndex))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Repeat match on the next day");
    }

    protected Constraint startToAwayHop(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Match.class)
                .ifNotExists(Round.class,
                        equal(match -> match.getRoundIndex() - 1, Round::getIndex))
                .penalize(HardSoftScore.ONE_SOFT,
                        match -> match.getAwayTeam().getDistance(match.getHomeTeam()))
                .asConstraint("Start to away hop");
    }

    protected Constraint homeToAwayHop(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Match.class)
                .join(Match.class, equal(Match::getHomeTeam, Match::getAwayTeam),
                        equal(match -> match.getRoundIndex() + 1, Match::getRoundIndex))
                .penalize(HardSoftScore.ONE_SOFT,
                        (match, otherMatch) -> match.getHomeTeam().getDistance(otherMatch.getHomeTeam()))
                .asConstraint("Home to away hop");
    }

    protected Constraint awayToAwayHop(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Match.class)
                .join(Match.class, equal(Match::getAwayTeam, Match::getAwayTeam),
                        equal(match -> match.getRoundIndex() + 1, Match::getRoundIndex))
                .penalize(HardSoftScore.ONE_SOFT,
                        (match, otherMatch) -> match.getHomeTeam().getDistance(otherMatch.getHomeTeam()))
                .asConstraint("Away to away hop");
    }

    protected Constraint awayToHomeHop(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Match.class)
                .join(Match.class, equal(Match::getAwayTeam, Match::getHomeTeam),
                        equal(match -> match.getRoundIndex() + 1, Match::getRoundIndex))
                .penalize(HardSoftScore.ONE_SOFT,
                        (match, otherMatch) -> match.getHomeTeam().getDistance(match.getAwayTeam()))
                .asConstraint("Away to home hop");
    }

    protected Constraint awayToEndHop(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Match.class)
                .ifNotExists(Round.class, equal(match -> match.getRoundIndex() + 1, Round::getIndex))
                .penalize(HardSoftScore.ONE_SOFT,
                        match -> match.getHomeTeam().getDistance(match.getAwayTeam()))
                .asConstraint("Away to end hop");
    }

    protected Constraint classicMatches(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Match.class)
                .filter(match -> match.isClassicMatch() && !match.getRound().isWeekendOrHoliday())
                .penalize(HardSoftScore.ofSoft(1000))
                .asConstraint("Classic matches played on weekends or holidays");
    }

}
