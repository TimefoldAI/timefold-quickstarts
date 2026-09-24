package org.acme.sportsleagueschedule.demo;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.IntStream;

import org.acme.sportsleagueschedule.dto.input.LeagueScheduleInput;
import org.acme.sportsleagueschedule.dto.input.MatchInputDTO;
import org.acme.sportsleagueschedule.dto.input.RoundInputDTO;
import org.acme.sportsleagueschedule.dto.input.TeamInputDTO;

/**
 * Builds the demo dataset: a double round-robin season for a fourteen-team South American league.
 * <p>
 * Which matches are classic ones comes from a {@link Random} with a fixed seed, so the dataset is
 * reproducible; only the weekend rounds move, because they are anchored to today.
 */
public final class DemoDataBuilder {

    private static final int ROUND_COUNT = 32;
    private static final long RANDOM_SEED = 0L;
    /** Share of the matches that are a classic, such as a derby. */
    private static final double CLASSIC_MATCH_RATIO = 0.05;

    private static final String[] TEAM_NAMES = { "Cruzeiro", "Argentinos Jr.", "Boca Juniors", "Estudiantes",
            "Independente", "Racing", "River Plate", "Flamengo", "Gremio", "Santos", "Colo-Colo", "Olimpia",
            "Nacional", "Penharol" };

    /** Distance in kilometres between the venues of every pair of teams, in {@link #TEAM_NAMES} order. */
    private static final int[][] DISTANCE_IN_KM = {
            { 0, 2163, 2163, 2160, 2156, 2156, 2163, 340, 1342, 512, 3038, 1526, 2054, 2054 },
            { 2163, 0, 11, 50, 813, 813, 11, 1967, 842, 1661, 1139, 1037, 202, 202 },
            { 2163, 11, 0, 50, 813, 813, 11, 1967, 842, 1661, 1139, 1037, 202, 202 },
            { 2160, 50, 50, 0, 862, 862, 50, 1957, 831, 1655, 1180, 1068, 161, 161 },
            { 2160, 813, 813, 862, 0, 1, 813, 2083, 1160, 1741, 910, 644, 600, 600 },
            { 2160, 813, 813, 862, 1, 0, 813, 2083, 1160, 1741, 910, 644, 600, 600 },
            { 2163, 11, 11, 50, 813, 813, 0, 1967, 842, 1661, 1139, 1037, 202, 202 },
            { 340, 1967, 1967, 1957, 2083, 2083, 1967, 0, 1126, 341, 2926, 1490, 1836, 1836 },
            { 1342, 842, 842, 831, 1160, 1160, 842, 1126, 0, 831, 1874, 820, 714, 714 },
            { 512, 1661, 1661, 1655, 1741, 1741, 1661, 341, 831, 0, 2589, 1151, 1545, 1545 },
            { 3038, 1139, 1139, 1180, 910, 910, 1139, 2926, 1874, 2589, 0, 1552, 1340, 1340 },
            { 1526, 1037, 1037, 1068, 644, 644, 1037, 1490, 820, 1151, 1552, 0, 1077, 1077 },
            { 2054, 202, 202, 161, 600, 600, 202, 1836, 714, 1545, 1340, 1077, 0, 14 },
            { 2054, 202, 202, 161, 600, 600, 202, 1836, 714, 1545, 1340, 1077, 14, 0 } };

    private DemoDataBuilder() {
    }

    public static LeagueScheduleInput basic() {
        return new LeagueScheduleInput(buildRounds(), buildTeams(), buildMatches());
    }

    private static List<RoundInputDTO> buildRounds() {
        // Rounds are played on consecutive days starting today, so the weekend ones move along with it.
        LocalDate today = LocalDate.now();
        return IntStream.range(0, ROUND_COUNT)
                .mapToObj(index -> new RoundInputDTO(index, isWeekend(today.plusDays(index))))
                .toList();
    }

    private static boolean isWeekend(LocalDate date) {
        return date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY;
    }

    private static List<TeamInputDTO> buildTeams() {
        return IntStream.range(0, TEAM_NAMES.length)
                .mapToObj(i -> new TeamInputDTO(teamId(i), TEAM_NAMES[i], buildDistances(i)))
                .toList();
    }

    private static Map<String, Integer> buildDistances(int teamIndex) {
        Map<String, Integer> distances = new LinkedHashMap<>();
        for (int otherTeamIndex = 0; otherTeamIndex < TEAM_NAMES.length; otherTeamIndex++) {
            if (otherTeamIndex != teamIndex) {
                distances.put(teamId(otherTeamIndex), DISTANCE_IN_KM[teamIndex][otherTeamIndex]);
            }
        }
        return distances;
    }

    /**
     * @return every ordered pair of teams, so each pairing is played twice: once at either venue
     */
    private static List<MatchInputDTO> buildMatches() {
        List<String> pairings = new ArrayList<>();
        for (int home = 0; home < TEAM_NAMES.length; home++) {
            for (int away = 0; away < TEAM_NAMES.length; away++) {
                if (home != away) {
                    pairings.add("%s-%s".formatted(teamId(home), teamId(away)));
                }
            }
        }

        // A classic match is a classic in both directions, so the reverse fixture is marked too.
        Random random = new Random(RANDOM_SEED);
        List<String> classicPairings = new ArrayList<>();
        int classicCount = (int) (pairings.size() * CLASSIC_MATCH_RATIO);
        while (classicPairings.size() < classicCount) {
            String pairing = pairings.get(random.nextInt(pairings.size()));
            if (!classicPairings.contains(pairing)) {
                classicPairings.add(pairing);
            }
        }

        return pairings.stream()
                .map(pairing -> {
                    String[] teamIds = pairing.split("-");
                    boolean classicMatch = classicPairings.contains(pairing)
                            || classicPairings.contains("%s-%s".formatted(teamIds[1], teamIds[0]));
                    return new MatchInputDTO(pairing, teamIds[0], teamIds[1], classicMatch, null);
                })
                .toList();
    }

    private static String teamId(int teamIndex) {
        return Integer.toString(teamIndex + 1);
    }
}
