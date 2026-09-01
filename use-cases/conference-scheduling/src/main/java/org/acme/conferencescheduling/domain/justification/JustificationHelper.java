package org.acme.conferencescheduling.domain.justification;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import org.acme.conferencescheduling.domain.Speaker;
import org.acme.conferencescheduling.domain.Timeslot;

/**
 * Small helpers shared by the justifications to turn domain objects into the plain values they expose.
 */
final class JustificationHelper {

    /**
     * @return the tags present in both collections, in the iteration order of {@code left}
     */
    static List<String> shared(Collection<String> left, Collection<String> right) {
        return left.stream()
                .filter(right::contains)
                .toList();
    }

    /**
     * @return the expected tags that are absent from the actual tags, in the iteration order of {@code expected}
     */
    static List<String> missing(Collection<String> expected, Collection<String> actual) {
        return expected.stream()
                .filter(tag -> !actual.contains(tag))
                .toList();
    }

    static List<String> speakerIds(Collection<Speaker> speakers) {
        return speakers.stream()
                .map(Speaker::id)
                .toList();
    }

    static List<String> timeslotIds(Collection<Timeslot> timeslots) {
        return timeslots.stream()
                .map(Timeslot::getId)
                .toList();
    }

    /**
     * @return the distinct tags of all speakers, as selected by {@code tagExtractor}
     */
    static List<String> speakerTags(Collection<Speaker> speakers,
            Function<Speaker, Collection<String>> tagExtractor) {
        return speakers.stream()
                .flatMap(speaker -> tagExtractor.apply(speaker).stream())
                .distinct()
                .toList();
    }

    private JustificationHelper() {
    }
}
