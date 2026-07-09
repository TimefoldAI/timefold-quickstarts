package org.acme.foodpackaging.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;

import ai.timefold.solver.service.definition.api.domain.ModelConfig;
import ai.timefold.solver.service.definition.api.validation.ModelValidator;
import ai.timefold.solver.service.definition.api.validation.ValidationBuilder;

import org.acme.foodpackaging.dto.JobDTO;
import org.acme.foodpackaging.dto.JobIdDetail;
import org.acme.foodpackaging.dto.LineDTO;
import org.acme.foodpackaging.dto.LineIdDetail;
import org.acme.foodpackaging.dto.PackagingScheduleConfigOverrides;
import org.acme.foodpackaging.dto.PackagingScheduleInput;
import org.acme.foodpackaging.dto.ProductDTO;
import org.acme.foodpackaging.service.PackagingScheduleIssues.DuplicateJobIdIssue;
import org.acme.foodpackaging.service.PackagingScheduleIssues.DuplicateLineIdIssue;
import org.acme.foodpackaging.service.PackagingScheduleIssues.JobIdMissingIssue;
import org.acme.foodpackaging.service.PackagingScheduleIssues.LineIdMissingIssue;
import org.acme.foodpackaging.service.PackagingScheduleIssues.NonExistingLineReferenceIssue;
import org.acme.foodpackaging.service.PackagingScheduleIssues.NonExistingProductReferenceIssue;

@ApplicationScoped
public class PackagingScheduleValidator
        implements ModelValidator<PackagingScheduleInput, PackagingScheduleConfigOverrides> {

    @Override
    public void validate(ValidationBuilder validationBuilder, PackagingScheduleInput modelInput,
            ModelConfig<PackagingScheduleConfigOverrides> modelConfig) {
        Set<String> lineIds = validateLines(validationBuilder, modelInput.lines());
        Set<String> productIds = collectProductIds(modelInput.products());
        validateJobs(validationBuilder, modelInput.jobs(), lineIds, productIds);
    }

    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private Set<String> validateLines(ValidationBuilder validationBuilder, List<LineDTO> lines) {
        Set<String> lineIds = new HashSet<>();
        for (LineDTO line : lines) {
            if (line.id() == null || line.id().isBlank()) {
                validationBuilder.addIssue(new LineIdMissingIssue());
            } else if (!lineIds.add(line.id())) {
                validationBuilder.addIssue(new DuplicateLineIdIssue(new LineIdDetail(line.id())));
            }
        }
        return lineIds;
    }

    private Set<String> collectProductIds(List<ProductDTO> products) {
        Set<String> productIds = new HashSet<>();
        for (ProductDTO product : products) {
            if (product.id() != null) {
                productIds.add(product.id());
            }
        }
        return productIds;
    }

    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private void validateJobs(ValidationBuilder validationBuilder, List<JobDTO> jobs, Set<String> lineIds,
            Set<String> productIds) {
        Set<String> jobIds = new HashSet<>();
        for (JobDTO job : jobs) {
            if (job.id() == null || job.id().isBlank()) {
                validationBuilder.addIssue(new JobIdMissingIssue());
            } else if (!jobIds.add(job.id())) {
                validationBuilder.addIssue(new DuplicateJobIdIssue(new JobIdDetail(job.id())));
            }
            if (job.lineId() != null && !lineIds.contains(job.lineId())) {
                validationBuilder.addIssue(new NonExistingLineReferenceIssue(new JobIdDetail(job.id())));
            }
            if (job.productId() != null && !productIds.contains(job.productId())) {
                validationBuilder.addIssue(new NonExistingProductReferenceIssue(new JobIdDetail(job.id())));
            }
        }
    }
}
