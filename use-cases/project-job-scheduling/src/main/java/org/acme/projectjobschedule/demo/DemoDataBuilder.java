package org.acme.projectjobschedule.demo;

import static java.util.Collections.emptyList;
import static org.acme.projectjobschedule.domain.JobType.SINK;
import static org.acme.projectjobschedule.domain.JobType.SOURCE;
import static org.acme.projectjobschedule.domain.JobType.STANDARD;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import ai.timefold.solver.core.api.solver.SolutionManager;

import org.acme.projectjobschedule.domain.Allocation;
import org.acme.projectjobschedule.domain.ExecutionMode;
import org.acme.projectjobschedule.domain.Job;
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
import org.acme.projectjobschedule.dto.ProjectJobScheduleInput;
import org.acme.projectjobschedule.dto.ResourceDTO;
import org.acme.projectjobschedule.dto.ResourceRequirementDTO;

public final class DemoDataBuilder {

    private static final String GLOBAL_RESOURCE_TYPE = "GLOBAL";
    private static final String LOCAL_RESOURCE_TYPE = "LOCAL";
    private static final String GLOBAL_RESOURCE_ID = "0";
    private static final long RANDOM_SEED = 0L;
    private static final double ONE_SUCCESSOR_PROB = 0.54;
    private static final double TWO_SUCCESSORS_PROB = 0.81;
    private static final double GLOBAL_RESOURCE_PROB = 0.4;
    private static final double FIRST_LOCAL_RESOURCE_PROB = 0.6;
    private static final double SECOND_LOCAL_RESOURCE_PROB = 0.75;
    private static final int EXECUTION_MODES_PER_JOB = 3;
    private static final int MIN_JOBS = 2;
    private static final int MIN_DURATION = 1;
    private static final int MAX_DURATION_EXCLUSIVE = 6;
    private static final int MIN_REQUIREMENT = 1;
    private static final int MAX_REQUIREMENT_EXCLUSIVE = 6;

    private int jobCount = 24;
    private final List<ProjectDefinition> projectDefinitions = new ArrayList<>();
    private final List<ResourceDefinition> resourceDefinitions = new ArrayList<>();

    private DemoDataBuilder() {
    }

    public static DemoDataBuilder builder() {
        return new DemoDataBuilder();
    }

    public DemoDataBuilder setJobCount(int jobCount) {
        this.jobCount = jobCount;
        return this;
    }

    public DemoDataBuilder addProject(String id, int releaseDate, int criticalPathDuration) {
        projectDefinitions.add(new ProjectDefinition(id, releaseDate, criticalPathDuration));
        return this;
    }

    public DemoDataBuilder addGlobalResource(String id, int capacity) {
        resourceDefinitions.add(new ResourceDefinition(id, GLOBAL_RESOURCE_TYPE, capacity, true, null));
        return this;
    }

    public DemoDataBuilder addLocalResource(String id, String projectId, int capacity, boolean renewable) {
        resourceDefinitions.add(new ResourceDefinition(id, LOCAL_RESOURCE_TYPE, capacity, renewable, projectId));
        return this;
    }

    public ProjectJobScheduleInput build() {
        if (jobCount < MIN_JOBS) {
            throw new IllegalStateException("Number of jobs (" + jobCount + ") must be at least two.");
        }
        if (projectDefinitions.isEmpty()) {
            throw new IllegalStateException("At least one project must be defined.");
        }
        if (resourceDefinitions.isEmpty()) {
            throw new IllegalStateException("At least one resource must be defined.");
        }
        ProjectJobSchedule schedule = buildDomain();
        SolutionManager.updateShadowVariables(schedule);
        return flatten(schedule);
    }

    private ProjectJobSchedule buildDomain() {
        Random random = new Random(RANDOM_SEED);
        List<Project> projects = new ArrayList<>(projectDefinitions.size());
        for (ProjectDefinition definition : projectDefinitions) {
            projects.add(toProject(definition));
        }
        List<Resource> resources = new ArrayList<>(resourceDefinitions.size());
        for (ResourceDefinition definition : resourceDefinitions) {
            resources.add(toResource(definition, projects));
        }
        List<Job> jobs = generateJobs(jobCount, projects, resources, random);
        List<Allocation> allocations = generateAllocations(jobs);
        List<ExecutionMode> executionModes = jobs.stream().flatMap(job -> job.getExecutionModes().stream()).toList();
        List<ResourceRequirement> resourceRequirements =
                executionModes.stream().flatMap(e -> e.getResourceRequirements().stream()).toList();
        return new ProjectJobSchedule(projects, resources, jobs, executionModes, resourceRequirements, allocations);
    }

