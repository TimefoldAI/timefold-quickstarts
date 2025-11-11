package org.acme.facilitylocation.rest;

import ai.timefold.solver.core.api.score.analysis.ScoreAnalysis;
import ai.timefold.solver.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
import ai.timefold.solver.core.api.solver.ScoreAnalysisFetchPolicy;
import ai.timefold.solver.core.api.solver.SolutionManager;
import ai.timefold.solver.core.api.solver.SolverManager;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.acme.facilitylocation.domain.FacilityLocationProblem;
import org.acme.facilitylocation.persistence.FacilityLocationProblemRepository;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Path("/flp")
public class SolverResource {

    private static final String PROBLEM_ID = "1";

    private final AtomicReference<Throwable> solverError = new AtomicReference<>();

    private final FacilityLocationProblemRepository repository;
    private final SolverManager<FacilityLocationProblem, String> solverManager;
    private final SolutionManager<FacilityLocationProblem, HardSoftLongScore> solutionManager;

    public SolverResource(FacilityLocationProblemRepository repository,
            SolverManager<FacilityLocationProblem, String> solverManager,
            SolutionManager<FacilityLocationProblem, HardSoftLongScore> solutionManager) {
        this.repository = repository;
        this.solverManager = solverManager;
        this.solutionManager = solutionManager;
    }

    private Status statusFromSolution(FacilityLocationProblem solution) {
        return new Status(solution,
                solutionManager.explain(solution).getSummary(),
                solverManager.getSolverStatus(PROBLEM_ID));
    }

    @GET
    @Path("status")
    public Status status() {
        Optional.ofNullable(solverError.getAndSet(null)).ifPresent(throwable -> {
            throw new RuntimeException("Solver failed", throwable);
        });
        return statusFromSolution(repository.solution().orElse(FacilityLocationProblem.empty()));
    }

    @POST
    @Path("solve")
    public void solve() {
        Optional<FacilityLocationProblem> maybeSolution = repository.solution();
        maybeSolution.ifPresent(facilityLocationProblem -> solverManager.solveBuilder()
                .withProblemId(PROBLEM_ID)
                .withProblemFinder(id -> facilityLocationProblem)
                .withBestSolutionEventConsumer(event -> repository.update(event.solution()))
                .withExceptionHandler((problemId, throwable) -> solverError.set(throwable))
                .run());
    }

    @PUT
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces(MediaType.APPLICATION_JSON)
    @Path("analyze")
    public ScoreAnalysis<HardSoftLongScore> analyze(@QueryParam("fetchPolicy") ScoreAnalysisFetchPolicy fetchPolicy) {
        FacilityLocationProblem problem = repository.solution().get();
        return fetchPolicy == null ? solutionManager.analyze(problem) : solutionManager.analyze(problem, fetchPolicy);
    }

    @POST
    @Path("stopSolving")
    public void stopSolving() {
        solverManager.terminateEarly(PROBLEM_ID);
    }
}
