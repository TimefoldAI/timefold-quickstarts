package org.acme.conferencescheduling.domain;

import java.util.Objects;
import java.util.Set;

public record TalkType(
        String name,
        Set<Timeslot> compatibleTimeslots,
        Set<Room> compatibleRooms) {

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TalkType talkType)) {
            return false;
        }
        return Objects.equals(name(), talkType.name());
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }
}
