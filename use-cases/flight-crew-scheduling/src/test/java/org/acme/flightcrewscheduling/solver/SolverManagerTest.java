package org.acme.flightcrewscheduling.solver;

import static org.acme.flightcrewscheduling.support.TestHelper.createProblem;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

import jakarta.inject.Inject;

import ai.timefold.solver.core.api.solver.SolverManager;
import ai.timefold.solver.service.definition.api.domain.ModelConfig;

import org.acme.flightcrewscheduling.domain.FlightCrewSchedule;
import org.acme.flightcrewscheduling.dto.input.FlightCrewScheduleInputMetrics;
import org.acme.flightcrewscheduling.dto.output.FlightCrewScheduleOutputMetrics;
import org.acme.flightcrewscheduling.service.FlightCrewScheduleModelConvertor;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class SolverManagerTest {

    @Inject
    SolverManager<FlightCrewSchedule> solverManager;

    @Inject
    FlightCrewScheduleModelConvertor modelConvertor;

    @Test
    void solve() throws ExecutionException, InterruptedException {
        FlightCrewSchedule problem = modelConvertor.toSolverModel(createProblem(), ModelConfig.empty(), Optional.empty());

        FlightCrewSchedule solution = solverManager.solveBuilder().withProblemId(0L)
                .withProblemFinder(id -> problem).run().getFinalBestSolution();
        assertThat(solution.getScore().isFeasible()).isTrue();

        FlightCrewScheduleInputMetrics inputMetrics = solution.getInputMetrics();
        assertThat(inputMetrics.flightAssignments()).isEqualTo(solution.getFlightAssignments().size());
        assertThat(inputMetrics.flights()).isEqualTo(solution.getFlights().size());
        assertThat(inputMetrics.employees()).isEqualTo(solution.getEmployees().size());
        assertThat(inputMetrics.airports()).isEqualTo(solution.getAirports().size());

        FlightCrewScheduleOutputMetrics outputMetrics = solution.getOutputMetrics();
        assertThat(outputMetrics.totalAssignedFlightAssignments()).isEqualTo(solution.getFlightAssignments().size());
        assertThat(outputMetrics.totalUnassignedFlightAssignments()).isZero();
        assertThat(outputMetrics.totalUsedEmployees()).isPositive();
        assertThat(outputMetrics.totalCoveredFlights()).isEqualTo(solution.getFlights().size());
    }
}
