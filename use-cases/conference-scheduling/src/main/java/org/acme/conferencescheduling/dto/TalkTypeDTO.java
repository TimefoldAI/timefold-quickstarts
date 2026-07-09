package org.acme.conferencescheduling.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A type of talk, e.g. Breakout or Lab, restricting compatible timeslots and rooms.")
public record TalkTypeDTO(
        @Schema(description = "Unique name of the talk type.") String name) {

    public TalkTypeDTO {
        name = name == null ? "" : name;
    }

    public TalkTypeDTO withName(String name) {
        return new TalkTypeDTO(name);
    }
}
