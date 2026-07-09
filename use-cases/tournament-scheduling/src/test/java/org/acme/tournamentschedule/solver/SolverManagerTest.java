package org.acme.tournamentschedule.solver;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

import jakarta.inject.Inject;

import ai.timefold.solver.core.api.solver.SolverManager;
import ai.timefold.solver.service.definition.api.domain.ModelConfig;

import org.acme.tournamentschedule.domain.TournamentSchedule;
import org.acme.tournamentschedule.dto.TournamentScheduleInputMetrics;
import org.acme.tournamentschedule.dto.TournamentScheduleOutputMetrics;
import org.acme.tournamentschedule.service.TournamentScheduleModelConvertor;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class SolverManagerTest {

    @Inject
    SolverManager<TournamentSchedule> solverManager;

    @Inject
    TournamentScheduleModelConvertor modelConvertor;

    @Test
    void solve() throws ExecutionException, InterruptedException {
        var input = SolverTestDataFactory.createProblem();

        TournamentSchedule problem = modelConvertor.toSolverModel(input, ModelConfig.empty(), Optional.empty());

        TournamentSchedule solution = solverManager.solveBuilder().withProblemId(0L)
                .withProblemFinder(id -> problem).run().getFinalBestSolution();
        assertThat(solution.getScore().isFeasible()).isTrue();

        TournamentScheduleInputMetrics inputMetrics = solution.getInputMetrics();
        assertThat(inputMetrics.teams()).isEqualTo(solution.getTeams().size());
        assertThat(inputMetrics.days()).isEqualTo(solution.getDays().size());
        assertThat(inputMetrics.teamAssignments()).isEqualTo(solution.getTeamAssignments().size());
        assertThat(inputMetrics.unavailabilityPenalties()).isEqualTo(solution.getUnavailabilityPenalties().size());

        TournamentScheduleOutputMetrics outputMetrics = solution.getOutputMetrics();
        assertThat(outputMetrics.totalAssignedTeamAssignments()).isEqualTo(solution.getTeamAssignments().size());
        assertThat(outputMetrics.totalUnassignedTeamAssignments()).isZero();
        assertThat(outputMetrics.totalUsedTeams()).isPositive();
        assertThat(outputMetrics.totalUsedDays()).isPositive();
    }
}
