package org.acme.conferencescheduling.solver;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

import jakarta.inject.Inject;

import ai.timefold.solver.core.api.solver.SolverManager;
import ai.timefold.solver.service.definition.api.domain.ModelConfig;

import org.acme.conferencescheduling.domain.ConferenceSchedule;
import org.acme.conferencescheduling.dto.ConferenceScheduleInputMetrics;
import org.acme.conferencescheduling.dto.ConferenceScheduleOutputMetrics;
import org.acme.conferencescheduling.service.ConferenceScheduleModelConvertor;
import org.acme.conferencescheduling.support.SolverTestDataFactory;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class SolverManagerTest {

    @Inject
    SolverManager<ConferenceSchedule> solverManager;

    @Inject
    ConferenceScheduleModelConvertor modelConvertor;

    @Test
    void solve() throws ExecutionException, InterruptedException {
        var input = SolverTestDataFactory.createProblem();

        ConferenceSchedule problem = modelConvertor.toSolverModel(input, ModelConfig.empty(), Optional.empty());

        ConferenceSchedule solution = solverManager.solveBuilder().withProblemId(0L)
                .withProblemFinder(id -> problem).run().getFinalBestSolution();
        assertThat(solution.getScore().isFeasible()).isTrue();

        ConferenceScheduleInputMetrics inputMetrics = solution.getInputMetrics();
        assertThat(inputMetrics.talks()).isEqualTo(solution.getTalks().size());
        assertThat(inputMetrics.speakers()).isEqualTo(solution.getSpeakers().size());
        assertThat(inputMetrics.rooms()).isEqualTo(solution.getRooms().size());
        assertThat(inputMetrics.timeslots()).isEqualTo(solution.getTimeslots().size());
        assertThat(inputMetrics.talkTypes()).isPositive();

        ConferenceScheduleOutputMetrics outputMetrics = solution.getOutputMetrics();
        assertThat(outputMetrics.totalScheduledTalks()).isEqualTo(solution.getTalks().size());
        assertThat(outputMetrics.totalUnscheduledTalks()).isZero();
        assertThat(outputMetrics.totalUsedRooms()).isPositive();
        assertThat(outputMetrics.totalUsedTimeslots()).isPositive();
    }
}
