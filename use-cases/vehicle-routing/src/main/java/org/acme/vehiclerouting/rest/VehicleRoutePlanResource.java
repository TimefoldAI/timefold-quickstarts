package org.acme.vehiclerouting.rest;

import jakarta.ws.rs.Path;

import ai.timefold.solver.service.rest.api.ModelRest;

@Path("/route-plans")
public interface VehicleRoutePlanResource extends ModelRest {
}
