package org.acme.meetingschedule.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A person who can attend meetings.")
public record PersonDTO(
        @Schema(description = "Unique identifier of the person.") String id,
        @Schema(description = "Full name of the person.") String fullName) {

    public PersonDTO {
        fullName = fullName == null ? "" : fullName;
    }

    public PersonDTO withId(String id) {
        return new PersonDTO(id, fullName);
    }

    public PersonDTO withFullName(String fullName) {
        return new PersonDTO(id, fullName);
    }
}
