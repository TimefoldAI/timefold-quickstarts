package org.acme.projectjobschedule.domain;

import java.util.Objects;

/**
 * Something a job consumes while it executes, e.g. a machine or a crew.
 * <p>
 * A renewable resource is only occupied while a job runs, so its capacity is a limit per day; a
 * non-renewable resource is consumed for good, so its capacity is a limit over the whole schedule.
 * A resource without a {@link #project()} is global: every project draws from the same capacity.
 */
public record Resource(String id, int capacity, Project project, boolean renewable) {

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Resource resource)) {
            return false;
        }
        return Objects.equals(id, resource.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Resource-" + id;
    }
}
