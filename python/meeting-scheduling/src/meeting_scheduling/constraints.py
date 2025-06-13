from timefold.solver.score import (constraint_provider, HardMediumSoftScore, Joiners,
                                   ConstraintFactory, Constraint)

from .domain import *

@constraint_provider
def define_constraints(constraint_factory: ConstraintFactory):
    """
    Defines all constraints for the meeting scheduling problem, organized by priority (hard, medium, soft).

    Args:
        constraint_factory (ConstraintFactory): The constraint factory.
    Returns:
        List[Constraint]: All defined constraints.
    """
    return [
        # Hard constraints
        room_conflict(constraint_factory),
        avoid_overtime(constraint_factory),
        required_attendance_conflict(constraint_factory),
        required_room_capacity(constraint_factory),
        start_and_end_on_same_day(constraint_factory),
        # Medium constraints
        required_and_preferred_attendance_conflict(constraint_factory),
        preferred_attendance_conflict(constraint_factory),
        # Soft constraints
        do_meetings_as_soon_as_possible(constraint_factory),
        one_break_between_consecutive_meetings(constraint_factory),
        overlapping_meetings(constraint_factory),
        assign_larger_rooms_first(constraint_factory),
        room_stability(constraint_factory)
    ]

# ************************************************************************
# Hard constraints
# ************************************************************************

def room_conflict(constraint_factory: ConstraintFactory) -> Constraint:
    """
    Hard constraint: Prevents overlapping meetings in the same room.

    Penalizes pairs of meetings scheduled in the same room whose time slots overlap, with penalty proportional to the overlap duration.

    Args:
        constraint_factory (ConstraintFactory): The constraint factory.
    Returns:
        Constraint: The defined constraint.
    """
    return (constraint_factory
            .for_each_unique_pair(MeetingAssignment,
                                 Joiners.equal(lambda assignment: assignment.room),
                                 Joiners.overlapping(lambda assignment: assignment.get_grain_index(),
                                                    lambda assignment: assignment.get_last_time_grain_index() + 1))
            .penalize(HardMediumSoftScore.ONE_HARD,
                     lambda left_assignment, right_assignment: right_assignment.calculate_overlap(left_assignment))
            .as_constraint("Room conflict"))


def avoid_overtime(constraint_factory: ConstraintFactory) -> Constraint:
    """
    Hard constraint: Prevents meetings from extending beyond available time slots.

    Penalizes meetings that end after the last available time grain, based on how far they extend beyond the schedule.

    Args:
        constraint_factory (ConstraintFactory): The constraint factory.
    Returns:
        Constraint: The defined constraint.
    """
    return (constraint_factory
            .for_each_including_unassigned(MeetingAssignment)
            .filter(lambda meeting_assignment: meeting_assignment.starting_time_grain is not None)
            .if_not_exists(TimeGrain,
                          Joiners.equal(lambda assignment: assignment.get_last_time_grain_index(),
                                       lambda time_grain: time_grain.grain_index))
            .penalize(HardMediumSoftScore.ONE_HARD,
                     lambda meeting_assignment: meeting_assignment.get_last_time_grain_index())
            .as_constraint("Don't go in overtime"))


def required_attendance_conflict(constraint_factory: ConstraintFactory) -> Constraint:
    """
    Hard constraint: Prevents required attendees from having overlapping meetings.

    Penalizes when a person required at multiple meetings is scheduled for overlapping meetings, proportional to the overlap duration.

    Args:
        constraint_factory (ConstraintFactory): The constraint factory.
    Returns:
        Constraint: The defined constraint.
    """
    return (constraint_factory
            .for_each_unique_pair(RequiredAttendance,
                                 Joiners.equal(lambda attendance: attendance.person))
            .join(MeetingAssignment,
                 Joiners.equal(lambda left_required, right_required: left_required.meeting,
                              lambda assignment: assignment.meeting))
            .join(MeetingAssignment,
                 Joiners.equal(lambda left_required, right_required, left_assignment: right_required.meeting,
                              lambda assignment: assignment.meeting),
                 Joiners.overlapping(lambda attendee1, attendee2, assignment: assignment.get_grain_index(),
                                    lambda attendee1, attendee2, assignment: assignment.get_last_time_grain_index() + 1,
                                    lambda assignment: assignment.get_grain_index(),
                                    lambda assignment: assignment.get_last_time_grain_index() + 1))
            .penalize(HardMediumSoftScore.ONE_HARD,
                     lambda left_required, right_required, left_assignment, right_assignment: 
                     right_assignment.calculate_overlap(left_assignment))
            .as_constraint("Required attendance conflict"))


