package org.acme.conferencescheduling.domain.justification;

import java.util.List;

import org.acme.conferencescheduling.domain.Talk;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Two talks sharing a theme track overlap in time.")
public record TalksWithSameThemeTrackOverlappingJustification(
        @Schema(description = "The code of the first talk.") String talk,
        @Schema(description = "The code of the second talk.") String otherTalk,
        @Schema(description = "The theme track tags both talks have in common.") List<String> sharedThemeTrackTags,
        @Schema(description = "The number of minutes during which both talks overlap.") int overlapInMinutes)
        implements
            ConferenceSchedulingJustification {

    public static TalksWithSameThemeTrackOverlappingJustification of(Talk talk, Talk otherTalk) {
        return new TalksWithSameThemeTrackOverlappingJustification(talk.getCode(), otherTalk.getCode(),
                JustificationHelper.shared(talk.getThemeTrackTags(), otherTalk.getThemeTrackTags()),
                talk.overlappingDurationInMinutes(otherTalk));
    }

    @Override
    public String getDescription() {
        return "Talks '%s' and '%s' share the theme track tags [%s] and overlap for %d minutes."
                .formatted(talk, otherTalk, String.join(", ", sharedThemeTrackTags), overlapInMinutes);
    }
}
