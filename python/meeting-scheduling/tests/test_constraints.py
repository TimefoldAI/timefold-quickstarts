import pytest
from timefold.solver.score import HardMediumSoftScore
from timefold.solver import SolverFactory
from timefold.solver.config import SolverConfig, ScoreDirectorFactoryConfig

from meeting_scheduling.domain import *
from meeting_scheduling.constraints import define_constraints
from meeting_scheduling.solver import solution_manager


def get_score_director():
    solver_config = SolverConfig(
        solution_class=MeetingSchedule,
        entity_class_list=[MeetingAssignment],
        score_director_factory_config=ScoreDirectorFactoryConfig(
            constraint_provider_function=define_constraints
        )
    )
    solver_factory = SolverFactory.create(solver_config)
    return solver_factory.build_score_director()


def test_room_conflict_unpenalized():
    room = Room(id="1", name="Room 1", capacity=10)

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
    meeting1 = Meeting(id="1", topic="Meeting 1", duration_in_grains=4)
    left_assignment = MeetingAssignment(id="0", meeting=meeting1, starting_time_grain=time_grains[0], room=room)

    meeting2 = Meeting(id="2", topic="Meeting 2", duration_in_grains=4)
    right_assignment = MeetingAssignment(id="1", meeting=meeting2, starting_time_grain=time_grains[4], room=room)

    schedule = MeetingSchedule(
        people=[],
        time_grains=time_grains,
        rooms=[room],
        meetings=[meeting1, meeting2],
        meeting_assignments=[left_assignment, right_assignment]
    )

    analysis = solution_manager.analyze(schedule)
    score = analysis.score
    for constraint in analysis.constraint_analyses:
        print(f"{constraint.constraint_name}: {constraint.score}")
        
    # Only room_conflict is tested, no overtime or other constraint should fire
    assert score.hard_score == 0


def test_room_conflict_penalized():
    room = Room(id="1", name="Room 1", capacity=10)

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
    meeting1 = Meeting(id="1", topic="Meeting 1", duration_in_grains=4)
    left_assignment = MeetingAssignment(id="0", meeting=meeting1, starting_time_grain=time_grains[0], room=room)

    meeting2 = Meeting(id="2", topic="Meeting 2", duration_in_grains=4)
    # Overlap meetings in the same room
    right_assignment = MeetingAssignment(id="1", meeting=meeting2, starting_time_grain=time_grains[2], room=room)

    schedule = MeetingSchedule(
        people=[],
        time_grains=time_grains,
        rooms=[room],
        meetings=[meeting1, meeting2],
        meeting_assignments=[left_assignment, right_assignment]
    )

    analysis = solution_manager.analyze(schedule)
    score = analysis.score
    for constraint in analysis.constraint_analyses:
        print(f"{constraint.constraint_name}: {constraint.score}")
    # Only room_conflict should fire, penalty -2 (overlap of 2 grains)
    assert score.hard_score == -2


def test_avoid_overtime_unpenalized():
    time_grain = TimeGrain(id="1", grain_index=3, day_of_year=1, starting_minute_of_day=525)
    
    assignment_time_grain = TimeGrain(id="2", grain_index=0, day_of_year=1, starting_minute_of_day=480)
    meeting = Meeting(id="1", topic="Meeting 1", duration_in_grains=4)
    room = Room(id="1", name="Room 1", capacity=10)
    
    meeting_assignment = MeetingAssignment(id="0", meeting=meeting, starting_time_grain=assignment_time_grain, room=room)

    # Create time grains for all needed indices
    time_grains = [
        assignment_time_grain,
        TimeGrain(id="3", grain_index=1, day_of_year=1, starting_minute_of_day=495),
        TimeGrain(id="4", grain_index=2, day_of_year=1, starting_minute_of_day=510),
        time_grain  # index 3
    ]

    schedule = MeetingSchedule(
        people=[],
        time_grains=time_grains,
        rooms=[room],
        meetings=[meeting],
        meeting_assignments=[meeting_assignment]
    )

    analysis = solution_manager.analyze(schedule)
    score = analysis.score
    for constraint in analysis.constraint_analyses:
        print(f"{constraint.constraint_name}: {constraint.score}")
    # Verify that avoid_overtime constraint is not triggered
    assert score.hard_score == 0


