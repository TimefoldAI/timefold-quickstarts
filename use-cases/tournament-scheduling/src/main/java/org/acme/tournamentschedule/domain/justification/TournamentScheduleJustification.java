package org.acme.tournamentschedule.domain.justification;

import java.util.Map;

import ai.timefold.solver.core.api.score.stream.common.LoadBalance;
import ai.timefold.solver.service.definition.api.ModelConstraintJustification;

import org.acme.tournamentschedule.domain.Confrontation;
import org.acme.tournamentschedule.domain.Team;
import org.acme.tournamentschedule.domain.TeamAssignment;
import org.acme.tournamentschedule.domain.UnavailabilityPenalty;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Common contract for every tournament scheduling justification.
 * <p>
 * Each implementation is a record dedicated to exactly one thing that is being justified, so that the Timefold
 * Platform can both render a human-readable {@link #getDescription() description} and expose the individual facts
 * behind it through the OpenAPI schema.
 * <p>
 * Every implementation must be listed in the {@link Schema#oneOf()} below, otherwise it does not show up in the
 * generated OpenAPI schema.
 */
@Schema(description = "Explains why a tournament scheduling constraint was matched.",
        oneOf = {
                // Hard constraints
                TournamentScheduleJustification.OneAssignmentPerDatePerTeamJustification.class,
                TournamentScheduleJustification.UnavailabilityPenaltyJustification.class,

                // Medium constraints
                TournamentScheduleJustification.FairAssignmentCountJustification.class,

                // Soft constraints
                TournamentScheduleJustification.EvenConfrontationCountJustification.class
        })
public interface TournamentScheduleJustification extends ModelConstraintJustification {

    /**
     * @return never null, a human-readable explanation of the constraint match
     */
    String getDescription();

    /**
     * Exposes the description as the {@code description} property of {@link ModelConstraintJustification}.
     */
    default String description() {
        return getDescription();
    }

    // ************************************************************************
    // Hard constraints
    // ************************************************************************

    @Schema(description = "A team is assigned to two matches on the same day.",
            allOf = { TournamentScheduleJustification.class })
    record OneAssignmentPerDatePerTeamJustification(
            @Schema(description = "The name of the double-booked team.") String team,
            @Schema(description = "The date both matches are held on, in ISO-8601 date format.") String date,
            @Schema(description = "The ID of the first match.") String match,
            @Schema(description = "The ID of the second match.") String otherMatch)
            implements
                TournamentScheduleJustification {

        public static OneAssignmentPerDatePerTeamJustification of(TeamAssignment assignment,
                TeamAssignment otherAssignment) {
            return new OneAssignmentPerDatePerTeamJustification(assignment.getTeam().name(),
                    assignment.getMatchDate().toString(), assignment.getId(), otherAssignment.getId());
        }

        @Override
        public String getDescription() {
            return "Team '%s' is assigned to both match '%s' and match '%s' on %s."
                    .formatted(team, match, otherMatch, date);
        }
    }

    @Schema(description = "A team is assigned to a match on a day it is unavailable.",
            allOf = { TournamentScheduleJustification.class })
    record UnavailabilityPenaltyJustification(
            @Schema(description = "The name of the unavailable team.") String team,
            @Schema(description = "The date the team is unavailable on, in ISO-8601 date format.") String date)
            implements
                TournamentScheduleJustification {

        public static UnavailabilityPenaltyJustification of(UnavailabilityPenalty penalty) {
            return new UnavailabilityPenaltyJustification(penalty.team().name(), penalty.date().toString());
        }

        @Override
        public String getDescription() {
            return "Team '%s' is assigned to a match on %s, a day it is unavailable.".formatted(team, date);
        }
    }

    // ************************************************************************
    // Medium constraints
    // ************************************************************************

    @Schema(description = "The number of matches is unevenly distributed across teams.",
            allOf = { TournamentScheduleJustification.class })
    record FairAssignmentCountJustification(
            @Schema(description = "The name of the team with the most matches.") String mostAssignedTeam,
            @Schema(description = "The number of matches that team has.") int mostAssignedCount,
            @Schema(description = "The name of the team with the fewest matches.") String leastAssignedTeam,
            @Schema(description = "The number of matches that team has.") int leastAssignedCount)
            implements
                TournamentScheduleJustification {

        public static FairAssignmentCountJustification of(LoadBalance<Team> loadBalance) {
            Map.Entry<Team, Long> most = maxEntry(loadBalance.loads());
            Map.Entry<Team, Long> least = minEntry(loadBalance.loads());
            return new FairAssignmentCountJustification(most.getKey().name(), most.getValue().intValue(),
                    least.getKey().name(), least.getValue().intValue());
        }

        @Override
        public String getDescription() {
            return "Team '%s' has %d match(es) while team '%s' has only %d, an uneven distribution."
                    .formatted(mostAssignedTeam, mostAssignedCount, leastAssignedTeam, leastAssignedCount);
        }
    }

    // ************************************************************************
    // Soft constraints
    // ************************************************************************

    @Schema(description = "One pair of teams faces each other much more, or much less, often than another pair.",
            allOf = { TournamentScheduleJustification.class })
    record EvenConfrontationCountJustification(
            @Schema(description = "The name of one team of the pair that confronts most often.") String mostFrequentTeam,
            @Schema(
                    description = "The name of the other team of the pair that confronts most often.") String mostFrequentOpponent,
            @Schema(description = "The number of times that pair confronts each other.") int mostFrequentCount,
            @Schema(description = "The name of one team of the pair that confronts least often.") String leastFrequentTeam,
            @Schema(
                    description = "The name of the other team of the pair that confronts least often.") String leastFrequentOpponent,
            @Schema(description = "The number of times that pair confronts each other.") int leastFrequentCount)
            implements
                TournamentScheduleJustification {

        public static EvenConfrontationCountJustification of(LoadBalance<Confrontation> loadBalance) {
            Map.Entry<Confrontation, Long> most = maxEntry(loadBalance.loads());
            Map.Entry<Confrontation, Long> least = minEntry(loadBalance.loads());
            return new EvenConfrontationCountJustification(most.getKey().first().name(),
                    most.getKey().second().name(), most.getValue().intValue(), least.getKey().first().name(),
                    least.getKey().second().name(), least.getValue().intValue());
        }

        @Override
        public String getDescription() {
            return "Teams '%s' and '%s' confront each other %d time(s), while '%s' and '%s' only confront %d time(s)."
                    .formatted(mostFrequentTeam, mostFrequentOpponent, mostFrequentCount, leastFrequentTeam,
                            leastFrequentOpponent, leastFrequentCount);
        }
    }

    private static <K> Map.Entry<K, Long> maxEntry(Map<K, Long> loads) {
        return loads.entrySet().stream().max(Map.Entry.comparingByValue()).orElseThrow();
    }

    private static <K> Map.Entry<K, Long> minEntry(Map<K, Long> loads) {
        return loads.entrySet().stream().min(Map.Entry.comparingByValue()).orElseThrow();
    }
}
