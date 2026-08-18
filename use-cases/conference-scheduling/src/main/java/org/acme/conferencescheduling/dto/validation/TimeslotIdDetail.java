package org.acme.conferencescheduling.dto.validation;

import static java.util.Objects.requireNonNull;

import ai.timefold.solver.service.definition.api.validation.IssueMetadata;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Details about a timeslot ID validation issue.")
public record TimeslotIdDetail(
        @Schema(description = "The ID of the timeslot.") String timeslotId) implements IssueMetadata {

    public TimeslotIdDetail {
        timeslotId = requireNonNull(timeslotId);
    }

    @Override
    public String getType() {
        return "TimeslotId";
    }
}
