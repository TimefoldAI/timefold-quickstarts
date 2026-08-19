
package org.acme.conferencescheduling.domain;

import java.util.Objects;
import java.util.SequencedSet;

public record Speaker(
        String id,
        String name,
        SequencedSet<Timeslot> unavailableTimeslots,
        SequencedSet<String> requiredTimeslotTags,
        SequencedSet<String> preferredTimeslotTags,
        SequencedSet<String> prohibitedTimeslotTags,
        SequencedSet<String> undesiredTimeslotTags,
        SequencedSet<String> requiredRoomTags,
        SequencedSet<String> preferredRoomTags,
        SequencedSet<String> prohibitedRoomTags,
        SequencedSet<String> undesiredRoomTags) {

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Speaker speaker)) {
            return false;
        }
        return Objects.equals(id, speaker.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return name;
    }
}
