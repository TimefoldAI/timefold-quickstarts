package org.acme.conferencescheduling.demo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.acme.conferencescheduling.dto.input.ConferenceScheduleInput;
import org.acme.conferencescheduling.dto.input.RoomDTO;
import org.acme.conferencescheduling.dto.input.SpeakerDTO;
import org.acme.conferencescheduling.dto.input.TagPreferencesDTO;
import org.acme.conferencescheduling.dto.input.TalkDTO;
import org.acme.conferencescheduling.dto.input.TalkTypeDTO;
import org.acme.conferencescheduling.dto.input.TimeslotDTO;

public final class DemoDataBuilder {

    private static final String LAB = "Lab";
    private static final String BREAKOUT = "Breakout";
    private static final String AFTER_LUNCH = "After lunch";
    private static final String RECORDED = "Recorded";
    private static final String LARGE = "Large";

    private static final List<String> THEME_TAGS = List.of("Optimization", "AI", "Cloud");
    private static final List<String> SECTOR_TAGS = List.of("Green", "Blue", "Orange");
    private static final List<String> AUDIENCE_TAGS = List.of("Programmers", "Analysts", "Managers");
    private static final List<String> CONTENT_TAGS = List.of("Timefold", "Constraints", "Metaheuristics", "Kubernetes");
    private static final LocalDate CONFERENCE_DATE = LocalDate.of(2024, 1, 1);

    private DemoDataBuilder() {
    }

    public static DemoDataBuilder builder() {
        return new DemoDataBuilder();
    }

    public ConferenceScheduleInput build() {
        List<TalkTypeDTO> talkTypes = List.of(new TalkTypeDTO(LAB), new TalkTypeDTO(BREAKOUT));
        return new ConferenceScheduleInput("Conference", talkTypes, buildTimeslots(), buildRooms(), buildSpeakers(),
                buildTalks());
    }

    private static LocalDateTime dateTime(int hour, int minute) {
        return LocalDateTime.of(CONFERENCE_DATE, LocalTime.of(hour, minute));
    }

    private static List<TimeslotDTO> buildTimeslots() {
        List<TimeslotDTO> timeslots = new ArrayList<>();
        timeslots.add(new TimeslotDTO("T1", dateTime(10, 15), dateTime(12, 15), List.of(LAB), List.of()));
        timeslots.add(new TimeslotDTO("T2", dateTime(10, 15), dateTime(11, 0), List.of(BREAKOUT), List.of()));
        timeslots.add(new TimeslotDTO("T3", dateTime(11, 30), dateTime(12, 15), List.of(BREAKOUT), List.of()));
        timeslots.add(new TimeslotDTO("T4", dateTime(13, 0), dateTime(15, 0), List.of(LAB), List.of(AFTER_LUNCH)));
        timeslots.add(new TimeslotDTO("T5", dateTime(15, 30), dateTime(16, 15), List.of(BREAKOUT), List.of()));
        timeslots.add(new TimeslotDTO("T6", dateTime(16, 30), dateTime(17, 15), List.of(BREAKOUT), List.of()));
        return timeslots;
    }

    private static List<RoomDTO> buildRooms() {
        List<RoomDTO> rooms = new ArrayList<>();
        rooms.add(new RoomDTO("R1", "Room A", 60, List.of(BREAKOUT), List.of(), List.of(RECORDED)));
        rooms.add(new RoomDTO("R2", "Room B", 240, List.of(BREAKOUT), List.of(), List.of()));
        rooms.add(new RoomDTO("R3", "Room C", 630, List.of(BREAKOUT), List.of(), List.of(RECORDED, LARGE)));
        rooms.add(new RoomDTO("R4", "Room D", 70, List.of(BREAKOUT), List.of(), List.of(RECORDED)));
        rooms.add(new RoomDTO("R5", "Room E (LAB)", 490, List.of(LAB), List.of(), List.of(RECORDED)));
        return rooms;
    }

    private static SpeakerDTO speaker(String id, String name) {
        return speaker(id, name, List.of());
    }

    private static SpeakerDTO speaker(String id, String name, List<String> undesiredTimeslotTags) {
        return new SpeakerDTO(id, name, List.of(),
                new TagPreferencesDTO(List.of(), List.of(), List.of(), undesiredTimeslotTags),
                TagPreferencesDTO.EMPTY);
    }

