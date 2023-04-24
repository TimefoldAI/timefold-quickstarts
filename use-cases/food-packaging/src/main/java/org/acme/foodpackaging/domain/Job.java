package org.acme.foodpackaging.domain;

import java.time.Duration;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.acme.foodpackaging.domain.solver.StartDateTimeUpdatingVariableListener;
import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.entity.PlanningPin;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.api.domain.variable.InverseRelationShadowVariable;
import ai.timefold.solver.core.api.domain.variable.NextElementShadowVariable;
import ai.timefold.solver.core.api.domain.variable.PiggybackShadowVariable;
import ai.timefold.solver.core.api.domain.variable.PreviousElementShadowVariable;
import ai.timefold.solver.core.api.domain.variable.ShadowVariable;

@PlanningEntity
public class Job {

    @PlanningId
    private Long id;
    private String name;

    private Product product;
    private Duration duration;
    private LocalDateTime readyDateTime;
    private LocalDateTime idealEndDateTime;
    private LocalDateTime dueDateTime;
    /** Higher priority is a higher number. */
    private int priority;
    @PlanningPin
    private boolean pinned;

    @InverseRelationShadowVariable(sourceVariableName = "jobList")
    private Line line;
    @JsonIgnore
    @PreviousElementShadowVariable(sourceVariableName = "jobList")
    private Job previousJob;
    @JsonIgnore
    @NextElementShadowVariable(sourceVariableName = "jobList")
    private Job nextJob;
    /** Start is after cleanup. */
    private LocalDateTime startCleaningDateTime;
    private LocalDateTime startProductionDateTime;
    private LocalDateTime endDateTime;

    // No-arg constructor required for OptaPlanner and Jackson
    public Job() {
    }

    public Job(Long id, String name, Product product, Duration duration, LocalDateTime readyDateTime, LocalDateTime idealEndDateTime, LocalDateTime dueDateTime, int priority, boolean pinned) {
        this(id, name, product, duration, readyDateTime, idealEndDateTime, dueDateTime, priority, pinned, null, null);
    }

    public Job(Long id, String name, Product product, Duration duration, LocalDateTime readyDateTime, LocalDateTime idealEndDateTime, LocalDateTime dueDateTime, int priority, boolean pinned,
            LocalDateTime startCleaningDateTime, LocalDateTime startProductionDateTime) {
        this.id = id;
        this.name = name;
        this.product = product;
        this.duration = duration;
        this.readyDateTime = readyDateTime;
        this.idealEndDateTime = idealEndDateTime;
        this.dueDateTime = dueDateTime;
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

    public Long getId() {
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

    public LocalDateTime getReadyDateTime() {
        return readyDateTime;
    }

    public LocalDateTime getIdealEndDateTime() {
        return idealEndDateTime;
    }

    public LocalDateTime getDueDateTime() {
        return dueDateTime;
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

    // TODO Move this to field after fixing https://github.com/TimefoldAI/timefold-solver/issues/7
    @ShadowVariable(variableListenerClass = StartDateTimeUpdatingVariableListener.class, sourceVariableName = "line")
    @ShadowVariable(variableListenerClass = StartDateTimeUpdatingVariableListener.class, sourceVariableName = "previousJob")
    public LocalDateTime getStartCleaningDateTime() {
        return startCleaningDateTime;
    }

    public void setStartCleaningDateTime(LocalDateTime startCleaningDateTime) {
        this.startCleaningDateTime = startCleaningDateTime;
    }

    // TODO Move this to field after fixing https://github.com/TimefoldAI/timefold-solver/issues/7
    @PiggybackShadowVariable(shadowVariableName = "startCleaningDateTime")
    public LocalDateTime getStartProductionDateTime() {
        return startProductionDateTime;
    }

    public void setStartProductionDateTime(LocalDateTime startProductionDateTime) {
        this.startProductionDateTime = startProductionDateTime;
    }

    // TODO Move this to field after fixing https://github.com/TimefoldAI/timefold-solver/issues/7
    @PiggybackShadowVariable(shadowVariableName = "startCleaningDateTime")
    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(LocalDateTime endDateTime) {
        this.endDateTime = endDateTime;
    }

}
