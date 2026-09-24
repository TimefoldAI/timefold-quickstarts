package org.acme.sportsleagueschedule.solver;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

import jakarta.inject.Inject;

import ai.timefold.solver.core.api.solver.SolverManager;
import ai.timefold.solver.service.definition.api.domain.ModelConfig;

import org.acme.sportsleagueschedule.domain.LeagueSchedule;
import org.acme.sportsleagueschedule.domain.Match;
import org.acme.sportsleagueschedule.dto.input.LeagueScheduleInputMetrics;
import org.acme.sportsleagueschedule.dto.output.LeagueScheduleOutputMetrics;
import org.acme.sportsleagueschedule.service.LeagueScheduleModelConvertor;
import org.acme.sportsleagueschedule.support.TestHelper;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class SolverManagerTest {

    @Inject
    SolverManager<LeagueSchedule> solverManager;

    @Inject
    LeagueScheduleModelConvertor modelConvertor;

    @Test
    void solve() throws ExecutionException, InterruptedException {
        var input = TestHelper.createProblem();

        LeagueSchedule problem = modelConvertor.toSolverModel(input, ModelConfig.empty(), Optional.empty());

        LeagueSchedule solution = solverManager.solveBuilder().withProblemId(0L)
                .withProblemFinder(id -> problem).run().getFinalBestSolution();
        assertThat(solution.getScore().isFeasible()).isTrue();
        assertThat(solution.getMatches()).allMatch(Match::isAssigned);

        LeagueScheduleInputMetrics inputMetrics = solution.getInputMetrics();
        assertThat(inputMetrics.matches()).isEqualTo(solution.getMatches().size());
        assertThat(inputMetrics.rounds()).isEqualTo(solution.getRounds().size());
        assertThat(inputMetrics.teams()).isEqualTo(solution.getTeams().size());

        LeagueScheduleOutputMetrics outputMetrics = solution.getOutputMetrics();
        assertThat(outputMetrics.totalAssignedMatches()).isEqualTo(solution.getMatches().size());
        assertThat(outputMetrics.totalUnassignedMatches()).isZero();
        assertThat(outputMetrics.totalUsedRounds()).isPositive();
        assertThat(outputMetrics.totalClassicMatchesOffPeak()).isNotNegative();
    }
}
