package org.acme.schooltimetabling.demo;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.acme.schooltimetabling.dto.LessonDTO;
import org.acme.schooltimetabling.dto.RoomDTO;
import org.acme.schooltimetabling.dto.TimeslotDTO;
import org.acme.schooltimetabling.dto.TimetableInput;

public final class DemoDataBuilder {

    private static final String UNSCHEDULED = "";
    private static final int MINIMUM_COUNT = 1;

    private int dayCount;
    private int roomCount;
    private final List<String[]> lessonDefinitions = new ArrayList<>();

    private DemoDataBuilder() {
    }

    public static DemoDataBuilder builder() {
        return new DemoDataBuilder();
    }

    public DemoDataBuilder setDayCount(int dayCount) {
        this.dayCount = dayCount;
        return this;
    }

    public DemoDataBuilder setRoomCount(int roomCount) {
        this.roomCount = roomCount;
        return this;
    }

    public DemoDataBuilder addLesson(String subject, String teacher, String studentGroup) {
        lessonDefinitions.add(new String[] { subject, teacher, studentGroup });
        return this;
    }

    public TimetableInput build() {
        if (dayCount < MINIMUM_COUNT) {
            throw new IllegalStateException("Number of days (" + dayCount + ") must be greater than zero.");
        }
        if (roomCount < MINIMUM_COUNT) {
            throw new IllegalStateException("Number of rooms (" + roomCount + ") must be greater than zero.");
        }
        if (lessonDefinitions.isEmpty()) {
            throw new IllegalStateException("At least one lesson must be defined.");
        }
        return new TimetableInput(buildLessons(), buildTimeslots(), buildRooms());
    }

    private List<TimeslotDTO> buildTimeslots() {
        long timeslotSequence = 0;
        List<TimeslotDTO> timeslots = new ArrayList<>();
        LocalTime[] starts = { LocalTime.of(8, 30), LocalTime.of(9, 30), LocalTime.of(10, 30), LocalTime.of(13, 30),
                LocalTime.of(14, 30) };
        DayOfWeek[] days = DayOfWeek.values();
        for (int day = 0; day < dayCount; day++) {
            for (LocalTime start : starts) {
                String id = Long.toString(timeslotSequence);
                timeslotSequence += 1;
                timeslots.add(new TimeslotDTO(id, days[day].name(), start.toString(), start.plusHours(1).toString()));
            }
        }
        return timeslots;
    }

    private List<RoomDTO> buildRooms() {
        List<RoomDTO> rooms = new ArrayList<>();
        for (int room = 0; room < roomCount; room++) {
            rooms.add(new RoomDTO(Long.toString(room), "Room " + (char) ('A' + room)));
        }
        return rooms;
    }

    private List<LessonDTO> buildLessons() {
        long lessonSequence = 0;
        List<LessonDTO> lessons = new ArrayList<>();
        for (String[] definition : lessonDefinitions) {
            String id = Long.toString(lessonSequence);
            lessonSequence += 1;
            lessons.add(new LessonDTO(id, definition[0], definition[1], definition[2], UNSCHEDULED, UNSCHEDULED));
        }
        return lessons;
    }
}
