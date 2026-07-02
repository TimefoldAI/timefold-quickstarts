package org.acme.projectjobschedule.domain;

import java.util.List;

import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty;
import ai.timefold.solver.core.api.score.HardMediumSoftScore;
import ai.timefold.solver.core.api.solver.SolverStatus;

import org.acme.projectjobschedule.domain.resource.Resource;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@PlanningSolution
public class ProjectJobSchedule {

    @ProblemFactCollectionProperty
    private List<Project> projects;
    @ProblemFactCollectionProperty
    private List<Resource> resources;
    @ProblemFactCollectionProperty
    private List<Job> jobs;
    @JsonIgnore
    @ProblemFactCollectionProperty
    private List<ExecutionMode> executionModes;
    @JsonIgnore
    @ProblemFactCollectionProperty
    private List<ResourceRequirement> resourceRequirements;

    @PlanningEntityCollectionProperty
    private List<Allocation> allocations;

    @PlanningScore
    private HardMediumSoftScore score;
    private SolverStatus solverStatus;

    public ProjectJobSchedule() {
    }

    @JsonCreator
    public ProjectJobSchedule(@JsonProperty("projects") List<Project> projects,
            @JsonProperty("resources") List<Resource> resources,
            @JsonProperty("jobs") List<Job> jobs, @JsonProperty("allocations") List<Allocation> allocations) {
        this.projects = projects;
        this.resources = resources;
        this.jobs = jobs;
        this.allocations = allocations;
        this.executionModes = jobs.stream().flatMap(job -> job.getExecutionModes().stream()).toList();
        this.resourceRequirements = executionModes.stream().flatMap(e -> e.getResourceRequirements().stream()).toList();
    }

    public ProjectJobSchedule(HardMediumSoftScore score, SolverStatus solverStatus) {
        this.score = score;
        this.solverStatus = solverStatus;
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

    public HardMediumSoftScore getScore() {
        return score;
    }

    public void setScore(HardMediumSoftScore score) {
        this.score = score;
    }

    public SolverStatus getSolverStatus() {
        return solverStatus;
    }

    public void setSolverStatus(SolverStatus solverStatus) {
        this.solverStatus = solverStatus;
    }
}
