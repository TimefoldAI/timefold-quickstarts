package org.acme.tournamentschedule.demo;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.acme.tournamentschedule.dto.DayDTO;
import org.acme.tournamentschedule.dto.TeamAssignmentDTO;
import org.acme.tournamentschedule.dto.TeamDTO;
import org.acme.tournamentschedule.dto.TournamentScheduleInput;
import org.acme.tournamentschedule.dto.UnavailabilityPenaltyDTO;

public final class DemoDataBuilder {

    private static final String UNASSIGNED = "";
    private static final int MINIMUM_COUNT = 1;

    private int dayCount;
    private int assignmentsPerDay;
    private int unavailabilityPenaltyCount;
    private long randomSeed;
    private final List<String> teamNames = new ArrayList<>();

    private DemoDataBuilder() {
    }

    public static DemoDataBuilder builder() {
        return new DemoDataBuilder();
    }

    public DemoDataBuilder setDayCount(int dayCount) {
        this.dayCount = dayCount;
        return this;
    }

    public DemoDataBuilder setAssignmentsPerDay(int assignmentsPerDay) {
        this.assignmentsPerDay = assignmentsPerDay;
        return this;
    }

    public DemoDataBuilder setUnavailabilityPenaltyCount(int unavailabilityPenaltyCount) {
        this.unavailabilityPenaltyCount = unavailabilityPenaltyCount;
        return this;
    }

    public DemoDataBuilder setRandomSeed(long randomSeed) {
        this.randomSeed = randomSeed;
        return this;
    }

    public DemoDataBuilder addTeam(String name) {
        teamNames.add(name);
        return this;
    }

    public TournamentScheduleInput build() {
        if (dayCount < MINIMUM_COUNT) {
            throw new IllegalStateException("Number of days (" + dayCount + ") must be greater than zero.");
        }
        if (assignmentsPerDay < MINIMUM_COUNT) {
            throw new IllegalStateException(
                    "Number of assignments per day (" + assignmentsPerDay + ") must be greater than zero.");
        }
        if (teamNames.isEmpty()) {
            throw new IllegalStateException("At least one team must be defined.");
        }
        List<TeamDTO> teams = buildTeams();
        List<DayDTO> days = buildDays();
        return new TournamentScheduleInput(teams, days, buildUnavailabilityPenalties(teams, days),
                buildTeamAssignments());
    }

    private List<TeamDTO> buildTeams() {
        List<TeamDTO> teams = new ArrayList<>();
        for (int i = 0; i < teamNames.size(); i++) {
            teams.add(new TeamDTO(Integer.toString(i), teamNames.get(i)));
        }
        return teams;
    }

    private List<DayDTO> buildDays() {
        List<DayDTO> days = new ArrayList<>();
        for (int i = 0; i < dayCount; i++) {
            days.add(new DayDTO(i));
        }
        return days;
    }

    private List<UnavailabilityPenaltyDTO> buildUnavailabilityPenalties(List<TeamDTO> teams, List<DayDTO> days) {
        Random random = new Random(randomSeed);
        List<UnavailabilityPenaltyDTO> penalties = new ArrayList<>();
        int target = Math.min(unavailabilityPenaltyCount, teams.size() * days.size());
        while (penalties.size() < target) {
            String teamId = teams.get(random.nextInt(teams.size())).id();
            int dateIndex = days.get(random.nextInt(days.size())).dateIndex();
            boolean exists = penalties.stream()
                    .anyMatch(p -> p.teamId().equals(teamId) && p.dateIndex() == dateIndex);
            if (!exists) {
                penalties.add(new UnavailabilityPenaltyDTO(teamId, dateIndex));
            }
        }
        return penalties;
    }

    private List<TeamAssignmentDTO> buildTeamAssignments() {
        List<TeamAssignmentDTO> assignments = new ArrayList<>();
        long sequence = 0;
        for (int day = 0; day < dayCount; day++) {
            for (int index = 0; index < assignmentsPerDay; index++) {
                assignments.add(
                        new TeamAssignmentDTO(Long.toString(sequence), day, index, false, UNASSIGNED));
                sequence += 1;
            }
        }
        return assignments;
    }
}
