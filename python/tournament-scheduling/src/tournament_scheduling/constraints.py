from timefold.solver.score import *
from datetime import time
from typing import Final

from .domain import *
from .score_analysis import LoadBalanceJustification


@constraint_provider
def define_constraints(constraint_factory: ConstraintFactory):
    return [
        one_assignment_per_date_per_team(constraint_factory),
        unavailability_penalty(constraint_factory),
        fair_assignment_count_per_team(constraint_factory),
        evenly_confrontation_count(constraint_factory)
    ]


def one_assignment_per_date_per_team(constraint_factory: ConstraintFactory) -> Constraint:
    return (constraint_factory
            .for_each(TeamAssignment)
            .join(TeamAssignment,
                  Joiners.equal(lambda team_assignment: team_assignment.team),
                          Joiners.equal(lambda team_assignment: team_assignment.day),
                          Joiners.less_than(lambda team_assignment: team_assignment.id))
            .penalize(HardMediumSoftDecimalScore.ONE_HARD)
            .as_constraint("oneAssignmentPerDatePerTeam"))


def unavailability_penalty(constraint_factory: ConstraintFactory) -> Constraint:
    return (constraint_factory
            .for_each(UnavailabilityPenalty)
            .if_exists(TeamAssignment,
                       Joiners.equal(lambda unavailability_penalty: unavailability_penalty.team,
                                     lambda team_assignment: team_assignment.team),
                       Joiners.equal(lambda unavailability_penalty: unavailability_penalty.day,
                                     lambda team_assignment: team_assignment.day))
            .penalize(HardMediumSoftDecimalScore.ONE_HARD)
            .as_constraint("unavailabilityPenalty"))


def fair_assignment_count_per_team(constraint_factory: ConstraintFactory) -> Constraint:
    return (constraint_factory
            .for_each(TeamAssignment)
            .group_by(ConstraintCollectors.load_balance(lambda team_assignment: team_assignment.team))
            .penalize_decimal(HardMediumSoftDecimalScore.ONE_MEDIUM, lambda balance: balance.unfairness())
            .justify_with(lambda balance, score: LoadBalanceJustification(balance.unfairness()))
            .as_constraint("fairAssignmentCountPerTeam"))


def evenly_confrontation_count(constraint_factory: ConstraintFactory) -> Constraint:
    return (constraint_factory
            .for_each(TeamAssignment)
            .join(TeamAssignment,
                  Joiners.equal(lambda team_assignment: team_assignment.day),
                          Joiners.less_than(lambda assignment: assignment.team.id))
            .group_by(ConstraintCollectors.load_balance(lambda assignment, other_assignment: (assignment.team, other_assignment.team)))
            .penalize_decimal(HardMediumSoftDecimalScore.ONE_SOFT, lambda balance: balance.unfairness())
            .justify_with(lambda balance, score: LoadBalanceJustification(balance.unfairness()))
            .as_constraint("evenlyConfrontationCount"))