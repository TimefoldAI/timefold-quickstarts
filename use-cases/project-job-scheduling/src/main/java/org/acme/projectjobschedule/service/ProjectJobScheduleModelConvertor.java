package org.acme.projectjobschedule.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.LongFunction;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;

import ai.timefold.solver.core.api.domain.solution.ConstraintWeightOverrides;
import ai.timefold.solver.core.api.score.HardMediumSoftScore;
import ai.timefold.solver.service.definition.api.ModelConvertor;
import ai.timefold.solver.service.definition.api.domain.ModelConfig;

import org.acme.projectjobschedule.domain.Allocation;
import org.acme.projectjobschedule.domain.ExecutionMode;
import org.acme.projectjobschedule.domain.Job;
import org.acme.projectjobschedule.domain.JobType;
import org.acme.projectjobschedule.domain.Project;
import org.acme.projectjobschedule.domain.ProjectJobSchedule;
import org.acme.projectjobschedule.domain.ResourceRequirement;
import org.acme.projectjobschedule.domain.resource.GlobalResource;
import org.acme.projectjobschedule.domain.resource.LocalResource;
import org.acme.projectjobschedule.domain.resource.Resource;
import org.acme.projectjobschedule.dto.AllocationDTO;
import org.acme.projectjobschedule.dto.ExecutionModeDTO;
import org.acme.projectjobschedule.dto.JobDTO;
import org.acme.projectjobschedule.dto.ProjectDTO;
import org.acme.projectjobschedule.dto.ProjectJobScheduleConfigOverrides;
import org.acme.projectjobschedule.dto.ProjectJobScheduleInput;
import org.acme.projectjobschedule.dto.ProjectJobScheduleOutput;
import org.acme.projectjobschedule.dto.ResourceDTO;
import org.acme.projectjobschedule.dto.ResourceRequirementDTO;
import org.acme.projectjobschedule.solver.ProjectJobScheduleConstraintProperties;

