package org.acme.schooltimetabling.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.acme.schooltimetabling.domain.TimeTable;
import org.acme.schooltimetabling.message.SolverRequest;
import org.acme.schooltimetabling.message.SolverResponse;
import org.acme.schooltimetabling.persistence.TimeTableRepository;
import org.eclipse.microprofile.reactive.messaging.Acknowledgment;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import ai.timefold.solver.core.api.solver.SolverStatus;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Path("timeTable")
public class TimeTableResource {

    public static final Long SINGLETON_TIME_TABLE_ID = 1L;

    @Inject
    TimeTableRepository timeTableRepository;

    @Inject
    @Channel("solver_request")
    Emitter<String> solverRequestEmitter;

    @Inject
    ObjectMapper objectMapper;

    @Incoming("solver_response")
    @Acknowledgment(Acknowledgment.Strategy.POST_PROCESSING)
    public void process(String solverResponseMessage) {
        SolverResponse solverResponse;
        try {
            solverResponse = objectMapper.readValue(solverResponseMessage, SolverResponse.class);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Unable to deserialize the solver response.", ex);
        }

        if (solverResponse.isSuccess()) {
            TimeTable timeTable = solverResponse.getTimeTable();
            timeTable.setSolverStatus(SolverStatus.NOT_SOLVING);
            timeTableRepository.update(timeTable);
        } else {
            timeTableRepository.get().setSolverStatus(SolverStatus.NOT_SOLVING);
            throw new IllegalStateException("Solving failed with exception class ("
                    + solverResponse.getErrorInfo().getExceptionClassName()
                    + ") and message (" + solverResponse.getErrorInfo().getExceptionMessage() + ").");
        }
    }

    // To try, open http://localhost:8080/timeTable
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public TimeTable getTimeTable() {
        TimeTable timeTable = timeTableRepository.get();
        return timeTable;
    }

    @POST
    @Path("solve")
    public void solve() throws JsonProcessingException {
        TimeTable timeTable = timeTableRepository.get();
        timeTable.setSolverStatus(SolverStatus.SOLVING_SCHEDULED);

        SolverRequest solverRequest = new SolverRequest(SINGLETON_TIME_TABLE_ID, timeTable);
        String solverRequestJson = objectMapper.writeValueAsString(solverRequest);
        solverRequestEmitter.send(solverRequestJson);
    }
}
