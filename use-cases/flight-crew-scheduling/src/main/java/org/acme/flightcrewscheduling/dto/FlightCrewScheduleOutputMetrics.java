package org.acme.flightcrewscheduling.dto;

import ai.timefold.solver.service.definition.api.domain.DataFormat;
import ai.timefold.solver.service.definition.api.metrics.ModelOutputMetrics;

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.extensions.Extension;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonFormat;

@Schema(description = "Metrics describing the flight crew scheduling solution produced for this schedule.")
public record FlightCrewScheduleOutputMetrics(
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = TOTAL_ASSIGNED_FLIGHT_ASSIGNMENTS,
                title = "Assigned crew slots", format = DataFormat.Values.NUMBER,
                description = "The number of crew slots assigned to an employee in this schedule.",
                type = SchemaType.INTEGER, example = "70", minimum = "0", readOnly = true,
                extensions = {
                        @Extension(name = X_TF_PRIORITY, value = "1"),
                        @Extension(name = X_TF_EXAMPLE, value = "70") }) int totalAssignedFlightAssignments,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = TOTAL_UNASSIGNED_FLIGHT_ASSIGNMENTS,
                title = "Unassigned crew slots", format = DataFormat.Values.NUMBER,
                description = "The number of crew slots left without an employee in this schedule.",
                type = SchemaType.INTEGER, example = "0", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "2"),
                        @Extension(name = X_TF_EXAMPLE, value = "0") }) int totalUnassignedFlightAssignments,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = TOTAL_USED_EMPLOYEES, title = "Used employees",
                format = DataFormat.Values.NUMBER,
                description = "The number of distinct employees assigned to at least one crew slot in this schedule.",
                type = SchemaType.INTEGER, example = "18", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "3"),
                        @Extension(name = X_TF_EXAMPLE, value = "18") }) int totalUsedEmployees)
        implements
            ModelOutputMetrics{

    private static final String X_TF_PRIORITY = "x-tf-priority";
    private static final String X_TF_EXAMPLE = "x-tf-example";

    public static final String TOTAL_ASSIGNED_FLIGHT_ASSIGNMENTS = "totalAssignedFlightAssignments";
    public static final String TOTAL_UNASSIGNED_FLIGHT_ASSIGNMENTS = "totalUnassignedFlightAssignments";
    public static final String TOTAL_USED_EMPLOYEES = "totalUsedEmployees";

    public FlightCrewScheduleOutputMetrics {
        if (totalAssignedFlightAssignments < 0 || totalUnassignedFlightAssignments < 0 || totalUsedEmployees < 0) {
            throw new IllegalArgumentException("Output metrics must not be negative.");
        }
    }

    public FlightCrewScheduleOutputMetrics withTotalAssignedFlightAssignments(int totalAssignedFlightAssignments) {
        return new FlightCrewScheduleOutputMetrics(totalAssignedFlightAssignments, totalUnassignedFlightAssignments,
                totalUsedEmployees);
    }

    public FlightCrewScheduleOutputMetrics withTotalUnassignedFlightAssignments(int totalUnassignedFlightAssignments) {
        return new FlightCrewScheduleOutputMetrics(totalAssignedFlightAssignments, totalUnassignedFlightAssignments,
                totalUsedEmployees);
    }

    public FlightCrewScheduleOutputMetrics withTotalUsedEmployees(int totalUsedEmployees) {
        return new FlightCrewScheduleOutputMetrics(totalAssignedFlightAssignments, totalUnassignedFlightAssignments,
                totalUsedEmployees);
    }
}
