package org.acme.schooltimetabling.rest;

import jakarta.ws.rs.Path;

import ai.timefold.solver.service.rest.api.ModelRest;

// Endpoints are automatically added by the Service Module.
@Path("/timetables")
public interface TimetableResource extends ModelRest {
}
