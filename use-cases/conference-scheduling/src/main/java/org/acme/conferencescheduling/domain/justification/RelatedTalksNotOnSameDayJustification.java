package org.acme.conferencescheduling.domain.justification;

import java.time.LocalDate;
import java.util.List;

import org.acme.conferencescheduling.domain.Talk;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Two talks sharing content or a theme track are scheduled on different days.")
public record RelatedTalksNotOnSameDayJustification(
        @Schema(description = "The code of the first talk.") String talk,
        @Schema(description = "The day on which the first talk is scheduled.") LocalDate date,
        @Schema(description = "The code of the second talk.") String otherTalk,
        @Schema(description = "The day on which the second talk is scheduled.") LocalDate otherDate,
        @Schema(description = "The content tags both talks have in common.") List<String> sharedContentTags,
        @Schema(description = "The theme track tags both talks have in common.") List<String> sharedThemeTrackTags)
        implements
            ConferenceSchedulingJustification {

    public static RelatedTalksNotOnSameDayJustification of(Talk talk, Talk otherTalk) {
        return new RelatedTalksNotOnSameDayJustification(talk.getCode(),
                talk.getTimeslot().getStartDateTime().toLocalDate(), otherTalk.getCode(),
                otherTalk.getTimeslot().getStartDateTime().toLocalDate(),
                JustificationHelper.shared(talk.getContentTags(), otherTalk.getContentTags()),
                JustificationHelper.shared(talk.getThemeTrackTags(), otherTalk.getThemeTrackTags()));
    }

    @Override
    public String getDescription() {
        return "Talks '%s' (%s) and '%s' (%s) share the content tags [%s] and the theme track tags [%s], but are not scheduled on the same day."
                .formatted(talk, date, otherTalk, otherDate, String.join(", ", sharedContentTags),
                        String.join(", ", sharedThemeTrackTags));
    }
}
