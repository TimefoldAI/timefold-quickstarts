package org.acme.conferencescheduling.rest;

import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toSet;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;

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
    private static final String ARTIFICIAL_INTELLIGENCE_TAG = "Artificial Intelligence";
    private static final String BIG_DATA_TAG = "Big Data";
    private static final String CLOUD_TAG = "Cloud";
    private static final String CULTURE_TAG = "Culture";
    private static final String IOT_TAG = "IoT";
    private static final String MIDDLEWARE_TAG = "Middleware";
    private static final String MOBILE_TAG = "Mobile";
    private static final String MODERN_WEB_TAG = "Modern Web";
    // Sector tags
    private static final String TRANSPORTATION_TAG = "Transportation";
    private static final String TELECOMMUNICATIONS_TAG = "Telecommunications";
    // Audience tags
    private static final String BUSINESS_ANALYSTS_TAG = "Business analysts";
    private static final String MANAGERS_TAG = "Managers";
    private static final String PROGRAMMERS_TAG = "Programmers";
    // Content tags
    private static final String ANDROID_TAG = "Android";
    private static final String ANGULAR_TAG = "Angular";
    private static final String CAMEL_TAG = "Camel";
    private static final String DOCKER_TAG = "Docker";
    private static final String DROOLS_TAG = "Drools";
    private static final String ERRAI_TAG = "Errai";
    private static final String GWT_TAG = "GWT";
    private static final String HIBERNATE_TAG = "Hibernate";
    private static final String JACKSON_TAG = "Jackson";
    private static final String JBPM_TAG = "jBPM";
    private static final String KUBERNETES_TAG = "Kubernetes";
    private static final String OPENSHIFT_TAG = "OpenShift";
    private static final String PLANTINUM_SPONSOR_TAG = "Platinum Sponsor";
    private static final String REST_EASY_TAG = "RestEasy";
    private static final String SPRING_TAG = "Spring";
    private static final String TENSORFLOW_TAG = "Tensorflow";
    private static final String TIMEFOLD_TAG = "Timefold";
    private static final String VERTX_TAG = "VertX";
    private static final String WELD_TAG = "Weld";
    private static final String WILDFLY_TAG = "WildFly";

    private static final Set<TalkType> TALK_TYPES = Set.of(
            new TalkType(LAB_TALK_TAG),
            new TalkType(BREAKOUT_TALK_TAG));

    public ConferenceSchedule generateDemoData() {
        Set<Speaker> speakers = generateSpeakers();
        return new ConferenceSchedule("Conference", TALK_TYPES, generateTimeslots(), generateRooms(), speakers,
                generateTalks(speakers));
    }

    private Set<Timeslot> generateTimeslots() {
        return Set.of(
                new Timeslot("T1", LocalDateTime.now().withHour(10).withMinute(15).withSecond(0),
                        LocalDateTime.now().withHour(12).withMinute(15).withSecond(0), Set.of(getTalkType(LAB_TALK_TAG)),
                        emptySet()),
                new Timeslot("T2", LocalDateTime.now().withHour(10).withMinute(15).withSecond(0),
                        LocalDateTime.now().withHour(11).withMinute(0).withSecond(0), Set.of(getTalkType(BREAKOUT_TALK_TAG)),
                        emptySet()),
                new Timeslot("T3", LocalDateTime.now().withHour(11).withMinute(30).withSecond(0),
                        LocalDateTime.now().withHour(12).withMinute(15).withSecond(0), Set.of(getTalkType(BREAKOUT_TALK_TAG)),
                        emptySet()),
                new Timeslot("T4", LocalDateTime.now().withHour(13).withMinute(0).withSecond(0),
                        LocalDateTime.now().withHour(15).withMinute(0).withSecond(0), Set.of(getTalkType(LAB_TALK_TAG)),
                        Set.of(AFTER_LUNCH_TAG)),
                new Timeslot("T5", LocalDateTime.now().withHour(15).withMinute(30).withSecond(0),
                        LocalDateTime.now().withHour(16).withMinute(15).withSecond(0), Set.of(getTalkType(BREAKOUT_TALK_TAG)),
                        emptySet()),
                new Timeslot("T6", LocalDateTime.now().withHour(16).withMinute(30).withSecond(0),
                        LocalDateTime.now().withHour(17).withMinute(15).withSecond(0), Set.of(getTalkType(BREAKOUT_TALK_TAG)),
                        emptySet()));
    }

    private Set<Room> generateRooms() {
        return Set.of(
                new Room("R1", 60, Set.of(getTalkType(BREAKOUT_TALK_TAG)), Set.of(RECORDED_TAG)),
                new Room("R2", 240, Set.of(getTalkType(BREAKOUT_TALK_TAG)), emptySet()),
                new Room("R3", 630, Set.of(getTalkType(BREAKOUT_TALK_TAG)), Set.of(RECORDED_TAG, LARGE_TAG)),
                new Room("R4", 70, Set.of(getTalkType(BREAKOUT_TALK_TAG)), Set.of(RECORDED_TAG)),
                new Room("R5", 490, Set.of(getTalkType(LAB_TALK_TAG)), Set.of(RECORDED_TAG)));
    }

    private Set<Speaker> generateSpeakers() {
        return Set.of(
                new Speaker("Amy Cole"),
                new Speaker("Beth Fox"),
                new Speaker("Chad Green"),
                new Speaker("Dan Jones"),
                new Speaker("Elsa King"),
                new Speaker("Flo Li"),
                new Speaker("Gus Poe"),
                new Speaker("Hugo Rye"),
                new Speaker("Ivy Smith"),
                new Speaker("Jay Watt"),
                new Speaker("Amy Fox"),
                new Speaker("Beth Green", Set.of(AFTER_LUNCH_TAG)));
    }

    private Set<Talk> generateTalks(Set<Speaker> speakers) {
        Set<Talk> talks = new HashSet<>();
        talks.add(new Talk("S00", "Hands on real-time OpenShift", getTalkType(LAB_TALK_TAG),
                getSpeakers(speakers, "Amy Cole", "Beth Fox"), Set.of(ARTIFICIAL_INTELLIGENCE_TAG), emptySet(),
                Set.of(MANAGERS_TAG), 2, Set.of(OPENSHIFT_TAG, KUBERNETES_TAG), "en", 551, 1));
        talks.add(new Talk("S01", "Advanced containerized WildFly", getTalkType(LAB_TALK_TAG),
                getSpeakers(speakers, "Chad Green"), Set.of(CLOUD_TAG), emptySet(),
                Set.of(BUSINESS_ANALYSTS_TAG), 3, Set.of(WILDFLY_TAG), "en", 528, 0)
                .withUndesiredRoomTagSet(Set.of(RECORDED_TAG)));
        talks.add(new Talk("S02", "Learn virtualized Spring", getTalkType(BREAKOUT_TALK_TAG),
                getSpeakers(speakers, "Dan Jones"), Set.of(ARTIFICIAL_INTELLIGENCE_TAG), Set.of(TRANSPORTATION_TAG),
                Set.of(MANAGERS_TAG), 3, Set.of(SPRING_TAG), "en", 497, 0));
        talks.add(new Talk("S03", "Intro to serverless Drools", getTalkType(BREAKOUT_TALK_TAG),
                getSpeakers(speakers, "Elsa King", "Flo Li"), Set.of(IOT_TAG), emptySet(),
                Set.of(BUSINESS_ANALYSTS_TAG), 1, Set.of(DROOLS_TAG), "en", 560, 0));
        talks.add(new Talk("S04", "Discover AI-driven Timefold", getTalkType(BREAKOUT_TALK_TAG),
                getSpeakers(speakers, "Gus Poe", "Hugo Rye"), Set.of(BIG_DATA_TAG), emptySet(),
                Set.of(PROGRAMMERS_TAG), 1, Set.of(TIMEFOLD_TAG), "en", 957, 0));
        talks.add(new Talk("S05", "Mastering machine learning jBPM", getTalkType(BREAKOUT_TALK_TAG),
                getSpeakers(speakers, "Ivy Smith"), Set.of(MOBILE_TAG), emptySet(),
                Set.of(PROGRAMMERS_TAG), 1, Set.of(JBPM_TAG), "en", 957, 0)
                .withPrerequisiteTalkSet(talks.stream().filter(t -> t.getCode().equals("S02")).collect(toSet())));
        talks.add(new Talk("S06", "Tuning IOT-driven Camel", getTalkType(BREAKOUT_TALK_TAG),
                getSpeakers(speakers, "Jay Watt"), Set.of(MOBILE_TAG), emptySet(),
                Set.of(BUSINESS_ANALYSTS_TAG), 3, Set.of(CAMEL_TAG), "en", 568, 0));
        talks.add(new Talk("S07", "Building deep learning Jackson", getTalkType(BREAKOUT_TALK_TAG),
                getSpeakers(speakers, "Amy Fox"), Set.of(CULTURE_TAG), Set.of(TELECOMMUNICATIONS_TAG),
                Set.of(BUSINESS_ANALYSTS_TAG), 3, Set.of(JACKSON_TAG), "en", 183, 0));
        talks.add(new Talk("S08", "Securing scalable Docker", getTalkType(BREAKOUT_TALK_TAG),
                getSpeakers(speakers, "Beth Green", "Amy Cole"), Set.of(CLOUD_TAG, MODERN_WEB_TAG), emptySet(),
                Set.of(BUSINESS_ANALYSTS_TAG), 3, Set.of(DOCKER_TAG), "en", 619, 0));
        talks.add(new Talk("S09", "Debug enterprise Hibernate", getTalkType(BREAKOUT_TALK_TAG),
                getSpeakers(speakers, "Beth Fox", "Chad Green"), Set.of(CULTURE_TAG), Set.of(TRANSPORTATION_TAG),
                Set.of(BUSINESS_ANALYSTS_TAG), 3, Set.of(HIBERNATE_TAG), "en", 603, 1));
        talks.add(new Talk("S10", "Prepare for streaming GWT", getTalkType(BREAKOUT_TALK_TAG),
                getSpeakers(speakers, "Dan Jones", "Elsa King"), Set.of(ARTIFICIAL_INTELLIGENCE_TAG), emptySet(),
                Set.of(MANAGERS_TAG), 1, Set.of(GWT_TAG), "en", 39, 0));
        talks.add(new Talk("S11", "Understand mobile Errai", getTalkType(BREAKOUT_TALK_TAG),
                getSpeakers(speakers, "Flo Li", "Gus Poe"), Set.of(CULTURE_TAG), emptySet(),
                Set.of(MANAGERS_TAG), 3, Set.of(ERRAI_TAG), "en", 977, 0)
                .withMutuallyExclusiveTalksTagSet(Set.of(PLANTINUM_SPONSOR_TAG)));
        talks.add(new Talk("S12", "Applying modern Angular", getTalkType(BREAKOUT_TALK_TAG),
                getSpeakers(speakers, "Hugo Rye"), Set.of(MIDDLEWARE_TAG), emptySet(),
                Set.of(MANAGERS_TAG), 3, Set.of(ANGULAR_TAG), "en", 494, 0));
        talks.add(new Talk("S13", "Grok distributed Weld", getTalkType(BREAKOUT_TALK_TAG),
                getSpeakers(speakers, "Ivy Smith"), Set.of(MOBILE_TAG), emptySet(),
                Set.of(BUSINESS_ANALYSTS_TAG), 3, Set.of(WELD_TAG), "en", 500, 0));
        talks.add(new Talk("S14", "Troubleshooting reliable RestEasy", getTalkType(BREAKOUT_TALK_TAG),
                getSpeakers(speakers, "Jay Watt"), Set.of(MODERN_WEB_TAG), emptySet(),
                Set.of(BUSINESS_ANALYSTS_TAG), 2, Set.of(REST_EASY_TAG), "en", 658, 0)
                .withRequiredRoomTagSet(Set.of(RECORDED_TAG)));
        talks.add(new Talk("S15", "Using secure Android", getTalkType(BREAKOUT_TALK_TAG),
                getSpeakers(speakers, "Amy Fox", "Beth Green"), Set.of(IOT_TAG), emptySet(),
                Set.of(MANAGERS_TAG), 1, Set.of(ANDROID_TAG), "en", 592, 0));
        talks.add(new Talk("S16", "Deliver stable Tensorflow", getTalkType(BREAKOUT_TALK_TAG),
                getSpeakers(speakers, "Amy Cole"), Set.of(MOBILE_TAG), emptySet(),
                Set.of(BUSINESS_ANALYSTS_TAG), 2, Set.of(TENSORFLOW_TAG), "en", 66, 0));
        talks.add(new Talk("S17", "Implement platform-independent VertX", getTalkType(BREAKOUT_TALK_TAG),
                getSpeakers(speakers, "Beth Fox", "Chad Green"), Set.of(MIDDLEWARE_TAG), emptySet(),
                Set.of(PROGRAMMERS_TAG), 2, Set.of(VERTX_TAG), "en", 81, 0));

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

}
