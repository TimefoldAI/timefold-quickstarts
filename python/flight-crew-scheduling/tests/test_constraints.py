from datetime import timedelta, datetime, date

from timefold.solver.test import ConstraintVerifier

from flight_crew_scheduling.domain import *
from flight_crew_scheduling.constraints import *


constraint_verifier = ConstraintVerifier.build(define_constraints, FlightCrewSchedule, FlightAssignment)


def test_required_skill():
    employee = Employee(id="1", name="John Doe", skills=["2"])
    assignment = FlightAssignment(id="1", flight=None, index_in_flight=0, required_skill="1", employee=employee)

    constraint_verifier.verify_that(required_skill).given(assignment).penalizes_by(1)  # Missing required skill


def test_flight_conflict():
    employee = Employee(id="1", name="John Doe")

    flight1 = Flight(
        flight_number="1",
        departure_utc_date_time=datetime.now(),
        arrival_utc_date_time=datetime.now() + timedelta(minutes=10),
    )
    assignment1 = FlightAssignment(id="1", flight=flight1, index_in_flight=0, required_skill="1", employee=employee)

    overlapping_flight = Flight(
        flight_number="2",
        departure_utc_date_time=datetime.now() + timedelta(minutes=1),
        arrival_utc_date_time=datetime.now() + timedelta(minutes=11),
    )
    overlapping_assignment = FlightAssignment(id="2", flight=overlapping_flight, index_in_flight=0, required_skill="1", employee= employee)

    # one overlapping thirdFlight
    constraint_verifier.verify_that(flight_conflict).given(assignment1, overlapping_assignment).penalizes_by(1)


def test_transfer_between_two_flights():
    employee = Employee(id="1", name="John Doe")

    first_airport = Airport(code="1", name="Airport1")
    second_airport = Airport(code="2", name="Airport2")

    first_flight = Flight(
        flight_number="1",
        departure_airport=first_airport,
        departure_utc_date_time=datetime.now(),
        arrival_airport=second_airport,
        arrival_utc_date_time=datetime.now() + timedelta(minutes=10),
    )
    first_assignment = FlightAssignment(id="1", flight=first_flight, index_in_flight=0, required_skill="1", employee=employee)

    first_invalid_flight = Flight(
        flight_number="2",
        departure_airport=first_airport,
        departure_utc_date_time=datetime.now() + timedelta(minutes=11),
        arrival_airport=second_airport,
        arrival_utc_date_time=datetime.now() + timedelta(minutes=12),
    )
    first_invalid_assignment = FlightAssignment(id="2", flight=first_invalid_flight, index_in_flight=0,
                                                required_skill="1", employee=employee)

    second_invalid_flight = Flight(
        flight_number="3",
        departure_airport=first_airport,
        departure_utc_date_time=datetime.now() + timedelta(minutes=13),
        arrival_airport=second_airport,
        arrival_utc_date_time=datetime.now() + timedelta(minutes=14),
    )
    second_invalid_assignment = FlightAssignment(id="3", flight=second_invalid_flight, index_in_flight=0,
                                                 required_skill="1", employee=employee)

    constraint_verifier.verify_that(transfer_between_two_flights).given(
        first_assignment, first_invalid_assignment, second_invalid_assignment
    ).penalizes_by(2) # two invalid connections


def test_employee_unavailability():
    unavailability_date = date.today()
    employee = Employee(id="1", name="John Doe", unavailable_days=[unavailability_date])

    flight = Flight(
        flight_number="1",
        departure_utc_date_time=datetime.combine(unavailability_date, datetime.min.time()),
        arrival_utc_date_time=datetime.combine(unavailability_date, datetime.min.time()) + timedelta(minutes=10),
    )
    assignment = FlightAssignment(id="1", flight=flight, index_in_flight=0, required_skill="1", employee=employee)

    constraint_verifier.verify_that(employee_unavailability).given(assignment).penalizes_by(1) # unavailable at departure

    flight.departure_utc_date_time = datetime.combine(unavailability_date - timedelta(days=1), datetime.min.time())
    constraint_verifier.verify_that(employee_unavailability).given(assignment).penalizes_by(1) # unavailable during flight

    flight.departure_utc_date_time = datetime.combine(unavailability_date + timedelta(days=1), datetime.min.time())
    flight.arrival_utc_date_time = flight.departure_utc_date_time + timedelta(minutes=10)

    constraint_verifier.verify_that(employee_unavailability).given(assignment).penalizes_by(0) # employee available


