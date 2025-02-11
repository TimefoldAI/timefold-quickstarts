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


async def setup_context(request: Request) -> FlightCrewSchedule:
    json = await request.json()

    airports_dict = {
        airport['code']: Airport.model_validate(airport) for airport in json.get('airports', [])
    }

    # Preprocess flights to replace airport codes with Airport objects
    for flight in json.get('flights', []):
        if isinstance(flight.get('departureAirport'), str):
            flight['departureAirport'] = airports_dict.get(flight['departureAirport'], None)
        if isinstance(flight.get('arrivalAirport'), str):
            flight['arrivalAirport'] = airports_dict.get(flight['arrivalAirport'], None)

    # Preprocess employees to replace airport codes with Airport objects
    for employee in json.get('employees', []):
        if isinstance(employee.get('homeAirport'), str):
            airport_code = employee['homeAirport']
            employee['homeAirport'] = airports_dict.get(airport_code, None)

    return FlightCrewSchedule.model_validate(json,
                                         context={
                                             'airports': airports_dict,
                                             'employees': {
                                                 employee['id']: Employee.model_validate(employee) for
                                                 employee in json.get('employees', [])
                                             },
                                             'flights': {
                                                 flight['flightNumber']: Flight.model_validate(flight) for
                                                 flight in json.get('flights', [])
                                             },
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


def update_flight_crew_schedule(problem_id: str, flight_crew_schedule: FlightCrewSchedule):
    global data_sets
    data_sets[problem_id]["schedule"] = flight_crew_schedule


@app.post("/schedules")
async def solve_schedule(flight_crew_schedule: Annotated[FlightCrewSchedule, Depends(setup_context)]) -> str:
    job_id = str(uuid4())
    data_sets[job_id] = {
        "schedule": flight_crew_schedule,
        "created_at": datetime.now(),
        "exception": None,
    }
    solver_manager.solve_and_listen(job_id, flight_crew_schedule,
                                    lambda solution: update_flight_crew_schedule(job_id, solution))
    clean_jobs()
    return job_id


@app.get("/schedules/{problem_id}")
async def get_flight_crew_schedule(problem_id: str) -> FlightCrewSchedule:
    flight_crew_schedule = data_sets[problem_id]["schedule"]
    return flight_crew_schedule.model_copy(update={
        'solver_status': solver_manager.get_solver_status(problem_id)
    })


@app.get("/schedules/{job_id}/status")
async def get_schedule_status(job_id: str) -> dict:
    flight_crew_schedule = data_sets[job_id]["schedule"]
    return {"solver_status": flight_crew_schedule.solver_status}


@app.put("/schedules/analyze")
async def analyze_timetable(flight_crew_schedule: Annotated[FlightCrewSchedule, Depends(setup_context)]) -> dict:
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
    ) for constraint in solution_manager.analyze(flight_crew_schedule).constraint_analyses]}


@app.delete("/schedules/{problem_id}")
async def stop_solving(problem_id: str) -> None:
    solver_manager.terminate_early(problem_id)


app.mount("/", StaticFiles(directory="static", html=True), name="static")
