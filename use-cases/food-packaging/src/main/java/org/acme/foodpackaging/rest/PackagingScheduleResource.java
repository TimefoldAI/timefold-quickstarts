package org.acme.foodpackaging.rest;

import jakarta.ws.rs.Path;

import ai.timefold.solver.service.rest.api.ModelRest;

@Path("/schedules")
public interface PackagingScheduleResource extends ModelRest {
}
