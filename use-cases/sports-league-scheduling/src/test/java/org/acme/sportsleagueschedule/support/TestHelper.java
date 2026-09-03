package org.acme.sportsleagueschedule.support;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import org.acme.sportsleagueschedule.domain.Match;
import org.acme.sportsleagueschedule.domain.Round;
import org.acme.sportsleagueschedule.domain.Team;
import org.acme.sportsleagueschedule.dto.input.LeagueScheduleInput;
import org.acme.sportsleagueschedule.dto.input.MatchInputDTO;
import org.acme.sportsleagueschedule.dto.input.RoundInputDTO;
import org.acme.sportsleagueschedule.dto.input.TeamInputDTO;

// To keep our production classes as simple as possible, we've added these methods to help construct the data needed for testing.
public final class TestHelper {

    private static final int PROBLEM_TEAM_COUNT = 4;
    private static final int PROBLEM_ROUND_COUNT = 12;
    /** Every pair of teams meets twice, once at either venue. */
    private static final int PROBLEM_DISTANCE_IN_KM = 100;

    private TestHelper() {
    }

    public static LeagueScheduleInput input(List<RoundInputDTO> rounds, List<TeamInputDTO> teams,
            List<MatchInputDTO> matches) {
        return new LeagueScheduleInput(rounds, teams, matches);
    }

    /**
     * @return a small but feasible problem: a double round-robin for four teams, with twice as many
     *         rounds as any team needs, so alternating home and away is always possible
     */
    public static LeagueScheduleInput createProblem() {
        List<RoundInputDTO> rounds = rounds(PROBLEM_ROUND_COUNT);
        List<TeamInputDTO> teams = teams(PROBLEM_TEAM_COUNT);
        List<MatchInputDTO> matches = doubleRoundRobin(PROBLEM_TEAM_COUNT);
        return input(rounds, teams, matches);
    }

    /** @return {@code count} rounds, every third one a weekend round */
    public static List<RoundInputDTO> rounds(int count) {
        return IntStream.range(0, count)
                .mapToObj(index -> aRoundDTO(index).weekendOrHoliday(index % 3 == 0).build())
                .toList();
    }

    /** @return {@code count} teams, each a fixed distance away from every other team */
    public static List<TeamInputDTO> teams(int count) {
        return IntStream.rangeClosed(1, count)
                .mapToObj(i -> {
                    TeamDTOBuilder builder = aTeamDTO(Integer.toString(i));
                    IntStream.rangeClosed(1, count)
                            .filter(j -> j != i)
                            .forEach(j -> builder.distanceTo(Integer.toString(j), PROBLEM_DISTANCE_IN_KM));
                    return builder.build();
                })
                .toList();
    }

    /** @return every ordered pair of {@code count} teams as an unassigned match */
    public static List<MatchInputDTO> doubleRoundRobin(int count) {
        return IntStream.rangeClosed(1, count)
                .boxed()
                .flatMap(home -> IntStream.rangeClosed(1, count)
                        .filter(away -> away != home)
                        .mapToObj(away -> aMatchDTO("%d-%d".formatted(home, away))
                                .homeTeamId(Integer.toString(home))
                                .awayTeamId(Integer.toString(away))
                                .build()))
                .toList();
    }

    public static RoundDTOBuilder aRoundDTO(int index) {
        return new RoundDTOBuilder(index);
    }

    public static TeamDTOBuilder aTeamDTO(String id) {
        return new TeamDTOBuilder(id);
    }

    public static MatchDTOBuilder aMatchDTO(String id) {
        return new MatchDTOBuilder(id);
    }

    public static RoundBuilder aRound(int index) {
        return new RoundBuilder(index);
    }

    public static TeamBuilder aTeam(String id) {
        return new TeamBuilder(id);
    }

    public static MatchBuilder aMatch(String id) {
        return new MatchBuilder(id);
    }

    public static final class RoundDTOBuilder {

        private final int index;
        private boolean weekendOrHoliday;

        private RoundDTOBuilder(int index) {
            this.index = index;
        }

        public RoundDTOBuilder weekendOrHoliday(boolean weekendOrHoliday) {
            this.weekendOrHoliday = weekendOrHoliday;
            return this;
        }

