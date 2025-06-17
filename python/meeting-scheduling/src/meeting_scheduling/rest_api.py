from fastapi import FastAPI, Depends, Request
from fastapi.staticfiles import StaticFiles
from typing import Annotated, Dict, List
from uuid import uuid4
from fastapi.encoders import jsonable_encoder
import json

from .domain import *
from .demo_data import DemoData, generate_demo_data
from .solver import solver_manager, solution_manager

app = FastAPI(docs_url='/q/swagger-ui')
data_sets = {}


@app.get("/demo-data")
async def get_demo_data() -> MeetingScheduleModel:
    """Get the demo data set (always the same)."""
    schedule = generate_demo_data()

    # Convert dataclass to Pydantic model for camelCase serialization
    model = MeetingScheduleModel.model_validate(schedule)

    # Fix meetingAssignments to use only IDs for meeting, room, and startingTimeGrain
    for ma in model.meeting_assignments:
        if hasattr(ma, 'meeting') and hasattr(ma.meeting, 'id'):
            ma.meeting = ma.meeting.id
        if hasattr(ma, 'room') and ma.room is not None and hasattr(ma.room, 'id'):
            ma.room = ma.room.id
        elif hasattr(ma, 'room'):
            ma.room = None
        if hasattr(ma, 'starting_time_grain') and ma.starting_time_grain is not None and hasattr(ma.starting_time_grain, 'id'):
            ma.starting_time_grain = ma.starting_time_grain.id
        elif hasattr(ma, 'starting_time_grain'):
            ma.starting_time_grain = None
    model_dict = model.model_dump(by_alias=True)
    return model_dict


@app.get("/schedules")
async def list_schedules() -> List[str]:
    """List all job IDs of submitted schedules."""
    return list(data_sets.keys())


@app.get("/schedules/{schedule_id}")
async def get_schedule(schedule_id: str) -> MeetingScheduleModel:
    """Get the solution and score for a given job ID."""
    if schedule_id not in data_sets:
        raise ValueError(f"No schedule found with ID {schedule_id}")
    
    schedule = data_sets[schedule_id]
    solver_status = solver_manager.get_solver_status(schedule_id)
    schedule.solver_status = solver_status

    # Convert score to string for Pydantic compatibility
    if hasattr(schedule, "score") and schedule.score is not None:
        schedule.score = str(schedule.score)

    # Rebuild meetings with correct attendances
    new_meetings = []
    for m in schedule.meetings:
        new_meetings.append(
            type(m)(
                id=m.id,
                topic=m.topic,
                duration_in_grains=m.duration_in_grains,
                speakers=m.speakers,
                content=m.content,
                entire_group_meeting=m.entire_group_meeting,
                required_attendances=[a for a in schedule.required_attendances if str(a.meeting_id) == str(m.id)],
                preferred_attendances=[a for a in schedule.preferred_attendances if str(a.meeting_id) == str(m.id)],
            )
        )
    schedule.meetings = new_meetings
    # Convert dataclass to Pydantic model for camelCase serialization
    model = MeetingScheduleModel.model_validate(schedule)
    # Fix meetingAssignments to use only IDs for meeting, room, and startingTimeGrain
    for ma in model.meeting_assignments:
        if hasattr(ma, 'meeting') and hasattr(ma.meeting, 'id'):
            ma.meeting = ma.meeting.id

        if hasattr(ma, 'room') and ma.room is not None and hasattr(ma.room, 'id'):
            ma.room = ma.room.id

        elif hasattr(ma, 'room'):
            ma.room = None

        if hasattr(ma, 'starting_time_grain') and ma.starting_time_grain is not None and hasattr(ma.starting_time_grain, 'id'):
            ma.starting_time_grain = ma.starting_time_grain.id

        elif hasattr(ma, 'starting_time_grain'):
            ma.starting_time_grain = None

    model_dict = model.model_dump(by_alias=True)

    return model_dict


@app.get("/schedules/{problem_id}/status")
async def get_status(problem_id: str) -> Dict:
    """Get the schedule status and score for a given job ID."""
    if problem_id not in data_sets:
        raise ValueError(f"No schedule found with ID {problem_id}")
    
    schedule = data_sets[problem_id]
    solver_status = solver_manager.get_solver_status(problem_id)
    
    return {
        "score": {
            "hardScore": schedule.score.hard_score() if schedule.score else 0,
            "mediumScore": schedule.score.medium_score() if schedule.score else 0,
            "softScore": schedule.score.soft_score() if schedule.score else 0
        },
        "solverStatus": solver_status.name
    }


@app.delete("/schedules/{problem_id}")
async def terminate_solving(problem_id: str) -> MeetingScheduleModel:
    """Terminate solving for a given job ID."""
    if problem_id not in data_sets:
        raise ValueError(f"No schedule found with ID {problem_id}")
    
    try:
        solver_manager.terminate_early(problem_id)

    except Exception as e:
        print(f"Warning: terminate_early failed for {problem_id}: {e}")

    return await get_schedule(problem_id)


