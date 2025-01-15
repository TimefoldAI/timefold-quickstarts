from timefold.solver.score import *
from datetime import time
from typing import Final

from .domain import *


MAX_CONSECUTIVE_MATCHES: Final[int] = 4


@constraint_provider
def define_constraints(constraint_factory: ConstraintFactory):
    return [
        matches_on_same_day(constraint_factory),
        multiple_consecutive_home_matches(constraint_factory),
        multiple_consecutive_away_matches(constraint_factory),
        repeat_match_on_the_next_day(constraint_factory),
        start_to_away_hop(constraint_factory),
        home_to_away_hop(constraint_factory),
        away_to_away_hop(constraint_factory),
        away_to_home_hop(constraint_factory),
        away_to_end_hop(constraint_factory),
        classic_matches(constraint_factory)
    ]


def matches_on_same_day(constraint_factory: ConstraintFactory) -> Constraint:
    return (constraint_factory
            .for_each_unique_pair(Match,
                                  Joiners.equal(lambda match: match.round.index),
                                  Joiners.filtering(are_teams_overlapping))
            .penalize(HardSoftScore.ONE_HARD)
            .as_constraint("Matches on the same day"))

def are_teams_overlapping(match1 : Match, match2:Match) -> bool:
    return (match1.home_team == match2.home_team or match1.home_team == match2.away_team
            or match1.away_team == match2.home_team or match1.away_team == match2.away_team)


def multiple_consecutive_home_matches(constraint_factory: ConstraintFactory) -> Constraint:
    return (constraint_factory
            .for_each(Match)
            .join(Team,
                  Joiners.equal(lambda match: match.home_team, lambda team: team))
            .group_by(lambda match, team: team,
                      ConstraintCollectors.to_consecutive_sequences(lambda match, team: match.round,
                                                                    lambda match_round: match_round.index))
            .flatten_last(lambda sequences: sequences.getConsecutiveSequences())
            .filter(lambda team, matches: matches.getCount() >= MAX_CONSECUTIVE_MATCHES)
            .penalize(HardSoftScore.ONE_HARD, lambda team, matches: matches.getCount())
            .as_constraint("4 or more consecutive home matches"))


def multiple_consecutive_away_matches(constraint_factory: ConstraintFactory) -> Constraint:
    return (constraint_factory
            .for_each(Match)
            .join(Team,
                  Joiners.equal(lambda match: match.away_team, lambda team: team))
            .group_by(lambda match, team: team,
                      ConstraintCollectors.to_consecutive_sequences(lambda match, team: match.round,
                                                                    lambda match_round: match_round.index))
            .flatten_last(lambda sequences: sequences.getConsecutiveSequences())
            .filter(lambda team, matches: matches.getCount() >= MAX_CONSECUTIVE_MATCHES)
            .penalize(HardSoftScore.ONE_HARD, lambda team, matches: matches.getCount())
            .as_constraint("4 or more consecutive away matches"))


def repeat_match_on_the_next_day(constraint_factory: ConstraintFactory) -> Constraint:
    return (constraint_factory
            .for_each(Match)
            .if_exists(Match,
                       Joiners.equal(lambda match: match.home_team, lambda match: match.away_team),
                       Joiners.equal(lambda match: match.away_team, lambda match: match.home_team),
                       Joiners.equal(lambda match: match.round.index + 1, lambda match: match.round.index))
            .penalize(HardSoftScore.ONE_HARD)
            .as_constraint("Repeat match on the next day"))


def start_to_away_hop(constraint_factory: ConstraintFactory) -> Constraint:
    return (constraint_factory
            .for_each(Match)
            .if_not_exists(Round, Joiners.equal(lambda match: match.round.index - 1,
                                                lambda match_round: match_round.index))
            .penalize(HardSoftScore.ONE_SOFT, away_home_match_distance_lambda)
            .as_constraint("Start to away hop"))


def home_to_away_hop(constraint_factory: ConstraintFactory) -> Constraint:
    return (constraint_factory
            .for_each(Match)
            .join(Match,
                  Joiners.equal(lambda match: match.home_team, lambda match: match.away_team),
                  Joiners.equal(lambda match: match.round.index + 1, lambda match: match.round.index))
            .penalize(HardSoftScore.ONE_SOFT, home_matches_distance_lambda)
            .as_constraint("Home to away hop"))


def away_to_away_hop(constraint_factory: ConstraintFactory) -> Constraint:
    return (constraint_factory
            .for_each(Match)
            .join(Match,
                  Joiners.equal(lambda match: match.away_team, lambda match: match.away_team),
                  Joiners.equal(lambda match: match.round.index + 1, lambda match: match.round.index))
            .penalize(HardSoftScore.ONE_SOFT, home_matches_distance_lambda)
            .as_constraint("Away to away hop"))


def away_to_home_hop(constraint_factory: ConstraintFactory) -> Constraint:
    return (constraint_factory
            .for_each(Match)
            .join(Match,
                  Joiners.equal(lambda match: match.away_team, lambda match: match.home_team),
                  Joiners.equal(lambda match: match.round.index + 1, lambda match: match.round.index))
            .penalize(HardSoftScore.ONE_SOFT, home_away_matches_distance_lambda)
            .as_constraint("Away to home hop"))

home_away_match_distance_lambda = lambda match: (
    match.home_team.distance_to_team.get(match.away_team.id, 0)
    if isinstance(match.home_team, Team) and isinstance(match.away_team, Team) else 0
)


away_home_match_distance_lambda = lambda match: (
    match.away_team.distance_to_team.get(match.home_team.id, 0)
    if isinstance(match.away_team, Team) and isinstance(match.home_team, Team) else 0
)

home_away_matches_distance_lambda = lambda match, other_match: (
    match.home_team.distance_to_team.get(match.away_team.id, 0)
    if isinstance(match.home_team, Team) and isinstance(match.away_team, Team) else 0
)

home_matches_distance_lambda = lambda match, other_match: (
    match.home_team.distance_to_team.get(other_match.home_team.id, 0)
    if isinstance(match.home_team, Team) and isinstance(other_match.home_team, Team) else 0
)

def away_to_end_hop(constraint_factory: ConstraintFactory) -> Constraint:
    return (constraint_factory
            .for_each(Match)
            .if_not_exists(Round, Joiners.equal(lambda match: match.round.index + 1,
                                                lambda match_round: match_round.index))
            .penalize(HardSoftScore.ONE_SOFT, home_away_match_distance_lambda)
            .as_constraint("Away to end hop"))


def classic_matches(constraint_factory: ConstraintFactory) -> Constraint:
    return (constraint_factory
            .for_each(Match)
            .filter(is_invalid_classic_match)
            .penalize(HardSoftScore.of_soft(1000))
            .as_constraint("Classic matches played on weekends or holidays"))

def is_invalid_classic_match(match: Match) -> bool:
    return match.classic_match and not match.round.weekend_or_holiday
