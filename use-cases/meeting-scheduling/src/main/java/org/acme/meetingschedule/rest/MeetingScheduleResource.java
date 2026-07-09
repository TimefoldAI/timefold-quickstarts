package org.acme.meetingschedule.rest;

import jakarta.ws.rs.Path;

import ai.timefold.solver.service.rest.api.ModelRest;

@Path("/meeting-schedules")
public interface MeetingScheduleResource extends ModelRest {
}
