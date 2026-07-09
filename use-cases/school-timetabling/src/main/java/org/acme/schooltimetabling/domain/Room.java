package org.acme.schooltimetabling.domain;

import ai.timefold.solver.core.api.domain.common.PlanningId;

/**
 * A room in which a {@link Lesson} can be taught.
 */
public class Room {

    @PlanningId
    private String id;

    private String name;

    public Room() {
    }

    public Room(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
