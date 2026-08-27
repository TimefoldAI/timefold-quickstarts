package org.acme.conferencescheduling.solver;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.Optional;

import jakarta.inject.Inject;

import ai.timefold.solver.core.api.solver.Solver;
import ai.timefold.solver.core.api.solver.SolverFactory;
import ai.timefold.solver.core.config.solver.EnvironmentMode;
import ai.timefold.solver.core.config.solver.SolverConfig;
import ai.timefold.solver.service.definition.api.domain.ModelConfig;

import org.acme.conferencescheduling.domain.ConferenceSchedule;
import org.acme.conferencescheduling.service.ConferenceScheduleModelConvertor;
import org.acme.conferencescheduling.support.TestHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@EnabledIfSystemProperty(named = "slowly", matches = "true")
class ConferenceScheduleEnvironmentTest {

    @Inject
    SolverConfig solverConfig;

    @Inject
    ConferenceScheduleModelConvertor modelConvertor;

    @Test
    void solveFullAssert() {
        solve(EnvironmentMode.FULL_ASSERT, null);
    }

    @Test
    void solveStepAssert() {
        solve(EnvironmentMode.STEP_ASSERT, null);
    }

    // Multithreaded solving is a Timefold Solver Enterprise Edition feature, so these only run
    // when the enterprise Maven profile (-Denterprise) is active.
    @Test
    @EnabledIfSystemProperty(named = "timefold.solver.enterprise", matches = "true")
    void solveFullAssertMultithreaded() {
        solve(EnvironmentMode.FULL_ASSERT, SolverConfig.MOVE_THREAD_COUNT_AUTO);
    }

    @Test
    @EnabledIfSystemProperty(named = "timefold.solver.enterprise", matches = "true")
    void solveStepAssertMultithreaded() {
        solve(EnvironmentMode.STEP_ASSERT, SolverConfig.MOVE_THREAD_COUNT_AUTO);
    }

    void solve(EnvironmentMode environmentMode, String moveThreadCount) {
        var input = TestHelper.createProblem();
        ConferenceSchedule problem = modelConvertor.toSolverModel(input, ModelConfig.empty(), Optional.empty());

        SolverConfig updatedConfig = solverConfig.copyConfig();
        updatedConfig.withEnvironmentMode(environmentMode).withTerminationSpentLimit(Duration.ofSeconds(30))
                .getTerminationConfig().withBestScoreLimit(null);
        if (moveThreadCount != null) {
            updatedConfig.withMoveThreadCount(moveThreadCount);
        }
        SolverFactory<ConferenceSchedule> solverFactory = SolverFactory.create(updatedConfig);

        Solver<ConferenceSchedule> solver = solverFactory.buildSolver();
        ConferenceSchedule solution = solver.solve(problem);
        assertThat(solution.getScore()).isNotNull();
    }
}
