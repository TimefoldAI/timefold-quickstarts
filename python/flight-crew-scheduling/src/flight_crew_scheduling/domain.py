from timefold.solver import SolverStatus
from timefold.solver.domain import (planning_entity, planning_solution, PlanningId, PlanningVariable,
                                    PlanningEntityCollectionProperty,
                                    ProblemFactCollectionProperty, ValueRangeProvider,
                                    PlanningScore)
from timefold.solver.score import HardSoftScore
from typing import Dict, List, Any, Annotated, Tuple, Optional
from .json_serialization import *
from datetime import date, timedelta, datetime


class Airport(JsonDomainBase):
    code: Annotated[str, PlanningId]
    name: str
    latitude: Annotated[float, Field(default=0.0)]
    longitude: Annotated[float, Field(default=0.0)]
    taxi_time_in_minutes: Annotated[Dict[str, int] | None,
                                   TaxiTimeValidator,
                                   Field(default_factory=dict)]


    def __str__(self) -> str:
        return self.name

    def __repr__(self) -> str:
        return (f"Airport(code={self.code}, name={self.name}, latitude={self.latitude}, longitude={self.longitude},"
                f" taxi_time_in_minutes={self.taxi_time_in_minutes})")

    def __eq__(self, other: object) -> bool:
        if not isinstance(other, Airport):
            return False
        return self.code == other.code

    def __hash__(self) -> int:
        return hash(self.code)

    def __lt__(self, other: "Airport") -> bool:
        return self.code < other.code


class Employee(JsonDomainBase):
    id: Annotated[str, PlanningId]
    name: str
    home_airport: Annotated[Airport | None, IdSerializer, AirportDeserializer, Field(default=None)]
    skills: Annotated[List[str], Field(default_factory=list)]
    unavailable_days: Annotated[List[date], Field(default_factory=list)]


    def has_skill(self, skill: str) -> bool:
        """Checks if the employee has a specific skill."""
        return skill in self.skills

    def is_available(self, from_date_inclusive: date, to_date_inclusive: date) -> bool:
        """Checks if the employee is available between two dates."""
        if len(self.unavailable_days) == 0:
            return True
        current_date = from_date_inclusive
        while current_date <= to_date_inclusive:
            if current_date in self.unavailable_days:
                return False
            current_date += timedelta(days=1)
        return True

    def __str__(self) -> str:
        return self.name

    def __repr__(self) -> str:
        return (f"Employee(id={self.id}, name={self.name}, home_airport={self.home_airport}, skills={self.skills},"
                f" unavailable_days={self.unavailable_days})")

    def __eq__(self, other: object) -> bool:
        if not isinstance(other, Employee):
            return False
        return self.id == other.id

    def __hash__(self) -> int:
        return hash(self.id)


class Flight(JsonDomainBase):
    flight_number: Annotated[str, PlanningId]
    departure_airport: Annotated[Airport | None, IdSerializer, AirportDeserializer, Field(default=None)]
    departure_utc_date_time: Annotated[datetime | None, Field(default=None, alias="departureUTCDateTime")]
    arrival_airport: Annotated[Airport | None, IdSerializer, AirportDeserializer, Field(default=None)]
    arrival_utc_date_time: Annotated[datetime | None, Field(default=None, alias="arrivalUTCDateTime")]


    def get_departure_utc_date(self) -> date:
        """Retrieve flight's departure date."""
        return self.departure_utc_date_time.date()

    def __str__(self) -> str:
        return f"{self.flight_number}@{self.get_departure_utc_date()}"

    def __repr__(self) -> str:
        return (f"Flight(flight_number={self.flight_number}, departure_airport={self.departure_airport}, departure_utc_date_time={self.departure_utc_date_time},"
                f" arrival_airport={self.arrival_airport}, arrival_utc_date_time{self.arrival_utc_date_time})")

    def __eq__(self, other: object) -> bool:
        if not isinstance(other, Flight):
            return False
        return self.flight_number == other.flight_number

    def __hash__(self) -> int:
        return hash(self.flight_number)

    def _comparison_key(self) -> Tuple[Optional[datetime], Optional[Airport], Optional[datetime], Optional[Airport], str]:
        return (self.departure_utc_date_time, self.departure_airport, self.arrival_utc_date_time, self.arrival_airport, self.flight_number)

    def __lt__(self, other: "Flight") -> bool:
        return self._comparison_key() < other._comparison_key()


@planning_entity
class FlightAssignment(JsonDomainBase):
    id: Annotated[str, PlanningId]
    flight: Annotated[Flight | None, IdSerializer, FlightDeserializer, Field(default=None)]
    index_in_flight: int
    required_skill: str
    employee: Annotated[Employee | None, PlanningVariable, IdSerializer, EmployeeDeserializer, Field(default=None)]


    def has_required_skills(self) -> bool:
        """Checks if the employee has a specific skill."""
        return self.employee is not None and self.employee.has_skill(self.required_skill)

    def is_unavailable_employee(self) -> bool:
        """Checks if the employee is unavailable."""
        return self.employee is not None and not self.employee.is_available(
            self.flight.get_departure_utc_date(), self.flight.arrival_utc_date_time.date()
        )

    def get_departure_utc_date_time(self) -> datetime:
        """Retrieve flight's departure date time."""
        return self.flight.departure_utc_date_time

    def __str__(self) -> str:
        return f"{self.flight}-{self.index_in_flight}"

    def __repr__(self) -> str:
        return (f"FlightAssignment(id='{self.id}', flight={self.flight}, index_in_flight={self.index_in_flight},"
                f" required_skill='{self.required_skill}', employee={self.employee})")

    def __eq__(self, other: object) -> bool:
        if not isinstance(other, FlightAssignment):
            return False
        return self.id == other.id

    def __hash__(self) -> int:
        return hash(self.id)


@planning_solution
class FlightCrewSchedule(JsonDomainBase):
    airports: Annotated[list[Airport],
                       ProblemFactCollectionProperty]
    employees: Annotated[list[Employee],
                        ProblemFactCollectionProperty,
                        ValueRangeProvider]
    flights: Annotated[list[Flight],
                      ProblemFactCollectionProperty]
    flight_assignments: Annotated[list[FlightAssignment],
                                 PlanningEntityCollectionProperty]
    score: Annotated[HardSoftScore | None,
                    PlanningScore,
                    ScoreSerializer,
                    ScoreValidator,
                    Field(default=None)]
    solver_status: Annotated[SolverStatus | None, Field(default=SolverStatus.NOT_SOLVING)]
