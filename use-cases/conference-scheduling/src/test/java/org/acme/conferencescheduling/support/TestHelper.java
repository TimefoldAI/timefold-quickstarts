package org.acme.conferencescheduling.support;

import static java.util.Collections.emptyList;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.SequencedSet;

import org.acme.conferencescheduling.domain.ConferenceConstraintProperties;
import org.acme.conferencescheduling.domain.Room;
import org.acme.conferencescheduling.domain.Speaker;
import org.acme.conferencescheduling.domain.Talk;
import org.acme.conferencescheduling.domain.TalkType;
import org.acme.conferencescheduling.domain.Timeslot;
import org.acme.conferencescheduling.dto.input.ConferenceScheduleInput;
import org.acme.conferencescheduling.dto.input.RoomDTO;
import org.acme.conferencescheduling.dto.input.SpeakerDTO;
import org.acme.conferencescheduling.dto.input.TagPreferencesDTO;
import org.acme.conferencescheduling.dto.input.TalkDTO;
import org.acme.conferencescheduling.dto.input.TalkTypeDTO;
import org.acme.conferencescheduling.dto.input.TimeslotDTO;

// To keep our production classes as simple as possible, we've added these methods to help construct the data needed for testing.
public final class TestHelper {

    public static final String CONFERENCE_NAME = "Test conference";
    public static final String BREAKOUT = "Breakout";
    public static final String LAB = "Lab";

    public static final List<TalkTypeDTO> TALK_TYPES = List.of(new TalkTypeDTO(BREAKOUT), new TalkTypeDTO(LAB));

    public static final List<TimeslotDTO> TIMESLOTS = List.of(
            timeslot("ts1", "2024-01-01T09:00Z", "2024-01-01T10:00Z"),
            timeslot("ts2", "2024-01-01T10:30Z", "2024-01-01T11:30Z"),
            timeslot("ts3", "2024-01-01T12:00Z", "2024-01-01T13:00Z"));

    public static final List<RoomDTO> ROOMS = List.of(
            room("r1", "Room A"),
            room("r2", "Room B"),
            room("r3", "Room C"),
            room("r4", "Room D"));

    public static final List<SpeakerDTO> SPEAKERS = List.of(
            speaker("s1", "Amy"),
            speaker("s2", "Bob"),
            speaker("s3", "Cara"));

    public static final List<TalkDTO> TALKS = List.of(
            talk("S01", "s1"),
            talk("S02", "s2"),
            talk("S03", "s3"));

    private TestHelper() {
    }

    public static RoomBuilder aRoom(String id) {
        return new RoomBuilder(id);
    }

    public static SpeakerBuilder aSpeaker(String id) {
        return new SpeakerBuilder(id);
    }

    public static TalkBuilder aTalk(String code) {
        return new TalkBuilder(code);
    }

    public static TimeslotBuilder aTimeslot(String id) {
        return new TimeslotBuilder(id);
    }

    public static ConfigurationBuilder aConfiguration() {
        return new ConfigurationBuilder();
    }

    /**
     * @return a complete, feasible problem with every talk still unassigned
     */
    public static ConferenceScheduleInput createProblem() {
        return input(TALK_TYPES, TIMESLOTS, ROOMS, SPEAKERS, TALKS);
    }

    public static ConferenceScheduleInput input(List<TalkTypeDTO> talkTypes, List<TimeslotDTO> timeslots,
            List<RoomDTO> rooms, List<SpeakerDTO> speakers, List<TalkDTO> talks) {
        return new ConferenceScheduleInput(CONFERENCE_NAME, talkTypes, timeslots, rooms, speakers, talks);
    }

    public static ConferenceScheduleInput inputWithTimeslots(TimeslotDTO... timeslots) {
        return input(TALK_TYPES, List.of(timeslots), ROOMS, SPEAKERS, TALKS);
    }

