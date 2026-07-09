package org.acme.maintenancescheduling.rest;

import jakarta.ws.rs.Path;

import ai.timefold.solver.service.rest.api.ModelRest;

@Path("/schedules")
public interface MaintenanceScheduleResource extends ModelRest {
}
