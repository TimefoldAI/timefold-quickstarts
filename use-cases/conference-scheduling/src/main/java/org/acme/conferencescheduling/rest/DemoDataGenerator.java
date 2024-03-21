package org.acme.conferencescheduling.rest;

import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toSet;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;

import org.acme.conferencescheduling.domain.ConferenceConstraintConfiguration;
import org.acme.conferencescheduling.domain.ConferenceSchedule;
import org.acme.conferencescheduling.domain.Room;
import org.acme.conferencescheduling.domain.Speaker;
import org.acme.conferencescheduling.domain.Talk;
import org.acme.conferencescheduling.domain.TalkType;
import org.acme.conferencescheduling.domain.Timeslot;

@ApplicationScoped
public class DemoDataGenerator {

    // Talk types
    private static final String BREAKOUT_TALK_TAG = "Breakout";
    private static final String LAB_TALK_TAG = "Lab";
    // Tags
    private static final String AFTER_LUNCH_TAG = "After lunch";
    private static final String RECORDED_TAG = "Recorded";
    private static final String LARGE_TAG = "Large";
    // Theme tags
    private static final List<String> THEME_TAGS = List.of("Optimization", "AI", "Cloud");
    // Sector tags
    private static final List<String> SECTOR_TAGS = List.of("Green", "Blue", "Orange");
    // Audience tags
    private static final List<String> AUDIENCE_TAGS = List.of("Programmers", "Analysts", "Managers");
    // Content tags
    private static final List<String> CONTENT_TAGS = List.of("Timefold", "Constraints", "Metaheuristics", "Kubernetes");
    private final Random random = new Random(0);

    private static final Set<TalkType> TALK_TYPES = buildSet(List.of(
            new TalkType(LAB_TALK_TAG),
            new TalkType(BREAKOUT_TALK_TAG)));

    public ConferenceSchedule generateDemoData() {
        Set<Speaker> speakers = generateSpeakers();
        ConferenceSchedule schedule = new ConferenceSchedule("Conference", TALK_TYPES, generateTimeslots(), generateRooms(),
                speakers, generateTalks(speakers));
        schedule.setConstraintConfiguration(new ConferenceConstraintConfiguration());
        return schedule;
    }

    private Set<Timeslot> generateTimeslots() {
        return buildSet(List.of(
                new Timeslot("T1", LocalDateTime.now().withHour(10).withMinute(15).withSecond(0).withNano(0),
                        LocalDateTime.now().withHour(12).withMinute(15).withSecond(0).withNano(0),
                        Set.of(getTalkType(LAB_TALK_TAG)),
                        emptySet()),
                new Timeslot("T2", LocalDateTime.now().withHour(10).withMinute(15).withSecond(0).withNano(0),
                        LocalDateTime.now().withHour(11).withMinute(0).withSecond(0).withNano(0),
                        Set.of(getTalkType(BREAKOUT_TALK_TAG)),
                        emptySet()),
                new Timeslot("T3", LocalDateTime.now().withHour(11).withMinute(30).withSecond(0).withNano(0),
                        LocalDateTime.now().withHour(12).withMinute(15).withSecond(0).withNano(0),
                        Set.of(getTalkType(BREAKOUT_TALK_TAG)),
                        emptySet()),
                new Timeslot("T4", LocalDateTime.now().withHour(13).withMinute(0).withSecond(0).withNano(0),
                        LocalDateTime.now().withHour(15).withMinute(0).withSecond(0).withNano(0),
                        Set.of(getTalkType(LAB_TALK_TAG)),
                        Set.of(AFTER_LUNCH_TAG)),
                new Timeslot("T5", LocalDateTime.now().withHour(15).withMinute(30).withSecond(0).withNano(0),
                        LocalDateTime.now().withHour(16).withMinute(15).withSecond(0).withNano(0),
                        Set.of(getTalkType(BREAKOUT_TALK_TAG)),
                        emptySet()),
                new Timeslot("T6", LocalDateTime.now().withHour(16).withMinute(30).withSecond(0).withNano(0),
                        LocalDateTime.now().withHour(17).withMinute(15).withSecond(0).withNano(0),
                        Set.of(getTalkType(BREAKOUT_TALK_TAG)),
                        emptySet())));
    }

