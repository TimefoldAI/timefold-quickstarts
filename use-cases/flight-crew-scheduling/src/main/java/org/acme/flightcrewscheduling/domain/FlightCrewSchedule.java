package org.acme.flightcrewscheduling.domain;

import java.util.List;

import ai.timefold.solver.core.api.domain.solution.ConstraintWeightOverrides;
import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.HardMediumSoftScore;
import ai.timefold.solver.service.definition.api.SolverModel;
import ai.timefold.solver.service.definition.api.metrics.InputMetricsAware;
import ai.timefold.solver.service.definition.api.metrics.OutputMetricsAware;

import org.acme.flightcrewscheduling.dto.FlightCrewScheduleInputMetrics;
import org.acme.flightcrewscheduling.dto.FlightCrewScheduleOutputMetrics;

@PlanningSolution
public class FlightCrewSchedule implements SolverModel<HardMediumSoftScore>,
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
    private HardMediumSoftScore score;

    private ConstraintWeightOverrides<HardMediumSoftScore> constraintWeightOverrides = ConstraintWeightOverrides.none();

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

    public void setAirports(List<Airport> airports) {
        this.airports = airports;
    }

    public List<Employee> getEmployees() {
        return employees;
    }

    public void setEmployees(List<Employee> employees) {
        this.employees = employees;
    }

    public List<Flight> getFlights() {
        return flights;
    }

    public void setFlights(List<Flight> flights) {
        this.flights = flights;
    }

    public List<FlightAssignment> getFlightAssignments() {
        return flightAssignments;
    }

    public void setFlightAssignments(List<FlightAssignment> flightAssignments) {
        this.flightAssignments = flightAssignments;
    }

    @Override
    public HardMediumSoftScore getScore() {
        return score;
    }

    public void setScore(HardMediumSoftScore score) {
        this.score = score;
    }

    @Override
    public ConstraintWeightOverrides<HardMediumSoftScore> getConstraintWeightOverrides() {
        return constraintWeightOverrides;
    }

    public void setConstraintWeightOverrides(ConstraintWeightOverrides<HardMediumSoftScore> constraintWeightOverrides) {
        this.constraintWeightOverrides = constraintWeightOverrides;
    }

    @Override
    public FlightCrewScheduleInputMetrics getInputMetrics() {
        return new FlightCrewScheduleInputMetrics(flightAssignments.size(), flights.size(), employees.size(),
                airports.size());
    }

    @Override
    public FlightCrewScheduleOutputMetrics getOutputMetrics() {
        int assignedAssignments = (int) flightAssignments.stream().filter(FlightAssignment::isAssigned).count();
        int unassignedAssignments = flightAssignments.size() - assignedAssignments;
        int usedEmployees = (int) flightAssignments.stream().filter(FlightAssignment::isAssigned)
                .map(FlightAssignment::getEmployee).distinct().count();
        return new FlightCrewScheduleOutputMetrics(assignedAssignments, unassignedAssignments, usedEmployees);
    }

    @Override
    public String toString() {
        return "FlightCrewSchedule{flightAssignments: " + flightAssignments.size() + ", score: " + score + '}';
    }
}
