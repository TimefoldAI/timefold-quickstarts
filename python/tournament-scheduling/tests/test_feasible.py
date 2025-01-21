from tournament_scheduling.rest_api import app
from tournament_scheduling.domain import *

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
        tournament_schedule_response = client.get(f"/schedules/{job_id}")
        assert tournament_schedule_response.status_code == 200
        tournament_schedule_json = tournament_schedule_response.json()
        tournament_schedule = TournamentSchedule.model_validate(tournament_schedule_json,
                                                                context={
                                                                    'teams': {
                                                                        team['id']: Team.model_validate(team) for
                                                                        team in tournament_schedule_json.get('teams', [])
                                                                    },
                                                                    'days': {
                                                                        day['dateIndex']: Day.model_validate(day) for
                                                                        day in tournament_schedule_json.get('days', [])
                                                                    }
                                                                })
        if tournament_schedule.score is not None and tournament_schedule.score.is_feasible:
            stop_solving_response = client.delete(f"/schedules/{job_id}")
            assert stop_solving_response.status_code == 200
            return

    client.delete(f"/schedules/{job_id}")
    fail('solution is not feasible')

    # Analyze the Timetable
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
