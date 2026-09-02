package org.acme.maintenancescheduling.domain;

import java.util.Objects;

import ai.timefold.solver.core.api.domain.common.PlanningId;

public record Crew(
        @PlanningId String id,
        String name) {

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Crew crew)) {
            return false;
        }
        return Objects.equals(id, crew.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return name;
    }
}
