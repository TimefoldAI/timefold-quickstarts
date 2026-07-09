package org.acme.schooltimetabling.rest;

import jakarta.ws.rs.Path;

import ai.timefold.solver.service.rest.api.ModelRest;

@Path("/timetables")
public interface TimetableResource extends ModelRest {
}
