package org.acme.projectjobschedule.domain;

import java.util.List;
import java.util.Objects;

/**
 * A unit of work within a {@link Project}. Each job is scheduled through exactly one
 * {@link Allocation}, which picks one of its {@link #executionModes()}.
 * <p>
 * The precedence relations between jobs are not held here: they are wired up between the
 * allocations themselves (see {@link Allocation#getPredecessorAllocations()}), which is what the
 * shadow variables need to compute the dates.
 */
public record Job(String id, Project project, JobType jobType, List<ExecutionMode> executionModes) {

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Job job)) {
            return false;
        }
        return Objects.equals(id, job.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Job-" + id;
    }
}
