package org.acme.schooltimetabling.domain;

import java.util.Objects;

import ai.timefold.solver.core.api.domain.common.PlanningId;

/**
 * A room lessons are taught in. It holds one lesson per {@link Timeslot}.
 */
public record Room(@PlanningId String id, String name) {

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Room room)) {
            return false;
        }
        return Objects.equals(id, room.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }
}
