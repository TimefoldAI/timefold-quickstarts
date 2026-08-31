package org.acme.bedallocation.dto.input;

import static java.util.Collections.emptyList;

import java.time.LocalDate;
import java.util.List;

import org.acme.bedallocation.domain.Gender;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A patient stay that must be assigned to a bed.")
public record StayInputDTO(
        @Schema(description = "Unique identifier of the stay.", required = true, minLength = 1) String id,
        @Schema(description = "Name of the patient.", required = true, minLength = 1) String patientName,
        @Schema(description = "Gender of the patient: MALE or FEMALE.", required = true) Gender patientGender,
        @Schema(description = "Age of the patient in years.", required = true, minimum = "0",
                maximum = "150") Integer patientAge,
        @Schema(description = "Maximum room capacity preferred by the patient, or null if there is no preference.") Integer patientPreferredMaximumRoomCapacity,
        @Schema(description = "Medical equipment required by the patient.") List<String> patientRequiredEquipments,
        @Schema(description = "Medical equipment preferred by the patient.") List<String> patientPreferredEquipments,
        @Schema(description = "First night of the stay, in ISO-8601 date format.", required = true) LocalDate arrivalDate,
        @Schema(description = "Departure date, in ISO-8601 date format.", required = true) LocalDate departureDate,
        @Schema(description = "Medical specialty required during the stay.", required = true, minLength = 1) String specialty,
        @Schema(description = "ID of the bed this stay is assigned to, or null if unassigned.") String bedId,
        @Schema(description = "Whether this stay's bed assignment is pinned and must not be changed by the solver.") Boolean pinned) {

    public StayInputDTO {
        patientRequiredEquipments = patientRequiredEquipments != null ? patientRequiredEquipments : emptyList();
        patientPreferredEquipments = patientPreferredEquipments != null ? patientPreferredEquipments : emptyList();
    }

    public StayInputDTO withBedId(String bedId) {
        return new StayInputDTO(id, patientName, patientGender, patientAge, patientPreferredMaximumRoomCapacity,
                patientRequiredEquipments, patientPreferredEquipments, arrivalDate, departureDate, specialty, bedId,
                pinned);
    }
}
