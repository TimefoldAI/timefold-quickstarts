package org.acme.tournamentschedule.domain;

import java.util.Objects;

import ai.timefold.solver.core.api.domain.common.PlanningId;

/**
 * A team that has to be assigned to matches, spread as fairly and evenly as possible over the tournament.
 */
public record Team(
        @PlanningId String id,
        String name) {

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

    @Override
    public String toString() {
        return name;
    }
}