def test_avoid_overtime_penalized():
    # Only one meeting, not enough time grains for its duration
    time_grains = [
        TimeGrain(id="1", grain_index=0, day_of_year=1, starting_minute_of_day=480),
        TimeGrain(id="2", grain_index=1, day_of_year=1, starting_minute_of_day=495)
    ]
    meeting = Meeting(id="1", topic="Meeting 1", duration_in_grains=4)
    room = Room(id="1", name="Room 1", capacity=10)
    meeting_assignment = MeetingAssignment(id="0", meeting=meeting, starting_time_grain=time_grains[0], room=room)

    schedule = MeetingSchedule(
        people=[],
        time_grains=time_grains,
        rooms=[room],
        meetings=[meeting],
        meeting_assignments=[meeting_assignment]
    )

    analysis = solution_manager.analyze(schedule)
    score = analysis.score
    for constraint in analysis.constraint_analyses:
        print(f"{constraint.constraint_name}: {constraint.score}")
    # Only overtime should fire, penalty -3 (missing 3 grains)
    assert score.hard_score == -3


def test_required_attendance_conflict_unpenalized():
    person = Person(id="1", full_name="Person 1")
    
    left_meeting = Meeting(id="1", topic="Meeting 1", duration_in_grains=2)
    required_attendance1 = RequiredAttendance(id="0", person=person, meeting_id=left_meeting.id)
    required_attendance1.meeting = left_meeting
    left_meeting.required_attendances = [required_attendance1]
    
    right_meeting = Meeting(id="2", topic="Meeting 2", duration_in_grains=2)
    required_attendance2 = RequiredAttendance(id="1", person=person, meeting_id=right_meeting.id)
    required_attendance2.meeting = right_meeting
    right_meeting.required_attendances = [required_attendance2]
    
    time_grains = [
        TimeGrain(id="1", grain_index=0, day_of_year=1, starting_minute_of_day=480),
        TimeGrain(id="2", grain_index=1, day_of_year=1, starting_minute_of_day=495),
        TimeGrain(id="3", grain_index=2, day_of_year=1, starting_minute_of_day=510),
        TimeGrain(id="4", grain_index=3, day_of_year=1, starting_minute_of_day=525)
    ]
    room = Room(id="1", name="Room 1", capacity=10)
    left_assignment = MeetingAssignment(id="0", meeting=left_meeting, starting_time_grain=time_grains[0], room=room)
    right_assignment = MeetingAssignment(id="1", meeting=right_meeting, starting_time_grain=time_grains[2], room=room)

    schedule = MeetingSchedule(
        people=[person],
        time_grains=time_grains,
        rooms=[room],
        meetings=[left_meeting, right_meeting],
        meeting_assignments=[left_assignment, right_assignment],
        required_attendances=[required_attendance1, required_attendance2]
    )

    analysis = solution_manager.analyze(schedule)
    score = analysis.score
    for constraint in analysis.constraint_analyses:
        print(f"{constraint.constraint_name}: {constraint.score}")
    # Only required_attendance_conflict is tested, no conflict should fire
    assert score.hard_score == 0


def test_required_attendance_conflict_penalized():
    person = Person(id="1", full_name="Person 1")
    
    left_meeting = Meeting(id="1", topic="Meeting 1", duration_in_grains=2)
    required_attendance1 = RequiredAttendance(id="0", person=person, meeting_id=left_meeting.id)
    required_attendance1.meeting = left_meeting
    left_meeting.required_attendances = [required_attendance1]
    
    right_meeting = Meeting(id="2", topic="Meeting 2", duration_in_grains=2)
    required_attendance2 = RequiredAttendance(id="1", person=person, meeting_id=right_meeting.id)
    required_attendance2.meeting = right_meeting
    right_meeting.required_attendances = [required_attendance2]
    
    time_grains = [
        TimeGrain(id="1", grain_index=0, day_of_year=1, starting_minute_of_day=480),
        TimeGrain(id="2", grain_index=1, day_of_year=1, starting_minute_of_day=495),
        TimeGrain(id="3", grain_index=2, day_of_year=1, starting_minute_of_day=510)
    ]
    room = Room(id="1", name="Room 1", capacity=10)
    left_assignment = MeetingAssignment(id="0", meeting=left_meeting, starting_time_grain=time_grains[0], room=room)
    right_assignment = MeetingAssignment(id="1", meeting=right_meeting, starting_time_grain=time_grains[1], room=room)

    schedule = MeetingSchedule(
        people=[person],
        time_grains=time_grains,
        rooms=[room],
        meetings=[left_meeting, right_meeting],
        meeting_assignments=[left_assignment, right_assignment],
        required_attendances=[required_attendance1, required_attendance2]
    )

    analysis = solution_manager.analyze(schedule)
    score = analysis.score
    for constraint in analysis.constraint_analyses:
        print(f"{constraint.constraint_name}: {constraint.score}")
    # Only required_attendance_conflict should fire, penalty -2 (overlap of 2 grains)
    assert score.hard_score == -2


