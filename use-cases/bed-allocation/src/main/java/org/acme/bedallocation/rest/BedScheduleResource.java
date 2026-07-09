package org.acme.bedallocation.rest;

import jakarta.ws.rs.Path;

import ai.timefold.solver.service.rest.api.ModelRest;

@Path("/schedules")
public interface BedScheduleResource extends ModelRest {
}
