package org.acme.projectjobschedule.domain;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

import ai.timefold.solver.core.api.domain.common.PlanningId;
import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.entity.PlanningPin;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;
import ai.timefold.solver.core.api.domain.variable.ShadowSources;
import ai.timefold.solver.core.api.domain.variable.ShadowVariable;

import org.acme.projectjobschedule.domain.solver.DelayStrengthComparator;

/**
 * Schedules exactly one {@link Job}: it picks the {@link ExecutionMode} the job is executed in and
 * the {@link #getDelay() delay} it waits on top of the date its predecessors are done.
 * <p>
 * All dates are day numbers counted from the start of the schedule, not calendar dates.
 */
@PlanningEntity
public class Allocation {

    /** The number of days a job may be delayed past the date its predecessors are done. */
    private static final int MAX_DELAY = 500;

    @PlanningId
    private String id;

    @PlanningPin
    private boolean pinned = false;

    private Job job;

    private Allocation sourceAllocation;
    private Allocation sinkAllocation;
    private List<Allocation> predecessorAllocations;
    private List<Allocation> successorAllocations;

    // Planning variables: changes during planning, between score calculations.
    @PlanningVariable
    private ExecutionMode executionMode;
    @PlanningVariable(comparatorClass = DelayStrengthComparator.class)
    private Integer delay; // In days

    // Shadow variables

    @ShadowVariable(supplierName = "predecessorsDoneDateSupplier")
    private Integer predecessorsDoneDate;

    // Filled from shadow variables
    @ShadowVariable(supplierName = "startDateSupplier")
    private Integer startDate;
    @ShadowVariable(supplierName = "endDateSupplier")
    private Integer endDate;
    @ShadowVariable(supplierName = "busyDatesSupplier")
    private List<Integer> busyDates;

    public Allocation() {
    }

    public Allocation(String id) {
        this.id = id;
    }

    public Allocation(String id, Job job) {
        this(id);
        setJob(job);
        this.predecessorsDoneDate = 0;
    }

    // ************************************************************************
    // Ranges
    // ************************************************************************

    @ValueRangeProvider
    public List<ExecutionMode> getExecutionModeRange() {
        return job.executionModes();
    }

    @ValueRangeProvider
    public List<Integer> getDelayRange() {
        return IntStream.range(0, MAX_DELAY).boxed().toList();
    }

    // ************************************************************************
    // Get and Set
    // ************************************************************************

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isPinned() {
        return pinned;
    }

    /**
     * Sources and sinks will always be pinned.
     *
     * @param pinned Ignored unless {@link #getJob()}'s {@link JobType} is {@link JobType#STANDARD}.
     */
    public void setPinned(boolean pinned) {
        if (job == null) {
            this.pinned = false;
            return;
        }
        var jobType = getJobType();
        if (jobType == JobType.STANDARD) {
            this.pinned = pinned;
        } else {
            this.pinned = true;
        }
    }

    public Job getJob() {
        return job;
    }

    /**
     * Resets {@link #isPinned()}.
     *
     * @param job never null
     */
    public void setJob(Job job) {
        this.job = job;
        setPinned(false);
    }

    public Allocation getSourceAllocation() {
        return sourceAllocation;
    }

    public void setSourceAllocation(Allocation sourceAllocation) {
        this.sourceAllocation = sourceAllocation;
    }

    public Allocation getSinkAllocation() {
        return sinkAllocation;
    }

    public void setSinkAllocation(Allocation sinkAllocation) {
        this.sinkAllocation = sinkAllocation;
    }

    public List<Allocation> getPredecessorAllocations() {
        return predecessorAllocations;
    }

    public void setPredecessorAllocations(List<Allocation> predecessorAllocations) {
        this.predecessorAllocations = predecessorAllocations;
    }

    public List<Allocation> getSuccessorAllocations() {
        return successorAllocations;
    }

    public void setSuccessorAllocations(List<Allocation> successorAllocations) {
        this.successorAllocations = successorAllocations;
    }

    public ExecutionMode getExecutionMode() {
        return executionMode;
    }

    public void setExecutionMode(ExecutionMode executionMode) {
        this.executionMode = executionMode;
    }

    public Integer getDelay() {
        return delay;
    }

    public void setDelay(Integer delay) {
        this.delay = delay;
    }

    public Integer getPredecessorsDoneDate() {
        return predecessorsDoneDate;
    }

    public void setPredecessorsDoneDate(Integer predecessorsDoneDate) {
        this.predecessorsDoneDate = predecessorsDoneDate;
    }

    public Integer getStartDate() {
        return startDate;
    }

    public void setStartDate(Integer startDate) {
        this.startDate = startDate;
    }

    public Integer getEndDate() {
        return endDate;
    }

    public void setEndDate(Integer endDate) {
        this.endDate = endDate;
    }

    public List<Integer> getBusyDates() {
        return busyDates;
    }

    public void setBusyDates(List<Integer> busyDates) {
        this.busyDates = busyDates;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

    @SuppressWarnings("unused")
    @ShadowSources("predecessorAllocations[].endDate")
    public Integer predecessorsDoneDateSupplier() {
        // No job of a project can start before the project is released, so that is also when the source is done.
        int doneDate = job.project().releaseDate();
        if (predecessorAllocations == null) {
            return doneDate;
        }
        for (Allocation predecessorAllocation : predecessorAllocations) {
            int endDate = predecessorAllocation.getEndDate();
            doneDate = Math.max(doneDate, endDate);
        }
        return doneDate;
    }

    @SuppressWarnings("unused")
    @ShadowSources({ "predecessorsDoneDate", "delay" })
    public Integer startDateSupplier() {
        return predecessorsDoneDate + Objects.requireNonNullElse(delay, 0);
    }

    @SuppressWarnings("unused")
    @ShadowSources({ "startDate", "executionMode" })
    public Integer endDateSupplier() {
        return getStartDate() + (executionMode == null ? 0 : executionMode.duration());
    }

    @SuppressWarnings("unused")
    @ShadowSources({ "startDate", "endDate" })
    public List<Integer> busyDatesSupplier() {
        var start = getStartDate();
        var end = getEndDate();
        var dates = new Integer[end - start];
        for (int i = 0; i < dates.length; i++) {
            dates[i] = start + i;
        }
        return Arrays.asList(dates);
    }

    /**
     * Recomputes every date that follows from {@link #getPredecessorsDoneDate()}, the way the solver
     * would once it has assigned the planning variables. Only needed where no solver is involved,
     * e.g. to seed the shadow variables of a hand-built schedule.
     */
    public void updateShadowsAfterPredecessorDoneDate() {
        startDate = startDateSupplier();
        endDate = endDateSupplier();
        busyDates = busyDatesSupplier();
    }

    public Project getProject() {
        return job.project();
    }

    public int getProjectCriticalPathEndDate() {
        return job.project().criticalPathEndDate();
    }

    /** The number of days this allocation ends past its project's critical path end date. */
    public int getProjectDelay() {
        return getEndDate() - getProjectCriticalPathEndDate();
    }

    public JobType getJobType() {
        return job.jobType();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Allocation that)) {
            return false;
        }
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Allocation-" + id;
    }
}
