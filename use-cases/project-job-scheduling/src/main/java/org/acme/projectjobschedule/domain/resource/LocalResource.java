package org.acme.projectjobschedule.domain.resource;

import org.acme.projectjobschedule.domain.Project;

public class LocalResource extends Resource {

    private Project project;
    private boolean renewable;

    public LocalResource() {
    }

    public LocalResource(String id, Project project, int capacity, boolean renewable) {
        super(id, capacity);
        this.project = project;
        this.renewable = renewable;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    @Override
    public boolean isRenewable() {
        return renewable;
    }

    public void setRenewable(boolean renewable) {
        this.renewable = renewable;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

}
