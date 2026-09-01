package org.acme.conferencescheduling.domain.justification;

import java.util.List;

import org.acme.conferencescheduling.domain.Talk;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Two talks sharing content overlap in time.")
public record TalksWithSameContentOverlappingJustification(
        @Schema(description = "The code of the first talk.") String talk,
        @Schema(description = "The code of the second talk.") String otherTalk,
        @Schema(description = "The content tags both talks have in common.") List<String> sharedContentTags,
        @Schema(description = "The number of minutes during which both talks overlap.") int overlapInMinutes)
        implements
            ConferenceSchedulingJustification {

    public static TalksWithSameContentOverlappingJustification of(Talk talk, Talk otherTalk) {
        return new TalksWithSameContentOverlappingJustification(talk.getCode(), otherTalk.getCode(),
                JustificationHelper.shared(talk.getContentTags(), otherTalk.getContentTags()),
                talk.overlappingDurationInMinutes(otherTalk));
    }

    @Override
    public String getDescription() {
        return "Talks '%s' and '%s' share the content tags [%s] and overlap for %d minutes."
                .formatted(talk, otherTalk, String.join(", ", sharedContentTags), overlapInMinutes);
    }
}
