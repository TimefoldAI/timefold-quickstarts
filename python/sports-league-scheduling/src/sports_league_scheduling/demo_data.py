import json
from random import Random
from datetime import datetime, time, timedelta
from typing import List, Callable

from .domain import *

random = Random(0)
DISTANCE_IN_KM = [
    [0, 2163, 2163, 2160, 2156, 2156, 2163, 340, 1342, 512, 3038, 1526, 2054, 2054],
    [2163, 0, 11, 50, 813, 813, 11, 1967, 842, 1661, 1139, 1037, 202, 202],
    [2163, 11, 0, 50, 813, 813, 11, 1967, 842, 1661, 1139, 1037, 202, 202],
    [2160, 50, 50, 0, 862, 862, 50, 1957, 831, 1655, 1180, 1068, 161, 161],
    [2160, 813, 813, 862, 0, 1, 813, 2083, 1160, 1741, 910, 644, 600, 600],
    [2160, 813, 813, 862, 1, 0, 813, 2083, 1160, 1741, 910, 644, 600, 600],
    [2163, 11, 11, 50, 813, 813, 0, 1967, 842, 1661, 1139, 1037, 202, 202],
    [340, 1967, 1967, 1957, 2083, 2083, 1967, 0, 1126, 341, 2926, 1490, 1836, 1836],
    [1342, 842, 842, 831, 1160, 1160, 842, 1126, 0, 831, 1874, 820, 714, 714],
    [512, 1661, 1661, 1655, 1741, 1741, 1661, 341, 831, 0, 2589, 1151, 1545, 1545],
    [3038, 1139, 1139, 1180, 910, 910, 1139, 2926, 1874, 2589, 0, 1552, 1340, 1340],
    [1526, 1037, 1037, 1068, 644, 644, 1037, 1490, 820, 1151, 1552, 0, 1077, 1077],
    [2054, 202, 202, 161, 600, 600, 202, 1836, 714, 1545, 1340, 1077, 0, 14],
    [2054, 202, 202, 161, 600, 600, 202, 1836, 714, 1545, 1340, 1077, 14, 0],
]


def id_generator():
    current = 0
    while True:
        yield str(current)
        current += 1


def generate_rounds(count_rounds : int) -> List[Round]:
    today = datetime.now()
    rounds = [Round(index=i, weekend_or_holiday=False) for i in range(count_rounds)]

    # Mark weekend rounds as important
    for round_obj in rounds:
        future_date = today + timedelta(days=round_obj.index)
        if future_date.weekday() in (5, 6):  # Saturday or Sunday
            round_obj.weekend_or_holiday = True

    return rounds


def generate_teams() -> List[Team]:
    team_names = [
        "Cruzeiro", "Argentinos Jr.", "Boca Juniors", "Estudiantes", "Independente",
        "Racing", "River Plate", "Flamengo", "Gremio", "Santos",
        "Colo-Colo", "Olimpia", "Nacional", "Penharol"
    ]

    teams = [Team(id=str(i + 1), name=name, distance_to_team={}) for i, name in enumerate(team_names)]

    # Assign distances
    for i, team in enumerate(teams):
        team.distance_to_team = {
            teams[j].id: DISTANCE_IN_KM[i][j]
            for j in range(len(teams))
            if i != j
        }

    return teams

def generate_matches(teams: List[Team]) -> List[Match]:
    reciprocal_match = None
    matches = [
        Match(id=f'{team1.id}-{team2.id}', home_team=team1, away_team=team2, classic_match=False, round=None)
        for team1 in teams
        for team2 in teams
        if team1 != team2
    ]

    # 5% classic matches
    apply_random_value(
        count=int(len(matches) * 0.05),
        values = matches,
        filter_func = lambda match_league: not match_league.classic_match,
        consumer_func = lambda match_league: setattr(match_league, 'classic_match', True)
    )

    # Ensure reciprocity for classic matches
    for match in matches:
        if match.classic_match:
            reciprocal_match = next((m for m in matches if m.home_team == match.away_team and m.away_team == match.home_team), None)
        if reciprocal_match:
            reciprocal_match.classic_match = True

    return matches


def apply_random_value(count: int, values: List, filter_func: Callable, consumer_func: Callable) -> None:
    filtered_values = [value for value in values if filter_func(value)]
    size = len(filtered_values)

    for _ in range(count):
        if size > 0:
            selected_value = random.choice(filtered_values)
            consumer_func(selected_value)
            filtered_values.remove(selected_value)
            size -= 1
        else:
            break


def generate_demo_data() -> LeagueSchedule:
    count_rounds = 32
    # Rounds
    rounds = generate_rounds(count_rounds)
    # Teams
    teams = generate_teams()
    # Matches
    matches = generate_matches(teams)

    # Create Schedule
    schedule = LeagueSchedule(
        id="demo-schedule",
        rounds=rounds,
        teams=teams,
        matches=matches,
        score=None,
        solver_status=None
    )

    return schedule