package org.acme.projectjobschedule.demo;

import static org.assertj.core.api.Assertions.assertThat;

import org.acme.projectjobschedule.domain.JobType;
import org.acme.projectjobschedule.dto.input.ProjectJobScheduleInput;
import org.junit.jupiter.api.Test;

class DemoDataBuilderTest {

    @Test
    void shouldBuildData() {
        ProjectJobScheduleInput problem = DemoDataBuilder.builder().build();

        assertThat(problem.projects()).hasSize(2);
        assertThat(problem.resources()).hasSize(7);
        assertThat(problem.jobs()).hasSize(24);
        assertThat(problem.allocations()).hasSize(24);

        assertThat(problem.jobs()).allSatisfy(job -> {
            assertThat(job.projectId()).isIn("0", "1");
            if (job.jobType() == JobType.STANDARD) {
                assertThat(job.executionModes()).hasSize(DemoDataBuilder.EXECUTION_MODE_COUNT);
                assertThat(job.executionModes()).allSatisfy(executionMode -> {
                    assertThat(executionMode.duration()).isBetween(1, 5);
                    assertThat(executionMode.resourceRequirements()).hasSizeBetween(1, 3);
                });
            } else {
                // A source or a sink job is not real work: it takes no time and consumes nothing.
                assertThat(job.executionModes()).hasSize(1);
                assertThat(job.executionModes().getFirst().duration()).isZero();
                assertThat(job.executionModes().getFirst().resourceRequirements()).isEmpty();
            }
        });

        // Every project is bracketed by exactly one source and one sink job.
        assertThat(problem.jobs()).filteredOn(job -> job.jobType() == JobType.SOURCE).hasSize(2);
        assertThat(problem.jobs()).filteredOn(job -> job.jobType() == JobType.SINK).hasSize(2);

        // A job only ever precedes a job that comes after it, which keeps the precedences acyclic.
        assertThat(problem.jobs()).allSatisfy(job -> assertThat(job.successorJobIds())
                .allSatisfy(successorJobId -> assertThat(Integer.parseInt(successorJobId))
                        .isGreaterThan(Integer.parseInt(job.id()))));

        // Nothing is scheduled yet: the solver picks the execution mode and the delay of every job.
        assertThat(problem.allocations()).allSatisfy(allocation -> {
            assertThat(allocation.executionModeId()).isNull();
            assertThat(allocation.delay()).isNull();
            assertThat(allocation.pinned()).isNull();
        });
    }
}
