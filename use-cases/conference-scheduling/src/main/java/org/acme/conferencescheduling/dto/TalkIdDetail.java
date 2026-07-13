package org.acme.conferencescheduling.dto;

import ai.timefold.solver.service.definition.api.validation.IssueMetadata;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Details about a talk ID validation issue.")
public record TalkIdDetail(
        @Schema(description = "The ID of the talk.") String talkId) implements IssueMetadata {

    public TalkIdDetail {
        talkId = talkId == null ? "" : talkId;
    }

    @Override
    public String getType() {
        return "TalkId";
    }
}
