package org.acme.foodpackaging.domain;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.entity.PlanningPin;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.api.domain.variable.CascadingUpdateShadowVariable;
import ai.timefold.solver.core.api.domain.variable.InverseRelationShadowVariable;
import ai.timefold.solver.core.api.domain.variable.NextElementShadowVariable;
import ai.timefold.solver.core.api.domain.variable.PreviousElementShadowVariable;
import ai.timefold.solver.core.api.domain.variable.ShadowVariable;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.Duration;
import java.time.LocalDateTime;

@PlanningEntity
public class Job {

    @PlanningId
    private String id;
    private String name;

    private Product product;
    private Duration duration;
    private LocalDateTime minStartTime;
    private LocalDateTime idealEndTime;
    private LocalDateTime maxEndTime;
    /**
     * Higher priority is a higher number.
     */
    private int priority;
    @PlanningPin
    private boolean pinned;

    @InverseRelationShadowVariable(sourceVariableName = "jobs")
    private Line line;
    @ShadowVariable(
            variableListenerClass = LineOperatorUpdatingVariableListener.class,
            sourceEntityClass = Line.class,
            sourceVariableName = "operator")
    @ShadowVariable(
            variableListenerClass = JobOperatorUpdatingVariableListener.class,
            sourceVariableName = "line")
    private Operator lineOperator;
    @JsonIgnore
    @PreviousElementShadowVariable(sourceVariableName = "jobs")
    private Job previousJob;
    @JsonIgnore
    @NextElementShadowVariable(sourceVariableName = "jobs")
    private Job nextJob;

    /**
     * Start is after cleanup.
     */
    @CascadingUpdateShadowVariable(targetMethodName = "updateStartCleaningDateTime")
    private LocalDateTime startCleaningDateTime;
    @CascadingUpdateShadowVariable(targetMethodName = "updateStartCleaningDateTime")
    private LocalDateTime startProductionDateTime;
    @CascadingUpdateShadowVariable(targetMethodName = "updateStartCleaningDateTime")
    private LocalDateTime endDateTime;

    // No-arg constructor required for Timefold
    public Job() {
    }

    public Job(String id, String name, Product product, Duration duration, LocalDateTime minStartTime, LocalDateTime idealEndTime, LocalDateTime maxEndTime, int priority, boolean pinned) {
        this(id, name, product, duration, minStartTime, idealEndTime, maxEndTime, priority, pinned, null, null);
    }

    public Job(String id, String name, Product product, Duration duration, LocalDateTime minStartTime, LocalDateTime idealEndTime, LocalDateTime maxEndTime, int priority, boolean pinned,
               LocalDateTime startCleaningDateTime, LocalDateTime startProductionDateTime) {
        this.id = id;
        this.name = name;
        this.product = product;
        this.duration = duration;
        this.minStartTime = minStartTime;
        this.idealEndTime = idealEndTime;
        this.maxEndTime = maxEndTime;
        this.priority = priority;
        this.startCleaningDateTime = startCleaningDateTime;
        this.startProductionDateTime = startProductionDateTime;
        this.endDateTime = startProductionDateTime == null ? null : startProductionDateTime.plus(duration);
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

    public LocalDateTime getMinStartTime() {
        return minStartTime;
    }

    public LocalDateTime getIdealEndTime() {
        return idealEndTime;
    }

    public LocalDateTime getMaxEndTime() {
        return maxEndTime;
    }

    public int getPriority() {
        return priority;
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

    public LocalDateTime getStartCleaningDateTime() {
        return startCleaningDateTime;
    }

    public void setStartCleaningDateTime(LocalDateTime startCleaningDateTime) {
        this.startCleaningDateTime = startCleaningDateTime;
    }

    public LocalDateTime getStartProductionDateTime() {
        return startProductionDateTime;
    }

    public void setStartProductionDateTime(LocalDateTime startProductionDateTime) {
        this.startProductionDateTime = startProductionDateTime;
    }

    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(LocalDateTime endDateTime) {
        this.endDateTime = endDateTime;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

    @SuppressWarnings("unused")
    private void updateStartCleaningDateTime() {
        if (getLine() == null) {
            if (getStartCleaningDateTime() != null) {
                setStartCleaningDateTime(null);
                setStartProductionDateTime(null);
                setEndDateTime(null);
            }
            return;
        }
        Job previous = getPreviousJob();
        LocalDateTime startCleaning;
        LocalDateTime startProduction;
        if (previous == null) {
            startCleaning = line.getStartDateTime();
            startProduction = line.getStartDateTime();
        } else {
            startCleaning = previous.getEndDateTime();
            startProduction = startCleaning == null ? null : startCleaning.plus(getProduct().getCleanupDuration(previous.getProduct()));
        }
        setStartCleaningDateTime(startCleaning);
        setStartProductionDateTime(startProduction);
        var endTime = startProduction == null ? null : startProduction.plus(getDuration());
        setEndDateTime(endTime);
    }
}
