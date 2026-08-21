package org.acme.bedallocation.support;

import org.acme.bedallocation.domain.Bed;
import org.acme.bedallocation.domain.Room;

/**
 * Builds a {@link Bed} for tests, so a test only has to state the fields it actually cares about.
 * <p>
 * Production code calls the {@link Bed} constructor directly; this builder deliberately lives in the test
 * sources so the domain class stays free of construction scaffolding.
 */
public final class TestBedBuilder {

    private final String id;
    private Room room;
    private int indexInRoom;

    private TestBedBuilder(String id) {
        this.id = id;
    }

    public static TestBedBuilder aBed(String id) {
        return new TestBedBuilder(id);
    }

    public TestBedBuilder room(Room room) {
        this.room = room;
        return this;
    }

    public TestBedBuilder indexInRoom(int indexInRoom) {
        this.indexInRoom = indexInRoom;
        return this;
    }

    public Bed build() {
        Bed bed = new Bed(id, room, indexInRoom);
        if (room != null) {
            room.beds().add(bed);
        }
        return bed;
    }
}
