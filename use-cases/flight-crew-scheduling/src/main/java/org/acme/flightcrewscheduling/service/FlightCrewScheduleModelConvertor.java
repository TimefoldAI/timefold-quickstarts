package org.acme.flightcrewscheduling.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;

import ai.timefold.solver.core.api.domain.solution.ConstraintWeightOverrides;
import ai.timefold.solver.core.api.score.HardMediumSoftScore;
import ai.timefold.solver.service.definition.api.ModelConvertor;
import ai.timefold.solver.service.definition.api.domain.ModelConfig;

import org.acme.flightcrewscheduling.domain.Airport;
import org.acme.flightcrewscheduling.domain.Employee;
import org.acme.flightcrewscheduling.domain.Flight;
import org.acme.flightcrewscheduling.domain.FlightAssignment;
import org.acme.flightcrewscheduling.domain.FlightCrewSchedule;
import org.acme.flightcrewscheduling.dto.AirportDTO;
import org.acme.flightcrewscheduling.dto.EmployeeDTO;
import org.acme.flightcrewscheduling.dto.FlightAssignmentDTO;
import org.acme.flightcrewscheduling.dto.FlightCrewScheduleConfigOverrides;
import org.acme.flightcrewscheduling.dto.FlightCrewScheduleInput;
import org.acme.flightcrewscheduling.dto.FlightCrewScheduleOutput;
import org.acme.flightcrewscheduling.dto.FlightDTO;
import org.acme.flightcrewscheduling.solver.FlightCrewSchedulingConstraintProvider;

