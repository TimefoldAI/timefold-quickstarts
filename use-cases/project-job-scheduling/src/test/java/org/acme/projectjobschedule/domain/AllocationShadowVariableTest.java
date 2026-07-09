package org.acme.projectjobschedule.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

class AllocationShadowVariableTest {

    private static Allocation allocation(String id, int predecessorsDoneDate, Integer delay, ExecutionMode executionMode) {
        Project project = new Project("p", 0, 0);
        Job job = new Job(id, project, JobType.STANDARD);
        Allocation allocation = new Allocation(id, job);
        allocation.setPredecessorsDoneDate(predecessorsDoneDate);
        allocation.setDelay(delay);
        allocation.setExecutionMode(executionMode);
        return allocation;
    }

    @Test
    void predecessorsDoneDateSupplierWithoutPredecessors() {
        Allocation allocation = allocation("1", 0, 0, null);
        allocation.setPredecessorAllocations(null);
        assertThat(allocation.predecessorsDoneDateSupplier()).isZero();
    }

    @Test
    void predecessorsDoneDateSupplierTakesMaxEndDate() {
        Project project = new Project("p", 0, 0);
        Job job = new Job("job", project, JobType.STANDARD);
        ExecutionMode mode = new ExecutionMode("m", job, 3);

        Allocation first = new Allocation("pred1", job);
        first.setPredecessorsDoneDate(2);
        first.setDelay(0);
        first.setExecutionMode(mode);
        first.updateShadowsAfterPredecessorDoneDate();

        Allocation second = new Allocation("pred2", job);
        second.setPredecessorsDoneDate(5);
        second.setDelay(0);
        second.setExecutionMode(mode);
        second.updateShadowsAfterPredecessorDoneDate();

        Allocation allocation = new Allocation("main", job);
        allocation.setPredecessorAllocations(List.of(first, second));
        assertThat(allocation.predecessorsDoneDateSupplier()).isEqualTo(second.getEndDate());
    }

    @Test
    void startDateSupplierAddsDelay() {
        Allocation allocation = allocation("1", 4, 3, null);
        assertThat(allocation.startDateSupplier()).isEqualTo(7);
    }

    @Test
    void startDateSupplierHandlesNullDelay() {
        Allocation allocation = allocation("1", 4, null, null);
        assertThat(allocation.startDateSupplier()).isEqualTo(4);
    }

    @Test
    void endDateSupplierAddsDuration() {
        Project project = new Project("p", 0, 0);
        Job job = new Job("job", project, JobType.STANDARD);
        ExecutionMode mode = new ExecutionMode("m", job, 5);
        Allocation allocation = allocation("1", 2, 1, mode);
        allocation.setStartDate(allocation.startDateSupplier());
        assertThat(allocation.endDateSupplier()).isEqualTo(allocation.getStartDate() + 5);
    }

    @Test
    void endDateSupplierHandlesNullExecutionMode() {
        Allocation allocation = allocation("1", 2, 1, null);
        allocation.setStartDate(allocation.startDateSupplier());
        assertThat(allocation.endDateSupplier()).isEqualTo(allocation.getStartDate());
    }

    @Test
    void busyDatesSupplierListsEachDay() {
        Project project = new Project("p", 0, 0);
        Job job = new Job("job", project, JobType.STANDARD);
        ExecutionMode mode = new ExecutionMode("m", job, 3);
        Allocation allocation = allocation("1", 2, 0, mode);
        allocation.updateShadowsAfterPredecessorDoneDate();
        assertThat(allocation.busyDatesSupplier()).containsExactly(2, 3, 4);
    }
}
