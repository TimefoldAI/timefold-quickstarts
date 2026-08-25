package org.acme.bedallocation.domain;

import java.util.List;
import java.util.Set;

import ai.timefold.solver.core.api.domain.common.PlanningId;

public record Room(
        @PlanningId String id,
        String name,
        Department department,
        int capacity,
        GenderLimitation genderLimitation,
        Set<String> equipments,
        List<Bed> beds) {

    @Override
    public String toString() {
        return id;
    }
}