    private static Project toProject(ProjectDefinition definition) {
        return new Project(definition.projectId, definition.releaseDate, definition.criticalPathDuration);
    }

    private static Resource toResource(ResourceDefinition definition, List<Project> projects) {
        if (LOCAL_RESOURCE_TYPE.equals(definition.resourceType)) {
            Project project = projects.stream()
                    .filter(p -> p.getId().equals(definition.projectId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException(
                            "Local resource references unknown project " + definition.projectId));
            return new LocalResource(definition.resourceId, project, definition.capacity, definition.renewable);
        }
        return new GlobalResource(definition.resourceId, definition.capacity);
    }

    private List<Job> generateJobs(int jobsSize, List<Project> projects, List<Resource> resources, Random random) {
        List<Job> jobs = new ArrayList<>(jobsSize);
        int jobsCountPerProject = jobsSize / projects.size();
        int countJob = 0;
        for (Project project : projects) {
            List<Job> jobsPerProject = newList(jobsCountPerProject);
            jobsPerProject.add(newJob(String.valueOf(countJob), project, SOURCE));
            countJob++;
            for (int i = 0; i < jobsCountPerProject - 2; i++) {
                jobsPerProject.add(newJob(String.valueOf(countJob), project, STANDARD));
                countJob++;
            }
            jobsPerProject.add(newJob(String.valueOf(countJob), project, SINK));
            countJob++;
            jobs.addAll(jobsPerProject);
            jobsPerProject.forEach(job -> generateExecutionModes(jobs, job,
                    resources.stream()
                            .filter(r -> GLOBAL_RESOURCE_ID.equals(r.getId()) || (r instanceof LocalResource local
                                    && local.getProject().equals(project)))
                            .toList(),
                    random));
            Job firstJob = jobs.stream()
                    .filter(j -> j.getProject().equals(project) && j.getJobType() == SOURCE)
                    .findFirst()
                    .orElseThrow();
            firstJob.setSuccessorJobs(jobs.stream()
                    .filter(j -> j.getProject().equals(project) && j.getJobType() == STANDARD)
                    .toList()
                    .subList(0, 3));
            for (int i = 1; i < jobsCountPerProject; i++) {
                double jProb = random.nextDouble();
                int countSuccessorJobs = successorCount(jProb);
                if (countSuccessorJobs > jobsCountPerProject - i - 1) {
                    countSuccessorJobs = jobsCountPerProject - i - 1;
                }
                List<Job> successorJobs = newList(countSuccessorJobs);
                while (successorJobs.size() < countSuccessorJobs) {
                    int jobIdx = random.nextInt(i + 1, jobsCountPerProject);
                    if (!successorJobs.contains(jobsPerProject.get(jobIdx))) {
                        successorJobs.add(jobsPerProject.get(jobIdx));
                    }
                }
                successorJobs.sort(Comparator.comparing(Job::getId));
                jobsPerProject.get(i).setSuccessorJobs(successorJobs);
            }
            jobsPerProject.get(jobsCountPerProject - 2)
                    .setSuccessorJobs(List.of(jobsPerProject.get(jobsCountPerProject - 1)));
            jobsPerProject.get(jobsCountPerProject - 1).setSuccessorJobs(emptyList());
        }
        return jobs;
    }

    private static int successorCount(double jProb) {
        if (jProb <= ONE_SUCCESSOR_PROB) {
            return 1;
        }
        if (jProb <= TWO_SUCCESSORS_PROB) {
            return 2;
        }
        return EXECUTION_MODES_PER_JOB;
    }

    private static int resourceIndex(double rProb) {
        if (rProb <= GLOBAL_RESOURCE_PROB) {
            return 0;
        }
        if (rProb <= FIRST_LOCAL_RESOURCE_PROB) {
            return 1;
        }
        if (rProb <= SECOND_LOCAL_RESOURCE_PROB) {
            return 2;
        }
        return EXECUTION_MODES_PER_JOB;
    }

    private void generateExecutionModes(List<Job> jobs, Job job, List<Resource> resources, Random random) {
        int countExecutionMode = (int) jobs.stream()
                .filter(j -> j.getExecutionModes() != null)
                .mapToLong(j -> j.getExecutionModes().size())
                .sum();
        int countRequirements = (int) jobs.stream()
                .filter(j -> j.getExecutionModes() != null)
                .flatMap(e -> e.getExecutionModes().stream())
                .filter(e -> e.getResourceRequirements() != null)
                .mapToLong(e -> e.getResourceRequirements().size())
                .sum();
        if (job.getJobType() == SOURCE || job.getJobType() == SINK) {
            job.setExecutionModes(
                    List.of(newExecutionMode(String.valueOf(countExecutionMode), job, 0, emptyList())));
        } else if (job.getJobType() == STANDARD) {
            List<ExecutionMode> executionModes = newExecutionModeList();
            int requirementsSize = random.nextInt(1, EXECUTION_MODES_PER_JOB + 1);
            for (int i = 0; i < EXECUTION_MODES_PER_JOB; i++) {
                int duration = random.nextInt(MIN_DURATION, MAX_DURATION_EXCLUSIVE);
                ExecutionMode executionMode =
                        newExecutionMode(String.valueOf(countExecutionMode), job, duration, null);
                countExecutionMode++;
                List<ResourceRequirement> requirements = newRequirementList(requirementsSize);
                while (requirements.size() < requirementsSize) {
                    double rProb = random.nextDouble();
                    Resource resource = resources.get(resourceIndex(rProb));
                    if (requirements.stream().noneMatch(r -> r.getResource().equals(resource))) {
                        int requirementAmount = random.nextInt(MIN_REQUIREMENT, MAX_REQUIREMENT_EXCLUSIVE);
                        requirements.add(newResourceRequirement(String.valueOf(countRequirements), executionMode,
                                resource, requirementAmount));
                        countRequirements++;
                    }
                }
                executionMode.setResourceRequirements(requirements);
                executionModes.add(executionMode);
            }
            job.setExecutionModes(executionModes);
        }
    }

    private List<Allocation> generateAllocations(List<Job> jobs) {
        List<Allocation> allocations = new ArrayList<>(jobs.size());
        int doneDate = 0;
        for (int i = 0; i < jobs.size(); i++) {
            allocations.add(newAllocation(String.valueOf(i), jobs.get(i)));
        }
        for (int i = 0; i < jobs.size(); i++) {
            Allocation allocation = allocations.get(i);
            Allocation sourceAllocation = allocations.stream()
                    .filter(a -> a.getJob().getJobType() == SOURCE
                            && a.getJob().getProject().equals(allocation.getJob().getProject()))
                    .findFirst()
                    .orElseThrow();
            Allocation sinkAllocation = allocations.stream()
                    .filter(a -> a.getJob().getJobType() == SINK
                            && a.getJob().getProject().equals(allocation.getJob().getProject()))
                    .findFirst()
                    .orElseThrow();
            List<Allocation> predecessorAllocations = allocations.stream()
                    .filter(a -> !a.equals(allocation) && a.getJob().getSuccessorJobs().contains(allocation.getJob()))
                    .distinct()
                    .toList();
            List<Allocation> successorAllocations = allocation.getJob().getSuccessorJobs().stream()
                    .map(j -> allocations.stream().filter(a -> a.getJob().equals(j)).findFirst().orElseThrow())
                    .toList();
            allocation.setSourceAllocation(sourceAllocation);
            allocation.setSinkAllocation(sinkAllocation);
            allocation.setPredecessorAllocations(predecessorAllocations);
            allocation.setSuccessorAllocations(successorAllocations);
            allocation.setPredecessorsDoneDate(doneDate);
            boolean isSource = allocation.getJob().getJobType() == SOURCE;
            boolean isSink = allocation.getJob().getJobType() == SINK;
            if (isSource || isSink) {
                allocation.setExecutionMode(allocation.getJob().getExecutionModes().get(0));
                allocation.setDelay(0);
                if (isSink) {
                    doneDate += 4;
                }
            }
        }
        return allocations;
    }

    private static ProjectJobScheduleInput flatten(ProjectJobSchedule schedule) {
        List<ProjectDTO> projects = schedule.getProjects().stream()
                .map(p -> new ProjectDTO(p.getId(), p.getReleaseDate(), p.getCriticalPathDuration()))
                .toList();
        List<ResourceDTO> resources = schedule.getResources().stream()
                .map(DemoDataBuilder::toResourceDto)
                .toList();
        List<JobDTO> jobs = schedule.getJobs().stream()
                .map(DemoDataBuilder::toJobDto)
                .toList();
        List<ExecutionModeDTO> executionModes = schedule.getExecutionModes().stream()
                .map(e -> new ExecutionModeDTO(e.getId(), e.getJob().getId(), e.getDuration()))
                .toList();
        List<ResourceRequirementDTO> resourceRequirements = schedule.getResourceRequirements().stream()
                .map(r -> new ResourceRequirementDTO(r.getId(), r.getExecutionMode().getId(), r.getResource().getId(),
                        r.getRequirement()))
                .toList();
        List<AllocationDTO> allocations = schedule.getAllocations().stream()
                .map(DemoDataBuilder::toAllocationDto)
                .toList();
        return new ProjectJobScheduleInput(projects, resources, jobs, executionModes, resourceRequirements,
                allocations);
    }

    private static ResourceDTO toResourceDto(Resource resource) {
        if (resource instanceof LocalResource local) {
            return new ResourceDTO(local.getId(), LOCAL_RESOURCE_TYPE, local.getCapacity(), local.isRenewable(),
                    local.getProject().getId());
        }
        return new ResourceDTO(resource.getId(), GLOBAL_RESOURCE_TYPE, resource.getCapacity(), resource.isRenewable(),
                "");
    }

    private static JobDTO toJobDto(Job job) {
        List<String> successorJobIds = job.getSuccessorJobs() == null ? List.of()
                : job.getSuccessorJobs().stream().map(Job::getId).toList();
        return new JobDTO(job.getId(), job.getProject().getId(), job.getJobType().name(), successorJobIds);
    }

    private static AllocationDTO toAllocationDto(Allocation allocation) {
        Allocation source = allocation.getSourceAllocation();
        Allocation sink = allocation.getSinkAllocation();
        ExecutionMode mode = allocation.getExecutionMode();
        return new AllocationDTO(allocation.getId(), allocation.getJob().getId(),
                source == null ? "" : source.getId(),
                sink == null ? "" : sink.getId(),
                ids(allocation.getPredecessorAllocations()),
                ids(allocation.getSuccessorAllocations()),
                mode == null ? "" : mode.getId(),
                allocation.getDelay(), allocation.getStartDate(), allocation.getEndDate());
    }

    private static List<String> ids(List<Allocation> allocations) {
        return allocations == null ? List.of() : allocations.stream().map(Allocation::getId).toList();
    }

    private static <T> List<T> newList(int capacity) {
        return new ArrayList<>(capacity);
    }

    private static List<ExecutionMode> newExecutionModeList() {
        return new ArrayList<>(EXECUTION_MODES_PER_JOB);
    }

    private static List<ResourceRequirement> newRequirementList(int capacity) {
        return new ArrayList<>(capacity);
    }

    private static Job newJob(String id, Project project, org.acme.projectjobschedule.domain.JobType jobType) {
        return new Job(id, project, jobType);
    }

    private static ExecutionMode newExecutionMode(String id, Job job, int duration,
            List<ResourceRequirement> requirements) {
        if (requirements == null) {
            return new ExecutionMode(id, job, duration);
        }
        return new ExecutionMode(id, job, duration, requirements);
    }

    private static ResourceRequirement newResourceRequirement(String id, ExecutionMode executionMode, Resource resource,
            int requirement) {
        return new ResourceRequirement(id, executionMode, resource, requirement);
    }

    private static Allocation newAllocation(String id, Job job) {
        return new Allocation(id, job);
    }

    private static final class ProjectDefinition {
        private final String projectId;
        private final int releaseDate;
        private final int criticalPathDuration;

        private ProjectDefinition(String projectId, int releaseDate, int criticalPathDuration) {
            this.projectId = projectId;
            this.releaseDate = releaseDate;
            this.criticalPathDuration = criticalPathDuration;
        }
    }

    private static final class ResourceDefinition {
        private final String resourceId;
        private final String resourceType;
        private final int capacity;
        private final boolean renewable;
        private final String projectId;

        private ResourceDefinition(String resourceId, String resourceType, int capacity, boolean renewable,
                String projectId) {
            this.resourceId = resourceId;
            this.resourceType = resourceType;
            this.capacity = capacity;
            this.renewable = renewable;
            this.projectId = projectId;
        }
    }
}
