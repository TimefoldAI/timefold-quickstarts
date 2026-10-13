package org.acme.projectjobschedule.demo;

import static org.acme.projectjobschedule.domain.JobType.SINK;
import static org.acme.projectjobschedule.domain.JobType.SOURCE;
import static org.acme.projectjobschedule.domain.JobType.STANDARD;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.acme.projectjobschedule.domain.JobType;
import org.acme.projectjobschedule.dto.input.AllocationInputDTO;
import org.acme.projectjobschedule.dto.input.ExecutionModeInputDTO;
import org.acme.projectjobschedule.dto.input.JobInputDTO;
import org.acme.projectjobschedule.dto.input.ProjectInputDTO;
import org.acme.projectjobschedule.dto.input.ProjectJobScheduleInput;
import org.acme.projectjobschedule.dto.input.ResourceInputDTO;
import org.acme.projectjobschedule.dto.input.ResourceRequirementInputDTO;

/**
 * Builds the demo dataset: two projects of twelve jobs each, drawing on one resource shared by both
 * projects and three resources of their own.
 * <p>
 * The durations, the resource requirements and the precedence relations are drawn from a
 * {@link Random} seeded with {@link #RANDOM_SEED}, so the dataset is the same on every run.
 */
public final class DemoDataBuilder {

    static final int PROJECT_JOB_COUNT = 12;
    static final int EXECUTION_MODE_COUNT = 3;

    private static final long RANDOM_SEED = 0;

    private static final List<ProjectInputDTO> PROJECTS = List.of(
            new ProjectInputDTO("0", 0, 10),
            new ProjectInputDTO("1", 4, 19));

    /**
     * Resource "0" has no project, so both projects draw on it; the other six belong to one project
     * each. A renewable resource is freed again after every job, a non-renewable one is not.
     */
    private static final List<ResourceInputDTO> RESOURCES = List.of(
            new ResourceInputDTO("0", 16, null, true),
            new ResourceInputDTO("1", 13, "0", true),
            new ResourceInputDTO("2", 44, "0", false),
            new ResourceInputDTO("3", 39, "0", false),
            new ResourceInputDTO("4", 24, "1", true),
            new ResourceInputDTO("5", 66, "1", false),
            new ResourceInputDTO("6", 56, "1", false));

    /**
     * Upper probability bound per number of successor jobs: 54% of the jobs have one successor, 27%
     * have two, the remaining 19% have three.
     */
    private static final double[] SUCCESSOR_COUNT_PROBABILITIES = { 0.54, 0.81, 1.0 };

    /**
     * Upper probability bound per resource of the project, in the order returned by
     * {@link #projectResourceIds(ProjectInputDTO)}: the shared resource is required by 40% of the
     * execution modes, the project's own three by 20%, 15% and 25% respectively.
     */
    private static final double[] RESOURCE_PROBABILITIES = { 0.4, 0.6, 0.75, 1.0 };

    private DemoDataBuilder() {
    }

    public static DemoDataBuilder builder() {
        return new DemoDataBuilder();
    }

    public ProjectJobScheduleInput build() {
        Random random = new Random(RANDOM_SEED);
        List<JobInputDTO> jobs = generateJobs(random);
        return new ProjectJobScheduleInput(PROJECTS, RESOURCES, jobs, generateAllocations(jobs));
    }

    private static List<JobInputDTO> generateJobs(Random random) {
        AtomicInteger executionModeIndex = new AtomicInteger();
        AtomicInteger jobIndex = new AtomicInteger();
        List<JobInputDTO> jobs = new ArrayList<>(PROJECTS.size() * PROJECT_JOB_COUNT);
        for (ProjectInputDTO project : PROJECTS) {
            List<String> jobIds = new ArrayList<>(PROJECT_JOB_COUNT);
            for (int i = 0; i < PROJECT_JOB_COUNT; i++) {
                jobIds.add(String.valueOf(jobIndex.getAndIncrement()));
            }
            List<String> resourceIds = projectResourceIds(project);
            List<List<ExecutionModeInputDTO>> executionModes = new ArrayList<>(PROJECT_JOB_COUNT);
            for (int i = 0; i < PROJECT_JOB_COUNT; i++) {
                executionModes.add(generateExecutionModes(jobType(i), resourceIds, random, executionModeIndex));
            }
            List<List<String>> successorJobIds = generateSuccessorJobIds(jobIds, random);
            for (int i = 0; i < PROJECT_JOB_COUNT; i++) {
                jobs.add(new JobInputDTO(jobIds.get(i), project.id(), jobType(i), executionModes.get(i),
                        successorJobIds.get(i)));
            }
        }
        return List.copyOf(jobs);
    }

