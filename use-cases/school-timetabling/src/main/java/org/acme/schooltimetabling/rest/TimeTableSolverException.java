package org.acme.schooltimetabling.rest;

import jakarta.ws.rs.core.Response;

public class TimeTableSolverException extends RuntimeException {

    private final String jobId;

    private final Response.Status status;

    public TimeTableSolverException(String jobId, Response.Status status, String message) {
        super(message);
        this.jobId = jobId;
        this.status = status;
    }

    public TimeTableSolverException(String jobId, Throwable cause) {
        super(cause.getMessage(), cause);
        this.jobId = jobId;
        this.status = Response.Status.INTERNAL_SERVER_ERROR;
    }

    public String getJobId() {
        return jobId;
    }

    public Response.Status getStatus() {
        return status;
    }
}
