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


def test_room_conflict_unpenalized():
    """Test that no penalty is applied when meetings in the same room do not overlap."""
    # Provide enough time grains to cover both meetings without overtime
    time_grains = [
        TimeGrain(id="1", grain_index=0, day_of_year=1, starting_minute_of_day=480),
        TimeGrain(id="2", grain_index=1, day_of_year=1, starting_minute_of_day=495),
        TimeGrain(id="3", grain_index=2, day_of_year=1, starting_minute_of_day=510),
        TimeGrain(id="4", grain_index=3, day_of_year=1, starting_minute_of_day=525),
        TimeGrain(id="5", grain_index=4, day_of_year=1, starting_minute_of_day=540),
        TimeGrain(id="6", grain_index=5, day_of_year=1, starting_minute_of_day=555),
        TimeGrain(id="7", grain_index=6, day_of_year=1, starting_minute_of_day=570),
        TimeGrain(id="8", grain_index=7, day_of_year=1, starting_minute_of_day=585)
    ]

    room = Room(id="1", name="Room 1", capacity=10)
    
    # Create time grains for all needed indices
    meeting1 = Meeting(id="1", topic="Meeting 1", duration_in_grains=4)
    left_assignment = MeetingAssignment(id="0", meeting=meeting1, starting_time_grain=time_grains[0], room=room)

    # Overlap meetings in the same room
    meeting2 = Meeting(id="2", topic="Meeting 2", duration_in_grains=4)
    right_assignment = MeetingAssignment(id="1", meeting=meeting2, starting_time_grain=time_grains[4], room=room)

    constraint_verifier = ConstraintVerifier.build(define_constraints, MeetingSchedule, MeetingAssignment)
    constraint_verifier.verify_that(room_conflict).given(left_assignment, right_assignment).penalizes(0)


def test_room_conflict_penalized():
    """Test that a penalty is applied when meetings in the same room overlap."""
    # Provide enough time grains to cover both meetings without overtime
    time_grains = [
        TimeGrain(id="1", grain_index=0, day_of_year=1, starting_minute_of_day=480),
        TimeGrain(id="2", grain_index=1, day_of_year=1, starting_minute_of_day=495),
        TimeGrain(id="3", grain_index=2, day_of_year=1, starting_minute_of_day=510),
        TimeGrain(id="4", grain_index=3, day_of_year=1, starting_minute_of_day=525),
        TimeGrain(id="5", grain_index=4, day_of_year=1, starting_minute_of_day=540),
        TimeGrain(id="6", grain_index=5, day_of_year=1, starting_minute_of_day=555),
        TimeGrain(id="7", grain_index=6, day_of_year=1, starting_minute_of_day=570),
        TimeGrain(id="8", grain_index=7, day_of_year=1, starting_minute_of_day=585)
    ]

    room = Room(id="1", name="Room 1", capacity=10)

    # Overlap meetings in the same room
    meeting1 = Meeting(id="1", topic="Meeting 1", duration_in_grains=4)
    left_assignment = MeetingAssignment(id="0", meeting=meeting1, starting_time_grain=time_grains[0], room=room)
    meeting2 = Meeting(id="2", topic="Meeting 2", duration_in_grains=4)
    right_assignment = MeetingAssignment(id="1", meeting=meeting2, starting_time_grain=time_grains[2], room=room)

    constraint_verifier = ConstraintVerifier.build(define_constraints, MeetingSchedule, MeetingAssignment)
    constraint_verifier.verify_that(room_conflict).given(left_assignment, right_assignment).penalizes_by(2)


def test_avoid_overtime_unpenalized():
    """Test that no penalty is applied when a meeting fits within available time grains (no overtime)."""
    time_grain = TimeGrain(id="1", grain_index=3, day_of_year=1, starting_minute_of_day=525)
    assignment_time_grain = TimeGrain(id="2", grain_index=0, day_of_year=1, starting_minute_of_day=480)

    time_grains = [
        assignment_time_grain,
        TimeGrain(id="3", grain_index=1, day_of_year=1, starting_minute_of_day=495),
        TimeGrain(id="4", grain_index=2, day_of_year=1, starting_minute_of_day=510),
        time_grain
    ]

    room = Room(id="1", name="Room 1", capacity=10)
    meeting = Meeting(id="1", topic="Meeting 1", duration_in_grains=4)
    meeting_assignment = MeetingAssignment(id="0", meeting=meeting, starting_time_grain=assignment_time_grain, room=room)

    constraint_verifier = ConstraintVerifier.build(define_constraints, MeetingSchedule, MeetingAssignment)
    constraint_verifier.verify_that(avoid_overtime).given(meeting_assignment, *time_grains).penalizes(0)


