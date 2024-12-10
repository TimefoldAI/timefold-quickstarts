from fastapi import FastAPI
from fastapi.staticfiles import StaticFiles
from uuid import uuid4

from .domain import EmployeeSchedule
from .demo_data import DemoData, generate_demo_data
from .solver import solver_manager, solution_manager
from .solver import solver_manager

app = FastAPI(docs_url='/q/swagger-ui')
data_sets: dict[str, EmployeeSchedule] = {}


@app.get("/demo-data")
async def demo_data_list() -> list[DemoData]:
    return [e for e in DemoData]


@app.get("/demo-data/{dataset_id}",  response_model_exclude_none=True)
async def get_demo_data(dataset_id: str) -> EmployeeSchedule:
    demo_data = getattr(DemoData, dataset_id)
    return generate_demo_data(demo_data)


@app.get("/schedules/{problem_id}",  response_model_exclude_none=True)
async def get_timetable(problem_id: str) -> EmployeeSchedule:
    schedule = data_sets[problem_id]
    return schedule.model_copy(update={
        'solver_status': solver_manager.get_solver_status(problem_id)
    })


def update_schedule(problem_id: str, schedule: EmployeeSchedule):
    global data_sets
    data_sets[problem_id] = schedule


@app.post("/schedules")
async def solve_timetable(schedule: EmployeeSchedule) -> str:
    job_id = str(uuid4())
    data_sets[job_id] = schedule
    solver_manager.solve_and_listen(job_id, schedule,
                                    lambda solution: update_schedule(job_id, solution))
    return job_id


@app.delete("/schedules/{problem_id}")
async def stop_solving(problem_id: str) -> None:
    solver_manager.terminate_early(problem_id)


app.mount("/", StaticFiles(directory="static", html=True), name="static")