def required_room_capacity(constraint_factory: ConstraintFactory) -> Constraint:
    """
    Hard constraint: Ensures rooms have enough capacity for required attendees.

    Penalizes meetings assigned to rooms with insufficient capacity, proportional to the shortfall.

    Args:
        constraint_factory (ConstraintFactory): The constraint factory.
    Returns:
        Constraint: The defined constraint.
    """
    return (constraint_factory
            .for_each_including_unassigned(MeetingAssignment)
            .filter(lambda meeting_assignment: meeting_assignment.get_required_capacity() > meeting_assignment.get_room_capacity())
            .penalize(HardMediumSoftScore.ONE_HARD,
                     lambda meeting_assignment: meeting_assignment.get_required_capacity() - meeting_assignment.get_room_capacity())
            .as_constraint("Required room capacity"))


def start_and_end_on_same_day(constraint_factory: ConstraintFactory) -> Constraint:
    """
    Hard constraint: Ensures meetings start and end on the same day.

    Penalizes meetings that span multiple days.

    Args:
        constraint_factory (ConstraintFactory): The constraint factory.
    Returns:
        Constraint: The defined constraint.
    """
    return (constraint_factory
            .for_each_including_unassigned(MeetingAssignment)
            .filter(lambda meeting_assignment: meeting_assignment.starting_time_grain is not None)
            .join(TimeGrain,
                 Joiners.equal(lambda meeting_assignment: meeting_assignment.get_last_time_grain_index(),
                              lambda time_grain: time_grain.grain_index),
                 Joiners.filtering(lambda meeting_assignment, time_grain: 
                                  meeting_assignment.starting_time_grain.day_of_year != time_grain.day_of_year))
            .penalize(HardMediumSoftScore.ONE_HARD)
            .as_constraint("Start and end on same day"))


# ************************************************************************
# Medium constraints
# ************************************************************************

def required_and_preferred_attendance_conflict(constraint_factory: ConstraintFactory) -> Constraint:
    """
    Medium constraint: Discourages conflicts between required and preferred attendance for the same person.

    Penalizes when a person required at one meeting and preferred at another is scheduled for overlapping meetings, proportional to the overlap duration.

    Args:
        constraint_factory (ConstraintFactory): The constraint factory.
    Returns:
        Constraint: The defined constraint.
    """
    return (constraint_factory
            .for_each(RequiredAttendance)
            .join(PreferredAttendance,
                 Joiners.equal(lambda required: required.person,
                              lambda preferred: preferred.person))
            .join(MeetingAssignment,
                 Joiners.equal(lambda required, preferred: required.meeting,
                              lambda assignment: assignment.meeting))
            .join(MeetingAssignment,
                 Joiners.equal(lambda required, preferred, left_assignment: preferred.meeting,
                              lambda assignment: assignment.meeting),
                 Joiners.overlapping(lambda required, preferred, assignment: assignment.get_grain_index(),
                                    lambda required, preferred, assignment: assignment.get_last_time_grain_index() + 1,
                                    lambda assignment: assignment.get_grain_index(),
                                    lambda assignment: assignment.get_last_time_grain_index() + 1))
            .penalize(HardMediumSoftScore.ONE_MEDIUM,
                     lambda required, preferred, left_assignment, right_assignment: 
                     right_assignment.calculate_overlap(left_assignment))
            .as_constraint("Required and preferred attendance conflict"))


def preferred_attendance_conflict(constraint_factory: ConstraintFactory) -> Constraint:
    """
    Medium constraint: Discourages conflicts between preferred attendees.

    Penalizes when a person preferred at multiple meetings is scheduled for overlapping meetings, proportional to the overlap duration.

    Args:
        constraint_factory (ConstraintFactory): The constraint factory.
    Returns:
        Constraint: The defined constraint.
    """
    return (constraint_factory
            .for_each_unique_pair(PreferredAttendance,
                                 Joiners.equal(lambda attendance: attendance.person))
            .join(MeetingAssignment,
                 Joiners.equal(lambda left_attendance, right_attendance: left_attendance.meeting,
                              lambda assignment: assignment.meeting))
            .join(MeetingAssignment,
                 Joiners.equal(lambda left_attendance, right_attendance, left_assignment: right_attendance.meeting,
                              lambda assignment: assignment.meeting),
                 Joiners.overlapping(lambda attendee1, attendee2, assignment: assignment.get_grain_index(),
                                    lambda attendee1, attendee2, assignment: assignment.get_last_time_grain_index() + 1,
                                    lambda assignment: assignment.get_grain_index(),
                                    lambda assignment: assignment.get_last_time_grain_index() + 1))
            .penalize(HardMediumSoftScore.ONE_MEDIUM,
                     lambda left_attendance, right_attendance, left_assignment, right_assignment: 
                     right_assignment.calculate_overlap(left_assignment))
            .as_constraint("Preferred attendance conflict"))


