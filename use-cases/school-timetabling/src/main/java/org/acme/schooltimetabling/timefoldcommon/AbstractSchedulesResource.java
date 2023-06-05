package org.acme.schooltimetabling.timefoldcommon;

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


public abstract class AbstractSchedulesResource<Solution_> {

    private final SolverManager<Solution_, String> solverManager;

    // TODO Unify map and introduce SolverStatus
    private ConcurrentMap<String, Solution_> bestSolutionMap = new ConcurrentHashMap<>();
    private ConcurrentMap<String, Throwable> exceptionMap = new ConcurrentHashMap<>();

    protected AbstractSchedulesResource(SolverManager<Solution_, String> solverManager) {
        this.solverManager = solverManager;
    }

    @GET
    public List<String> list() {
        return bestSolutionMap.keySet().stream().toList();
    }

    @POST
    public String solve(Solution_ problem) {
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
    public Solution_ getSchedule(@PathParam("jobId") String jobId) {
        // TODO check exception map
        return bestSolutionMap.get(jobId);
    }

    @DELETE
    @Path("{jobId}")
    public Solution_ terminateSolving(@PathParam("jobId") String jobId) {
        // TODO check exception map
        solverManager.terminateEarly(jobId);
        return getSchedule(jobId);
    }

}
