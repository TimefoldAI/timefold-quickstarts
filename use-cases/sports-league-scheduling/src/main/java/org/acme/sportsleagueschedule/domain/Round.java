package org.acme.sportsleagueschedule.domain;

import ai.timefold.solver.core.api.domain.common.PlanningId;

/**
 * One matchday of the league. Rounds are played back to back, so their {@link #getIndex() index}
 * doubles as the moment in time a match assigned to it takes place.
 */
public class Round {

    @PlanningId
    private int index;
    // Rounds scheduled on weekends and holidays. It's common for classic matches to be scheduled on weekends or holidays.
    private boolean weekendOrHoliday;

    public Round() {
    }

    public Round(int index) {
        this.index = index;
    }

    public Round(int index, boolean weekendOrHoliday) {
        this(index);
        this.weekendOrHoliday = weekendOrHoliday;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public boolean isWeekendOrHoliday() {
        return weekendOrHoliday;
    }

    public void setWeekendOrHoliday(boolean weekendOrHoliday) {
        this.weekendOrHoliday = weekendOrHoliday;
    }

    @Override
    public String toString() {
        return "Round-" + index;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Round round)) {
            return false;
        }
        return getIndex() == round.getIndex();
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(index);
    }
}
