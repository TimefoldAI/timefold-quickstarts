package org.acme.schooltimetabling.bootstrap;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

import org.acme.schooltimetabling.domain.Lesson;
import org.acme.schooltimetabling.domain.Room;
import org.acme.schooltimetabling.domain.TimeTable;
import org.acme.schooltimetabling.domain.Timeslot;
import org.acme.schooltimetabling.persistence.TimeTableRepository;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.runtime.LaunchMode;
import io.quarkus.runtime.StartupEvent;

@ApplicationScoped
public class DemoDataGenerator {

    private static final AtomicLong idGenerator = new AtomicLong();

    @ConfigProperty(name = "timeTable.demoData", defaultValue = "SMALL")
    DemoData demoData;

    @Inject
    TimeTableRepository timeTableRepository;

    public void generateDemoData(@Observes StartupEvent startupEvent) {
        if (LaunchMode.current() == LaunchMode.TEST) {
            return;
        }
        if (demoData == DemoData.NONE) {
            return;
        }

        List<Timeslot> timeslotList = new ArrayList<>(10);
        timeslotList.add(new Timeslot(idGenerator.incrementAndGet(), DayOfWeek.MONDAY, LocalTime.of(8, 30), LocalTime.of(9, 30)));
        timeslotList.add(new Timeslot(idGenerator.incrementAndGet(), DayOfWeek.MONDAY, LocalTime.of(9, 30), LocalTime.of(10, 30)));
        timeslotList.add(new Timeslot(idGenerator.incrementAndGet(),DayOfWeek.MONDAY, LocalTime.of(10, 30), LocalTime.of(11, 30)));
        timeslotList.add(new Timeslot(idGenerator.incrementAndGet(),DayOfWeek.MONDAY, LocalTime.of(13, 30), LocalTime.of(14, 30)));
        timeslotList.add(new Timeslot(idGenerator.incrementAndGet(),DayOfWeek.MONDAY, LocalTime.of(14, 30), LocalTime.of(15, 30)));

        timeslotList.add(new Timeslot(idGenerator.incrementAndGet(),DayOfWeek.TUESDAY, LocalTime.of(8, 30), LocalTime.of(9, 30)));
        timeslotList.add(new Timeslot(idGenerator.incrementAndGet(),DayOfWeek.TUESDAY, LocalTime.of(9, 30), LocalTime.of(10, 30)));
        timeslotList.add(new Timeslot(idGenerator.incrementAndGet(),DayOfWeek.TUESDAY, LocalTime.of(10, 30), LocalTime.of(11, 30)));
        timeslotList.add(new Timeslot(idGenerator.incrementAndGet(),DayOfWeek.TUESDAY, LocalTime.of(13, 30), LocalTime.of(14, 30)));
        timeslotList.add(new Timeslot(idGenerator.incrementAndGet(),DayOfWeek.TUESDAY, LocalTime.of(14, 30), LocalTime.of(15, 30)));
        if (demoData == DemoData.LARGE) {
            timeslotList.add(new Timeslot(idGenerator.incrementAndGet(),DayOfWeek.WEDNESDAY, LocalTime.of(8, 30), LocalTime.of(9, 30)));
            timeslotList.add(new Timeslot(idGenerator.incrementAndGet(),DayOfWeek.WEDNESDAY, LocalTime.of(9, 30), LocalTime.of(10, 30)));
            timeslotList.add(new Timeslot(idGenerator.incrementAndGet(),DayOfWeek.WEDNESDAY, LocalTime.of(10, 30), LocalTime.of(11, 30)));
            timeslotList.add(new Timeslot(idGenerator.incrementAndGet(),DayOfWeek.WEDNESDAY, LocalTime.of(13, 30), LocalTime.of(14, 30)));
            timeslotList.add(new Timeslot(idGenerator.incrementAndGet(),DayOfWeek.WEDNESDAY, LocalTime.of(14, 30), LocalTime.of(15, 30)));
            timeslotList.add(new Timeslot(idGenerator.incrementAndGet(),DayOfWeek.THURSDAY, LocalTime.of(8, 30), LocalTime.of(9, 30)));
            timeslotList.add(new Timeslot(idGenerator.incrementAndGet(),DayOfWeek.THURSDAY, LocalTime.of(9, 30), LocalTime.of(10, 30)));
            timeslotList.add(new Timeslot(idGenerator.incrementAndGet(),DayOfWeek.THURSDAY, LocalTime.of(10, 30), LocalTime.of(11, 30)));
            timeslotList.add(new Timeslot(idGenerator.incrementAndGet(),DayOfWeek.THURSDAY, LocalTime.of(13, 30), LocalTime.of(14, 30)));
            timeslotList.add(new Timeslot(idGenerator.incrementAndGet(),DayOfWeek.THURSDAY, LocalTime.of(14, 30), LocalTime.of(15, 30)));
            timeslotList.add(new Timeslot(idGenerator.incrementAndGet(),DayOfWeek.FRIDAY, LocalTime.of(8, 30), LocalTime.of(9, 30)));
            timeslotList.add(new Timeslot(idGenerator.incrementAndGet(),DayOfWeek.FRIDAY, LocalTime.of(9, 30), LocalTime.of(10, 30)));
            timeslotList.add(new Timeslot(idGenerator.incrementAndGet(),DayOfWeek.FRIDAY, LocalTime.of(10, 30), LocalTime.of(11, 30)));
            timeslotList.add(new Timeslot(idGenerator.incrementAndGet(),DayOfWeek.FRIDAY, LocalTime.of(13, 30), LocalTime.of(14, 30)));
            timeslotList.add(new Timeslot(idGenerator.incrementAndGet(),DayOfWeek.FRIDAY, LocalTime.of(14, 30), LocalTime.of(15, 30)));
        }

        List<Room> roomList = new ArrayList<>(3);
        roomList.add(new Room(idGenerator.incrementAndGet(), "Room A"));
        roomList.add(new Room(idGenerator.incrementAndGet(), "Room B"));
        roomList.add(new Room(idGenerator.incrementAndGet(), "Room C"));
        if (demoData == DemoData.LARGE) {
            roomList.add(new Room(idGenerator.incrementAndGet(), "Room D"));
            roomList.add(new Room(idGenerator.incrementAndGet(), "Room E"));
            roomList.add(new Room(idGenerator.incrementAndGet(), "Room F"));
        }

        List<Lesson> lessonList = new ArrayList<>();
        lessonList.add(new Lesson(idGenerator.incrementAndGet(), "Math", "A. Turing", "9th grade"));
        lessonList.add(new Lesson(idGenerator.incrementAndGet(), "Math", "A. Turing", "9th grade"));
        lessonList.add(new Lesson(idGenerator.incrementAndGet(), "Physics", "M. Curie", "9th grade"));
        lessonList.add(new Lesson(idGenerator.incrementAndGet(), "Chemistry", "M. Curie", "9th grade"));
        lessonList.add(new Lesson(idGenerator.incrementAndGet(), "Biology", "C. Darwin", "9th grade"));
        lessonList.add(new Lesson(idGenerator.incrementAndGet(), "History", "I. Jones", "9th grade"));
        lessonList.add(new Lesson(idGenerator.incrementAndGet(), "English", "I. Jones", "9th grade"));
        lessonList.add(new Lesson(idGenerator.incrementAndGet(), "English", "I. Jones", "9th grade"));
        lessonList.add(new Lesson(idGenerator.incrementAndGet(), "Spanish", "P. Cruz", "9th grade"));
        lessonList.add(new Lesson(idGenerator.incrementAndGet(), "Spanish", "P. Cruz", "9th grade"));
        if (demoData == DemoData.LARGE) {
            lessonList.add(new Lesson(idGenerator.incrementAndGet(), "Math", "A. Turing", "9th grade"));
            lessonList.add(new Lesson(idGenerator.incrementAndGet(), "Math", "A. Turing", "9th grade"));
            lessonList.add(new Lesson(idGenerator.incrementAndGet(), "Math", "A. Turing", "9th grade"));
            lessonList.add(new Lesson(idGenerator.incrementAndGet(), "ICT", "A. Turing", "9th grade"));
            lessonList.add(new Lesson(idGenerator.incrementAndGet(), "Physics", "M. Curie", "9th grade"));
            lessonList.add(new Lesson(idGenerator.incrementAndGet(), "Geography", "C. Darwin", "9th grade"));
            lessonList.add(new Lesson(idGenerator.incrementAndGet(), "Geology", "C. Darwin", "9th grade"));
            lessonList.add(new Lesson(idGenerator.incrementAndGet(), "History", "I. Jones", "9th grade"));
            lessonList.add(new Lesson(idGenerator.incrementAndGet(), "English", "I. Jones", "9th grade"));
            lessonList.add(new Lesson(idGenerator.incrementAndGet(), "Drama", "I. Jones", "9th grade"));
            lessonList.add(new Lesson(idGenerator.incrementAndGet(), "Art", "S. Dali", "9th grade"));
            lessonList.add(new Lesson(idGenerator.incrementAndGet(), "Art", "S. Dali", "9th grade"));
            lessonList.add(new Lesson(idGenerator.incrementAndGet(), "Physical education", "C. Lewis", "9th grade"));
            lessonList.add(new Lesson(idGenerator.incrementAndGet(), "Physical education", "C. Lewis", "9th grade"));
            lessonList.add(new Lesson(idGenerator.incrementAndGet(), "Physical education", "C. Lewis", "9th grade"));
        }

        lessonList.add(new Lesson(idGenerator.incrementAndGet(), "Math", "A. Turing", "10th grade"));
        lessonList.add(new Lesson(idGenerator.incrementAndGet(), "Math", "A. Turing", "10th grade"));
        lessonList.add(new Lesson(idGenerator.incrementAndGet(), "Math", "A. Turing", "10th grade"));
        lessonList.add(new Lesson(idGenerator.incrementAndGet(), "Physics", "M. Curie", "10th grade"));
        lessonList.add(new Lesson(idGenerator.incrementAndGet(), "Chemistry", "M. Curie", "10th grade"));
        lessonList.add(new Lesson(idGenerator.incrementAndGet(), "French", "M. Curie", "10th grade"));
        lessonList.add(new Lesson(idGenerator.incrementAndGet(), "Geography", "C. Darwin", "10th grade"));
        lessonList.add(new Lesson(idGenerator.incrementAndGet(), "History", "I. Jones", "10th grade"));
        lessonList.add(new Lesson(idGenerator.incrementAndGet(), "English", "P. Cruz", "10th grade"));
        lessonList.add(new Lesson(idGenerator.incrementAndGet(), "Spanish", "P. Cruz", "10th grade"));
        if (demoData == DemoData.LARGE) {
            lessonList.add(new Lesson(idGenerator.incrementAndGet(), "Math", "A. Turing", "10th grade"));
            lessonList.add(new Lesson(idGenerator.incrementAndGet(), "Math", "A. Turing", "10th grade"));
            lessonList.add(new Lesson(idGenerator.incrementAndGet(), "ICT", "A. Turing", "10th grade"));
            lessonList.add(new Lesson(idGenerator.incrementAndGet(), "Physics", "M. Curie", "10th grade"));
            lessonList.add(new Lesson(idGenerator.incrementAndGet(), "Biology", "C. Darwin", "10th grade"));
            lessonList.add(new Lesson(idGenerator.incrementAndGet(), "Geology", "C. Darwin", "10th grade"));
            lessonList.add(new Lesson(idGenerator.incrementAndGet(), "History", "I. Jones", "10th grade"));
            lessonList.add(new Lesson(idGenerator.incrementAndGet(), "English", "P. Cruz", "10th grade"));
            lessonList.add(new Lesson(idGenerator.incrementAndGet(), "English", "P. Cruz", "10th grade"));
            lessonList.add(new Lesson(idGenerator.incrementAndGet(), "Drama", "I. Jones", "10th grade"));
            lessonList.add(new Lesson(idGenerator.incrementAndGet(), "Art", "S. Dali", "10th grade"));
            lessonList.add(new Lesson(idGenerator.incrementAndGet(), "Art", "S. Dali", "10th grade"));
            lessonList.add(new Lesson(idGenerator.incrementAndGet(), "Physical education", "C. Lewis", "10th grade"));
            lessonList.add(new Lesson(idGenerator.incrementAndGet(), "Physical education", "C. Lewis", "10th grade"));
            lessonList.add(new Lesson(idGenerator.incrementAndGet(), "Physical education", "C. Lewis", "10th grade"));

            lessonList.add(new Lesson(idGenerator.incrementAndGet(), "Math", "A. Turing", "11th grade"));
            lessonList.add(new Lesson(idGenerator.incrementAndGet(), "Math", "A. Turing", "11th grade"));
            lessonList.add(new Lesson(idGenerator.incrementAndGet(), "Math", "A. Turing", "11th grade"));
            lessonList.add(new Lesson(idGenerator.incrementAndGet(), "Math", "A. Turing", "11th grade"));
            lessonList.add(new Lesson(idGenerator.incrementAndGet(), "Math", "A. Turing", "11th grade"));
            lessonList.add(new Lesson(idGenerator.incrementAndGet(), "ICT", "A. Turing", "11th grade"));
            lessonList.add(new Lesson(idGenerator.incrementAndGet(), "Physics", "M. Curie", "11th grade"));
            lessonList.add(new Lesson(idGenerator.incrementAndGet(), "Chemistry", "M. Curie", "11th grade"));
            lessonList.add(new Lesson(idGenerator.incrementAndGet(), "French", "M. Curie", "11th grade"));
            lessonList.add(new Lesson(idGenerator.incrementAndGet(), "Physics", "M. Curie", "11th grade"));
            lessonList.add(new Lesson(idGenerator.incrementAndGet(), "Geography", "C. Darwin", "11th grade"));
            lessonList.add(new Lesson(idGenerator.incrementAndGet(), "Biology", "C. Darwin", "11th grade"));
            lessonList.add(new Lesson(idGenerator.incrementAndGet(), "Geology", "C. Darwin", "11th grade"));
            lessonList.add(new Lesson(idGenerator.incrementAndGet(), "History", "I. Jones", "11th grade"));
            lessonList.add(new Lesson(idGenerator.incrementAndGet(), "History", "I. Jones", "11th grade"));
            lessonList.add(new Lesson(idGenerator.incrementAndGet(), "English", "P. Cruz", "11th grade"));
            lessonList.add(new Lesson(idGenerator.incrementAndGet(), "English", "P. Cruz", "11th grade"));
            lessonList.add(new Lesson(idGenerator.incrementAndGet(), "English", "P. Cruz", "11th grade"));
            lessonList.add(new Lesson(idGenerator.incrementAndGet(), "Spanish", "P. Cruz", "11th grade"));
            lessonList.add(new Lesson(idGenerator.incrementAndGet(), "Drama", "P. Cruz", "11th grade"));
            lessonList.add(new Lesson(idGenerator.incrementAndGet(), "Art", "S. Dali", "11th grade"));
            lessonList.add(new Lesson(idGenerator.incrementAndGet(), "Art", "S. Dali", "11th grade"));
            lessonList.add(new Lesson(idGenerator.incrementAndGet(), "Physical education", "C. Lewis", "11th grade"));
            lessonList.add(new Lesson(idGenerator.incrementAndGet(), "Physical education", "C. Lewis", "11th grade"));
            lessonList.add(new Lesson(idGenerator.incrementAndGet(), "Physical education", "C. Lewis", "11th grade"));

            lessonList.add(new Lesson(idGenerator.incrementAndGet(), "Math", "A. Turing", "12th grade"));
            lessonList.add(new Lesson(idGenerator.incrementAndGet(), "Math", "A. Turing", "12th grade"));
            lessonList.add(new Lesson(idGenerator.incrementAndGet(), "Math", "A. Turing", "12th grade"));
            lessonList.add(new Lesson(idGenerator.incrementAndGet(), "Math", "A. Turing", "12th grade"));
            lessonList.add(new Lesson(idGenerator.incrementAndGet(), "Math", "A. Turing", "12th grade"));
            lessonList.add(new Lesson(idGenerator.incrementAndGet(), "ICT", "A. Turing", "12th grade"));
            lessonList.add(new Lesson(idGenerator.incrementAndGet(), "Physics", "M. Curie", "12th grade"));
            lessonList.add(new Lesson(idGenerator.incrementAndGet(), "Chemistry", "M. Curie", "12th grade"));
            lessonList.add(new Lesson(idGenerator.incrementAndGet(), "French", "M. Curie", "12th grade"));
            lessonList.add(new Lesson(idGenerator.incrementAndGet(), "Physics", "M. Curie", "12th grade"));
            lessonList.add(new Lesson(idGenerator.incrementAndGet(), "Geography", "C. Darwin", "12th grade"));
            lessonList.add(new Lesson(idGenerator.incrementAndGet(), "Biology", "C. Darwin", "12th grade"));
            lessonList.add(new Lesson(idGenerator.incrementAndGet(), "Geology", "C. Darwin", "12th grade"));
            lessonList.add(new Lesson(idGenerator.incrementAndGet(), "History", "I. Jones", "12th grade"));
            lessonList.add(new Lesson(idGenerator.incrementAndGet(), "History", "I. Jones", "12th grade"));
            lessonList.add(new Lesson(idGenerator.incrementAndGet(), "English", "P. Cruz", "12th grade"));
            lessonList.add(new Lesson(idGenerator.incrementAndGet(), "English", "P. Cruz", "12th grade"));
            lessonList.add(new Lesson(idGenerator.incrementAndGet(), "English", "P. Cruz", "12th grade"));
            lessonList.add(new Lesson(idGenerator.incrementAndGet(), "Spanish", "P. Cruz", "12th grade"));
            lessonList.add(new Lesson(idGenerator.incrementAndGet(), "Drama", "P. Cruz", "12th grade"));
            lessonList.add(new Lesson(idGenerator.incrementAndGet(), "Art", "S. Dali", "12th grade"));
            lessonList.add(new Lesson(idGenerator.incrementAndGet(), "Art", "S. Dali", "12th grade"));
            lessonList.add(new Lesson(idGenerator.incrementAndGet(), "Physical education", "C. Lewis", "12th grade"));
            lessonList.add(new Lesson(idGenerator.incrementAndGet(), "Physical education", "C. Lewis", "12th grade"));
            lessonList.add(new Lesson(idGenerator.incrementAndGet(), "Physical education", "C. Lewis", "12th grade"));
        }

        Lesson lesson = lessonList.get(0);
        lesson.setTimeslot(timeslotList.get(0));
        lesson.setRoom(roomList.get(0));

        TimeTable timeTable = new TimeTable(timeslotList, roomList, lessonList);
        timeTableRepository.update(timeTable);
    }

    public enum DemoData {
        NONE,
        SMALL,
        LARGE
    }

}
