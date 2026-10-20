package org.acme.orderpicking.service;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;

import ai.timefold.solver.service.definition.api.domain.ModelConfig;
import ai.timefold.solver.service.definition.api.validation.ModelValidator;
import ai.timefold.solver.service.definition.api.validation.ValidationBuilder;

import org.acme.orderpicking.domain.Warehouse;
import org.acme.orderpicking.dto.input.PickTaskInputDTO;
import org.acme.orderpicking.dto.input.PickingCartInputDTO;
import org.acme.orderpicking.dto.input.PickingPlanConfigOverrides;
import org.acme.orderpicking.dto.input.PickingPlanInput;
import org.acme.orderpicking.dto.input.ProductInputDTO;
import org.acme.orderpicking.dto.input.WarehouseLocationInputDTO;
import org.acme.orderpicking.service.validation.PickingPlanIssue.DuplicatePickTaskIdIssue;
import org.acme.orderpicking.service.validation.PickingPlanIssue.DuplicatePickingCartIdIssue;
import org.acme.orderpicking.service.validation.PickingPlanIssue.DuplicateProductIdIssue;
import org.acme.orderpicking.service.validation.PickingPlanIssue.NonExistingPickTaskReferenceIssue;
import org.acme.orderpicking.service.validation.PickingPlanIssue.NonExistingProductReferenceIssue;
import org.acme.orderpicking.service.validation.PickingPlanIssue.PickTaskAssignedMoreThanOnceIssue;
import org.acme.orderpicking.service.validation.PickingPlanIssue.ProductExceedsBucketCapacityIssue;
import org.acme.orderpicking.service.validation.PickingPlanIssue.UnknownRackIssue;

@ApplicationScoped
public class PickingPlanValidator implements ModelValidator<PickingPlanInput, PickingPlanConfigOverrides> {

    @Override
    public void validate(ValidationBuilder validationBuilder, PickingPlanInput modelInput,
            ModelConfig<PickingPlanConfigOverrides> modelConfig) {
        // OpenAPI spec compliance is enforced by the Service module at the REST layer, before this validator ever
        // runs; only domain-specific checks belong here.
        List<ProductInputDTO> products = orEmpty(modelInput.products());
        List<PickTaskInputDTO> pickTasks = orEmpty(modelInput.pickTasks());
        List<PickingCartInputDTO> pickingCarts = orEmpty(modelInput.pickingCarts());
        Set<String> productIds = validateProducts(validationBuilder, products);
        Set<String> pickTaskIds = validatePickTasks(validationBuilder, pickTasks, productIds);
        validatePickingCarts(validationBuilder, pickingCarts, pickTaskIds);
        validateRacks(validationBuilder, products, pickingCarts);
        validateBucketCapacity(validationBuilder, products, pickingCarts);
    }

    private static Set<String> validateProducts(ValidationBuilder validationBuilder, List<ProductInputDTO> products) {
        Set<String> productIds = new HashSet<>();
        for (ProductInputDTO product : products) {
            if (hasId(product.id()) && !productIds.add(product.id())) {
                validationBuilder.addIssue(new DuplicateProductIdIssue(product.id()));
            }
        }
        return productIds;
    }

    private static Set<String> validatePickTasks(ValidationBuilder validationBuilder, List<PickTaskInputDTO> pickTasks,
            Set<String> productIds) {
        Set<String> pickTaskIds = new HashSet<>();
        for (PickTaskInputDTO pickTask : pickTasks) {
            if (hasId(pickTask.id()) && !pickTaskIds.add(pickTask.id())) {
                validationBuilder.addIssue(new DuplicatePickTaskIdIssue(pickTask.id()));
            }
            if (pickTask.productId() != null && !productIds.contains(pickTask.productId())) {
                validationBuilder.addIssue(new NonExistingProductReferenceIssue(pickTask.id()));
            }
        }
        return pickTaskIds;
    }

    private static void validatePickingCarts(ValidationBuilder validationBuilder, List<PickingCartInputDTO> pickingCarts,
            Set<String> pickTaskIds) {
        Set<String> pickingCartIds = new HashSet<>();
        Set<String> assignedPickTaskIds = new HashSet<>();
        for (PickingCartInputDTO pickingCart : pickingCarts) {
            if (hasId(pickingCart.id()) && !pickingCartIds.add(pickingCart.id())) {
                validationBuilder.addIssue(new DuplicatePickingCartIdIssue(pickingCart.id()));
            }
            boolean reportedUnknownPickTask = false;
            for (String pickTaskId : orEmpty(pickingCart.pickTaskIds())) {
                if (!pickTaskIds.contains(pickTaskId)) {
                    if (!reportedUnknownPickTask) {
                        validationBuilder.addIssue(new NonExistingPickTaskReferenceIssue(pickingCart.id(), pickTaskId));
                        reportedUnknownPickTask = true;
                    }
                } else if (!assignedPickTaskIds.add(pickTaskId)) {
                    validationBuilder.addIssue(new PickTaskAssignedMoreThanOnceIssue(pickTaskId));
                }
            }
        }
    }

    private static void validateRacks(ValidationBuilder validationBuilder, List<ProductInputDTO> products,
            List<PickingCartInputDTO> pickingCarts) {
        Set<String> unknownRackIds = new LinkedHashSet<>();
        for (ProductInputDTO product : products) {
            addIfUnknownRack(unknownRackIds, product.location());
        }
        for (PickingCartInputDTO pickingCart : pickingCarts) {
            addIfUnknownRack(unknownRackIds, pickingCart.location());
        }
        unknownRackIds.forEach(rackId -> validationBuilder.addIssue(new UnknownRackIssue(rackId)));
    }

    private static void addIfUnknownRack(Set<String> unknownRackIds, WarehouseLocationInputDTO location) {
        if (location != null && location.rackId() != null && !Warehouse.hasRack(location.rackId())) {
            unknownRackIds.add(location.rackId());
        }
    }

    private static void validateBucketCapacity(ValidationBuilder validationBuilder, List<ProductInputDTO> products,
            List<PickingCartInputDTO> pickingCarts) {
        if (pickingCarts.isEmpty()) {
            return;
        }
        int largestBucketCapacity = pickingCarts.stream().mapToInt(PickingCartInputDTO::bucketCapacity).max().orElseThrow();
        for (ProductInputDTO product : products) {
            if (product.volume() > largestBucketCapacity) {
                validationBuilder.addIssue(
                        new ProductExceedsBucketCapacityIssue(product.id(), product.volume(), largestBucketCapacity));
            }
        }
    }

    private static <T> List<T> orEmpty(List<T> list) {
        return list == null ? List.of() : list;
    }

    private static boolean hasId(String id) {
        return id != null && !id.isEmpty();
    }
}