    public static ConferenceScheduleInput inputWithRooms(RoomDTO... rooms) {
        return input(TALK_TYPES, TIMESLOTS, List.of(rooms), SPEAKERS, TALKS);
    }

    public static ConferenceScheduleInput inputWithSpeakers(SpeakerDTO... speakers) {
        return input(TALK_TYPES, TIMESLOTS, ROOMS, List.of(speakers),
                List.of(talk("TEST", Arrays.stream(speakers).toList().get(0).id())));
    }

    public static ConferenceScheduleInput inputWithTalks(TalkDTO... talks) {
        return input(TALK_TYPES, TIMESLOTS, ROOMS, SPEAKERS, List.of(talks));
    }

    public static TimeslotDTO timeslot(String id) {
        return timeslot(id, "2024-01-01T09:00Z", "2024-01-01T10:00Z");
    }

    public static TimeslotDTO timeslot(String id, String start, String end) {
        return new TimeslotDTO(id, OffsetDateTime.parse(start), OffsetDateTime.parse(end), List.of(BREAKOUT), List.of());
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
        return new SpeakerDTO(id, name, List.of(), TagPreferencesDTO.EMPTY, TagPreferencesDTO.EMPTY);
    }

    private static TalkDTO talk(String code, String talkType, List<String> speakerIds, int audienceLevel,
            String language, int favoriteCount, int crowdControlRisk, String timeslotId, String roomId) {
        return new TalkDTO(code, "Title " + code, talkType, speakerIds, List.of(), List.of(), List.of(), List.of(),
                audienceLevel, language, TagPreferencesDTO.EMPTY, TagPreferencesDTO.EMPTY, List.of(), List.of(),
                favoriteCount, crowdControlRisk, timeslotId, roomId);
    }

    public static TalkDTO talk(String code, String... speakerIds) {
        return talk(code, BREAKOUT, List.of(speakerIds), 1, "en", 1, 0, null, null);
    }

    public static TalkDTO talkOfType(String code, String talkType, String... speakerIds) {
        return talk(code, talkType, List.of(speakerIds), 0, null, 0, 0, null, null);
    }

    public static TalkDTO assignedTalk(String code, String timeslotId, String roomId, String... speakerIds) {
        return talk(code, BREAKOUT, List.of(speakerIds), 1, "en", 1, 0, timeslotId, roomId);
    }

    public static TalkDTO assignedTalkOfType(String code, String talkType, String timeslotId, String roomId,
            String... speakerIds) {
        return talk(code, talkType, List.of(speakerIds), 0, null, 0, 0, timeslotId, roomId);
    }

    /**
     * Builds a {@link Room} for tests, so a test only has to state the fields it actually cares about.
     * <p>
     * Production code calls the {@link Room} constructor directly; this builder deliberately lives in the test
     * sources so the domain class stays free of construction scaffolding.
     */
    public static final class RoomBuilder {

        private final String id;
        private String name;
        private int capacity = 0;
        private List<Timeslot> unavailableTimeslots = List.of();
        private List<String> tags = List.of();

        private RoomBuilder(String id) {
            this.id = id;
            this.name = id;
        }

        public RoomBuilder name(String name) {
            this.name = name;
            return this;
        }

        public RoomBuilder capacity(int capacity) {
            this.capacity = capacity;
            return this;
        }

        public RoomBuilder unavailableTimeslots(List<Timeslot> unavailableTimeslots) {
            this.unavailableTimeslots = unavailableTimeslots;
            return this;
        }

        public RoomBuilder tags(List<String> tags) {
            this.tags = tags;
            return this;
        }

        public Room build() {
            return new Room(id, name, capacity, unavailableTimeslots, tags);
        }
    }

    /**
     * Builds a {@link Speaker} for tests, so a test only has to state the fields it actually cares about.
     * <p>
     * Production code calls the {@link Speaker} constructor directly; this builder deliberately lives in the test
     * sources so the domain class stays free of construction scaffolding.
     */
    public static final class SpeakerBuilder {

