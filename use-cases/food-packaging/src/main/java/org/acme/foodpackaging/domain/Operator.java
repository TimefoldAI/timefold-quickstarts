package org.acme.foodpackaging.domain;

import java.util.Objects;

import ai.timefold.solver.core.api.domain.common.PlanningId;

/**
 * The person operating one or more production lines. An operator has to be present during a line's
 * changeover cleaning, so the same operator must not have to clean two lines at the same time.
 */
public record Operator(@PlanningId String id, String name) {

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Operator operator)) {
            return false;
        }
        return Objects.equals(id, operator.id);
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
