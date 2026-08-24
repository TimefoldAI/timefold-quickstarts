package org.acme.bedallocation.dto;

import java.util.List;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A patient stay that must be assigned to a bed.")
public record StayDTO(
        @Schema(description = "Unique identifier of the stay.", required = true) String id,
        @Schema(description = "Name of the patient.") String patientName,
        @Schema(description = "Gender of the patient: MALE or FEMALE.", required = true) String patientGender,
        @Schema(description = "Age of the patient in years.", required = true) int patientAge,
        @Schema(description = "Maximum room capacity preferred by the patient, or null if there is no preference.") Integer patientPreferredMaximumRoomCapacity,
        @Schema(description = "Medical equipment required by the patient.") List<String> patientRequiredEquipments,
        @Schema(description = "Medical equipment preferred by the patient.") List<String> patientPreferredEquipments,
        @Schema(description = "First night of the stay, in ISO-8601 date format.", required = true) String arrivalDate,
        @Schema(description = "Last night of the stay, in ISO-8601 date format.", required = true) String departureDate,
        @Schema(description = "Medical specialty required during the stay.") String specialty,
        @Schema(description = "ID of the bed this stay is assigned to, or null if unassigned.") String bedId) {

    public StayDTO {
        patientRequiredEquipments = patientRequiredEquipments == null ? List.of() : patientRequiredEquipments;
        patientPreferredEquipments = patientPreferredEquipments == null ? List.of() : patientPreferredEquipments;
        bedId = normalizeId(bedId);
    }

    public StayDTO(String id, String arrivalDate, String departureDate) {
        this(id, null, null, 0, null, null, null, arrivalDate, departureDate, null, null);
    }

    private static String normalizeId(String id) {
        return id != null && id.isBlank() ? null : id;
    }

    public StayDTO withPatientName(String patientName) {
        return new StayDTO(id, patientName, patientGender, patientAge, patientPreferredMaximumRoomCapacity,
                patientRequiredEquipments, patientPreferredEquipments, arrivalDate, departureDate, specialty, bedId);
    }

    public StayDTO withPatientGender(String patientGender) {
        return new StayDTO(id, patientName, patientGender, patientAge, patientPreferredMaximumRoomCapacity,
                patientRequiredEquipments, patientPreferredEquipments, arrivalDate, departureDate, specialty, bedId);
    }

    public StayDTO withPatientAge(int patientAge) {
        return new StayDTO(id, patientName, patientGender, patientAge, patientPreferredMaximumRoomCapacity,
                patientRequiredEquipments, patientPreferredEquipments, arrivalDate, departureDate, specialty, bedId);
    }

    public StayDTO withPatientPreferredMaximumRoomCapacity(Integer patientPreferredMaximumRoomCapacity) {
        return new StayDTO(id, patientName, patientGender, patientAge, patientPreferredMaximumRoomCapacity,
                patientRequiredEquipments, patientPreferredEquipments, arrivalDate, departureDate, specialty, bedId);
    }

    public StayDTO withPatientRequiredEquipments(List<String> patientRequiredEquipments) {
        return new StayDTO(id, patientName, patientGender, patientAge, patientPreferredMaximumRoomCapacity,
                patientRequiredEquipments, patientPreferredEquipments, arrivalDate, departureDate, specialty, bedId);
    }

    public StayDTO withPatientPreferredEquipments(List<String> patientPreferredEquipments) {
        return new StayDTO(id, patientName, patientGender, patientAge, patientPreferredMaximumRoomCapacity,
                patientRequiredEquipments, patientPreferredEquipments, arrivalDate, departureDate, specialty, bedId);
    }

    public StayDTO withSpecialty(String specialty) {
        return new StayDTO(id, patientName, patientGender, patientAge, patientPreferredMaximumRoomCapacity,
                patientRequiredEquipments, patientPreferredEquipments, arrivalDate, departureDate, specialty, bedId);
    }

    public StayDTO withBedId(String bedId) {
        return new StayDTO(id, patientName, patientGender, patientAge, patientPreferredMaximumRoomCapacity,
                patientRequiredEquipments, patientPreferredEquipments, arrivalDate, departureDate, specialty, bedId);
    }
}
