package org.acme.bedallocation.solver;

import static org.acme.bedallocation.support.TestHelper.aBedDTO;
import static org.acme.bedallocation.support.TestHelper.aDepartmentDTO;
import static org.acme.bedallocation.support.TestHelper.aRoomDTO;
import static org.acme.bedallocation.support.TestHelper.aStayDTO;
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
import org.acme.bedallocation.dto.BedPlanInput;
import org.acme.bedallocation.dto.DepartmentDTO;
import org.acme.bedallocation.dto.RoomDTO;
import org.acme.bedallocation.dto.StayDTO;
import org.acme.bedallocation.service.BedPlanModelConvertor;
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
        solve(EnvironmentMode.FULL_ASSERT);
    }

    @Test
    void solveStepAssert() {
        solve(EnvironmentMode.STEP_ASSERT);
    }

    void solve(EnvironmentMode environmentMode) {
        var input = createProblem();
        BedPlan problem = modelConvertor.toSolverModel(input, ModelConfig.empty(), Optional.empty());

        SolverConfig updatedConfig = solverConfig.copyConfig();
        updatedConfig.withEnvironmentMode(environmentMode).withTerminationSpentLimit(Duration.ofSeconds(30))
                .getTerminationConfig().withBestScoreLimit(null);
        SolverFactory<BedPlan> solverFactory = SolverFactory.create(updatedConfig);

        Solver<BedPlan> solver = solverFactory.buildSolver();
        BedPlan solution = solver.solve(problem);
        assertThat(solution.getScore()).isNotNull();
    }

    private static BedPlanInput createProblem() {
        List<RoomDTO> rooms = List.of(room("r1"), room("r2"), room("r3"));
        DepartmentDTO department = aDepartmentDTO("d1").rooms(rooms).build();
        List<StayDTO> stays = List.of(aStayDTO("s1").build(), aStayDTO("s2").build(), aStayDTO("s3").build(),
                aStayDTO("s4").build());
        return new BedPlanInput(List.of(department), stays);
    }

    private static RoomDTO room(String id) {
        return aRoomDTO(id).beds(List.of(aBedDTO(id + "-bed0").build(), aBedDTO(id + "-bed1").build()))
                .build();
    }
}
