package org.acme.meetingschedule.demo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import org.acme.meetingschedule.dto.MeetingAssignmentDTO;
import org.acme.meetingschedule.dto.MeetingDTO;
import org.acme.meetingschedule.dto.MeetingScheduleInput;
import org.acme.meetingschedule.dto.PersonDTO;
import org.acme.meetingschedule.dto.RoomDTO;
import org.acme.meetingschedule.dto.TimeGrainDTO;

public final class DemoDataBuilder {

    private static final int MINIMUM_COUNT = 1;
    private static final int GRAIN_LENGTH_IN_MINUTES = 15;
    private static final String UNASSIGNED = "";

    private static final String[] FIRST_NAMES = { "Amy", "Beth", "Carl", "Dan", "Elsa", "Flo", "Gus", "Hugo", "Ivy", "Jay",
            "Jeri", "Hope", "Avis", "Lino", "Lyle", "Nick", "Dino", "Otha", "Gwen", "Jose", "Dena", "Jana", "Dave",
            "Russ", "Josh", "Dana", "Katy" };
    private static final String[] LAST_NAMES = { "Cole", "Fox", "Green", "Jones", "King", "Li", "Poe", "Rye", "Smith",
            "Watt", "Howe", "Lowe", "Wise", "Clay", "Carr", "Hood", "Long", "Horn", "Haas", "Meza" };
    private static final String[] TOPICS = { "Strategize B2B", "Fast track e-business", "Cross sell virtualization",
            "Profitize multitasking", "Transform one stop shop", "Engage braindumps", "Downsize data mining",
            "Ramp up policies", "On board synergies", "Reinvigorate user experience", "Strategize e-business",
            "Fast track virtualization", "Cross sell multitasking", "Profitize one stop shop", "Transform braindumps",
            "Engage data mining", "Downsize policies", "Ramp up synergies", "On board user experience", "Reinvigorate B2B",
            "Strategize virtualization", "Fast track multitasking", "Cross sell one stop shop", "Reinvigorate multitasking" };

    private int personCount;
    private long randomSeed;

    private DemoDataBuilder() {
    }

    public static DemoDataBuilder builder() {
        return new DemoDataBuilder();
    }

    public DemoDataBuilder setPersonCount(int personCount) {
        this.personCount = personCount;
        return this;
    }

    public DemoDataBuilder setRandomSeed(long randomSeed) {
        this.randomSeed = randomSeed;
        return this;
    }

    public MeetingScheduleInput build() {
        if (personCount < MINIMUM_COUNT) {
            throw new IllegalStateException("Number of people (" + personCount + ") must be greater than zero.");
        }
        Random random = new Random(randomSeed);
        List<PersonDTO> people = buildPeople(random);
        List<TimeGrainDTO> timeGrains = buildTimeGrains();
        List<RoomDTO> rooms = buildRooms();
        Meeting[] meetings = buildMeetings(people, random);
        List<MeetingDTO> meetingDTOs = new ArrayList<>();
        List<MeetingAssignmentDTO> assignmentDTOs = new ArrayList<>();
        appendMeetings(meetings, meetingDTOs, assignmentDTOs);
        return new MeetingScheduleInput(people, timeGrains, rooms, meetingDTOs, assignmentDTOs);
    }

    private List<PersonDTO> buildPeople(Random random) {
        Supplier<String> nameSupplier = () -> {
            Function<String[], String> randomStringSelector = strings -> strings[random.nextInt(strings.length)];
            String firstName = randomStringSelector.apply(FIRST_NAMES);
            String lastName = randomStringSelector.apply(LAST_NAMES);
            return firstName + " " + lastName;
        };
        return IntStream.range(0, personCount)
                .mapToObj(i -> new PersonDTO(String.valueOf(i), nameSupplier.get()))
                .collect(java.util.stream.Collectors.toList());
    }

