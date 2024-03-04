package org.acme.employeescheduling.rest;

import java.time.LocalDate;
import java.util.Collection;
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

import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.solver.SolutionManager;
import ai.timefold.solver.core.api.solver.SolverManager;
import ai.timefold.solver.core.api.solver.SolverStatus;

import org.acme.employeescheduling.domain.EmployeeSchedule;
import org.acme.employeescheduling.domain.ScheduleState;
import org.acme.employeescheduling.rest.exception.EmployeeScheduleSolverException;
import org.acme.employeescheduling.rest.exception.ErrorInfo;
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

@Tag(name = "Employee Schedules", description = "Employee Schedules service for assigning employees to shifts.")
@Path("schedules")
public class EmployeeScheduleResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmployeeScheduleResource.class);

    public static final String SINGLETON_SCHEDULE_ID = "1";

    private final DemoDataGenerator dataGenerator;

    SolverManager<EmployeeSchedule, String> solverManager;
    SolutionManager<EmployeeSchedule, HardSoftScore> solutionManager;

    // TODO: Without any "time to live", the map may eventually grow out of memory.
    private final ConcurrentMap<String, Job> jobIdToJob = new ConcurrentHashMap<>();

    @Inject
    public EmployeeScheduleResource(SolverManager<EmployeeSchedule, String> solverManager,
            SolutionManager<EmployeeSchedule, HardSoftScore> solutionManager, DemoDataGenerator dataGenerator) {
        this.solverManager = solverManager;
        this.solutionManager = solutionManager;
        this.dataGenerator = dataGenerator;
    }

    @Operation(summary = "List the job IDs of all submitted schedules.")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "List of all job IDs.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = SchemaType.ARRAY, implementation = String.class))) })
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<String> list() {
        return jobIdToJob.keySet();
    }

    @Operation(summary = "Submit a schedule to start solving as soon as CPU resources are available.")
    @APIResponses(value = {
            @APIResponse(responseCode = "202",
                    description = "The job ID. Use that ID to get the solution with the other methods.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(implementation = String.class))) })
    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces(MediaType.TEXT_PLAIN)
    public String solve(EmployeeSchedule problem) {
        String jobId = UUID.randomUUID().toString();
        jobIdToJob.put(jobId, Job.ofSchedule(problem));
        solverManager.solveBuilder()
                .withProblemId(jobId)
                .withProblemFinder(jobId_ -> jobIdToJob.get(jobId).schedule)
                .withBestSolutionConsumer(solution -> jobIdToJob.put(jobId, Job.ofSchedule(solution)))
                .withExceptionHandler((jobId_, exception) -> {
                    jobIdToJob.put(jobId, Job.ofException(exception));
                    LOGGER.error("Failed solving jobId ({}).", jobId, exception);
                })
                .run();
        return jobId;
    }

    @Operation(
            summary = "Get the solution and score for a given job ID. This is the best solution so far, as it might still be running or not even started.")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "The best solution of the schedule so far.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = EmployeeSchedule.class))),
            @APIResponse(responseCode = "404", description = "No schedule found.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ErrorInfo.class))),
            @APIResponse(responseCode = "500", description = "Exception during solving a schedule.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ErrorInfo.class)))
    })
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{jobId}")
    public EmployeeSchedule getEmployeeSchedule(
            @Parameter(description = "The job ID returned by the POST method.") @PathParam("jobId") String jobId) {
        EmployeeSchedule schedule = getEmployeeScheduleAndCheckForExceptions(jobId);
        SolverStatus solverStatus = solverManager.getSolverStatus(jobId);
        schedule.setSolverStatus(solverStatus);
        return schedule;
    }

    private EmployeeSchedule getEmployeeScheduleAndCheckForExceptions(String jobId) {
        Job job = jobIdToJob.get(jobId);
        if (job == null) {
            throw new EmployeeScheduleSolverException(jobId, Response.Status.NOT_FOUND, "No schedule found.");
        }
        if (job.exception != null) {
            throw new EmployeeScheduleSolverException(jobId, job.exception);
        }
        return job.schedule;
    }

    @Operation(
            summary = "Terminate solving for a given job ID. Returns the best solution of the schedule so far, as it might still be running or not even started.")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "The best solution of the schedule so far.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = EmployeeSchedule.class))),
            @APIResponse(responseCode = "404", description = "No schedule found.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ErrorInfo.class))),
            @APIResponse(responseCode = "500", description = "Exception during solving a schedule.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ErrorInfo.class)))
    })
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{jobId}")
    public EmployeeSchedule terminateSolving(
            @Parameter(description = "The job ID returned by the POST method.") @PathParam("jobId") String jobId) {
        // TODO: Replace with .terminateEarlyAndWait(... [, timeout]); see https://github.com/TimefoldAI/timefold-solver/issues/77
        solverManager.terminateEarly(jobId);
        return getEmployeeSchedule(jobId);
    }

    @Operation(
            summary = "Get the schedule status and score for a given job ID.")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "The schedule status and the best score so far.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = EmployeeSchedule.class))),
            @APIResponse(responseCode = "404", description = "No schedule found.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ErrorInfo.class))),
            @APIResponse(responseCode = "500", description = "Exception during solving a schedule.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ErrorInfo.class)))
    })
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{jobId}/status")
    public EmployeeSchedule getStatus(
            @Parameter(description = "The job ID returned by the POST method.") @PathParam("jobId") String jobId) {
        EmployeeSchedule schedule = getEmployeeScheduleAndCheckForExceptions(jobId);
        SolverStatus solverStatus = solverManager.getSolverStatus(jobId);
        return new EmployeeSchedule(schedule.getScore(), solverStatus);
    }

    @Operation(
            summary = "Publish a schedule.")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "The schedule updated.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = EmployeeSchedule.class))),
            @APIResponse(responseCode = "404", description = "No schedule found.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ErrorInfo.class))),
            @APIResponse(responseCode = "500", description = "Exception while trying to publish the schedule.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ErrorInfo.class)))
    })
    @POST
    @Path("{jobId}/publish")
    @Produces(MediaType.APPLICATION_JSON)
    public void
            publish(@Parameter(description = "The job ID returned by the POST method.") @PathParam("jobId") String jobId) {
        if (!getStatus(jobId).getSolverStatus().equals(SolverStatus.NOT_SOLVING)) {
            throw new IllegalStateException("Cannot publish a schedule while solving is in progress.");
        }
        EmployeeSchedule schedule = getEmployeeScheduleAndCheckForExceptions(jobId);
        ScheduleState scheduleState = schedule.getScheduleState();
        LocalDate newHistoricDate = scheduleState.getFirstDraftDate();
        LocalDate newDraftDate = scheduleState.getFirstDraftDate().plusDays(scheduleState.getPublishLength());

        scheduleState.setLastHistoricDate(newHistoricDate);
        scheduleState.setFirstDraftDate(newDraftDate);

        dataGenerator.addDraftShifts(schedule);
        jobIdToJob.put(jobId, Job.ofSchedule(schedule));
    }

    private record Job(EmployeeSchedule schedule, Throwable exception) {

        static Job ofSchedule(EmployeeSchedule schedule) {
            return new Job(schedule, null);
        }

        static Job ofException(Throwable error) {
            return new Job(null, error);
        }
    }
}
