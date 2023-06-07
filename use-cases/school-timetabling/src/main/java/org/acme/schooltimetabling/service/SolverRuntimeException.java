package org.acme.schooltimetabling.service;

public class SolverRuntimeException extends RuntimeException {

    private final String jobId;

    public SolverRuntimeException(String jobId, Throwable cause) {
        super(cause);
        this.jobId = jobId;
    }

    public SolverRuntimeException(String jobId, String message, Throwable cause) {
        super(message, cause);
        this.jobId = jobId;
    }

    public String getJobId() {
        return jobId;
    }
}