        private final String id;
        private String name;
        private SequencedSet<Timeslot> unavailableTimeslots = new LinkedHashSet<>();
        private SequencedSet<String> requiredTimeslotTags = new LinkedHashSet<>();
        private SequencedSet<String> preferredTimeslotTags = new LinkedHashSet<>();
        private SequencedSet<String> prohibitedTimeslotTags = new LinkedHashSet<>();
        private SequencedSet<String> undesiredTimeslotTags = new LinkedHashSet<>();
        private SequencedSet<String> requiredRoomTags = new LinkedHashSet<>();
        private SequencedSet<String> preferredRoomTags = new LinkedHashSet<>();
        private SequencedSet<String> prohibitedRoomTags = new LinkedHashSet<>();
        private SequencedSet<String> undesiredRoomTags = new LinkedHashSet<>();

        private SpeakerBuilder(String id) {
            this.id = id;
            this.name = id;
        }

        public SpeakerBuilder name(String name) {
            this.name = name;
            return this;
        }

        public SpeakerBuilder unavailableTimeslots(SequencedSet<Timeslot> unavailableTimeslots) {
            this.unavailableTimeslots = unavailableTimeslots;
            return this;
        }

        public SpeakerBuilder requiredTimeslotTags(SequencedSet<String> requiredTimeslotTags) {
            this.requiredTimeslotTags = requiredTimeslotTags;
            return this;
        }

        public SpeakerBuilder preferredTimeslotTags(SequencedSet<String> preferredTimeslotTags) {
            this.preferredTimeslotTags = preferredTimeslotTags;
            return this;
        }

        public SpeakerBuilder prohibitedTimeslotTags(SequencedSet<String> prohibitedTimeslotTags) {
            this.prohibitedTimeslotTags = prohibitedTimeslotTags;
            return this;
        }

        public SpeakerBuilder undesiredTimeslotTags(SequencedSet<String> undesiredTimeslotTags) {
            this.undesiredTimeslotTags = undesiredTimeslotTags;
            return this;
        }

        public SpeakerBuilder requiredRoomTags(SequencedSet<String> requiredRoomTags) {
            this.requiredRoomTags = requiredRoomTags;
            return this;
        }

        public SpeakerBuilder preferredRoomTags(SequencedSet<String> preferredRoomTags) {
            this.preferredRoomTags = preferredRoomTags;
            return this;
        }

        public SpeakerBuilder prohibitedRoomTags(SequencedSet<String> prohibitedRoomTags) {
            this.prohibitedRoomTags = prohibitedRoomTags;
            return this;
        }

        public SpeakerBuilder undesiredRoomTags(SequencedSet<String> undesiredRoomTags) {
            this.undesiredRoomTags = undesiredRoomTags;
            return this;
        }

        public Speaker build() {
            return new Speaker(id, name, unavailableTimeslots, requiredTimeslotTags, preferredTimeslotTags,
                    prohibitedTimeslotTags, undesiredTimeslotTags, requiredRoomTags, preferredRoomTags,
                    prohibitedRoomTags, undesiredRoomTags);
        }
    }

    /**
     * Builds a {@link Talk} for tests, so a test only has to state the fields it actually cares about.
     * <p>
     * Production code calls the {@link Talk} constructor directly; this builder deliberately lives in the test
     * sources so the domain class stays free of construction scaffolding.
     */
    public static final class TalkBuilder {