def test_required_room_capacity_unpenalized():
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
    
    time_grains = [
        TimeGrain(id="1", grain_index=0, day_of_year=1, starting_minute_of_day=480),
        TimeGrain(id="2", grain_index=1, day_of_year=1, starting_minute_of_day=495)
    ]
    
    meeting_assignment = MeetingAssignment(id="0", meeting=meeting, starting_time_grain=time_grains[0], room=room)

    schedule = MeetingSchedule(
        people=[person1, person2],
        time_grains=time_grains,
        rooms=[room],
        meetings=[meeting],
        meeting_assignments=[meeting_assignment],
        required_attendances=[required_attendance],
        preferred_attendances=[preferred_attendance]
    )

    analysis = solution_manager.analyze(schedule)
    score = analysis.score
    for constraint in analysis.constraint_analyses:
        print(f"{constraint.constraint_name}: {constraint.score}")
    # Only required_room_capacity is tested, no constraint should fire
    assert score.hard_score == 0


def test_required_room_capacity_penalized():
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
    
    time_grains = [
        TimeGrain(id="1", grain_index=0, day_of_year=1, starting_minute_of_day=480),
        TimeGrain(id="2", grain_index=1, day_of_year=1, starting_minute_of_day=495)
    ]
    
    meeting_assignment = MeetingAssignment(id="0", meeting=meeting, starting_time_grain=time_grains[0], room=room)

    schedule = MeetingSchedule(
        people=[person1, person2],
        time_grains=time_grains,
        rooms=[room],
        meetings=[meeting],
        meeting_assignments=[meeting_assignment],
        required_attendances=[required_attendance],
        preferred_attendances=[preferred_attendance]
    )

    analysis = solution_manager.analyze(schedule)
    score = analysis.score
    for constraint in analysis.constraint_analyses:
        print(f"{constraint.constraint_name}: {constraint.score}")
    # Only required_room_capacity should fire, penalty -1 (1 person over capacity)
    assert score.hard_score == -1


def test_start_and_end_on_same_day_unpenalized():
    start_time_grain = TimeGrain(id="1", grain_index=0, day_of_year=0, starting_minute_of_day=480)
    end_time_grain = TimeGrain(id="2", grain_index=3, day_of_year=0, starting_minute_of_day=525)
    
    meeting = Meeting(id="1", topic="Meeting 1", duration_in_grains=4)
    room = Room(id="1", name="Room 1", capacity=10)
    
    meeting_assignment = MeetingAssignment(id="0", meeting=meeting, starting_time_grain=start_time_grain, room=room)

    # Create time grains for all needed indices
    time_grains = [
        start_time_grain,
        TimeGrain(id="3", grain_index=1, day_of_year=0, starting_minute_of_day=495),
        TimeGrain(id="4", grain_index=2, day_of_year=0, starting_minute_of_day=510),
        end_time_grain  # index 3
    ]

    schedule = MeetingSchedule(
        people=[],
        time_grains=time_grains,
        rooms=[room],
        meetings=[meeting],
        meeting_assignments=[meeting_assignment]
    )

    analysis = solution_manager.analyze(schedule)
    score = analysis.score
    for constraint in analysis.constraint_analyses:
        print(f"{constraint.constraint_name}: {constraint.score}")
    # Verify that start_and_end_on_same_day constraint is not triggered
    assert score.hard_score == 0


def test_start_and_end_on_same_day_penalized():
    start_time_grain = TimeGrain(id="1", grain_index=0, day_of_year=0, starting_minute_of_day=480)
    end_time_grain = TimeGrain(id="2", grain_index=3, day_of_year=1, starting_minute_of_day=525)  # Different day
    
    meeting = Meeting(id="1", topic="Meeting 1", duration_in_grains=4)
    room = Room(id="1", name="Room 1", capacity=10)
    
    meeting_assignment = MeetingAssignment(id="0", meeting=meeting, starting_time_grain=start_time_grain, room=room)

    # Create time grains for all needed indices
    time_grains = [
        start_time_grain,
        TimeGrain(id="3", grain_index=1, day_of_year=0, starting_minute_of_day=495),
        TimeGrain(id="4", grain_index=2, day_of_year=0, starting_minute_of_day=510),
        end_time_grain  # index 3, different day
    ]

    schedule = MeetingSchedule(
        people=[],
        time_grains=time_grains,
        rooms=[room],
        meetings=[meeting],
        meeting_assignments=[meeting_assignment]
    )

    analysis = solution_manager.analyze(schedule)
    score = analysis.score
    for constraint in analysis.constraint_analyses:
        print(f"{constraint.constraint_name}: {constraint.score}")
    # Verify that start_and_end_on_same_day constraint is triggered
    assert score.hard_score == -1


