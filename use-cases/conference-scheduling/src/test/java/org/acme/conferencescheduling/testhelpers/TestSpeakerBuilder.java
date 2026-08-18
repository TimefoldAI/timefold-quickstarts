package org.acme.conferencescheduling.testhelpers;

import java.util.LinkedHashSet;
import java.util.SequencedSet;

import org.acme.conferencescheduling.domain.Speaker;
import org.acme.conferencescheduling.domain.Timeslot;

/**
 * Builds a {@link Speaker} for tests, so a test only has to state the fields it actually cares about.
 * <p>
 * Production code calls the {@link Speaker} constructor directly; this builder deliberately lives in the test
 * sources so the domain class stays free of construction scaffolding.
 */
public final class TestSpeakerBuilder {

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

    private TestSpeakerBuilder(String id) {
        this.id = id;
        this.name = id;
    }

    public static TestSpeakerBuilder aSpeaker(String id) {
        return new TestSpeakerBuilder(id);
    }

    public TestSpeakerBuilder name(String name) {
        this.name = name;
        return this;
    }

    public TestSpeakerBuilder unavailableTimeslots(SequencedSet<Timeslot> unavailableTimeslots) {
        this.unavailableTimeslots = unavailableTimeslots;
        return this;
    }

    public TestSpeakerBuilder requiredTimeslotTags(SequencedSet<String> requiredTimeslotTags) {
        this.requiredTimeslotTags = requiredTimeslotTags;
        return this;
    }

    public TestSpeakerBuilder preferredTimeslotTags(SequencedSet<String> preferredTimeslotTags) {
        this.preferredTimeslotTags = preferredTimeslotTags;
        return this;
    }

    public TestSpeakerBuilder prohibitedTimeslotTags(SequencedSet<String> prohibitedTimeslotTags) {
        this.prohibitedTimeslotTags = prohibitedTimeslotTags;
        return this;
    }

    public TestSpeakerBuilder undesiredTimeslotTags(SequencedSet<String> undesiredTimeslotTags) {
        this.undesiredTimeslotTags = undesiredTimeslotTags;
        return this;
    }

    public TestSpeakerBuilder requiredRoomTags(SequencedSet<String> requiredRoomTags) {
        this.requiredRoomTags = requiredRoomTags;
        return this;
    }

    public TestSpeakerBuilder preferredRoomTags(SequencedSet<String> preferredRoomTags) {
        this.preferredRoomTags = preferredRoomTags;
        return this;
    }

    public TestSpeakerBuilder prohibitedRoomTags(SequencedSet<String> prohibitedRoomTags) {
        this.prohibitedRoomTags = prohibitedRoomTags;
        return this;
    }

    public TestSpeakerBuilder undesiredRoomTags(SequencedSet<String> undesiredRoomTags) {
        this.undesiredRoomTags = undesiredRoomTags;
        return this;
    }

    public Speaker build() {
        return new Speaker(id, name, unavailableTimeslots, requiredTimeslotTags, preferredTimeslotTags,
                prohibitedTimeslotTags, undesiredTimeslotTags, requiredRoomTags, preferredRoomTags,
                prohibitedRoomTags, undesiredRoomTags);
    }
}
