package org.acme.flightcrewscheduling.solver;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.Optional;

import jakarta.inject.Inject;

import ai.timefold.solver.core.api.solver.Solver;
import ai.timefold.solver.core.api.solver.SolverFactory;
import ai.timefold.solver.core.config.solver.EnvironmentMode;
import ai.timefold.solver.core.config.solver.SolverConfig;
import ai.timefold.solver.service.definition.api.domain.ModelConfig;

import org.acme.flightcrewscheduling.domain.FlightCrewSchedule;
import org.acme.flightcrewscheduling.service.FlightCrewScheduleModelConvertor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@EnabledIfSystemProperty(named = "slowly", matches = "true")
class FlightCrewSchedulingEnvironmentTest {

    @Inject
    SolverConfig solverConfig;

    @Inject
    FlightCrewScheduleModelConvertor modelConvertor;

    @Test
    void solveFullAssert() {
        solve(EnvironmentMode.FULL_ASSERT);
    }

    @Test
    void solveStepAssert() {
        solve(EnvironmentMode.STEP_ASSERT);
    }

    void solve(EnvironmentMode environmentMode) {
        var input = SolverTestDataFactory.createProblem();
        FlightCrewSchedule problem = modelConvertor.toSolverModel(input, ModelConfig.empty(), Optional.empty());

        SolverConfig updatedConfig = solverConfig.copyConfig();
        updatedConfig.withEnvironmentMode(environmentMode).withTerminationSpentLimit(Duration.ofSeconds(30))
                .getTerminationConfig().withBestScoreLimit(null);
        SolverFactory<FlightCrewSchedule> solverFactory = SolverFactory.create(updatedConfig);

        Solver<FlightCrewSchedule> solver = solverFactory.buildSolver();
        FlightCrewSchedule solution = solver.solve(problem);
        assertThat(solution.getScore()).isNotNull();
    }
}
