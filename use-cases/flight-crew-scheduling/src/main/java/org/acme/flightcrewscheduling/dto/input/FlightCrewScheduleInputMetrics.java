package org.acme.flightcrewscheduling.dto.input;

import ai.timefold.solver.service.definition.api.domain.DataFormat;
import ai.timefold.solver.service.definition.api.metrics.ModelInputMetrics;

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.extensions.Extension;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonFormat;

@Schema(description = "Metrics describing the flight crew scheduling problem submitted in the input dataset.")
public record FlightCrewScheduleInputMetrics(
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = INPUT_METRIC_FLIGHT_ASSIGNMENTS,
                title = "Flight assignments", format = DataFormat.Values.NUMBER,
                description = "The number of crew seats submitted in the input dataset.",
                type = SchemaType.INTEGER, examples = "62", minimum = "0", readOnly = true,
                extensions = {
                        @Extension(name = X_TF_PRIORITY, value = "1"),
                        @Extension(name = X_TF_EXAMPLE, value = "62") }) int flightAssignments,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = INPUT_METRIC_FLIGHTS, title = "Flights",
                format = DataFormat.Values.NUMBER, description = "The number of flights submitted in the input dataset.",
                type = SchemaType.INTEGER, examples = "14", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "2"),
                        @Extension(name = X_TF_EXAMPLE, value = "14") }) int flights,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = INPUT_METRIC_EMPLOYEES, title = "Crew members",
                format = DataFormat.Values.NUMBER,
                description = "The number of crew members submitted in the input dataset.",
                type = SchemaType.INTEGER, examples = "24", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "3"),
                        @Extension(name = X_TF_EXAMPLE, value = "24") }) int employees,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = INPUT_METRIC_AIRPORTS, title = "Airports",
                format = DataFormat.Values.NUMBER, description = "The number of airports submitted in the input dataset.",
                type = SchemaType.INTEGER, examples = "6", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "4"),
                        @Extension(name = X_TF_EXAMPLE, value = "6") }) int airports)
        implements
            ModelInputMetrics {

    private static final String X_TF_PRIORITY = "x-tf-priority";
    private static final String X_TF_EXAMPLE = "x-tf-example";

    public static final String INPUT_METRIC_FLIGHT_ASSIGNMENTS = "flightAssignments";
    public static final String INPUT_METRIC_FLIGHTS = "flights";
    public static final String INPUT_METRIC_EMPLOYEES = "employees";
    public static final String INPUT_METRIC_AIRPORTS = "airports";

    public FlightCrewScheduleInputMetrics {
        if (flightAssignments < 0 || flights < 0 || employees < 0 || airports < 0) {
            throw new IllegalArgumentException(
                    "Input metrics must not be negative, but were flightAssignments (%d), flights (%d), employees (%d), airports (%d)."
                            .formatted(flightAssignments, flights, employees, airports));
        }
    }
}
