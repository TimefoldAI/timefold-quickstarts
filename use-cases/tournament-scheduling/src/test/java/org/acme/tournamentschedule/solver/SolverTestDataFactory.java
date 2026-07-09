package org.acme.tournamentschedule.solver;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.acme.tournamentschedule.dto.DayDTO;
import org.acme.tournamentschedule.dto.TeamAssignmentDTO;
import org.acme.tournamentschedule.dto.TeamDTO;
import org.acme.tournamentschedule.dto.TournamentScheduleInput;
import org.acme.tournamentschedule.dto.UnavailabilityPenaltyDTO;

final class SolverTestDataFactory {

    private SolverTestDataFactory() {
    }

    static TournamentScheduleInput createProblem() {
        String[] teamNames = { "Maarten", "Geoffrey", "Lukas", "Chris", "Fred", "Radek", "Maciej" };
        List<TeamDTO> teams = new ArrayList<>();
        for (int i = 0; i < teamNames.length; i++) {
            teams.add(new TeamDTO(Integer.toString(i), teamNames[i]));
        }

        int dayCount = 18;
        List<DayDTO> days = new ArrayList<>();
        for (int i = 0; i < dayCount; i++) {
            days.add(new DayDTO(i));
        }

        Random random = new Random(0);
        List<UnavailabilityPenaltyDTO> penalties = new ArrayList<>();
        while (penalties.size() < 12) {
            String teamId = teams.get(random.nextInt(teams.size())).id();
            int dateIndex = days.get(random.nextInt(days.size())).dateIndex();
            boolean exists = penalties.stream()
                    .anyMatch(p -> p.teamId().equals(teamId) && p.dateIndex() == dateIndex);
            if (!exists) {
                penalties.add(new UnavailabilityPenaltyDTO(teamId, dateIndex));
            }
        }

        List<TeamAssignmentDTO> assignments = new ArrayList<>();
        long sequence = 0;
        for (int day = 0; day < dayCount; day++) {
            for (int index = 0; index < 4; index++) {
                assignments.add(new TeamAssignmentDTO(Long.toString(sequence++), day, index, false, ""));
            }
        }

        return new TournamentScheduleInput(teams, days, penalties, assignments);
    }
}
