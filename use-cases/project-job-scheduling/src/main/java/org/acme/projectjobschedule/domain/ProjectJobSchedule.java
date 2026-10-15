package org.acme.projectjobschedule.domain;

import java.util.List;

import ai.timefold.solver.core.api.domain.solution.ConstraintWeightOverrides;
import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.HardMediumSoftScore;
import ai.timefold.solver.service.definition.api.SolverModel;
import ai.timefold.solver.service.definition.api.metrics.InputMetricsAware;
import ai.timefold.solver.service.definition.api.metrics.OutputMetricsAware;

import org.acme.projectjobschedule.dto.input.ProjectJobScheduleInputMetrics;
import org.acme.projectjobschedule.dto.output.ProjectJobScheduleOutputMetrics;

@PlanningSolution
public class ProjectJobSchedule implements SolverModel<HardMediumSoftScore>,
        InputMetricsAware<ProjectJobScheduleInputMetrics>, OutputMetricsAware<ProjectJobScheduleOutputMetrics> {

    @ProblemFactCollectionProperty
    private List<Project> projects;
    @ProblemFactCollectionProperty
    @ValueRangeProvider
    private List<Resource> resources;
    @ProblemFactCollectionProperty
    private List<Job> jobs;
    @ProblemFactCollectionProperty
    private List<ExecutionMode> executionModes;
    @ProblemFactCollectionProperty
    private List<ResourceRequirement> resourceRequirements;

    @PlanningEntityCollectionProperty
    private List<Allocation> allocations;
    // REPRODUCER ONLY: a second entity class, whose basic variable takes its values from the solution.
    @PlanningEntityCollectionProperty
    private List<ProjectLead> projectLeads;

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
        this.projectLeads = projects.stream()
                .map(project -> new ProjectLead("lead-" + project.id(), project))
                .toList();
    }

    public List<Project> getProjects() {
        return projects;
    }

    public List<Resource> getResources() {
        return resources;
    }

    public List<Job> getJobs() {
        return jobs;
    }

    public List<ExecutionMode> getExecutionModes() {
        return executionModes;
    }

    public List<ResourceRequirement> getResourceRequirements() {
        return resourceRequirements;
    }

    public List<Allocation> getAllocations() {
        return allocations;
    }

    public List<ProjectLead> getProjectLeads() {
        return projectLeads;
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
        return new ProjectJobScheduleInputMetrics(allocations.size(), jobs.size(), projects.size(), resources.size());
    }

    @Override
    public ProjectJobScheduleOutputMetrics getOutputMetrics() {
        // Read the planning variables rather than the shadow variables wherever possible: an
        // allocation whose execution mode is still unassigned has no meaningful dates yet.
        int assignedAllocations = (int) allocations.stream()
                .filter(allocation -> allocation.getExecutionMode() != null && allocation.getDelay() != null)
                .count();
        int unassignedAllocations = allocations.size() - assignedAllocations;
        int totalProjectDelay = allocations.stream()
                .filter(allocation -> allocation.getJobType() == JobType.SINK)
                .filter(allocation -> allocation.getEndDate() != null)
                .mapToInt(allocation -> Math.max(0, allocation.getProjectDelay()))
                .sum();
        int makespan = allocations.stream()
                .filter(allocation -> allocation.getJobType() == JobType.SINK)
                .filter(allocation -> allocation.getEndDate() != null)
                .mapToInt(Allocation::getEndDate)
                .max()
                .orElse(0);
        return new ProjectJobScheduleOutputMetrics(assignedAllocations, unassignedAllocations, totalProjectDelay,
                makespan);
    }
}
