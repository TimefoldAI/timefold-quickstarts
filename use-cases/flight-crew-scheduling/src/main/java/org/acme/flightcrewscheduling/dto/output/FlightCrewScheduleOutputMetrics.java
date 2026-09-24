package org.acme.flightcrewscheduling.dto.output;

import ai.timefold.solver.service.definition.api.domain.DataFormat;
import ai.timefold.solver.service.definition.api.metrics.ModelOutputMetrics;

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.extensions.Extension;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonFormat;

@Schema(description = "Metrics describing the flight crew scheduling solution produced for this schedule.")
public record FlightCrewScheduleOutputMetrics(
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = TOTAL_ASSIGNED_FLIGHT_ASSIGNMENTS,
                title = "Assigned seats", format = DataFormat.Values.NUMBER,
                description = "The number of crew seats assigned to a crew member in this schedule.",
                type = SchemaType.INTEGER, examples = "62", minimum = "0", readOnly = true,
                extensions = {
                        @Extension(name = X_TF_PRIORITY, value = "1"),
                        @Extension(name = X_TF_EXAMPLE, value = "62") }) int totalAssignedFlightAssignments,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = TOTAL_UNASSIGNED_FLIGHT_ASSIGNMENTS,
                title = "Unassigned seats", format = DataFormat.Values.NUMBER,
                description = "The number of crew seats left without a crew member in this schedule.",
                type = SchemaType.INTEGER, examples = "0", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "2"),
                        @Extension(name = X_TF_EXAMPLE, value = "0") }) int totalUnassignedFlightAssignments,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = TOTAL_USED_EMPLOYEES, title = "Rostered crew",
                format = DataFormat.Values.NUMBER,
                description = "The number of distinct crew members flying at least one seat in this schedule.",
                type = SchemaType.INTEGER, examples = "24", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "3"),
                        @Extension(name = X_TF_EXAMPLE, value = "24") }) int totalUsedEmployees,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = TOTAL_COVERED_FLIGHTS, title = "Crewed flights",
                format = DataFormat.Values.NUMBER,
                description = "The number of distinct flights with at least one crewed seat in this schedule.",
                type = SchemaType.INTEGER, examples = "14", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "4"),
                        @Extension(name = X_TF_EXAMPLE, value = "14") }) int totalCoveredFlights)
        implements
            ModelOutputMetrics {

    private static final String X_TF_PRIORITY = "x-tf-priority";
    private static final String X_TF_EXAMPLE = "x-tf-example";

    public static final String TOTAL_ASSIGNED_FLIGHT_ASSIGNMENTS = "totalAssignedFlightAssignments";
    public static final String TOTAL_UNASSIGNED_FLIGHT_ASSIGNMENTS = "totalUnassignedFlightAssignments";
    public static final String TOTAL_USED_EMPLOYEES = "totalUsedEmployees";
    public static final String TOTAL_COVERED_FLIGHTS = "totalCoveredFlights";

    public FlightCrewScheduleOutputMetrics {
        if (totalAssignedFlightAssignments < 0 || totalUnassignedFlightAssignments < 0 || totalUsedEmployees < 0
                || totalCoveredFlights < 0) {
            throw new IllegalArgumentException(
                    "Output metrics must not be negative, but were totalAssignedFlightAssignments (%d), totalUnassignedFlightAssignments (%d), totalUsedEmployees (%d), totalCoveredFlights (%d)."
                            .formatted(totalAssignedFlightAssignments, totalUnassignedFlightAssignments,
                                    totalUsedEmployees, totalCoveredFlights));
        }
    }
}
