package org.acme.schooltimetabling.solver;

import static org.acme.schooltimetabling.support.TestHelper.createProblem;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

import jakarta.inject.Inject;

import ai.timefold.solver.core.api.solver.SolverManager;
import ai.timefold.solver.service.definition.api.domain.ModelConfig;

import org.acme.schooltimetabling.domain.Lesson;
import org.acme.schooltimetabling.domain.Timetable;
import org.acme.schooltimetabling.dto.input.TimetableInputMetrics;
import org.acme.schooltimetabling.dto.output.TimetableOutputMetrics;
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
        Timetable problem = modelConvertor.toSolverModel(createProblem(), ModelConfig.empty(), Optional.empty());

        TimetableOutputMetrics unsolvedMetrics = problem.getOutputMetrics();
        assertThat(unsolvedMetrics.totalAssignedLessons()).isZero();
        assertThat(unsolvedMetrics.totalUnassignedLessons()).isEqualTo(problem.getLessons().size());

        Timetable solution = solverManager.solveBuilder().withProblemId(0L)
                .withProblemFinder(id -> problem).run().getFinalBestSolution();
        assertThat(solution.getScore().isFeasible()).isTrue();
        assertThat(solution.getLessons()).allMatch(Lesson::isAssigned);

        TimetableInputMetrics inputMetrics = solution.getInputMetrics();
        assertThat(inputMetrics.lessons()).isEqualTo(4);
        assertThat(inputMetrics.timeslots()).isEqualTo(4);
        assertThat(inputMetrics.rooms()).isEqualTo(2);
        assertThat(inputMetrics.teachers()).isEqualTo(2);
        assertThat(inputMetrics.studentGroups()).isEqualTo(2);

        TimetableOutputMetrics outputMetrics = solution.getOutputMetrics();
        assertThat(outputMetrics.totalAssignedLessons()).isEqualTo(solution.getLessons().size());
        assertThat(outputMetrics.totalUnassignedLessons()).isZero();
    }
}
