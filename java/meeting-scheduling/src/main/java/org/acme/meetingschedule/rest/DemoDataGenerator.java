package org.acme.meetingschedule.rest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import jakarta.enterprise.context.ApplicationScoped;

import org.acme.meetingschedule.domain.Meeting;
import org.acme.meetingschedule.domain.MeetingAssignment;
import org.acme.meetingschedule.domain.MeetingSchedule;
import org.acme.meetingschedule.domain.Person;
import org.acme.meetingschedule.domain.Room;
import org.acme.meetingschedule.domain.TimeGrain;

@ApplicationScoped
public class DemoDataGenerator {

    private static final String[] FIRST_NAMES = {"Amy", "Beth", "Carl", "Dan", "Elsa", "Flo", "Gus", "Hugo", "Ivy", "Jay",
            "Jeri", "Hope", "Avis", "Lino", "Lyle", "Nick", "Dino", "Otha", "Gwen", "Jose", "Dena", "Jana", "Dave",
            "Russ", "Josh", "Dana", "Katy"};
    private static final String[] LAST_NAMES =
            {"Cole", "Fox", "Green", "Jones", "King", "Li", "Poe", "Rye", "Smith", "Watt", "Howe", "Lowe", "Wise", "Clay",
                    "Carr", "Hood", "Long", "Horn", "Haas", "Meza"};

    public MeetingSchedule generateDemoData() {
        Random random = new Random(0);
        MeetingSchedule schedule = new MeetingSchedule();
        // People
        int countPeople = 20;
        List<Person> people = generatePeople(countPeople, random);
        // Time grain
        List<TimeGrain> timeGrains = generateTimeGrain();
        // Rooms
        List<Room> rooms = List.of(
                new Room("R1", "Room 1", 30),
                new Room("R2", "Room 2", 20),
                new Room("R3", "Room 3", 16));
        // Meetings
        List<Meeting> meetings = generateMeetings(people, random);
        // Meeting assignments
        List<MeetingAssignment> meetingAssignments = generateMeetingAssignments(meetings);
        // Update schedule
        schedule.setRooms(rooms);
        schedule.setPeople(people);
        schedule.setTimeGrains(timeGrains);
        schedule.setMeetings(meetings);
        schedule.setMeetingAssignments(meetingAssignments);
        schedule.setAttendances(Stream.concat(
                        schedule.getMeetings().stream().flatMap(m -> m.getRequiredAttendances().stream()),
                        schedule.getMeetings().stream().flatMap(m -> m.getPreferredAttendances().stream()))
                .toList());
        return schedule;
    }

    private List<Person> generatePeople(int countPeople, Random random) {
        Supplier<String> nameSupplier = () -> {
            Function<String[], String> randomStringSelector = strings -> strings[random.nextInt(strings.length)];
            String firstName = randomStringSelector.apply(FIRST_NAMES);
            String lastName = randomStringSelector.apply(LAST_NAMES);
            return firstName + " " + lastName;
        };

        return IntStream.range(0, countPeople)
                .mapToObj(i -> new Person(String.valueOf(i), nameSupplier.get()))
                .toList();
    }

    private List<TimeGrain> generateTimeGrain() {
        List<TimeGrain> timeGrains = new ArrayList<>();
        LocalDate currentDate = LocalDate.now().plusDays(1);
        int count = 0;
        while (currentDate.isBefore(LocalDate.now().plusDays(5))) {
            LocalTime currentTime = LocalTime.of(8, 0);
            timeGrains.add(new TimeGrain(String.valueOf(++count), count,
                    LocalDateTime.of(currentDate, currentTime).getDayOfYear(),
                    currentTime.getHour() * 60 + currentTime.getMinute()));
            while (currentTime.isBefore(LocalTime.of(17, 45))) {
                currentTime = currentTime.plusMinutes(15);
                timeGrains.add(new TimeGrain(String.valueOf(++count), count,
                        LocalDateTime.of(currentDate, currentTime).getDayOfYear(),
                        currentTime.getHour() * 60 + currentTime.getMinute()));
            }
            currentDate = currentDate.plusDays(1);
        }
        return timeGrains;
    }

