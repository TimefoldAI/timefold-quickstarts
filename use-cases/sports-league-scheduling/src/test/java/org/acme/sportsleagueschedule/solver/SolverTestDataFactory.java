package org.acme.sportsleagueschedule.solver;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.acme.sportsleagueschedule.dto.LeagueScheduleInput;
import org.acme.sportsleagueschedule.dto.MatchDTO;
import org.acme.sportsleagueschedule.dto.RoundDTO;
import org.acme.sportsleagueschedule.dto.TeamDTO;

final class SolverTestDataFactory {

    private SolverTestDataFactory() {
    }

    static LeagueScheduleInput createProblem() {
        int teamCount = 4;
        int roundCount = 10;

        List<RoundDTO> rounds = new ArrayList<>(roundCount);
        for (int i = 0; i < roundCount; i++) {
            rounds.add(new RoundDTO(i, i % 7 >= 5));
        }

        List<TeamDTO> teams = new ArrayList<>(teamCount);
        for (int i = 0; i < teamCount; i++) {
            Map<String, Integer> distances = new LinkedHashMap<>();
            for (int j = 0; j < teamCount; j++) {
                if (i != j) {
                    distances.put(Integer.toString(j + 1), 100 * Math.abs(i - j));
                }
            }
            teams.add(new TeamDTO(Integer.toString(i + 1), "Team " + (i + 1), distances));
        }

        List<MatchDTO> matches = new ArrayList<>();
        Integer unassignedRound = null;
        int counter = 0;
        for (int i = 0; i < teamCount; i++) {
            for (int j = 0; j < teamCount; j++) {
                if (i != j) {
                    matches.add(new MatchDTO("%d-%d".formatted(i + 1, j + 1), Integer.toString(i + 1),
                            Integer.toString(j + 1), counter % 5 == 0, unassignedRound));
                    counter++;
                }
            }
        }

        return new LeagueScheduleInput(rounds, teams, matches);
    }
}
