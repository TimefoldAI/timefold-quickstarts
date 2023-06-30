package org.acme.vehiclerouting.rest;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import ai.timefold.solver.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
import ai.timefold.solver.core.api.solver.SolutionManager;
import ai.timefold.solver.core.api.solver.SolverManager;
import ai.timefold.solver.core.api.solver.SolverStatus;

import org.acme.vehiclerouting.domain.VehicleRoutePlan;
import org.acme.vehiclerouting.rest.exception.VehicleRoutingSolverException;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("route-plans")
public class VehicleRoutePlanResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(VehicleRoutePlanResource.class);

    private final SolverManager<VehicleRoutePlan, String> solverManager;

    private final SolutionManager<VehicleRoutePlan, HardSoftLongScore> solutionManager;

    // TODO: Without any "time to live", the map may eventually grow out of memory.
    private final ConcurrentMap<String, Job> jobIdToJob = new ConcurrentHashMap<>();

    // Workaround to make Quarkus CDI happy. Do not use.
    public VehicleRoutePlanResource() {
        this.solverManager = null;
        this.solutionManager = null;
    }

    @Inject
    public VehicleRoutePlanResource(SolverManager<VehicleRoutePlan, String> solverManager,
            SolutionManager<VehicleRoutePlan, HardSoftLongScore> solutionManager) {
        this.solverManager = solverManager;
        this.solutionManager = solutionManager;
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces(MediaType.TEXT_PLAIN)
    public String solve(VehicleRoutePlan problem) {
        String jobId = UUID.randomUUID().toString();
        jobIdToJob.put(jobId, Job.ofRoutePlan(problem));
        solverManager.solveAndListen(jobId,
                jobId_ -> jobIdToJob.get(jobId).routePlan,
                solution -> jobIdToJob.put(jobId, Job.ofRoutePlan(solution)),
                (jobId_, exception) -> {
                    jobIdToJob.put(jobId, Job.ofException(exception));
                    LOGGER.error("Failed solving jobId ({}).", jobId, exception);
                });
        return jobId;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{jobId}")
    public VehicleRoutePlan getRoutePlan(
            @Parameter(description = "The job ID returned by the POST method.") @PathParam("jobId") String jobId) {
        VehicleRoutePlan routePlan = getRoutePlanAndCheckForExceptions(jobId);
        SolverStatus solverStatus = solverManager.getSolverStatus(jobId);
        String scoreExplanation = solutionManager.explain(routePlan).getSummary();
        routePlan.setSolverStatus(solverStatus);
        routePlan.setScoreExplanation(scoreExplanation);
        return routePlan;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{jobId}/status")
    public VehicleRoutePlan getStatus(
            @Parameter(description = "The job ID returned by the POST method.") @PathParam("jobId") String jobId) {
        VehicleRoutePlan routePlan = getRoutePlanAndCheckForExceptions(jobId);
        SolverStatus solverStatus = solverManager.getSolverStatus(jobId);
        return new VehicleRoutePlan(routePlan.getName(), routePlan.getScore(), solverStatus);
    }

    private VehicleRoutePlan getRoutePlanAndCheckForExceptions(String jobId) {
        Job job = jobIdToJob.get(jobId);
        if (job == null) {
            throw new VehicleRoutingSolverException(jobId, Response.Status.NOT_FOUND, "No route plan found.");
        }
        if (job.exception != null) {
            throw new VehicleRoutingSolverException(jobId, job.exception);
        }
        return job.routePlan;
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{jobId}")
    public VehicleRoutePlan terminateSolving(
            @Parameter(description = "The job ID returned by the POST method.") @PathParam("jobId") String jobId) {
        // TODO: Replace with .terminateEarlyAndWait(... [, timeout]); see https://github.com/TimefoldAI/timefold-solver/issues/77
        solverManager.terminateEarly(jobId);
        return getRoutePlan(jobId);
    }

    private record Job(VehicleRoutePlan routePlan, Throwable exception) {

        static Job ofRoutePlan(VehicleRoutePlan routePlan) {
            return new Job(routePlan, null);
        }

        static Job ofException(Throwable exception) {
            return new Job(null, exception);
        }

    }
}
