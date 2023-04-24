package org.acme.schooltimetabling.messaging;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.acme.schooltimetabling.domain.TimeTable;
import org.acme.schooltimetabling.message.SolverRequest;
import org.acme.schooltimetabling.message.SolverResponse;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import ai.timefold.solver.core.api.solver.Solver;
import ai.timefold.solver.core.api.solver.SolverFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@ApplicationScoped
public class TimeTableMessagingHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(TimeTableMessagingHandler.class);
    public static final String SOLVER_REQUEST_CHANNEL = "solver_request";
    public static final String SOLVER_RESPONSE_CHANNEL = "solver_response";

    Solver<TimeTable> solver;

    @Inject
    ObjectMapper objectMapper;

    @Inject
    @Channel(SOLVER_RESPONSE_CHANNEL)
    Emitter<String> solverResponseEmitter;

    @Inject
    TimeTableMessagingHandler(SolverFactory<TimeTable> solverFactory) {
        solver = solverFactory.buildSolver();
    }

    @Incoming(SOLVER_REQUEST_CHANNEL)
    public CompletionStage<Void> solve(Message<String> solverRequestMessage) {
        return CompletableFuture.runAsync(() -> {
            SolverRequest solverRequest;
            try {
                solverRequest = objectMapper.readValue(solverRequestMessage.getPayload(), SolverRequest.class);
            } catch (Throwable throwable) {
                LOGGER.warn("Unable to deserialize solver request from JSON.", throwable);
                /* Usually a bad request, which should be immediately rejected. No error response can be sent back
                   as the problemId is unknown. Such a NACKed message is redirected to the DLQ (Dead letter queue).
                   Catching the Throwable to make sure no unchecked exceptions are missed. */
                solverRequestMessage.nack(throwable);
                return;
            }

            TimeTable solution;
            try {
                solution = solver.solve(solverRequest.getTimeTable());
                replySuccess(solverRequestMessage, solverRequest.getProblemId(), solution);
            } catch (Throwable throwable) {
                LOGGER.warn("Error during processing a solver request ({}).", solverRequest.getProblemId(), throwable);
                replyFailure(solverRequestMessage, solverRequest.getProblemId(), throwable);
            }
        });
    }

    private void replySuccess(Message<String> solverRequestMessage, Long problemId, TimeTable solution) {
        SolverResponse solverResponse = new SolverResponse(problemId, solution);
        reply(solverRequestMessage, solverResponse, exception -> replyFailure(solverRequestMessage, problemId, exception));
    }

    private void replyFailure(Message<String> solverRequestMessage, Long problemId, Throwable throwable) {
        SolverResponse solverResponse =
                new SolverResponse(problemId,
                        new SolverResponse.ErrorInfo(throwable.getClass().getName(), throwable.getMessage()));
        reply(solverRequestMessage, solverResponse, serializationException -> {
            throw new IllegalStateException("Unable to serialize error response.", serializationException);
        });
    }

    private void reply(Message<String> solverRequestMessage, SolverResponse solverResponse,
            Consumer<? super Exception> onFailure) {
        try {
            String jsonResponse = objectMapper.writeValueAsString(solverResponse);
            solverResponseEmitter.send(jsonResponse).thenAccept(x -> solverRequestMessage.ack());
        } catch (JsonProcessingException ex) {
            onFailure.accept(ex);
        }
    }
}
