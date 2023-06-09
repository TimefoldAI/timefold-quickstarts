package org.acme.schooltimetabling.rest;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class TimeTableSolverExceptionMapper implements ExceptionMapper<TimeTableSolverException> {

    @Override
    public Response toResponse(TimeTableSolverException exception) {
        return Response
                .status(exception.getStatus())
                .type(MediaType.APPLICATION_JSON)
                .entity(new ErrorInfo(exception.getJobId(), exception.getMessage()))
                .build();
    }
}
