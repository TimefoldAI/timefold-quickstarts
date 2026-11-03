package org.acme.tournamentschedule.domain;

/**
 * An unordered pairing of two teams playing on the same day, used to measure how often they play each other.
 * <p>
 * The two teams are swapped into ascending {@link Team#id() id} order on construction, so that the same pairing is
 * always the same record, no matter which order the teams were handed in. Constraints rely on that: a confrontation
 * built from a pair of assignments has to be equal to the same confrontation built from a pair of teams, otherwise
 * one logical pairing would show up twice in the load balance.
 */
public record Confrontation(
        Team first,
        Team second) {

    public Confrontation {
        if (first.id().compareTo(second.id()) > 0) {
            Team lower = second;
            second = first;
            first = lower;
        }
    }
}
