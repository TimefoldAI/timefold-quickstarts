package org.acme.tournamentschedule.demo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.acme.tournamentschedule.dto.input.MatchInputDTO;
import org.acme.tournamentschedule.dto.input.TeamInputDTO;
import org.acme.tournamentschedule.dto.input.TournamentScheduleInput;
import org.acme.tournamentschedule.dto.input.UnavailabilityInputDTO;

/**
 * Builds a demo dataset of a round-robin-style tournament for a handful of teams.
 * <p>
 * Only the unavailability picking is random, and it draws from a {@link Random} seeded with {@link #RANDOM_SEED}, so
 * the dataset is the same on every run.
 */
public final class DemoDataBuilder {

    private static final long RANDOM_SEED = 0;

    private static final int DAY_COUNT = 18;
    private static final int MATCHES_PER_DAY = 4;
    private static final int UNAVAILABILITY_COUNT = 12;

    private static final String[] TEAM_NAMES =
            { "Maarten", "Geoffrey", "Lukas", "Chris", "Fred", "Radek", "Maciej" };

    private DemoDataBuilder() {
    }

    public static TournamentScheduleInput basic() {
        Random random = new Random(RANDOM_SEED);
        List<TeamInputDTO> teams = buildTeams();
        LocalDate firstMatchDate = LocalDate.now();
        List<MatchInputDTO> matches = buildMatches(firstMatchDate);
        List<UnavailabilityInputDTO> unavailabilities =
                buildUnavailabilities(teams, firstMatchDate, random);
        return new TournamentScheduleInput(teams, unavailabilities, matches);
    }

    private static List<TeamInputDTO> buildTeams() {
        List<TeamInputDTO> teams = new ArrayList<>(TEAM_NAMES.length);
        for (int i = 0; i < TEAM_NAMES.length; i++) {
            teams.add(new TeamInputDTO("T%d".formatted(i + 1), TEAM_NAMES[i]));
        }
        return teams;
    }

    private static List<MatchInputDTO> buildMatches(LocalDate firstMatchDate) {
        List<MatchInputDTO> matches = new ArrayList<>(DAY_COUNT * MATCHES_PER_DAY);
        int matchNumber = 0;
        for (int dayOffset = 0; dayOffset < DAY_COUNT; dayOffset++) {
            LocalDate date = firstMatchDate.plusDays(dayOffset);
            for (int i = 0; i < MATCHES_PER_DAY; i++) {
                matchNumber++;
                matches.add(new MatchInputDTO("M%d".formatted(matchNumber), date, null, false));
            }
        }
        return matches;
    }

    private static List<UnavailabilityInputDTO> buildUnavailabilities(List<TeamInputDTO> teams,
            LocalDate firstMatchDate, Random random) {
        List<UnavailabilityInputDTO> unavailabilities = new ArrayList<>(UNAVAILABILITY_COUNT);
        while (unavailabilities.size() < UNAVAILABILITY_COUNT) {
            TeamInputDTO team = teams.get(random.nextInt(teams.size()));
            LocalDate date = firstMatchDate.plusDays(random.nextInt(DAY_COUNT));
            boolean alreadyUnavailable = unavailabilities.stream()
                    .anyMatch(u -> u.teamId().equals(team.id()) && u.date().equals(date));
            if (!alreadyUnavailable) {
                unavailabilities.add(new UnavailabilityInputDTO(team.id(), date));
            }
        }
        return unavailabilities;
    }
}