@ApplicationScoped
public class ProjectJobScheduleModelConvertor implements
        ModelConvertor<HardMediumSoftScore, ProjectJobScheduleInput, ProjectJobScheduleConfigOverrides, ProjectJobSchedule, ProjectJobScheduleOutput> {

    private static final String LOCAL_RESOURCE_TYPE = "LOCAL";

    @Override
    public ProjectJobScheduleInput applyOutputToInput(ProjectJobScheduleInput modelInput,
            ProjectJobScheduleOutput modelOutput) {
        Map<String, AllocationDTO> outputAllocations = modelOutput.allocations().stream()
                .collect(Collectors.toMap(AllocationDTO::id, allocation -> allocation));
        List<AllocationDTO> updatedAllocations = modelInput.allocations().stream()
                .map(allocation -> {
                    AllocationDTO solved = outputAllocations.get(allocation.id());
                    if (solved == null) {
                        return allocation;
                    }
                    return allocation.withExecutionModeId(solved.executionModeId()).withDelay(solved.delay())
                            .withStartDate(solved.startDate()).withEndDate(solved.endDate());
                })
                .collect(Collectors.toList());
        return modelInput.withAllocations(updatedAllocations);
    }

    @Override
    public ProjectJobSchedule toSolverModel(ProjectJobScheduleInput modelInput,
            ModelConfig<ProjectJobScheduleConfigOverrides> modelConfig,
            Optional<ProjectJobScheduleOutput> lastModelOutput) {
        Map<String, Project> projectMap = new HashMap<>();
        List<Project> projects = new ArrayList<>();
        for (ProjectDTO dto : modelInput.projects()) {
            Project project = toProject(dto);
            projectMap.put(project.getId(), project);
            projects.add(project);
        }

        Map<String, Resource> resourceMap = new HashMap<>();
        List<Resource> resources = new ArrayList<>();
        for (ResourceDTO dto : modelInput.resources()) {
            Resource resource = toResource(dto, projectMap);
            resourceMap.put(resource.getId(), resource);
            resources.add(resource);
        }

        Map<String, Job> jobMap = new HashMap<>();
        List<Job> jobs = new ArrayList<>();
        for (JobDTO dto : modelInput.jobs()) {
            Job job = toJob(dto, projectMap);
            jobMap.put(job.getId(), job);
            jobs.add(job);
        }
        // Wire successor jobs now that all jobs exist.
        for (JobDTO dto : modelInput.jobs()) {
            Job job = jobMap.get(dto.id());
            job.setSuccessorJobs(dto.successorJobIds().stream().map(jobMap::get).toList());
        }

        Map<String, ExecutionMode> executionModeMap = new HashMap<>();
        List<ExecutionMode> executionModes = new ArrayList<>();
        for (ExecutionModeDTO dto : modelInput.executionModes()) {
            ExecutionMode executionMode = toExecutionMode(dto, jobMap);
            executionModeMap.put(executionMode.getId(), executionMode);
            executionModes.add(executionMode);
        }
        Map<String, List<ExecutionMode>> executionModesByJob = executionModes.stream()
                .collect(Collectors.groupingBy(executionMode -> executionMode.getJob().getId()));
        for (Job job : jobs) {
            job.setExecutionModes(executionModesByJob.getOrDefault(job.getId(), List.of()));
        }

        Map<String, List<ResourceRequirement>> requirementsByExecutionMode = new HashMap<>();
        List<ResourceRequirement> resourceRequirements = new ArrayList<>();
        for (ResourceRequirementDTO dto : modelInput.resourceRequirements()) {
            ResourceRequirement requirement = toResourceRequirement(dto, executionModeMap, resourceMap);
            resourceRequirements.add(requirement);
        }
        requirementsByExecutionMode.putAll(resourceRequirements.stream()
                .collect(Collectors.groupingBy(requirement -> requirement.getExecutionMode().getId())));
        for (ExecutionMode executionMode : executionModes) {
            executionMode
                    .setResourceRequirements(requirementsByExecutionMode.getOrDefault(executionMode.getId(), List.of()));
        }

        Map<String, Allocation> allocationMap = new HashMap<>();
        List<Allocation> allocations = new ArrayList<>();
        for (AllocationDTO dto : modelInput.allocations()) {
            Allocation allocation = toAllocation(dto, jobMap);
            allocationMap.put(allocation.getId(), allocation);
            allocations.add(allocation);
        }
        for (AllocationDTO dto : modelInput.allocations()) {
            wireAllocation(dto, allocationMap, executionModeMap);
        }

        ProjectJobSchedule schedule = new ProjectJobSchedule(projects, resources, jobs, executionModes,
                resourceRequirements, allocations);
        applyConstraintWeightOverrides(schedule, modelConfig);
        applyLastOutput(allocationMap, executionModeMap, lastModelOutput);
        return schedule;
    }

    private static Project toProject(ProjectDTO dto) {
        return new Project(dto.id(), dto.releaseDate(), dto.criticalPathDuration());
    }

    private static Resource toResource(ResourceDTO dto, Map<String, Project> projectMap) {
        if (LOCAL_RESOURCE_TYPE.equalsIgnoreCase(dto.resourceType()) || dto.projectId() != null) {
            Project project = dto.projectId() == null ? null : projectMap.get(dto.projectId());
            return new LocalResource(dto.id(), project, dto.capacity(), dto.renewable());
        }
        return new GlobalResource(dto.id(), dto.capacity());
    }

    private static Job toJob(JobDTO dto, Map<String, Project> projectMap) {
        return new Job(dto.id(), projectMap.get(dto.projectId()), JobType.valueOf(dto.jobType()));
    }

    private static ExecutionMode toExecutionMode(ExecutionModeDTO dto, Map<String, Job> jobMap) {
        return new ExecutionMode(dto.id(), jobMap.get(dto.jobId()), dto.duration());
    }

    private static ResourceRequirement toResourceRequirement(ResourceRequirementDTO dto,
            Map<String, ExecutionMode> executionModeMap, Map<String, Resource> resourceMap) {
        return new ResourceRequirement(dto.id(), executionModeMap.get(dto.executionModeId()),
                resourceMap.get(dto.resourceId()), dto.requirement());
    }

    private static Allocation toAllocation(AllocationDTO dto, Map<String, Job> jobMap) {
        return new Allocation(dto.id(), jobMap.get(dto.jobId()));
    }

    private static void wireAllocation(AllocationDTO dto, Map<String, Allocation> allocationMap,
            Map<String, ExecutionMode> executionModeMap) {
        Allocation allocation = allocationMap.get(dto.id());
        if (dto.sourceAllocationId() != null) {
            allocation.setSourceAllocation(allocationMap.get(dto.sourceAllocationId()));
        }
        if (dto.sinkAllocationId() != null) {
            allocation.setSinkAllocation(allocationMap.get(dto.sinkAllocationId()));
        }
        allocation.setPredecessorAllocations(dto.predecessorAllocationIds().stream().map(allocationMap::get).toList());
        allocation.setSuccessorAllocations(dto.successorAllocationIds().stream().map(allocationMap::get).toList());
        if (dto.executionModeId() != null) {
            allocation.setExecutionMode(executionModeMap.get(dto.executionModeId()));
        }
        if (dto.delay() != null) {
            allocation.setDelay(dto.delay());
        }
    }

    private static void applyConstraintWeightOverrides(ProjectJobSchedule schedule,
            ModelConfig<ProjectJobScheduleConfigOverrides> modelConfig) {
        if (modelConfig == null || modelConfig.overrides() == null) {
            return;
        }
        ProjectJobScheduleConfigOverrides overrides = modelConfig.overrides();
        // Only apply weights that are actually set (non-null) in the merged overrides. A null weight means the
        // input did not override it, so the configuration profile value (or the constraint's default) is kept.
        Map<String, HardMediumSoftScore> weights = new HashMap<>();
        putIfPresent(weights, ProjectJobScheduleConstraintProperties.TOTAL_PROJECT_DELAY,
                overrides.totalProjectDelayWeight(), HardMediumSoftScore::ofMedium);
        putIfPresent(weights, ProjectJobScheduleConstraintProperties.TOTAL_MAKESPAN,
                overrides.totalMakespanWeight(), HardMediumSoftScore::ofSoft);
        if (!weights.isEmpty()) {
            schedule.setConstraintWeightOverrides(ConstraintWeightOverrides.of(weights));
        }
    }

    private static void putIfPresent(Map<String, HardMediumSoftScore> weights, String constraintName, Long weight,
            LongFunction<HardMediumSoftScore> scoreFactory) {
        if (weight != null) {
            weights.put(constraintName, scoreFactory.apply(weight));
        }
    }

    private static void applyLastOutput(Map<String, Allocation> allocationMap,
            Map<String, ExecutionMode> executionModeMap, Optional<ProjectJobScheduleOutput> lastModelOutput) {
        if (lastModelOutput.isEmpty()) {
            return;
        }
        for (AllocationDTO solved : lastModelOutput.get().allocations()) {
            Allocation allocation = allocationMap.get(solved.id());
            if (allocation == null) {
                continue;
            }
            if (solved.executionModeId() != null) {
                allocation.setExecutionMode(executionModeMap.get(solved.executionModeId()));
            }
            if (solved.delay() != null) {
                allocation.setDelay(solved.delay());
            }
        }
    }

    @Override
    public ProjectJobScheduleOutput toModelOutput(ProjectJobSchedule solverModel) {
        List<ProjectDTO> projects = solverModel.getProjects().stream().map(this::toDTO).collect(Collectors.toList());
        List<ResourceDTO> resources = solverModel.getResources().stream().map(this::toDTO).collect(Collectors.toList());
        List<JobDTO> jobs = solverModel.getJobs().stream().map(this::toDTO).collect(Collectors.toList());
        List<ExecutionModeDTO> executionModes =
                solverModel.getExecutionModes().stream().map(this::toDTO).collect(Collectors.toList());
        List<ResourceRequirementDTO> resourceRequirements =
                solverModel.getResourceRequirements().stream().map(this::toDTO).collect(Collectors.toList());
        List<AllocationDTO> allocations =
                solverModel.getAllocations().stream().map(this::toDTO).collect(Collectors.toList());
        String score = solverModel.getScore() == null ? "" : solverModel.getScore().toString();
        return new ProjectJobScheduleOutput(projects, resources, jobs, executionModes, resourceRequirements, allocations,
                score);
    }

    private ProjectDTO toDTO(Project project) {
        return new ProjectDTO(project.getId(), project.getReleaseDate(), project.getCriticalPathDuration());
    }

    private ResourceDTO toDTO(Resource resource) {
        if (resource instanceof LocalResource local) {
            String projectId = local.getProject() == null ? "" : local.getProject().getId();
            return new ResourceDTO(local.getId(), LOCAL_RESOURCE_TYPE, local.getCapacity(), local.isRenewable(),
                    projectId);
        }
        return new ResourceDTO(resource.getId(), "GLOBAL", resource.getCapacity(), resource.isRenewable(), "");
    }

    private JobDTO toDTO(Job job) {
        List<String> successorJobIds = job.getSuccessorJobs() == null ? List.of()
                : job.getSuccessorJobs().stream().map(Job::getId).toList();
        String projectId = job.getProject() == null ? "" : job.getProject().getId();
        return new JobDTO(job.getId(), projectId, job.getJobType().name(), successorJobIds);
    }

    private ExecutionModeDTO toDTO(ExecutionMode executionMode) {
        String jobId = executionMode.getJob() == null ? "" : executionMode.getJob().getId();
        return new ExecutionModeDTO(executionMode.getId(), jobId, executionMode.getDuration());
    }

    private ResourceRequirementDTO toDTO(ResourceRequirement requirement) {
        String executionModeId = requirement.getExecutionMode() == null ? "" : requirement.getExecutionMode().getId();
        String resourceId = requirement.getResource() == null ? "" : requirement.getResource().getId();
        return new ResourceRequirementDTO(requirement.getId(), executionModeId, resourceId, requirement.getRequirement());
    }

    private AllocationDTO toDTO(Allocation allocation) {
        String sourceId = allocation.getSourceAllocation() == null ? "" : allocation.getSourceAllocation().getId();
        String sinkId = allocation.getSinkAllocation() == null ? "" : allocation.getSinkAllocation().getId();
        List<String> predecessorIds = allocation.getPredecessorAllocations() == null ? List.of()
                : allocation.getPredecessorAllocations().stream().map(Allocation::getId).toList();
        List<String> successorIds = allocation.getSuccessorAllocations() == null ? List.of()
                : allocation.getSuccessorAllocations().stream().map(Allocation::getId).toList();
        String executionModeId =
                allocation.getExecutionMode() == null ? "" : allocation.getExecutionMode().getId();
        return new AllocationDTO(allocation.getId(), allocation.getJob() == null ? "" : allocation.getJob().getId(),
                sourceId, sinkId, predecessorIds, successorIds, executionModeId, allocation.getDelay(),
                allocation.getStartDate(), allocation.getEndDate());
    }
}
