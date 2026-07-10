package org.acme.sportsleagueschedule.domain;

import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(scope = Team.class, generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Team {

    private String id;
    private String name;
    private Map<Team, Integer> distanceToTeam;

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

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<Team, Integer> getDistanceToTeam() {
        return distanceToTeam;
    }

    public void setDistanceToTeam(Map<Team, Integer> distanceToTeam) {
        this.distanceToTeam = distanceToTeam;
    }

    @JsonIgnore
    public int getDistance(Team other) {
        return distanceToTeam.get(other);
    }

    @Override
    public String toString() {
        return getId();
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
        return getId().hashCode();
    }
}
