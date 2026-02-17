package org.acme.tournamentschedule.domain;

import java.util.Objects;

import ai.timefold.solver.core.api.domain.common.PlanningId;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(scope = Team.class, generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Team {

    @PlanningId
    private long id;
    private String name;

    public Team() {
    }

    public Team(long id) {
        this.id = id;
    }

    public Team(long id, String name) {
        this(id);
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name == null ? super.toString() : name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Team team))
            return false;
        return Objects.equals(getId(), team.getId());
    }

    @Override
    public int hashCode() {
        return (int) (31 * getId());
    }
}
