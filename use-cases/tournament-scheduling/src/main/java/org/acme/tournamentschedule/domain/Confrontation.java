package org.acme.tournamentschedule.domain;

/**
 * An unordered pairing of two teams playing on the same day, used to measure how often they play each other.
 */
public record Confrontation(
        Team first,
        Team second) {
}
