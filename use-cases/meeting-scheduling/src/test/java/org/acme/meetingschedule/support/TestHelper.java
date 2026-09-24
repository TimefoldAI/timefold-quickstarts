package org.acme.meetingschedule.support;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import org.acme.meetingschedule.domain.Meeting;
import org.acme.meetingschedule.domain.MeetingAssignment;
import org.acme.meetingschedule.domain.Person;
import org.acme.meetingschedule.domain.PreferredAttendance;
import org.acme.meetingschedule.domain.RequiredAttendance;
import org.acme.meetingschedule.domain.Room;
import org.acme.meetingschedule.domain.TimeGrain;
import org.acme.meetingschedule.dto.input.MeetingInputDTO;
import org.acme.meetingschedule.dto.input.MeetingScheduleInput;
import org.acme.meetingschedule.dto.input.OfficeHoursDTO;
import org.acme.meetingschedule.dto.input.PersonInputDTO;
import org.acme.meetingschedule.dto.input.RoomInputDTO;
import org.acme.meetingschedule.dto.input.TimeConfigurationDTO;

// To keep our production classes as simple as possible, we've added these methods to help construct the data needed for testing.
public final class TestHelper {

    private static final LocalDate DEFAULT_DATE = LocalDate.of(2024, 1, 1);
    private static final LocalTime OFFICE_HOURS_START = LocalTime.of(8, 0);
    private static final LocalTime OFFICE_HOURS_END = LocalTime.of(18, 0);
    private static final int DEFAULT_GRANULARITY_IN_MINUTES = 15;
    private static final ZoneOffset OFFSET = ZoneOffset.UTC;

    private TestHelper() {
    }

    // ************************************************************************
    // Solver model
    // ************************************************************************

    public static PersonBuilder aPerson(String id) {
        return new PersonBuilder(id);
    }

    public static RoomBuilder aRoom(String id) {
        return new RoomBuilder(id);
    }

    public static TimeGrainBuilder aTimeGrain(String id) {
        return new TimeGrainBuilder(id);
    }

    public static MeetingBuilder aMeeting(String id) {
        return new MeetingBuilder(id);
    }

    public static RequiredAttendance aRequiredAttendance(String id, MeetingBuilder meeting, PersonBuilder person) {
        return new RequiredAttendance(id, meeting.build(), person.build());
    }

    public static PreferredAttendance aPreferredAttendance(String id, MeetingBuilder meeting, PersonBuilder person) {
        return new PreferredAttendance(id, meeting.build(), person.build());
    }

    public static MeetingAssignmentBuilder anAssignment(String id, MeetingBuilder meeting) {
        return new MeetingAssignmentBuilder(id, meeting);
    }

    public static final class PersonBuilder {

        private final String id;
        private String fullName;

        private PersonBuilder(String id) {
            this.id = id;
            this.fullName = "Person " + id;
        }

        public PersonBuilder fullName(String fullName) {
            this.fullName = fullName;
            return this;
        }

        public Person build() {
            return new Person(id, fullName);
        }
    }

    public static final class RoomBuilder {

        private final String id;
        private String name;
        private int capacity = 4;

        private RoomBuilder(String id) {
            this.id = id;
            this.name = "Room " + id;
        }

        public RoomBuilder name(String name) {
            this.name = name;
            return this;
        }

        public RoomBuilder capacity(int capacity) {
            this.capacity = capacity;
            return this;
        }

        public Room build() {
            return new Room(id, name, capacity);
        }
    }

    public static final class TimeGrainBuilder {

        private final String id;
        private int grainIndex;
        private LocalDate date = DEFAULT_DATE;
        private int lengthInMinutes = DEFAULT_GRANULARITY_IN_MINUTES;

        private TimeGrainBuilder(String id) {
            this.id = id;
        }

        public TimeGrainBuilder grainIndex(int grainIndex) {
            this.grainIndex = grainIndex;
            return this;
        }

        public TimeGrainBuilder date(LocalDate date) {
            this.date = date;
            return this;
        }

        public TimeGrainBuilder lengthInMinutes(int lengthInMinutes) {
            this.lengthInMinutes = lengthInMinutes;
            return this;
        }

        public TimeGrain build() {
            LocalTime time = OFFICE_HOURS_START.plusMinutes((long) grainIndex * lengthInMinutes);
            return new TimeGrain(id, grainIndex, OffsetDateTime.of(date, time, OFFSET), lengthInMinutes);
        }
    }

    public static final class MeetingBuilder {

