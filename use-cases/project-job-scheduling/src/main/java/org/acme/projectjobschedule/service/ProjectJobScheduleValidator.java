package org.acme.projectjobschedule.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;

import ai.timefold.solver.service.definition.api.domain.ModelConfig;
import ai.timefold.solver.service.definition.api.validation.ModelValidator;
import ai.timefold.solver.service.definition.api.validation.ValidationBuilder;

import org.acme.projectjobschedule.dto.AllocationDTO;
import org.acme.projectjobschedule.dto.AllocationIdDetail;
import org.acme.projectjobschedule.dto.ExecutionModeDTO;
import org.acme.projectjobschedule.dto.JobDTO;
import org.acme.projectjobschedule.dto.JobIdDetail;
import org.acme.projectjobschedule.dto.ProjectJobScheduleConfigOverrides;
import org.acme.projectjobschedule.dto.ProjectJobScheduleInput;
import org.acme.projectjobschedule.dto.ResourceDTO;
import org.acme.projectjobschedule.dto.ResourceIdDetail;
import org.acme.projectjobschedule.service.ProjectJobScheduleIssues.AllocationIdMissingIssue;
import org.acme.projectjobschedule.service.ProjectJobScheduleIssues.DuplicateAllocationIdIssue;
import org.acme.projectjobschedule.service.ProjectJobScheduleIssues.DuplicateJobIdIssue;
import org.acme.projectjobschedule.service.ProjectJobScheduleIssues.DuplicateResourceIdIssue;
import org.acme.projectjobschedule.service.ProjectJobScheduleIssues.JobIdMissingIssue;
import org.acme.projectjobschedule.service.ProjectJobScheduleIssues.NonExistingExecutionModeReferenceIssue;
import org.acme.projectjobschedule.service.ProjectJobScheduleIssues.NonExistingJobReferenceIssue;
import org.acme.projectjobschedule.service.ProjectJobScheduleIssues.ResourceIdMissingIssue;

@ApplicationScoped
public class ProjectJobScheduleValidator
        implements ModelValidator<ProjectJobScheduleInput, ProjectJobScheduleConfigOverrides> {

    @Override
    public void validate(ValidationBuilder validationBuilder, ProjectJobScheduleInput modelInput,
            ModelConfig<ProjectJobScheduleConfigOverrides> modelConfig) {
        Set<String> jobIds = validateJobs(validationBuilder, modelInput.jobs());
        validateResources(validationBuilder, modelInput.resources());
        Set<String> executionModeIds = collectExecutionModeIds(modelInput.executionModes());
        validateAllocations(validationBuilder, modelInput.allocations(), jobIds, executionModeIds);
    }

    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private Set<String> validateJobs(ValidationBuilder validationBuilder, List<JobDTO> jobs) {
        Set<String> jobIds = new HashSet<>();
        for (JobDTO job : jobs) {
            if (job.id() == null || job.id().isBlank()) {
                validationBuilder.addIssue(new JobIdMissingIssue());
            } else if (!jobIds.add(job.id())) {
                validationBuilder.addIssue(new DuplicateJobIdIssue(new JobIdDetail(job.id())));
            }
        }
        return jobIds;
    }

    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private void validateResources(ValidationBuilder validationBuilder, List<ResourceDTO> resources) {
        Set<String> resourceIds = new HashSet<>();
        for (ResourceDTO resource : resources) {
            if (resource.id() == null || resource.id().isBlank()) {
                validationBuilder.addIssue(new ResourceIdMissingIssue());
            } else if (!resourceIds.add(resource.id())) {
                validationBuilder.addIssue(new DuplicateResourceIdIssue(new ResourceIdDetail(resource.id())));
            }
        }
    }

    private Set<String> collectExecutionModeIds(List<ExecutionModeDTO> executionModes) {
        Set<String> executionModeIds = new HashSet<>();
        for (ExecutionModeDTO executionMode : executionModes) {
            executionModeIds.add(executionMode.id());
        }
        return executionModeIds;
    }

    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private void validateAllocations(ValidationBuilder validationBuilder, List<AllocationDTO> allocations,
            Set<String> jobIds, Set<String> executionModeIds) {
        Set<String> allocationIds = new HashSet<>();
        for (AllocationDTO allocation : allocations) {
            if (allocation.id() == null || allocation.id().isBlank()) {
                validationBuilder.addIssue(new AllocationIdMissingIssue());
            } else if (!allocationIds.add(allocation.id())) {
                validationBuilder.addIssue(new DuplicateAllocationIdIssue(new AllocationIdDetail(allocation.id())));
            }
            if (!allocation.jobId().isBlank() && !jobIds.contains(allocation.jobId())) {
                validationBuilder.addIssue(new NonExistingJobReferenceIssue(new AllocationIdDetail(allocation.id())));
            }
            if (allocation.executionModeId() != null && !executionModeIds.contains(allocation.executionModeId())) {
                validationBuilder.addIssue(
                        new NonExistingExecutionModeReferenceIssue(new AllocationIdDetail(allocation.id())));
            }
        }
    }
}
