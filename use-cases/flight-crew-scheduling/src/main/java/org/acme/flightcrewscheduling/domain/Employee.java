package org.acme.flightcrewscheduling.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import ai.timefold.solver.core.api.domain.common.PlanningId;

public record Employee(
        @PlanningId String id,
        String name,
        Airport homeAirport,
        List<String> skills,
        List<LocalDate> unavailableDays) {

    public boolean hasSkill(String skill) {
        return skills.contains(skill);
    }

    public boolean isAvailable(LocalDate fromDateInclusive, LocalDate toDateInclusive) {
        return fromDateInclusive
                .datesUntil(toDateInclusive.plusDays(1))
                .noneMatch(unavailableDays::contains);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Employee employee)) {
            return false;
        }
        return Objects.equals(id, employee.id);
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
