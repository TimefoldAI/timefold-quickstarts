package org.acme.maintenancescheduling.demo;

import static java.time.DayOfWeek.SATURDAY;
import static java.time.DayOfWeek.SUNDAY;
import static org.assertj.core.api.Assertions.assertThat;

import org.acme.maintenancescheduling.dto.input.MaintenanceScheduleInput;
import org.junit.jupiter.api.Test;

class DemoDataBuilderTest {

    @Test
    void shouldBuildData() {
        MaintenanceScheduleInput problem = DemoDataBuilder.basic();

        assertThat(problem.crews()).hasSize(4);
        assertThat(problem.jobs()).hasSize(28);

        var workCalendar = problem.workCalendar();
        assertThat(workCalendar.fromDate().getDayOfWeek()).isNotIn(SATURDAY, SUNDAY);
        assertThat(workCalendar.toDate()).isEqualTo(workCalendar.fromDate().plusWeeks(12));

        problem.jobs().forEach(job -> {
            assertThat(job.id()).isNotNull();
            assertThat(job.name()).isNotNull();
            assertThat(job.durationInDays()).isBetween(1, 10);
            assertThat(job.tags()).isNotEmpty();
            // Unsolved: no crew and no start date yet.
            assertThat(job.crewId()).isNull();
            assertThat(job.startDate()).isNull();
            // Every job's window fits inside the work calendar, and holds its duration plus some slack.
            assertThat(job.minStartDate()).isBetween(workCalendar.fromDate(), workCalendar.toDate());
            assertThat(job.maxEndDate()).isBetween(job.minStartDate(), workCalendar.toDate());
            assertThat(job.idealEndDate()).isBetween(job.minStartDate(), job.maxEndDate());
        });
    }

    @Test
    void shouldBuildTheSameDataTwice() {
        // The dataset is seeded, so two builds only differ in the work calendar, which is anchored to today.
        assertThat(DemoDataBuilder.basic()).isEqualTo(DemoDataBuilder.basic());
    }
}