    private List<TimeGrainDTO> buildTimeGrains() {
        List<TimeGrainDTO> timeGrains = new ArrayList<>();
        LocalDate currentDate = LocalDate.now(ZoneOffset.UTC).plusDays(1);
        LocalDate endDate = LocalDate.now(ZoneOffset.UTC).plusDays(5);
        int count = 0;
        while (currentDate.isBefore(endDate)) {
            LocalTime currentTime = LocalTime.of(8, 0);
            count++;
            timeGrains.add(new TimeGrainDTO(String.valueOf(count), count,
                    LocalDateTime.of(currentDate, currentTime).getDayOfYear(),
                    currentTime.getHour() * 60 + currentTime.getMinute()));
            while (currentTime.isBefore(LocalTime.of(17, 45))) {
                currentTime = currentTime.plusMinutes(GRAIN_LENGTH_IN_MINUTES);
                count++;
                timeGrains.add(new TimeGrainDTO(String.valueOf(count), count,
                        LocalDateTime.of(currentDate, currentTime).getDayOfYear(),
                        currentTime.getHour() * 60 + currentTime.getMinute()));
            }
            currentDate = currentDate.plusDays(1);
        }
        return timeGrains;
    }

    private List<RoomDTO> buildRooms() {
        return List.of(
                new RoomDTO("R1", "Room 1", 30),
                new RoomDTO("R2", "Room 2", 20),
                new RoomDTO("R3", "Room 3", 16));
    }

    private Meeting[] buildMeetings(List<PersonDTO> people, Random random) {
        Meeting[] meetings = new Meeting[TOPICS.length];
        for (int i = 0; i < TOPICS.length; i++) {
            meetings[i] = new Meeting(String.valueOf(i), TOPICS[i]);
        }
        List<Meeting> meetingList = List.of(meetings);
        // Duration
        List<Pair> durationGrainsCount = List.of(
                new Pair(0.33f, 8),
                new Pair(0.33f, 12),
                new Pair(0.33f, 16));
        durationGrainsCount.forEach(p -> applyRandomValue((int) (p.weightRatio() * meetingList.size()), meetingList,
                m -> m.getDurationInGrains() == 0, m -> m.setDurationInGrains(p.targetValue()), random));
        meetingList.stream()
                .filter(m -> m.getDurationInGrains() == 0)
                .forEach(m -> m.setDurationInGrains(8));
        // Required attendants
        BiConsumer<Meeting, Integer> requiredAttendantConsumer = (meeting, size) -> {
            do {
                int nextPerson = random.nextInt(people.size());
                String personId = people.get(nextPerson).id();
                if (!meeting.getRequiredAttendancePersonIds().contains(personId)) {
                    meeting.getRequiredAttendancePersonIds().add(personId);
                }
            } while (meeting.getRequiredAttendancePersonIds().size() < size);
        };
        List<Pair> requiredAttendantsCount = List.of(
                new Pair(0.36f, 2),
                new Pair(0.08f, 3),
                new Pair(0.02f, 4),
                new Pair(0.08f, 5),
                new Pair(0.1f, 6),
                new Pair(0.05f, 7),
                new Pair(0.05f, 8),
                new Pair(0.05f, 10));
        requiredAttendantsCount
                .forEach(p -> applyRandomValue((int) (p.weightRatio() * meetingList.size()), meetingList, p.targetValue(),
                        m -> m.getRequiredAttendancePersonIds().isEmpty(), requiredAttendantConsumer, random));
        meetingList.stream()
                .filter(m -> m.getRequiredAttendancePersonIds().isEmpty())
                .forEach(m -> requiredAttendantConsumer.accept(m, 2));
        // Preferred attendants
        BiConsumer<Meeting, Integer> preferredAttendantConsumer = (meeting, size) -> {
            do {
                int nextPerson = random.nextInt(people.size());
                String personId = people.get(nextPerson).id();
                if (!meeting.getPreferredAttendancePersonIds().contains(personId)
                        && !meeting.getRequiredAttendancePersonIds().contains(personId)) {
                    meeting.getPreferredAttendancePersonIds().add(personId);
                }
            } while (meeting.getPreferredAttendancePersonIds().size() < size);
        };
        List<Pair> preferredAttendantsCount = List.of(
                new Pair(0.06f, 1),
                new Pair(0.2f, 2),
                new Pair(0.18f, 3),
                new Pair(0.06f, 4),
                new Pair(0.04f, 5),
                new Pair(0.04f, 6),
                new Pair(0.04f, 7),
                new Pair(0.04f, 8),
                new Pair(0.08f, 9),
                new Pair(0.04f, 10));
        preferredAttendantsCount
                .forEach(p -> applyRandomValue((int) (p.weightRatio() * meetingList.size()), meetingList, p.targetValue(),
                        m -> m.getPreferredAttendancePersonIds().isEmpty(), preferredAttendantConsumer, random));
        return meetings;
    }

