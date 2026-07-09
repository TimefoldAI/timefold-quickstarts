package org.acme.conferencescheduling.dto;

import ai.timefold.solver.service.definition.api.validation.IssueMetadata;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Details about a speaker ID validation issue.")
public record SpeakerIdDetail(
        @Schema(description = "The ID of the speaker.") String speakerId) implements IssueMetadata {

    public SpeakerIdDetail {
        speakerId = speakerId == null ? "" : speakerId;
    }

    public SpeakerIdDetail withSpeakerId(String speakerId) {
        return new SpeakerIdDetail(speakerId);
    }

    @Override
    public String getType() {
        return "SpeakerId";
    }
}
