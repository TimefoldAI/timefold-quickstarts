package org.acme.meetingschedule.demo;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.acme.meetingschedule.dto.input.MeetingInputDTO;
import org.acme.meetingschedule.dto.input.MeetingScheduleInput;
import org.acme.meetingschedule.dto.input.OfficeHoursDTO;
import org.acme.meetingschedule.dto.input.PersonInputDTO;
import org.acme.meetingschedule.dto.input.RoomInputDTO;
import org.acme.meetingschedule.dto.input.TimeConfigurationDTO;

/**
 * Builds a demo dataset of one work week of meetings for a small company.
 * <p>
 * Only the attendee picking is random, and it draws from a {@link Random} seeded with {@link #RANDOM_SEED}, so the
 * dataset is the same on every run. It is deliberately sized so that every meeting fits in one of the rooms and in one
 * office day: no meeting needs more seats than the largest room has, nor more minutes than a day of office hours holds.
 */
public final class DemoDataBuilder {

    private static final long RANDOM_SEED = 0;

    private static final int PEOPLE_COUNT = 20;

    /** Office days, starting the day after today. */
    private static final int DAY_COUNT = 5;
    private static final LocalTime OFFICE_HOURS_START = LocalTime.of(8, 0);
    private static final LocalTime OFFICE_HOURS_END = LocalTime.of(18, 0);
    private static final int GRANULARITY_IN_MINUTES = 15;
    private static final ZoneOffset OFFSET = ZoneOffset.UTC;

    private static final String[] FIRST_NAMES = { "Amy", "Beth", "Carl", "Dan", "Elsa", "Flo", "Gus", "Hugo", "Ivy",
            "Jay", "Jeri", "Hope", "Avis", "Lino", "Lyle", "Nick", "Dino", "Otha", "Gwen", "Jose" };
    private static final String[] LAST_NAMES = { "Cole", "Fox", "Green", "Jones", "King", "Li", "Poe", "Rye", "Smith",
            "Watt", "Howe", "Lowe", "Wise", "Clay", "Carr", "Hood", "Long", "Horn", "Haas", "Meza" };

    private static final String[] TOPICS = {
            "Strategize B2B",
            "Fast track e-business",
            "Cross sell virtualization",
            "Profitize multitasking",
            "Transform one stop shop",
            "Engage braindumps",
            "Downsize data mining",
            "Ramp up policies",
            "On board synergies",
            "Reinvigorate user experience",
            "Strategize e-business",
            "Fast track virtualization",
            "Cross sell multitasking",
            "Profitize one stop shop",
            "Transform braindumps",
            "Engage data mining",
            "Downsize policies",
            "Ramp up synergies",
            "On board user experience",
            "Reinvigorate B2B",
            "Strategize virtualization",
            "Fast track multitasking",
            "Cross sell one stop shop",
            "Reinvigorate multitasking" };

    /** Meeting durations of two, three and four hours, in equal shares. */
    private static final int[] DURATIONS_IN_MINUTES = { 120, 180, 240 };

    /**
     * Number of required and preferred attendees per meeting; their sum stays below {@link #PEOPLE_COUNT}, since the
     * two groups are drawn from one shuffle of the people.
     * <p>
     * Required attendance is a hard constraint, so these counts are kept low enough that nobody is required in so many
     * meetings that they can no longer all be fitted into office hours. Preferred attendance is only a medium
     * constraint, so it can be laid on more thickly: the one meeting with 19 attendees is there to make the room
     * capacity constraint bite, since only two of the three rooms seat that many.
     */
    private static final int[] REQUIRED_ATTENDEE_COUNTS =
            { 2, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 4, 4, 4, 5, 2, 2, 3, 3, 3, 2, 2 };
    private static final int[] PREFERRED_ATTENDEE_COUNTS =
            { 1, 2, 2, 2, 3, 3, 3, 4, 4, 5, 2, 2, 3, 6, 7, 8, 14, 2, 3, 1, 2, 3, 4, 2 };

    private DemoDataBuilder() {
    }

    public static DemoDataBuilder builder() {
        return new DemoDataBuilder();
    }

    public MeetingScheduleInput build() {
        Random random = new Random(RANDOM_SEED);
        List<PersonInputDTO> people = buildPeople();
        List<RoomInputDTO> rooms = List.of(
                new RoomInputDTO("R1", "Room 1", 30),
                new RoomInputDTO("R2", "Room 2", 20),
                new RoomInputDTO("R3", "Room 3", 16));
        return new MeetingScheduleInput(people, rooms, buildTimeConfiguration(), buildMeetings(people, random));
    }

    private static List<PersonInputDTO> buildPeople() {
        List<PersonInputDTO> people = new ArrayList<>(PEOPLE_COUNT);
        for (int i = 0; i < PEOPLE_COUNT; i++) {
            String fullName = "%s %s".formatted(FIRST_NAMES[i % FIRST_NAMES.length],
                    LAST_NAMES[(i * 7) % LAST_NAMES.length]);
            people.add(new PersonInputDTO("P%d".formatted(i + 1), fullName));
        }
        return people;
    }

    /**
     * Office hours of {@value #DAY_COUNT} consecutive days. Evenings and nights are simply not office hours, which is
     * what keeps a meeting from being scheduled overnight.
     */
    private static TimeConfigurationDTO buildTimeConfiguration() {
        LocalDate firstDay = LocalDate.now().plusDays(1);
        List<OfficeHoursDTO> days = new ArrayList<>(DAY_COUNT);
        for (int dayOffset = 0; dayOffset < DAY_COUNT; dayOffset++) {
            LocalDate date = firstDay.plusDays(dayOffset);
            days.add(new OfficeHoursDTO(OffsetDateTime.of(date, OFFICE_HOURS_START, OFFSET),
                    OffsetDateTime.of(date, OFFICE_HOURS_END, OFFSET)));
        }
        return new TimeConfigurationDTO(GRANULARITY_IN_MINUTES, days);
    }

    private static List<MeetingInputDTO> buildMeetings(List<PersonInputDTO> people, Random random) {
        List<MeetingInputDTO> meetings = new ArrayList<>(TOPICS.length);
        for (int i = 0; i < TOPICS.length; i++) {
            // Required and preferred attendees are drawn from one shuffle, so nobody is both.
            List<PersonInputDTO> shuffledPeople = new ArrayList<>(people);
            Collections.shuffle(shuffledPeople, random);
            int requiredCount = REQUIRED_ATTENDEE_COUNTS[i];
            int preferredCount = PREFERRED_ATTENDEE_COUNTS[i];
            meetings.add(new MeetingInputDTO("M%d".formatted(i + 1), TOPICS[i],
                    DURATIONS_IN_MINUTES[i % DURATIONS_IN_MINUTES.length],
                    personIds(shuffledPeople.subList(0, requiredCount)),
                    personIds(shuffledPeople.subList(requiredCount, requiredCount + preferredCount)),
                    null, null, false));
        }
        return meetings;
    }

    private static List<String> personIds(List<PersonInputDTO> people) {
        return people.stream().map(PersonInputDTO::id).toList();
    }
}