    /** A project is bracketed by a source and a sink job; everything in between is real work. */
    private static JobType jobType(int jobIndexInProject) {
        if (jobIndexInProject == 0) {
            return SOURCE;
        }
        return jobIndexInProject == PROJECT_JOB_COUNT - 1 ? SINK : STANDARD;
    }

    /** The resource shared by every project, followed by the project's own resources. */
    private static List<String> projectResourceIds(ProjectInputDTO project) {
        return RESOURCES.stream()
                .filter(resource -> resource.projectId() == null || resource.projectId().equals(project.id()))
                .map(ResourceInputDTO::id)
                .toList();
    }

    private static List<ExecutionModeInputDTO> generateExecutionModes(JobType jobType, List<String> resourceIds,
            Random random, AtomicInteger executionModeIndex) {
        if (jobType != STANDARD) {
            // A source or a sink job is not real work: it takes no time and consumes nothing.
            return List.of(new ExecutionModeInputDTO(String.valueOf(executionModeIndex.getAndIncrement()), 0,
                    List.of()));
        }
        // Every execution mode of a job requires the same number of resources, but not necessarily
        // the same ones, and each takes between one and five days.
        int requirementCount = random.nextInt(1, 4);
        List<ExecutionModeInputDTO> executionModes = new ArrayList<>(EXECUTION_MODE_COUNT);
        for (int i = 0; i < EXECUTION_MODE_COUNT; i++) {
            String id = String.valueOf(executionModeIndex.getAndIncrement());
            int duration = random.nextInt(1, 6);
            List<ResourceRequirementInputDTO> requirements = new ArrayList<>(requirementCount);
            Set<String> requiredResourceIds = new LinkedHashSet<>();
            while (requiredResourceIds.size() < requirementCount) {
                String resourceId = resourceIds.get(pick(random.nextDouble(), RESOURCE_PROBABILITIES));
                if (requiredResourceIds.add(resourceId)) {
                    requirements.add(new ResourceRequirementInputDTO(resourceId, random.nextInt(1, 6)));
                }
            }
            executionModes.add(new ExecutionModeInputDTO(id, duration, List.copyOf(requirements)));
        }
        return List.copyOf(executionModes);
    }

    /**
     * Draws the precedence relations, at the same index as the job they belong to. A job only ever
     * precedes a job that comes after it in {@code jobIds}, which keeps the relations acyclic.
     */
    private static List<List<String>> generateSuccessorJobIds(List<String> jobIds, Random random) {
        int jobCount = jobIds.size();
        List<List<String>> successorJobIds = new ArrayList<>(jobCount);
        // The source job starts the project's first three jobs.
        successorJobIds.add(List.copyOf(jobIds.subList(1, 4)));
        for (int i = 1; i < jobCount; i++) {
            int successorCount = Math.min(pick(random.nextDouble(), SUCCESSOR_COUNT_PROBABILITIES) + 1,
                    jobCount - i - 1);
            List<String> successors = new ArrayList<>(successorCount);
            while (successors.size() < successorCount) {
                String successorJobId = jobIds.get(random.nextInt(i + 1, jobCount));
                if (!successors.contains(successorJobId)) {
                    successors.add(successorJobId);
                }
            }
            successors.sort(Comparator.naturalOrder());
            successorJobIds.add(List.copyOf(successors));
        }
        // The last job before the sink hands over to it, and the sink ends the project.
        successorJobIds.set(jobCount - 2, List.of(jobIds.get(jobCount - 1)));
        successorJobIds.set(jobCount - 1, List.of());
        return successorJobIds;
    }

    /**
     * @param probability between 0 (inclusive) and 1 (exclusive)
     * @param upperBounds ascending upper probability bound per index, the last one being 1
     * @return the index of the first bound the probability falls within
     */
    private static int pick(double probability, double[] upperBounds) {
        for (int i = 0; i < upperBounds.length; i++) {
            if (probability <= upperBounds[i]) {
                return i;
            }
        }
        return upperBounds.length - 1;
    }

    /**
     * One allocation per job, with nothing assigned yet: the solver picks the execution mode and the
     * delay of every job, except for the source and sink jobs, which are pinned by the model itself.
     */
    private static List<AllocationInputDTO> generateAllocations(List<JobInputDTO> jobs) {
        return jobs.stream()
                .map(job -> new AllocationInputDTO(job.id(), job.id(), null, null, null))
                .toList();
    }
}
