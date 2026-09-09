package org.acme.sportsleagueschedule.domain;

import java.util.Map;
import java.util.Objects;

/**
 * A team in the league, together with the distance from its own venue to every other team's venue.
 */
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

    /**
     * @return the distance in km from this team's venue to the other team's venue, 0 for itself
     */
    public int getDistance(Team other) {
        if (equals(other)) {
            return 0;
        }
        Integer distance = distanceToTeam == null ? null : distanceToTeam.get(other);
        return distance == null ? 0 : distance;
    }

    @Override
    public String toString() {
        return getId();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Team team)) {
            return false;
        }
        return Objects.equals(getId(), team.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
