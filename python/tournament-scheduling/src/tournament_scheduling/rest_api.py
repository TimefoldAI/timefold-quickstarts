from fastapi import FastAPI, Depends, Request
from fastapi.staticfiles import StaticFiles
from typing import Annotated, Final
from uuid import uuid4
from datetime import datetime

from .domain import *
from .score_analysis import *
from .demo_data import generate_demo_data
from .solver import solver_manager, solution_manager


app = FastAPI(docs_url='/q/swagger-ui')
MAX_JOBS_CACHE_SIZE: Final[int] = 2
data_sets: Dict[str, dict] = {}


@app.get("/demo-data")
async def get_demo_data():
    return generate_demo_data()


async def setup_context(request: Request) -> TournamentSchedule:
    json = await request.json()
    return TournamentSchedule.model_validate(json,
                                         context={
                                             'teams': {
                                                 team['id']: Team.model_validate(team) for
                                                 team in json.get('teams', [])
                                             },
                                             'days': {
                                                 day['dateIndex']: Day.model_validate(day) for
                                                 day in json.get('days', [])
                                             }
                                         })


def clean_jobs():
    """
    The method retains only the records of the last MAX_JOBS_CACHE_SIZE completed jobs by removing the oldest ones.
    """
    global data_sets
    if len(data_sets) <= MAX_JOBS_CACHE_SIZE:
        return

    completed_jobs = [
        (job_id, job_data)
        for job_id, job_data in data_sets.items()
        if job_data["schedule"] is not None
    ]

    completed_jobs.sort(key=lambda job: job[1]["created_at"])

    for job_id, _ in completed_jobs[:len(completed_jobs) - MAX_JOBS_CACHE_SIZE]:
        del data_sets[job_id]


def update_tournament_schedule(job_id: str, tournament_schedule: TournamentSchedule):
    global data_sets
    data_sets[job_id]["schedule"] = tournament_schedule


@app.post("/schedules")
async def solve_schedule(tournament_schedule: Annotated[TournamentSchedule, Depends(setup_context)]) -> str:
    job_id = str(uuid4())
    data_sets[job_id] = {
        "schedule": tournament_schedule,
        "created_at": datetime.now(),
        "exception": None,
    }
    solver_manager.solve_and_listen(job_id, tournament_schedule,
                                    lambda solution: update_tournament_schedule(job_id, solution))
    clean_jobs()
    return job_id


@app.get("/schedules/{job_id}")
async def get_tournament_schedule(job_id: str) -> TournamentSchedule:
    tournament_schedule = data_sets[job_id]["schedule"]
    return tournament_schedule.model_copy(update={
        'solver_status': solver_manager.get_solver_status(job_id)
    })


@app.get("/schedules/{job_id}/status")
async def get_tournament_schedule_status(job_id: str) -> dict:
    tournament_schedule = data_sets[job_id]["schedule"]
    return {"solver_status": tournament_schedule.solver_status}


@app.put("/schedules/analyze")
async def analyze_timetable(tournament_schedule: Annotated[TournamentSchedule, Depends(setup_context)]) -> dict:
    return {'constraints': [ConstraintAnalysisDTO(
        name=constraint.constraint_name,
        weight=constraint.weight,
        score=constraint.score,
        matches=[
            MatchAnalysisDTO(
                name=match.constraint_ref.constraint_name,
                score=match.score,
                justification=match.justification
            )
            for match in constraint.matches
        ]
    ) for constraint in solution_manager.analyze(tournament_schedule).constraint_analyses]}


@app.delete("/schedules/{job_id}")
async def stop_solving(job_id: str) -> None:
    solver_manager.terminate_early(job_id)


app.mount("/", StaticFiles(directory="static", html=True), name="static")