def test_avoid_overtime_penalized():
    """Test that a penalty is applied when a meeting exceeds available time grains (overtime)."""
    # Only one meeting, not enough time grains for its duration
    time_grains = [
        TimeGrain(id="1", grain_index=0, day_of_year=1, starting_minute_of_day=480),
        TimeGrain(id="2", grain_index=1, day_of_year=1, starting_minute_of_day=495)
    ]

    room = Room(id="1", name="Room 1", capacity=10)
    meeting = Meeting(id="1", topic="Meeting 1", duration_in_grains=4)
    meeting_assignment = MeetingAssignment(id="0", meeting=meeting, starting_time_grain=time_grains[0], room=room)

    constraint_verifier = ConstraintVerifier.build(define_constraints, MeetingSchedule, MeetingAssignment)
    constraint_verifier.verify_that(avoid_overtime).given(meeting_assignment).penalizes_by(3)


def test_required_attendance_conflict_unpenalized():
    """Test that no penalty is applied when a person does not have overlapping required meetings."""
    time_grains = [
        TimeGrain(id="1", grain_index=0, day_of_year=1, starting_minute_of_day=480),
        TimeGrain(id="2", grain_index=1, day_of_year=1, starting_minute_of_day=495),
        TimeGrain(id="3", grain_index=2, day_of_year=1, starting_minute_of_day=510),
        TimeGrain(id="4", grain_index=3, day_of_year=1, starting_minute_of_day=525)
    ]

    room = Room(id="1", name="Room 1", capacity=10)
    person = Person(id="1", full_name="Person 1")

    left_meeting = Meeting(id="1", topic="Meeting 1", duration_in_grains=2)
    required_attendance1 = RequiredAttendance(id="0", person=person, meeting_id=left_meeting.id)
    required_attendance1.meeting = left_meeting
    left_meeting.required_attendances = [required_attendance1]

    right_meeting = Meeting(id="2", topic="Meeting 2", duration_in_grains=2)
    required_attendance2 = RequiredAttendance(id="1", person=person, meeting_id=right_meeting.id)
    required_attendance2.meeting = right_meeting
    right_meeting.required_attendances = [required_attendance2]
    
    left_assignment = MeetingAssignment(id="0", meeting=left_meeting, starting_time_grain=time_grains[0], room=room)
    right_assignment = MeetingAssignment(id="1", meeting=right_meeting, starting_time_grain=time_grains[2], room=room)

    constraint_verifier = ConstraintVerifier.build(define_constraints, MeetingSchedule, MeetingAssignment)
    constraint_verifier.verify_that(required_attendance_conflict).given(required_attendance1, required_attendance2, left_assignment, right_assignment).penalizes(0)


def test_required_attendance_conflict_penalized():
    """Test that a penalty is applied when a person has overlapping required meetings."""
    time_grains = [
        TimeGrain(id="1", grain_index=0, day_of_year=1, starting_minute_of_day=480),
        TimeGrain(id="2", grain_index=1, day_of_year=1, starting_minute_of_day=495),
        TimeGrain(id="3", grain_index=2, day_of_year=1, starting_minute_of_day=510)
    ]

    room = Room(id="1", name="Room 1", capacity=10)
    person = Person(id="1", full_name="Person 1")

    left_meeting = Meeting(id="1", topic="Meeting 1", duration_in_grains=2)
    required_attendance1 = RequiredAttendance(id="0", person=person, meeting_id=left_meeting.id)
    required_attendance1.meeting = left_meeting
    left_meeting.required_attendances = [required_attendance1]

    right_meeting = Meeting(id="2", topic="Meeting 2", duration_in_grains=2)
    required_attendance2 = RequiredAttendance(id="1", person=person, meeting_id=right_meeting.id)
    required_attendance2.meeting = right_meeting
    right_meeting.required_attendances = [required_attendance2]

    left_assignment = MeetingAssignment(id="0", meeting=left_meeting, starting_time_grain=time_grains[0], room=room)
    right_assignment = MeetingAssignment(id="1", meeting=right_meeting, starting_time_grain=time_grains[1], room=room)
    
    constraint_verifier = ConstraintVerifier.build(define_constraints, MeetingSchedule, MeetingAssignment)
    constraint_verifier.verify_that(required_attendance_conflict).given(
        required_attendance1, required_attendance2,
        left_assignment, right_assignment,
        left_meeting, right_meeting,
        person, *time_grains
    ).penalizes_by(1)


