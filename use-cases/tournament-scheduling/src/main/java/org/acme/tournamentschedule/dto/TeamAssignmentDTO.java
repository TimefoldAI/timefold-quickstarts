package org.acme.tournamentschedule.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A slot on a day to which the solver assigns a team.")
public record TeamAssignmentDTO(
        @Schema(description = "Unique identifier of the team assignment.") String id,
        @Schema(description = "Index of the day the assignment belongs to.") int dateIndex,
        @Schema(description = "Position of the assignment within its day.") int indexInDay,
        @Schema(description = "Whether the assignment is pinned and must not be changed by the solver.") boolean pinned,
        @Schema(description = "ID of the team assigned to this slot. Null when unassigned.") String teamId) {

    public TeamAssignmentDTO {
        teamId = normalizeId(teamId);
    }

    private static String normalizeId(String id) {
        return id != null && id.isBlank() ? null : id;
    }

    public TeamAssignmentDTO withId(String id) {
        return new TeamAssignmentDTO(id, dateIndex, indexInDay, pinned, teamId);
    }

    public TeamAssignmentDTO withDateIndex(int dateIndex) {
        return new TeamAssignmentDTO(id, dateIndex, indexInDay, pinned, teamId);
    }

    public TeamAssignmentDTO withIndexInDay(int indexInDay) {
        return new TeamAssignmentDTO(id, dateIndex, indexInDay, pinned, teamId);
    }

    public TeamAssignmentDTO withPinned(boolean pinned) {
        return new TeamAssignmentDTO(id, dateIndex, indexInDay, pinned, teamId);
    }

    public TeamAssignmentDTO withTeamId(String teamId) {
        return new TeamAssignmentDTO(id, dateIndex, indexInDay, pinned, teamId);
    }
}
