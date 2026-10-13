package org.acme.projectjobschedule.domain;

import java.util.Objects;

/**
 * A set of jobs that has to be executed, with a release date and a critical path duration:
 * the shortest possible duration of the project, so the earliest date it can possibly end on.
 */
public record Project(String id, int releaseDate, int criticalPathDuration) {

    /** The earliest date this project could possibly end on, so anything later is a delay. */
    public int criticalPathEndDate() {
        return releaseDate + criticalPathDuration;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Project project)) {
            return false;
        }
        return Objects.equals(id, project.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Project-" + id;
    }
}
