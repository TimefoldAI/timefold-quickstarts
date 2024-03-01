package org.acme.maintenancescheduling.rest;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
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

import org.acme.maintenancescheduling.domain.MaintenanceSchedule;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("schedules")
public class MaintenanceScheduleResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(MaintenanceScheduleResource.class);

    public static final String SINGLETON_SCHEDULE_ID = "1";

    SolverManager<MaintenanceSchedule, String> solverManager;
    SolutionManager<MaintenanceSchedule, HardSoftScore> solutionManager;

    // TODO: Without any "time to live", the map may eventually grow out of memory.
    private final ConcurrentMap<String, Job> jobIdToJob = new ConcurrentHashMap<>();

    @Inject
    public MaintenanceScheduleResource(SolverManager<MaintenanceSchedule, String> solverManager,
            SolutionManager<MaintenanceSchedule, HardSoftScore> solutionManager) {
        this.solverManager = solverManager;
        this.solutionManager = solutionManager;
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
    public String solve(MaintenanceSchedule problem) {
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
            @APIResponse(responseCode = "200", description = "The best solution of the timetable so far.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = Timetable.class))),
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
    public MaintenanceSchedule getSchedule(
            @Parameter(description = "The job ID returned by the POST method.") @PathParam("jobId") String jobId) {
        MaintenanceSchedule schedule = getMaintenanceScheduleAndCheckForExceptions(jobId);
        SolverStatus solverStatus = solverManager.getSolverStatus(jobId);
        schedule.setSolverStatus(solverStatus);
        return schedule;
    }

    //
    //    // To try, open http://localhost:8080/schedule
    //    @GET
    //    public MaintenanceSchedule getSchedule() {
    //        // Get the solver status before loading the solution
    //        // to avoid the race condition that the solver terminates between them
    //        SolverStatus solverStatus = getSolverStatus();
    //        MaintenanceSchedule solution = findById(SINGLETON_SCHEDULE_ID);
    //        solutionManager.update(solution); // Sets the score
    //        solution.setSolverStatus(solverStatus);
    //        return solution;
    //    }
    //
    //    public SolverStatus getSolverStatus() {
    //        return solverManager.getSolverStatus(SINGLETON_SCHEDULE_ID);
    //    }
    //
    //    @POST
    //    @Path("solve")
    //    public void solve() {
    //        solverManager.solveBuilder()
    //                .withProblemId(SINGLETON_SCHEDULE_ID)
    //                .withProblemFinder(this::findById)
    //                .withBestSolutionConsumer(this::save)
    //                .run();
    //    }
    //
    //    @POST
    //    @Path("stopSolving")
    //    public void stopSolving() {
    //        solverManager.terminateEarly(SINGLETON_SCHEDULE_ID);
    //    }
    //
    //    @Transactional
    //    protected MaintenanceSchedule findById(String id) {
    //        if (!SINGLETON_SCHEDULE_ID.equals(id)) {
    //            throw new IllegalStateException("There is no schedule with id (" + id + ").");
    //        }
    //        return new MaintenanceSchedule(
    //                workCalendarRepository.listAll().get(0),
    //                crewRepository.listAll(Sort.by("name").and("id")),
    //                jobRepository.listAll(Sort.by("dueDate").and("readyDate").and("name").and("id")));
    //    }
    //
    //    @Transactional
    //    protected void save(MaintenanceSchedule schedule) {
    //        for (Job job : schedule.getJobs()) {
    //            // TODO this is awfully naive: optimistic locking causes issues if called by the SolverManager
    //            Job attachedJob = jobRepository.findById(job.getId());
    //            attachedJob.setCrew(job.getCrew());
    //            attachedJob.setStartDate(job.getStartDate());
    //            attachedJob.setEndDate(job.getEndDate());
    //        }
    //    }
    private MaintenanceSchedule getMaintenanceScheduleAndCheckForExceptions(String jobId) {
        Job job = jobIdToJob.get(jobId);
        if (job == null) {
            throw new TimetableSolverException(jobId, Response.Status.NOT_FOUND, "No timetable found.");
        }
        if (job.exception != null) {
            throw new TimetableSolverException(jobId, job.exception);
        }
        return job.schedule;
    }

    private record Job(MaintenanceSchedule schedule, Throwable exception) {

        static Job ofSchedule(MaintenanceSchedule schedule) {
            return new Job(schedule, null);
        }

        static Job ofException(Throwable error) {
            return new Job(null, error);
        }
    }
}
