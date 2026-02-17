package org.acme.sportsleagueschedule.domain;

import java.util.Objects;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.common.PlanningId;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;

import com.fasterxml.jackson.annotation.JsonIgnore;

@PlanningEntity
public class Match {

    @PlanningId
    private String id;
    private Team homeTeam;
    private Team awayTeam;
    // A classic/important match can impact aspects like revenue (e.g., derby)
    private boolean classicMatch;
    @PlanningVariable
    private Round round;

    public Match() {
    }

    public Match(String id) {
        this.id = id;
    }

    public Match(String id, Team homeTeam, Team awayTeam) {
        this(id);
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
    }

    public Match(String id, Team homeTeam, Team awayTeam, boolean classicMatch) {
        this(id, homeTeam, awayTeam);
        this.classicMatch = classicMatch;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Team getHomeTeam() {
        return homeTeam;
    }

    public void setHomeTeam(Team homeTeam) {
        this.homeTeam = homeTeam;
    }

    public Team getAwayTeam() {
        return awayTeam;
    }

    public void setAwayTeam(Team awayTeam) {
        this.awayTeam = awayTeam;
    }

    public boolean isClassicMatch() {
        return classicMatch;
    }

    public void setClassicMatch(boolean classicMatch) {
        this.classicMatch = classicMatch;
    }

    public Round getRound() {
        return round;
    }

    public void setRound(Round round) {
        this.round = round;
    }

    @Override
    public String toString() {
        return homeTeam + "+" + awayTeam;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************
    @JsonIgnore
    public int getRoundIndex() {
        return getRound().getIndex();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Match match))
            return false;
        return Objects.equals(getId(), match.getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }
}
