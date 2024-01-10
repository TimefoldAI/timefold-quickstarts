package org.acme.kotlin.schooltimetabling.rest.exception

import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.ext.ExceptionMapper
import jakarta.ws.rs.ext.Provider

@Provider
class TimetableSolverExceptionMapper :
    ExceptionMapper<TimetableSolverException> {
    override fun toResponse(exception: TimetableSolverException): Response {
        return Response
            .status(exception.status)
            .type(MediaType.APPLICATION_JSON)
            .entity(ErrorInfo(exception.jobId, exception.message ?: ""))
            .build()
    }
}