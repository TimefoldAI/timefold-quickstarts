package org.acme.foodpackaging.service;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;

import ai.timefold.solver.service.definition.api.domain.ModelConfig;
import ai.timefold.solver.service.definition.api.validation.ModelValidator;
import ai.timefold.solver.service.definition.api.validation.ValidationBuilder;

import org.acme.foodpackaging.dto.input.CleaningDurationDTO;
import org.acme.foodpackaging.dto.input.JobDTO;
import org.acme.foodpackaging.dto.input.LineDTO;
import org.acme.foodpackaging.dto.input.OperatorDTO;
import org.acme.foodpackaging.dto.input.PackagingScheduleConfigOverrides;
import org.acme.foodpackaging.dto.input.PackagingScheduleInput;
import org.acme.foodpackaging.dto.input.ProductDTO;
import org.acme.foodpackaging.service.validation.PackagingScheduleIssue.DuplicateJobIdIssue;
import org.acme.foodpackaging.service.validation.PackagingScheduleIssue.DuplicateLineIdIssue;
import org.acme.foodpackaging.service.validation.PackagingScheduleIssue.DuplicateOperatorIdIssue;
import org.acme.foodpackaging.service.validation.PackagingScheduleIssue.DuplicateProductIdIssue;
import org.acme.foodpackaging.service.validation.PackagingScheduleIssue.JobOnMultipleLinesIssue;
import org.acme.foodpackaging.service.validation.PackagingScheduleIssue.MissingCleaningDurationIssue;
import org.acme.foodpackaging.service.validation.PackagingScheduleIssue.NonExistingJobReferenceIssue;
import org.acme.foodpackaging.service.validation.PackagingScheduleIssue.NonExistingOperatorReferenceIssue;
import org.acme.foodpackaging.service.validation.PackagingScheduleIssue.NonExistingProductReferenceIssue;

@ApplicationScoped
public class PackagingScheduleValidator
        implements ModelValidator<PackagingScheduleInput, PackagingScheduleConfigOverrides> {

    @Override
    public void validate(ValidationBuilder validationBuilder, PackagingScheduleInput modelInput,
            ModelConfig<PackagingScheduleConfigOverrides> modelConfig) {
        // OpenAPI spec (Bean Validation) compliance is enforced by the Service module at the REST layer,
        // before this validator ever runs; only domain-specific checks (duplicate and dangling references,
        // and the completeness of the cleaning duration matrix) belong here.
        List<ProductDTO> products = orEmpty(modelInput.products());
        Set<String> productIds = validateProducts(validationBuilder, products);
        validateCleaningDurations(validationBuilder, products, productIds);
        Set<String> operatorIds = validateOperators(validationBuilder, orEmpty(modelInput.operators()));
        Set<String> jobIds = validateJobs(validationBuilder, orEmpty(modelInput.jobs()), productIds);
        validateLines(validationBuilder, orEmpty(modelInput.lines()), operatorIds, jobIds);
    }

    private static <T> List<T> orEmpty(List<T> list) {
        return list == null ? List.of() : list;
    }

    private static Set<String> validateProducts(ValidationBuilder validationBuilder, List<ProductDTO> products) {
        Set<String> productIds = new LinkedHashSet<>();
        for (ProductDTO product : products) {
            if (hasId(product.id()) && !productIds.add(product.id())) {
                validationBuilder.addIssue(new DuplicateProductIdIssue(product.id()));
            }
        }
        return productIds;
    }

    /**
     * A line can switch from any product to any other one (and can produce the same product twice in a row),
     * so every product needs a cleaning duration for every product of the dataset, itself included. Only the
     * first missing entry of a product is reported: a dataset that forgot the matrix entirely would otherwise
     * produce one issue per product pair.
     */
    private static void validateCleaningDurations(ValidationBuilder validationBuilder, List<ProductDTO> products,
            Set<String> productIds) {
        for (ProductDTO product : products) {
            Set<String> previousProductIds = new HashSet<>();
            for (CleaningDurationDTO cleaningDuration : product.cleaningDurations()) {
                if (!productIds.contains(cleaningDuration.previousProductId())) {
                    validationBuilder.addIssue(new NonExistingProductReferenceIssue(cleaningDuration.previousProductId()));
                    continue;
                }
                previousProductIds.add(cleaningDuration.previousProductId());
            }
            productIds.stream()
                    .filter(productId -> !previousProductIds.contains(productId))
                    .findFirst()
                    .ifPresent(missingProductId -> validationBuilder
                            .addIssue(new MissingCleaningDurationIssue(product.id(), missingProductId)));
        }
    }

    private static Set<String> validateOperators(ValidationBuilder validationBuilder, List<OperatorDTO> operators) {
        Set<String> operatorIds = new LinkedHashSet<>();
        for (OperatorDTO operator : operators) {
            if (hasId(operator.id()) && !operatorIds.add(operator.id())) {
                validationBuilder.addIssue(new DuplicateOperatorIdIssue(operator.id()));
            }
        }
        return operatorIds;
    }

    private static Set<String> validateJobs(ValidationBuilder validationBuilder, List<JobDTO> jobs,
            Set<String> productIds) {
        Set<String> jobIds = new LinkedHashSet<>();
        for (JobDTO job : jobs) {
            if (hasId(job.id()) && !jobIds.add(job.id())) {
                validationBuilder.addIssue(new DuplicateJobIdIssue(job.id()));
            }
            if (!productIds.contains(job.productId())) {
                validationBuilder.addIssue(new NonExistingProductReferenceIssue(job.productId()));
            }
        }
        return jobIds;
    }

    private static void validateLines(ValidationBuilder validationBuilder, List<LineDTO> lines,
            Set<String> operatorIds, Set<String> jobIds) {
        Set<String> lineIds = new HashSet<>();
        Set<String> scheduledJobIds = new HashSet<>();
        for (LineDTO line : lines) {
            // A line without an ID cannot be pointed at, so its other issues are reported without a line ID.
            String lineId = hasId(line.id()) ? line.id() : null;
            if (lineId != null && !lineIds.add(lineId)) {
                validationBuilder.addIssue(new DuplicateLineIdIssue(lineId));
            }
            if (line.operatorId() != null && !operatorIds.contains(line.operatorId())) {
                validationBuilder.addIssue(new NonExistingOperatorReferenceIssue(lineId));
            }
            for (String jobId : line.jobIds()) {
                if (!jobIds.contains(jobId)) {
                    validationBuilder.addIssue(new NonExistingJobReferenceIssue(lineId));
                } else if (!scheduledJobIds.add(jobId)) {
                    validationBuilder.addIssue(new JobOnMultipleLinesIssue(jobId));
                }
            }
        }
    }

    private static boolean hasId(String id) {
        return id != null && !id.isBlank();
    }
}
