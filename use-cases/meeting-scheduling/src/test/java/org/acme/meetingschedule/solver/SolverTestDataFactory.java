package org.acme.meetingschedule.solver;

import java.util.ArrayList;
import java.util.List;

import org.acme.meetingschedule.dto.MeetingAssignmentDTO;
import org.acme.meetingschedule.dto.MeetingDTO;
import org.acme.meetingschedule.dto.MeetingScheduleInput;
import org.acme.meetingschedule.dto.PersonDTO;
import org.acme.meetingschedule.dto.RoomDTO;
import org.acme.meetingschedule.dto.TimeGrainDTO;

final class SolverTestDataFactory {

    private SolverTestDataFactory() {
    }

    static MeetingScheduleInput createProblem() {
        List<PersonDTO> people = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            people.add(new PersonDTO("P" + i, "Person " + i));
        }

        List<TimeGrainDTO> timeGrains = new ArrayList<>();
        int dayOfYear = 100;
        for (int i = 0; i < 40; i++) {
            timeGrains.add(new TimeGrainDTO("T" + i, i, dayOfYear, 8 * 60 + i * 15));
        }

        List<RoomDTO> rooms = List.of(
                new RoomDTO("R1", "Room 1", 30),
                new RoomDTO("R2", "Room 2", 20),
                new RoomDTO("R3", "Room 3", 16));

        List<MeetingDTO> meetings = new ArrayList<>();
        List<MeetingAssignmentDTO> assignments = new ArrayList<>();
        String[][] definitions = {
                { "Kickoff", "P0", "P1" },
                { "Design", "P1", "P2" },
                { "Review", "P2", "P3" },
                { "Planning", "P3", "P4" },
                { "Retrospective", "P4", "P5" },
                { "Sync", "P5", "P0" }
        };
        for (int i = 0; i < definitions.length; i++) {
            String[] definition = definitions[i];
            meetings.add(new MeetingDTO(String.valueOf(i), definition[0], 4,
                    List.of(definition[1]), List.of(definition[2])));
            assignments.add(new MeetingAssignmentDTO(String.valueOf(i), String.valueOf(i), "", "", false));
        }

        return new MeetingScheduleInput(people, timeGrains, rooms, meetings, assignments);
    }
}
