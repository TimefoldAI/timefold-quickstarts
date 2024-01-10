package org.acme.kotlin.schooltimetabling.rest

import ai.timefold.solver.core.api.score.analysis.ScoreAnalysis
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore
import ai.timefold.solver.core.api.solver.ScoreAnalysisFetchPolicy
import ai.timefold.solver.core.api.solver.SolutionManager
import ai.timefold.solver.core.api.solver.SolverManager
import jakarta.inject.Inject
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.DELETE
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.PUT
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.QueryParam
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.acme.kotlin.schooltimetabling.domain.Timetable
import org.acme.kotlin.schooltimetabling.rest.exception.ErrorInfo
import org.acme.kotlin.schooltimetabling.rest.exception.TimetableSolverException
import org.eclipse.microprofile.openapi.annotations.Operation
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType
import org.eclipse.microprofile.openapi.annotations.media.Content
import org.eclipse.microprofile.openapi.annotations.media.Schema
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses
import org.eclipse.microprofile.openapi.annotations.tags.Tag
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.function.BiConsumer
import java.util.function.Consumer
import java.util.function.Function

@Tag(name = "School Timetables", description = "School timetable service assigning lessons to rooms and timeslots.")
@Path("timetables")
class TimetableResource {

    private val LOGGER: Logger = LoggerFactory.getLogger(TimetableResource::class.java)

    private final var solverManager: SolverManager<Timetable, String>?

    private final var solutionManager: SolutionManager<Timetable, HardSoftScore>?

    // TODO: Without any "time to live", the map may eventually grow out of memory.
    private val jobIdToJob: ConcurrentMap<String, Job> = ConcurrentHashMap()

    // Workaround to make Quarkus CDI happy. Do not use.
    constructor() {
        solverManager = null
        solutionManager = null
    }

    @Inject
    constructor(
        solverManager: SolverManager<Timetable, String>, solutionManager: SolutionManager<Timetable, HardSoftScore>
    ) {
        this.solverManager = solverManager
        this.solutionManager = solutionManager
    }