def test_required_room_capacity_unpenalized():
    """Test that no penalty is applied when the room has enough capacity for all required and preferred attendees."""
    time_grains = [
        TimeGrain(id="1", grain_index=0, day_of_year=1, starting_minute_of_day=480),
        TimeGrain(id="2", grain_index=1, day_of_year=1, starting_minute_of_day=495)
    ]

    room = Room(id="1", name="Room 1", capacity=2)

    person1 = Person(id="1", full_name="Person 1")
    
    required_attendance = RequiredAttendance(id="0", person=person1, meeting_id="1")
    meeting = Meeting(id="1", topic="Meeting 1", duration_in_grains=2)
    required_attendance.meeting = meeting
    meeting.required_attendances = [required_attendance]
    
    person2 = Person(id="2", full_name="Person 2")
    
    preferred_attendance = PreferredAttendance(id="1", person=person2, meeting_id="1")
    preferred_attendance.meeting = meeting
    meeting.preferred_attendances = [preferred_attendance]
    
    meeting_assignment = MeetingAssignment(id="0", meeting=meeting, starting_time_grain=time_grains[0], room=room)

    constraint_verifier = ConstraintVerifier.build(define_constraints, MeetingSchedule, MeetingAssignment)
    constraint_verifier.verify_that(required_room_capacity).given(meeting_assignment).penalizes(0)


def test_required_room_capacity_penalized():
    """Test that a penalty is applied when the room does not have enough capacity for all required and preferred attendees."""
    time_grains = [
        TimeGrain(id="1", grain_index=0, day_of_year=1, starting_minute_of_day=480),
        TimeGrain(id="2", grain_index=1, day_of_year=1, starting_minute_of_day=495)
    ]

    room = Room(id="1", name="Room 1", capacity=1)
    
    person1 = Person(id="1", full_name="Person 1")
    required_attendance = RequiredAttendance(id="0", person=person1, meeting_id="1")
    meeting = Meeting(id="1", topic="Meeting 1", duration_in_grains=2)
    required_attendance.meeting = meeting
    meeting.required_attendances = [required_attendance]
    
    person2 = Person(id="2", full_name="Person 2")
    preferred_attendance = PreferredAttendance(id="1", person=person2, meeting_id="1")
    preferred_attendance.meeting = meeting
    meeting.preferred_attendances = [preferred_attendance]
    
    meeting_assignment = MeetingAssignment(id="0", meeting=meeting, starting_time_grain=time_grains[0], room=room)

    constraint_verifier = ConstraintVerifier.build(define_constraints, MeetingSchedule, MeetingAssignment)
    constraint_verifier.verify_that(required_room_capacity).given(meeting_assignment).penalizes_by(1)


def test_start_and_end_on_same_day_unpenalized():
    """Test that no penalty is applied when a meeting starts and ends on the same day."""
    start_time_grain = TimeGrain(id="1", grain_index=0, day_of_year=0, starting_minute_of_day=480)
    end_time_grain = TimeGrain(id="2", grain_index=3, day_of_year=0, starting_minute_of_day=525)
    
    meeting = Meeting(id="1", topic="Meeting 1", duration_in_grains=4)
    room = Room(id="1", name="Room 1", capacity=10)
    
    meeting_assignment = MeetingAssignment(id="0", meeting=meeting, starting_time_grain=start_time_grain, room=room)

    constraint_verifier = ConstraintVerifier.build(define_constraints, MeetingSchedule, MeetingAssignment)
    constraint_verifier.verify_that(start_and_end_on_same_day).given(meeting_assignment, end_time_grain).penalizes(0)


def test_start_and_end_on_same_day_penalized():
    """Test that a penalty is applied when a meeting starts and ends on different days."""
    start_time_grain = TimeGrain(id="1", grain_index=0, day_of_year=0, starting_minute_of_day=480)
    end_time_grain = TimeGrain(id="2", grain_index=3, day_of_year=1, starting_minute_of_day=525)  # Different day
    
    meeting = Meeting(id="1", topic="Meeting 1", duration_in_grains=4)
    room = Room(id="1", name="Room 1", capacity=10)
    
    meeting_assignment = MeetingAssignment(id="0", meeting=meeting, starting_time_grain=start_time_grain, room=room)

    constraint_verifier = ConstraintVerifier.build(define_constraints, MeetingSchedule, MeetingAssignment)
    constraint_verifier.verify_that(start_and_end_on_same_day).given(meeting_assignment, end_time_grain).penalizes_by(1)


