package org.acme.tournamentschedule.domain;

import ai.timefold.solver.core.api.domain.common.PlanningId;

/**
 * A day (round) on which {@link TeamAssignment}s take place.
 */
public class Day {

    @PlanningId
    private int dateIndex;

    public Day() {
    }

    public Day(int dateIndex) {
        this.dateIndex = dateIndex;
    }

    public int getDateIndex() {
        return dateIndex;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Day day)) {
            return false;
        }
        return dateIndex == day.dateIndex;
    }

    @Override
    public int hashCode() {
        return 31 * dateIndex;
    }

    @Override
    public String toString() {
        return "Day-" + dateIndex;
    }
}