    private static List<SpeakerDTO> buildSpeakers() {
        List<SpeakerDTO> speakers = new ArrayList<>();
        speakers.add(speaker("1", "Amy Cole"));
        speakers.add(speaker("2", "Beth Fox"));
        speakers.add(speaker("3", "Carl Green"));
        speakers.add(speaker("4", "Dan Jones"));
        speakers.add(speaker("5", "Elsa King"));
        speakers.add(speaker("6", "Flo Li"));
        speakers.add(speaker("7", "Gus Poe"));
        speakers.add(speaker("8", "Hugo Rye"));
        speakers.add(speaker("9", "Ivy Smith"));
        speakers.add(speaker("10", "Jay Watt"));
        speakers.add(speaker("11", "Amy Fox"));
        speakers.add(speaker("12", "Beth Green", List.of(AFTER_LUNCH)));
        return speakers;
    }

    private static TalkDTO talk(int index, String code, String title, String talkType, List<String> speakerIds,
            int audienceLevel, int favoriteCount, int crowdControlRisk) {
        return talk(index, code, title, talkType, speakerIds, audienceLevel, favoriteCount, crowdControlRisk,
                List.of(), List.of(), List.of(), List.of());
    }

    private static TalkDTO talk(int index, String code, String title, String talkType, List<String> speakerIds,
            int audienceLevel, int favoriteCount, int crowdControlRisk, List<String> requiredRoomTags,
            List<String> undesiredRoomTags, List<String> mutuallyExclusiveTalksTags, List<String> prerequisiteTalkCodes) {
        return new TalkDTO(code, title, talkType, speakerIds,
                List.of(THEME_TAGS.get(index % THEME_TAGS.size())),
                List.of(SECTOR_TAGS.get(index % SECTOR_TAGS.size())),
                List.of(CONTENT_TAGS.get(index % CONTENT_TAGS.size())),
                List.of(AUDIENCE_TAGS.get(index % AUDIENCE_TAGS.size())),
                audienceLevel,
                "en",
                TagPreferencesDTO.EMPTY,
                new TagPreferencesDTO(requiredRoomTags, List.of(), List.of(), undesiredRoomTags),
                mutuallyExclusiveTalksTags, prerequisiteTalkCodes, favoriteCount, crowdControlRisk, null, null);
    }

    private static List<TalkDTO> buildTalks() {
        List<TalkDTO> talks = new ArrayList<>();
        talks.add(talk(0, "S01", "Talk One", LAB, List.of("1", "2"), 2, 551, 1,
                List.of(), List.of(RECORDED), List.of(), List.of()));
        talks.add(talk(1, "S02", "Talk Two", LAB, List.of("3"), 3, 528, 0));
        talks.add(talk(2, "S03", "Talk Three", BREAKOUT, List.of("4"), 3, 497, 0));
        talks.add(talk(3, "S04", "Talk Four", BREAKOUT, List.of("5", "6"), 1, 560, 0));
        talks.add(talk(4, "S05", "Talk Five", BREAKOUT, List.of("7", "8"), 1, 957, 0,
                List.of(), List.of(), List.of(), List.of("S02")));
        talks.add(talk(5, "S06", "Talk Six", BREAKOUT, List.of("9"), 1, 957, 0));
        talks.add(talk(6, "S07", "Talk Seven", BREAKOUT, List.of("10"), 3, 568, 0));
        talks.add(talk(7, "S08", "Talk Eight", BREAKOUT, List.of("11"), 3, 183, 0));
        talks.add(talk(8, "S09", "Talk Nine", BREAKOUT, List.of("12", "1"), 3, 619, 0));
        talks.add(talk(9, "S10", "Talk Ten", BREAKOUT, List.of("2", "3"), 3, 603, 1));
        talks.add(talk(10, "S11", "Talk Eleven", BREAKOUT, List.of("4", "5"), 1, 39, 0,
                List.of(RECORDED), List.of(), List.of("Constraints"), List.of()));
        talks.add(talk(11, "S12", "Talk Twelve", BREAKOUT, List.of("6", "7"), 3, 977, 0));
        talks.add(talk(12, "S13", "Talk Thirteen", BREAKOUT, List.of("8"), 3, 494, 0));
        talks.add(talk(13, "S14", "Talk Fourteen", BREAKOUT, List.of("9"), 3, 500, 0));
        talks.add(talk(14, "S15", "Talk Fifteen", BREAKOUT, List.of("10"), 2, 658, 0));
        return talks;
    }
}
