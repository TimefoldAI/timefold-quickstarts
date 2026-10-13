package org.acme.projectjobschedule.domain;

import java.util.Objects;

/**
 * How much of a {@link Resource} an {@link ExecutionMode} consumes.
 */
public record ResourceRequirement(String id, ExecutionMode executionMode, Resource resource, int requirement) {

    public boolean isResourceRenewable() {
        return resource.renewable();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ResourceRequirement resourceRequirement)) {
            return false;
        }
        return Objects.equals(id, resourceRequirement.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "ResourceRequirement-" + id;
    }
}
