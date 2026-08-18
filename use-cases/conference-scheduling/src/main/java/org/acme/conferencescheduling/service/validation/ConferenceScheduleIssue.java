package org.acme.conferencescheduling.service.validation;

import java.util.List;

import ai.timefold.solver.service.definition.api.validation.AbstractIssue;
import ai.timefold.solver.service.definition.api.validation.IssueCode;
import ai.timefold.solver.service.definition.api.validation.IssueMetadata;
import ai.timefold.solver.service.definition.api.validation.IssueSeverity;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A dataset validation issue reported for a conference schedule input.",
        oneOf = {
                DuplicateRoomIdIssue.class,
                DuplicateSpeakerIdIssue.class,
                DuplicateTalkIdIssue.class,
                DuplicateTimeslotIdIssue.class,
                NonExistingRoomReferenceIssue.class,
                NonExistingSpeakerReferenceIssue.class,
                NonExistingTalkTypeReferenceIssue.class,
                NonExistingTimeslotReferenceIssue.class,
                RoomIdMissingIssue.class,
                SpeakerIdMissingIssue.class,
                TalkIdMissingIssue.class,
                TimeslotIdMissingIssue.class
        })
public abstract class ConferenceScheduleIssue extends AbstractIssue {

    protected ConferenceScheduleIssue(IssueCode code, IssueSeverity severity, List<IssueMetadata> metadata) {
        super(code, severity, metadata);
    }
}
