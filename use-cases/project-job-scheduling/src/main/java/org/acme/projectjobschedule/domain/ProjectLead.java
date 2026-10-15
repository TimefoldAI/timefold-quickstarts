package org.acme.projectjobschedule.domain;

import java.util.Objects;

import ai.timefold.solver.core.api.domain.common.PlanningId;
import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;

/**
 * REPRODUCER ONLY: a second entity class with a basic variable whose value range comes from the
 * solution ({@link ProjectJobSchedule#getResources()}), while {@link Allocation} uses entity-level
 * value ranges.
 * <p>
 * With Timefold Solver 2.7, the default swap move selector enables entity value range filtering
 * as soon as any basic variable in the model has an entity-level range, and then also applies it to
 * this entity class, which has none. Solving crashes.
 */
@PlanningEntity
public class ProjectLead {

    @PlanningId
    private String id;
    private Project project;

    @PlanningVariable
    private Resource resource;

    public ProjectLead() {
    }

    public ProjectLead(String id, Project project) {
        this.id = id;
        this.project = project;
    }

    // Workaround: uncomment (and fill it from ProjectJobSchedule with every resource) to give this
    // entity class an entity-level range too; the solve then succeeds.
    // private List<Resource> resourceRange;
    //
    // @ValueRangeProvider
    // public List<Resource> getResourceRange() {
    //     return resourceRange;
    // }

    public String getId() {
        return id;
    }

    public Project getProject() {
        return project;
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ProjectLead projectLead)) {
            return false;
        }
        return Objects.equals(id, projectLead.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return id;
    }
}
