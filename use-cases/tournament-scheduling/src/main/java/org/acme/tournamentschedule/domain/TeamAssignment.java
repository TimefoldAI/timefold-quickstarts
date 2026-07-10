package org.acme.tournamentschedule.domain;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.entity.PlanningPin;
import ai.timefold.solver.core.api.domain.common.PlanningId;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;

@PlanningEntity
public class TeamAssignment {

    @PlanningId
    private long id;
    private Day day;
    private int indexInDate;
    @PlanningPin
    private boolean pinned;

    @PlanningVariable
    private Team team;

    public TeamAssignment() {
    }

    public TeamAssignment(long id) {
        this.id = id;
    }

    public TeamAssignment(long id, Day day, int indexInDate) {
        this(id);
        this.day = day;
        this.indexInDate = indexInDate;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Day getDay() {
        return day;
    }

    public void setDay(Day day) {
        this.day = day;
    }

    public int getIndexInDay() {
        return indexInDate;
    }

    public void setIndexInDay(int indexInDate) {
        this.indexInDate = indexInDate;
    }

    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    @Override
    public String toString() {
        return "Round-" + day.getDateIndex() + "(" + indexInDate + ")";
    }

}
