package com.acme.schooltimetabling.rest;

import jakarta.ws.rs.Path;

import ai.timefold.solver.service.rest.api.ModelRest;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Tag(name = "School Timetabling",
        description = "School timetabling service assigning lessons to timeslots.") // OpenAPI documentation annotation
@Path("/timetables")
public interface TimetableResource extends ModelRest {
}
