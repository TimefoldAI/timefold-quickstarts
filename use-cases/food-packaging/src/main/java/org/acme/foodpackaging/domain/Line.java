package org.acme.foodpackaging.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import ai.timefold.solver.core.api.domain.common.PlanningId;
import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.variable.PlanningListVariable;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;

@PlanningEntity
public class Line {

    @PlanningId
    private String id;
    private String name;
    private LocalDateTime startDateTime;

    @PlanningVariable
    private Operator operator;

    @PlanningListVariable(allowsUnassignedValues = true)
    private List<Job> jobs;

    // No-arg constructor required for Timefold
    public Line() {
    }

    public Line(String id, String name, LocalDateTime startDateTime) {
        this(id, name, null, startDateTime);
    }

    public Line(String id, String name, Operator operator, LocalDateTime startDateTime) {
        this.id = id;
        this.name = name;
        this.operator = operator;
        this.startDateTime = startDateTime;
        jobs = new ArrayList<>();
    }

    @Override
    public String toString() {
        return name;
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

    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    public Operator getOperator() {
        return operator;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    public List<Job> getJobs() {
        return jobs;
    }

    public void setJobs(List<Job> jobs) {
        this.jobs = jobs;
    }
}
