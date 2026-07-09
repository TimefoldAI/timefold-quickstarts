package org.acme.employeescheduling.rest;

import jakarta.ws.rs.Path;

import ai.timefold.solver.service.rest.api.ModelRest;

@Path("/schedules")
public interface EmployeeScheduleResource extends ModelRest {
}
