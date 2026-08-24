package org.acme.bedallocation.domain;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import ai.timefold.solver.core.api.domain.common.PlanningId;

public record Department(
        @PlanningId String id,
        String name,
        Integer minimumAge,
        Integer maximumAge,
        Map<String, Integer> specialtyToPriority,
        List<Room> rooms) {

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Department department)) {
            return false;
        }
        return Objects.equals(id, department.id);
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
