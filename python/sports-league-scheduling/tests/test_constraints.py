from timefold.solver.test import ConstraintVerifier

from sports_league_scheduling.domain import *
from sports_league_scheduling.constraints import *

constraint_verifier = ConstraintVerifier.build(define_constraints, LeagueSchedule, Match)


def test_matches_same_day():
    # Two matches for the home team
    home_team = Team(id="1", name="TeamA")
    rival_team = Team(id="2", name="TeamB")
    match1 = Match(id="1", home_team=home_team, away_team=rival_team, round=Round(index=0))
    match2 = Match(id="2", home_team=home_team, away_team=rival_team, round=Round(index=0))
    match3 = Match(id="3", home_team=home_team, away_team=rival_team)

    constraint_verifier.verify_that(matches_on_same_day).given(match1, match2, match3).penalizes_by(1)

    # Two matches, one for home and another for away match
    other_team = Team(id="3", name="TeamC")
    match1 = Match(id="1", home_team=home_team, away_team=rival_team, round=Round(index=0))
    match2 = Match(id="2", home_team=rival_team, away_team=other_team, round=Round(index=0))

    constraint_verifier.verify_that(matches_on_same_day).given(match1, match2, match3).penalizes_by(1)


def test_multiple_consecutive_home_matches():
    home_team = Team(id="1", name="TeamA")
    rival_team = Team(id="2", name="TeamB")
    matches = [
        Match(id="1", home_team=home_team, away_team=rival_team, round=Round(index=0)),
        Match(id="2", home_team=home_team, away_team=rival_team, round=Round(index=1)),
        Match(id="3", home_team=home_team, away_team=rival_team, round=Round(index=2)),
        Match(id="4", home_team=home_team, away_team=rival_team, round=Round(index=3)),
        Match(id="5", home_team=Team(id="3", name="TeamC"), away_team=home_team)
    ]
    # four consecutive home matches for homeTeam
    constraint_verifier.verify_that(multiple_consecutive_home_matches).given(*matches, home_team, rival_team).penalizes_by(4)


def test_multiple_consecutive_away_matches():
    home_team = Team(id="1", name="TeamA")
    rival_team = Team(id="2", name="TeamB")
    matches = [
        Match(id="1", home_team=home_team, away_team=rival_team, round=Round(index=0)),
        Match(id="2", home_team=home_team, away_team=rival_team, round=Round(index=1)),
        Match(id="3", home_team=home_team, away_team=rival_team, round=Round(index=2)),
        Match(id="4", home_team=home_team, away_team=rival_team, round=Round(index=3)),
        Match(id="5", home_team=Team(id="3", name="TeamC"), away_team=home_team)
    ]
    # four consecutive away matches for homeTeam
    constraint_verifier.verify_that(multiple_consecutive_away_matches).given(*matches, home_team, rival_team).penalizes_by(4)


def test_repeat_match_on_next_day():
    home_team = Team(id="1", name="TeamA")
    rival_team = Team(id="2", name="TeamB")
    matches = [
        Match(id="1", home_team=home_team, away_team=rival_team, round=Round(index=0)),
        Match(id="2", home_team=rival_team, away_team=home_team, round=Round(index=1)),
        Match(id="3", home_team=home_team, away_team=rival_team, round=Round(index=4)),
        Match(id="4", home_team=rival_team, away_team=home_team, round=Round(index=6))
    ]
    # one match repeating on the next day
    constraint_verifier.verify_that(repeat_match_on_the_next_day).given(*matches).penalizes_by(1)


def test_start_to_away_hop():
    home_team = Team(id="1", name="TeamA", distance_to_team={"2": 5})
    second_team = Team(id="2", name="TeamB", distance_to_team={"1": 5})
    third_team = Team(id="3", name="TeamC")
    matches = [
        Match(id="1", home_team=home_team, away_team=second_team, round=Round(index=0)),
        Match(id="2", home_team=home_team, away_team=third_team, round=Round(index=1))
    ]
    # match with the second team
    constraint_verifier.verify_that(start_to_away_hop).given(*matches).penalizes_by(5)


def test_home_to_away_hop():
    home_team = Team(id="1", name="TeamA", distance_to_team={"3": 7})
    second_team = Team(id="2", name="TeamB")
    third_team = Team(id="3", name="TeamC", distance_to_team={"1": 7})
    matches = [
        Match(id="1", home_team=home_team, away_team=second_team, round=Round(index=0)),
        Match(id="2", home_team=third_team, away_team=home_team, round=Round(index=1))
    ]
    # match with the home team
    constraint_verifier.verify_that(home_to_away_hop).given(*matches).penalizes_by(7)


def test_away_to_away_hop():
    second_team = Team(id="2", name="TeamB", distance_to_team={"3": 2})
    third_team = Team(id="3", name="TeamC", distance_to_team={"2": 2})
    matches = [
        Match(id="1", home_team=second_team, away_team=Team(id="1", name="TeamA"), round=Round(index=0)),
        Match(id="2", home_team=third_team, away_team=Team(id="1", name="TeamA"), round=Round(index=1))
    ]
    # match with the home team
    constraint_verifier.verify_that(away_to_away_hop).given(*matches).penalizes_by(2)


def test_away_to_home_hop():
    home_team = Team(id="1",name="TeamA", distance_to_team={"2": 20})
    second_team = Team(id="2", name="TeamB", distance_to_team={"1": 20})
    matches = [
        Match(id="1", home_team=second_team, away_team=home_team, round=Round(index=0)),
        Match(id="2", home_team=home_team, away_team=Team(id="3", name="TeamC"), round=Round(index=1))
    ]
    # match with the home team
    constraint_verifier.verify_that(away_to_home_hop).given(*matches).penalizes_by(20)


def test_away_to_end_hop():
    home_team = Team(id="1", name="TeamA", distance_to_team={"3": 15})
    third_team = Team(id="3", name="TeamC", distance_to_team={"1": 15})
    matches = [
        Match(id="1", home_team=home_team, away_team=Team(id="2", name="TeamB"), round=Round(index=0)),
        Match(id="2", home_team=third_team, away_team=home_team, round=Round(index=1))
    ]
    # match with the home team
    constraint_verifier.verify_that(away_to_end_hop).given(*matches).penalizes_by(15)


def test_classic_matches():
    home_team = Team(id="1", name="TeamA")
    rival_team = Team(id="2", name="TeamB")
    matches = [
        Match(id="1", home_team=home_team, away_team=rival_team, round=Round(index=0),classic_match=True),
        Match(id="2", home_team=rival_team, away_team=home_team, round=Round(index=1)),
        Match(id="3", home_team=home_team, away_team=rival_team, round=Round(index=4), classic_match=True)
    ]
    # two classic matches
    constraint_verifier.verify_that(classic_matches).given(*matches).penalizes_by(2)