    private Set<Room> generateRooms() {
        return buildSet(List.of(
                new Room("R1", "Room A", 60, Set.of(getTalkType(BREAKOUT_TALK_TAG)), Set.of(RECORDED_TAG)),
                new Room("R2", "Room B", 240, Set.of(getTalkType(BREAKOUT_TALK_TAG)), emptySet()),
                new Room("R3", "Room C", 630, Set.of(getTalkType(BREAKOUT_TALK_TAG)), Set.of(RECORDED_TAG, LARGE_TAG)),
                new Room("R4", "Room D", 70, Set.of(getTalkType(BREAKOUT_TALK_TAG)), Set.of(RECORDED_TAG)),
                new Room("R5", "Room E", 490, Set.of(getTalkType(LAB_TALK_TAG)), Set.of(RECORDED_TAG))));
    }

    private Set<Speaker> generateSpeakers() {
        return buildSet(List.of(
                new Speaker("1", "Amy Cole"),
                new Speaker("2", "Beth Fox"),
                new Speaker("3", "Chad Green"),
                new Speaker("4", "Dan Jones"),
                new Speaker("5", "Elsa King"),
                new Speaker("6", "Flo Li"),
                new Speaker("7", "Gus Poe"),
                new Speaker("8", "Hugo Rye"),
                new Speaker("9", "Ivy Smith"),
                new Speaker("10", "Jay Watt"),
                new Speaker("11", "Amy Fox"),
                new Speaker("12", "Beth Green", Set.of(AFTER_LUNCH_TAG))));
    }

