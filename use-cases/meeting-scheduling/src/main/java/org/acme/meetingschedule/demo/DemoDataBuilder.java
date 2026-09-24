package org.acme.meetingschedule.demo;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
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

    /** Office days, starting on the Monday after today. */
    private static final int DAY_COUNT = 5;
    private static final OffsetTime OFFICE_HOURS_START = OffsetTime.of(8, 0, 0, 0, ZoneOffset.UTC);
    private static final OffsetTime OFFICE_HOURS_END = OffsetTime.of(18, 0, 0, 0, ZoneOffset.UTC);
    private static final int GRANULARITY_IN_MINUTES = 15;

    private static final String[] FIRST_NAMES = { "Amy", "Beth", "Carl", "Dan", "Elsa", "Flo", "Gus", "Hugo", "Ivy",
            "Jay", "Jeri", "Hope", "Avis", "Lino", "Lyle", "Nick", "Dino", "Otha", "Gwen", "Jose" };
    private static final String[] LAST_NAMES = { "Cole", "Fox", "Green", "Jones", "King", "Li", "Poe", "Rye", "Smith",
            "Watt", "Howe", "Lowe", "Wise", "Clay", "Carr", "Hood", "Long", "Horn", "Haas", "Meza" };

    /** Meeting durations of two, three and four hours, in equal shares. */
    private static final int[] DURATIONS_IN_MINUTES = { 120, 180, 240 };

    private record MeetingDefinition(String topic, int requiredAttendeeCount, int preferredAttendeeCount) {
    }

    private DemoDataBuilder() {
    }

    public static MeetingScheduleInput basic() {
        Random random = new Random(RANDOM_SEED);
        List<PersonInputDTO> people = buildPeople(20);
        List<RoomInputDTO> rooms = List.of(
                new RoomInputDTO("R1", "Room 1", 30),
                new RoomInputDTO("R2", "Room 2", 20),
                new RoomInputDTO("R3", "Room 3", 16));

        List<MeetingDefinition> meetingDefinitions = List.of(
                new MeetingDefinition("Strategize B2B", 2, 1),
                new MeetingDefinition("Fast track e-business", 2, 2),
                new MeetingDefinition("Cross sell virtualization", 2, 2),
                new MeetingDefinition("Profitize multitasking", 2, 2),
                new MeetingDefinition("Transform one stop shop", 2, 3),
                new MeetingDefinition("Engage braindumps", 2, 3),
                new MeetingDefinition("Downsize data mining", 2, 3),
                new MeetingDefinition("Ramp up policies", 2, 4),
                new MeetingDefinition("On board synergies", 3, 2),
                new MeetingDefinition("Reinvigorate user experience", 3, 5),
                new MeetingDefinition("Strategize e-business", 3, 2),
                new MeetingDefinition("Fast track virtualization", 3, 2),
                new MeetingDefinition("Cross sell multitasking", 3, 3),
                new MeetingDefinition("Profitize one stop shop", 4, 2),
                new MeetingDefinition("Transform braindumps", 4, 3),
                new MeetingDefinition("Engage data mining", 4, 4),
                new MeetingDefinition("Downsize policies", 5, 7),
                new MeetingDefinition("Ramp up synergies", 2, 2),
                new MeetingDefinition("On board user experience", 2, 3),
                new MeetingDefinition("Reinvigorate B2B", 3, 1),
                new MeetingDefinition("Strategize virtualization", 3, 2),
                new MeetingDefinition("Fast track multitasking", 3, 3),
                new MeetingDefinition("Cross sell one stop shop", 2, 4),
                new MeetingDefinition("Reinvigorate multitasking", 2, 2));

        return new MeetingScheduleInput(people, rooms, buildTimeConfiguration(),
                buildMeetings(meetingDefinitions, people, random));
    }

    private static List<PersonInputDTO> buildPeople(int peopleCount) {
        List<PersonInputDTO> people = new ArrayList<>(peopleCount);
        for (int i = 0; i < peopleCount; i++) {
            String fullName = "%s %s".formatted(FIRST_NAMES[i % FIRST_NAMES.length],
                    LAST_NAMES[(i * 7) % LAST_NAMES.length]);
            people.add(new PersonInputDTO("P%d".formatted(i + 1), fullName));
        }
        return people;
    }

    /**
     * Office hours of the {@value #DAY_COUNT} consecutive days that start on the Monday after today, so the schedule
     * always covers a work week rather than straddling a weekend. Evenings and nights are simply not office hours,
     * which is what keeps a meeting from being scheduled overnight.
     */
    private static TimeConfigurationDTO buildTimeConfiguration() {
        LocalDate firstMonday = LocalDate.now(ZoneOffset.UTC).with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        List<OfficeHoursDTO> days = new ArrayList<>(DAY_COUNT);
        for (int dayOffset = 0; dayOffset < DAY_COUNT; dayOffset++) {
            LocalDate date = firstMonday.plusDays(dayOffset);
            days.add(new OfficeHoursDTO(date.atTime(OFFICE_HOURS_START), date.atTime(OFFICE_HOURS_END)));
        }
        return new TimeConfigurationDTO(GRANULARITY_IN_MINUTES, days);
    }

    private static List<MeetingInputDTO> buildMeetings(List<MeetingDefinition> meetingDefinitions,
            List<PersonInputDTO> people, Random random) {
        List<MeetingInputDTO> meetings = new ArrayList<>(meetingDefinitions.size());
        for (int i = 0; i < meetingDefinitions.size(); i++) {
            MeetingDefinition definition = meetingDefinitions.get(i);
            // Required and preferred attendees are drawn from one shuffle, so nobody is both.
            List<PersonInputDTO> shuffledPeople = new ArrayList<>(people);
            Collections.shuffle(shuffledPeople, random);
            int requiredCount = definition.requiredAttendeeCount();
            int preferredCount = definition.preferredAttendeeCount();
            meetings.add(new MeetingInputDTO("M%d".formatted(i + 1), definition.topic(),
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
