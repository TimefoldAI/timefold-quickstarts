package org.acme.flightcrewscheduling.solver;

import java.util.List;

import org.acme.flightcrewscheduling.dto.AirportDTO;
import org.acme.flightcrewscheduling.dto.EmployeeDTO;
import org.acme.flightcrewscheduling.dto.FlightAssignmentDTO;
import org.acme.flightcrewscheduling.dto.FlightCrewScheduleInput;
import org.acme.flightcrewscheduling.dto.FlightDTO;

final class SolverTestDataFactory {

    private static final String PILOT = "Pilot";
    private static final String ATTENDANT = "Flight attendant";
    private static final String HOME = "H";
    private static final String AWAY = "A";

    private SolverTestDataFactory() {
    }

    static FlightCrewScheduleInput createProblem() {
        List<AirportDTO> airports = List.of(
                new AirportDTO(HOME, "Home"),
                new AirportDTO(AWAY, "Away"));

        List<EmployeeDTO> employees = List.of(
                new EmployeeDTO("p1", "Pilot One", HOME, List.of(PILOT), List.of()),
                new EmployeeDTO("p2", "Pilot Two", HOME, List.of(PILOT), List.of()),
                new EmployeeDTO("a1", "Attendant One", HOME, List.of(ATTENDANT), List.of()),
                new EmployeeDTO("a2", "Attendant Two", HOME, List.of(ATTENDANT), List.of()));

        List<FlightDTO> flights = List.of(
                new FlightDTO("F1", HOME, "2024-01-01T08:00", AWAY, "2024-01-01T10:00"),
                new FlightDTO("F2", AWAY, "2024-01-01T12:00", HOME, "2024-01-01T14:00"));

        List<FlightAssignmentDTO> assignments = List.of(
                new FlightAssignmentDTO("1", "F1", 1, PILOT, ""),
                new FlightAssignmentDTO("2", "F1", 2, PILOT, ""),
                new FlightAssignmentDTO("3", "F1", 3, ATTENDANT, ""),
                new FlightAssignmentDTO("4", "F1", 4, ATTENDANT, ""),
                new FlightAssignmentDTO("5", "F2", 1, PILOT, ""),
                new FlightAssignmentDTO("6", "F2", 2, PILOT, ""),
                new FlightAssignmentDTO("7", "F2", 3, ATTENDANT, ""),
                new FlightAssignmentDTO("8", "F2", 4, ATTENDANT, ""));

        return new FlightCrewScheduleInput(airports, employees, flights, assignments);
    }
}
