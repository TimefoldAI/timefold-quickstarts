package org.acme.meetingschedule.dto;

import ai.timefold.solver.service.definition.api.validation.IssueMetadata;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Details about a person ID validation issue.")
public record PersonIdDetail(
        @Schema(description = "The ID of the person.") String personId) implements IssueMetadata {

    public PersonIdDetail {
        personId = personId == null ? "" : personId;
    }

    public PersonIdDetail withPersonId(String personId) {
        return new PersonIdDetail(personId);
    }

    @Override
    public String getType() {
        return "PersonId";
    }
}
