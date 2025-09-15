package org.acme.tournamentschedule.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import jakarta.enterprise.context.ApplicationScoped;

import org.acme.tournamentschedule.domain.Day;
import org.acme.tournamentschedule.domain.Team;
import org.acme.tournamentschedule.domain.TeamAssignment;
import org.acme.tournamentschedule.domain.TournamentSchedule;
import org.acme.tournamentschedule.domain.UnavailabilityPenalty;

@ApplicationScoped
public class DemoDataGenerator {

    public TournamentSchedule generateDemoData() {
        Random random = new Random(0);
        TournamentSchedule schedule = new TournamentSchedule();
        // Teams
        List<Team> teams = generateTeams();
        // Days
        int countDays = 18;
        List<Day> days = IntStream.range(0, countDays)
                .mapToObj(Day::new)
                .toList();
        // Unavailability penalty
        int countUnavailabilityPenalties = 12;
        List<UnavailabilityPenalty> unavailabilityPenalties =
                generateUnavailabilityPenalties(countUnavailabilityPenalties, teams, days, random);
        // Assignments
        int countAssignmentsPerDay = 4;
        List<TeamAssignment> teamAssignments = generateTeamAssignments(countAssignmentsPerDay, days);
        // Update schedule
        schedule.setTeams(teams);
        schedule.setDays(days);
        schedule.setUnavailabilityPenalties(unavailabilityPenalties);
        schedule.setTeamAssignments(teamAssignments);
        return schedule;
    }

    private List<Team> generateTeams() {
        return List.of(
                new Team(0, "Maarten"),
                new Team(1, "Geoffrey"),
                new Team(2, "Lukas"),
                new Team(3, "Chris"),
                new Team(4, "Fred"),
                new Team(5, "Radek"),
                new Team(6, "Maciej"));
    }

    private List<UnavailabilityPenalty> generateUnavailabilityPenalties(int countUnavailabilityPenalities, List<Team> teams,
                                                                        List<Day> days, Random random) {
        List<UnavailabilityPenalty> unavailabilityPenalties = new ArrayList<>(countUnavailabilityPenalities);
        while (unavailabilityPenalties.size() < countUnavailabilityPenalities) {
            Team team = teams.get(random.nextInt(teams.size()));
            Day day = days.get(random.nextInt(days.size()));
            if (unavailabilityPenalties.stream().noneMatch(p -> p.getTeam().equals(team) && p.getDay().equals(day))) {
                unavailabilityPenalties.add(new UnavailabilityPenalty(team, day));
            }
        }
        return unavailabilityPenalties;
    }

    private List<TeamAssignment> generateTeamAssignments(int countAssignmentsPerDay, List<Day> days) {
        List<TeamAssignment> assignments = new ArrayList<>(days.size() * countAssignmentsPerDay);
        int count = 0;
        for (Day day : days) {
            for (int i = 0; i < countAssignmentsPerDay; i++) {
                assignments.add(new TeamAssignment(count++, day, i));
            }
        }
        return assignments;
    }

}
