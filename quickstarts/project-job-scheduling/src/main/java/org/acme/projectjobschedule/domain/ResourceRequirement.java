package org.acme.projectjobschedule.domain;

import org.acme.projectjobschedule.domain.resource.Resource;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ResourceRequirement {

    private String id;
    private ExecutionMode executionMode;
    private Resource resource;
    private int requirement;

    public ResourceRequirement() {
    }

    public ResourceRequirement(String id) {
        this.id = id;
    }

    public ResourceRequirement(String id, ExecutionMode executionMode, Resource resource, int requirement) {
        this(id);
        this.executionMode = executionMode;
        this.resource = resource;
        this.requirement = requirement;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ExecutionMode getExecutionMode() {
        return executionMode;
    }

    public void setExecutionMode(ExecutionMode executionMode) {
        this.executionMode = executionMode;
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public int getRequirement() {
        return requirement;
    }

    public void setRequirement(int requirement) {
        this.requirement = requirement;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

    @JsonIgnore
    public boolean isResourceRenewable() {
        return resource.isRenewable();
    }

}
