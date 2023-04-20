package org.acme.schooltimetabling;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.JMSProducer;
import javax.jms.Session;

import org.acme.schooltimetabling.domain.Lesson;
import org.acme.schooltimetabling.domain.Room;
import org.acme.schooltimetabling.domain.TimeTable;
import org.acme.schooltimetabling.domain.Timeslot;
import org.acme.schooltimetabling.message.SolverRequest;
import org.acme.schooltimetabling.message.SolverResponse;
import org.acme.schooltimetabling.messaging.TimeTableMessagingHandler;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import ai.timefold.solver.jackson.api.OptaPlannerJacksonModule;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@QuarkusTestResource(ActiveMQEmbeddedBroker.class)
public class TimeTableMessagingHandlerTest {

    private static final long TEST_TIMEOUT_SECONDS = 60L;
    private static final int MESSAGE_RECEIVE_TIMEOUT_SECONDS = 10;

    private ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");

    // Native test does not support @Inject.
    private ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .registerModule(OptaPlannerJacksonModule.createModule());

    @Test
    @Timeout(TEST_TIMEOUT_SECONDS)
    void solve() {
        long problemId = 1L;
        TimeTable unsolvedTimeTable = createTestTimeTable();
        sendSolverRequest(new SolverRequest(problemId, unsolvedTimeTable));
        SolverResponse solverResponse = receiveSolverResponse(MESSAGE_RECEIVE_TIMEOUT_SECONDS);

        assertThat(SolverResponse.ResponseStatus.SUCCESS == solverResponse.getResponseStatus());
        assertThat(problemId == solverResponse.getProblemId());
        assertThat(solverResponse.getTimeTable().getLessonList()).hasSameSizeAs(unsolvedTimeTable.getLessonList());
        assertThat(solverResponse.getTimeTable().getScore().isFeasible()).isTrue();
    }

    @Test
    @Timeout(TEST_TIMEOUT_SECONDS)
    void solvingThrowsException() {
        long problemId = 10L;

        // OptaPlanner doesn't tolerate a null planningId.
        TimeTable timeTableWithIncorrectLesson = new TimeTable(
                Arrays.asList(new Timeslot(1L, DayOfWeek.MONDAY, LocalTime.NOON, LocalTime.NOON.plusMinutes(30))),
                Arrays.asList(new Room(1L, "room-A")),
                Arrays.asList(new Lesson(null, "Math", "A. Touring", "10th grade")));

        sendSolverRequest(new SolverRequest(problemId, timeTableWithIncorrectLesson));

        SolverResponse solverResponse = receiveSolverResponse(MESSAGE_RECEIVE_TIMEOUT_SECONDS);
        assertThat(SolverResponse.ResponseStatus.FAILURE == solverResponse.getResponseStatus());
        assertThat(problemId == solverResponse.getProblemId());
        assertThat(solverResponse.getErrorInfo().getExceptionClassName()).isEqualTo(IllegalArgumentException.class.getName());
        assertThat(solverResponse.getErrorInfo().getExceptionMessage()).startsWith("The planningId (null)");
    }

    @Test
    @Timeout(TEST_TIMEOUT_SECONDS)
    void badRequest_DLQ() {
        final String wrongMessage = "Bad request!";
        sendMessage(wrongMessage);
        String messageFromDLQ = receiveMessage("DLQ", 10);
        assertThat(messageFromDLQ).isEqualTo(wrongMessage);
    }

    private SolverResponse receiveSolverResponse(int timeoutSeconds) {
        String solverResponsePayload = receiveMessage(TimeTableMessagingHandler.SOLVER_RESPONSE_CHANNEL, timeoutSeconds);
        try {
            return objectMapper.readValue(solverResponsePayload, SolverResponse.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }

    private String receiveMessage(String queueName, int timeoutSeconds) {
        try (JMSContext context = connectionFactory.createContext("quarkus", "quarkus", Session.AUTO_ACKNOWLEDGE)) {
            JMSConsumer consumer = context.createConsumer(context.createQueue(queueName));
            javax.jms.Message message = consumer.receive(timeoutSeconds * 1_000);
            return message.getBody(String.class);
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendSolverRequest(SolverRequest solverRequest) {
        try {
            String message = objectMapper.writeValueAsString(solverRequest);
            sendMessage(message);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendMessage(String message) {
        try (JMSContext context = connectionFactory.createContext("quarkus", "quarkus", Session.AUTO_ACKNOWLEDGE)) {
            JMSProducer producer = context.createProducer();
            Destination solverRequestQueue = context.createQueue(TimeTableMessagingHandler.SOLVER_REQUEST_CHANNEL);
            producer.send(solverRequestQueue, message);
        }
    }

    private TimeTable createTestTimeTable() {
        List<Room> rooms = Collections.singletonList(new Room(1L, "room-A"));
        List<Timeslot> timeslots = Arrays.asList(
                new Timeslot(1L, DayOfWeek.MONDAY, LocalTime.NOON, LocalTime.NOON.plusMinutes(30)),
                new Timeslot(2L, DayOfWeek.TUESDAY, LocalTime.NOON, LocalTime.NOON.plusMinutes(30)),
                new Timeslot(3L, DayOfWeek.WEDNESDAY, LocalTime.NOON, LocalTime.NOON.plusMinutes(30)));
        List<Lesson> lessons = Arrays.asList(
                new Lesson(1L, "Math", "A. Touring", "10th grade"),
                new Lesson(2L, "Biology", "C. Darwin", "11th grade"),
                new Lesson(3L, "Physics", "M. Curie", "12th grade"));
        return new TimeTable(timeslots, rooms, lessons);
    }
}
