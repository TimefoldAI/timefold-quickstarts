package org.acme.foodpackaging.domain;

import java.time.LocalDate;

public class WorkCalendar {

    private LocalDate fromDate; // Inclusive
    private LocalDate toDate; // Exclusive

    public WorkCalendar() {
    }

    public WorkCalendar(LocalDate fromDate, LocalDate toDate) {
        this.fromDate = fromDate;
        this.toDate = toDate;
    }

    @Override
    public String toString() {
        return fromDate + " - " + toDate;
    }

    // ************************************************************************
    // Getters and setters
    // ************************************************************************

    public LocalDate getFromDate() {
        return fromDate;
    }

    public LocalDate getToDate() {
        return toDate;
    }

}
