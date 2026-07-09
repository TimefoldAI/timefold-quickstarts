package org.acme.facilitylocation.rest;

import jakarta.ws.rs.Path;

import ai.timefold.solver.service.rest.api.ModelRest;

@Path("/facilitylocations")
public interface FacilityLocationResource extends ModelRest {
}