def update_schedule(problem_id: str, schedule: MeetingSchedule) -> None:
    """Update the schedule in the data sets."""
    global data_sets
    data_sets[problem_id] = schedule


def pydantic_to_dataclass_schedule(model: MeetingScheduleModel) -> MeetingSchedule:
    # Convert Pydantic MeetingScheduleModel to dataclass MeetingSchedule
    people = [Person(**person.model_dump()) for person in model.people]
    time_grains = [TimeGrain(**tg.model_dump()) for tg in model.time_grains]
    rooms = [Room(**room.model_dump()) for room in model.rooms]
    meetings = []
    meeting_map = {}

    for m in model.meetings:
        meeting = Meeting(
            id=m.id,
            topic=m.topic,
            duration_in_grains=m.duration_in_grains,
            speakers=[Person(**p.model_dump()) for p in m.speakers] if m.speakers else None,
            content=m.content,
            entire_group_meeting=m.entire_group_meeting,
        )
        meetings.append(meeting)
        meeting_map[m.id] = meeting
    
    room_map = {r.id: r for r in rooms}
    time_grain_map = {tg.id: tg for tg in time_grains}
    
    required_attendances = [RequiredAttendance(
        id=ra.id,
        person=Person(**ra.person.model_dump()),
        meeting_id=ra.meeting_id
    ) for ra in model.required_attendances]

    preferred_attendances = [PreferredAttendance(
        id=pa.id,
        person=Person(**pa.person.model_dump()),
        meeting_id=pa.meeting_id
    ) for pa in model.preferred_attendances]

    meeting_assignments = []

    for ma in model.meeting_assignments:
        # Accept both IDs and objects for meeting, room, and starting_time_grain
        meeting = meeting_map[ma.meeting] if isinstance(ma.meeting, str) else meeting_map[ma.meeting.id]
        room = None
        if ma.room is not None:
            if isinstance(ma.room, str):
                room = room_map.get(ma.room)

            else:
                room = Room(**ma.room.model_dump())

        starting_time_grain = None

        if ma.starting_time_grain is not None:
            if isinstance(ma.starting_time_grain, str):
                starting_time_grain = time_grain_map.get(ma.starting_time_grain)

            else:
                starting_time_grain = TimeGrain(**ma.starting_time_grain.model_dump())

        meeting_assignments.append(MeetingAssignment(
            id=ma.id,
            meeting=meeting,
            pinned=ma.pinned,
            starting_time_grain=starting_time_grain,
            room=room
        ))

    return MeetingSchedule(
        people=people,
        time_grains=time_grains,
        rooms=rooms,
        meetings=meetings,
        required_attendances=required_attendances,
        preferred_attendances=preferred_attendances,
        meeting_assignments=meeting_assignments,
        score=None,
        solver_status=None
    )


async def setup_context(request: Request) -> MeetingSchedule:
    json_data = await request.json()
    model = MeetingScheduleModel.model_validate(json_data)

    # Fix meetingAssignments to use only IDs for meeting, room, and starting_time_grain
    for ma in model.meeting_assignments:
        if hasattr(ma, 'meeting') and hasattr(ma.meeting, 'id'):
            ma.meeting = ma.meeting.id

        if hasattr(ma, 'room') and ma.room is not None and hasattr(ma.room, 'id'):
            ma.room = ma.room.id

        elif hasattr(ma, 'room'):
            ma.room = None

        if hasattr(ma, 'starting_time_grain') and ma.starting_time_grain is not None and hasattr(ma.starting_time_grain, 'id'):
            ma.starting_time_grain = ma.starting_time_grain.id
        
        elif hasattr(ma, 'starting_time_grain'):
            ma.starting_time_grain = None

    return pydantic_to_dataclass_schedule(model)


@app.post("/schedules")
async def solve_schedule(schedule: Annotated[MeetingSchedule, Depends(setup_context)]) -> str:
    job_id = str(uuid4())
    data_sets[job_id] = schedule
    solver_manager.solve_and_listen(
        job_id,
        schedule,
        lambda solution: update_schedule(job_id, solution)
    )
    return job_id


@app.put("/schedules/analyze")
async def analyze_schedule(schedule: Annotated[MeetingSchedule, Depends(setup_context)]) -> Dict:
    """Submit a schedule to analyze its score."""
    analysis = solution_manager.analyze(schedule)
    
    return {
        "constraints": [
            {
                "name": constraint.constraint_name,
                "weight": constraint.weight,
                "score": constraint.score,
                "matches": [
                    {
                        "name": match.constraint_ref.constraint_name,
                        "score": match.score,
                        "justification": match.justification
                    }
                    for match in constraint.matches
                ]
            }
            for constraint in analysis.constraint_analyses
        ]
    }


# Mount static files
app.mount("/", StaticFiles(directory="static", html=True), name="static")