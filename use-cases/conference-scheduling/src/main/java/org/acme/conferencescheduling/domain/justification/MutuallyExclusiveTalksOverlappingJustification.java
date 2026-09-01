package org.acme.conferencescheduling.domain.justification;

import java.util.List;

import org.acme.conferencescheduling.domain.Talk;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Two talks sharing a mutually-exclusive-talks tag overlap in time.")
public record MutuallyExclusiveTalksOverlappingJustification(
        @Schema(description = "The code of the first talk.") String talk,
        @Schema(description = "The code of the second talk.") String otherTalk,
        @Schema(description = "The mutually-exclusive-talks tags both talks have in common.") List<String> sharedTags,
        @Schema(description = "The number of minutes during which both talks overlap.") int overlapInMinutes)
        implements
            ConferenceSchedulingJustification {

    public static MutuallyExclusiveTalksOverlappingJustification of(Talk talk, Talk otherTalk) {
        return new MutuallyExclusiveTalksOverlappingJustification(talk.getCode(), otherTalk.getCode(),
                JustificationHelper.shared(talk.getMutuallyExclusiveTalksTags(), otherTalk.getMutuallyExclusiveTalksTags()),
                talk.overlappingDurationInMinutes(otherTalk));
    }

    @Override
    public String getDescription() {
        return "Talks '%s' and '%s' share the mutually-exclusive-talks tags [%s] and overlap for %d minutes."
                .formatted(talk, otherTalk, String.join(", ", sharedTags), overlapInMinutes);
    }
}
