package org.acme.facilitylocation.rest;

import jakarta.ws.rs.Path;

import ai.timefold.solver.service.rest.api.ModelRest;

// Endpoints are automatically added by the Service Module.
@Path("/plans")
public interface FacilityPlanResource extends ModelRest {
}