        private final String code;
        private String title;
        private TalkType talkType;
        private List<SpeakerBuilder> speakers = emptyList();
        private SequencedSet<String> themeTrackTags = new LinkedHashSet<>();
        private SequencedSet<String> sectorTags = new LinkedHashSet<>();
        private SequencedSet<String> audienceTypes = new LinkedHashSet<>();
        private int audienceLevel;
        private SequencedSet<String> contentTags = new LinkedHashSet<>();
        private String language;
        private SequencedSet<String> requiredTimeslotTags = new LinkedHashSet<>();
        private SequencedSet<String> preferredTimeslotTags = new LinkedHashSet<>();
        private SequencedSet<String> prohibitedTimeslotTags = new LinkedHashSet<>();
        private SequencedSet<String> undesiredTimeslotTags = new LinkedHashSet<>();
        private SequencedSet<String> requiredRoomTags = new LinkedHashSet<>();
        private SequencedSet<String> preferredRoomTags = new LinkedHashSet<>();
        private SequencedSet<String> prohibitedRoomTags = new LinkedHashSet<>();
        private SequencedSet<String> undesiredRoomTags = new LinkedHashSet<>();
        private SequencedSet<String> mutuallyExclusiveTalksTags = new LinkedHashSet<>();
        private SequencedSet<Talk> prerequisiteTalks = new LinkedHashSet<>();
        private int favoriteCount;
        private int crowdControlRisk;
        private Timeslot timeslot = aTimeslot("ts1").build();
        private RoomBuilder room;

        private TalkBuilder(String code) {
            this.code = code;
        }

        public TalkBuilder title(String title) {
            this.title = title;
            return this;
        }

        public TalkBuilder talkType(TalkType talkType) {
            this.talkType = talkType;
            return this;
        }

        public TalkBuilder speakers(List<SpeakerBuilder> speakers) {
            this.speakers = speakers;
            return this;
        }

        public TalkBuilder timeslot(Timeslot timeslot) {
            this.timeslot = timeslot;
            return this;
        }

        public TalkBuilder room(RoomBuilder room) {
            this.room = room;
            return this;
        }

        public TalkBuilder themeTrackTags(SequencedSet<String> themeTrackTags) {
            this.themeTrackTags = themeTrackTags;
            return this;
        }

        public TalkBuilder sectorTags(SequencedSet<String> sectorTags) {
            this.sectorTags = sectorTags;
            return this;
        }

        public TalkBuilder audienceTypes(SequencedSet<String> audienceTypes) {
            this.audienceTypes = audienceTypes;
            return this;
        }

        public TalkBuilder audienceLevel(int audienceLevel) {
            this.audienceLevel = audienceLevel;
            return this;
        }

        public TalkBuilder contentTags(SequencedSet<String> contentTags) {
            this.contentTags = contentTags;
            return this;
        }

        public TalkBuilder language(String language) {
            this.language = language;
            return this;
        }

        public TalkBuilder requiredTimeslotTags(SequencedSet<String> requiredTimeslotTags) {
            this.requiredTimeslotTags = requiredTimeslotTags;
            return this;
        }

        public TalkBuilder preferredTimeslotTags(SequencedSet<String> preferredTimeslotTags) {
            this.preferredTimeslotTags = preferredTimeslotTags;
            return this;
        }

        public TalkBuilder prohibitedTimeslotTags(SequencedSet<String> prohibitedTimeslotTags) {
            this.prohibitedTimeslotTags = prohibitedTimeslotTags;
            return this;
        }

        public TalkBuilder undesiredTimeslotTags(SequencedSet<String> undesiredTimeslotTags) {
            this.undesiredTimeslotTags = undesiredTimeslotTags;
            return this;
        }

        public TalkBuilder requiredRoomTags(SequencedSet<String> requiredRoomTags) {
            this.requiredRoomTags = requiredRoomTags;
            return this;
        }

        public TalkBuilder preferredRoomTags(SequencedSet<String> preferredRoomTags) {
            this.preferredRoomTags = preferredRoomTags;
            return this;
        }

        public TalkBuilder prohibitedRoomTags(SequencedSet<String> prohibitedRoomTags) {
            this.prohibitedRoomTags = prohibitedRoomTags;
            return this;
        }

        public TalkBuilder undesiredRoomTags(SequencedSet<String> undesiredRoomTags) {
            this.undesiredRoomTags = undesiredRoomTags;
            return this;
        }

