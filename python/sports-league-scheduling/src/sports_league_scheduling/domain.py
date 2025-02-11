from timefold.solver import SolverStatus
from timefold.solver.domain import (planning_entity, planning_solution, PlanningId, PlanningVariable,
                                    PlanningEntityCollectionProperty,
                                    ProblemFactCollectionProperty, ValueRangeProvider,
                                    PlanningScore)
from timefold.solver.score import HardSoftScore
from typing import Dict, List, Any, Annotated

from .json_serialization import *


class Team(JsonDomainBase):
    id: str
    name: str
    distance_to_team: Annotated[Dict[str, int],
                                DistanceToTeamValidator,
                                Field(default_factory=dict)]


    def get_distance(self, other_team: "Team") -> int:
        """
        Get the distance to another team.
        """
        if not isinstance(other_team, Team):
            raise TypeError(f"Expected a Team, got {type(other_team)}")
        return self.distance_to_team.get(other_team.id, 0)

    def __eq__(self, other):
        if self is other:
            return True
        if not isinstance(other, Team):
            return False
        return self.id == other.id

    def __hash__(self):
        return hash(self.id)

    def __str__(self):
        return self.id

    def __repr__(self):
        return f'Team({self.id}, {self.name}, {self.distance_to_team})'


class Round(JsonDomainBase):
    index: Annotated[int, PlanningId]
    # Rounds scheduled on weekends and holidays. It's common for classic matches to be scheduled on weekends or holidays.
    weekend_or_holiday: Annotated[bool, Field(default=False)]


    def __eq__(self, other):
        if self is other:
            return True
        if not isinstance(other, Round):
            return False
        return self.index == other.index

    def __hash__(self):
        return 31 * self.index

    def __str__(self):
        return f'Round-{self.index}'

    def __repr__(self):
        return f'Round({self.index}, {self.weekendOrHoliday})'


@planning_entity
class Match(JsonDomainBase):
    id: Annotated[str, PlanningId]
    home_team: Annotated[Team,
                        IdStrSerializer,
                        TeamDeserializer]
    away_team: Annotated[Team,
                        IdStrSerializer,
                        TeamDeserializer]
    # A classic/important match can impact aspects like revenue (e.g., derby)
    classic_match: Annotated[bool, Field(default=False)]
    round: Annotated[Round | None,
                    PlanningVariable,
                    IdIntSerializer,
                    RoundDeserializer,
                    Field(default=None)]


    def __eq__(self, other):
        if self is other:
            return True
        if not isinstance(other, Match):
            return False
        return self.id == other.id

    def __hash__(self):
        return hash(self.id)

    def __str__(self):
        return f'{self.home_team} + {self.away_team}'

    def __repr__(self):
        return f'Match({self.id}, {self.home_team}, {self.away_team}, {self.classic_match})'


@planning_solution
class LeagueSchedule(JsonDomainBase):
    id: str
    rounds: Annotated[list[Round],
                    ProblemFactCollectionProperty,
                    ValueRangeProvider]
    teams: Annotated[list[Team],
                    ProblemFactCollectionProperty,
                    ValueRangeProvider]
    matches: Annotated[list[Match],
                      PlanningEntityCollectionProperty]
    score: Annotated[HardSoftScore | None,
                    PlanningScore,
                    ScoreSerializer,
                    ScoreValidator,
                    Field(default=None)]
    solver_status: Annotated[SolverStatus | None, Field(default=SolverStatus.NOT_SOLVING)]