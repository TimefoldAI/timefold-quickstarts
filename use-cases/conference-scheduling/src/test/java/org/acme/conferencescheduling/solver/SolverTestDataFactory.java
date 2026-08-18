package org.acme.conferencescheduling.solver;

import java.util.List;

import org.acme.conferencescheduling.dto.ConferenceScheduleInput;
import org.acme.conferencescheduling.dto.RoomDTO;
import org.acme.conferencescheduling.dto.SpeakerDTO;
import org.acme.conferencescheduling.dto.TalkDTO;
import org.acme.conferencescheduling.dto.TalkTypeDTO;
import org.acme.conferencescheduling.dto.TimeslotDTO;

/**
 * Shared test data for the conference scheduling tests.
 * <p>
 * {@link #createProblem()} returns a complete, feasible problem for the solver tests. The constants and
 * factory methods are the building blocks of that problem and can be recombined, which lets tests that
 * focus on a single aspect - such as the validation tests - vary one collection and keep the rest valid.
 */
public final class SolverTestDataFactory {

    public static final String CONFERENCE_NAME = "Test conference";
    public static final String BREAKOUT = "Breakout";
    public static final String LAB = "Lab";

    public static final List<TalkTypeDTO> TALK_TYPES = List.of(new TalkTypeDTO(BREAKOUT), new TalkTypeDTO(LAB));

    public static final List<TimeslotDTO> TIMESLOTS = List.of(
            timeslot("ts1", "2024-01-01T09:00", "2024-01-01T10:00"),
            timeslot("ts2", "2024-01-01T10:30", "2024-01-01T11:30"),
            timeslot("ts3", "2024-01-01T12:00", "2024-01-01T13:00"));

    public static final List<RoomDTO> ROOMS = List.of(
            room("r1", "Room A"),
            room("r2", "Room B"));

    public static final List<SpeakerDTO> SPEAKERS = List.of(
            speaker("s1", "Amy"),
            speaker("s2", "Bob"),
            speaker("s3", "Cara"));

    private SolverTestDataFactory() {
    }

    /**
     * @return a complete, feasible problem with every talk still unassigned
     */
    public static ConferenceScheduleInput createProblem() {
        return input(TALK_TYPES, TIMESLOTS, ROOMS, SPEAKERS,
                List.of(talk("S01", "s1"), talk("S02", "s2"), talk("S03", "s3")));
    }

    public static ConferenceScheduleInput input(List<TalkTypeDTO> talkTypes, List<TimeslotDTO> timeslots,
            List<RoomDTO> rooms, List<SpeakerDTO> speakers, List<TalkDTO> talks) {
        return new ConferenceScheduleInput(CONFERENCE_NAME, talkTypes, timeslots, rooms, speakers, talks);
    }

    public static ConferenceScheduleInput inputWithTimeslots(TimeslotDTO... timeslots) {
        return input(TALK_TYPES, List.of(timeslots), ROOMS, SPEAKERS, List.of());
    }

    public static ConferenceScheduleInput inputWithRooms(RoomDTO... rooms) {
        return input(TALK_TYPES, TIMESLOTS, List.of(rooms), SPEAKERS, List.of());
    }

    public static ConferenceScheduleInput inputWithSpeakers(SpeakerDTO... speakers) {
        return input(TALK_TYPES, TIMESLOTS, ROOMS, List.of(speakers), List.of());
    }

    public static ConferenceScheduleInput inputWithTalks(TalkDTO... talks) {
        return input(TALK_TYPES, TIMESLOTS, ROOMS, SPEAKERS, List.of(talks));
    }

    public static TimeslotDTO timeslot(String id) {
        return timeslot(id, "2024-01-01T09:00", "2024-01-01T10:00");
    }

    public static TimeslotDTO timeslot(String id, String start, String end) {
        return new TimeslotDTO(id, start, end, List.of(BREAKOUT), List.of());
    }

    public static RoomDTO room(String id) {
        return room(id, "Room " + id);
    }

    public static RoomDTO room(String id, String name) {
        return new RoomDTO(id, name, 100, List.of(BREAKOUT), List.of(), List.of());
    }

    public static SpeakerDTO speaker(String id) {
        return speaker(id, "Speaker " + id);
    }

    public static SpeakerDTO speaker(String id, String name) {
        return SpeakerDTO.builder(id, name).build();
    }

    public static TalkDTO talk(String code, String... speakerIds) {
        return TalkDTO.builder(code, "Title " + code, BREAKOUT)
                .speakerIds(List.of(speakerIds))
                .audienceLevel(1)
                .language("en")
                .favoriteCount(1)
                .build();
    }
}
