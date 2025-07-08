from fastapi import FastAPI, Depends, Request
from fastapi.staticfiles import StaticFiles
from typing import Annotated, Dict, List
from uuid import uuid4
from fastapi.encoders import jsonable_encoder
import json

from timefold.solver import SolverManager, SolutionManager

from .domain import *
from .demo_data import generate_demo_data
from .solver import solver_manager, solution_manager

app = FastAPI(docs_url='/q/swagger-ui')

# Dictionary to store submitted data sets
data_sets: Dict[str, MeetingSchedule] = {}


@app.get("/demo-data")
async def get_demo_data() -> MeetingSchedule:
    """Get the demo data set (always the same)."""
    return generate_demo_data()


@app.get("/schedules")
async def list_schedules() -> List[str]:
    """List all job IDs of submitted schedules."""
    return list(data_sets.keys())


@app.get("/schedules/{schedule_id}")
async def get_schedule(schedule_id: str) -> MeetingSchedule:
    """Get the solution and score for a given job ID."""
    if schedule_id not in data_sets:
        raise ValueError(f"No schedule found with ID {schedule_id}")
    
    schedule = data_sets[schedule_id]
    solver_status = solver_manager.get_solver_status(schedule_id)
    schedule.solver_status = solver_status

    return schedule


@app.get("/schedules/{problem_id}/status")
async def get_status(problem_id: str) -> Dict:
    """Get the schedule status and score for a given job ID."""
    if problem_id not in data_sets:
        raise ValueError(f"No schedule found with ID {problem_id}")
    
    schedule = data_sets[problem_id]
    solver_status = solver_manager.get_solver_status(problem_id)
    
    return {
        "score": {
            "hardScore": schedule.score.hard_score if schedule.score else 0,
            "mediumScore": schedule.score.medium_score if schedule.score else 0,
            "softScore": schedule.score.soft_score if schedule.score else 0
        },
        "solverStatus": solver_status.name
    }


@app.delete("/schedules/{problem_id}")
async def terminate_solving(problem_id: str) -> MeetingSchedule:
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


@app.post("/schedules")
async def solve_schedule(request: Request) -> str:
    json_data = await request.json()
    job_id = str(uuid4())
    
    # Create lookup dictionaries for validation context
    meetings = {m['id']: Meeting.model_validate(m) for m in json_data.get('meetings', [])}
    rooms = {r['id']: Room.model_validate(r) for r in json_data.get('rooms', [])}
    time_grains = {tg['id']: TimeGrain.model_validate(tg) for tg in json_data.get('timeGrains', [])}
    
    # Parse the incoming JSON with proper context for deserialization
    schedule = MeetingSchedule.model_validate(json_data, context={
        'meetings': meetings,
        'rooms': rooms,
        'timeGrains': time_grains
    })
    
    data_sets[job_id] = schedule
    solver_manager.solve_and_listen(
        job_id,
        schedule,
        lambda solution: update_schedule(job_id, solution)
    )
    return job_id


@app.put("/schedules/analyze")
async def analyze_schedule(request: Request) -> Dict:
    """Submit a schedule to analyze its score."""
    json_data = await request.json()
    
    # Create lookup dictionaries for validation context
    meetings = {m['id']: Meeting.model_validate(m) for m in json_data.get('meetings', [])}
    rooms = {r['id']: Room.model_validate(r) for r in json_data.get('rooms', [])}
    time_grains = {tg['id']: TimeGrain.model_validate(tg) for tg in json_data.get('timeGrains', [])}
    
    # Parse the incoming JSON with proper context
    schedule = MeetingSchedule.model_validate(json_data, context={
        'meetings': meetings,
        'rooms': rooms,
        'timeGrains': time_grains
    })
    
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