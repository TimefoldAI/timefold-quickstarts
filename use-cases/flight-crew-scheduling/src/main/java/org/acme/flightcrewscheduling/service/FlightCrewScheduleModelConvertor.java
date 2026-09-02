package org.acme.flightcrewscheduling.service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;

import ai.timefold.solver.core.api.domain.solution.ConstraintWeightOverrides;
import ai.timefold.solver.core.api.score.HardSoftScore;
import ai.timefold.solver.service.definition.api.ModelConvertor;
import ai.timefold.solver.service.definition.api.domain.ModelConfig;

import org.acme.flightcrewscheduling.domain.Airport;
import org.acme.flightcrewscheduling.domain.Employee;
import org.acme.flightcrewscheduling.domain.Flight;
import org.acme.flightcrewscheduling.domain.FlightAssignment;
import org.acme.flightcrewscheduling.domain.FlightCrewSchedule;
import org.acme.flightcrewscheduling.domain.FlightCrewScheduleConstraintProperties;
import org.acme.flightcrewscheduling.dto.input.AirportInputDTO;
import org.acme.flightcrewscheduling.dto.input.EmployeeInputDTO;
import org.acme.flightcrewscheduling.dto.input.FlightAssignmentInputDTO;
import org.acme.flightcrewscheduling.dto.input.FlightCrewScheduleConfigOverrides;
import org.acme.flightcrewscheduling.dto.input.FlightCrewScheduleInput;
import org.acme.flightcrewscheduling.dto.input.FlightInputDTO;
import org.acme.flightcrewscheduling.dto.output.FlightAssignmentOutputDTO;
import org.acme.flightcrewscheduling.dto.output.FlightCrewScheduleOutput;

