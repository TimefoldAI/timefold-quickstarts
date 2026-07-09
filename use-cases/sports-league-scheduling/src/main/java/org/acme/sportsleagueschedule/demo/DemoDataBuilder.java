package org.acme.sportsleagueschedule.demo;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.acme.sportsleagueschedule.dto.LeagueScheduleInput;
import org.acme.sportsleagueschedule.dto.MatchDTO;
import org.acme.sportsleagueschedule.dto.RoundDTO;
import org.acme.sportsleagueschedule.dto.TeamDTO;

public final class DemoDataBuilder {

    private static final String[] TEAM_NAMES = {
            "Cruzeiro", "Argentinos Jr.", "Boca Juniors", "Estudiantes", "Independente", "Racing", "River Plate",
            "Flamengo", "Gremio", "Santos", "Colo-Colo", "Olimpia", "Nacional", "Penharol" };

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
            { 2054, 202, 202, 161, 600, 600, 202, 1836, 714, 1545, 1340, 1077, 14, 0 },
    };

    private int roundCount = 32;
    private int classicMatchEvery = 19;

    private DemoDataBuilder() {
    }

    public static DemoDataBuilder builder() {
        return new DemoDataBuilder();
    }

    public DemoDataBuilder setRoundCount(int roundCount) {
        this.roundCount = roundCount;
        return this;
    }

    public DemoDataBuilder setClassicMatchEvery(int classicMatchEvery) {
        this.classicMatchEvery = classicMatchEvery;
        return this;
    }

    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public LeagueScheduleInput build() {
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        List<RoundDTO> rounds = new ArrayList<>(roundCount);
        for (int i = 0; i < roundCount; i++) {
            DayOfWeek dayOfWeek = today.plusDays(i).getDayOfWeek();
            boolean weekendOrHoliday = dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
            rounds.add(new RoundDTO(i, weekendOrHoliday));
        }

        int teamCount = TEAM_NAMES.length;
        List<TeamDTO> teams = new ArrayList<>(teamCount);
        for (int i = 0; i < teamCount; i++) {
            Map<String, Integer> distances = new LinkedHashMap<>();
            for (int j = 0; j < teamCount; j++) {
                if (i != j) {
                    distances.put(Integer.toString(j + 1), DISTANCE_IN_KM[i][j]);
                }
            }
            teams.add(new TeamDTO(Integer.toString(i + 1), TEAM_NAMES[i], distances));
        }

        List<MatchDTO> matches = new ArrayList<>(teamCount * (teamCount - 1));
        Integer unassignedRound = null;
        int counter = 0;
        for (int i = 0; i < teamCount; i++) {
            for (int j = 0; j < teamCount; j++) {
                if (i != j) {
                    boolean classicMatch = counter % classicMatchEvery == 0;
                    matches.add(new MatchDTO("%d-%d".formatted(i + 1, j + 1), Integer.toString(i + 1),
                            Integer.toString(j + 1), classicMatch, unassignedRound));
                    counter++;
                }
            }
        }

        return new LeagueScheduleInput(rounds, teams, matches);
    }
}
