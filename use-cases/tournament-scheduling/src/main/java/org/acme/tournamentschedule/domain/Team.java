package org.acme.tournamentschedule.domain;

import java.util.Objects;

import ai.timefold.solver.core.api.domain.common.PlanningId;

/**
 * A team that competes in the tournament. The solver assigns teams to
 * {@link TeamAssignment} slots.
 */
public class Team {

    @PlanningId
    private String id;
    private String name;

    public Team() {
    }

    public Team(String id) {
        this.id = id;
    }

    public Team(String id, String name) {
        this(id);
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name == null ? super.toString() : name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Team team)) {
            return false;
        }
        return Objects.equals(id, team.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
