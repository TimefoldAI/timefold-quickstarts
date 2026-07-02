package org.acme.tournamentschedule.domain;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(scope = Day.class, generator = ObjectIdGenerators.PropertyGenerator.class, property = "dateIndex")
public class Day {

    private int dateIndex;

    public Day() {
    }

    public Day(int dateIndex) {
        this.dateIndex = dateIndex;
    }

    public int getDateIndex() {
        return dateIndex;
    }

    public void setDateIndex(int dateIndex) {
        this.dateIndex = dateIndex;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Day day))
            return false;
        return getDateIndex() == day.getDateIndex();
    }

    @Override
    public int hashCode() {
        return 31 * dateIndex;
    }
}
