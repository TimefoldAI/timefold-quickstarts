package org.acme.schooltimetabling;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import ai.timefold.solver.core.api.score.HardSoftScore;
import ai.timefold.solver.quarkus.jackson.TimefoldJacksonModule;
import ai.timefold.solver.quarkus.jackson.solution.JacksonSolutionFileIO;

import org.acme.schooltimetabling.domain.Lesson;
import org.acme.schooltimetabling.domain.Room;
import org.acme.schooltimetabling.domain.Timeslot;
import org.acme.schooltimetabling.domain.Timetable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

class TimetableJsonTest {

    @TempDir
    Path tempDir;

    // Mirrors TimetableConsoleApp.writeSolution(): the default JacksonSolutionFileIO(Class) mapper
    // leaves WRITE_DATES_AS_TIMESTAMPS enabled, which would serialize LocalTime as an int array.
    private static JacksonSolutionFileIO<Timetable> newSolutionFileIO() {
        var mapper = JsonMapper.builder()
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();
        mapper.registerModule(TimefoldJacksonModule.createModule());
        return new JacksonSolutionFileIO<>(Timetable.class, mapper);
    }

    @Test
    void writesRepeatedReferencesAsBareIdsInsteadOfDuplicatingObjects() throws IOException {
        var timeslot = new Timeslot("1", DayOfWeek.MONDAY, LocalTime.NOON);
        var room = new Room("1", "Room A");
        var firstLesson = new Lesson("1", "Math", "A. Turing", "9th grade", timeslot, room);
        var secondLesson = new Lesson("2", "Physics", "M. Curie", "9th grade", timeslot, room);
        var timetable = new Timetable("TEST", List.of(timeslot), List.of(room), List.of(firstLesson, secondLesson));
        timetable.setScore(HardSoftScore.ZERO);

        var outputFile = tempDir.resolve("solution.json");
        newSolutionFileIO().write(timetable, outputFile.toFile());
        var json = Files.readString(outputFile);

        // The timeslots/rooms arrays hold full objects, not just ids.
        assertThat(json).contains("\"dayOfWeek\" : \"MONDAY\"");
        assertThat(json).contains("\"name\" : \"Room A\"");
        // LocalTime serializes as an ISO string, not as a [hour, minute, ...] array.
        assertThat(json).contains("\"startTime\" : \"12:00");
        // Score serializes as a plain string, not a nested object.
        assertThat(json).contains("\"score\" : \"0hard/0soft\"");
        // @JsonIdentityInfo on Timeslot/Room means only their first occurrence (inside the
        // timeslots/rooms arrays) is inlined in full; both lessons then refer back by id.
        assertThat(json).containsOnlyOnce("\"dayOfWeek\"");
        assertThat(json).containsOnlyOnce("\"name\" : \"Room A\"");
    }

    @Test
    void writesUnassignedLessonsWithNullReferences() throws IOException {
        var lesson = new Lesson("1", "Math", "A. Turing", "9th grade");
        var timetable = new Timetable("TEST", List.of(), List.of(), List.of(lesson));
        timetable.setScore(HardSoftScore.ZERO);

        var outputFile = tempDir.resolve("solution.json");
        newSolutionFileIO().write(timetable, outputFile.toFile());
        var json = Files.readString(outputFile);

        assertThat(json).contains("\"timeslot\" : null");
        assertThat(json).contains("\"room\" : null");
    }

}