    private Set<Talk> generateTalks(Set<Speaker> speakers) {
        Set<Talk> talks = new LinkedHashSet<>();
        talks.add(new Talk("S01", "Talk One", getTalkType(LAB_TALK_TAG),
                getSpeakers(speakers, "Amy Cole", "Beth Fox"), Set.of(getRandomTheme()), Set.of(getRandomSector()),
                Set.of(getRandomAudience()), 2, Set.of(getRandomContent()), "en", 551, 1));
        talks.add(new Talk("S02", "Talk Two", getTalkType(LAB_TALK_TAG),
                getSpeakers(speakers, "Chad Green"), Set.of(getRandomTheme()), Set.of(getRandomSector()),
                Set.of(getRandomAudience()), 3, Set.of(getRandomContent()), "en", 528, 0));
        talks.stream().filter(t -> t.getCode().equals("S01")).findFirst()
                .ifPresent(t -> t.setUndesiredRoomTags(Set.of(RECORDED_TAG)));
        talks.add(new Talk("S03", "Talk Three", getTalkType(BREAKOUT_TALK_TAG),
                getSpeakers(speakers, "Dan Jones"), Set.of(getRandomTheme()), Set.of(getRandomSector()),
                Set.of(getRandomAudience()), 3, Set.of(getRandomContent()), "en", 497, 0));
        talks.add(new Talk("S04", "Talk Four", getTalkType(BREAKOUT_TALK_TAG),
                getSpeakers(speakers, "Elsa King", "Flo Li"), Set.of(getRandomTheme()), Set.of(getRandomSector()),
                Set.of(getRandomAudience()), 1, Set.of(getRandomContent()), "en", 560, 0));
        talks.add(new Talk("S05", "Talk Five", getTalkType(BREAKOUT_TALK_TAG),
                getSpeakers(speakers, "Gus Poe", "Hugo Rye"), Set.of(getRandomTheme()), Set.of(getRandomSector()),
                Set.of(getRandomAudience()), 1, Set.of(getRandomContent()), "en", 957, 0));
        talks.add(new Talk("S06", "Talk Six", getTalkType(BREAKOUT_TALK_TAG),
                getSpeakers(speakers, "Ivy Smith"), Set.of(getRandomTheme()), Set.of(getRandomSector()),
                Set.of(getRandomAudience()), 1, Set.of(getRandomContent()), "en", 957, 0));
        talks.stream().filter(t -> t.getCode().equals("S05")).findFirst()
                .ifPresent(
                        t -> t.setPrerequisiteTalks(talks.stream().filter(t2 -> t2.getCode().equals("S02")).collect(toSet())));
        talks.add(new Talk("S07", "Talk Seven", getTalkType(BREAKOUT_TALK_TAG),
                getSpeakers(speakers, "Jay Watt"), Set.of(getRandomTheme()), Set.of(getRandomSector()),
                Set.of(getRandomAudience()), 3, Set.of(getRandomContent()), "en", 568, 0));
        talks.add(new Talk("S08", "Talk Eight", getTalkType(BREAKOUT_TALK_TAG),
                getSpeakers(speakers, "Amy Fox"), Set.of(getRandomTheme()), Set.of(getRandomSector()),
                Set.of(getRandomAudience()), 3, Set.of(getRandomContent()), "en", 183, 0));
        talks.add(new Talk("S09", "Talk Nine", getTalkType(BREAKOUT_TALK_TAG),
                getSpeakers(speakers, "Beth Green", "Amy Cole"), Set.of(getRandomTheme()),
                Set.of(getRandomSector()),
                Set.of(getRandomAudience()), 3, Set.of(getRandomContent()), "en", 619, 0));
        talks.add(new Talk("S10", "Talk Ten", getTalkType(BREAKOUT_TALK_TAG),
                getSpeakers(speakers, "Beth Fox", "Chad Green"), Set.of(getRandomTheme()),
                Set.of(getRandomSector()),
                Set.of(getRandomAudience()), 3, Set.of(getRandomContent()), "en", 603, 1));
        talks.add(new Talk("S11", "Talk Eleven", getTalkType(BREAKOUT_TALK_TAG),
                getSpeakers(speakers, "Dan Jones", "Elsa King"), Set.of(getRandomTheme()),
                Set.of(getRandomSector()),
                Set.of(getRandomAudience()), 1, Set.of(getRandomContent()), "en", 39, 0));
        talks.add(new Talk("S12", "Talk Twelve", getTalkType(BREAKOUT_TALK_TAG),
                getSpeakers(speakers, "Flo Li", "Gus Poe"), Set.of(getRandomTheme()), Set.of(getRandomSector()),
                Set.of(getRandomAudience()), 3, Set.of(getRandomContent()), "en", 977, 0));
        talks.stream().filter(t -> t.getCode().equals("S11")).findFirst()
                .ifPresent(t -> t.setMutuallyExclusiveTalksTags(Set.of(getRandomContent())));
        talks.add(new Talk("S13", "Talk Thirteen", getTalkType(BREAKOUT_TALK_TAG),
                getSpeakers(speakers, "Hugo Rye"), Set.of(getRandomTheme()), Set.of(getRandomSector()),
                Set.of(getRandomAudience()), 3, Set.of(getRandomContent()), "en", 494, 0));
        talks.add(new Talk("S14", "Talk Fourteen", getTalkType(BREAKOUT_TALK_TAG),
                getSpeakers(speakers, "Ivy Smith"), Set.of(getRandomTheme()), Set.of(getRandomSector()),
                Set.of(getRandomAudience()), 3, Set.of(getRandomContent()), "en", 500, 0));
        talks.add(new Talk("S15", "Talk Fifteen", getTalkType(BREAKOUT_TALK_TAG),
                getSpeakers(speakers, "Jay Watt"), Set.of(getRandomTheme()), Set.of(getRandomSector()),
                Set.of(getRandomAudience()), 2, Set.of(getRandomContent()), "en", 658, 0));
        talks.stream().filter(t -> t.getCode().equals("S11")).findFirst()
                .ifPresent(t -> t.setRequiredRoomTags(Set.of(RECORDED_TAG)));

        return talks;
    }

    private TalkType getTalkType(String name) {
        return TALK_TYPES.stream().filter(t -> t.getName().equals(name)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Tag type %s not found.".formatted(name)));
    }

    private List<Speaker> getSpeakers(Set<Speaker> speakers, String... names) {
        return Arrays.stream(names)
                .map(n -> speakers.stream().filter(s -> s.getName().equals(n)).findFirst())
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    private String getRandomTheme() {
        return THEME_TAGS.get(random.nextInt(THEME_TAGS.size()));
    }

    private String getRandomAudience() {
        return AUDIENCE_TAGS.get(random.nextInt(AUDIENCE_TAGS.size()));
    }

    private String getRandomContent() {
        return CONTENT_TAGS.get(random.nextInt(CONTENT_TAGS.size()));
    }

    private String getRandomSector() {
        return SECTOR_TAGS.get(random.nextInt(SECTOR_TAGS.size()));
    }

    private static <T> Set<T> buildSet(List<T> values) {
        var newSet = new LinkedHashSet<>(values.size());
        newSet.addAll(values);
        return (Set<T>) Collections.unmodifiableSet(newSet);
    }
}
