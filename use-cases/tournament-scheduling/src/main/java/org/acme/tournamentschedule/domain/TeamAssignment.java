package org.acme.tournamentschedule.domain;

import java.time.LocalDate;
import java.util.Objects;

import ai.timefold.solver.core.api.domain.common.PlanningId;
import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.entity.PlanningPin;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;

/**
 * The planning entity: a match slot on a fixed {@link #matchDate}, waiting for the {@link Team} that plays it.
 * There is exactly one assignment per match slot.
 */
@PlanningEntity
public class TeamAssignment {

    @PlanningId
    private String id;
    private LocalDate matchDate;
    @PlanningPin
    private boolean pinned;

    @PlanningVariable
    private Team team;

    public TeamAssignment() {
    }

    public TeamAssignment(String id, LocalDate matchDate) {
        this.id = id;
        this.matchDate = matchDate;
    }

    public TeamAssignment(String id, LocalDate matchDate, Team team, boolean pinned) {
        this(id, matchDate);
        this.team = team;
        this.pinned = pinned;
    }

    public String getId() {
        return id;
    }

    public LocalDate getMatchDate() {
        return matchDate;
    }

    public boolean isPinned() {
        return pinned;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TeamAssignment that)) {
            return false;
        }
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return id;
    }
}