def test_first_assignment_not_departing_from_home():
    employee = Employee(id="1", name="John Doe", home_airport=Airport(code="1", name="Home"), unavailable_days=[date.today()])

    flight = Flight(
        flight_number="1",
        departure_airport=Airport(code="2", name="Airport2"),
        departure_utc_date_time=datetime.now(),
        arrival_airport=Airport(code="3", name="Airport3"),
        arrival_utc_date_time=datetime.now() + timedelta(minutes=10),
    )
    assignment = FlightAssignment(id="1", flight=flight, index_in_flight=0, required_skill="1", employee= employee)

    flight2 = Flight(
        flight_number="2",
        departure_airport=Airport(code="2", name="Airport2"),
        departure_utc_date_time=datetime.now() + timedelta(minutes=1),
        arrival_airport=Airport(code="3", name="Airport3"),
        arrival_utc_date_time=datetime.now() + timedelta(minutes=10),
    )
    assignment2 = FlightAssignment(id="2", flight=flight2, index_in_flight=0, required_skill="1", employee=employee)

    flight3 = Flight(
        flight_number="3",
        departure_airport=Airport(code="2", name="Airport2"),
        departure_utc_date_time=datetime.now() + timedelta(minutes=1),
        arrival_airport=Airport(code="3", name="Airport3"),
        arrival_utc_date_time=datetime.now() + timedelta(minutes=10),
    )
    assignment3 = FlightAssignment(id="3", flight=flight3, index_in_flight=0, required_skill="1", employee=employee)

    second_employee = Employee(id="2", name="Jane Doe", home_airport=Airport(code="3", name="Airport3"),
                               unavailable_days=[date.today()])

    flight4 = Flight(
        flight_number="4",
        departure_airport=Airport(code="3", name="Airport3"),
        departure_utc_date_time=datetime.now(),
        arrival_airport=Airport(code="4", name="Airport4"),
        arrival_utc_date_time=datetime.now() + timedelta(minutes=10),
    )
    assignment4 = FlightAssignment(id="4", flight=flight4, index_in_flight=0, required_skill="1",
                                   employee=second_employee)

    (constraint_verifier.verify_that(first_assignment_not_departing_from_home).given(
        employee, second_employee, assignment, assignment2, assignment3, assignment4)
     .penalizes_by(1))  # invalid first airport


def test_last_assignment_not_arriving_at_home():
    employee = Employee(id="1", name="John Doe", home_airport=Airport(code="1", name="Home"), unavailable_days=[date.today()])

    first_flight = Flight(
        flight_number="1",
        departure_airport=Airport(code="2", name="Airport2"),
        departure_utc_date_time=datetime.now(),
        arrival_airport=Airport(code="3", name="Airport3"),
        arrival_utc_date_time=datetime.now() + timedelta(minutes=10),
    )
    first_assignment = FlightAssignment(id="1", flight=first_flight, index_in_flight=0, required_skill="1", employee=employee)

    second_flight = Flight(
        flight_number="2",
        departure_airport=Airport(code="3", name="Airport3"),
        departure_utc_date_time=datetime.now() + timedelta(minutes=11),
        arrival_airport=Airport(code="4", name="Airport4"),
        arrival_utc_date_time=datetime.now() + timedelta(minutes=12),
    )
    second_assignment = FlightAssignment(id="2", flight=second_flight, index_in_flight=0, required_skill="1", employee=employee)

    second_employee = Employee(id="2", name="Jane Doe", home_airport=Airport(code="2", name="Airport2"),
                               unavailable_days=[date.today()])

    flight3 = Flight(
        flight_number="3",
        departure_airport=Airport(code="2", name="Airport2"),
        departure_utc_date_time=datetime.now(),
        arrival_airport=Airport(code="3", name="Airport3"),
        arrival_utc_date_time=datetime.now() + timedelta(minutes=10),
    )
    third_assignment = FlightAssignment(id="3", flight=flight3, index_in_flight=0, required_skill="1",
                                   employee=second_employee)

    flight4 = Flight(
        flight_number="4",
        departure_airport=Airport(code="3", name="Airport3"),
        departure_utc_date_time=datetime.now() + timedelta(minutes=11),
        arrival_airport=Airport(code="2", name="Airport2"),
        arrival_utc_date_time=datetime.now() + timedelta(minutes=12),
    )
    fourth_assignment = FlightAssignment(id="4", flight=flight4, index_in_flight=0, required_skill="1",
                                   employee=second_employee)

    (constraint_verifier.verify_that(last_assignment_not_arriving_at_home).given(
        employee, second_employee, first_assignment, second_assignment, third_assignment, fourth_assignment).
     penalizes_by(1))  # invalid last airport