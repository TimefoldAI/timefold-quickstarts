package org.acme.employeescheduling.domain;

import java.time.LocalDate;
import java.util.Objects;
import java.util.SequencedSet;

import ai.timefold.solver.core.api.domain.common.PlanningId;

/**
 * An employee, identified by their unique name, who can work {@link Shift shifts} that require one of their {@link #skills()}.
 * <p>
 * The dates are calendar days in the time zone offset of the shifts they are compared to.
 *
 * @param unavailableDates days the employee cannot work at all
 * @param undesiredDates days the employee would rather not work
 * @param desiredDates days the employee would like to work
 */
public record Employee(@PlanningId String name, SequencedSet<String> skills,
        SequencedSet<LocalDate> unavailableDates, SequencedSet<LocalDate> undesiredDates,
        SequencedSet<LocalDate> desiredDates) {

    public boolean hasSkill(String skill) {
        return skills.contains(skill);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Employee employee)) {
            return false;
        }
        return Objects.equals(name, employee.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }
}
