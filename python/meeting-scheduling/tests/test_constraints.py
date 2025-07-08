from timefold.solver.test import ConstraintVerifier

from meeting_scheduling.domain import *
from meeting_scheduling.constraints import (
    define_constraints,
    room_conflict,
    avoid_overtime,
    required_attendance_conflict,
    required_room_capacity,
    start_and_end_on_same_day
)


DEFAULT_TIME_GRAINS = [
    TimeGrain(id=str(i+1), grain_index=i, day_of_year=1, 
             starting_minute_of_day=480 + i*15)
    for i in range(8)
]

DEFAULT_ROOM = Room(id="1", name="Room 1", capacity=10)
SMALL_ROOM = Room(id="2", name="Small Room", capacity=1)
LARGE_ROOM = Room(id="3", name="Large Room", capacity=2)


constraint_verifier = ConstraintVerifier.build(define_constraints, MeetingSchedule, MeetingAssignment)


def test_room_conflict_unpenalized():
    """Test that no penalty is applied when meetings in the same room do not overlap."""
    meeting1 = create_meeting(1)
    left_assignment = create_meeting_assignment(0, meeting1, DEFAULT_TIME_GRAINS[0], DEFAULT_ROOM)

    meeting2 = create_meeting(2)
    right_assignment = create_meeting_assignment(1, meeting2, DEFAULT_TIME_GRAINS[4], DEFAULT_ROOM)

    constraint_verifier.verify_that(room_conflict).given(left_assignment, right_assignment).penalizes(0)


def test_room_conflict_penalized():
    """Test that a penalty is applied when meetings in the same room overlap."""
    meeting1 = create_meeting(1)
    left_assignment = create_meeting_assignment(0, meeting1, DEFAULT_TIME_GRAINS[0], DEFAULT_ROOM)
    
    meeting2 = create_meeting(2)
    right_assignment = create_meeting_assignment(1, meeting2, DEFAULT_TIME_GRAINS[2], DEFAULT_ROOM)

    constraint_verifier.verify_that(room_conflict).given(left_assignment, right_assignment).penalizes_by(2)


def test_avoid_overtime_unpenalized():
    """Test that no penalty is applied when a meeting fits within available time grains (no overtime)."""
    meeting = create_meeting(1)
    meeting_assignment = create_meeting_assignment(0, meeting, DEFAULT_TIME_GRAINS[0], DEFAULT_ROOM)

    constraint_verifier.verify_that(avoid_overtime).given(meeting_assignment, *DEFAULT_TIME_GRAINS).penalizes(0)


def test_avoid_overtime_penalized():
    """Test that a penalty is applied when a meeting exceeds available time grains (overtime)."""
    meeting = create_meeting(1)
    meeting_assignment = create_meeting_assignment(0, meeting, DEFAULT_TIME_GRAINS[0], DEFAULT_ROOM)

    constraint_verifier.verify_that(avoid_overtime).given(meeting_assignment).penalizes_by(3)


def test_required_attendance_conflict_unpenalized():
    """Test that no penalty is applied when a person does not have overlapping required meetings."""
    person = create_person(1)
    
    left_meeting = create_meeting(1, duration=2)
    required_attendance1 = create_required_attendance(0, person, left_meeting)
    
    right_meeting = create_meeting(2, duration=2)
    required_attendance2 = create_required_attendance(1, person, right_meeting)
    
    left_assignment = create_meeting_assignment(0, left_meeting, DEFAULT_TIME_GRAINS[0], DEFAULT_ROOM)
    right_assignment = create_meeting_assignment(1, right_meeting, DEFAULT_TIME_GRAINS[2], DEFAULT_ROOM)

    constraint_verifier.verify_that(required_attendance_conflict).given(
        required_attendance1, required_attendance2,
        left_assignment, right_assignment
    ).penalizes(0)


def test_required_attendance_conflict_penalized():
    """Test that a penalty is applied when a person has overlapping required meetings."""
    person = create_person(1)

    left_meeting = create_meeting(1, duration=2)
    required_attendance1 = create_required_attendance(0, person, left_meeting)

    right_meeting = create_meeting(2, duration=2)
    required_attendance2 = create_required_attendance(1, person, right_meeting)

    left_assignment = create_meeting_assignment(0, left_meeting, DEFAULT_TIME_GRAINS[0], DEFAULT_ROOM)
    right_assignment = create_meeting_assignment(1, right_meeting, DEFAULT_TIME_GRAINS[1], DEFAULT_ROOM)
    
    constraint_verifier.verify_that(required_attendance_conflict).given(
        required_attendance1, required_attendance2,
        left_assignment, right_assignment
    ).penalizes_by(1)


