package org.acme.conferencescheduling.dto.input;

import static java.util.Collections.emptyList;

import java.util.List;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Tags that are required, preferred, prohibited, or undesired.")
public record TagPreferencesDTO(
        @Schema(description = "Tags that must be present.") List<String> required,
        @Schema(description = "Tags that are preferred, but not required.") List<String> preferred,
        @Schema(description = "Tags that must not be present.") List<String> prohibited,
        @Schema(description = "Tags that are undesired, but not prohibited.") List<String> undesired) {

    public static final TagPreferencesDTO EMPTY = new TagPreferencesDTO(emptyList(), emptyList(), emptyList(), emptyList());

    public TagPreferencesDTO {
        required = required != null ? required : emptyList();
        preferred = preferred != null ? preferred : emptyList();
        prohibited = prohibited != null ? prohibited : emptyList();
        undesired = undesired != null ? undesired : emptyList();
    }
}