    @Operation(summary = "List the job IDs of all submitted timetables.")
    @APIResponses(
        value = [APIResponse(
            responseCode = "200", description = "List of all job IDs.", content = [Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = Schema(type = SchemaType.ARRAY, implementation = String::class)
            )]
        )]
    )
    @GET
    @Produces(
        MediaType.APPLICATION_JSON
    )
    fun list(): Collection<String> {
        return jobIdToJob.keys
    }

    @Operation(summary = "Submit a timetable to start solving as soon as CPU resources are available.")
    @APIResponses(
        value = [APIResponse(
            responseCode = "202",
            description = "The job ID. Use that ID to get the solution with the other methods.",
            content = [Content(mediaType = MediaType.TEXT_PLAIN, schema = Schema(implementation = String::class))]
        )]
    )
    @POST
    @Consumes(
        MediaType.APPLICATION_JSON
    )
    @Produces(MediaType.TEXT_PLAIN)
    fun solve(problem: Timetable?): String {
        val jobId = UUID.randomUUID().toString()
        jobIdToJob[jobId] = Job.ofTimetable(problem)
        solverManager!!.solveBuilder()
            .withProblemId(jobId)
            .withProblemFinder(Function<String, Timetable?> { jobId_: String? ->
                jobIdToJob[jobId]!!.timetable
            })
            .withBestSolutionConsumer(Consumer { solution: Timetable? ->
                jobIdToJob[jobId] = Job.ofTimetable(solution)
            })
            .withExceptionHandler(BiConsumer { jobId_: String?, exception: Throwable? ->
                jobIdToJob[jobId] = Job.ofException(exception)
                LOGGER.error("Failed solving jobId ({}).", jobId, exception)
            })
            .run()
        return jobId
    }

    @Operation(summary = "Submit a timetable to analyze its score.")
    @APIResponses(
        value = [APIResponse(
            responseCode = "202",
            description = "Resulting score analysis, optionally without constraint matches.",
            content = [Content(
                mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = ScoreAnalysis::class)
            )]
        )]
    )
    @PUT
    @Consumes(
        MediaType.APPLICATION_JSON
    )
    @Produces(MediaType.APPLICATION_JSON)
    @Path("analyze")
    fun analyze(
        problem: Timetable, @QueryParam("fetchPolicy") fetchPolicy: ScoreAnalysisFetchPolicy?
    ): ScoreAnalysis<HardSoftScore> {
        return if (fetchPolicy == null) solutionManager!!.analyze(problem) else solutionManager!!.analyze(
            problem, fetchPolicy
        )
    }

    @Operation(summary = "Get the solution and score for a given job ID. This is the best solution so far, as it might still be running or not even started.")
    @APIResponses(
        value = [APIResponse(
            responseCode = "200", description = "The best solution of the timetable so far.", content = [Content(
                mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = Timetable::class)
            )]
        ), APIResponse(
            responseCode = "404", description = "No timetable found.", content = [Content(
                mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = ErrorInfo::class)
            )]
        ), APIResponse(
            responseCode = "500", description = "Exception during solving a timetable.", content = [Content(
                mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = ErrorInfo::class)
            )]
        )]
    )
    @GET
    @Produces(
        MediaType.APPLICATION_JSON
    )
    @Path("{jobId}")
    fun getTimeTable(
        @Parameter(description = "The job ID returned by the POST method.") @PathParam("jobId") jobId: String
    ): Timetable? {
        val timetable: Timetable? = getTimetableAndCheckForExceptions(jobId)
        val solverStatus = solverManager!!.getSolverStatus(jobId)
        timetable?.solverStatus = solverStatus
        return timetable
    }

    @Operation(summary = "Get the timetable status and score for a given job ID.")
    @APIResponses(
        value = [
            APIResponse(
                responseCode = "200", description = "The timetable status and the best score so far.", content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = Timetable::class)
                    )
                ]
            ),
            APIResponse(
                responseCode = "404", description = "No timetable found.", content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = ErrorInfo::class)
                    )
                ]
            ),
            APIResponse(
                responseCode = "500", description = "Exception during solving a timetable.", content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = ErrorInfo::class)
                    )
                ]
            )]
    )
    @GET
    @Produces(
        MediaType.APPLICATION_JSON
    )
    @Path("{jobId}/status")
    fun getStatus(
        @Parameter(description = "The job ID returned by the POST method.") @PathParam("jobId") jobId: String
    ): Timetable {
        val timetable: Timetable = getTimetableAndCheckForExceptions(jobId)
        val solverStatus = solverManager!!.getSolverStatus(jobId)
        return Timetable(timetable.name, timetable.score, solverStatus)
    }

    private fun getTimetableAndCheckForExceptions(jobId: String): Timetable {
        val job = jobIdToJob[jobId] ?: throw TimetableSolverException(
            jobId, Response.Status.NOT_FOUND, "No timetable found."
        )
        if (job.exception != null) {
            throw TimetableSolverException(jobId, job.exception)
        }
        return job.timetable!!
    }

    @Operation(summary = "Terminate solving for a given job ID. Returns the best solution of the timetable so far, as it might still be running or not even started.")
    @APIResponses(
        value = [
            APIResponse(
                responseCode = "200", description = "The best solution of the timetable so far.", content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = Timetable::class)
                    )
                ]
            ),
            APIResponse(
                responseCode = "404", description = "No timetable found.", content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = ErrorInfo::class)
                    )
                ]
            ),
            APIResponse(
                responseCode = "500", description = "Exception during solving a timetable.", content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = ErrorInfo::class)
                    )
                ]
            )]
    )
    @DELETE
    @Produces(
        MediaType.APPLICATION_JSON
    )
    @Path("{jobId}")
    fun terminateSolving(
        @Parameter(description = "The job ID returned by the POST method.") @PathParam("jobId") jobId: String
    ): Timetable? {
        // TODO: Replace with .terminateEarlyAndWait(... [, timeout]); see https://github.com/TimefoldAI/timefold-solver/issues/77
        solverManager!!.terminateEarly(jobId)
        return getTimeTable(jobId)
    }


    data class Job(val timetable: Timetable?, val exception: Throwable?) {
        companion object {
            fun ofTimetable(timetable: Timetable?): Job {
                return Job(timetable, null)
            }

            fun ofException(error: Throwable?): Job {
                return Job(null, error)
            }
        }
    }
}