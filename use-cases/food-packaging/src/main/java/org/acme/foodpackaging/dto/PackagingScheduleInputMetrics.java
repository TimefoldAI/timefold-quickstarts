package org.acme.foodpackaging.dto;

import ai.timefold.solver.service.definition.api.domain.DataFormat;
import ai.timefold.solver.service.definition.api.metrics.ModelInputMetrics;

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.extensions.Extension;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonFormat;

@Schema(description = "Metrics describing the food packaging problem submitted in the input dataset.")
public record PackagingScheduleInputMetrics(
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = INPUT_METRIC_JOBS, title = "Jobs",
                format = DataFormat.Values.NUMBER,
                description = "The number of jobs submitted in the input dataset.",
                type = SchemaType.INTEGER, example = "100", minimum = "0", readOnly = true,
                extensions = {
                        @Extension(name = X_TF_PRIORITY, value = "1"),
                        @Extension(name = X_TF_EXAMPLE, value = "100") }) int jobs,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = INPUT_METRIC_LINES, title = "Lines",
                format = DataFormat.Values.NUMBER,
                description = "The number of lines submitted in the input dataset.",
                type = SchemaType.INTEGER, example = "5", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "2"),
                        @Extension(name = X_TF_EXAMPLE, value = "5") }) int lines,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = INPUT_METRIC_OPERATORS, title = "Operators",
                format = DataFormat.Values.NUMBER,
                description = "The number of operators submitted in the input dataset.",
                type = SchemaType.INTEGER, example = "5", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "3"),
                        @Extension(name = X_TF_EXAMPLE, value = "5") }) int operators,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = INPUT_METRIC_PRODUCTS, title = "Products",
                format = DataFormat.Values.NUMBER,
                description = "The number of products submitted in the input dataset.",
                type = SchemaType.INTEGER, example = "60", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "4"),
                        @Extension(name = X_TF_EXAMPLE, value = "60") }) int products)
        implements
            ModelInputMetrics{

    private static final String X_TF_PRIORITY = "x-tf-priority";
    private static final String X_TF_EXAMPLE = "x-tf-example";

    public static final String INPUT_METRIC_JOBS = "jobs";
    public static final String INPUT_METRIC_LINES = "lines";
    public static final String INPUT_METRIC_OPERATORS = "operators";
    public static final String INPUT_METRIC_PRODUCTS = "products";

    public PackagingScheduleInputMetrics {
        if (jobs < 0 || lines < 0 || operators < 0 || products < 0) {
            throw new IllegalArgumentException("Input metrics must not be negative.");
        }
    }

    public PackagingScheduleInputMetrics withJobs(int jobs) {
        return new PackagingScheduleInputMetrics(jobs, lines, operators, products);
    }

    public PackagingScheduleInputMetrics withLines(int lines) {
        return new PackagingScheduleInputMetrics(jobs, lines, operators, products);
    }

    public PackagingScheduleInputMetrics withOperators(int operators) {
        return new PackagingScheduleInputMetrics(jobs, lines, operators, products);
    }

    public PackagingScheduleInputMetrics withProducts(int products) {
        return new PackagingScheduleInputMetrics(jobs, lines, operators, products);
    }
}
