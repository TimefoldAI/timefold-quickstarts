package org.acme.bedallocation.dto;

import java.util.List;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A patient stay that must be assigned to a bed for a number of nights.")
public record StayDTO(
        @Schema(description = "Unique identifier of the stay.") String id,
        @Schema(description = "Name of the patient for this stay.") String patientName,
        @Schema(description = "Gender of the patient, MALE or FEMALE.") String patientGender,
        @Schema(description = "Age of the patient.") int patientAge,
        @Schema(description = "Maximum room capacity the patient prefers. Null when there is no preference.") Integer patientPreferredMaximumRoomCapacity,
        @Schema(description = "Equipment the stay requires in the assigned room.") List<String> patientRequiredEquipments,
        @Schema(description = "Equipment the stay prefers in the assigned room.") List<String> patientPreferredEquipments,
        @Schema(description = "First night of the stay, inclusive, in ISO-8601 format (yyyy-MM-dd).") String arrivalDate,
        @Schema(description = "Last night of the stay, inclusive, in ISO-8601 format (yyyy-MM-dd).") String departureDate,
        @Schema(description = "Required specialty for the stay.") String specialty,
        @Schema(description = "ID of the bed assigned to the stay. Null when unassigned.") String bedId) {

    @SuppressWarnings("PMD.NullAssignment")
    public StayDTO {
        patientName = patientName == null ? "" : patientName;
        patientRequiredEquipments = patientRequiredEquipments == null ? List.of() : List.copyOf(patientRequiredEquipments);
        patientPreferredEquipments = patientPreferredEquipments == null ? List.of() : List.copyOf(patientPreferredEquipments);
        bedId = bedId != null && bedId.isBlank() ? null : bedId;
    }

    public StayDTO withId(String id) {
        return new StayDTO(id, patientName, patientGender, patientAge, patientPreferredMaximumRoomCapacity,
                patientRequiredEquipments, patientPreferredEquipments, arrivalDate, departureDate, specialty, bedId);
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

    public StayDTO withArrivalDate(String arrivalDate) {
        return new StayDTO(id, patientName, patientGender, patientAge, patientPreferredMaximumRoomCapacity,
                patientRequiredEquipments, patientPreferredEquipments, arrivalDate, departureDate, specialty, bedId);
    }

    public StayDTO withDepartureDate(String departureDate) {
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
