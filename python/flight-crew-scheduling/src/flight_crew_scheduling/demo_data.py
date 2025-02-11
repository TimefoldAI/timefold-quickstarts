from itertools import count
from random import Random
from datetime import datetime, time, timedelta
from typing import List, Callable, TypeVar

from .domain import *

random = Random(0)
T = TypeVar('T')

# Constants for employee skills
ATTENDANT_SKILL = "Flight attendant"
PILOT_SKILL = "Pilot"

# First names and last names
FIRST_NAMES = ["Amy", "Beth", "Carl", "Dan", "Elsa", "Flo", "Gus", "Hugo", "Ivy", "Jay",
               "Jeri", "Hope", "Avis", "Lino", "Lyle", "Nick", "Dino", "Otha", "Gwen", "Jose",
               "Dena", "Jana", "Dave", "Russ", "Josh", "Dana", "Katy"]

LAST_NAMES = ["Cole", "Fox", "Green", "Jones", "King", "Li", "Poe", "Rye", "Smith", "Watt", "Howe",
              "Lowe", "Wise", "Clay", "Carr", "Hood", "Long", "Horn", "Haas", "Meza"]


def generate_demo_data() -> FlightCrewSchedule:
    # Airports
    airports = [
        Airport(code="LHR",name="LHR", latitude=51.4775, longitude=-0.461389),
        Airport(code="JFK", name="JFK", latitude=40.639722, longitude=-73.778889),
        Airport(code="CNF", name="CNF", latitude=-19.624444, longitude=-43.971944),
        Airport(code="BRU", name="BRU", latitude=50.901389, longitude=4.484444),
        Airport(code="ATL", name="ATL", latitude=33.636667, longitude=-84.428056),
        Airport(code="BNE", name="BNE", latitude=-27.383333, longitude=153.118333)
    ]
    distances = {
        "LHR-JFK": 8, "LHR-CNF": 12, "LHR-BRU": 13, "LHR-ATL": 9, "LHR-BNE": 21,
        "JFK-LHR": 8, "JFK-BRU": 14, "JFK-CNF": 10, "JFK-ATL": 6, "JFK-BNE": 20,
        "CNF-LHR": 12, "CNF-JFK": 10, "CNF-BRU": 19, "CNF-ATL": 10, "CNF-BNE": 19,
        "BRU-LHR": 13, "BRU-JFK": 14, "BRU-CNF": 19, "BRU-ATL": 9, "BRU-BNE": 21,
        "ATL-LHR": 9, "ATL-JFK": 6, "ATL-CNF": 10, "ATL-BRU": 9, "ATL-BNE": 18,
        "BNE-LHR": 21, "BNE-JFK": 20, "BNE-CNF": 19, "BNE-BRU": 21, "BNE-ATL": 18
    }

    # Flights
    first_date = date.today()
    count_days = 5
    dates = [first_date + timedelta(days=i) for i in range(count_days)]
    home_airports = random.sample(airports, 2)
    times = [time(hour=i, minute=0) for i in range(24)]
    count_flights = 14
    flights = generate_flights(count_flights, datetime.now() + timedelta(minutes=1), airports, home_airports, dates,
                               times, distances)
    # Flight assignments
    flight_assignments = generate_flight_assignments(flights)
    # Employees
    employees = generate_employees(flights, dates)

    # Flight Crew Schedule
    schedule = FlightCrewSchedule(airports=airports, employees=employees, flights=flights,
                                  flight_assignments=flight_assignments, score=None, solver_status=SolverStatus.NOT_SOLVING)

    return schedule


def generate_flights(size: int, start_datetime: datetime, airports: List[Airport],
                     home_airports: List[Airport], dates: List[datetime.date],
                     time_groups: List[datetime.time], distances: Dict[str, int]) -> List[Flight]:
    if size % 2 != 0:
        raise ValueError("The size of flights must be even")

    # Departure and arrival airports
    flights = []
    remaining_airports = [airport for airport in airports if airport not in home_airports]
    count_flights = 0

    while count_flights < size:
        route_size = pick_random_route_size(count_flights, size)
        home_airport = random.choice(home_airports)
        home_flight = Flight(flight_number=str(count_flights), departure_airport=home_airport, arrival_airport=random.choice(remaining_airports))
        flights.append(home_flight)
        count_flights += 1

        next_flight = home_flight
        for _ in range(route_size - 2):
            next_flight = Flight(
                flight_number=str(count_flights),
                departure_airport=next_flight.arrival_airport,
                arrival_airport=pick_random_airport(remaining_airports, next_flight.arrival_airport.code)
            )
            flights.append(next_flight)
            count_flights += 1

        flights.append(Flight(flight_number=str(count_flights), departure_airport=next_flight.arrival_airport, arrival_airport=home_flight.departure_airport))
        count_flights += 1

    # Assign flight numbers
    for i, flight in enumerate(flights):
        flight.flight_number = f"Flight {i + 1}"

    # Assign flight durations
    count_dates = size // len(dates)

    def flight_consumer(f: Flight, d: date):
        key = f"{f.departure_airport.code}-{f.arrival_airport.code}"
        count_hours = distances[key]
        start_time = random.choice(time_groups)
        departure_datetime = datetime.combine(d, start_time)

        if departure_datetime < start_datetime:
            departure_datetime = start_datetime + timedelta(hours=random.randint(0, 4))

        arrival_datetime = departure_datetime + timedelta(hours=count_hours)
        f.departure_utc_date_time = departure_datetime
        f.arrival_utc_date_time = arrival_datetime

    for start_date in dates:
        apply_random_value(count_dates, flights, lambda f: f.departure_utc_date_time is None, lambda f: flight_consumer(f, start_date))

    # Ensure no flights are left without assigned dates
    for flight in flights:
        if flight.departure_utc_date_time is None:
            flight_consumer(flight, random.choice(dates))

    return flights


