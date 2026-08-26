package org.acme.bedallocation.domain;

import java.util.Map;

import ai.timefold.solver.core.api.domain.common.PlanningId;

public record Department(
        @PlanningId String id,
        String name,
        Integer minimumAge,
        Integer maximumAge,
        Map<String, Integer> specialtyToPriority) {

    @Override
    public String toString() {
        return id;
    }
}
