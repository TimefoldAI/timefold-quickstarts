from timefold.solver.score import *
from datetime import time

from .domain import *


@constraint_provider
def define_constraints(constraint_factory: ConstraintFactory):
    return [
        required_skill(constraint_factory),
        flight_conflict(constraint_factory),
        transfer_between_two_flights(constraint_factory),
        employee_unavailability(constraint_factory),
        first_assignment_not_departing_from_home(constraint_factory),
        last_assignment_not_arriving_at_home(constraint_factory)
    ]


def required_skill(constraint_factory: ConstraintFactory) -> Constraint:
    return (constraint_factory
            .for_each(FlightAssignment)
            .filter(lambda fa: not fa.has_required_skills())
            .penalize(HardSoftScore.of_hard(100))
            .as_constraint("Required skill"))


def flight_conflict(constraint_factory: ConstraintFactory) -> Constraint:
    return (constraint_factory
            .for_each_unique_pair(FlightAssignment,
                                  Joiners.equal(lambda fa: fa.employee),
                                  Joiners.overlapping(lambda fa: fa.flight.departure_utc_date_time,
                                                      lambda fa: fa.flight.arrival_utc_date_time))
            .penalize(HardSoftScore.of_hard(10))
            .as_constraint("Flight conflict"))


def transfer_between_two_flights(constraint_factory: ConstraintFactory) -> Constraint:
    return (constraint_factory
            .for_each(FlightAssignment)
            .join(FlightAssignment,
                  Joiners.equal(lambda fa: fa.employee),
                  Joiners.less_than(lambda fa: fa.get_departure_utc_date_time()),
                  Joiners.filtering(lambda fa1, fa2: fa1.id != fa2.id))
            .if_not_exists(FlightAssignment,
                           Joiners.equal(lambda fa1, fa2: fa1.employee, lambda fa2: fa2.employee),
                           Joiners.filtering(
                               lambda fa1, fa2, other_fa: other_fa.id != fa1.id and other_fa.id != fa2.id and
                                                          other_fa.get_departure_utc_date_time() >= fa1.get_departure_utc_date_time() and
                                                          other_fa.get_departure_utc_date_time() < fa2.get_departure_utc_date_time()))
            .filter(lambda fa1, fa2: fa1.flight.arrival_airport != fa2.flight.departure_airport)
            .penalize(HardSoftScore.of_hard(1))
            .as_constraint("Transfer between two flights"))


def employee_unavailability(constraint_factory: ConstraintFactory) -> Constraint:
    return (constraint_factory
            .for_each(FlightAssignment)
            .filter(lambda fa: fa.is_unavailable_employee())
            .penalize(HardSoftScore.of_hard(10))
            .as_constraint("Employee unavailable"))


def first_assignment_not_departing_from_home(constraint_factory: ConstraintFactory) -> Constraint:
    return (constraint_factory
            .for_each(Employee)
            .join(FlightAssignment, Joiners.equal(lambda emp: emp, lambda fa: fa.employee))
            .if_not_exists(FlightAssignment,
                           Joiners.equal(lambda emp, fa: emp, lambda fa: fa.employee),
                           Joiners.greater_than(lambda emp, fa: fa.get_departure_utc_date_time(),
                                                lambda fa: fa.get_departure_utc_date_time()))
            .filter(lambda emp, fa: emp.home_airport != fa.flight.departure_airport)
            .penalize(HardSoftScore.of_soft(1000))
            .as_constraint("First assignment not departing from home"))


def last_assignment_not_arriving_at_home(constraint_factory: ConstraintFactory) -> Constraint:
    return (constraint_factory
            .for_each(Employee)
            .join(FlightAssignment, Joiners.equal(lambda emp: emp, lambda fa: fa.employee))
            .if_not_exists(FlightAssignment,
                           Joiners.equal(lambda emp, fa: emp, lambda fa: fa.employee),
                           Joiners.less_than(lambda emp, fa: fa.get_departure_utc_date_time(),
                                             lambda fa: fa.get_departure_utc_date_time()))
            .filter(lambda emp, fa: emp.home_airport != fa.flight.arrival_airport)
            .penalize(HardSoftScore.of_soft(1000))
            .as_constraint("Last assignment not arriving at home"))
