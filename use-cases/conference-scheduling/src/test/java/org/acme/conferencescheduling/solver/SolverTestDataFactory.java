package org.acme.conferencescheduling.solver;

import java.util.ArrayList;
import java.util.List;

import org.acme.conferencescheduling.dto.ConferenceScheduleInput;
import org.acme.conferencescheduling.dto.RoomDTO;
import org.acme.conferencescheduling.dto.SpeakerDTO;
import org.acme.conferencescheduling.dto.TalkDTO;
import org.acme.conferencescheduling.dto.TalkTypeDTO;
import org.acme.conferencescheduling.dto.TimeslotDTO;

final class SolverTestDataFactory {

    private static final String BREAKOUT = "Breakout";

    private SolverTestDataFactory() {
    }

    static ConferenceScheduleInput createProblem() {
        List<TalkTypeDTO> talkTypes = List.of(new TalkTypeDTO(BREAKOUT));

        List<TimeslotDTO> timeslots = new ArrayList<>();
        timeslots.add(new TimeslotDTO("ts1", "2024-01-01T09:00", "2024-01-01T10:00", List.of(BREAKOUT), List.of()));
        timeslots.add(new TimeslotDTO("ts2", "2024-01-01T10:30", "2024-01-01T11:30", List.of(BREAKOUT), List.of()));
        timeslots.add(new TimeslotDTO("ts3", "2024-01-01T12:00", "2024-01-01T13:00", List.of(BREAKOUT), List.of()));

        List<RoomDTO> rooms = new ArrayList<>();
        rooms.add(new RoomDTO("r1", "Room A", 100, List.of(BREAKOUT), List.of(), List.of()));
        rooms.add(new RoomDTO("r2", "Room B", 100, List.of(BREAKOUT), List.of(), List.of()));

        List<SpeakerDTO> speakers = new ArrayList<>();
        speakers.add(speaker("s1", "Amy"));
        speakers.add(speaker("s2", "Bob"));
        speakers.add(speaker("s3", "Cara"));

        List<TalkDTO> talks = new ArrayList<>();
        talks.add(talk("S01", "s1"));
        talks.add(talk("S02", "s2"));
        talks.add(talk("S03", "s3"));

        return new ConferenceScheduleInput("Test conference", talkTypes, timeslots, rooms, speakers, talks);
    }

    private static SpeakerDTO speaker(String id, String name) {
        return new SpeakerDTO(id, name, List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(),
                List.of(), List.of());
    }

    private static TalkDTO talk(String code, String speakerId) {
        return new TalkDTO(code, code, BREAKOUT, List.of(speakerId), List.of(), List.of(), List.of(), 1, List.of(),
                "en", List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(),
                List.of(), 1, 0, "", "");
    }
}
