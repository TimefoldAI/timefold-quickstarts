package org.acme.sportsleagueschedule.domain.justification;

import ai.timefold.solver.core.api.score.stream.common.Sequence;
import ai.timefold.solver.service.definition.api.ModelConstraintJustification;

import org.acme.sportsleagueschedule.domain.Match;
import org.acme.sportsleagueschedule.domain.Round;
import org.acme.sportsleagueschedule.domain.Team;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Common contract for every sports league scheduling justification.
 * <p>
 * Each implementation is a record dedicated to exactly one thing that is being justified, so that the Timefold Platform can
 * both render a human-readable {@link #getDescription() description} and expose the individual facts behind it through the
 * OpenAPI schema.
 * <p>
 * Every implementation must be listed in the {@link Schema#oneOf()} below, otherwise it does not show up in the generated
 * OpenAPI schema.
 */
@Schema(description = "Explains why a sports league scheduling constraint was matched.",
        oneOf = {
                // Hard constraints
                LeagueScheduleJustification.MatchesOnSameDayJustification.class,
                LeagueScheduleJustification.ConsecutiveHomeMatchesJustification.class,
                LeagueScheduleJustification.ConsecutiveAwayMatchesJustification.class,
                LeagueScheduleJustification.RepeatMatchOnTheNextDayJustification.class,

                // Soft constraints
                LeagueScheduleJustification.StartToAwayHopJustification.class,
                LeagueScheduleJustification.HomeToAwayHopJustification.class,
                LeagueScheduleJustification.AwayToAwayHopJustification.class,
                LeagueScheduleJustification.AwayToHomeHopJustification.class,
                LeagueScheduleJustification.AwayToEndHopJustification.class,
                LeagueScheduleJustification.ClassicMatchesJustification.class
        })
public interface LeagueScheduleJustification extends ModelConstraintJustification {

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

    @Schema(description = "Two matches on the same matchday share a team.",
            allOf = { LeagueScheduleJustification.class })
    record MatchesOnSameDayJustification(
            @Schema(description = "The id of the first match.") String match,
            @Schema(description = "The id of the second match.") String otherMatch,
            @Schema(description = "The index of the round both matches are played in.") int roundIndex)
            implements
                LeagueScheduleJustification {

        public static MatchesOnSameDayJustification of(Match match, Match otherMatch) {
            return new MatchesOnSameDayJustification(match.getId(), otherMatch.getId(), match.getRoundIndex());
        }

        @Override
        public String getDescription() {
            return "Matches '%s' and '%s' are both played in round %d and share a team."
                    .formatted(match, otherMatch, roundIndex);
        }
    }

    @Schema(description = "A team plays too many consecutive matchdays at its own venue.",
            allOf = { LeagueScheduleJustification.class })
    record ConsecutiveHomeMatchesJustification(
            @Schema(description = "The id of the team.") String team,
            @Schema(description = "The number of consecutive home matches.") int matchCount,
            @Schema(description = "The index of the first round in the streak.") int firstRoundIndex,
            @Schema(description = "The index of the last round in the streak.") int lastRoundIndex)
            implements
                LeagueScheduleJustification {

        public static ConsecutiveHomeMatchesJustification of(Team team, Sequence<Round, Integer> rounds) {
            return new ConsecutiveHomeMatchesJustification(team.getId(), rounds.getCount(),
                    rounds.getFirstItem().getIndex(), rounds.getLastItem().getIndex());
        }

        @Override
        public String getDescription() {
            return "Team '%s' plays %d consecutive home matches, in rounds %d to %d."
                    .formatted(team, matchCount, firstRoundIndex, lastRoundIndex);
        }
    }

    @Schema(description = "A team plays too many consecutive matchdays away from its own venue.",
            allOf = { LeagueScheduleJustification.class })
    record ConsecutiveAwayMatchesJustification(
            @Schema(description = "The id of the team.") String team,
            @Schema(description = "The number of consecutive away matches.") int matchCount,
            @Schema(description = "The index of the first round in the streak.") int firstRoundIndex,
            @Schema(description = "The index of the last round in the streak.") int lastRoundIndex)
            implements
                LeagueScheduleJustification {

        public static ConsecutiveAwayMatchesJustification of(Team team, Sequence<Round, Integer> rounds) {
            return new ConsecutiveAwayMatchesJustification(team.getId(), rounds.getCount(),
                    rounds.getFirstItem().getIndex(), rounds.getLastItem().getIndex());
        }

        @Override
        public String getDescription() {
            return "Team '%s' plays %d consecutive away matches, in rounds %d to %d."
                    .formatted(team, matchCount, firstRoundIndex, lastRoundIndex);
        }
    }

    @Schema(description = "The same pairing is replayed on the round right after it was played, with the venues swapped.",
            allOf = { LeagueScheduleJustification.class })
    record RepeatMatchOnTheNextDayJustification(
            @Schema(description = "The id of the match that is replayed the next round.") String match,
            @Schema(description = "The id of the home team.") String homeTeam,
            @Schema(description = "The id of the away team.") String awayTeam,
            @Schema(description = "The index of the round this match is played in.") int roundIndex)
            implements
                LeagueScheduleJustification {

        public static RepeatMatchOnTheNextDayJustification of(Match match) {
            return new RepeatMatchOnTheNextDayJustification(match.getId(), match.getHomeTeam().getId(),
                    match.getAwayTeam().getId(), match.getRoundIndex());
        }

        @Override
        public String getDescription() {
            return "Match '%s' between '%s' and '%s' in round %d is replayed the very next round."
                    .formatted(match, homeTeam, awayTeam, roundIndex);
        }
    }

    @Schema(description = "A team travels from its own venue to the first away match of the season.",
            allOf = { LeagueScheduleJustification.class })
    record StartToAwayHopJustification(
            @Schema(description = "The id of the travelling team.") String team,
            @Schema(description = "The id of the match played after the hop.") String match,
            @Schema(description = "The distance in kilometres of the hop.") int distance)
            implements
                LeagueScheduleJustification {

        public static StartToAwayHopJustification of(Match match) {
            return new StartToAwayHopJustification(match.getAwayTeam().getId(), match.getId(),
                    match.getAwayTeam().getDistance(match.getHomeTeam()));
        }

        @Override
        public String getDescription() {
            return "Team '%s' travels %d km from home to the season's opening match '%s'."
                    .formatted(team, distance, match);
        }
    }

    @Schema(description = "A team travels from a home match to the away match on the next round.",
            allOf = { LeagueScheduleJustification.class })
    record HomeToAwayHopJustification(
            @Schema(description = "The id of the travelling team.") String team,
            @Schema(description = "The id of the match played before the hop.") String match,
            @Schema(description = "The id of the match played after the hop.") String otherMatch,
            @Schema(description = "The distance in kilometres of the hop.") int distance)
            implements
                LeagueScheduleJustification {

        public static HomeToAwayHopJustification of(Match match, Match otherMatch) {
            return new HomeToAwayHopJustification(match.getHomeTeam().getId(), match.getId(), otherMatch.getId(),
                    match.getHomeTeam().getDistance(otherMatch.getHomeTeam()));
        }

        @Override
        public String getDescription() {
            return "Team '%s' travels %d km from its home match '%s' to the away match '%s' on the next round."
                    .formatted(team, distance, match, otherMatch);
        }
    }

    @Schema(description = "A team travels from one away match to the away match on the next round.",
            allOf = { LeagueScheduleJustification.class })
    record AwayToAwayHopJustification(
            @Schema(description = "The id of the travelling team.") String team,
            @Schema(description = "The id of the match played before the hop.") String match,
            @Schema(description = "The id of the match played after the hop.") String otherMatch,
            @Schema(description = "The distance in kilometres of the hop.") int distance)
            implements
                LeagueScheduleJustification {

        public static AwayToAwayHopJustification of(Match match, Match otherMatch) {
            return new AwayToAwayHopJustification(match.getAwayTeam().getId(), match.getId(), otherMatch.getId(),
                    match.getHomeTeam().getDistance(otherMatch.getHomeTeam()));
        }

        @Override
        public String getDescription() {
            return "Team '%s' travels %d km from the away match '%s' to the away match '%s' on the next round."
                    .formatted(team, distance, match, otherMatch);
        }
    }

    @Schema(description = "A team travels back home from an away match to play at its own venue the next round.",
            allOf = { LeagueScheduleJustification.class })
    record AwayToHomeHopJustification(
            @Schema(description = "The id of the travelling team.") String team,
            @Schema(description = "The id of the match played before the hop.") String match,
            @Schema(description = "The id of the match played after the hop.") String otherMatch,
            @Schema(description = "The distance in kilometres of the hop.") int distance)
            implements
                LeagueScheduleJustification {

        public static AwayToHomeHopJustification of(Match match, Match otherMatch) {
            return new AwayToHomeHopJustification(match.getAwayTeam().getId(), match.getId(), otherMatch.getId(),
                    match.getHomeTeam().getDistance(match.getAwayTeam()));
        }

        @Override
        public String getDescription() {
            return "Team '%s' travels %d km back home from the away match '%s' to its home match '%s' on the next round."
                    .formatted(team, distance, match, otherMatch);
        }
    }

    @Schema(description = "A team travels home after the last away match of the season.",
            allOf = { LeagueScheduleJustification.class })
    record AwayToEndHopJustification(
            @Schema(description = "The id of the travelling team.") String team,
            @Schema(description = "The id of the match played before the hop.") String match,
            @Schema(description = "The distance in kilometres of the hop.") int distance)
            implements
                LeagueScheduleJustification {

        public static AwayToEndHopJustification of(Match match) {
            return new AwayToEndHopJustification(match.getAwayTeam().getId(), match.getId(),
                    match.getHomeTeam().getDistance(match.getAwayTeam()));
        }

        @Override
        public String getDescription() {
            return "Team '%s' travels %d km home after the season's closing match '%s'."
                    .formatted(team, distance, match);
        }
    }

    @Schema(description = "A classic match is played on a round that is neither a weekend nor a holiday.",
            allOf = { LeagueScheduleJustification.class })
    record ClassicMatchesJustification(
            @Schema(description = "The id of the classic match.") String match,
            @Schema(description = "The id of the home team.") String homeTeam,
            @Schema(description = "The id of the away team.") String awayTeam,
            @Schema(description = "The index of the round the classic match is played in.") int roundIndex)
            implements
                LeagueScheduleJustification {

        public static ClassicMatchesJustification of(Match match) {
            return new ClassicMatchesJustification(match.getId(), match.getHomeTeam().getId(),
                    match.getAwayTeam().getId(), match.getRoundIndex());
        }

        @Override
        public String getDescription() {
            return "Classic match '%s' between '%s' and '%s' is played in round %d, which is neither a weekend nor a holiday."
                    .formatted(match, homeTeam, awayTeam, roundIndex);
        }
    }
}