# ************************************************************************
# Soft constraints
# ************************************************************************

def do_meetings_as_soon_as_possible(constraint_factory: ConstraintFactory) -> Constraint:
    """
    Soft constraint: Encourages scheduling meetings earlier in the available time slots.

    Penalizes meetings scheduled later in the available time grains, proportional to their start time.

    Args:
        constraint_factory (ConstraintFactory): The constraint factory.
    Returns:
        Constraint: The defined constraint.
    """
    return (constraint_factory
            .for_each_including_unassigned(MeetingAssignment)
            .filter(lambda meeting_assignment: meeting_assignment.starting_time_grain is not None)
            .penalize(HardMediumSoftScore.ONE_SOFT,
                     lambda meeting_assignment: meeting_assignment.starting_time_grain.grain_index)
            .as_constraint("Do meetings as soon as possible"))


def one_break_between_consecutive_meetings(constraint_factory: ConstraintFactory) -> Constraint:
    """
    Soft constraint: Rewards consecutive or nearly consecutive meetings in the same room.

    Rewards pairs of meetings in the same room that are scheduled consecutively or with at most one time grain break between them.

    Args:
        constraint_factory (ConstraintFactory): The constraint factory.
    Returns:
        Constraint: The defined constraint.
    """
    return (constraint_factory
            .for_each_unique_pair(MeetingAssignment,
                                 Joiners.equal(lambda assignment: assignment.room),
                                 Joiners.less_than(lambda a: a.get_grain_index(),
                                                 lambda b: b.get_grain_index()))
            .filter(lambda a, b: a.get_last_time_grain_index() + 2 >= b.get_grain_index())
            .reward(HardMediumSoftScore.ONE_SOFT)
            .as_constraint("One break between consecutive meetings"))


def overlapping_meetings(constraint_factory: ConstraintFactory) -> Constraint:
    """
    Soft constraint: Discourages overlapping meetings, even in different rooms.

    Penalizes pairs of meetings that overlap in time, regardless of room, proportional to the overlap duration.

    Args:
        constraint_factory (ConstraintFactory): The constraint factory.
    Returns:
        Constraint: The defined constraint.
    """
    return (constraint_factory
            .for_each_unique_pair(MeetingAssignment,
                                 Joiners.overlapping(lambda a: a.get_grain_index(),
                                                   lambda a: a.get_last_time_grain_index() + 1,
                                                   lambda b: b.get_grain_index(),
                                                   lambda b: b.get_last_time_grain_index() + 1))
            .penalize(HardMediumSoftScore.ONE_SOFT,
                     lambda a, b: a.calculate_overlap(b))
            .as_constraint("Overlapping meetings"))


def assign_larger_rooms_first(constraint_factory: ConstraintFactory) -> Constraint:
    """
    Soft constraint: Penalizes assigning smaller rooms to earlier meetings when larger rooms are assigned later.

    Penalizes when a smaller room is assigned to an earlier meeting and a larger room is assigned to a later meeting, regardless of room availability at the earlier time.

    Args:
        constraint_factory (ConstraintFactory): The constraint factory.
    Returns:
        Constraint: The defined constraint.
    """
    return (constraint_factory
            .for_each_unique_pair(MeetingAssignment,
                                 Joiners.less_than(lambda a: a.get_grain_index(),
                                                 lambda b: b.get_grain_index()))
            .filter(lambda a, b: a.room is not None and b.room is not None and
                   a.room.capacity < b.room.capacity)
            .penalize(HardMediumSoftScore.ONE_SOFT)
            .as_constraint("Assign larger rooms first"))


def room_stability(constraint_factory: ConstraintFactory) -> Constraint:
    """
    Soft constraint: Encourages keeping the same room for recurring meetings.

    Penalizes when the same meeting (by meeting ID) is assigned to different rooms across time slots. Has effect only if meetings are recurring.

    Args:
        constraint_factory (ConstraintFactory): The constraint factory.
    Returns:
        Constraint: The defined constraint.
    """
    return (constraint_factory
            .for_each_unique_pair(MeetingAssignment,
                                 Joiners.equal(lambda a: a.meeting.id))
            .filter(lambda a, b: a.room != b.room)
            .penalize(HardMediumSoftScore.ONE_SOFT)
            .as_constraint("Room stability"))