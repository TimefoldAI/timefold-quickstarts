from timefold.solver import SolverStatus
from timefold.solver.domain import (planning_entity, planning_solution, PlanningId, PlanningVariable,
                                    PlanningEntityCollectionProperty,
                                    ProblemFactCollectionProperty, ValueRangeProvider,
                                    PlanningScore, PlanningPin)
from typing import Dict, List, Any, Annotated

from .json_serialization import *


class Day(JsonDomainBase):
    date_index: int

    def __eq__(self, other):
        if self is other:
            return True
        if not isinstance(other, Day):
            return False
        return self.date_index == other.date_index

    def __hash__(self):
        return 31 * self.date_index


class Team(JsonDomainBase):
    id: Annotated[int, PlanningId]
    name: Annotated[str, Field(default=None)]


    def __eq__(self, other):
        if self is other:
            return True
        if not isinstance(other, Team):
            return False
        return self.id == other.id

    def __hash__(self):
        return 31 * hash(self.id)

    def __str__(self):
        return self.name if self.name is not None else super().__str__()

    def __repr__(self):
        return f'Team({self.id}, {self.name})'


class UnavailabilityPenalty(JsonDomainBase):
    team: Annotated[Team | None,
                   IdSerializer,
                   TeamDeserializer,
                   Field(default=None)]
    day: Annotated[Day | None,
                  IdSerializer,
                  DayDeserializer,
                  Field(default=None)]


@planning_entity
class TeamAssignment(JsonDomainBase):
    id: Annotated[int, PlanningId]
    day: Annotated[Day | None,
                  IdSerializer,
                  DayDeserializer,
                  Field(default=None)]
    index_in_day: int
    pinned: Annotated[bool, PlanningPin, Field(default=False)]
    team: Annotated[Team | None,
                   PlanningVariable,
                   IdSerializer,
                   TeamDeserializer,
                   Field(default=None)]


    def __str__(self):
        return f'Round-{self.day.dateIndex}({self.index_in_day})'

    def __repr__(self):
        return f'TeamAssignment({self.id}, {self.day}, {self.index_in_day}, {self.pinned}, {self.team})'


@planning_solution
class TournamentSchedule(JsonDomainBase):
    teams: Annotated[list[Team],
                    ProblemFactCollectionProperty,
                    ValueRangeProvider]
    days: Annotated[list[Day],
                   ProblemFactCollectionProperty]
    unavailability_penalties: Annotated[list[UnavailabilityPenalty],
                                       ProblemFactCollectionProperty]
    team_assignments: Annotated[list[TeamAssignment],
                               PlanningEntityCollectionProperty]
    score: Annotated[HardMediumSoftDecimalScore | None,
                    PlanningScore,
                    ScoreSerializer,
                    ScoreValidator,
                    Field(default=None)]
    solver_status: Annotated[SolverStatus | None, Field(default=SolverStatus.NOT_SOLVING)]