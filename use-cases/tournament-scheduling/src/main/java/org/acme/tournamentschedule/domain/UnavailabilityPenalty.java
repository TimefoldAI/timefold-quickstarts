package org.acme.tournamentschedule.domain;

import java.time.LocalDate;

/**
 * A day a {@link Team} is unavailable to play a match.
 */
public record UnavailabilityPenalty(
        Team team,
        LocalDate date) {
}