        private final String id;
        private String topic;
        private int durationInGrains = 4;
        private int requiredCapacity;

        private MeetingBuilder(String id) {
            this.id = id;
            this.topic = "Meeting " + id;
        }

        public MeetingBuilder topic(String topic) {
            this.topic = topic;
            return this;
        }

        public MeetingBuilder durationInGrains(int durationInGrains) {
            this.durationInGrains = durationInGrains;
            return this;
        }

        public MeetingBuilder requiredCapacity(int requiredCapacity) {
            this.requiredCapacity = requiredCapacity;
            return this;
        }

        public Meeting build() {
            return new Meeting(id, topic, durationInGrains, requiredCapacity);
        }
    }

    public static final class MeetingAssignmentBuilder {

        private final String id;
        private final MeetingBuilder meeting;
        private TimeGrainBuilder startingTimeGrain;
        private RoomBuilder room;
        private boolean pinned;

        private MeetingAssignmentBuilder(String id, MeetingBuilder meeting) {
            this.id = id;
            this.meeting = meeting;
        }

        public MeetingAssignmentBuilder startingTimeGrain(TimeGrainBuilder startingTimeGrain) {
            this.startingTimeGrain = startingTimeGrain;
            return this;
        }

        public MeetingAssignmentBuilder room(RoomBuilder room) {
            this.room = room;
            return this;
        }

        public MeetingAssignmentBuilder pinned(boolean pinned) {
            this.pinned = pinned;
            return this;
        }

        public MeetingAssignment build() {
            return new MeetingAssignment(id, meeting.build(),
                    startingTimeGrain == null ? null : startingTimeGrain.build(),
                    room == null ? null : room.build(), pinned);
        }
    }

    // ************************************************************************
    // Input DTOs
    // ************************************************************************

    public static MeetingScheduleInput input(List<PersonInputDTO> people, List<RoomInputDTO> rooms,
            TimeConfigurationDTO timeConfiguration, List<MeetingInputDTO> meetings) {
        return new MeetingScheduleInput(people, rooms, timeConfiguration, meetings);
    }

    public static PersonDTOBuilder aPersonDTO(String id) {
        return new PersonDTOBuilder(id);
    }

    public static RoomDTOBuilder aRoomDTO(String id) {
        return new RoomDTOBuilder(id);
    }

    public static TimeConfigurationDTOBuilder aTimeConfigurationDTO() {
        return new TimeConfigurationDTOBuilder();
    }

    public static OfficeHoursDTOBuilder anOfficeHoursDTO() {
        return new OfficeHoursDTOBuilder();
    }

    public static MeetingDTOBuilder aMeetingDTO(String id) {
        return new MeetingDTOBuilder(id);
    }

    /**
     * A small, deliberately conflict-free problem: the meetings that share attendees have plenty of
     * time grains to move apart into, and nobody is a preferred attendee, so both a feasible score
     * and a zero medium score are within easy reach of the solver.
     */
    public static MeetingScheduleInput createProblem() {
        List<PersonInputDTO> people = List.of(aPersonDTO("P1").build(), aPersonDTO("P2").build(),
                aPersonDTO("P3").build(), aPersonDTO("P4").build());
        List<RoomInputDTO> rooms = List.of(aRoomDTO("R1").build(), aRoomDTO("R2").build(), aRoomDTO("R3").build());
        List<MeetingInputDTO> meetings = List.of(
                aMeetingDTO("M1").requiredAttendeeIds(List.of("P1", "P2")).build(),
                aMeetingDTO("M2").requiredAttendeeIds(List.of("P3", "P4")).build(),
                aMeetingDTO("M3").requiredAttendeeIds(List.of("P1", "P2")).build(),
                aMeetingDTO("M4").requiredAttendeeIds(List.of("P3", "P4")).build());
        return input(people, rooms, officeDay(), meetings);
    }

    /**
     * @return one day of 08:00 to 18:00 office hours on a 15 minute grid, so 40 slots a meeting can start in and no
     *         reason for any meeting to span two days
     */
    public static TimeConfigurationDTO officeDay() {
        return aTimeConfigurationDTO().build();
    }

    public static final class PersonDTOBuilder {

        private final String id;
        private String fullName;

        private PersonDTOBuilder(String id) {
            this.id = id;
            this.fullName = "Person " + id;
        }

        public PersonDTOBuilder fullName(String fullName) {
            this.fullName = fullName;
            return this;
        }

        public PersonInputDTO build() {
            return new PersonInputDTO(id, fullName);
        }
    }