def test_avoid_overtime_penalized_expected():
    assignment_time_grain = TimeGrain(id="1", grain_index=0, day_of_year=1, starting_minute_of_day=480)
    meeting = Meeting(id="1", topic="Meeting 1", duration_in_grains=4)
    room = Room(id="1", name="Room 1", capacity=10)
    
    meeting_assignment = MeetingAssignment(id="0", meeting=meeting, starting_time_grain=assignment_time_grain, room=room)

    schedule = MeetingSchedule(
        people=[],
        time_grains=[assignment_time_grain],  # Missing time grain for the end of the meeting
        rooms=[room],
        meetings=[meeting],
        meeting_assignments=[meeting_assignment]
    )

    analysis = solution_manager.analyze(schedule)
    score = analysis.score
    for constraint in analysis.constraint_analyses:
        print(f"{constraint.constraint_name}: {constraint.score}")
    # Only overtime should fire, penalty -3 (missing 3 grains)
    assert score.hard_score == -3


def test_required_attendance_conflict_penalized_expected():
    person = Person(id="1", full_name="Person 1")
    
    left_meeting = Meeting(id="1", topic="Meeting 1", duration_in_grains=4)
    required_attendance1 = RequiredAttendance(id="0", person=person, meeting_id=left_meeting.id)
    required_attendance1.meeting = left_meeting
    left_meeting.required_attendances = [required_attendance1]
    
    left_time_grain = TimeGrain(id="1", grain_index=0, day_of_year=1, starting_minute_of_day=480)
    right_time_grain = TimeGrain(id="2", grain_index=2, day_of_year=1, starting_minute_of_day=510)
    # Add all intermediate time grains
    time_grains = [
        left_time_grain,
        TimeGrain(id="3", grain_index=1, day_of_year=1, starting_minute_of_day=495),
        right_time_grain,
        TimeGrain(id="4", grain_index=3, day_of_year=1, starting_minute_of_day=525),
        TimeGrain(id="5", grain_index=4, day_of_year=1, starting_minute_of_day=540),
        TimeGrain(id="6", grain_index=5, day_of_year=1, starting_minute_of_day=555)
    ]
    room = Room(id="1", name="Room 1", capacity=10)
    left_assignment = MeetingAssignment(id="0", meeting=left_meeting, starting_time_grain=left_time_grain, room=room)
    
    right_meeting = Meeting(id="2", topic="Meeting 2", duration_in_grains=4)
    required_attendance2 = RequiredAttendance(id="1", person=person, meeting_id=right_meeting.id)
    required_attendance2.meeting = right_meeting
    right_meeting.required_attendances = [required_attendance2]
    right_assignment = MeetingAssignment(id="1", meeting=right_meeting, starting_time_grain=right_time_grain, room=room)

    schedule = MeetingSchedule(
        people=[person],
        time_grains=time_grains,
        rooms=[room],
        meetings=[left_meeting, right_meeting],
        meeting_assignments=[left_assignment, right_assignment],
        required_attendances=[required_attendance1, required_attendance2]
    )

    analysis = solution_manager.analyze(schedule)
    score = analysis.score
    for constraint in analysis.constraint_analyses:
        print(f"{constraint.constraint_name}: {constraint.score}")
    # Only room_conflict (-2) and required_attendance_conflict (-2) should fire, total -4
    assert score.hard_score == -4


def test_required_room_capacity_penalized_expected():
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
    
    # Provide enough time grains to cover the meeting without overtime
    time_grains = [
        TimeGrain(id="1", grain_index=0, day_of_year=1, starting_minute_of_day=480),
        TimeGrain(id="2", grain_index=1, day_of_year=1, starting_minute_of_day=495),
        TimeGrain(id="3", grain_index=2, day_of_year=1, starting_minute_of_day=510),
        TimeGrain(id="4", grain_index=3, day_of_year=1, starting_minute_of_day=525)
    ]
    
    meeting_assignment = MeetingAssignment(id="0", meeting=meeting, starting_time_grain=time_grains[0], room=room)

    schedule = MeetingSchedule(
        people=[person1, person2],
        time_grains=time_grains,
        rooms=[room],
        meetings=[meeting],
        meeting_assignments=[meeting_assignment],
        required_attendances=[required_attendance],
        preferred_attendances=[preferred_attendance]
    )

    analysis = solution_manager.analyze(schedule)
    score = analysis.score
    for constraint in analysis.constraint_analyses:
        print(f"{constraint.constraint_name}: {constraint.score}")
    # Only required_room_capacity should fire, penalty -1 (1 person over capacity)
    assert score.hard_score == -1