package org.acme.orderpicking.rest;

import jakarta.ws.rs.Path;

import ai.timefold.solver.service.rest.api.ModelRest;

// Endpoints are automatically added by the Service Module.
@Path("/picking-plans")
public interface PickingPlanResource extends ModelRest {
}
