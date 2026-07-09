package org.acme.tournamentschedule.domain;

/**
 * A penalty expressing that a {@link Team} is unavailable on a given {@link Day}.
 */
public class UnavailabilityPenalty {

    private Team team;
    private Day day;

    public UnavailabilityPenalty() {
    }

    public UnavailabilityPenalty(Team team, Day day) {
        this.team = team;
        this.day = day;
    }

    public Team getTeam() {
        return team;
    }

    public Day getDay() {
        return day;
    }

    @Override
    public String toString() {
        return team + "@" + day;
    }
}
