package org.acme.schooltimetabling;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

import io.smallrye.reactive.messaging.memory.InMemoryConnector;
import jakarta.enterprise.inject.Any;
import jakarta.inject.Inject;

import org.acme.schooltimetabling.domain.Lesson;
import org.acme.schooltimetabling.domain.Room;
import org.acme.schooltimetabling.domain.TimeTable;
import org.acme.schooltimetabling.domain.Timeslot;
import org.acme.schooltimetabling.message.SolverRequest;
import org.acme.schooltimetabling.message.SolverResponse;
import org.acme.schooltimetabling.persistence.TimeTableRepository;
import org.acme.schooltimetabling.rest.TimeTableResource;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.reactive.messaging.memory.InMemorySink;
import io.smallrye.reactive.messaging.memory.InMemorySource;

@QuarkusTest
@QuarkusTestResource(ActiveMQTestResourceLifecycleManager.class)
public class TimeTableResourceTest {

    private static final Duration AWAIT_TIMEOUT = Duration.of(20L, ChronoUnit.SECONDS);

    @Inject
    @Any
    InMemoryConnector connector;

    @Inject
    TimeTableResource timeTableResource;

    @Inject
    TimeTableRepository timeTableRepository;

    @Inject
    ObjectMapper objectMapper;

    private InMemorySink<String> solverRequestSink;
    private InMemorySource<String> solverResponseSource;

    @BeforeEach
    void setupChannels() {
        solverRequestSink = connector.sink("solver_request");
        solverResponseSource = connector.source("solver_response");
        prepareTestTimeTable();
    }

    @Test
    @Timeout(60L)
    void solveAndRetrieve() throws JsonProcessingException {
        timeTableResource.solve();

        await()
                .timeout(AWAIT_TIMEOUT)
                .until(solverRequestSink::received, messages -> messages.size() == 1);

        Message<String> solverRequestMessage = solverRequestSink.received().get(0);
        SolverRequest solverRequest = objectMapper.readValue(solverRequestMessage.getPayload(), SolverRequest.class);

        // Assign one lesson to simulate solving.
        TimeTable requestTimeTable = solverRequest.getTimeTable();
        Lesson firstLesson = requestTimeTable.getLessonList().get(0);
        firstLesson.setRoom(requestTimeTable.getRoomList().get(0));
        firstLesson.setTimeslot(requestTimeTable.getTimeslotList().get(0));

        SolverResponse solverResponse = new SolverResponse(solverRequest.getProblemId(), requestTimeTable);
        solverResponseSource.send(objectMapper.writeValueAsString(solverResponse));

        // Wait until the client receives the message and saves the new timetable to a database.
        await()
                .timeout(AWAIT_TIMEOUT)
                .until(timeTableResource::getTimeTable, timeTable -> timeTable.getLessonList().get(0).getRoom() != null);

        Lesson solvedFirstLesson = timeTableResource.getTimeTable().getLessonList().get(0);
        assertThat(solvedFirstLesson.getRoom()).isNotNull();
        assertThat(solvedFirstLesson.getTimeslot()).isNotNull();
    }

    private void prepareTestTimeTable() {
        List<Room> rooms = Collections.singletonList(new Room(1L, "room-A"));
        List<Timeslot> timeslots =
                Collections.singletonList(new Timeslot(1L, DayOfWeek.WEDNESDAY, LocalTime.NOON, LocalTime.NOON));
        List<Lesson> lessons = Collections.singletonList(new Lesson(1L, "Physics", "M. Curie", "12th grade"));
        timeTableRepository.update(new TimeTable(timeslots, rooms, lessons));
    }
}
