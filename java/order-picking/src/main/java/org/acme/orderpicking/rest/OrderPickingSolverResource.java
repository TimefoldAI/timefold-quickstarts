package org.acme.orderpicking.rest;

import ai.timefold.solver.core.api.score.analysis.ScoreAnalysis;
import ai.timefold.solver.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
import ai.timefold.solver.core.api.solver.ScoreAnalysisFetchPolicy;
import ai.timefold.solver.core.api.solver.SolutionManager;
import ai.timefold.solver.core.api.solver.SolverManager;
import ai.timefold.solver.core.api.solver.SolverStatus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.acme.orderpicking.domain.OrderPickingPlanning;
import org.acme.orderpicking.domain.OrderPickingSolution;
import org.acme.orderpicking.persistence.OrderPickingRepository;

import java.util.concurrent.atomic.AtomicBoolean;

@Path("orderPicking")
@ApplicationScoped
public class OrderPickingSolverResource {

    private static final String PROBLEM_ID = "1";
    private final AtomicBoolean solverWasNeverStarted = new AtomicBoolean(true);

    private SolverManager<OrderPickingSolution, String> solverManager;
    private SolutionManager<OrderPickingSolution, HardSoftLongScore> solutionManager;
    private OrderPickingRepository orderPickingRepository;

    @Inject
    public OrderPickingSolverResource(SolverManager<OrderPickingSolution, String> solverManager,
            SolutionManager<OrderPickingSolution, HardSoftLongScore> solutionManager,
            OrderPickingRepository orderPickingRepository) {
        this.solverManager = solverManager;
        this.solutionManager = solutionManager;
        this.orderPickingRepository = orderPickingRepository;
    }

    @GET
    public OrderPickingPlanning getBestSolution() {
        OrderPickingSolution solution = orderPickingRepository.find();
        SolverStatus solverStatus = solverManager.getSolverStatus(PROBLEM_ID);
        return new OrderPickingPlanning(solverStatus, solution, solverWasNeverStarted.get());
    }

    @POST
    @Path("solve")
    public void solve() {
        solverWasNeverStarted.set(false);
        solverManager.solveBuilder()
                .withProblemId(PROBLEM_ID)
                .withProblemFinder((problemId) -> orderPickingRepository.find())
                .withBestSolutionEventConsumer(event -> orderPickingRepository.save(event.solution()))
                .run();
    }

    @PUT
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces(MediaType.APPLICATION_JSON)
    @Path("analyze")
    public ScoreAnalysis<HardSoftLongScore> analyze(@QueryParam("fetchPolicy") ScoreAnalysisFetchPolicy fetchPolicy) {
        OrderPickingSolution problem = orderPickingRepository.find();
        return fetchPolicy == null ? solutionManager.analyze(problem) : solutionManager.analyze(problem, fetchPolicy);
    }

    @POST
    @Path("stopSolving")
    public void stopSolving() {
        solverManager.terminateEarly(PROBLEM_ID);
    }
}