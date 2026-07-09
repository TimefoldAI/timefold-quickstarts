package org.acme.taskassigning.rest;

import jakarta.ws.rs.Path;

import ai.timefold.solver.service.rest.api.ModelRest;

@Path("/task-assigning-plans")
public interface TaskAssigningResource extends ModelRest {
}
