package org.acme.projectjobschedule.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;

import ai.timefold.solver.core.api.domain.solution.ConstraintWeightOverrides;
import ai.timefold.solver.core.api.score.HardMediumSoftScore;
import ai.timefold.solver.core.api.solver.SolutionManager;
import ai.timefold.solver.service.definition.api.ModelConvertor;
import ai.timefold.solver.service.definition.api.domain.ModelConfig;

import org.acme.projectjobschedule.domain.Allocation;
import org.acme.projectjobschedule.domain.ExecutionMode;
import org.acme.projectjobschedule.domain.Job;
import org.acme.projectjobschedule.domain.JobType;
import org.acme.projectjobschedule.domain.Project;
import org.acme.projectjobschedule.domain.ProjectJobSchedule;
import org.acme.projectjobschedule.domain.ProjectJobScheduleConstraintProperties;
import org.acme.projectjobschedule.domain.Resource;
import org.acme.projectjobschedule.domain.ResourceRequirement;
import org.acme.projectjobschedule.dto.input.AllocationInputDTO;
import org.acme.projectjobschedule.dto.input.ExecutionModeInputDTO;
import org.acme.projectjobschedule.dto.input.JobInputDTO;
import org.acme.projectjobschedule.dto.input.ProjectInputDTO;
import org.acme.projectjobschedule.dto.input.ProjectJobScheduleConfigOverrides;
import org.acme.projectjobschedule.dto.input.ProjectJobScheduleInput;
import org.acme.projectjobschedule.dto.input.ResourceInputDTO;
import org.acme.projectjobschedule.dto.input.ResourceRequirementInputDTO;
import org.acme.projectjobschedule.dto.output.AllocationOutputDTO;
import org.acme.projectjobschedule.dto.output.ProjectJobScheduleOutput;

