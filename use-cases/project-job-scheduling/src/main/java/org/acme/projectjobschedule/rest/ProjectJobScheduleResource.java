package org.acme.projectjobschedule.rest;

import jakarta.ws.rs.Path;

import ai.timefold.solver.service.rest.api.ModelRest;

@Path("/project-job-schedules")
public interface ProjectJobScheduleResource extends ModelRest {
}
