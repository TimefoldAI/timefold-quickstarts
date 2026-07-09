package org.acme.projectjobschedule.domain;

import java.util.List;

import ai.timefold.solver.core.api.domain.solution.ConstraintWeightOverrides;
import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty;
import ai.timefold.solver.core.api.score.HardMediumSoftScore;
import ai.timefold.solver.service.definition.api.SolverModel;
import ai.timefold.solver.service.definition.api.metrics.InputMetricsAware;
import ai.timefold.solver.service.definition.api.metrics.OutputMetricsAware;

import org.acme.projectjobschedule.domain.resource.Resource;
import org.acme.projectjobschedule.dto.ProjectJobScheduleInputMetrics;
import org.acme.projectjobschedule.dto.ProjectJobScheduleOutputMetrics;

@PlanningSolution
public class ProjectJobSchedule implements SolverModel<HardMediumSoftScore>,
        InputMetricsAware<ProjectJobScheduleInputMetrics>, OutputMetricsAware<ProjectJobScheduleOutputMetrics> {

    @ProblemFactCollectionProperty
    private List<Project> projects;
    @ProblemFactCollectionProperty
    private List<Resource> resources;
    @ProblemFactCollectionProperty
    private List<Job> jobs;
    @ProblemFactCollectionProperty
    private List<ExecutionMode> executionModes;
    @ProblemFactCollectionProperty
    private List<ResourceRequirement> resourceRequirements;

    @PlanningEntityCollectionProperty
    private List<Allocation> allocations;

    @PlanningScore
    private HardMediumSoftScore score;

    private ConstraintWeightOverrides<HardMediumSoftScore> constraintWeightOverrides = ConstraintWeightOverrides.none();

    public ProjectJobSchedule() {
    }

    public ProjectJobSchedule(List<Project> projects, List<Resource> resources, List<Job> jobs,
            List<ExecutionMode> executionModes, List<ResourceRequirement> resourceRequirements,
            List<Allocation> allocations) {
        this.projects = projects;
        this.resources = resources;
        this.jobs = jobs;
        this.executionModes = executionModes;
        this.resourceRequirements = resourceRequirements;
        this.allocations = allocations;
    }

    public List<Project> getProjects() {
        return projects;
    }

    public void setProjects(List<Project> projects) {
        this.projects = projects;
    }

    public List<Job> getJobs() {
        return jobs;
    }

    public void setJobs(List<Job> jobs) {
        this.jobs = jobs;
    }

    public List<ExecutionMode> getExecutionModes() {
        return executionModes;
    }

    public void setExecutionModes(List<ExecutionMode> executionModes) {
        this.executionModes = executionModes;
    }

    public List<Resource> getResources() {
        return resources;
    }

    public void setResources(List<Resource> resources) {
        this.resources = resources;
    }

    public List<ResourceRequirement> getResourceRequirements() {
        return resourceRequirements;
    }

    public void setResourceRequirements(List<ResourceRequirement> resourceRequirements) {
        this.resourceRequirements = resourceRequirements;
    }

    public List<Allocation> getAllocations() {
        return allocations;
    }

    public void setAllocations(List<Allocation> allocations) {
        this.allocations = allocations;
    }

    @Override
    public HardMediumSoftScore getScore() {
        return score;
    }

    public void setScore(HardMediumSoftScore score) {
        this.score = score;
    }

    @Override
    public ConstraintWeightOverrides<HardMediumSoftScore> getConstraintWeightOverrides() {
        return constraintWeightOverrides;
    }

    public void setConstraintWeightOverrides(ConstraintWeightOverrides<HardMediumSoftScore> constraintWeightOverrides) {
        this.constraintWeightOverrides = constraintWeightOverrides;
    }

    @Override
    public ProjectJobScheduleInputMetrics getInputMetrics() {
        return new ProjectJobScheduleInputMetrics(projects.size(), jobs.size(), resources.size(),
                executionModes.size(), allocations.size());
    }

    @Override
    public ProjectJobScheduleOutputMetrics getOutputMetrics() {
        int totalMakespan = allocations.stream()
                .filter(allocation -> allocation.getJobType() == JobType.SINK)
                .filter(allocation -> allocation.getEndDate() != null)
                .mapToInt(Allocation::getEndDate)
                .max()
                .orElse(0);
        int totalProjectDelay = allocations.stream()
                .filter(allocation -> allocation.getJobType() == JobType.SINK)
                .filter(allocation -> allocation.getEndDate() != null)
                .mapToInt(allocation -> Math.max(0, allocation.getProjectDelay()))
                .sum();
        int scheduledAllocations = (int) allocations.stream()
                .filter(allocation -> allocation.getExecutionMode() != null)
                .count();
        int unscheduledAllocations = allocations.size() - scheduledAllocations;
        return new ProjectJobScheduleOutputMetrics(totalMakespan, totalProjectDelay, scheduledAllocations,
                unscheduledAllocations);
    }

    @Override
    public String toString() {
        return "ProjectJobSchedule{allocations: " + allocations.size() + ", score: " + score + '}';
    }
}
