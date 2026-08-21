package org.acme.bedallocation.domain;

import java.util.List;
import java.util.Objects;

import ai.timefold.solver.core.api.domain.common.PlanningId;

public record Room(
        @PlanningId String id,
        String name,
        Department department,
        int capacity,
        GenderLimitation genderLimitation,
        List<String> equipments,
        List<Bed> beds) {

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
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return name;
    }
}
