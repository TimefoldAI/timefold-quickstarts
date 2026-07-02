package org.acme.tournamentschedule.domain;

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

    public void setTeam(Team team) {
        this.team = team;
    }

    public Day getDay() {
        return day;
    }

    public void setDay(Day day) {
        this.day = day;
    }

}
