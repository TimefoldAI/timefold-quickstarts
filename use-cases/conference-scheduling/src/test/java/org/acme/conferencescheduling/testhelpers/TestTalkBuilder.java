package org.acme.conferencescheduling.testhelpers;

import static java.util.Collections.emptyList;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.SequencedSet;

import org.acme.conferencescheduling.domain.Room;
import org.acme.conferencescheduling.domain.Speaker;
import org.acme.conferencescheduling.domain.Talk;
import org.acme.conferencescheduling.domain.TalkType;
import org.acme.conferencescheduling.domain.Timeslot;

/**
 * Builds a {@link Talk} for tests, so a test only has to state the fields it actually cares about.
 * <p>
 * Production code calls the {@link Talk} constructor directly; this builder deliberately lives in the test
 * sources so the domain class stays free of construction scaffolding.
 */
public final class TestTalkBuilder {

    private final String code;
    private String title;
    private TalkType talkType;
    private List<Speaker> speakers = emptyList();
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
    private Timeslot timeslot;
    private Room room;

    private TestTalkBuilder(String code) {
        this.code = code;
    }

    public static TestTalkBuilder aTalk(String code) {
        return new TestTalkBuilder(code);
    }

    public TestTalkBuilder title(String title) {
        this.title = title;
        return this;
    }

    public TestTalkBuilder talkType(TalkType talkType) {
        this.talkType = talkType;
        return this;
    }

    public TestTalkBuilder speakers(List<Speaker> speakers) {
        this.speakers = speakers;
        return this;
    }

    public TestTalkBuilder timeslot(Timeslot timeslot) {
        this.timeslot = timeslot;
        return this;
    }

    public TestTalkBuilder room(Room room) {
        this.room = room;
        return this;
    }

    public TestTalkBuilder themeTrackTags(SequencedSet<String> themeTrackTags) {
        this.themeTrackTags = themeTrackTags;
        return this;
    }

    public TestTalkBuilder sectorTags(SequencedSet<String> sectorTags) {
        this.sectorTags = sectorTags;
        return this;
    }

    public TestTalkBuilder audienceTypes(SequencedSet<String> audienceTypes) {
        this.audienceTypes = audienceTypes;
        return this;
    }

    public TestTalkBuilder audienceLevel(int audienceLevel) {
        this.audienceLevel = audienceLevel;
        return this;
    }

    public TestTalkBuilder contentTags(SequencedSet<String> contentTags) {
        this.contentTags = contentTags;
        return this;
    }

    public TestTalkBuilder language(String language) {
        this.language = language;
        return this;
    }

    public TestTalkBuilder requiredTimeslotTags(SequencedSet<String> requiredTimeslotTags) {
        this.requiredTimeslotTags = requiredTimeslotTags;
        return this;
    }

    public TestTalkBuilder preferredTimeslotTags(SequencedSet<String> preferredTimeslotTags) {
        this.preferredTimeslotTags = preferredTimeslotTags;
        return this;
    }

    public TestTalkBuilder prohibitedTimeslotTags(SequencedSet<String> prohibitedTimeslotTags) {
        this.prohibitedTimeslotTags = prohibitedTimeslotTags;
        return this;
    }

    public TestTalkBuilder undesiredTimeslotTags(SequencedSet<String> undesiredTimeslotTags) {
        this.undesiredTimeslotTags = undesiredTimeslotTags;
        return this;
    }

    public TestTalkBuilder requiredRoomTags(SequencedSet<String> requiredRoomTags) {
        this.requiredRoomTags = requiredRoomTags;
        return this;
    }

    public TestTalkBuilder preferredRoomTags(SequencedSet<String> preferredRoomTags) {
        this.preferredRoomTags = preferredRoomTags;
        return this;
    }

    public TestTalkBuilder prohibitedRoomTags(SequencedSet<String> prohibitedRoomTags) {
        this.prohibitedRoomTags = prohibitedRoomTags;
        return this;
    }

    public TestTalkBuilder undesiredRoomTags(SequencedSet<String> undesiredRoomTags) {
        this.undesiredRoomTags = undesiredRoomTags;
        return this;
    }

    public TestTalkBuilder mutuallyExclusiveTalksTags(SequencedSet<String> mutuallyExclusiveTalksTags) {
        this.mutuallyExclusiveTalksTags = mutuallyExclusiveTalksTags;
        return this;
    }

    public TestTalkBuilder prerequisiteTalks(SequencedSet<Talk> prerequisiteTalks) {
        this.prerequisiteTalks = prerequisiteTalks;
        return this;
    }

    public TestTalkBuilder favoriteCount(int favoriteCount) {
        this.favoriteCount = favoriteCount;
        return this;
    }

    public TestTalkBuilder crowdControlRisk(int crowdControlRisk) {
        this.crowdControlRisk = crowdControlRisk;
        return this;
    }

    public Talk build() {
        Talk talk = new Talk(code, title, talkType, speakers, themeTrackTags, sectorTags, audienceTypes, audienceLevel,
                contentTags, language, requiredTimeslotTags, preferredTimeslotTags, prohibitedTimeslotTags,
                undesiredTimeslotTags, requiredRoomTags, preferredRoomTags, prohibitedRoomTags, undesiredRoomTags,
                mutuallyExclusiveTalksTags, prerequisiteTalks, favoriteCount, crowdControlRisk);
        talk.setTimeslot(timeslot);
        talk.setRoom(room);
        return talk;
    }
}