        public RoundInputDTO build() {
            return new RoundInputDTO(index, weekendOrHoliday);
        }
    }

    public static final class TeamDTOBuilder {

        private final String id;
        private String name;
        private final Map<String, Integer> distanceToTeam = new LinkedHashMap<>();

        private TeamDTOBuilder(String id) {
            this.id = id;
            this.name = "Team " + id;
        }

        public TeamDTOBuilder name(String name) {
            this.name = name;
            return this;
        }

        public TeamDTOBuilder distanceTo(String otherTeamId, int distance) {
            distanceToTeam.put(otherTeamId, distance);
            return this;
        }

        public TeamInputDTO build() {
            return new TeamInputDTO(id, name, Map.copyOf(distanceToTeam));
        }
    }

    public static final class MatchDTOBuilder {

        private final String id;
        private String homeTeamId = "1";
        private String awayTeamId = "2";
        private boolean classicMatch;
        private Integer roundIndex;

        private MatchDTOBuilder(String id) {
            this.id = id;
        }

        public MatchDTOBuilder homeTeamId(String homeTeamId) {
            this.homeTeamId = homeTeamId;
            return this;
        }

        public MatchDTOBuilder awayTeamId(String awayTeamId) {
            this.awayTeamId = awayTeamId;
            return this;
        }

        public MatchDTOBuilder classicMatch(boolean classicMatch) {
            this.classicMatch = classicMatch;
            return this;
        }

        public MatchDTOBuilder roundIndex(Integer roundIndex) {
            this.roundIndex = roundIndex;
            return this;
        }

        public MatchInputDTO build() {
            return new MatchInputDTO(id, homeTeamId, awayTeamId, classicMatch, roundIndex);
        }
    }

    public static final class RoundBuilder {

        private final int index;
        private boolean weekendOrHoliday;

        private RoundBuilder(int index) {
            this.index = index;
        }

        public RoundBuilder weekendOrHoliday(boolean weekendOrHoliday) {
            this.weekendOrHoliday = weekendOrHoliday;
            return this;
        }

        public Round build() {
            return new Round(index, weekendOrHoliday);
        }
    }

    public static final class TeamBuilder {

        private final String id;
        private String name;
        private final Map<String, Integer> distanceToTeam = new LinkedHashMap<>();

        private TeamBuilder(String id) {
            this.id = id;
            this.name = "Team " + id;
        }

        public TeamBuilder name(String name) {
            this.name = name;
            return this;
        }

        public TeamBuilder distanceTo(TeamBuilder otherTeam, int distance) {
            distanceToTeam.put(otherTeam.id, distance);
            return this;
        }

        /**
         * Team equality is id-based, so keying the distance map by a fresh Team with the other
         * team's id resolves to the very same team the solver model holds.
         */
        public Team build() {
            Team team = new Team(id, name);
            Map<Team, Integer> distances = new LinkedHashMap<>();
            distanceToTeam.forEach((otherTeamId, distance) -> distances.put(new Team(otherTeamId), distance));
            team.setDistanceToTeam(distances);
            return team;
        }
    }

    public static final class MatchBuilder {

        private final String id;
        private TeamBuilder homeTeam;
        private TeamBuilder awayTeam;
        private boolean classicMatch;
        private RoundBuilder round;

        private MatchBuilder(String id) {
            this.id = id;
        }

        public MatchBuilder homeTeam(TeamBuilder homeTeam) {
            this.homeTeam = homeTeam;
            return this;
        }

        public MatchBuilder awayTeam(TeamBuilder awayTeam) {
            this.awayTeam = awayTeam;
            return this;
        }

        public MatchBuilder classicMatch(boolean classicMatch) {
            this.classicMatch = classicMatch;
            return this;
        }

        public MatchBuilder round(RoundBuilder round) {
            this.round = round;
            return this;
        }

        public Match build() {
            Match match = new Match(id, homeTeam == null ? null : homeTeam.build(),
                    awayTeam == null ? null : awayTeam.build(), classicMatch);
            if (round != null) {
                match.setRound(round.build());
            }
            return match;
        }
    }
}