@ApplicationScoped
public class ProjectJobScheduleModelConvertor
        implements
        ModelConvertor<HardMediumSoftScore, ProjectJobScheduleInput, ProjectJobScheduleConfigOverrides, ProjectJobSchedule, ProjectJobScheduleOutput> {

    @Override
    public ProjectJobScheduleInput applyOutputToInput(ProjectJobScheduleInput modelInput,
            ProjectJobScheduleOutput modelOutput) {
        Map<String, AllocationOutputDTO> outputAllocations = new LinkedHashMap<>();
        for (AllocationOutputDTO allocation : modelOutput.allocations()) {
            outputAllocations.put(allocation.id(), allocation);
        }
        List<AllocationInputDTO> updatedAllocations = modelInput.allocations().stream()
                .map(allocation -> {
                    AllocationOutputDTO solved = outputAllocations.get(allocation.id());
                    return solved == null ? allocation
                            : allocation.withAssignment(solved.executionModeId(), solved.delay());
                })
                .toList();
        return modelInput.withAllocations(updatedAllocations);
    }

    @Override
    public ProjectJobSchedule toSolverModel(ProjectJobScheduleInput modelInput,
            ModelConfig<ProjectJobScheduleConfigOverrides> modelConfig,
            Optional<ProjectJobScheduleOutput> lastModelOutput) {
        Map<String, Project> projectMap = new LinkedHashMap<>();
        for (ProjectInputDTO dto : modelInput.projects()) {
            projectMap.put(dto.id(), new Project(dto.id(), dto.releaseDate(), dto.criticalPathDuration()));
        }

        Map<String, Resource> resourceMap = new LinkedHashMap<>();
        for (ResourceInputDTO dto : modelInput.resources()) {
            Project project = dto.projectId() == null ? null : require(projectMap, dto.projectId(), "project");
            resourceMap.put(dto.id(), new Resource(dto.id(), dto.capacity(), project, dto.renewable()));
        }

        Map<String, ExecutionMode> executionModeMap = new LinkedHashMap<>();
        List<ResourceRequirement> resourceRequirements = new ArrayList<>();
        Map<String, Job> jobMap = new LinkedHashMap<>();
        for (JobInputDTO dto : modelInput.jobs()) {
            List<ExecutionMode> executionModes = new ArrayList<>(dto.executionModes().size());
            for (ExecutionModeInputDTO executionModeDto : dto.executionModes()) {
                ExecutionMode executionMode = new ExecutionMode(executionModeDto.id(), executionModeDto.duration());
                executionModeMap.put(executionMode.id(), executionMode);
                executionModes.add(executionMode);
                for (ResourceRequirementInputDTO requirementDto : executionModeDto.resourceRequirements()) {
                    resourceRequirements.add(toResourceRequirement(requirementDto, executionMode, resourceMap));
                }
            }
            jobMap.put(dto.id(), new Job(dto.id(), require(projectMap, dto.projectId(), "project"), dto.jobType(),
                    List.copyOf(executionModes)));
        }

        List<Allocation> allocations = modelInput.allocations().stream()
                .map(dto -> toAllocation(dto, jobMap, executionModeMap))
                .toList();
        linkAllocations(allocations, modelInput.jobs());

        ProjectJobSchedule schedule = new ProjectJobSchedule(List.copyOf(projectMap.values()),
                List.copyOf(resourceMap.values()), List.copyOf(jobMap.values()),
                List.copyOf(executionModeMap.values()), List.copyOf(resourceRequirements), allocations);
        applyLastOutput(allocations, executionModeMap, lastModelOutput);
        // The dates are declarative shadow variables, so they are only recomputed once solving starts;
        // seed them here so an unsolved schedule already reports meaningful dates and metrics.
        SolutionManager.updateShadowVariables(schedule);
        applyConstraintWeightOverrides(schedule, modelConfig);
        return schedule;
    }

    @Override
    public ProjectJobScheduleOutput toModelOutput(ProjectJobSchedule solverModel) {
        List<AllocationOutputDTO> allocations = solverModel.getAllocations().stream()
                .map(allocation -> new AllocationOutputDTO(allocation.getId(),
                        allocation.getExecutionMode() == null ? null : allocation.getExecutionMode().id(),
                        allocation.getDelay(), allocation.getStartDate(), allocation.getEndDate()))
                .toList();
        return new ProjectJobScheduleOutput(allocations);
    }

    private static ResourceRequirement toResourceRequirement(ResourceRequirementInputDTO dto,
            ExecutionMode executionMode, Map<String, Resource> resourceMap) {
        // A requirement is identified by the execution mode consuming it and the resource it consumes,
        // so that pair is its id: an input dataset never has to make one up.
        String id = "%d:%s%s".formatted(executionMode.id().length(), executionMode.id(), dto.resourceId());
        return new ResourceRequirement(id, executionMode, require(resourceMap, dto.resourceId(), "resource"),
                dto.requirement());
    }

    private static Allocation toAllocation(AllocationInputDTO dto, Map<String, Job> jobMap,
            Map<String, ExecutionMode> executionModeMap) {
        Job job = require(jobMap, dto.jobId(), "job");
        Allocation allocation = new Allocation(dto.id(), job);
        if (job.jobType() == JobType.STANDARD) {
            allocation.setExecutionMode(
                    dto.executionModeId() == null ? null : require(executionModeMap, dto.executionModeId(), "execution mode"));
            allocation.setDelay(dto.delay());
            allocation.setPinned(Boolean.TRUE.equals(dto.pinned()));
        } else {
            // A source or a sink job is not real work: it has a single, zero-duration execution mode and
            // never waits, which is why its allocation is pinned (see Allocation#setPinned(boolean)).
            allocation.setExecutionMode(job.executionModes().getFirst());
            allocation.setDelay(0);
        }
        return allocation;
    }

    /**
     * Wires up the precedence relations between the allocations from the precedence relations between
     * their jobs, which is what the date shadow variables walk.
     */
    private static void linkAllocations(List<Allocation> allocations, List<JobInputDTO> jobs) {
        Map<String, Allocation> allocationByJobId = new LinkedHashMap<>();
        for (Allocation allocation : allocations) {
            allocationByJobId.put(allocation.getJob().id(), allocation);
        }
        Map<String, List<Allocation>> predecessorsByJobId = new HashMap<>();
        Map<String, List<Allocation>> successorsByJobId = new HashMap<>();
        for (JobInputDTO job : jobs) {
            Allocation allocation = require(allocationByJobId, job.id(), "allocation of job");
            for (String successorJobId : job.successorJobIds()) {
                Allocation successor = require(allocationByJobId, successorJobId, "allocation of job");
                if (successor.equals(allocation)) {
                    continue; // A job is not its own predecessor.
                }
                successorsByJobId.computeIfAbsent(job.id(), id -> new ArrayList<>()).add(successor);
                predecessorsByJobId.computeIfAbsent(successorJobId, id -> new ArrayList<>()).add(allocation);
            }
        }
        Map<String, Allocation> sourceAllocationByProjectId = allocationsByProjectId(allocations, JobType.SOURCE);
        Map<String, Allocation> sinkAllocationByProjectId = allocationsByProjectId(allocations, JobType.SINK);
        for (Allocation allocation : allocations) {
            String projectId = allocation.getProject().id();
            allocation.setSourceAllocation(sourceAllocationByProjectId.get(projectId));
            allocation.setSinkAllocation(sinkAllocationByProjectId.get(projectId));
            allocation.setPredecessorAllocations(
                    List.copyOf(predecessorsByJobId.getOrDefault(allocation.getJob().id(), List.of())));
            allocation.setSuccessorAllocations(
                    List.copyOf(successorsByJobId.getOrDefault(allocation.getJob().id(), List.of())));
        }
    }

    private static Map<String, Allocation> allocationsByProjectId(List<Allocation> allocations, JobType jobType) {
        Map<String, Allocation> allocationByProjectId = new LinkedHashMap<>();
        for (Allocation allocation : allocations) {
            if (allocation.getJobType() == jobType) {
                allocationByProjectId.putIfAbsent(allocation.getProject().id(), allocation);
            }
        }
        return allocationByProjectId;
    }

    /**
     * Fails fast with an actionable message instead of letting an unknown reference
     * turn into a null in the solver model and a delayed NullPointerException.
     */
    private static <T> T require(Map<String, T> map, String key, String kind) {
        T value = map.get(key);
        if (value == null) {
            throw new IllegalArgumentException("Unknown %s '%s'.".formatted(kind, key));
        }
        return value;
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
        if (overrides.totalProjectDelayWeight() != null) {
            weights.put(ProjectJobScheduleConstraintProperties.TOTAL_PROJECT_DELAY,
                    HardMediumSoftScore.ofMedium(overrides.totalProjectDelayWeight()));
        }
        if (overrides.totalMakespanWeight() != null) {
            weights.put(ProjectJobScheduleConstraintProperties.TOTAL_MAKESPAN,
                    HardMediumSoftScore.ofSoft(overrides.totalMakespanWeight()));
        }
        if (!weights.isEmpty()) {
            schedule.setConstraintWeightOverrides(ConstraintWeightOverrides.of(weights));
        }
    }

    // lastModelOutput is used to recover a run that stopped halfway; it overrides the input assignment.
    private static void applyLastOutput(List<Allocation> allocations, Map<String, ExecutionMode> executionModeMap,
            Optional<ProjectJobScheduleOutput> lastModelOutput) {
        if (lastModelOutput.isEmpty()) {
            return;
        }
        Map<String, Allocation> allocationMap = new LinkedHashMap<>();
        for (Allocation allocation : allocations) {
            allocationMap.put(allocation.getId(), allocation);
        }
        for (AllocationOutputDTO solved : lastModelOutput.get().allocations()) {
            Allocation allocation = allocationMap.get(solved.id());
            if (allocation == null || allocation.getJobType() != JobType.STANDARD
                    || solved.executionModeId() == null || solved.delay() == null) {
                continue;
            }
            ExecutionMode executionMode = executionModeMap.get(solved.executionModeId());
            if (executionMode != null) {
                allocation.setExecutionMode(executionMode);
                allocation.setDelay(solved.delay());
            }
        }
    }
}
