package org.acme.foodpackaging.demo;

import static org.assertj.core.api.Assertions.assertThat;

import org.acme.foodpackaging.dto.PackagingScheduleInput;
import org.junit.jupiter.api.Test;

class DemoDataBuilderTest {

    @Test
    void buildsConsistentProblem() {
        PackagingScheduleInput problem = DemoDataBuilder.builder()
                .setLineCount(3)
                .setJobCount(12)
                .setWeekCount(2)
                .build();

        assertThat(problem.workCalendar()).isNotNull();
        assertThat(problem.lines()).hasSize(3);
        assertThat(problem.operators()).hasSize(3);
        assertThat(problem.jobs()).hasSize(12);
        assertThat(problem.products()).isNotEmpty();
        assertThat(problem.jobs()).allSatisfy(job -> {
            assertThat(job.id()).isNotBlank();
            assertThat(job.productId()).isNotBlank();
            assertThat(job.minStartTime()).isNotBlank();
            assertThat(job.lineId()).isNull();
        });
        assertThat(problem.lines()).allSatisfy(line -> {
            assertThat(line.jobIds()).isEmpty();
            assertThat(line.operatorId()).isNull();
        });
        assertThat(problem.products()).allSatisfy(product -> assertThat(product.cleaningDurations()).isNotEmpty());
    }
}
