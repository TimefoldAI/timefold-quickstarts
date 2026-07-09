package org.acme.foodpackaging.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ai.timefold.solver.core.api.domain.common.PlanningId;
import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.variable.InverseRelationShadowVariable;

@PlanningEntity
public class Operator {

    @PlanningId
    private String id;

    @InverseRelationShadowVariable(sourceVariableName = "operator")
    private List<Line> lines;

    // No-arg constructor required for Timefold
    public Operator() {
    }

    public Operator(String id) {
        this.id = id;
        this.lines = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Line> getLines() {
        return lines;
    }

    public void setLines(List<Line> lines) {
        this.lines = lines;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Operator operator)) {
            return false;
        }
        return Objects.equals(getId(), operator.getId());
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return id;
    }
}