def test_avoid_overtime_penalized_expected():
    """Test that the expected penalty is applied for overtime when meeting duration exceeds available time grains."""
    assignment_time_grain = TimeGrain(id="1", grain_index=0, day_of_year=1, starting_minute_of_day=480)
    meeting = Meeting(id="1", topic="Meeting 1", duration_in_grains=4)
    room = Room(id="1", name="Room 1", capacity=10)
    
    meeting_assignment = MeetingAssignment(id="0", meeting=meeting, starting_time_grain=assignment_time_grain, room=room)

    constraint_verifier = ConstraintVerifier.build(define_constraints, MeetingSchedule, MeetingAssignment)
    constraint_verifier.verify_that(avoid_overtime).given(meeting_assignment).penalizes_by(3)


def test_required_attendance_conflict_penalized_expected():
    """Test that the expected penalty is applied for overlapping required attendances and room conflicts."""
    left_time_grain = TimeGrain(id="1", grain_index=0, day_of_year=1, starting_minute_of_day=480)
    right_time_grain = TimeGrain(id="2", grain_index=2, day_of_year=1, starting_minute_of_day=510)
    
    room = Room(id="1", name="Room 1", capacity=10)
    person = Person(id="1", full_name="Person 1")
    
    left_meeting = Meeting(id="1", topic="Meeting 1", duration_in_grains=4)
    required_attendance1 = RequiredAttendance(id="0", person=person, meeting_id=left_meeting.id)
    required_attendance1.meeting = left_meeting
    left_meeting.required_attendances = [required_attendance1]

    left_assignment = MeetingAssignment(id="0", meeting=left_meeting, starting_time_grain=left_time_grain, room=room)
    
    right_meeting = Meeting(id="2", topic="Meeting 2", duration_in_grains=4)
    required_attendance2 = RequiredAttendance(id="1", person=person, meeting_id=right_meeting.id)
    required_attendance2.meeting = right_meeting
    right_meeting.required_attendances = [required_attendance2]

    right_assignment = MeetingAssignment(id="1", meeting=right_meeting, starting_time_grain=right_time_grain, room=room)

    constraint_verifier = ConstraintVerifier.build(define_constraints, MeetingSchedule, MeetingAssignment)
    constraint_verifier.verify_that(room_conflict).given(left_assignment, right_assignment).penalizes_by(2)
    constraint_verifier.verify_that(required_attendance_conflict).given(required_attendance1, required_attendance2, left_assignment, right_assignment).penalizes_by(2)


def test_required_room_capacity_penalized_expected():
    """Test that the expected penalty is applied when room capacity is insufficient for all attendees."""
    time_grains = [
        TimeGrain(id="1", grain_index=0, day_of_year=1, starting_minute_of_day=480),
        TimeGrain(id="2", grain_index=1, day_of_year=1, starting_minute_of_day=495),
        TimeGrain(id="3", grain_index=2, day_of_year=1, starting_minute_of_day=510),
        TimeGrain(id="4", grain_index=3, day_of_year=1, starting_minute_of_day=525)
    ]

    room = Room(id="1", name="Room 1", capacity=1)
    
    person1 = Person(id="1", full_name="Person 1")
    required_attendance = RequiredAttendance(id="0", person=person1, meeting_id="1")
    meeting = Meeting(id="1", topic="Meeting 1", duration_in_grains=4)
    required_attendance.meeting = meeting
    meeting.required_attendances = [required_attendance]
    
    person2 = Person(id="2", full_name="Person 2")
    preferred_attendance = PreferredAttendance(id="1", person=person2, meeting_id="1")
    preferred_attendance.meeting = meeting
    meeting.preferred_attendances = [preferred_attendance]
    
    meeting_assignment = MeetingAssignment(id="0", meeting=meeting, starting_time_grain=time_grains[0], room=room)

    constraint_verifier = ConstraintVerifier.build(define_constraints, MeetingSchedule, MeetingAssignment)
    constraint_verifier.verify_that(required_room_capacity).given(meeting_assignment).penalizes_by(1)