@ApplicationScoped
public class FlightCrewScheduleModelConvertor
        implements
        ModelConvertor<HardSoftScore, FlightCrewScheduleInput, FlightCrewScheduleConfigOverrides, FlightCrewSchedule, FlightCrewScheduleOutput> {

    @Override
    public FlightCrewScheduleInput applyOutputToInput(FlightCrewScheduleInput modelInput,
            FlightCrewScheduleOutput modelOutput) {
        Map<String, FlightAssignmentOutputDTO> outputAssignments = modelOutput.flightAssignments().stream()
                .collect(Collectors.toMap(FlightAssignmentOutputDTO::id, assignment -> assignment));
        List<FlightAssignmentInputDTO> updatedAssignments = modelInput.flightAssignments().stream()
                .map(assignment -> {
                    FlightAssignmentOutputDTO solved = outputAssignments.get(assignment.id());
                    return solved == null ? assignment : assignment.withEmployeeId(solved.employeeId());
                })
                .collect(Collectors.toList());
        return modelInput.withFlightAssignments(updatedAssignments);
    }

    @Override
    public FlightCrewSchedule toSolverModel(FlightCrewScheduleInput modelInput,
            ModelConfig<FlightCrewScheduleConfigOverrides> modelConfig,
            Optional<FlightCrewScheduleOutput> lastModelOutput) {
        List<Airport> airports = modelInput.airports().stream()
                .map(FlightCrewScheduleModelConvertor::toAirport)
                .toList();
        Map<String, Airport> airportMap = airports.stream()
                .collect(Collectors.toMap(Airport::code, Function.identity(), (a, b) -> a, LinkedHashMap::new));

        List<Employee> employees = modelInput.employees().stream()
                .map(dto -> toEmployee(dto, airportMap))
                .toList();
        Map<String, Employee> employeeMap = employees.stream()
                .collect(Collectors.toMap(Employee::id, Function.identity(), (a, b) -> a, LinkedHashMap::new));

        List<Flight> flights = modelInput.flights().stream()
                .map(dto -> toFlight(dto, airportMap))
                .toList();
        Map<String, Flight> flightMap = flights.stream()
                .collect(Collectors.toMap(Flight::flightNumber, Function.identity(), (a, b) -> a, LinkedHashMap::new));

        List<FlightAssignment> flightAssignments = modelInput.flightAssignments().stream()
                .map(dto -> toFlightAssignment(dto, flightMap, employeeMap))
                .toList();

        FlightCrewSchedule schedule = new FlightCrewSchedule(airports, employees, flights, flightAssignments);
        applyConstraintWeightOverrides(schedule, modelConfig);
        applyLastOutput(flightAssignments, employeeMap, lastModelOutput);
        return schedule;
    }

    @Override
    public FlightCrewScheduleOutput toModelOutput(FlightCrewSchedule solverModel) {
        List<FlightAssignmentOutputDTO> flightAssignments = solverModel.getFlightAssignments().stream()
                .map(assignment -> new FlightAssignmentOutputDTO(assignment.getId(),
                        assignment.getEmployee() == null ? null : assignment.getEmployee().id()))
                .toList();
        return new FlightCrewScheduleOutput(flightAssignments);
    }

    private static Airport toAirport(AirportInputDTO dto) {
        return new Airport(dto.code(), dto.name(), dto.latitude(), dto.longitude());
    }

    private static Employee toEmployee(EmployeeInputDTO dto, Map<String, Airport> airportMap) {
        return new Employee(dto.id(), dto.name(), require(airportMap, dto.homeAirportCode(), "airport"), dto.skills(),
                dto.unavailableDays());
    }

    private static Flight toFlight(FlightInputDTO dto, Map<String, Airport> airportMap) {
        return new Flight(dto.flightNumber(), require(airportMap, dto.departureAirportCode(), "airport"),
                dto.departureUTCDateTime(), require(airportMap, dto.arrivalAirportCode(), "airport"),
                dto.arrivalUTCDateTime());
    }

    private static FlightAssignment toFlightAssignment(FlightAssignmentInputDTO dto, Map<String, Flight> flightMap,
            Map<String, Employee> employeeMap) {
        Employee employee = dto.employeeId() == null ? null : require(employeeMap, dto.employeeId(), "employee");
        return new FlightAssignment(dto.id(), require(flightMap, dto.flightNumber(), "flight"), dto.indexInFlight(),
                dto.requiredSkill(), employee);
    }

    /**
     * Fails fast with an actionable message instead of letting an unknown reference
     * turn into a null in the solver model and a delayed NullPointerException.
     */
    private static <T> T require(Map<String, T> map, String key, String kind) {
        T value = map.get(key);
        if (value == null) {
            throw new IllegalArgumentException("Unknown %s '%s'.".formatted(kind, key));
        }
        return value;
    }

    private static void applyConstraintWeightOverrides(FlightCrewSchedule schedule,
            ModelConfig<FlightCrewScheduleConfigOverrides> modelConfig) {
        if (modelConfig == null || modelConfig.overrides() == null) {
            return;
        }
        FlightCrewScheduleConfigOverrides overrides = modelConfig.overrides();
        // Only apply weights that are actually set (non-null) in the merged overrides. A null weight means the
        // input did not override it, so the configuration profile value (or the constraint's default) is kept.
        Map<String, HardSoftScore> weights = new HashMap<>();
        putIfPresent(weights, FlightCrewScheduleConstraintProperties.FIRST_ASSIGNMENT_NOT_DEPARTING_FROM_HOME,
                overrides.firstAssignmentNotDepartingFromHomeWeight());
        putIfPresent(weights, FlightCrewScheduleConstraintProperties.LAST_ASSIGNMENT_NOT_ARRIVING_AT_HOME,
                overrides.lastAssignmentNotArrivingAtHomeWeight());
        if (!weights.isEmpty()) {
            schedule.setConstraintWeightOverrides(ConstraintWeightOverrides.of(weights));
        }
    }

    private static void putIfPresent(Map<String, HardSoftScore> weights, String constraintName, Long weight) {
        if (weight != null) {
            weights.put(constraintName, HardSoftScore.ofSoft(weight));
        }
    }

    // lastModelOutput is used to recover a run that stopped halfway; it overrides the input assignment.
    private static void applyLastOutput(List<FlightAssignment> flightAssignments, Map<String, Employee> employeeMap,
            Optional<FlightCrewScheduleOutput> lastModelOutput) {
        if (lastModelOutput.isEmpty()) {
            return;
        }
        Map<String, FlightAssignment> assignmentMap = flightAssignments.stream()
                .collect(Collectors.toMap(FlightAssignment::getId, assignment -> assignment));
        for (FlightAssignmentOutputDTO solved : lastModelOutput.get().flightAssignments()) {
            FlightAssignment assignment = assignmentMap.get(solved.id());
            if (assignment == null || solved.employeeId() == null) {
                continue;
            }
            Employee employee = employeeMap.get(solved.employeeId());
            if (employee != null) {
                assignment.setEmployee(employee);
            }
        }
    }
}
