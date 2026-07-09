package org.acme.tournamentschedule.domain;

import ai.timefold.solver.core.api.domain.common.PlanningId;
import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.entity.PlanningPin;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;

/**
 * A slot on a {@link Day} to which the solver assigns a {@link Team}.
 */
@PlanningEntity
public class TeamAssignment {

    @PlanningId
    private String id;
    private Day day;
    private int indexInDay;
    @PlanningPin
    private boolean pinned;

    @PlanningVariable
    private Team team;

    public TeamAssignment() {
    }

    public TeamAssignment(String id) {
        this.id = id;
    }

    public TeamAssignment(String id, Day day, int indexInDay) {
        this(id);
        this.day = day;
        this.indexInDay = indexInDay;
    }

    public TeamAssignment(String id, Day day, int indexInDay, Team team) {
        this(id, day, indexInDay);
        this.team = team;
    }

    public boolean isAssigned() {
        return team != null;
    }

    public String getId() {
        return id;
    }

    public Day getDay() {
        return day;
    }

    public int getIndexInDay() {
        return indexInDay;
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
        return "Round-" + day.getDateIndex() + "(" + indexInDay + ")";
    }
}
