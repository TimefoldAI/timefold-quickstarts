from dataclasses import dataclass, field
from typing import List, Optional, Annotated, Union
from timefold.solver.domain import (
    planning_entity, planning_solution, PlanningId, PlanningVariable,
    PlanningEntityCollectionProperty, ProblemFactCollectionProperty, ValueRangeProvider,
    PlanningScore, PlanningPin
)
from timefold.solver import SolverStatus
from timefold.solver.score import HardMediumSoftScore
from .json_serialization import JsonDomainBase
from pydantic import Field

# Time granularity is 15 minutes (which is often recommended when dealing with humans for practical purposes).
GRAIN_LENGTH_IN_MINUTES = 15

@dataclass
class Person:
    id: Annotated[str, PlanningId]
    full_name: str

@dataclass
class TimeGrain:
    id: Annotated[str, PlanningId]
    grain_index: int
    day_of_year: int
    starting_minute_of_day: int

@dataclass
class Room:
    id: Annotated[str, PlanningId]
    name: str
    capacity: int

# Define RequiredAttendance and PreferredAttendance before Meeting to avoid forward references
@dataclass
class RequiredAttendance:
    id: Annotated[str, PlanningId]
    person: Person
    meeting_id: str

@dataclass
class PreferredAttendance:
    id: Annotated[str, PlanningId]
    person: Person
    meeting_id: str

@dataclass
class Meeting:
    id: Annotated[str, PlanningId]
    topic: str
    duration_in_grains: int
    speakers: Optional[List[Person]] = None
    content: Optional[str] = None
    entire_group_meeting: bool = False
    required_attendances: List[RequiredAttendance] = field(default_factory=list)
    preferred_attendances: List[PreferredAttendance] = field(default_factory=list)

    def get_required_capacity(self) -> int:
        return len(self.required_attendances) + len(self.preferred_attendances)

    def add_required_attendant(self, person: Person) -> None:
        if any(r.person.id == person.id for r in self.required_attendances):
            raise ValueError(f"The person {person.id} is already assigned to the meeting {self.id}.")
        self.required_attendances.append(
            RequiredAttendance(id=f"{self.id}-{self.get_required_capacity() + 1}", 
                              meeting_id=self.id, 
                              person=person))

    def add_preferred_attendant(self, person: Person) -> None:
        if any(p.person.id == person.id for p in self.preferred_attendances):
            raise ValueError(f"The person {person.id} is already assigned to the meeting {self.id}.")
        self.preferred_attendances.append(
            PreferredAttendance(id=f"{self.id}-{self.get_required_capacity() + 1}", 
                               meeting_id=self.id, 
                               person=person))

@planning_entity
@dataclass
class MeetingAssignment:
    id: Annotated[str, PlanningId]
    meeting: Meeting
    pinned: Annotated[bool, PlanningPin] = False
    starting_time_grain: Annotated[Optional[TimeGrain], PlanningVariable] = None
    room: Annotated[Optional[Room], PlanningVariable] = None

    def get_grain_index(self) -> Optional[int]:
        if self.starting_time_grain is None:
            return None
        return self.starting_time_grain.grain_index

    def calculate_overlap(self, other: "MeetingAssignment") -> int:
        if self.starting_time_grain is None or other.starting_time_grain is None:
            return 0
        # start is inclusive, end is exclusive
        start = self.starting_time_grain.grain_index
        end = self.get_last_time_grain_index() + 1
        other_start = other.starting_time_grain.grain_index
        other_end = other.get_last_time_grain_index() + 1
        
        if other_end < start or end < other_start:
            return 0
        
        return min(end, other_end) - max(start, other_start)

    def get_last_time_grain_index(self) -> Optional[int]:
        if self.starting_time_grain is None:
            return None
        return self.starting_time_grain.grain_index + self.meeting.duration_in_grains - 1

    def get_room_capacity(self) -> int:
        if self.room is None:
            return 0
        return self.room.capacity

    def get_required_capacity(self) -> int:
        return self.meeting.get_required_capacity()

@planning_solution
@dataclass
class MeetingSchedule:
    people: Annotated[List[Person], ProblemFactCollectionProperty]
    time_grains: Annotated[List[TimeGrain], ProblemFactCollectionProperty, ValueRangeProvider]
    rooms: Annotated[List[Room], ProblemFactCollectionProperty, ValueRangeProvider]
    meetings: Annotated[List[Meeting], ProblemFactCollectionProperty]
    required_attendances: Annotated[List[RequiredAttendance], ProblemFactCollectionProperty] = field(default_factory=list)
    preferred_attendances: Annotated[List[PreferredAttendance], ProblemFactCollectionProperty] = field(default_factory=list)
    meeting_assignments: Annotated[List[MeetingAssignment], PlanningEntityCollectionProperty] = field(default_factory=list)
    score: Annotated[Optional[HardMediumSoftScore], PlanningScore] = None
    solver_status: SolverStatus = SolverStatus.NOT_SOLVING

# Pydantic REST models for API (used for deserialization and context)
class PersonModel(JsonDomainBase):
    id: str
    full_name: str

class TimeGrainModel(JsonDomainBase):
    id: str
    grain_index: int
    day_of_year: int
    starting_minute_of_day: int

class RoomModel(JsonDomainBase):
    id: str
    name: str
    capacity: int

class RequiredAttendanceModel(JsonDomainBase):
    id: str
    person: PersonModel
    meeting_id: str = Field(..., alias="meeting")

class PreferredAttendanceModel(JsonDomainBase):
    id: str
    person: PersonModel
    meeting_id: str = Field(..., alias="meeting")

class MeetingModel(JsonDomainBase):
    id: str
    topic: str
    duration_in_grains: int
    speakers: Optional[List[PersonModel]] = None
    content: Optional[str] = None
    entire_group_meeting: bool = False
    required_attendances: List[RequiredAttendanceModel] = Field(default_factory=list, alias="requiredAttendances")
    preferred_attendances: List[PreferredAttendanceModel] = Field(default_factory=list, alias="preferredAttendances")

class MeetingAssignmentModel(JsonDomainBase):
    id: str
    meeting: Union[str, MeetingModel]
    pinned: bool = False
    starting_time_grain: Union[str, TimeGrainModel, None] = None
    room: Union[str, RoomModel, None] = None

class MeetingScheduleModel(JsonDomainBase):
    people: List[PersonModel]
    time_grains: List[TimeGrainModel]
    rooms: List[RoomModel]
    meetings: List[MeetingModel]
    required_attendances: List[RequiredAttendanceModel] = Field(default_factory=list, alias="requiredAttendances")
    preferred_attendances: List[PreferredAttendanceModel] = Field(default_factory=list, alias="preferredAttendances")
    meeting_assignments: List[MeetingAssignmentModel] = Field(default_factory=list)
    score: Optional[str] = None
    solver_status: Optional[str] = None