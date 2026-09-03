package org.acme.maintenancescheduling.domain;

import java.time.LocalDate;
import java.util.Objects;

/**
 * The window of workdays that jobs can be scheduled in.
 *
 * @param fromDate inclusive
 * @param toDate exclusive
 */
public record WorkCalendar(
        String id,
        LocalDate fromDate,
        LocalDate toDate) {

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof WorkCalendar workCalendar)) {
            return false;
        }
        return Objects.equals(id, workCalendar.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return fromDate + " - " + toDate;
    }
}
