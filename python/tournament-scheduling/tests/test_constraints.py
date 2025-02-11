from timefold.solver.test import ConstraintVerifier
from tournament_scheduling.domain import *
from tournament_scheduling.constraints import *

constraint_verifier = ConstraintVerifier.build(define_constraints, TournamentSchedule, TeamAssignment)

DAY0 = Day(date_index=0)
DAY1 = Day(date_index=1)
DAY2 = Day(date_index=2)
TEAM0 = Team(id=0, name="A")
TEAM1 = Team(id=1, name="B")
TEAM2 = Team(id=2, name="C")

def test_one_assignment_per_day_per_team():
    assignment1 = TeamAssignment(id=0, day=DAY0, index_in_day=0, team=TEAM0)
    assignment2 = TeamAssignment(id=1, day=DAY0, index_in_day=1, team=TEAM0)
    assignment3 = TeamAssignment(id=2, day=DAY0, index_in_day=2, team=TEAM0)
    assignment4 = TeamAssignment(id=3, day=DAY1, index_in_day=0, team=TEAM1)
    assignment5 = TeamAssignment(id=4, day=DAY2, index_in_day=1, team=TEAM1)

    (constraint_verifier.verify_that(one_assignment_per_date_per_team)
     .given(assignment1, assignment2, assignment3, assignment4, assignment5, TEAM0, TEAM1, TEAM2)
     .penalizes_by(3)) # TEAM0 by 2, TEAM1 by 1


def test_unavailability_penalty():
    assignment1 = TeamAssignment(id=0, day=DAY0, index_in_day=0, team=TEAM0)
    assignment2 = TeamAssignment(id=1, day=DAY1, index_in_day=0, team=TEAM1)
    assignment3 = TeamAssignment(id=2, day=DAY1, index_in_day=1, team=TEAM1)
    assignment4 = TeamAssignment(id=3, day=DAY2, index_in_day=0, team=TEAM1)

    unavailability_penalty1 = UnavailabilityPenalty(team=TEAM0, day=DAY0)
    unavailability_penalty2 = UnavailabilityPenalty(team=TEAM1, day=DAY1)

    (constraint_verifier.verify_that(unavailability_penalty)
     .given(assignment1, assignment2, assignment3, assignment4, unavailability_penalty1, unavailability_penalty2)
     .penalizes_by(2)) # TEAM0 by 1, TEAM1 by 1


def test_fair_assignment_count_per_team():
    assignment1 = TeamAssignment(id=0, day=DAY0, index_in_day=0, team=TEAM0)
    assignment2 = TeamAssignment(id=1, day=DAY1, index_in_day=0, team=TEAM1)
    assignment3 = TeamAssignment(id=2, day=DAY2, index_in_day=0, team=TEAM2)
    assignment4 = TeamAssignment(id=3, day=DAY0, index_in_day=0, team=TEAM2)
    (constraint_verifier.verify_that(fair_assignment_count_per_team)
     .given(assignment1, assignment2, assignment3)
     .penalizes_by(0))
    # Team 2 twice while everyone else just once = more unfair.

    (constraint_verifier.verify_that(fair_assignment_count_per_team)
     .given(assignment1, assignment2, assignment3, assignment4)
     .penalizes_more_than(0))


def test_evenly_confrontation_count():
    assignment1 = TeamAssignment(id=0, day=DAY0, index_in_day=0, team=TEAM0)
    assignment2 = TeamAssignment(id=1, day=DAY0, index_in_day=0, team=TEAM1)
    assignment3 = TeamAssignment(id=2, day=DAY0, index_in_day=0, team=TEAM2)
    assignment4 = TeamAssignment(id=3, day=DAY0, index_in_day=0, team=TEAM2)

    (constraint_verifier.verify_that(evenly_confrontation_count)
     .given(assignment1, assignment2, assignment3)
     .penalizes_by(0))
    # Team 2 twice while everyone else just once = more unfair.

    (constraint_verifier.verify_that(evenly_confrontation_count)
     .given(assignment1, assignment2, assignment3, assignment4)
     .penalizes_more_than(0))