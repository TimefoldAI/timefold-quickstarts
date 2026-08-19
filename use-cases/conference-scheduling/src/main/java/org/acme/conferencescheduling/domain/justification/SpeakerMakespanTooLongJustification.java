package org.acme.conferencescheduling.domain.justification;

import org.acme.conferencescheduling.domain.Speaker;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "The talks of a speaker are spread over more than two days.")
public record SpeakerMakespanTooLongJustification(
        @Schema(description = "The id of the speaker.") String speaker,
        @Schema(description = "The number of days between the speaker's first and last talk.") int daysBetweenTalks)
        implements
            ConferenceSchedulingJustification {

    public static SpeakerMakespanTooLongJustification of(Speaker speaker, int daysBetweenTalks) {
        return new SpeakerMakespanTooLongJustification(speaker.id(), daysBetweenTalks);
    }

    @Override
    public String getDescription() {
        return "The talks of speaker '%s' are spread over %d days."
                .formatted(speaker, daysBetweenTalks);
    }
}
