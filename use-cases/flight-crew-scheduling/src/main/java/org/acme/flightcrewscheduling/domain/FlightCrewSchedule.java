package org.acme.flightcrewscheduling.domain;

import java.util.List;
import java.util.Objects;

import ai.timefold.solver.core.api.domain.solution.ConstraintWeightOverrides;
import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.HardSoftScore;
import ai.timefold.solver.service.definition.api.SolverModel;
import ai.timefold.solver.service.definition.api.metrics.InputMetricsAware;
import ai.timefold.solver.service.definition.api.metrics.OutputMetricsAware;

import org.acme.flightcrewscheduling.dto.input.FlightCrewScheduleInputMetrics;
import org.acme.flightcrewscheduling.dto.output.FlightCrewScheduleOutputMetrics;

@PlanningSolution
public class FlightCrewSchedule implements SolverModel<HardSoftScore>,
        InputMetricsAware<FlightCrewScheduleInputMetrics>, OutputMetricsAware<FlightCrewScheduleOutputMetrics> {

    @ProblemFactCollectionProperty
    private List<Airport> airports;

    @ProblemFactCollectionProperty
    @ValueRangeProvider
    private List<Employee> employees;

    @ProblemFactCollectionProperty
    private List<Flight> flights;

    @PlanningEntityCollectionProperty
    private List<FlightAssignment> flightAssignments;

    @PlanningScore
    private HardSoftScore score;

    private ConstraintWeightOverrides<HardSoftScore> constraintWeightOverrides = ConstraintWeightOverrides.none();

    public FlightCrewSchedule() {
    }

    public FlightCrewSchedule(List<Airport> airports, List<Employee> employees, List<Flight> flights,
            List<FlightAssignment> flightAssignments) {
        this.airports = airports;
        this.employees = employees;
        this.flights = flights;
        this.flightAssignments = flightAssignments;
    }

    public List<Airport> getAirports() {
        return airports;
    }

    public List<Employee> getEmployees() {
        return employees;
    }

    public List<Flight> getFlights() {
        return flights;
    }

    public List<FlightAssignment> getFlightAssignments() {
        return flightAssignments;
    }

    @Override
    public HardSoftScore getScore() {
        return score;
    }

    public void setScore(HardSoftScore score) {
        this.score = score;
    }

    @Override
    public ConstraintWeightOverrides<HardSoftScore> getConstraintWeightOverrides() {
        return constraintWeightOverrides;
    }

    public void setConstraintWeightOverrides(ConstraintWeightOverrides<HardSoftScore> constraintWeightOverrides) {
        this.constraintWeightOverrides = constraintWeightOverrides;
    }

    @Override
    public FlightCrewScheduleInputMetrics getInputMetrics() {
        return new FlightCrewScheduleInputMetrics(flightAssignments.size(), flights.size(), employees.size(),
                airports.size());
    }

    @Override
    public FlightCrewScheduleOutputMetrics getOutputMetrics() {
        int assignedFlightAssignments =
                (int) flightAssignments.stream().filter(assignment -> assignment.getEmployee() != null).count();
        int unassignedFlightAssignments = flightAssignments.size() - assignedFlightAssignments;
        int usedEmployees = (int) flightAssignments.stream()
                .map(FlightAssignment::getEmployee)
                .filter(Objects::nonNull)
                .distinct()
                .count();
        int coveredFlights = (int) flightAssignments.stream()
                .filter(assignment -> assignment.getEmployee() != null)
                .map(FlightAssignment::getFlight)
                .distinct()
                .count();
        return new FlightCrewScheduleOutputMetrics(assignedFlightAssignments, unassignedFlightAssignments, usedEmployees,
                coveredFlights);
    }
}
