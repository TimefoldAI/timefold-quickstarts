package org.acme.foodpackaging.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import com.fasterxml.jackson.annotation.JsonIgnore;
import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.variable.PlanningListVariable;

@PlanningEntity
public class Line {

    @PlanningId
    private String id;
    private String name;
    private String operator;
    private LocalDateTime startDateTime;

    @JsonIgnore
    @PlanningListVariable
    private List<Job> jobs;

    // No-arg constructor required for Timefold
    public Line() {
    }

    public Line(String id, String name, String operator, LocalDateTime startDateTime) {
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

    public String getOperator() {
        return operator;
    }

    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    public List<Job> getJobs() {
        return jobs;
    }

}
