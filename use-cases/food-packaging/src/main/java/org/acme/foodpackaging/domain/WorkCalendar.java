package org.acme.foodpackaging.domain;

import java.time.LocalDate;

/**
 * The window the schedule covers: production starts on or after {@code fromDate} and the schedule is
 * reported up to (but excluding) {@code toDate}.
 */
public record WorkCalendar(LocalDate fromDate, LocalDate toDate) {

    @Override
    public String toString() {
        return fromDate + " - " + toDate;
    }
}
