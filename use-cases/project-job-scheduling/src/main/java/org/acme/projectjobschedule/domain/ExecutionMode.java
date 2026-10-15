package org.acme.projectjobschedule.domain;

import java.util.Objects;

import ai.timefold.solver.core.api.domain.common.PlanningId;

/**
 * One way of executing a {@link Job}: it takes {@link #duration()} days and consumes the resources
 * listed by the {@link ResourceRequirement}s that point at it.
 */
public record ExecutionMode(@PlanningId String id, int duration) {

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ExecutionMode executionMode)) {
            return false;
        }
        return Objects.equals(id, executionMode.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "ExecutionMode-" + id;
    }
}
