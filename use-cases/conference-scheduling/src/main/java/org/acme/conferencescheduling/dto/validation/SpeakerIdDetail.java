package org.acme.conferencescheduling.dto.validation;

import static java.util.Objects.requireNonNull;

import ai.timefold.solver.service.definition.api.validation.IssueMetadata;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Details about a speaker ID validation issue.")
public record SpeakerIdDetail(
        @Schema(description = "The ID of the speaker.", required = true) String speakerId) implements IssueMetadata {

    public SpeakerIdDetail {
        speakerId = requireNonNull(speakerId);
    }

    @Override
    public String getType() {
        return "SpeakerId";
    }
}
