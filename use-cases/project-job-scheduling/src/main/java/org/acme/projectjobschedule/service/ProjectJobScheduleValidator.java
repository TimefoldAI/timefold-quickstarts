package org.acme.projectjobschedule.service;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;

import ai.timefold.solver.service.definition.api.domain.ModelConfig;
import ai.timefold.solver.service.definition.api.validation.ModelValidator;
import ai.timefold.solver.service.definition.api.validation.ValidationBuilder;

import org.acme.projectjobschedule.domain.JobType;
import org.acme.projectjobschedule.dto.input.AllocationInputDTO;
import org.acme.projectjobschedule.dto.input.ExecutionModeInputDTO;
import org.acme.projectjobschedule.dto.input.JobInputDTO;
import org.acme.projectjobschedule.dto.input.ProjectInputDTO;
import org.acme.projectjobschedule.dto.input.ProjectJobScheduleConfigOverrides;
import org.acme.projectjobschedule.dto.input.ProjectJobScheduleInput;
import org.acme.projectjobschedule.dto.input.ResourceInputDTO;
import org.acme.projectjobschedule.dto.input.ResourceRequirementInputDTO;
import org.acme.projectjobschedule.service.validation.ProjectJobScheduleIssue.CrossProjectResourceRequirementIssue;
import org.acme.projectjobschedule.service.validation.ProjectJobScheduleIssue.CyclicJobPrecedenceIssue;
import org.acme.projectjobschedule.service.validation.ProjectJobScheduleIssue.DuplicateAllocationIdIssue;
import org.acme.projectjobschedule.service.validation.ProjectJobScheduleIssue.DuplicateExecutionModeIdIssue;
import org.acme.projectjobschedule.service.validation.ProjectJobScheduleIssue.DuplicateJobIdIssue;
import org.acme.projectjobschedule.service.validation.ProjectJobScheduleIssue.DuplicateProjectIdIssue;
import org.acme.projectjobschedule.service.validation.ProjectJobScheduleIssue.DuplicateResourceIdIssue;
import org.acme.projectjobschedule.service.validation.ProjectJobScheduleIssue.DuplicateResourceRequirementIssue;
import org.acme.projectjobschedule.service.validation.ProjectJobScheduleIssue.InvalidAllocationCountForJobIssue;
import org.acme.projectjobschedule.service.validation.ProjectJobScheduleIssue.InvalidSourceOrSinkExecutionModeIssue;
import org.acme.projectjobschedule.service.validation.ProjectJobScheduleIssue.NonExistingExecutionModeReferenceIssue;
import org.acme.projectjobschedule.service.validation.ProjectJobScheduleIssue.NonExistingJobReferenceIssue;
import org.acme.projectjobschedule.service.validation.ProjectJobScheduleIssue.NonExistingProjectReferenceIssue;
import org.acme.projectjobschedule.service.validation.ProjectJobScheduleIssue.NonExistingResourceReferenceIssue;
import org.acme.projectjobschedule.service.validation.ProjectJobScheduleIssue.PinnedAllocationWithoutAssignmentIssue;
import org.acme.projectjobschedule.service.validation.ProjectJobScheduleIssue.ProjectWithoutSourceOrSinkJobIssue;

