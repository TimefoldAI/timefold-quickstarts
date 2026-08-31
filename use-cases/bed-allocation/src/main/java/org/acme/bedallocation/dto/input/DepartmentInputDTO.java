package org.acme.bedallocation.dto.input;

import static java.util.Collections.emptyMap;

import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A hospital department, grouping rooms and specialty priorities.")
public record DepartmentInputDTO(
        @Schema(description = "Unique identifier of the department.") @NotBlank String id,
        @Schema(description = "Display name of the department.") @NotBlank String name,
        @Schema(description = "Minimum patient age accepted by this department, or null if there is none.") @Min(0) @Max(150) Integer minimumAge,
        @Schema(description = "Maximum patient age accepted by this department, or null if there is none.") @Min(0) @Max(150) Integer maximumAge,
        @Schema(description = "Priority (1 is highest) of each specialty treated by this department.") Map<String, Integer> specialtyToPriority,
        @Schema(description = "Rooms belonging to this department.") @NotEmpty List<@Valid RoomInputDTO> rooms) {

    public DepartmentInputDTO {
        specialtyToPriority = specialtyToPriority != null ? specialtyToPriority : emptyMap();
    }
}
