package org.acme.employeescheduling.dto;

import ai.timefold.solver.service.definition.api.domain.DataFormat;
import ai.timefold.solver.service.definition.api.metrics.ModelInputMetrics;

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.extensions.Extension;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonFormat;

@Schema(description = "Metrics describing the employee scheduling problem submitted in the input dataset.")
public record EmployeeScheduleInputMetrics(
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = INPUT_METRIC_EMPLOYEES, title = "Employees",
                format = DataFormat.Values.NUMBER,
                description = "The number of employees submitted in the input dataset.",
                type = SchemaType.INTEGER, example = "15", minimum = "0", readOnly = true,
                extensions = {
                        @Extension(name = X_TF_PRIORITY, value = "1"),
                        @Extension(name = X_TF_EXAMPLE, value = "15") }) int employees,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = INPUT_METRIC_SHIFTS, title = "Shifts",
                format = DataFormat.Values.NUMBER,
                description = "The number of shifts submitted in the input dataset.",
                type = SchemaType.INTEGER, example = "50", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "2"),
                        @Extension(name = X_TF_EXAMPLE, value = "50") }) int shifts,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = INPUT_METRIC_LOCATIONS, title = "Locations",
                format = DataFormat.Values.NUMBER,
                description = "The number of distinct locations submitted in the input dataset.",
                type = SchemaType.INTEGER, example = "3", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "3"),
                        @Extension(name = X_TF_EXAMPLE, value = "3") }) int locations,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = INPUT_METRIC_SKILLS, title = "Skills",
                format = DataFormat.Values.NUMBER,
                description = "The number of distinct skills submitted in the input dataset.",
                type = SchemaType.INTEGER, example = "4", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "4"),
                        @Extension(name = X_TF_EXAMPLE, value = "4") }) int skills)
        implements
            ModelInputMetrics{

    private static final String X_TF_PRIORITY = "x-tf-priority";
    private static final String X_TF_EXAMPLE = "x-tf-example";

    public static final String INPUT_METRIC_EMPLOYEES = "employees";
    public static final String INPUT_METRIC_SHIFTS = "shifts";
    public static final String INPUT_METRIC_LOCATIONS = "locations";
    public static final String INPUT_METRIC_SKILLS = "skills";

    public EmployeeScheduleInputMetrics {
        if (employees < 0 || shifts < 0 || locations < 0 || skills < 0) {
            throw new IllegalArgumentException("Input metrics must not be negative.");
        }
    }

    public EmployeeScheduleInputMetrics withEmployees(int employees) {
        return new EmployeeScheduleInputMetrics(employees, shifts, locations, skills);
    }

    public EmployeeScheduleInputMetrics withShifts(int shifts) {
        return new EmployeeScheduleInputMetrics(employees, shifts, locations, skills);
    }

    public EmployeeScheduleInputMetrics withLocations(int locations) {
        return new EmployeeScheduleInputMetrics(employees, shifts, locations, skills);
    }

    public EmployeeScheduleInputMetrics withSkills(int skills) {
        return new EmployeeScheduleInputMetrics(employees, shifts, locations, skills);
    }
}
