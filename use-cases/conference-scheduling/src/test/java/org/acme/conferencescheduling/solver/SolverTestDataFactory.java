package org.acme.conferencescheduling.solver;

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

        List<TimeslotDTO> timeslots = List.of(
                timeslot("ts1", "2024-01-01T09:00", "2024-01-01T10:00"),
                timeslot("ts2", "2024-01-01T10:30", "2024-01-01T11:30"),
                timeslot("ts3", "2024-01-01T12:00", "2024-01-01T13:00"));

        List<RoomDTO> rooms = List.of(
                room("r1", "Room A"),
                room("r2", "Room B"));

        List<SpeakerDTO> speakers = List.of(
                SpeakerDTO.builder("s1", "Amy").build(),
                SpeakerDTO.builder("s2", "Bob").build(),
                SpeakerDTO.builder("s3", "Cara").build());

        List<TalkDTO> talks = List.of(
                talk("S01", "s1"),
                talk("S02", "s2"),
                talk("S03", "s3"));

        return new ConferenceScheduleInput("Test conference", talkTypes, timeslots, rooms, speakers, talks);
    }

    private static TimeslotDTO timeslot(String id, String start, String end) {
        return new TimeslotDTO(id, start, end, List.of(BREAKOUT), List.of());
    }

    private static RoomDTO room(String id, String name) {
        return new RoomDTO(id, name, 100, List.of(BREAKOUT), List.of(), List.of());
    }

    private static TalkDTO talk(String code, String speakerId) {
        return TalkDTO.builder(code, code, BREAKOUT)
                .speakerIds(List.of(speakerId))
                .audienceLevel(1)
                .language("en")
                .favoriteCount(1)
                .build();
    }
}
