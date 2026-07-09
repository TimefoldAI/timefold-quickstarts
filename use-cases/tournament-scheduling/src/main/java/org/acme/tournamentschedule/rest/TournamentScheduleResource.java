package org.acme.tournamentschedule.rest;

import jakarta.ws.rs.Path;

import ai.timefold.solver.service.rest.api.ModelRest;

@Path("/schedules")
public interface TournamentScheduleResource extends ModelRest {
}
