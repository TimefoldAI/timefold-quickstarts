package org.acme.bedallocation.dto;

import ai.timefold.solver.service.definition.api.domain.DataFormat;
import ai.timefold.solver.service.definition.api.metrics.ModelInputMetrics;

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.extensions.Extension;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonFormat;

@Schema(description = "Metrics describing the bed allocation problem submitted in the input dataset.")
public record BedScheduleInputMetrics(
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = INPUT_METRIC_STAYS, title = "Stays",
                format = DataFormat.Values.NUMBER,
                description = "The number of stays submitted in the input dataset.",
                type = SchemaType.INTEGER, example = "100", minimum = "0", readOnly = true,
                extensions = {
                        @Extension(name = X_TF_PRIORITY, value = "1"),
                        @Extension(name = X_TF_EXAMPLE, value = "100") }) int stays,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = INPUT_METRIC_BEDS, title = "Beds",
                format = DataFormat.Values.NUMBER,
                description = "The number of beds submitted in the input dataset.",
                type = SchemaType.INTEGER, example = "20", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "2"),
                        @Extension(name = X_TF_EXAMPLE, value = "20") }) int beds,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = INPUT_METRIC_ROOMS, title = "Rooms",
                format = DataFormat.Values.NUMBER,
                description = "The number of rooms submitted in the input dataset.",
                type = SchemaType.INTEGER, example = "10", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "3"),
                        @Extension(name = X_TF_EXAMPLE, value = "10") }) int rooms)
        implements
            ModelInputMetrics{

    private static final String X_TF_PRIORITY = "x-tf-priority";
    private static final String X_TF_EXAMPLE = "x-tf-example";

    public static final String INPUT_METRIC_STAYS = "stays";
    public static final String INPUT_METRIC_BEDS = "beds";
    public static final String INPUT_METRIC_ROOMS = "rooms";

    public BedScheduleInputMetrics {
        if (stays < 0 || beds < 0 || rooms < 0) {
            throw new IllegalArgumentException("Input metrics must not be negative.");
        }
    }

    public BedScheduleInputMetrics withStays(int stays) {
        return new BedScheduleInputMetrics(stays, beds, rooms);
    }

    public BedScheduleInputMetrics withBeds(int beds) {
        return new BedScheduleInputMetrics(stays, beds, rooms);
    }

    public BedScheduleInputMetrics withRooms(int rooms) {
        return new BedScheduleInputMetrics(stays, beds, rooms);
    }
}
