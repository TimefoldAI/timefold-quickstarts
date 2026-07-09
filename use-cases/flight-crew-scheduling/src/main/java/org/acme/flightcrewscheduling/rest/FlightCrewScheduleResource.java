package org.acme.flightcrewscheduling.rest;

import jakarta.ws.rs.Path;

import ai.timefold.solver.service.rest.api.ModelRest;

@Path("/schedules")
public interface FlightCrewScheduleResource extends ModelRest {
}
