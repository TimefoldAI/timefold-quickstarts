package org.acme.conferencescheduling.domain;

import java.util.List;
import java.util.Objects;

public record TalkType(
        String name,
        List<Timeslot> compatibleTimeslots,
        List<Room> compatibleRooms) {

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
