package org.acme.callcenter.solver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import jakarta.inject.Inject;

import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.solver.SolverManager;
import ai.timefold.solver.core.config.localsearch.LocalSearchPhaseConfig;
import ai.timefold.solver.core.config.solver.SolverConfig;
import ai.timefold.solver.core.config.solver.SolverManagerConfig;
import ai.timefold.solver.core.config.solver.termination.TerminationConfig;

import org.acme.callcenter.data.DataGenerator;
import org.acme.callcenter.domain.Agent;
import org.acme.callcenter.domain.Call;
import org.acme.callcenter.domain.CallCenter;
import org.acme.callcenter.domain.PreviousCallOrAgent;
import org.acme.callcenter.domain.Skill;
import org.acme.callcenter.service.SolverService;
import org.acme.callcenter.solver.change.PinCallProblemChange;
import org.acme.callcenter.solver.change.RemoveCallProblemChange;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class SolverServiceTest {

    @Inject
    DataGenerator dataGenerator;

    @Inject
    SolverService solverService;

    @AfterEach
    void tearDown() {
        solverService.stopSolving();
    }

    @Test
    @Timeout(60)
    void addCall() {
        Call call1 = new Call("1", "123-456-7891", Skill.ENGLISH, Skill.CAR_INSURANCE);
        Call call2 = new Call("2", "123-456-7892", Skill.ENGLISH, Skill.CAR_INSURANCE);
        CallCenter bestSolution = solve(dataGenerator.generateCallCenter(), () -> solverService.addCall(call1),
                () -> solverService.addCall(call2));

        Agent agentWithCalls = getFirstAgentWithCallOrFail(bestSolution);

        assertThat(agentWithCalls.getAssignedCalls())
                .containsExactlyInAnyOrder(call1, call2);
        assertThat(agentWithCalls.getSkills()).contains(Skill.ENGLISH, Skill.CAR_INSURANCE);
    }

    @Test
    @Timeout(60)
    void prolongCall() {
        CallCenter inputProblem = dataGenerator.generateCallCenter();
        Call call1 = new Call("1", "123-456-7891", Skill.ENGLISH, Skill.CAR_INSURANCE);
        Call call2 = new Call("2", "123-456-7892", Skill.ENGLISH, Skill.CAR_INSURANCE);
        inputProblem.getCalls().add(call1);
        inputProblem.getCalls().add(call2);

        CallCenter bestSolution = solve(inputProblem, () -> solverService.prolongCall(call1.getId()));

        Agent agentWithCalls = getFirstAgentWithCallOrFail(bestSolution);
        assertThat(agentWithCalls.getSkills()).contains(Skill.ENGLISH, Skill.CAR_INSURANCE);

        assertThat(agentWithCalls.getAssignedCalls()).hasSize(2);
        Call prolongedCall = agentWithCalls.getAssignedCalls().stream()
                .filter(call -> call.getId().equals(call1.getId()))
                .findFirst()
                .orElseGet(() -> Assertions.fail("The expected prolonged call has not been found."));
        assertThat(prolongedCall.getDuration()).hasMinutes(1L);
        assertThat(prolongedCall.getDurationTillPickUp()).hasMinutes(1L);
    }

    @Test
    @Timeout(60)
    void removeCall() {
        CallCenter inputProblem = dataGenerator.generateCallCenter();
        Call call1 = new Call("1", "123-456-7891", Skill.ENGLISH, Skill.CAR_INSURANCE);
        Call call2 = new Call("2", "123-456-7892", Skill.ENGLISH, Skill.CAR_INSURANCE);
        inputProblem.getCalls().add(call1);
        inputProblem.getCalls().add(call2);

        CallCenter bestSolution = solve(inputProblem, () -> solverService.removeCall(call1.getId()));

        Agent agentWithCalls = getFirstAgentWithCallOrFail(bestSolution);
        assertThat(agentWithCalls.getSkills()).contains(Skill.ENGLISH, Skill.CAR_INSURANCE);

        assertThat(agentWithCalls.getAssignedCalls()).hasSize(1);
        Call call = agentWithCalls.getAssignedCalls().get(0);
        assertThat(call.getId()).isEqualTo(call2.getId());
    }

    @Test
    @Timeout(60)
    void removeCallWithInfeasibleInitialSolution() {
        // Invalid initial solution
        CallCenter inputProblem = dataGenerator.generateCallCenter();
        Call call1 = new Call("1", "123-456-7891", Skill.ENGLISH, Skill.CAR_INSURANCE);
        Call call2 = new Call("2", "123-456-7892", Skill.ENGLISH, Skill.CAR_INSURANCE);
        inputProblem.getCalls().add(call1);
        inputProblem.getCalls().add(call2);
        inputProblem.getAgents().get(0).setNextCall(call2);
        inputProblem.getAgents().get(1).setNextCall(call1);
        call1.setAgent(inputProblem.getAgents().get(1));
        call1.setPreviousCallOrAgent(inputProblem.getAgents().get(1));
        call1.setEstimatedWaiting(Duration.ofSeconds(1));
        call2.setAgent(inputProblem.getAgents().get(0));
        call2.setPreviousCallOrAgent(inputProblem.getAgents().get(0));
        call2.setEstimatedWaiting(Duration.ofSeconds(1));
        inputProblem.setScore(HardSoftScore.ofUninitialized(0, -1, 0));

        // We have an initial solution, then we run only LocalSearchPhaseConfig
        SolverConfig solverConfig = new SolverConfig()
                .withSolutionClass(CallCenter.class)
                .withEntityClasses(Call.class, PreviousCallOrAgent.class)
                .withConstraintProviderClass(CallCenterConstraintsProvider.class)
                .withPhases(new LocalSearchPhaseConfig())
                .withTerminationConfig(new TerminationConfig().withSpentLimit(Duration.ofSeconds(30L)));

        // Create a custom manager
        try (SolverManager<CallCenter, Long> localSearchSolverManager =
                SolverManager.create(solverConfig, new SolverManagerConfig())) {
            AtomicReference<CallCenter> bestSolutionRef = new AtomicReference<>();

            localSearchSolverManager.solveBuilder()
                    .withProblemId(1L)
                    .withProblemFinder(id -> inputProblem)
                    .withBestSolutionConsumer(bestSolution -> {
                        if (bestSolution.isFeasible()) {
                            bestSolutionRef.set(bestSolution);
                            bestSolution.getCalls().stream()
                                    .filter(call -> !call.isPinned()
                                            && call.getPreviousCallOrAgent() != null
                                            && call.getPreviousCallOrAgent() instanceof Agent)
                                    .map(PinCallProblemChange::new)
                                    .forEach(problemChange -> localSearchSolverManager.addProblemChange(1L, problemChange));
                        }
                    }).withExceptionHandler((id, error) -> {
                    })
                    .run();

            // Remove the call 1
            localSearchSolverManager.addProblemChange(1L, new RemoveCallProblemChange("1"));

            // Wait for the local search
            await()
                    .atMost(Duration.ofSeconds(30))
                    .pollInterval(Duration.ofMillis(100L))
                    .until(() -> bestSolutionRef.get() != null);

            Agent agentWithCalls = getFirstAgentWithCallOrFail(bestSolutionRef.get());
            assertThat(agentWithCalls.getSkills()).contains(Skill.ENGLISH, Skill.CAR_INSURANCE);
        }
    }

    @SafeVarargs
    private CallCenter solve(CallCenter inputProblem, Supplier<CompletableFuture<Void>>... problemChanges) {
        AtomicReference<Throwable> errorDuringSolving = new AtomicReference<>();
        AtomicReference<CallCenter> bestSolution = new AtomicReference<>();
        solverService.startSolving(inputProblem, bestSolution::set, errorDuringSolving::set);

        CountDownLatch allChangesProcessed = new CountDownLatch(problemChanges.length);
        for (Supplier<CompletableFuture<Void>> problemChange : problemChanges) {
            problemChange.get().thenRun(() -> allChangesProcessed.countDown());
        }
        try {
            allChangesProcessed.await(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            throw new RuntimeException("Waiting for problem changes in progress has been interrupted.", e);
        }

        if (errorDuringSolving.get() != null) {
            throw new IllegalStateException("Exception during solving", errorDuringSolving.get());
        }

        // We wait for the solver to find a feasible solution
        await()
                .atMost(Duration.ofSeconds(30))
                .pollInterval(Duration.ofMillis(100L))
                .until(() -> bestSolution.get() != null);

        return bestSolution.get();
    }

    private Agent getFirstAgentWithCallOrFail(CallCenter callCenter) {
        return callCenter.getAgents().stream()
                .filter(agent -> !agent.getAssignedCalls().isEmpty())
                .findFirst()
                .orElseGet(() -> Assertions.fail("There is no agent with assigned calls."));
    }
}
