package org.acme.schooltimetabling.domain;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Objects;

import ai.timefold.solver.core.api.domain.common.PlanningId;

/**
 * A recurring period in the weekly timetable, on {@link #dayOfWeek()} from {@link #startTime()} to
 * {@link #endTime()}.
 */
public record Timeslot(@PlanningId String id, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Timeslot timeslot)) {
            return false;
        }
        return Objects.equals(id, timeslot.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return dayOfWeek + " " + startTime;
    }
}
