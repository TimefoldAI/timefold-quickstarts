package org.acme.meetingschedule.domain;

import java.util.Objects;

/**
 * A meeting that has to be held once, in one room, starting at one {@link TimeGrain}.
 * <p>
 * The people attending it are not held here but in the {@link Attendance} facts that point back to this meeting, so that
 * the constraints can join required and preferred attendance independently. Only the {@link #requiredCapacity()}, the
 * total number of people expected to show up, is cached here, because the room capacity constraint needs it per meeting.
 */
public record Meeting(
        String id,
        String topic,
        int durationInGrains,
        int requiredCapacity) {

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Meeting meeting)) {
            return false;
        }
        return Objects.equals(id, meeting.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return topic;
    }
}
