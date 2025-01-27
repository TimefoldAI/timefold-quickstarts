from flight_crew_scheduling.rest_api import app
from flight_crew_scheduling.domain import *

from fastapi.testclient import TestClient
from time import sleep
from pytest import fail


client = TestClient(app)


def test_feasible():
    # Get Demo Data
    demo_data_response = client.get("/demo-data")
    assert demo_data_response.status_code == 200
    demo_data = demo_data_response.json()
    assert demo_data is not None

    # Post Schedule
    job_id_response = client.post("/schedules", json=demo_data_response.json())
    assert job_id_response.status_code == 200
    job_id = job_id_response.text[1:-1]

    # Test Feasibility
    ATTEMPTS: int = 1_000
    for _ in range(ATTEMPTS):
        sleep(0.1)
        flight_crew_schedule_response = client.get(f"/schedules/{job_id}")
        assert flight_crew_schedule_response.status_code == 200
        flight_crew_schedule_json = flight_crew_schedule_response.json()

        airports_dict = {
            airport['code']: Airport.model_validate(airport) for airport in flight_crew_schedule_json.get('airports', [])
        }

        # Preprocess flights to replace airport codes with Airport objects
        for flight in flight_crew_schedule_json.get('flights', []):
            if isinstance(flight.get('departureAirport'), str):
                flight['departureAirport'] = airports_dict.get(flight['departureAirport'], None)
            if isinstance(flight.get('arrivalAirport'), str):
                flight['arrivalAirport'] = airports_dict.get(flight['arrivalAirport'], None)

        # Preprocess employees to replace airport codes with Airport objects
        for employee in flight_crew_schedule_json.get('employees', []):
            if isinstance(employee.get('homeAirport'), str):
                airport_code = employee['homeAirport']
                employee['homeAirport'] = airports_dict.get(airport_code, None)

        flight_crew_schedule = FlightCrewSchedule.model_validate(flight_crew_schedule_json,
                                                                 context={
                                                                     'airports': airports_dict,
                                                                     'employees': {
                                                                         employee['id']: Employee.model_validate(
                                                                             employee) for
                                                                         employee in flight_crew_schedule_json.get('employees', [])
                                                                     },
                                                                     'flights': {
                                                                         flight['flightNumber']: Flight.model_validate(
                                                                             flight) for
                                                                         flight in flight_crew_schedule_json.get('flights', [])
                                                                     },
                                                                 })

        if flight_crew_schedule.score is not None and flight_crew_schedule.score.is_feasible:
            stop_solving_response = client.delete(f"/schedules/{job_id}")
            assert stop_solving_response.status_code == 200
            return

    client.delete(f"/schedules/{job_id}")
    fail('solution is not feasible')

    # Analyze the Schedule
    analyze_response = client.put("/schedules/analyze", json=demo_data)
    assert analyze_response.status_code == 200
    analyze_data = analyze_response.json()
    assert "constraints" in analyze_data

    # Check Solver Status
    status_response = client.get(f"/schedules/{job_id}/status")
    assert status_response.status_code == 200
    status_data = status_response.json()
    assert "solver_status" in status_data
    assert status_data["solver_status"] == "NOT_SOLVING"

    # Stop Solving
    stop_solving_response = client.delete(f"/schedules/{job_id}")
    assert stop_solving_response.status_code == 200

    # Verify Cleanup
    get_after_delete_response = client.get(f"/schedules/{job_id}")
    assert get_after_delete_response.status_code == 404
