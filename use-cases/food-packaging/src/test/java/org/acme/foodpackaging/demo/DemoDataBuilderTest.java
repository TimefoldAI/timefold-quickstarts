package org.acme.foodpackaging.demo;

import static org.assertj.core.api.Assertions.assertThat;

import org.acme.foodpackaging.dto.input.PackagingScheduleInput;
import org.junit.jupiter.api.Test;

class DemoDataBuilderTest {

    @Test
    void shouldBuildData() {
        PackagingScheduleInput problem = DemoDataBuilder.builder().build();

        assertThat(problem.products()).hasSize(60);
        assertThat(problem.operators()).hasSize(5);
        assertThat(problem.lines()).hasSize(5);
        assertThat(problem.jobs()).hasSize(100);
        assertThat(problem.workCalendar().fromDate()).isBefore(problem.workCalendar().toDate());
        // Nothing is scheduled yet, so every line is empty and without an operator.
        problem.lines().forEach(line -> {
            assertThat(line.operatorId()).isNull();
            assertThat(line.jobIds()).isEmpty();
        });
        // A line can switch between any two products, so the cleaning duration matrix has to be complete.
        problem.products().forEach(product -> assertThat(product.cleaningDurations()).hasSize(60));
    }
}
