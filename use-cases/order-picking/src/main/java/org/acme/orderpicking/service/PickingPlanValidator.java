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
import org.acme.orderpicking.dto.input.PickingPlanConfigOverrides;
import org.acme.orderpicking.dto.input.PickingPlanInput;
import org.acme.orderpicking.dto.input.ProductInputDTO;
import org.acme.orderpicking.dto.input.TrolleyInputDTO;
import org.acme.orderpicking.dto.input.WarehouseLocationInputDTO;
import org.acme.orderpicking.service.validation.PickingPlanIssue.DuplicatePickTaskIdIssue;
import org.acme.orderpicking.service.validation.PickingPlanIssue.DuplicateProductIdIssue;
import org.acme.orderpicking.service.validation.PickingPlanIssue.DuplicateTrolleyIdIssue;
import org.acme.orderpicking.service.validation.PickingPlanIssue.NonExistingPickTaskReferenceIssue;
import org.acme.orderpicking.service.validation.PickingPlanIssue.NonExistingProductReferenceIssue;
import org.acme.orderpicking.service.validation.PickingPlanIssue.PickTaskAssignedMoreThanOnceIssue;
import org.acme.orderpicking.service.validation.PickingPlanIssue.ProductExceedsBucketCapacityIssue;
import org.acme.orderpicking.service.validation.PickingPlanIssue.UnknownShelvingIssue;

@ApplicationScoped
public class PickingPlanValidator implements ModelValidator<PickingPlanInput, PickingPlanConfigOverrides> {

    @Override
    public void validate(ValidationBuilder validationBuilder, PickingPlanInput modelInput,
            ModelConfig<PickingPlanConfigOverrides> modelConfig) {
        // OpenAPI spec compliance is enforced by the Service module at the REST layer, before this validator ever
        // runs; only domain-specific checks belong here.
        List<ProductInputDTO> products = orEmpty(modelInput.products());
        List<PickTaskInputDTO> pickTasks = orEmpty(modelInput.pickTasks());
        List<TrolleyInputDTO> trolleys = orEmpty(modelInput.trolleys());
        Set<String> productIds = validateProducts(validationBuilder, products);
        Set<String> pickTaskIds = validatePickTasks(validationBuilder, pickTasks, productIds);
        validateTrolleys(validationBuilder, trolleys, pickTaskIds);
        validateShelvings(validationBuilder, products, trolleys);
        validateBucketCapacity(validationBuilder, products, trolleys);
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

    private static void validateTrolleys(ValidationBuilder validationBuilder, List<TrolleyInputDTO> trolleys,
            Set<String> pickTaskIds) {
        Set<String> trolleyIds = new HashSet<>();
        Set<String> assignedPickTaskIds = new HashSet<>();
        for (TrolleyInputDTO trolley : trolleys) {
            if (hasId(trolley.id()) && !trolleyIds.add(trolley.id())) {
                validationBuilder.addIssue(new DuplicateTrolleyIdIssue(trolley.id()));
            }
            boolean reportedUnknownPickTask = false;
            for (String pickTaskId : orEmpty(trolley.pickTaskIds())) {
                if (!pickTaskIds.contains(pickTaskId)) {
                    if (!reportedUnknownPickTask) {
                        validationBuilder.addIssue(new NonExistingPickTaskReferenceIssue(trolley.id(), pickTaskId));
                        reportedUnknownPickTask = true;
                    }
                } else if (!assignedPickTaskIds.add(pickTaskId)) {
                    validationBuilder.addIssue(new PickTaskAssignedMoreThanOnceIssue(pickTaskId));
                }
            }
        }
    }

    private static void validateShelvings(ValidationBuilder validationBuilder, List<ProductInputDTO> products,
            List<TrolleyInputDTO> trolleys) {
        Set<String> unknownShelvingIds = new LinkedHashSet<>();
        for (ProductInputDTO product : products) {
            addIfUnknownShelving(unknownShelvingIds, product.location());
        }
        for (TrolleyInputDTO trolley : trolleys) {
            addIfUnknownShelving(unknownShelvingIds, trolley.location());
        }
        unknownShelvingIds.forEach(shelvingId -> validationBuilder.addIssue(new UnknownShelvingIssue(shelvingId)));
    }

    private static void addIfUnknownShelving(Set<String> unknownShelvingIds, WarehouseLocationInputDTO location) {
        if (location != null && location.shelvingId() != null && !Warehouse.hasShelving(location.shelvingId())) {
            unknownShelvingIds.add(location.shelvingId());
        }
    }

    private static void validateBucketCapacity(ValidationBuilder validationBuilder, List<ProductInputDTO> products,
            List<TrolleyInputDTO> trolleys) {
        if (trolleys.isEmpty()) {
            return;
        }
        int largestBucketCapacity = trolleys.stream().mapToInt(TrolleyInputDTO::bucketCapacity).max().orElseThrow();
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
