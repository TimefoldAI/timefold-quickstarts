package org.acme.conferencescheduling.dto.input;

import java.util.List;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;

@Schema(description = "Tags that are required, preferred, prohibited, or undesired.")
public record TagPreferencesDTO(
        @Schema(description = "Tags that must be present.") @JsonSetter(nulls = Nulls.AS_EMPTY) List<String> required,
        @Schema(description = "Tags that are preferred, but not required.") @JsonSetter(
                nulls = Nulls.AS_EMPTY) List<String> preferred,
        @Schema(description = "Tags that must not be present.") @JsonSetter(nulls = Nulls.AS_EMPTY) List<String> prohibited,
        @Schema(description = "Tags that are undesired, but not prohibited.") @JsonSetter(
                nulls = Nulls.AS_EMPTY) List<String> undesired) {
}
