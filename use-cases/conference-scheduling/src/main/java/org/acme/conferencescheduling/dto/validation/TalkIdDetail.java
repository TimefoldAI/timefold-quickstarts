package org.acme.conferencescheduling.dto.validation;

import static java.util.Objects.requireNonNull;

import ai.timefold.solver.service.definition.api.validation.IssueMetadata;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Details about a talk ID validation issue.")
public record TalkIdDetail(
        @Schema(description = "The ID of the talk.", required = true) String talkId) implements IssueMetadata {

    public TalkIdDetail {
        talkId = requireNonNull(talkId);
    }

    @Override
    public String getType() {
        return "TalkId";
    }
}
