package org.acme.conferencescheduling.support;

import static java.util.Collections.emptySet;

import java.util.Set;

import org.acme.conferencescheduling.domain.Room;
import org.acme.conferencescheduling.domain.TalkType;
import org.acme.conferencescheduling.domain.Timeslot;

/**
 * Builds a {@link Room} for tests, so a test only has to state the fields it actually cares about.
 * <p>
 * Production code calls the {@link Room} constructor directly; this builder deliberately lives in the test
 * sources so the domain class stays free of construction scaffolding.
 */
public final class TestRoomBuilder {

    private final String id;
    private String name;
    private int capacity = 0;
    private Set<TalkType> talkTypes = emptySet();
    private Set<Timeslot> unavailableTimeslots = emptySet();
    private Set<String> tags = emptySet();

    private TestRoomBuilder(String id) {
        this.id = id;
        this.name = id;
    }

    public static TestRoomBuilder aRoom(String id) {
        return new TestRoomBuilder(id);
    }

    public TestRoomBuilder name(String name) {
        this.name = name;
        return this;
    }

    public TestRoomBuilder capacity(int capacity) {
        this.capacity = capacity;
        return this;
    }

    public TestRoomBuilder talkTypes(Set<TalkType> talkTypes) {
        this.talkTypes = talkTypes;
        return this;
    }

    public TestRoomBuilder unavailableTimeslots(Set<Timeslot> unavailableTimeslots) {
        this.unavailableTimeslots = unavailableTimeslots;
        return this;
    }

    public TestRoomBuilder tags(Set<String> tags) {
        this.tags = tags;
        return this;
    }

    public Room build() {
        return new Room(id, name, capacity, talkTypes, unavailableTimeslots, tags);
    }
}