    private List<Meeting> generateMeetings(List<Person> people, Random random) {
        int count = 0;
        List<Meeting> meetings = List.of(
                new Meeting(String.valueOf(count++), "Strategize B2B"),
                new Meeting(String.valueOf(count++), "Fast track e-business"),
                new Meeting(String.valueOf(count++), "Cross sell virtualization"),
                new Meeting(String.valueOf(count++), "Profitize multitasking"),
                new Meeting(String.valueOf(count++), "Transform one stop shop"),
                new Meeting(String.valueOf(count++), "Engage braindumps"),
                new Meeting(String.valueOf(count++), "Downsize data mining"),
                new Meeting(String.valueOf(count++), "Ramp up policies"),
                new Meeting(String.valueOf(count++), "On board synergies"),
                new Meeting(String.valueOf(count++), "Reinvigorate user experience"),
                new Meeting(String.valueOf(count++), "Strategize e-business"),
                new Meeting(String.valueOf(count++), "Fast track virtualization"),
                new Meeting(String.valueOf(count++), "Cross sell multitasking"),
                new Meeting(String.valueOf(count++), "Profitize one stop shop"),
                new Meeting(String.valueOf(count++), "Transform braindumps"),
                new Meeting(String.valueOf(count++), "Engage data mining"),
                new Meeting(String.valueOf(count++), "Downsize policies"),
                new Meeting(String.valueOf(count++), "Ramp up synergies"),
                new Meeting(String.valueOf(count++), "On board user experience"),
                new Meeting(String.valueOf(count++), "Reinvigorate B2B"),
                new Meeting(String.valueOf(count++), "Strategize virtualization"),
                new Meeting(String.valueOf(count++), "Fast track multitasking"),
                new Meeting(String.valueOf(count++), "Cross sell one stop shop"),
                new Meeting(String.valueOf(count), "Reinvigorate multitasking"));
        // Duration
        List<Pair<Float, Integer>> durationGrainsCount = List.of(
                new Pair<>(0.33f, 8),
                new Pair<>(0.33f, 12),
                new Pair<>(0.33f, 16));
        durationGrainsCount.forEach(p -> applyRandomValue((int) (p.key() * meetings.size()), meetings,
                m -> m.getDurationInGrains() == 0, m -> m.setDurationInGrains(p.value()), random));
        // Ensure there are no empty duration
        meetings.stream()
                .filter(m -> m.getDurationInGrains() == 0)
                .forEach(m -> m.setDurationInGrains(8));
        // Attendants
        // Required
        BiConsumer<Meeting, Integer> requiredAttendantConsumer = (meeting, size) -> {
            do {
                int nextPerson = random.nextInt(people.size());
                boolean assignedToRequired = meeting.getRequiredAttendances().stream()
                        .anyMatch(requiredAttendance -> requiredAttendance.getPerson().equals(people.get(nextPerson)));
                if (!assignedToRequired) {
                    meeting.addRequiredAttendant(people.get(nextPerson));
                }
            } while (meeting.getRequiredAttendances().size() < size);
        };
        List<Pair<Float, Integer>> requiredAttendantsCount = List.of(
                new Pair<>(0.36f, 2), // 36% with two attendants
                new Pair<>(0.08f, 3), // 8% with three attendants, etc
                new Pair<>(0.02f, 4),
                new Pair<>(0.08f, 5),
                new Pair<>(0.1f, 6),
                new Pair<>(0.05f, 7),
                new Pair<>(0.05f, 8),
                new Pair<>(0.05f, 10));
        requiredAttendantsCount.forEach(p -> applyRandomValue((int) (p.key() * meetings.size()), meetings, p.value(),
                m -> m.getRequiredAttendances().isEmpty(), requiredAttendantConsumer, random));
        // Ensure there are no empty required attendants
        meetings.stream()
                .filter(m -> m.getRequiredAttendances() == null)
                .forEach(m -> requiredAttendantConsumer.accept(m, 2));
        // Preferred
        BiConsumer<Meeting, Integer> preferredAttendantConsumer = (meeting, size) -> {
            do {
                int nextPerson = random.nextInt(people.size());
                boolean assignedToPreferred = meeting.getPreferredAttendances().stream()
                        .anyMatch(preferredAttendance -> preferredAttendance.getPerson().equals(people.get(nextPerson)));
                boolean assignedToRequired = meeting.getRequiredAttendances().stream()
                        .anyMatch(requiredAttendance -> requiredAttendance.getPerson().equals(people.get(nextPerson)));
                if (!assignedToPreferred && !assignedToRequired) {
                    meeting.addPreferredAttendant(people.get(nextPerson));
                }
            } while (meeting.getPreferredAttendances().size() < size);
        };
        List<Pair<Float, Integer>> preferredAttendantsCount = List.of(
                new Pair<>(0.06f, 1), // 6% with one attendant
                new Pair<>(0.2f, 2), // 20% with two attendants, etc
                new Pair<>(0.18f, 3),
                new Pair<>(0.06f, 4),
                new Pair<>(0.04f, 5),
                new Pair<>(0.04f, 6),
                new Pair<>(0.04f, 7),
                new Pair<>(0.04f, 8),
                new Pair<>(0.08f, 9),
                new Pair<>(0.04f, 10));
        preferredAttendantsCount.forEach(p -> applyRandomValue((int) (p.key() * meetings.size()), meetings, p.value(),
                m -> m.getPreferredAttendances().isEmpty(), preferredAttendantConsumer, random));
        return meetings;
    }

    private List<MeetingAssignment> generateMeetingAssignments(List<Meeting> meetings) {
        return IntStream.range(0, meetings.size())
                .mapToObj(i -> new MeetingAssignment(String.valueOf(i), meetings.get(i)))
                .toList();
    }

    private <T> void applyRandomValue(int count, List<T> values, Predicate<T> filter, Consumer<T> consumer, Random random) {
        int size = (int) values.stream().filter(filter).count();
        for (int i = 0; i < count; i++) {
            values.stream()
                    .filter(filter)
                    .skip(size > 0 ? random.nextInt(size) : 0).findFirst()
                    .ifPresent(consumer);
            size--;
            if (size < 0) {
                break;
            }
        }
    }

    private <T, L> void applyRandomValue(int count, List<T> values, L secondParam, Predicate<T> filter,
                                         BiConsumer<T, L> consumer, Random random) {
        int size = (int) values.stream().filter(filter).count();
        for (int i = 0; i < count; i++) {
            values.stream()
                    .filter(filter)
                    .skip(size > 0 ? random.nextInt(size) : 0).findFirst()
                    .ifPresent(v -> consumer.accept(v, secondParam));
            size--;
            if (size < 0) {
                break;
            }
        }
    }

    private record Pair<K, V>(K key, V value) {
    }
}