        public TalkBuilder mutuallyExclusiveTalksTags(SequencedSet<String> mutuallyExclusiveTalksTags) {
            this.mutuallyExclusiveTalksTags = mutuallyExclusiveTalksTags;
            return this;
        }

        public TalkBuilder prerequisiteTalks(SequencedSet<Talk> prerequisiteTalks) {
            this.prerequisiteTalks = prerequisiteTalks;
            return this;
        }

        public TalkBuilder favoriteCount(int favoriteCount) {
            this.favoriteCount = favoriteCount;
            return this;
        }

        public TalkBuilder crowdControlRisk(int crowdControlRisk) {
            this.crowdControlRisk = crowdControlRisk;
            return this;
        }

        public Talk build() {
            List<Speaker> builtSpeakers = speakers.stream().map(SpeakerBuilder::build).toList();
            Talk talk = new Talk(code, title, talkType, builtSpeakers, themeTrackTags, sectorTags, audienceTypes,
                    audienceLevel, contentTags, language, requiredTimeslotTags, preferredTimeslotTags,
                    prohibitedTimeslotTags, undesiredTimeslotTags, requiredRoomTags, preferredRoomTags,
                    prohibitedRoomTags, undesiredRoomTags, mutuallyExclusiveTalksTags, prerequisiteTalks, favoriteCount,
                    crowdControlRisk);
            talk.setTimeslot(timeslot);
            talk.setRoom(room == null ? null : room.build());
            return talk;
        }
    }

    /**
     * Builds a {@link Timeslot} for tests, so a test only has to state the fields it actually cares about.
     * <p>
     * Production code calls the {@link Timeslot} constructor directly; this builder deliberately lives in the test
     * sources so the domain class stays free of construction scaffolding.
     */
    public static final class TimeslotBuilder {

        private static final OffsetDateTime DEFAULT_START = OffsetDateTime.of(2024, 1, 1, 9, 0, 0, 0, ZoneOffset.UTC);

        private final String id;
        private OffsetDateTime startDateTime = DEFAULT_START;
        private OffsetDateTime endDateTime = DEFAULT_START.plusHours(1);
        private List<String> tags = List.of();

        private TimeslotBuilder(String id) {
            this.id = id;
        }

        public TimeslotBuilder startDateTime(OffsetDateTime startDateTime) {
            this.startDateTime = startDateTime;
            return this;
        }

        public TimeslotBuilder endDateTime(OffsetDateTime endDateTime) {
            this.endDateTime = endDateTime;
            return this;
        }

        public TimeslotBuilder tags(List<String> tags) {
            this.tags = tags;
            return this;
        }

        public Timeslot build() {
            return new Timeslot(id, startDateTime, endDateTime, tags);
        }
    }

    /**
     * Builds a {@link ConferenceConstraintProperties} for tests, so a test only has to state the fields it actually
     * cares about.
     * <p>
     * Production code calls the {@link ConferenceConstraintProperties} constructor directly; this builder
     * deliberately lives in the test sources so the domain class stays free of construction scaffolding.
     */
    public static final class ConfigurationBuilder {

        private Integer minimumConsecutiveTalksPauseInMinutes;

        private ConfigurationBuilder() {
        }

        public ConfigurationBuilder minimumConsecutiveTalksPauseInMinutes(int minimumConsecutiveTalksPauseInMinutes) {
            this.minimumConsecutiveTalksPauseInMinutes = minimumConsecutiveTalksPauseInMinutes;
            return this;
        }

        public ConferenceConstraintProperties build() {
            ConferenceConstraintProperties configuration = new ConferenceConstraintProperties();
            if (minimumConsecutiveTalksPauseInMinutes != null) {
                configuration.setMinimumConsecutiveTalksPauseInMinutes(minimumConsecutiveTalksPauseInMinutes);
            }
            return configuration;
        }
    }
}
