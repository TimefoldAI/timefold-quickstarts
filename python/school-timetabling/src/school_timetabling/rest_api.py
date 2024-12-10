from fastapi import FastAPI, Depends, Request
from fastapi.staticfiles import StaticFiles
from typing import Annotated
from uuid import uuid4

from .domain import *
from .score_analysis import *
from .demo_data import DemoData, generate_demo_data
from .solver import solver_manager, solution_manager

app = FastAPI(docs_url='/q/swagger-ui')
data_sets = {}


@app.get("/demo-data")
async def demo_data_list():
    return [e.name for e in DemoData]


@app.get("/demo-data/{dataset_id}")
async def demo_data_list(dataset_id: str):
    return generate_demo_data(getattr(DemoData, dataset_id))


@app.get("/timetables/{problem_id}")
async def get_timetable(problem_id: str) -> Timetable:
    timetable = data_sets[problem_id]
    return timetable.model_copy(update={
        'solver_status': solver_manager.get_solver_status(problem_id)
    })


@app.delete("/timetables/{problem_id}")
async def stop_solving(problem_id: str) -> None:
    solver_manager.terminate_early(problem_id)


def update_timetable(problem_id: str, timetable: Timetable):
    global data_sets
    data_sets[problem_id] = timetable


@app.post("/timetables")
async def solve_timetable(timetable: Timetable) -> str:
    job_id = str(uuid4())
    data_sets[job_id] = timetable
    solver_manager.solve_and_listen(job_id, timetable,
                                    lambda solution: update_timetable(job_id, solution))
    return job_id


async def setup_context(request: Request) -> Timetable:
    json = await request.json()
    return Timetable.model_validate(json,
                                    context={
                                        'rooms': {
                                            room['id']: Room.model_validate(room) for room in json.get('rooms', [])
                                        },
                                        'timeslots': {
                                            timeslot['id']: Timeslot.model_validate(timeslot) for
                                            timeslot in json.get('timeslots', [])
                                        },
                                    })


@app.put("/timetables/analyze")
async def analyze_timetable(timetable: Annotated[Timetable, Depends(setup_context)]) -> dict:
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
    ) for constraint in solution_manager.analyze(timetable).constraint_analyses]}


app.mount("/", StaticFiles(directory="static", html=True), name="static")
