import json
from random import Random
from datetime import datetime, time, timedelta
from typing import List, Callable

from .domain import *

random = Random(0)


def id_generator():
    current = 0
    while True:
        yield str(current)
        current += 1


def generate_teams() -> List[Team]:
    team_names = [
        "Maarten", "Geoffrey", "Lukas", "Chris",
        "Fred", "Radek", "Maciej"
    ]
    teams = [Team(id=i, name=name) for i, name in enumerate(team_names)]

    return teams


def generate_unavailability_penalties(count: int, teams: List[Team], days: List[Day]) -> List[UnavailabilityPenalty]:
    penalties = []
    while len(penalties) < count:
        team = random.choice(teams)
        day = random.choice(days)
        if all(p.team != team or p.day != day for p in penalties):
            penalties.append(UnavailabilityPenalty(team=team, day=day))
    return penalties


def generate_team_assignments(count_per_day: int, days: List[Day]) -> List[TeamAssignment]:
    assignments = []
    count = 0
    for day in days:
        for i in range(count_per_day):
            assignments.append(TeamAssignment(id=count, day=day, index_in_day=i, pinned=False, team=None))
            count += 1
    return assignments


def generate_demo_data() -> TournamentSchedule:
    # Days
    count_days = 18
    days= [Day(date_index=i) for i in range(count_days)]
    # Teams
    teams = generate_teams()
    # Unavailability penalties
    count_unavailability_penalties = 12
    unavailability_penalties = generate_unavailability_penalties(count_unavailability_penalties, teams, days)
    # Team assignments
    count_assignments_per_day = 4
    team_assignments = generate_team_assignments(count_assignments_per_day, days)
    # Create Schedule
    schedule = TournamentSchedule(
        teams=teams,
        days=days,
        unavailability_penalties=unavailability_penalties,
        team_assignments=team_assignments,
        score=None,
        solver_status=SolverStatus.NOT_SOLVING
    )

    return schedule