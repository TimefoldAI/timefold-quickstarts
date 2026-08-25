package org.acme.bedallocation.domain;

import ai.timefold.solver.core.api.domain.common.PlanningId;

public record Bed(
        @PlanningId String id,
        Room room) {

    @Override
    public String toString() {
        return id;
    }
}
