package org.acme.bedallocation.dto;

import static org.acme.bedallocation.support.ObjectHelper.immutableCopy;

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
        patientRequiredEquipments = immutableCopy(patientRequiredEquipments);
        patientPreferredEquipments = immutableCopy(patientPreferredEquipments);
        bedId = normalizeId(bedId);
    }

    private static String normalizeId(String id) {
        return id != null && id.isBlank() ? null : id;
    }

    public StayDTO withBedId(String bedId) {
        return toBuilder().bedId(bedId).build();
    }

    public static Builder builder(String id, String arrivalDate, String departureDate) {
        return new Builder(id, arrivalDate, departureDate);
    }

    public Builder toBuilder() {
        return new Builder(id, arrivalDate, departureDate)
                .patientName(patientName)
                .patientGender(patientGender)
                .patientAge(patientAge)
                .patientPreferredMaximumRoomCapacity(patientPreferredMaximumRoomCapacity)
                .patientRequiredEquipments(patientRequiredEquipments)
                .patientPreferredEquipments(patientPreferredEquipments)
                .specialty(specialty)
                .bedId(bedId);
    }

    public static final class Builder {

        private final String id;
        private final String arrivalDate;
        private final String departureDate;
        private String patientName;
        private String patientGender;
        private int patientAge;
        private Integer patientPreferredMaximumRoomCapacity;
        private List<String> patientRequiredEquipments;
        private List<String> patientPreferredEquipments;
        private String specialty;
        private String bedId;

        private Builder(String id, String arrivalDate, String departureDate) {
            this.id = id;
            this.arrivalDate = arrivalDate;
            this.departureDate = departureDate;
        }

        public Builder patientName(String patientName) {
            this.patientName = patientName;
            return this;
        }

        public Builder patientGender(String patientGender) {
            this.patientGender = patientGender;
            return this;
        }

        public Builder patientAge(int patientAge) {
            this.patientAge = patientAge;
            return this;
        }

        public Builder patientPreferredMaximumRoomCapacity(Integer patientPreferredMaximumRoomCapacity) {
            this.patientPreferredMaximumRoomCapacity = patientPreferredMaximumRoomCapacity;
            return this;
        }

        public Builder patientRequiredEquipments(List<String> patientRequiredEquipments) {
            this.patientRequiredEquipments = patientRequiredEquipments;
            return this;
        }

        public Builder patientPreferredEquipments(List<String> patientPreferredEquipments) {
            this.patientPreferredEquipments = patientPreferredEquipments;
            return this;
        }

        public Builder specialty(String specialty) {
            this.specialty = specialty;
            return this;
        }

        public Builder bedId(String bedId) {
            this.bedId = bedId;
            return this;
        }

        public StayDTO build() {
            return new StayDTO(id, patientName, patientGender, patientAge, patientPreferredMaximumRoomCapacity,
                    patientRequiredEquipments, patientPreferredEquipments, arrivalDate, departureDate, specialty, bedId);
        }
    }
}
