package org.acme.conferencescheduling.domain.justification;

import java.util.List;

import org.acme.conferencescheduling.domain.Talk;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Two talks sharing a sector overlap in time.")
public record TalksWithSameSectorOverlappingJustification(
        @Schema(description = "The code of the first talk.") String talk,
        @Schema(description = "The code of the second talk.") String otherTalk,
        @Schema(description = "The sector tags both talks have in common.") List<String> sharedSectorTags,
        @Schema(description = "The number of minutes during which both talks overlap.") int overlapInMinutes)
        implements
            ConferenceSchedulingJustification {

    public static TalksWithSameSectorOverlappingJustification of(Talk talk, Talk otherTalk) {
        return new TalksWithSameSectorOverlappingJustification(talk.getCode(), otherTalk.getCode(),
                JustificationHelper.shared(talk.getSectorTags(), otherTalk.getSectorTags()),
                talk.overlappingDurationInMinutes(otherTalk));
    }

    @Override
    public String getDescription() {
        return "Talks '%s' and '%s' share the sector tags [%s] and overlap for %d minutes."
                .formatted(talk, otherTalk, String.join(", ", sharedSectorTags), overlapInMinutes);
    }
}
