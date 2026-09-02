package org.acme.meetingschedule.solver;

import static org.acme.meetingschedule.support.TestHelper.createProblem;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

import jakarta.inject.Inject;

import ai.timefold.solver.core.api.solver.SolverManager;
import ai.timefold.solver.service.definition.api.domain.ModelConfig;

import org.acme.meetingschedule.domain.MeetingSchedule;
import org.acme.meetingschedule.dto.input.MeetingScheduleInputMetrics;
import org.acme.meetingschedule.dto.output.MeetingScheduleOutputMetrics;
import org.acme.meetingschedule.service.MeetingScheduleModelConvertor;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class SolverManagerTest {

    @Inject
    SolverManager<MeetingSchedule> solverManager;

    @Inject
    MeetingScheduleModelConvertor modelConvertor;

    @Test
    void solve() throws ExecutionException, InterruptedException {
        var input = createProblem();

        MeetingSchedule problem = modelConvertor.toSolverModel(input, ModelConfig.empty(), Optional.empty());

        MeetingSchedule solution = solverManager.solveBuilder().withProblemId(0L)
                .withProblemFinder(id -> problem).run().getFinalBestSolution();
        assertThat(solution.getScore().isFeasible()).isTrue();

        MeetingScheduleInputMetrics inputMetrics = solution.getInputMetrics();
        assertThat(inputMetrics.meetings()).isEqualTo(solution.getMeetings().size());
        assertThat(inputMetrics.people()).isEqualTo(solution.getPeople().size());
        assertThat(inputMetrics.rooms()).isEqualTo(solution.getRooms().size());
        assertThat(inputMetrics.timeSlots()).isEqualTo(solution.getTimeGrains().size());

        MeetingScheduleOutputMetrics outputMetrics = solution.getOutputMetrics();
        assertThat(outputMetrics.totalAssignedMeetings()).isEqualTo(solution.getMeetingAssignments().size());
        assertThat(outputMetrics.totalUnassignedMeetings()).isZero();
        assertThat(outputMetrics.totalUsedRooms()).isPositive();
        assertThat(outputMetrics.totalOccupiedMinutes()).isPositive();
    }
}
