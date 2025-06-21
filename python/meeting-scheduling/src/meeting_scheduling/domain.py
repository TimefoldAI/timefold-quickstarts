from typing import List, Optional, Annotated, Any
from timefold.solver.domain import (
    planning_entity, planning_solution, PlanningId, PlanningVariable,
    PlanningEntityCollectionProperty, ProblemFactCollectionProperty, ValueRangeProvider,
    PlanningScore, PlanningPin
)
from timefold.solver import SolverStatus
from timefold.solver.score import HardMediumSoftScore
from pydantic import BaseModel, Field, ConfigDict, PlainSerializer, BeforeValidator, ValidationInfo
from pydantic.alias_generators import to_camel


# Time granularity is 15 minutes (which is often recommended when dealing with humans for practical purposes).
GRAIN_LENGTH_IN_MINUTES = 15

# Serializers and validators for Pydantic
def make_id_item_validator(key: str):
    def validator(v: Any, info: ValidationInfo) -> Any:
        if v is None:
            return None
        if not isinstance(v, str) or not info.context:
            return v
        return info.context.get(key, {}).get(v, v)
    return BeforeValidator(validator)

IdSerializer = PlainSerializer(lambda item: item.id if item is not None else None, return_type=str | None)
ScoreSerializer = PlainSerializer(lambda score: str(score) if score is not None else None, return_type=str | None)

def validate_score(v: Any, info: ValidationInfo) -> Any:
    if isinstance(v, HardMediumSoftScore) or v is None:
        return v
    if isinstance(v, str):
        return HardMediumSoftScore.parse(v)
    raise ValueError('"score" should be a string')

ScoreValidator = BeforeValidator(validate_score)

# Validators for foreign key references
MeetingValidator = make_id_item_validator('meetings')
RoomValidator = make_id_item_validator('rooms')
TimeGrainValidator = make_id_item_validator('timeGrains')

class JsonDomainBase(BaseModel):
    model_config = ConfigDict(
        alias_generator=to_camel,
        populate_by_name=True,
        from_attributes=True,
    )

class Person(JsonDomainBase):
    id: Annotated[str, PlanningId]
    full_name: str
    
    def __hash__(self) -> int:
        return hash(self.id)
    
    def __eq__(self, other) -> bool:
        if not isinstance(other, Person):
            return False
        return self.id == other.id

class TimeGrain(JsonDomainBase):
    id: Annotated[str, PlanningId]
    grain_index: int
    day_of_year: int
    starting_minute_of_day: int
    
    def __hash__(self) -> int:
        return hash(self.id)
    
    def __eq__(self, other) -> bool:
        if not isinstance(other, TimeGrain):
            return False
        return self.id == other.id

class Room(JsonDomainBase):
    id: Annotated[str, PlanningId]
    name: str
    capacity: int
    
    def __hash__(self) -> int:
        return hash(self.id)
    
    def __eq__(self, other) -> bool:
        if not isinstance(other, Room):
            return False
        return self.id == other.id


class Attendance(JsonDomainBase):
    """Abstract base class for attendance"""
    id: Annotated[str, PlanningId]
    person: Person
    meeting_id: str = Field(..., alias="meeting")

class RequiredAttendance(Attendance):
    pass

class PreferredAttendance(Attendance):
    pass


class Meeting(JsonDomainBase):
    id: Annotated[str, PlanningId]
    topic: str
    duration_in_grains: int
    speakers: List[Person] = Field(default_factory=list)
    content: str = ""
    entire_group_meeting: bool = False
    required_attendances: List[RequiredAttendance] = Field(default_factory=list)
    preferred_attendances: List[PreferredAttendance] = Field(default_factory=list)

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
class MeetingAssignment(JsonDomainBase):
    id: Annotated[str, PlanningId]
    meeting: Annotated[Meeting, MeetingValidator, IdSerializer]
    pinned: Annotated[bool, PlanningPin] = False
    starting_time_grain: Annotated[Optional[TimeGrain], PlanningVariable, TimeGrainValidator, IdSerializer] = None
    room: Annotated[Optional[Room], PlanningVariable, RoomValidator, IdSerializer] = None

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
class MeetingSchedule(JsonDomainBase):
    people: Annotated[List[Person], ProblemFactCollectionProperty]
    time_grains: Annotated[List[TimeGrain], ProblemFactCollectionProperty, ValueRangeProvider]
    rooms: Annotated[List[Room], ProblemFactCollectionProperty, ValueRangeProvider]
    meetings: Annotated[List[Meeting], ProblemFactCollectionProperty]
    required_attendances: Annotated[List[RequiredAttendance], ProblemFactCollectionProperty] = Field(default_factory=list)
    preferred_attendances: Annotated[List[PreferredAttendance], ProblemFactCollectionProperty] = Field(default_factory=list)
    meeting_assignments: Annotated[List[MeetingAssignment], PlanningEntityCollectionProperty] = Field(default_factory=list)
    score: Annotated[Optional[HardMediumSoftScore], PlanningScore, ScoreSerializer, ScoreValidator] = None
    solver_status: SolverStatus = SolverStatus.NOT_SOLVING