    public static final class RoomDTOBuilder {

        private final String id;
        private String name;
        private Integer capacity = 4;

        private RoomDTOBuilder(String id) {
            this.id = id;
            this.name = "Room " + id;
        }

        public RoomDTOBuilder name(String name) {
            this.name = name;
            return this;
        }

        public RoomDTOBuilder capacity(Integer capacity) {
            this.capacity = capacity;
            return this;
        }

        public RoomInputDTO build() {
            return new RoomInputDTO(id, name, capacity);
        }
    }

    public static final class OfficeHoursDTOBuilder {

        private LocalDate date = DEFAULT_DATE;
        private LocalTime startTime = OFFICE_HOURS_START;
        private LocalTime endTime = OFFICE_HOURS_END;

        private OfficeHoursDTOBuilder() {
        }

        public OfficeHoursDTOBuilder date(LocalDate date) {
            this.date = date;
            return this;
        }

        public OfficeHoursDTOBuilder startTime(LocalTime startTime) {
            this.startTime = startTime;
            return this;
        }

        public OfficeHoursDTOBuilder endTime(LocalTime endTime) {
            this.endTime = endTime;
            return this;
        }

        public OffsetDateTime startDateTime() {
            return OffsetDateTime.of(date, startTime, OFFSET);
        }

        public OfficeHoursDTO build() {
            return new OfficeHoursDTO(startDateTime(), OffsetDateTime.of(date, endTime, OFFSET));
        }
    }

    public static final class TimeConfigurationDTOBuilder {

        private Integer granularityInMinutes = DEFAULT_GRANULARITY_IN_MINUTES;
        private List<OfficeHoursDTOBuilder> days = List.of(anOfficeHoursDTO());

        private TimeConfigurationDTOBuilder() {
        }

        public TimeConfigurationDTOBuilder granularityInMinutes(Integer granularityInMinutes) {
            this.granularityInMinutes = granularityInMinutes;
            return this;
        }

        public TimeConfigurationDTOBuilder days(List<OfficeHoursDTOBuilder> days) {
            this.days = days;
            return this;
        }

        /**
         * @return consecutive office days, each with the default 08:00 to 18:00 office hours
         */
        public TimeConfigurationDTOBuilder dayCount(int dayCount) {
            List<OfficeHoursDTOBuilder> officeDays = new ArrayList<>(dayCount);
            for (int dayOffset = 0; dayOffset < dayCount; dayOffset++) {
                officeDays.add(anOfficeHoursDTO().date(DEFAULT_DATE.plusDays(dayOffset)));
            }
            return days(officeDays);
        }

        public TimeConfigurationDTO build() {
            return new TimeConfigurationDTO(granularityInMinutes,
                    days.stream().map(OfficeHoursDTOBuilder::build).toList());
        }
    }

    public static final class MeetingDTOBuilder {

        private final String id;
        private String topic;
        private Integer durationInMinutes = 60;
        private List<String> requiredAttendeeIds = List.of();
        private List<String> preferredAttendeeIds = List.of();
        private String roomId;
        private OffsetDateTime startDateTime;
        private Boolean pinned = false;

        private MeetingDTOBuilder(String id) {
            this.id = id;
            this.topic = "Meeting " + id;
        }

        public MeetingDTOBuilder topic(String topic) {
            this.topic = topic;
            return this;
        }

        public MeetingDTOBuilder durationInMinutes(Integer durationInMinutes) {
            this.durationInMinutes = durationInMinutes;
            return this;
        }

        public MeetingDTOBuilder requiredAttendeeIds(List<String> requiredAttendeeIds) {
            this.requiredAttendeeIds = requiredAttendeeIds;
            return this;
        }

        public MeetingDTOBuilder preferredAttendeeIds(List<String> preferredAttendeeIds) {
            this.preferredAttendeeIds = preferredAttendeeIds;
            return this;
        }

        public MeetingDTOBuilder roomId(String roomId) {
            this.roomId = roomId;
            return this;
        }

        public MeetingDTOBuilder startDateTime(OffsetDateTime startDateTime) {
            this.startDateTime = startDateTime;
            return this;
        }

        public MeetingDTOBuilder pinned(Boolean pinned) {
            this.pinned = pinned;
            return this;
        }

        public MeetingInputDTO build() {
            return new MeetingInputDTO(id, topic, durationInMinutes, requiredAttendeeIds, preferredAttendeeIds, roomId,
                    startDateTime, pinned);
        }
    }
}
