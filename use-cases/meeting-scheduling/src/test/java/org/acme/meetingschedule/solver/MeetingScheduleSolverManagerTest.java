package org.acme.meetingschedule.solver;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

import jakarta.inject.Inject;

import ai.timefold.solver.core.api.solver.SolverManager;
import ai.timefold.solver.service.definition.api.domain.ModelConfig;

import org.acme.meetingschedule.domain.MeetingSchedule;
import org.acme.meetingschedule.dto.MeetingScheduleInputMetrics;
import org.acme.meetingschedule.dto.MeetingScheduleOutputMetrics;
import org.acme.meetingschedule.service.MeetingScheduleModelConvertor;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class MeetingScheduleSolverManagerTest {

    @Inject
    SolverManager<MeetingSchedule> solverManager;

    @Inject
    MeetingScheduleModelConvertor modelConvertor;

    @Test
    void solve() throws ExecutionException, InterruptedException {
        var input = SolverTestDataFactory.createProblem();

        MeetingSchedule problem = modelConvertor.toSolverModel(input, ModelConfig.empty(), Optional.empty());

        MeetingSchedule solution = solverManager.solveBuilder().withProblemId(0L)
                .withProblemFinder(id -> problem).run().getFinalBestSolution();
        assertThat(solution.getScore()).isNotNull();

        MeetingScheduleInputMetrics inputMetrics = solution.getInputMetrics();
        assertThat(inputMetrics.meetings()).isEqualTo(solution.getMeetings().size());
        assertThat(inputMetrics.meetingAssignments()).isEqualTo(solution.getMeetingAssignments().size());
        assertThat(inputMetrics.people()).isEqualTo(solution.getPeople().size());
        assertThat(inputMetrics.rooms()).isEqualTo(solution.getRooms().size());
        assertThat(inputMetrics.timeGrains()).isEqualTo(solution.getTimeGrains().size());

        MeetingScheduleOutputMetrics outputMetrics = solution.getOutputMetrics();
        assertThat(outputMetrics.totalAssignedMeetings()).isPositive();
        assertThat(outputMetrics.totalUsedRooms()).isPositive();
    }
}
