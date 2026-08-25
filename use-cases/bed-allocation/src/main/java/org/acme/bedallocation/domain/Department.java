package org.acme.bedallocation.domain;

import java.util.List;
import java.util.Map;

import ai.timefold.solver.core.api.domain.common.PlanningId;

public record Department(
        @PlanningId String id,
        String name,
        Integer minimumAge,
        Integer maximumAge,
        Map<String, Integer> specialtyToPriority,
        List<Room> rooms) {

    @Override
    public String toString() {
        return id;
    }
}
