package org.acme.meetingschedule.domain;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.Objects;

import ai.timefold.solver.core.api.domain.common.PlanningId;

/**
 * One slot of {@link #lengthInMinutes()} minutes a meeting can start in.
 * <p>
 * The grains are built by the model convertor, which divides the office hours of the submitted time configuration into
 * slots of the submitted granularity. The {@link #grainIndex()} is the position of this grain in that horizon, so the
 * constraints can do all of their time arithmetic on a single int rather than on timestamps: a meeting occupies as many
 * consecutive grains as its duration, and the grains outside office hours simply do not exist.
 */
public record TimeGrain(
        @PlanningId String id,
        int grainIndex,
        OffsetDateTime startDateTime,
        int lengthInMinutes) implements Comparable<TimeGrain> {

    private static final Comparator<TimeGrain> COMPARATOR = Comparator.comparingInt(TimeGrain::grainIndex);

    public OffsetDateTime getEndDateTime() {
        return startDateTime.plusMinutes(lengthInMinutes);
    }

    public LocalDate getDate() {
        return startDateTime.toLocalDate();
    }

    @Override
    public int compareTo(TimeGrain other) {
        return COMPARATOR.compare(this, other);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TimeGrain timeGrain)) {
            return false;
        }
        return Objects.equals(id, timeGrain.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "%s (%s)".formatted(id, startDateTime);
    }
}
