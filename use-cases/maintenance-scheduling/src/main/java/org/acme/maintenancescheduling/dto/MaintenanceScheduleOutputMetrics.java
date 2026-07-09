package org.acme.maintenancescheduling.dto;

import ai.timefold.solver.service.definition.api.domain.DataFormat;
import ai.timefold.solver.service.definition.api.metrics.ModelOutputMetrics;

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.extensions.Extension;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonFormat;

@Schema(description = "Metrics describing the maintenance scheduling solution produced for this schedule.")
public record MaintenanceScheduleOutputMetrics(
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = TOTAL_ASSIGNED_JOBS, title = "Assigned jobs",
                format = DataFormat.Values.NUMBER,
                description = "The number of jobs assigned to both a crew and a start date in this schedule.",
                type = SchemaType.INTEGER, example = "24", minimum = "0", readOnly = true,
                extensions = {
                        @Extension(name = X_TF_PRIORITY, value = "1"),
                        @Extension(name = X_TF_EXAMPLE, value = "24") }) int totalAssignedJobs,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = TOTAL_UNASSIGNED_JOBS, title = "Unassigned jobs",
                format = DataFormat.Values.NUMBER,
                description = "The number of jobs left without a crew or start date in this schedule.",
                type = SchemaType.INTEGER, example = "0", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "2"),
                        @Extension(name = X_TF_EXAMPLE, value = "0") }) int totalUnassignedJobs,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = TOTAL_USED_CREWS, title = "Used crews",
                format = DataFormat.Values.NUMBER,
                description = "The number of distinct crews used by at least one job in this schedule.",
                type = SchemaType.INTEGER, example = "3", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "3"),
                        @Extension(name = X_TF_EXAMPLE, value = "3") }) int totalUsedCrews)
        implements
            ModelOutputMetrics{

    private static final String X_TF_PRIORITY = "x-tf-priority";
    private static final String X_TF_EXAMPLE = "x-tf-example";

    public static final String TOTAL_ASSIGNED_JOBS = "totalAssignedJobs";
    public static final String TOTAL_UNASSIGNED_JOBS = "totalUnassignedJobs";
    public static final String TOTAL_USED_CREWS = "totalUsedCrews";

    public MaintenanceScheduleOutputMetrics {
        if (totalAssignedJobs < 0 || totalUnassignedJobs < 0 || totalUsedCrews < 0) {
            throw new IllegalArgumentException("Output metrics must not be negative.");
        }
    }

    public MaintenanceScheduleOutputMetrics withTotalAssignedJobs(int totalAssignedJobs) {
        return new MaintenanceScheduleOutputMetrics(totalAssignedJobs, totalUnassignedJobs, totalUsedCrews);
    }

    public MaintenanceScheduleOutputMetrics withTotalUnassignedJobs(int totalUnassignedJobs) {
        return new MaintenanceScheduleOutputMetrics(totalAssignedJobs, totalUnassignedJobs, totalUsedCrews);
    }

    public MaintenanceScheduleOutputMetrics withTotalUsedCrews(int totalUsedCrews) {
        return new MaintenanceScheduleOutputMetrics(totalAssignedJobs, totalUnassignedJobs, totalUsedCrews);
    }
}
