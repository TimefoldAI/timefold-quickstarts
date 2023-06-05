package org.acme.schooltimetabling.service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import ai.timefold.solver.core.api.solver.SolverManager;
import jakarta.inject.Inject;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import org.acme.schooltimetabling.domain.TimeTable;

@Path("schedules")
public class TimeTableSchedulesResource {

    private final SolverManager<TimeTable, String> solverManager;

    // TODO Unify map and introduce SolverStatus
    private ConcurrentMap<String, TimeTable> bestSolutionMap = new ConcurrentHashMap<>();
    private ConcurrentMap<String, Throwable> exceptionMap = new ConcurrentHashMap<>();

    // Workaround to make Quarkus CDI happy. Do not use.
    public TimeTableSchedulesResource() {
        this.solverManager = null;
    }

    @Inject
    public TimeTableSchedulesResource(SolverManager<TimeTable, String> solverManager) {
        this.solverManager = solverManager;
    }

    @GET
    public List<String> list() {
        return bestSolutionMap.keySet().stream().toList();
    }

    @POST
    public String solve(TimeTable problem) {
        String jobId = UUID.randomUUID().toString();
        bestSolutionMap.put(jobId, problem);
        solverManager.solveAndListen(jobId,
                jobId_ -> bestSolutionMap.get(jobId),
                solution -> bestSolutionMap.put(jobId, solution),
                (jobId_, exception)  -> exceptionMap.put(jobId, exception));
        return jobId;
    }

    @GET
    @Path("{jobId}")
    public TimeTable getSchedule(@PathParam("jobId") String jobId) {
        // TODO check exception map
        return bestSolutionMap.get(jobId);
    }

    @DELETE
    @Path("{jobId}")
    public TimeTable terminateSolving(@PathParam("jobId") String jobId) {
        // TODO check exception map
        solverManager.terminateEarly(jobId);
        return getSchedule(jobId);
    }

}
