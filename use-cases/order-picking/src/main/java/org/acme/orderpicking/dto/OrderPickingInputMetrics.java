package org.acme.orderpicking.dto;

import ai.timefold.solver.service.definition.api.domain.DataFormat;
import ai.timefold.solver.service.definition.api.metrics.ModelInputMetrics;

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.extensions.Extension;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonFormat;

@Schema(description = "Metrics describing the order picking problem submitted in the input dataset.")
public record OrderPickingInputMetrics(
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = INPUT_METRIC_TROLLEYS, title = "Trolleys",
                format = DataFormat.Values.NUMBER,
                description = "The number of trolleys submitted in the input dataset.",
                type = SchemaType.INTEGER, example = "5", minimum = "0", readOnly = true,
                extensions = {
                        @Extension(name = X_TF_PRIORITY, value = "1"),
                        @Extension(name = X_TF_EXAMPLE, value = "5") }) int trolleys,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = INPUT_METRIC_ORDERS, title = "Orders",
                format = DataFormat.Values.NUMBER,
                description = "The number of distinct orders submitted in the input dataset.",
                type = SchemaType.INTEGER, example = "8", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "2"),
                        @Extension(name = X_TF_EXAMPLE, value = "8") }) int orders,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = INPUT_METRIC_PICK_TASKS, title = "Pick tasks",
                format = DataFormat.Values.NUMBER,
                description = "The number of pick tasks submitted in the input dataset.",
                type = SchemaType.INTEGER, example = "40", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "3"),
                        @Extension(name = X_TF_EXAMPLE, value = "40") }) int pickTasks,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = INPUT_METRIC_PRODUCTS, title = "Products",
                format = DataFormat.Values.NUMBER,
                description = "The number of distinct products submitted in the input dataset.",
                type = SchemaType.INTEGER, example = "30", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "4"),
                        @Extension(name = X_TF_EXAMPLE, value = "30") }) int products,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = INPUT_METRIC_TOTAL_VOLUME, title = "Total volume",
                format = DataFormat.Values.NUMBER,
                description = "The total volume in cm3 of all order items submitted in the input dataset.",
                type = SchemaType.INTEGER, example = "120000", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "5"),
                        @Extension(name = X_TF_EXAMPLE, value = "120000") }) long totalVolume)
        implements
            ModelInputMetrics{

    private static final String X_TF_PRIORITY = "x-tf-priority";
    private static final String X_TF_EXAMPLE = "x-tf-example";

    public static final String INPUT_METRIC_TROLLEYS = "trolleys";
    public static final String INPUT_METRIC_ORDERS = "orders";
    public static final String INPUT_METRIC_PICK_TASKS = "pickTasks";
    public static final String INPUT_METRIC_PRODUCTS = "products";
    public static final String INPUT_METRIC_TOTAL_VOLUME = "totalVolume";

    public OrderPickingInputMetrics {
        if (trolleys < 0 || orders < 0 || pickTasks < 0 || products < 0 || totalVolume < 0) {
            throw new IllegalArgumentException("Input metrics must not be negative.");
        }
    }

    public OrderPickingInputMetrics withTrolleys(int trolleys) {
        return new OrderPickingInputMetrics(trolleys, orders, pickTasks, products, totalVolume);
    }

    public OrderPickingInputMetrics withOrders(int orders) {
        return new OrderPickingInputMetrics(trolleys, orders, pickTasks, products, totalVolume);
    }

    public OrderPickingInputMetrics withPickTasks(int pickTasks) {
        return new OrderPickingInputMetrics(trolleys, orders, pickTasks, products, totalVolume);
    }

    public OrderPickingInputMetrics withProducts(int products) {
        return new OrderPickingInputMetrics(trolleys, orders, pickTasks, products, totalVolume);
    }

    public OrderPickingInputMetrics withTotalVolume(long totalVolume) {
        return new OrderPickingInputMetrics(trolleys, orders, pickTasks, products, totalVolume);
    }
}
