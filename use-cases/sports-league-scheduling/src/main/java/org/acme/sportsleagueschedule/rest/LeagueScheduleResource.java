package org.acme.sportsleagueschedule.rest;

import jakarta.ws.rs.Path;

import ai.timefold.solver.service.rest.api.ModelRest;

@Path("/schedules")
public interface LeagueScheduleResource extends ModelRest {
}