@ApplicationScoped
public class ProjectJobScheduleValidator
        implements ModelValidator<ProjectJobScheduleInput, ProjectJobScheduleConfigOverrides> {

    @Override
    public void validate(ValidationBuilder validationBuilder, ProjectJobScheduleInput modelInput,
            ModelConfig<ProjectJobScheduleConfigOverrides> modelConfig) {
        // OpenAPI spec (Bean Validation) compliance is enforced by the Service module at the REST layer,
        // before this validator ever runs; only domain-specific checks (duplicate ids, dangling references
        // and model invariants) belong here.
        List<JobInputDTO> jobs = orEmpty(modelInput.jobs());
        Set<String> projectIds = validateProjects(validationBuilder, orEmpty(modelInput.projects()));
        Map<String, String> projectIdByResourceId =
                validateResources(validationBuilder, orEmpty(modelInput.resources()), projectIds);
        Map<String, Set<String>> executionModeIdsByJobId =
                validateJobs(validationBuilder, jobs, projectIds, projectIdByResourceId);
        validateProjectBrackets(validationBuilder, jobs, projectIds);
        validateAllocations(validationBuilder, orEmpty(modelInput.allocations()), executionModeIdsByJobId,
                standardJobIds(jobs));
        validateAcyclicPrecedence(validationBuilder, jobs);
    }

    private static <T> List<T> orEmpty(List<T> list) {
        return list == null ? List.of() : list;
    }

    private static Set<String> validateProjects(ValidationBuilder validationBuilder, List<ProjectInputDTO> projects) {
        Set<String> projectIds = new LinkedHashSet<>();
        for (ProjectInputDTO project : projects) {
            if (hasId(project.id()) && !projectIds.add(project.id())) {
                validationBuilder.addIssue(new DuplicateProjectIdIssue(project.id()));
            }
        }
        return projectIds;
    }

    /**
     * @return the id of the project owning each resource, or null for a resource every project draws from,
     *         so a resource requirement can be checked against the project of the job it belongs to
     */
    private static Map<String, String> validateResources(ValidationBuilder validationBuilder,
            List<ResourceInputDTO> resources, Set<String> projectIds) {
        Map<String, String> projectIdByResourceId = new HashMap<>();
        for (ResourceInputDTO resource : resources) {
            if (hasId(resource.id())) {
                if (projectIdByResourceId.containsKey(resource.id())) {
                    validationBuilder.addIssue(new DuplicateResourceIdIssue(resource.id()));
                } else {
                    // A dangling project is reported once below, rather than again for every requirement
                    // of the resource, so such a resource is treated as available to every project.
                    projectIdByResourceId.put(resource.id(),
                            projectIds.contains(resource.projectId()) ? resource.projectId() : null);
                }
            }
            // A resource without a project is global, so only a project that is actually named has to exist.
            if (resource.projectId() != null && !projectIds.contains(resource.projectId())) {
                validationBuilder.addIssue(new NonExistingProjectReferenceIssue(resource.projectId()));
            }
        }
        return projectIdByResourceId;
    }

    /**
     * @return the ids of the execution modes of each job, so an allocation can be checked against the
     *         execution modes of the job it schedules
     */
    private static Map<String, Set<String>> validateJobs(ValidationBuilder validationBuilder, List<JobInputDTO> jobs,
            Set<String> projectIds, Map<String, String> projectIdByResourceId) {
        Set<String> jobIds = new LinkedHashSet<>();
        Set<String> executionModeIds = new HashSet<>();
        Map<String, Set<String>> executionModeIdsByJobId = new LinkedHashMap<>();
        for (JobInputDTO job : jobs) {
            if (hasId(job.id()) && !jobIds.add(job.id())) {
                validationBuilder.addIssue(new DuplicateJobIdIssue(job.id()));
            }
            if (!projectIds.contains(job.projectId())) {
                validationBuilder.addIssue(new NonExistingProjectReferenceIssue(job.projectId()));
            }
            Set<String> jobExecutionModeIds = new LinkedHashSet<>();
            for (ExecutionModeInputDTO executionMode : orEmpty(job.executionModes())) {
                if (hasId(executionMode.id()) && !executionModeIds.add(executionMode.id())) {
                    validationBuilder.addIssue(new DuplicateExecutionModeIdIssue(executionMode.id()));
                }
                jobExecutionModeIds.add(executionMode.id());
                validateResourceRequirements(validationBuilder, job, executionMode, projectIdByResourceId);
            }
            validateSourceOrSinkExecutionMode(validationBuilder, job);
            executionModeIdsByJobId.put(job.id(), jobExecutionModeIds);
        }
        for (JobInputDTO job : jobs) {
            for (String successorJobId : job.successorJobIds()) {
                if (!jobIds.contains(successorJobId)) {
                    validationBuilder.addIssue(new NonExistingJobReferenceIssue(successorJobId));
                }
            }
        }
        return executionModeIdsByJobId;
    }

    private static void validateResourceRequirements(ValidationBuilder validationBuilder, JobInputDTO job,
            ExecutionModeInputDTO executionMode, Map<String, String> projectIdByResourceId) {
        Set<String> requiredResourceIds = new LinkedHashSet<>();
        for (ResourceRequirementInputDTO requirement : executionMode.resourceRequirements()) {
            if (!projectIdByResourceId.containsKey(requirement.resourceId())) {
                validationBuilder.addIssue(new NonExistingResourceReferenceIssue(requirement.resourceId()));
            } else if (!isAvailableToProject(projectIdByResourceId.get(requirement.resourceId()), job.projectId())) {
                // The constraints only cap a resource's total consumption, so nothing else stops one
                // project from eating into the capacity another project owns.
                validationBuilder.addIssue(
                        new CrossProjectResourceRequirementIssue(executionMode.id(), requirement.resourceId()));
            } else if (!requiredResourceIds.add(requirement.resourceId())) {
                // Two requirements for the same resource cannot be told apart, and would double the
                // amount consumed without the dataset saying so.
                validationBuilder
                        .addIssue(new DuplicateResourceRequirementIssue(executionMode.id(), requirement.resourceId()));
            }
        }
    }

    private static boolean isAvailableToProject(String resourceProjectId, String jobProjectId) {
        return resourceProjectId == null || resourceProjectId.equals(jobProjectId);
    }

    /**
     * A SOURCE or SINK job only marks where its project starts or ends, so the model schedules it in its
     * first execution mode. Anything other than a single zero-duration mode without requirements would
     * shift the project's dates or consume capacity, and any other mode would be silently ignored.
     */
    private static void validateSourceOrSinkExecutionMode(ValidationBuilder validationBuilder, JobInputDTO job) {
        if (job.jobType() != JobType.SOURCE && job.jobType() != JobType.SINK) {
            return;
        }
        List<ExecutionModeInputDTO> executionModes = orEmpty(job.executionModes());
        if (executionModes.size() != 1 || !Integer.valueOf(0).equals(executionModes.getFirst().duration())
                || !executionModes.getFirst().resourceRequirements().isEmpty()) {
            validationBuilder.addIssue(new InvalidSourceOrSinkExecutionModeIssue(job.id()));
        }
    }

    /**
     * Every project needs exactly one source and one sink job: the sink's end date is the project's
     * completion date, and the source is what anchors the first real job to the project's release date.
     */
    private static void validateProjectBrackets(ValidationBuilder validationBuilder, List<JobInputDTO> jobs,
            Set<String> projectIds) {
        Map<String, Integer> sourceCountByProjectId = new HashMap<>();
        Map<String, Integer> sinkCountByProjectId = new HashMap<>();
        for (JobInputDTO job : jobs) {
            if (job.jobType() == JobType.SOURCE) {
                sourceCountByProjectId.merge(job.projectId(), 1, Integer::sum);
            } else if (job.jobType() == JobType.SINK) {
                sinkCountByProjectId.merge(job.projectId(), 1, Integer::sum);
            }
        }
        for (String projectId : projectIds) {
            if (sourceCountByProjectId.getOrDefault(projectId, 0) != 1
                    || sinkCountByProjectId.getOrDefault(projectId, 0) != 1) {
                validationBuilder.addIssue(new ProjectWithoutSourceOrSinkJobIssue(projectId));
            }
        }
    }

    /**
     * The model schedules SOURCE and SINK jobs itself and ignores whatever their allocation says, so only
     * the allocations of STANDARD jobs have an assignment worth checking.
     */
    private static Set<String> standardJobIds(List<JobInputDTO> jobs) {
        Set<String> standardJobIds = new HashSet<>();
        for (JobInputDTO job : jobs) {
            if (job.jobType() == JobType.STANDARD) {
                standardJobIds.add(job.id());
            }
        }
        return standardJobIds;
    }

    private static void validateAllocations(ValidationBuilder validationBuilder,
            List<AllocationInputDTO> allocations, Map<String, Set<String>> executionModeIdsByJobId,
            Set<String> standardJobIds) {
        Set<String> allocationIds = new LinkedHashSet<>();
        Map<String, Integer> allocationCountByJobId = new HashMap<>();
        for (AllocationInputDTO allocation : allocations) {
            if (hasId(allocation.id()) && !allocationIds.add(allocation.id())) {
                validationBuilder.addIssue(new DuplicateAllocationIdIssue(allocation.id()));
            }
            Set<String> jobExecutionModeIds = executionModeIdsByJobId.get(allocation.jobId());
            if (jobExecutionModeIds == null) {
                validationBuilder.addIssue(new NonExistingJobReferenceIssue(allocation.jobId()));
            } else {
                allocationCountByJobId.merge(allocation.jobId(), 1, Integer::sum);
                if (allocation.executionModeId() != null
                        && !jobExecutionModeIds.contains(allocation.executionModeId())) {
                    validationBuilder.addIssue(new NonExistingExecutionModeReferenceIssue(allocation.id()));
                }
            }
            if (Boolean.TRUE.equals(allocation.pinned()) && standardJobIds.contains(allocation.jobId())
                    && (allocation.executionModeId() == null || allocation.delay() == null)) {
                validationBuilder.addIssue(new PinnedAllocationWithoutAssignmentIssue(allocation.id()));
            }
        }
        for (String jobId : executionModeIdsByJobId.keySet()) {
            int allocationCount = allocationCountByJobId.getOrDefault(jobId, 0);
            if (allocationCount != 1) {
                validationBuilder.addIssue(new InvalidAllocationCountForJobIssue(jobId, allocationCount));
            }
        }
    }

    /**
     * A cycle in the precedence relations makes the start date of every job on it depend on itself, so
     * the schedule can never be built. Only the first cycle found is reported: the jobs on a second
     * one are usually the same typo, and one actionable job id is enough to go and fix it.
     */
    private static void validateAcyclicPrecedence(ValidationBuilder validationBuilder, List<JobInputDTO> jobs) {
        Map<String, List<String>> successorJobIdsByJobId = new LinkedHashMap<>();
        for (JobInputDTO job : jobs) {
            successorJobIdsByJobId.put(job.id(), job.successorJobIds());
        }
        Set<String> visited = new HashSet<>();
        for (String jobId : successorJobIdsByJobId.keySet()) {
            Optional<String> jobIdOnCycle = findJobOnCycle(jobId, successorJobIdsByJobId, visited);
            if (jobIdOnCycle.isPresent()) {
                validationBuilder.addIssue(new CyclicJobPrecedenceIssue(jobIdOnCycle.get()));
                return;
            }
        }
    }

    /**
     * Iterative depth-first search: {@code onPath} holds the jobs on the path currently being walked,
     * so reaching one of those again closes a cycle. Iterative rather than recursive, since a long
     * chain of jobs would otherwise overflow the stack.
     */
    private static Optional<String> findJobOnCycle(String startJobId, Map<String, List<String>> successorJobIdsByJobId,
            Set<String> visited) {
        if (visited.contains(startJobId)) {
            return Optional.empty();
        }
        Set<String> onPath = new LinkedHashSet<>();
        Deque<String> pending = new ArrayDeque<>();
        pending.push(startJobId);
        while (!pending.isEmpty()) {
            String jobId = pending.peek();
            if (visited.contains(jobId)) {
                // Already walked through another path, so it cannot be on a cycle.
                pending.pop();
            } else if (onPath.add(jobId)) {
                for (String successorJobId : successorJobIdsByJobId.getOrDefault(jobId, List.of())) {
                    if (onPath.contains(successorJobId)) {
                        return Optional.of(successorJobId);
                    }
                    if (!visited.contains(successorJobId) && successorJobIdsByJobId.containsKey(successorJobId)) {
                        pending.push(successorJobId);
                    }
                }
            } else {
                // Every successor of this job has been walked, so it is not on a cycle.
                pending.pop();
                onPath.remove(jobId);
                visited.add(jobId);
            }
        }
        return Optional.empty();
    }

    private static boolean hasId(String id) {
        return id != null && !id.isBlank();
    }
}
