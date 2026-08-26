package org.acme.bedallocation.solver;

import static org.acme.bedallocation.support.TestHelper.aBedDTO;
import static org.acme.bedallocation.support.TestHelper.aDepartmentDTO;
import static org.acme.bedallocation.support.TestHelper.aRoomDTO;
import static org.acme.bedallocation.support.TestHelper.aStayDTO;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import jakarta.inject.Inject;

import ai.timefold.solver.core.api.solver.SolverManager;
import ai.timefold.solver.service.definition.api.domain.ModelConfig;

import org.acme.bedallocation.domain.BedPlan;
import org.acme.bedallocation.dto.input.BedPlanInput;
import org.acme.bedallocation.dto.input.BedPlanInputMetrics;
import org.acme.bedallocation.dto.input.DepartmentInputDTO;
import org.acme.bedallocation.dto.input.RoomInputDTO;
import org.acme.bedallocation.dto.input.StayInputDTO;
import org.acme.bedallocation.dto.output.BedPlanOutputMetrics;
import org.acme.bedallocation.service.BedPlanModelConvertor;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class SolverManagerTest {

    @Inject
    SolverManager<BedPlan> solverManager;

    @Inject
    BedPlanModelConvertor modelConvertor;

    @Test
    void solve() throws ExecutionException, InterruptedException {
        var input = createProblem();

        BedPlan problem = modelConvertor.toSolverModel(input, ModelConfig.empty(), Optional.empty());

        BedPlan solution = solverManager.solveBuilder().withProblemId(0L)
                .withProblemFinder(id -> problem).run().getFinalBestSolution();
        assertThat(solution.getScore().isFeasible()).isTrue();

        BedPlanInputMetrics inputMetrics = solution.getInputMetrics();
        assertThat(inputMetrics.stays()).isEqualTo(solution.getStays().size());
        assertThat(inputMetrics.departments()).isEqualTo(solution.getDepartments().size());
        assertThat(inputMetrics.rooms()).isEqualTo(solution.getRooms().size());
        assertThat(inputMetrics.beds()).isEqualTo(solution.getBeds().size());

        BedPlanOutputMetrics outputMetrics = solution.getOutputMetrics();
        assertThat(outputMetrics.totalAssignedStays()).isEqualTo(solution.getStays().size());
        assertThat(outputMetrics.totalUnassignedStays()).isZero();
        assertThat(outputMetrics.totalUsedRooms()).isPositive();
        assertThat(outputMetrics.totalUsedBeds()).isPositive();
    }

    private static BedPlanInput createProblem() {
        List<RoomInputDTO> rooms = List.of(room("r1"), room("r2"), room("r3"));
        DepartmentInputDTO department = aDepartmentDTO("d1").rooms(rooms).build();
        List<StayInputDTO> stays = List.of(aStayDTO("s1").build(), aStayDTO("s2").build(), aStayDTO("s3").build(),
                aStayDTO("s4").build());
        return new BedPlanInput(List.of(department), stays);
    }

    private static RoomInputDTO room(String id) {
        return aRoomDTO(id).beds(List.of(aBedDTO(id + "-bed0").build(), aBedDTO(id + "-bed1").build()))
                .build();
    }
}
