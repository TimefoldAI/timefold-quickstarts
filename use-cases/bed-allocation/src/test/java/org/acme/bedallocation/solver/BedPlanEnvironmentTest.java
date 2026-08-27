package org.acme.bedallocation.solver;

import static org.acme.bedallocation.support.TestHelper.aBedDTO;
import static org.acme.bedallocation.support.TestHelper.aDepartmentDTO;
import static org.acme.bedallocation.support.TestHelper.aRoomDTO;
import static org.acme.bedallocation.support.TestHelper.aStayDTO;
import static org.acme.bedallocation.support.TestHelper.input;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import jakarta.inject.Inject;

import ai.timefold.solver.core.api.solver.Solver;
import ai.timefold.solver.core.api.solver.SolverFactory;
import ai.timefold.solver.core.config.solver.EnvironmentMode;
import ai.timefold.solver.core.config.solver.SolverConfig;
import ai.timefold.solver.service.definition.api.domain.ModelConfig;

import org.acme.bedallocation.domain.BedPlan;
import org.acme.bedallocation.dto.input.BedPlanInput;
import org.acme.bedallocation.dto.input.DepartmentInputDTO;
import org.acme.bedallocation.dto.input.StayInputDTO;
import org.acme.bedallocation.service.BedPlanModelConvertor;
import org.acme.bedallocation.support.TestHelper.RoomDTOBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@EnabledIfSystemProperty(named = "slowly", matches = "true")
class BedPlanEnvironmentTest {

    @Inject
    SolverConfig solverConfig;

    @Inject
    BedPlanModelConvertor modelConvertor;

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
        var input = createProblem();
        BedPlan problem = modelConvertor.toSolverModel(input, ModelConfig.empty(), Optional.empty());

        SolverConfig updatedConfig = solverConfig.copyConfig();
        updatedConfig.withEnvironmentMode(environmentMode).withTerminationSpentLimit(Duration.ofSeconds(30))
                .getTerminationConfig().withBestScoreLimit(null);
        if (moveThreadCount != null) {
            updatedConfig.withMoveThreadCount(moveThreadCount);
        }
        SolverFactory<BedPlan> solverFactory = SolverFactory.create(updatedConfig);

        Solver<BedPlan> solver = solverFactory.buildSolver();
        BedPlan solution = solver.solve(problem);
        assertThat(solution.getScore()).isNotNull();
    }

    private static BedPlanInput createProblem() {
        List<RoomDTOBuilder> rooms = List.of(room("r1"), room("r2"), room("r3"));
        DepartmentInputDTO department = aDepartmentDTO("d1").rooms(rooms).build();
        List<StayInputDTO> stays = List.of(aStayDTO("s1").build(), aStayDTO("s2").build(), aStayDTO("s3").build(),
                aStayDTO("s4").build());
        return input(List.of(department), stays);
    }

    private static RoomDTOBuilder room(String id) {
        return aRoomDTO(id).beds(List.of(aBedDTO(id + "-bed0"), aBedDTO(id + "-bed1")));
    }
}
