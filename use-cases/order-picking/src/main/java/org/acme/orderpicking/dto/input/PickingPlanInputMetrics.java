package org.acme.orderpicking.dto.input;

import ai.timefold.solver.service.definition.api.domain.DataFormat;
import ai.timefold.solver.service.definition.api.metrics.ModelInputMetrics;

import org.eclipse.microprofile.openapi.annotations.extensions.Extension;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Metrics describing the order picking problem submitted in the input dataset.")
public record PickingPlanInputMetrics(
        @Schema(title = "Pick tasks", format = DataFormat.Values.NUMBER,
                description = "The number of pick tasks submitted in the input dataset.",
                examples = "137", minimum = "0", readOnly = true,
                extensions = {
                        @Extension(name = X_TF_PRIORITY, value = "1"),
                        @Extension(name = X_TF_EXAMPLE, value = "137") }) int pickTasks,
        @Schema(title = "Orders", format = DataFormat.Values.NUMBER,
                description = "The number of distinct orders the pick tasks belong to.",
                examples = "8", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "2"),
                        @Extension(name = X_TF_EXAMPLE, value = "8") }) int orders,
        @Schema(title = "Products", format = DataFormat.Values.NUMBER,
                description = "The number of distinct products the pick tasks pick.",
                examples = "40", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "3"),
                        @Extension(name = X_TF_EXAMPLE, value = "40") }) int products,
        @Schema(title = "PickingCarts", format = DataFormat.Values.NUMBER,
                description = "The number of picking carts submitted in the input dataset.",
                examples = "5", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "4"),
                        @Extension(name = X_TF_EXAMPLE, value = "5") }) int pickingCarts)
        implements
            ModelInputMetrics {

    private static final String X_TF_PRIORITY = "x-tf-priority";
    private static final String X_TF_EXAMPLE = "x-tf-example";

    public PickingPlanInputMetrics {
        if (pickTasks < 0 || orders < 0 || products < 0 || pickingCarts < 0) {
            throw new IllegalArgumentException(
                    "Input metrics must not be negative, but were pickTasks (%d), orders (%d), products (%d), pickingCarts (%d)."
                            .formatted(pickTasks, orders, products, pickingCarts));
        }
    }
}
