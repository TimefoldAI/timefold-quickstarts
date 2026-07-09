package org.acme.conferencescheduling.dto;

import ai.timefold.solver.service.definition.api.validation.IssueMetadata;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Details about a timeslot ID validation issue.")
public record TimeslotIdDetail(
        @Schema(description = "The ID of the timeslot.") String timeslotId) implements IssueMetadata {

    public TimeslotIdDetail {
        timeslotId = timeslotId == null ? "" : timeslotId;
    }

    public TimeslotIdDetail withTimeslotId(String timeslotId) {
        return new TimeslotIdDetail(timeslotId);
    }

    @Override
    public String getType() {
        return "TimeslotId";
    }
}
