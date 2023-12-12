package org.acme.kotlin.schooltimetabling.rest.exception

import jakarta.ws.rs.core.Response

class TimetableSolverException : RuntimeException {

    var jobId: String

    var status: Response.Status

    constructor(jobId: String, status: Response.Status, message: String?) : super(message) {
        this.jobId = jobId
        this.status = status
    }

    constructor(jobId: String, cause: Throwable) : super(cause.message, cause) {
        this.jobId = jobId
        this.status = Response.Status.INTERNAL_SERVER_ERROR
    }
}
