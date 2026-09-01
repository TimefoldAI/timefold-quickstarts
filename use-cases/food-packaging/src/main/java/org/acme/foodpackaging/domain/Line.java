package org.acme.foodpackaging.domain;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ai.timefold.solver.core.api.domain.common.PlanningId;
import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.variable.PlanningListVariable;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;

/**
 * A production line, which produces the jobs assigned to it one after the other, starting at
 * {@link #getStartDateTime()}. Both the operator running the line and the sequence of jobs on it are
 * decided by the solver.
 */
@PlanningEntity
public class Line {

    @PlanningId
    private String id;
    private String name;
    private OffsetDateTime startDateTime;

    @PlanningVariable
    private Operator operator;

    @PlanningListVariable(allowsUnassignedValues = true)
    private List<Job> jobs = new ArrayList<>();

    // No-arg constructor required for Timefold
    public Line() {
    }

    public Line(String id, String name, OffsetDateTime startDateTime) {
        this.id = id;
        this.name = name;
        this.startDateTime = startDateTime;
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

    public OffsetDateTime getStartDateTime() {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Line line)) {
            return false;
        }
        return Objects.equals(id, line.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