def test_required_room_capacity_unpenalized():
    """Test that no penalty is applied when the room has enough capacity for all required and preferred attendees."""
    person1 = create_person(1)
    person2 = create_person(2)
    
    meeting = create_meeting(1, duration=2)
    create_required_attendance(0, person1, meeting)
    create_preferred_attendance(1, person2, meeting)
    
    meeting_assignment = create_meeting_assignment(0, meeting, DEFAULT_TIME_GRAINS[0], LARGE_ROOM)

    constraint_verifier.verify_that(required_room_capacity).given(meeting_assignment).penalizes(0)


def test_required_room_capacity_penalized():
    """Test that a penalty is applied when the room does not have enough capacity for all required and preferred attendees."""
    person1 = create_person(1)
    person2 = create_person(2)
    
    meeting = create_meeting(1, duration=2)
    create_required_attendance(0, person1, meeting)
    create_preferred_attendance(1, person2, meeting)
    
    meeting_assignment = create_meeting_assignment(0, meeting, DEFAULT_TIME_GRAINS[0], SMALL_ROOM)

    constraint_verifier.verify_that(required_room_capacity).given(meeting_assignment).penalizes_by(1)


def test_start_and_end_on_same_day_unpenalized():
    """Test that no penalty is applied when a meeting starts and ends on the same day."""
    # Need custom time grains with day_of_year=0 (DEFAULT_TIME_GRAINS use day_of_year=1)
    start_time_grain = TimeGrain(id="1", grain_index=0, day_of_year=0, starting_minute_of_day=480)
    end_time_grain = TimeGrain(id="2", grain_index=3, day_of_year=0, starting_minute_of_day=525)  # Same day
    
    meeting = create_meeting(1)
    meeting_assignment = create_meeting_assignment(0, meeting, start_time_grain, DEFAULT_ROOM)

    constraint_verifier.verify_that(start_and_end_on_same_day).given(meeting_assignment, end_time_grain).penalizes(0)


def test_start_and_end_on_same_day_penalized():
    """Test that a penalty is applied when a meeting starts and ends on different days."""
    # Need custom time grains to test different days (start=day 0, end=day 1)
    start_time_grain = TimeGrain(id="1", grain_index=0, day_of_year=0, starting_minute_of_day=480)
    end_time_grain = TimeGrain(id="2", grain_index=3, day_of_year=1, starting_minute_of_day=525)  # Different day
    
    meeting = create_meeting(1)
    meeting_assignment = create_meeting_assignment(0, meeting, start_time_grain, DEFAULT_ROOM)

    constraint_verifier.verify_that(start_and_end_on_same_day).given(meeting_assignment, end_time_grain).penalizes_by(1)


def test_multiple_constraint_violations():
    """Test that multiple constraints can be violated simultaneously."""
    person = create_person(1)
    
    left_meeting = create_meeting(1)
    required_attendance1 = create_required_attendance(0, person, left_meeting)
    left_assignment = create_meeting_assignment(0, left_meeting, DEFAULT_TIME_GRAINS[0], DEFAULT_ROOM)
    
    right_meeting = create_meeting(2)
    required_attendance2 = create_required_attendance(1, person, right_meeting)
    right_assignment = create_meeting_assignment(1, right_meeting, DEFAULT_TIME_GRAINS[2], DEFAULT_ROOM)

    constraint_verifier.verify_that(room_conflict).given(left_assignment, right_assignment).penalizes_by(2)
    constraint_verifier.verify_that(required_attendance_conflict).given(
        required_attendance1, required_attendance2, left_assignment, right_assignment
    ).penalizes_by(2)


### Helper functions ###

def create_meeting(id, topic="Meeting", duration=4):
    """Helper to create a meeting with standard parameters."""
    return Meeting(id=str(id), topic=f"{topic} {id}", duration_in_grains=duration)


def create_meeting_assignment(id, meeting, time_grain, room):
    """Helper to create a meeting assignment."""
    return MeetingAssignment(id=str(id), meeting=meeting, starting_time_grain=time_grain, room=room)


def create_person(id):
    """Helper to create a person."""
    return Person(id=str(id), full_name=f"Person {id}")


def create_required_attendance(id, person, meeting):
    """Helper to create and link required attendance."""
    attendance = RequiredAttendance(id=str(id), person=person, meeting_id=meeting.id)
    meeting.required_attendances = [attendance]
    return attendance


def create_preferred_attendance(id, person, meeting):
    """Helper to create and link preferred attendance."""
    attendance = PreferredAttendance(id=str(id), person=person, meeting_id=meeting.id)
    meeting.preferred_attendances = [attendance]
    return attendance