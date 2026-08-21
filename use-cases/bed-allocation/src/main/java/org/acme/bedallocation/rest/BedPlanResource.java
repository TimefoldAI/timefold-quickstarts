package org.acme.bedallocation.rest;

import jakarta.ws.rs.Path;

import ai.timefold.solver.service.rest.api.ModelRest;

// Endpoints are automatically added by the Service Module.
@Path("/schedules")
public interface BedPlanResource extends ModelRest {
}