    private void appendMeetings(Meeting[] meetings, List<MeetingDTO> meetingDTOs,
            List<MeetingAssignmentDTO> assignmentDTOs) {
        for (int i = 0; i < meetings.length; i++) {
            Meeting meeting = meetings[i];
            meetingDTOs.add(new MeetingDTO(meeting.getId(), meeting.getTopic(), meeting.getDurationInGrains(),
                    List.copyOf(meeting.getRequiredAttendancePersonIds()),
                    List.copyOf(meeting.getPreferredAttendancePersonIds())));
            assignmentDTOs.add(new MeetingAssignmentDTO(String.valueOf(i), meeting.getId(), UNASSIGNED, UNASSIGNED, false));
        }
    }

    private void applyRandomValue(int count, List<Meeting> values, Predicate<Meeting> filter, Consumer<Meeting> consumer,
            Random random) {
        int size = (int) values.stream().filter(filter).count();
        for (int i = 0; i < count; i++) {
            int skip = size > 0 ? random.nextInt(size) : 0;
            values.stream().filter(filter).skip(skip).findFirst().ifPresent(consumer);
            size--;
            if (size < 0) {
                break;
            }
        }
    }

    private void applyRandomValue(int count, List<Meeting> values, Integer secondParam, Predicate<Meeting> filter,
            BiConsumer<Meeting, Integer> consumer, Random random) {
        int size = (int) values.stream().filter(filter).count();
        for (int i = 0; i < count; i++) {
            int skip = size > 0 ? random.nextInt(size) : 0;
            values.stream().filter(filter).skip(skip).findFirst().ifPresent(v -> consumer.accept(v, secondParam));
            size--;
            if (size < 0) {
                break;
            }
        }
    }

    /** Mutable helper describing a demo meeting before conversion to a DTO. */
    static final class Meeting {
        private final String id;
        private final String topic;
        private int durationInGrains;
        private final List<String> requiredAttendancePersonIds = new ArrayList<>();
        private final List<String> preferredAttendancePersonIds = new ArrayList<>();

        Meeting(String id, String topic) {
            this.id = id;
            this.topic = topic;
        }

        String getId() {
            return id;
        }

        String getTopic() {
            return topic;
        }

        int getDurationInGrains() {
            return durationInGrains;
        }

        void setDurationInGrains(int durationInGrains) {
            this.durationInGrains = durationInGrains;
        }

        List<String> getRequiredAttendancePersonIds() {
            return requiredAttendancePersonIds;
        }

        List<String> getPreferredAttendancePersonIds() {
            return preferredAttendancePersonIds;
        }
    }

    /** Mutable helper pairing a probability weight with a target value. */
    static final class Pair {
        private final float weight;
        private final int target;

        Pair(float weight, int target) {
            this.weight = weight;
            this.target = target;
        }

        float weightRatio() {
            return weight;
        }

        int targetValue() {
            return target;
        }
    }
}
