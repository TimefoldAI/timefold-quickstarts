package org.acme.foodpackaging;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.acme.foodpackaging.dto.JobDTO;
import org.acme.foodpackaging.dto.JobIdDetail;
import org.acme.foodpackaging.dto.LineDTO;
import org.acme.foodpackaging.dto.LineIdDetail;
import org.acme.foodpackaging.dto.OperatorDTO;
import org.acme.foodpackaging.dto.PackagingScheduleConfigOverrides;
import org.acme.foodpackaging.dto.PackagingScheduleInput;
import org.acme.foodpackaging.dto.PackagingScheduleInputMetrics;
import org.acme.foodpackaging.dto.PackagingScheduleOutput;
import org.acme.foodpackaging.dto.PackagingScheduleOutputMetrics;
import org.acme.foodpackaging.dto.ProductDTO;
import org.acme.foodpackaging.dto.WorkCalendarDTO;
import org.junit.jupiter.api.Test;

class DtoWithMethodsUsageTest {

    @Test
    void allWithMethodsProduceUpdatedCopies() {
        var baseProduct = new ProductDTO("p1", "Product", Map.of("p0", 10L));
        var updatedProduct = baseProduct
                .withId("p2")
                .withName("Other product")
                .withCleaningDurations(Map.of("p1", 20L));

        var baseJob = new JobDTO("j1", "Job", "p1", 60L, "2024-01-01T00:00", "2024-01-02T00:00",
                "2024-01-03T00:00", 1, false, "l1", "2024-01-01T00:00", "2024-01-01T01:00",
                "2024-01-01T02:00", "op1");
        var updatedJob = baseJob
                .withId("j2")
                .withName("Other job")
                .withProductId("p2")
                .withDurationMinutes(120L)
                .withMinStartTime("2024-02-01T00:00")
                .withIdealEndTime("2024-02-02T00:00")
                .withMaxEndTime("2024-02-03T00:00")
                .withPriority(2)
                .withPinned(true)
                .withLineId("l2")
                .withStartCleaningDateTime("2024-02-01T00:00")
                .withStartProductionDateTime("2024-02-01T01:00")
                .withEndDateTime("2024-02-01T02:00")
                .withOperatorId("op2");

        var updatedOperator = new OperatorDTO("op1").withId("op2");

        var baseLine = new LineDTO("l1", "Line", "2024-01-01T00:00", "op1", List.of("j1"));
        var updatedLine = baseLine
                .withId("l2")
                .withName("Other line")
                .withStartDateTime("2024-02-01T00:00")
                .withOperatorId("op2")
                .withJobIds(List.of("j2"));

        var updatedWorkCalendar = new WorkCalendarDTO("2024-01-01", "2024-01-15")
                .withFromDate("2024-02-01")
                .withToDate("2024-02-15");

        var updatedJobIdDetail = new JobIdDetail("j1").withJobId("j2");
        var updatedLineIdDetail = new LineIdDetail("l1").withLineId("l2");

        var updatedOverrides = new PackagingScheduleConfigOverrides()
                .withIdealEndDateTimeWeight(11L)
                .withMaximizeJobsAssignedWeight(22L)
                .withMinimizeMakespanWeight(33L);

        var updatedInput = new PackagingScheduleInput(updatedWorkCalendar, List.of(baseProduct),
                List.of(updatedOperator), List.of(baseLine), List.of(baseJob))
                .withWorkCalendar(updatedWorkCalendar)
                .withProducts(List.of(updatedProduct))
                .withOperators(List.of(updatedOperator))
                .withLines(List.of(updatedLine))
                .withJobs(List.of(updatedJob));

        var updatedOutput = new PackagingScheduleOutput(updatedWorkCalendar, List.of(baseProduct),
                List.of(updatedOperator), List.of(baseLine), List.of(baseJob), "0hard/0medium/0soft")
                .withWorkCalendar(updatedWorkCalendar)
                .withProducts(List.of(updatedProduct))
                .withOperators(List.of(updatedOperator))
                .withLines(List.of(updatedLine))
                .withJobs(List.of(updatedJob))
                .withScore("0hard/0medium/-1soft");

        var updatedInputMetrics = new PackagingScheduleInputMetrics(1, 2, 3, 4)
                .withJobs(10)
                .withLines(20)
                .withOperators(30)
                .withProducts(40);

        var updatedOutputMetrics = new PackagingScheduleOutputMetrics(1, 2, 3, 4)
                .withTotalAssignedJobs(10)
                .withTotalUnassignedJobs(20)
                .withTotalUsedLines(30)
                .withMakespanMinutes(40);

        assertThat(updatedProduct.id()).isEqualTo("p2");
        assertThat(updatedProduct.name()).isEqualTo("Other product");
        assertThat(updatedProduct.cleaningDurations()).containsEntry("p1", 20L);
        assertThat(updatedJob.id()).isEqualTo("j2");
        assertThat(updatedJob.name()).isEqualTo("Other job");
        assertThat(updatedJob.productId()).isEqualTo("p2");
        assertThat(updatedJob.durationMinutes()).isEqualTo(120L);
        assertThat(updatedJob.minStartTime()).isEqualTo("2024-02-01T00:00");
        assertThat(updatedJob.idealEndTime()).isEqualTo("2024-02-02T00:00");
        assertThat(updatedJob.maxEndTime()).isEqualTo("2024-02-03T00:00");
        assertThat(updatedJob.priority()).isEqualTo(2);
        assertThat(updatedJob.pinned()).isTrue();
        assertThat(updatedJob.lineId()).isEqualTo("l2");
        assertThat(updatedJob.startCleaningDateTime()).isEqualTo("2024-02-01T00:00");
        assertThat(updatedJob.startProductionDateTime()).isEqualTo("2024-02-01T01:00");
        assertThat(updatedJob.endDateTime()).isEqualTo("2024-02-01T02:00");
        assertThat(updatedJob.operatorId()).isEqualTo("op2");
        assertThat(updatedOperator.id()).isEqualTo("op2");
        assertThat(updatedLine.id()).isEqualTo("l2");
        assertThat(updatedLine.name()).isEqualTo("Other line");
        assertThat(updatedLine.startDateTime()).isEqualTo("2024-02-01T00:00");
        assertThat(updatedLine.operatorId()).isEqualTo("op2");
        assertThat(updatedLine.jobIds()).containsExactly("j2");
        assertThat(updatedWorkCalendar.fromDate()).isEqualTo("2024-02-01");
        assertThat(updatedWorkCalendar.toDate()).isEqualTo("2024-02-15");
        assertThat(updatedJobIdDetail.jobId()).isEqualTo("j2");
        assertThat(updatedLineIdDetail.lineId()).isEqualTo("l2");
        assertThat(updatedOverrides.idealEndDateTimeWeight()).isEqualTo(11L);
        assertThat(updatedOverrides.maximizeJobsAssignedWeight()).isEqualTo(22L);
        assertThat(updatedOverrides.minimizeMakespanWeight()).isEqualTo(33L);
        assertThat(updatedInput.products()).containsExactly(updatedProduct);
        assertThat(updatedInput.operators()).containsExactly(updatedOperator);
        assertThat(updatedInput.lines()).containsExactly(updatedLine);
        assertThat(updatedInput.jobs()).containsExactly(updatedJob);
        assertThat(updatedInput.workCalendar()).isEqualTo(updatedWorkCalendar);
        assertThat(updatedOutput.products()).containsExactly(updatedProduct);
        assertThat(updatedOutput.operators()).containsExactly(updatedOperator);
        assertThat(updatedOutput.lines()).containsExactly(updatedLine);
        assertThat(updatedOutput.jobs()).containsExactly(updatedJob);
        assertThat(updatedOutput.workCalendar()).isEqualTo(updatedWorkCalendar);
        assertThat(updatedOutput.score()).isEqualTo("0hard/0medium/-1soft");
        assertThat(updatedInputMetrics.jobs()).isEqualTo(10);
        assertThat(updatedInputMetrics.lines()).isEqualTo(20);
        assertThat(updatedInputMetrics.operators()).isEqualTo(30);
        assertThat(updatedInputMetrics.products()).isEqualTo(40);
        assertThat(updatedOutputMetrics.totalAssignedJobs()).isEqualTo(10);
        assertThat(updatedOutputMetrics.totalUnassignedJobs()).isEqualTo(20);
        assertThat(updatedOutputMetrics.totalUsedLines()).isEqualTo(30);
        assertThat(updatedOutputMetrics.makespanMinutes()).isEqualTo(40);
    }
}