@ApplicationScoped
public class FlightCrewScheduleModelConvertor
        implements
        ModelConvertor<HardMediumSoftScore, FlightCrewScheduleInput, FlightCrewScheduleConfigOverrides, FlightCrewSchedule, FlightCrewScheduleOutput> {

    @Override
    public FlightCrewScheduleInput applyOutputToInput(FlightCrewScheduleInput modelInput,
            FlightCrewScheduleOutput modelOutput) {
        Map<String, FlightAssignmentDTO> outputAssignments = modelOutput.flightAssignments().stream()
                .collect(Collectors.toMap(FlightAssignmentDTO::id, assignment -> assignment));
        List<FlightAssignmentDTO> updatedAssignments = modelInput.flightAssignments().stream()
                .map(assignment -> {
                    FlightAssignmentDTO solved = outputAssignments.get(assignment.id());
                    if (solved == null) {
                        return assignment;
                    }
                    return assignment.withEmployeeId(solved.employeeId());
                })
                .collect(Collectors.toList());
        return new FlightCrewScheduleInput(modelInput.airports(), modelInput.employees(), modelInput.flights(),
                updatedAssignments);
    }

    @Override
    public FlightCrewSchedule toSolverModel(FlightCrewScheduleInput modelInput,
            ModelConfig<FlightCrewScheduleConfigOverrides> modelConfig,
            Optional<FlightCrewScheduleOutput> lastModelOutput) {
        Map<String, Airport> airportMap = new HashMap<>();
        List<Airport> airports = modelInput.airports().stream().map(dto -> {
            Airport airport = new Airport(dto.id(), dto.name());
            airportMap.put(airport.getCode(), airport);
            return airport;
        }).collect(Collectors.toList());

        Map<String, Employee> employeeMap = new HashMap<>();
        List<Employee> employees = modelInput.employees().stream().map(dto -> {
            Employee employee = new Employee(dto.id(), dto.name(), airportMap.get(dto.homeAirportId()),
                    List.copyOf(dto.skills()));
            employee.setUnavailableDays(dto.unavailableDays().stream().map(LocalDate::parse).collect(Collectors.toList()));
            employeeMap.put(employee.getId(), employee);
            return employee;
        }).collect(Collectors.toList());

        Map<String, Flight> flightMap = new HashMap<>();
        List<Flight> flights = modelInput.flights().stream().map(dto -> {
            Flight flight = new Flight(dto.flightNumber(), airportMap.get(dto.departureAirportId()),
                    LocalDateTime.parse(dto.departureUTCDateTime()), airportMap.get(dto.arrivalAirportId()),
                    LocalDateTime.parse(dto.arrivalUTCDateTime()));
            flightMap.put(flight.getFlightNumber(), flight);
            return flight;
        }).collect(Collectors.toList());

        List<FlightAssignment> flightAssignments = modelInput.flightAssignments().stream().map(dto -> {
            FlightAssignment assignment = new FlightAssignment(dto.id(), flightMap.get(dto.flightNumber()),
                    dto.indexInFlight(), dto.requiredSkill());
            if (dto.employeeId() != null) {
                assignment.setEmployee(employeeMap.get(dto.employeeId()));
            }
            return assignment;
        }).collect(Collectors.toList());

        FlightCrewSchedule schedule = new FlightCrewSchedule(airports, employees, flights, flightAssignments);
        applyConstraintWeightOverrides(schedule, modelConfig);
        applyLastOutput(flightAssignments, employeeMap, lastModelOutput);
        return schedule;
    }

    private static void applyConstraintWeightOverrides(FlightCrewSchedule schedule,
            ModelConfig<FlightCrewScheduleConfigOverrides> modelConfig) {
        if (modelConfig == null || modelConfig.overrides() == null) {
            return;
        }
        FlightCrewScheduleConfigOverrides overrides = modelConfig.overrides();
        // Only apply weights that are actually set (non-null) in the merged overrides. A null weight means the
        // input did not override it, so the configuration profile value (or the constraint's default) is kept.
        Map<String, HardMediumSoftScore> weights = new HashMap<>();
        putIfPresent(weights, FlightCrewSchedulingConstraintProvider.FIRST_ASSIGNMENT_NOT_DEPARTING_FROM_HOME,
                overrides.firstAssignmentNotDepartingFromHomeWeight());
        putIfPresent(weights, FlightCrewSchedulingConstraintProvider.LAST_ASSIGNMENT_NOT_ARRIVING_AT_HOME,
                overrides.lastAssignmentNotArrivingAtHomeWeight());
        if (!weights.isEmpty()) {
            schedule.setConstraintWeightOverrides(ConstraintWeightOverrides.of(weights));
        }
    }

    private static void putIfPresent(Map<String, HardMediumSoftScore> weights, String constraintName, Long weight) {
        if (weight != null) {
            weights.put(constraintName, HardMediumSoftScore.ofSoft(weight));
        }
    }

    private static void applyLastOutput(List<FlightAssignment> flightAssignments, Map<String, Employee> employeeMap,
            Optional<FlightCrewScheduleOutput> lastModelOutput) {
        if (lastModelOutput.isEmpty()) {
            return;
        }
        Map<String, FlightAssignmentDTO> assignmentMap = lastModelOutput.get().flightAssignments().stream()
                .collect(Collectors.toMap(FlightAssignmentDTO::id, assignment -> assignment));
        for (FlightAssignment assignment : flightAssignments) {
            FlightAssignmentDTO solved = assignmentMap.get(assignment.getId());
            if (solved != null && solved.employeeId() != null) {
                assignment.setEmployee(employeeMap.get(solved.employeeId()));
            }
        }
    }

    @Override
    public FlightCrewScheduleOutput toModelOutput(FlightCrewSchedule solverModel) {
        List<AirportDTO> airports = solverModel.getAirports().stream().map(this::toDTO).collect(Collectors.toList());
        List<EmployeeDTO> employees = solverModel.getEmployees().stream().map(this::toDTO).collect(Collectors.toList());
        List<FlightDTO> flights = solverModel.getFlights().stream().map(this::toDTO).collect(Collectors.toList());
        List<FlightAssignmentDTO> flightAssignments =
                solverModel.getFlightAssignments().stream().map(this::toDTO).collect(Collectors.toList());
        String score = solverModel.getScore() == null ? "" : solverModel.getScore().toString();
        return new FlightCrewScheduleOutput(airports, employees, flights, flightAssignments, score);
    }

    private AirportDTO toDTO(Airport airport) {
        return new AirportDTO(airport.getCode(), airport.getName());
    }

    private EmployeeDTO toDTO(Employee employee) {
        String homeAirportId = employee.getHomeAirport() == null ? null : employee.getHomeAirport().getCode();
        List<String> skills = employee.getSkills() == null ? List.of() : List.copyOf(employee.getSkills());
        List<String> unavailableDays = employee.getUnavailableDays() == null ? List.of()
                : employee.getUnavailableDays().stream().map(LocalDate::toString).collect(Collectors.toList());
        return new EmployeeDTO(employee.getId(), employee.getName(), homeAirportId, skills, unavailableDays);
    }

    private FlightDTO toDTO(Flight flight) {
        String departureAirportId = flight.getDepartureAirport() == null ? null : flight.getDepartureAirport().getCode();
        String arrivalAirportId = flight.getArrivalAirport() == null ? null : flight.getArrivalAirport().getCode();
        String departure = flight.getDepartureUTCDateTime() == null ? null : flight.getDepartureUTCDateTime().toString();
        String arrival = flight.getArrivalUTCDateTime() == null ? null : flight.getArrivalUTCDateTime().toString();
        return new FlightDTO(flight.getFlightNumber(), departureAirportId, departure, arrivalAirportId, arrival);
    }

    private FlightAssignmentDTO toDTO(FlightAssignment assignment) {
        String employeeId = assignment.getEmployee() == null ? null : assignment.getEmployee().getId();
        return new FlightAssignmentDTO(assignment.getId(), assignment.getFlight().getFlightNumber(),
                assignment.getIndexInFlight(), assignment.getRequiredSkill(), employeeId);
    }
}
