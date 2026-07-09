package org.acme.maintenancescheduling;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.acme.maintenancescheduling.dto.CrewDTO;
import org.acme.maintenancescheduling.dto.CrewIdDetail;
import org.acme.maintenancescheduling.dto.JobDTO;
import org.acme.maintenancescheduling.dto.JobIdDetail;
import org.acme.maintenancescheduling.dto.MaintenanceScheduleConfigOverrides;
import org.acme.maintenancescheduling.dto.MaintenanceScheduleInput;
import org.acme.maintenancescheduling.dto.MaintenanceScheduleInputMetrics;
import org.acme.maintenancescheduling.dto.MaintenanceScheduleOutput;
import org.acme.maintenancescheduling.dto.MaintenanceScheduleOutputMetrics;
import org.acme.maintenancescheduling.dto.WorkCalendarDTO;
import org.junit.jupiter.api.Test;

class DtoWithMethodsUsageTest {

    @Test
    void allWithMethodsProduceUpdatedCopies() {
        var baseCalendar = new WorkCalendarDTO("c1", "2024-01-01", "2024-03-01");
        var updatedCalendar = baseCalendar.withId("c2").withFromDate("2024-02-01").withToDate("2024-04-01");

        var baseCrew = new CrewDTO("r1", "Alpha");
        var updatedCrew = baseCrew.withId("r2").withName("Beta");

        var baseJob = new JobDTO("j1", "Job", 3, "2024-01-01", "2024-02-01", "2024-01-20",
                List.of("Downtown"), "", "", "");
        var updatedJob = baseJob.withId("j2")
                .withName("Other job")
                .withDurationInDays(5)
                .withMinStartDate("2024-01-05")
                .withMaxEndDate("2024-02-05")
                .withIdealEndDate("2024-01-25")
                .withTags(List.of("Uptown"))
                .withCrewId("r2")
                .withStartDate("2024-01-08")
                .withEndDate("2024-01-13");

        var updatedJobIdDetail = new JobIdDetail("j1").withJobId("j2");
        var updatedCrewIdDetail = new CrewIdDetail("r1").withCrewId("r2");

        var updatedOverrides = new MaintenanceScheduleConfigOverrides()
                .withBeforeIdealEndDateWeight(10L)
                .withAfterIdealEndDateWeight(20L)
                .withTagConflictWeight(30L);

        var updatedInput = new MaintenanceScheduleInput(baseCalendar, List.of(baseCrew), List.of(baseJob))
                .withWorkCalendar(updatedCalendar)
                .withCrews(List.of(updatedCrew))
                .withJobs(List.of(updatedJob));

        var updatedOutput = new MaintenanceScheduleOutput(baseCalendar, List.of(baseCrew), List.of(baseJob), "0hard")
                .withWorkCalendar(updatedCalendar)
                .withCrews(List.of(updatedCrew))
                .withJobs(List.of(updatedJob))
                .withScore("1hard");

        var updatedInputMetrics = new MaintenanceScheduleInputMetrics(1, 2, 3)
                .withJobs(10)
                .withCrews(20)
                .withTags(30);

        var updatedOutputMetrics = new MaintenanceScheduleOutputMetrics(1, 2, 3)
                .withTotalAssignedJobs(10)
                .withTotalUnassignedJobs(20)
                .withTotalUsedCrews(30);

        assertThat(updatedCalendar.id()).isEqualTo("c2");
        assertThat(updatedCalendar.fromDate()).isEqualTo("2024-02-01");
        assertThat(updatedCalendar.toDate()).isEqualTo("2024-04-01");
        assertThat(updatedCrew.id()).isEqualTo("r2");
        assertThat(updatedCrew.name()).isEqualTo("Beta");
        assertThat(updatedJob.id()).isEqualTo("j2");
        assertThat(updatedJob.name()).isEqualTo("Other job");
        assertThat(updatedJob.durationInDays()).isEqualTo(5);
        assertThat(updatedJob.minStartDate()).isEqualTo("2024-01-05");
        assertThat(updatedJob.maxEndDate()).isEqualTo("2024-02-05");
        assertThat(updatedJob.idealEndDate()).isEqualTo("2024-01-25");
        assertThat(updatedJob.tags()).containsExactly("Uptown");
        assertThat(updatedJob.crewId()).isEqualTo("r2");
        assertThat(updatedJob.startDate()).isEqualTo("2024-01-08");
        assertThat(updatedJob.endDate()).isEqualTo("2024-01-13");
        assertThat(updatedJobIdDetail.jobId()).isEqualTo("j2");
        assertThat(updatedCrewIdDetail.crewId()).isEqualTo("r2");
        assertThat(updatedOverrides.beforeIdealEndDateWeight()).isEqualTo(10L);
        assertThat(updatedOverrides.afterIdealEndDateWeight()).isEqualTo(20L);
        assertThat(updatedOverrides.tagConflictWeight()).isEqualTo(30L);
        assertThat(updatedInput.workCalendar()).isEqualTo(updatedCalendar);
        assertThat(updatedInput.crews()).containsExactly(updatedCrew);
        assertThat(updatedInput.jobs()).containsExactly(updatedJob);
        assertThat(updatedOutput.workCalendar()).isEqualTo(updatedCalendar);
        assertThat(updatedOutput.crews()).containsExactly(updatedCrew);
        assertThat(updatedOutput.jobs()).containsExactly(updatedJob);
        assertThat(updatedOutput.score()).isEqualTo("1hard");
        assertThat(updatedInputMetrics.jobs()).isEqualTo(10);
        assertThat(updatedInputMetrics.crews()).isEqualTo(20);
        assertThat(updatedInputMetrics.tags()).isEqualTo(30);
        assertThat(updatedOutputMetrics.totalAssignedJobs()).isEqualTo(10);
        assertThat(updatedOutputMetrics.totalUnassignedJobs()).isEqualTo(20);
        assertThat(updatedOutputMetrics.totalUsedCrews()).isEqualTo(30);
    }
}
