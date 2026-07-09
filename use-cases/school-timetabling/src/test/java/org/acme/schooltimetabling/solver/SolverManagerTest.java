package org.acme.schooltimetabling.solver;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

import jakarta.inject.Inject;

import ai.timefold.solver.core.api.solver.SolverManager;
import ai.timefold.solver.service.definition.api.domain.ModelConfig;

import org.acme.schooltimetabling.domain.Timetable;
import org.acme.schooltimetabling.dto.TimetableInputMetrics;
import org.acme.schooltimetabling.dto.TimetableOutputMetrics;
import org.acme.schooltimetabling.service.TimetableModelConvertor;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class SolverManagerTest {

    @Inject
    SolverManager<Timetable> solverManager;

    @Inject
    TimetableModelConvertor modelConvertor;

    @Test
    void solve() throws ExecutionException, InterruptedException {
        var input = SolverTestDataFactory.createProblem();

        Timetable problem = modelConvertor.toSolverModel(input, ModelConfig.empty(), Optional.empty());

        Timetable solution = solverManager.solveBuilder().withProblemId(0L)
                .withProblemFinder(id -> problem).run().getFinalBestSolution();
        assertThat(solution.getScore().isFeasible()).isTrue();

        TimetableInputMetrics inputMetrics = solution.getInputMetrics();
        assertThat(inputMetrics.lessons()).isEqualTo(solution.getLessons().size());
        assertThat(inputMetrics.timeslots()).isEqualTo(solution.getTimeslots().size());
        assertThat(inputMetrics.rooms()).isEqualTo(solution.getRooms().size());
        assertThat(inputMetrics.teachers()).isPositive();
        assertThat(inputMetrics.studentGroups()).isPositive();

        TimetableOutputMetrics outputMetrics = solution.getOutputMetrics();
        assertThat(outputMetrics.totalScheduledLessons()).isEqualTo(solution.getLessons().size());
        assertThat(outputMetrics.totalUnscheduledLessons()).isZero();
        assertThat(outputMetrics.totalUsedRooms()).isPositive();
        assertThat(outputMetrics.totalUsedTimeslots()).isPositive();
    }
}
