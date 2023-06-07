package org.acme.schooltimetabling.service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import ai.timefold.solver.core.api.solver.SolverManager;
import ai.timefold.solver.core.api.solver.SolverStatus;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.acme.schooltimetabling.domain.TimeTable;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;

@Path("schedules")
public class TimeTableSchedulesResource {
    private final SolverManager<TimeTable, String> solverManager;

    private ConcurrentMap<String, Job> jobs = new ConcurrentHashMap<>();

    // Workaround to make Quarkus CDI happy. Do not use.
    public TimeTableSchedulesResource() {
        this.solverManager = null;
    }

    @Inject
    public TimeTableSchedulesResource(SolverManager<TimeTable, String> solverManager) {
        this.solverManager = solverManager;
    }

    @Operation(summary = "List the job IDs of all submitted problems (scheduled, solving schedule, finished).")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "List of all job IDs.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class)))})
    @GET
    public List<String> list() {
        return jobs.keySet().stream().toList();
    }

    @Operation(summary = "Submits a problem to start solving as soon as CPU resources are available.")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "The job ID. Use that ID to get the solution with the other methods.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class)))})
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes({MediaType.APPLICATION_JSON})
    @POST
    public String solve(TimeTable problem) {
        String jobId = UUID.randomUUID().toString();
        jobs.put(jobId, Job.newTimeTable(problem));
        solverManager.solveAndListen(jobId,
                jobId_ -> jobs.get(jobId).timeTable,
                solution -> jobs.put(jobId, Job.newTimeTable(solution)),
                (jobId_, exception) -> jobs.put(jobId, Job.error(jobId_, exception)));
        return jobId;
    }

    @Operation(summary = "Get the solution and score for a given job ID. This is the best solution so far, as it might still be running or not even started.")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "The best solution so far.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TimeTable.class)))})
    @GET
    @Path("{jobId}")
    public TimeTable getSchedule(
            @Parameter(description = "The job ID returned by the POST method.", required = false)
            @PathParam("jobId")
            String jobId) {
        TimeTable timeTable = jobs.get(jobId).getTimeTableOrThrowError();
        SolverStatus solverStatus = solverManager.getSolverStatus(jobId);
        timeTable.setSolverStatus(solverStatus);
        return timeTable;
    }

    @Operation(summary = "Terminate solving for a given job ID. Returns the best solution so far, as it might still be running or not even started.")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "The best solution so far.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TimeTable.class)))})
    @DELETE
    @Path("{jobId}")
    public TimeTable terminateSolving(
            @Parameter(description = "The job ID returned by the POST method.", required = false)
            @PathParam("jobId")
            String jobId) {
        solverManager.terminateEarly(jobId);
        return getSchedule(jobId);
    }

    private record Job(String jobId, TimeTable timeTable, Throwable error) {

        static Job newTimeTable(TimeTable timeTable) {
            return new Job(null, timeTable, null);
        }

        static Job error(String jobId, Throwable error) {
            return new Job(jobId, null, error);
        }

        TimeTable getTimeTableOrThrowError() {
            if (error != null) {
                throw new SolverRuntimeException(jobId, error);
            }

            return timeTable;
        }
    }
}
