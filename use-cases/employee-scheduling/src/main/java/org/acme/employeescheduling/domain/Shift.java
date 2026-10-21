package org.acme.employeescheduling.domain;

import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Objects;

import ai.timefold.solver.core.api.domain.common.PlanningId;
import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;

/**
 * A shift at one location that needs one employee with the {@link #getRequiredSkill() required skill}.
 * The solver picks the {@link Employee} who works it.
 */
@PlanningEntity
public class Shift {

    @PlanningId
    private String id;

    private OffsetDateTime start;
    private OffsetDateTime end;

    private String location;
    private String requiredSkill;

    @PlanningVariable
    private Employee employee;

    public Shift() {
    }

    public Shift(String id, OffsetDateTime start, OffsetDateTime end, String location, String requiredSkill,
            Employee employee) {
        this.id = id;
        this.start = start;
        this.end = end;
        this.location = location;
        this.requiredSkill = requiredSkill;
        this.employee = employee;
    }

    // ************************************************************************
    // Getters and setters
    // ************************************************************************

    public String getId() {
        return id;
    }

    public OffsetDateTime getStart() {
        return start;
    }

    public OffsetDateTime getEnd() {
        return end;
    }

    public String getLocation() {
        return location;
    }

    public String getRequiredSkill() {
        return requiredSkill;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

    public boolean isAssigned() {
        return employee != null;
    }

    public LocalDate getStartDate() {
        return start.toLocalDate();
    }

    /**
     * @param other never null
     * @return the number of minutes both shifts take place at the same time, or 0 if they don't overlap
     */
    public int getOverlappingMinutes(Shift other) {
        return overlappingMinutes(start, end, other.start, other.end);
    }

    /**
     * @param later never null, a shift that starts after this one ends
     * @return the number of minutes between the end of this shift and the start of the later one
     */
    public int getMinutesUntil(Shift later) {
        return (int) Duration.between(end, later.start).toMinutes();
    }

    /**
     * @param date never null, a calendar day in the time zone offset of this shift
     * @return true if part of this shift takes place on that day
     */
    public boolean isOverlappingWithDate(LocalDate date) {
        return getOverlappingMinutes(date) > 0;
    }

    /**
     * @param date never null, a calendar day in the time zone offset of this shift
     * @return the number of minutes of this shift that take place on that day
     */
    public int getOverlappingMinutes(LocalDate date) {
        OffsetDateTime dayStart = date.atStartOfDay().atOffset(start.getOffset());
        return overlappingMinutes(start, end, dayStart, dayStart.plusDays(1));
    }

    private static int overlappingMinutes(OffsetDateTime start1, OffsetDateTime end1, OffsetDateTime start2,
            OffsetDateTime end2) {
        // Both periods are active after the later of their two starts and before the earlier of their two ends.
        OffsetDateTime overlapStart = start1.isAfter(start2) ? start1 : start2;
        OffsetDateTime overlapEnd = end1.isBefore(end2) ? end1 : end2;
        long minutes = Duration.between(overlapStart, overlapEnd).toMinutes();
        return minutes > 0 ? (int) minutes : 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Shift shift)) {
            return false;
        }
        return Objects.equals(id, shift.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return location + " " + start + "-" + end;
    }
}
