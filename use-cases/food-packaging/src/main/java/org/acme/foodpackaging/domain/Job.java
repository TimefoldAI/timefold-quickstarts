package org.acme.foodpackaging.domain;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Objects;

import ai.timefold.solver.core.api.domain.common.PlanningId;
import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.entity.PlanningPin;
import ai.timefold.solver.core.api.domain.variable.InverseRelationShadowVariable;
import ai.timefold.solver.core.api.domain.variable.NextElementShadowVariable;
import ai.timefold.solver.core.api.domain.variable.PreviousElementShadowVariable;
import ai.timefold.solver.core.api.domain.variable.ShadowSources;
import ai.timefold.solver.core.api.domain.variable.ShadowVariable;

/**
 * One packaging run of a single product. A job is scheduled by putting it in a {@link Line}'s job
 * sequence; everything else about its timing is derived from that position by the shadow variables
 * below.
 */
@PlanningEntity
public class Job {

    @PlanningId
    private String id;
    private String name;

    private Product product;
    private Duration duration;
    private OffsetDateTime minStartTime;
    private OffsetDateTime idealEndTime;
    private OffsetDateTime maxEndTime;
    @PlanningPin
    private boolean pinned;

    @InverseRelationShadowVariable(sourceVariableName = "jobs")
    private Line line;

    @ShadowVariable(supplierName = "lineOperatorSupplier")
    private Operator lineOperator;
    @PreviousElementShadowVariable(sourceVariableName = "jobs")
    private Job previousJob;
    @NextElementShadowVariable(sourceVariableName = "jobs")
    private Job nextJob;

    /**
     * Start is after cleanup.
     */
    @ShadowVariable(supplierName = "startCleaningDateTimeSupplier")
    private OffsetDateTime startCleaningDateTime;
    @ShadowVariable(supplierName = "startProductionDateTimeSupplier")
    private OffsetDateTime startProductionDateTime;
    @ShadowVariable(supplierName = "endDateTimeSupplier")
    private OffsetDateTime endDateTime;

    // No-arg constructor required for Timefold
    public Job() {
    }

    public Job(String id, String name, Product product, Duration duration, OffsetDateTime minStartTime,
            OffsetDateTime idealEndTime, OffsetDateTime maxEndTime, boolean pinned) {
        this.id = id;
        this.name = name;
        this.product = product;
        this.duration = duration;
        this.minStartTime = minStartTime;
        this.idealEndTime = idealEndTime;
        this.maxEndTime = maxEndTime;
        this.pinned = pinned;
    }

    @Override
    public String toString() {
        return id + "(" + product.getName() + ")";
    }

    // ************************************************************************
    // Getters and setters
    // ************************************************************************

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Product getProduct() {
        return product;
    }

    public Duration getDuration() {
        return duration;
    }

    public OffsetDateTime getMinStartTime() {
        return minStartTime;
    }

    public OffsetDateTime getIdealEndTime() {
        return idealEndTime;
    }

    public OffsetDateTime getMaxEndTime() {
        return maxEndTime;
    }

    public boolean isPinned() {
        return pinned;
    }

    public Line getLine() {
        return line;
    }

    public void setLine(Line line) {
        this.line = line;
    }

    public Operator getLineOperator() {
        return lineOperator;
    }

    public void setLineOperator(Operator lineOperator) {
        this.lineOperator = lineOperator;
    }

    public Job getPreviousJob() {
        return previousJob;
    }

    public void setPreviousJob(Job previousJob) {
        this.previousJob = previousJob;
    }

    public Job getNextJob() {
        return nextJob;
    }

    public void setNextJob(Job nextJob) {
        this.nextJob = nextJob;
    }

    public OffsetDateTime getStartCleaningDateTime() {
        return startCleaningDateTime;
    }

    public void setStartCleaningDateTime(OffsetDateTime startCleaningDateTime) {
        this.startCleaningDateTime = startCleaningDateTime;
    }

    public OffsetDateTime getStartProductionDateTime() {
        return startProductionDateTime;
    }

    public void setStartProductionDateTime(OffsetDateTime startProductionDateTime) {
        this.startProductionDateTime = startProductionDateTime;
    }

    public OffsetDateTime getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(OffsetDateTime endDateTime) {
        this.endDateTime = endDateTime;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

    @SuppressWarnings("unused")
    @ShadowSources({ "line", "line.operator" })
    public Operator lineOperatorSupplier() {
        if (line == null) {
            return null;
        }
        return line.getOperator();
    }

    @SuppressWarnings("unused")
    @ShadowSources({ "line", "previousJob.endDateTime" })
    public OffsetDateTime startCleaningDateTimeSupplier() {
        if (line == null) {
            return null;
        }
        if (previousJob == null) {
            return line.getStartDateTime();
        }
        return previousJob.getEndDateTime();
    }

    @SuppressWarnings("unused")
    @ShadowSources({ "line", "startCleaningDateTime" })
    public OffsetDateTime startProductionDateTimeSupplier() {
        if (line == null) {
            return null;
        }
        if (previousJob == null) {
            return line.getStartDateTime();
        }
        return startCleaningDateTime == null ? null
                : startCleaningDateTime.plus(product.getCleanupDuration(previousJob.getProduct()));
    }

    @SuppressWarnings("unused")
    @ShadowSources({ "startProductionDateTime" })
    public OffsetDateTime endDateTimeSupplier() {
        return startProductionDateTime == null ? null : startProductionDateTime.plus(duration);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Job job)) {
            return false;
        }
        return Objects.equals(id, job.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
