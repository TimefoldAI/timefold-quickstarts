package org.acme.foodpackaging.domain;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.api.domain.variable.PlanningListVariable;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@JsonIdentityInfo(scope = Line.class, generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@PlanningEntity
public class Line {

    @PlanningId
    private String id;
    private String name;
    private LocalDateTime startDateTime;

    @JsonIdentityReference(alwaysAsId = true)
    @PlanningVariable
    private Operator operator;

    @JsonIgnore
    @PlanningListVariable
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

    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    public Operator getOperator() {
        return operator;
    }

    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    public List<Job> getJobs() {
        return jobs;
    }
}