def generate_employees(flights: List[Flight], dates: List[date]) -> List[Employee]:
    name_supplier = lambda: f"{random.choice(FIRST_NAMES)} {random.choice(LAST_NAMES)}"

    # Get distinct departure airports from flights
    flight_airports = list({flight.departure_airport for flight in flights})

    # Two pilots and three attendants per airport
    employees = []
    ids = map(str, count())

    for airport in flight_airports:
        for _ in range(2):  # Two teams per airport
            employees.append(Employee(id=next(ids), name=name_supplier(), home_airport=airport, skills=[PILOT_SKILL], unavailable_days=[]))
            employees.append(Employee(id=next(ids), name=name_supplier(), home_airport=airport, skills=[PILOT_SKILL], unavailable_days=[]))
            employees.append(Employee(id=next(ids), name=name_supplier(), home_airport=airport, skills=[ATTENDANT_SKILL], unavailable_days=[]))
            employees.append(Employee(id=next(ids), name=name_supplier(), home_airport=airport, skills=[ATTENDANT_SKILL], unavailable_days=[]))

            if airport.code == "CNF":
                employees.append(Employee(id=next(ids), name=name_supplier(), home_airport=airport, skills=[ATTENDANT_SKILL], unavailable_days=[]))

    # Assign unavailable dates: 28% for one date, 4% for two dates
    apply_random_value(
        int(0.28 * len(employees)),
        employees,
        lambda e: not e.unavailable_days,
        lambda e: setattr(e, 'unavailable_days', [random.choice(dates)])
    )

    apply_random_value(
        int(0.04 * len(employees)),
        employees,
        lambda e: not e.unavailable_days,
        lambda e: setattr(e, 'unavailable_days', random.sample(dates, 2))
    )

    return employees


def pick_random_airport(airports: List[Airport], exclude_code: str) -> Airport:
    airport = None
    while airport is None or airport.code == exclude_code:
        airport = random.choice(airports)
    return airport

def pick_random_route_size(count_flights: int, max_count_flights: int) -> int:
    allowed_sizes = [2, 4, 6]
    limit = max_count_flights - count_flights
    route_size = 0
    while route_size == 0 or route_size > limit:
        route_size = random.choice(allowed_sizes)
    return route_size


def generate_flight_assignments(flights: List[Flight]) -> List[FlightAssignment]:
    # 2 pilots and 2 or 3 attendants
    flight_assignments = []
    id_counter = count(1)

    for flight in flights:
        index_skill = count(1)

        flight_assignments.append(FlightAssignment(
            id=str(next(id_counter)),
            flight=flight,
            index_in_flight=next(index_skill),
            required_skill=PILOT_SKILL
        ))

        flight_assignments.append(FlightAssignment(
            id=str(next(id_counter)),
            flight=flight,
            index_in_flight=next(index_skill),
            required_skill=PILOT_SKILL
        ))

        flight_assignments.append(FlightAssignment(
            id=str(next(id_counter)),
            flight=flight,
            index_in_flight=next(index_skill),
            required_skill=ATTENDANT_SKILL
        ))

        flight_assignments.append(FlightAssignment(
            id=str(next(id_counter)),
            flight=flight,
            index_in_flight=next(index_skill),
            required_skill=ATTENDANT_SKILL
        ))

        if flight.departure_airport.code == "CNF" or flight.arrival_airport.code == "CNF":
            flight_assignments.append(FlightAssignment(
                id=str(next(id_counter)),
                flight=flight,
                index_in_flight=next(index_skill),
                required_skill=ATTENDANT_SKILL
            ))

    return flight_assignments


def apply_random_value(counter: int, values: List[T], filter_func: Callable[[T], bool],
                       consumer: Callable[[T], None]) -> None:
    filtered_values = [v for v in values if filter_func(v)]
    size = len(filtered_values)

    for _ in range(counter):
        if size <= 0:
            break
        selected_item = random.choice(filtered_values) if size > 0 else None
        if selected_item:
            consumer(selected_item)
            filtered_values.remove(selected_item)
            size -= 1