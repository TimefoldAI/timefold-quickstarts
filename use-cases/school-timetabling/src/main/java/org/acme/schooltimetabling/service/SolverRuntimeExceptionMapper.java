package org.acme.schooltimetabling.service;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class SolverRuntimeExceptionMapper implements ExceptionMapper<SolverRuntimeException> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SolverRuntimeException.class);

    @Override
    public Response toResponse(SolverRuntimeException ex) {

        LOGGER.error("Runtime exception during solving jobId ({}), message ({})", ex.getJobId(), ex.getMessage(), ex);

        ErrorInfo errorInfo = new ErrorInfo(ex.getJobId(), ex.getMessage());

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .entity(errorInfo)
                .build();
    }

}
