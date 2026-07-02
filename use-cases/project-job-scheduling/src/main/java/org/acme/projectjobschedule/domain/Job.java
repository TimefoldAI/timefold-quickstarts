package org.acme.projectjobschedule.domain;

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(scope = Job.class, generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Job {

    private String id;
    private Project project;
    private JobType jobType;
    private List<ExecutionMode> executionModes;
    @JsonIdentityReference(alwaysAsId = true)
    private List<Job> successorJobs;

    public Job() {
    }

    public Job(String id) {
        this.id = id;
    }

    public Job(String id, Project project, JobType jobType) {
        this(id);
        this.project = project;
        this.jobType = jobType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public JobType getJobType() {
        return jobType;
    }

    public void setJobType(JobType jobType) {
        this.jobType = jobType;
    }

    public List<ExecutionMode> getExecutionModes() {
        return executionModes;
    }

    public void setExecutionModes(List<ExecutionMode> executionModes) {
        this.executionModes = executionModes;
    }

    public List<Job> getSuccessorJobs() {
        return successorJobs;
    }

    public void setSuccessorJobs(List<Job> successorJobs) {
        this.successorJobs = successorJobs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Job job))
            return false;
        return Objects.equals(getId(), job.getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    @Override
    public String toString() {
        return "Job-" + id;
    }
}
