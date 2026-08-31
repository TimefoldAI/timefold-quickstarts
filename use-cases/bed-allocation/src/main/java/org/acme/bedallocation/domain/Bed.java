package org.acme.bedallocation.domain;

import java.util.Objects;

import ai.timefold.solver.core.api.domain.common.PlanningId;

public record Bed(
        @PlanningId String id,
        Room room) {

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Bed bed)) {
            return false;
        }
        return Objects.equals(id, bed.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return id;
    }
}
