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
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.schooltimetabling.domain.TimeTable;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Tag(name = "School Timetables", description = "School timetable service assigning lessons to rooms and timeslots.")
@Path("timetables")
public class TimeTableResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(TimeTableResource.class);

    private final SolverManager<TimeTable, String> solverManager;

    private ConcurrentMap<String, Job> jobs = new ConcurrentHashMap<>();

    // Workaround to make Quarkus CDI happy. Do not use.
    public TimeTableResource() {
        this.solverManager = null;
    }

    @Inject
    public TimeTableResource(SolverManager<TimeTable, String> solverManager) {
        this.solverManager = solverManager;
    }

    @Operation(summary = "List the job IDs of all submitted timetables.")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "List of all job IDs.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = SchemaType.ARRAY, implementation = String.class))) })
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> list() {
        return jobs.keySet().stream().toList();
    }

    @Operation(summary = "Submit a timetable to start solving as soon as CPU resources are available.")
    @APIResponses(value = {
            @APIResponse(responseCode = "200",
                    description = "The job ID. Use that ID to get the solution with the other methods.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(implementation = String.class))) })
    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces(MediaType.TEXT_PLAIN)
    public String solve(TimeTable problem) {
        String jobId = UUID.randomUUID().toString();
        jobs.put(jobId, Job.newTimeTable(problem));
        solverManager.solveAndListen(jobId,
                jobId_ -> jobs.get(jobId).timeTable,
                solution -> jobs.put(jobId, Job.newTimeTable(solution)),
                (jobId_, exception) -> jobs.put(jobId, Job.error(jobId_, exception)));
        return jobId;
    }

    @Operation(
            summary = "Get the solution and score for a given job ID. This is the best solution so far, as it might still be running or not even started.")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "The best solution of the timetable so far.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = TimeTable.class))),
            @APIResponse(responseCode = "404", description = "No timetable found.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ErrorInfo.class))),
            @APIResponse(responseCode = "500", description = "Exception during solving a timetable.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ErrorInfo.class)))
    })
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{jobId}")
    public Response getTimeTable(
            @Parameter(description = "The job ID returned by the POST method.") @PathParam("jobId") String jobId,
            @QueryParam("retrieve") Retrieve retrieve) {
        retrieve = retrieve == null ? Retrieve.FULL : retrieve;
        Job job = jobs.get(jobId);
        if (job == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorInfo(jobId, "No timetable found."))
                    .build();
        }
        if (job.error != null) {
            LOGGER.error("Exception during solving jobId ({}), message ({}).", jobId, job.error.getMessage(), job.error);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorInfo(jobId, job.error.getMessage()))
                    .build();
        }
        TimeTable timeTable = job.timeTable;
        SolverStatus solverStatus = solverManager.getSolverStatus(jobId);
        if (retrieve == Retrieve.STATUS) {
            return Response.ok(new TimeTable(timeTable.getScore(), solverStatus)).build();
        }
        timeTable.setSolverStatus(solverStatus);
        return Response.ok(timeTable).build();
    }

    @Operation(
            summary = "Terminate solving for a given job ID. Returns the best solution of the timetable so far, as it might still be running or not even started.")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "The best solution of the timetable so far.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = TimeTable.class))),
            @APIResponse(responseCode = "404", description = "No timetable found.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ErrorInfo.class))),
            @APIResponse(responseCode = "500", description = "Exception during solving a timetable.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ErrorInfo.class)))
    })
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{jobId}")
    public Response terminateSolving(
            @Parameter(description = "The job ID returned by the POST method.") @PathParam("jobId") String jobId,
            @QueryParam("retrieve") Retrieve retrieve) {
        solverManager.terminateEarly(jobId);
        return getTimeTable(jobId, retrieve);
    }

    public record ErrorInfo(String jobId, String message) {
    }

    public enum Retrieve {
        STATUS,
        FULL
    }

    private record Job(String jobId, TimeTable timeTable, Throwable error) {

        static Job newTimeTable(TimeTable timeTable) {
            return new Job(null, timeTable, null);
        }

        static Job error(String jobId, Throwable error) {
            return new Job(jobId, null, error);
        }

    }
}
