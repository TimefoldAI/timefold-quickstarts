package org.acme.bedallocation.dto.input;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.acme.bedallocation.domain.Gender;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A patient stay that must be assigned to a bed.")
public record StayInputDTO(
        @Schema(description = "Unique identifier of the stay.") @NotBlank String id,
        @Schema(description = "Name of the patient.") @NotBlank String patientName,
        @Schema(description = "Gender of the patient: MALE or FEMALE.") @NotNull @Valid Gender patientGender,
        @Schema(description = "Age of the patient in years.") @NotNull @Min(0) @Max(150) Integer patientAge,
        @Schema(description = "Maximum room capacity preferred by the patient, or null if there is no preference.") Integer patientPreferredMaximumRoomCapacity,
        @Schema(description = "Medical equipment required by the patient.") @JsonSetter(nulls = Nulls.AS_EMPTY) List<String> patientRequiredEquipments,
        @Schema(description = "Medical equipment preferred by the patient.") @JsonSetter(nulls = Nulls.AS_EMPTY) List<String> patientPreferredEquipments,
        @Schema(description = "First night of the stay, in ISO-8601 date format.") @NotNull LocalDate arrivalDate,
        @Schema(description = "Departure date, in ISO-8601 date format.") @NotNull LocalDate departureDate,
        @Schema(description = "Medical specialty required during the stay.") @NotBlank String specialty,
        @Schema(description = "ID of the bed this stay is assigned to, or null if unassigned.") String bedId,
        @Schema(description = "Whether this stay's bed assignment is pinned and must not be changed by the solver.") Boolean pinned) {

    public StayInputDTO withBedId(String bedId) {
        return new StayInputDTO(id, patientName, patientGender, patientAge, patientPreferredMaximumRoomCapacity,
                patientRequiredEquipments, patientPreferredEquipments, arrivalDate, departureDate, specialty, bedId,
                pinned);
    }
}
