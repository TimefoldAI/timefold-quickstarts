package org.acme.schooltimetabling;

import java.time.DayOfWeek;
import java.time.LocalTime;

import org.acme.schooltimetabling.domain.Lesson;
import org.acme.schooltimetabling.domain.Room;
import org.acme.schooltimetabling.domain.Timeslot;
import org.acme.schooltimetabling.persistence.LessonRepository;
import org.acme.schooltimetabling.persistence.RoomRepository;
import org.acme.schooltimetabling.persistence.TimeslotRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Sort;

@SpringBootApplication
public class TimeTableSpringBootApp {

    public static void main(String[] args) {
        SpringApplication.run(TimeTableSpringBootApp.class, args);
    }

    @Value("${timeTable.demoData:SMALL}")
    private DemoData demoData;

    @Bean
    public CommandLineRunner demoData(
            TimeslotRepository timeslotRepository,
            RoomRepository roomRepository,
            LessonRepository lessonRepository) {
        return (args) -> {
            if (demoData == DemoData.NONE) {
                return;
            }

            timeslotRepository.save(new Timeslot(DayOfWeek.MONDAY, LocalTime.of(8, 30), LocalTime.of(9, 30)));
            timeslotRepository.save(new Timeslot(DayOfWeek.MONDAY, LocalTime.of(9, 30), LocalTime.of(10, 30)));
            timeslotRepository.save(new Timeslot(DayOfWeek.MONDAY, LocalTime.of(10, 30), LocalTime.of(11, 30)));
            timeslotRepository.save(new Timeslot(DayOfWeek.MONDAY, LocalTime.of(13, 30), LocalTime.of(14, 30)));
            timeslotRepository.save(new Timeslot(DayOfWeek.MONDAY, LocalTime.of(14, 30), LocalTime.of(15, 30)));
            timeslotRepository.save(new Timeslot(DayOfWeek.TUESDAY, LocalTime.of(8, 30), LocalTime.of(9, 30)));
            timeslotRepository.save(new Timeslot(DayOfWeek.TUESDAY, LocalTime.of(9, 30), LocalTime.of(10, 30)));
            timeslotRepository.save(new Timeslot(DayOfWeek.TUESDAY, LocalTime.of(10, 30), LocalTime.of(11, 30)));
            timeslotRepository.save(new Timeslot(DayOfWeek.TUESDAY, LocalTime.of(13, 30), LocalTime.of(14, 30)));
            timeslotRepository.save(new Timeslot(DayOfWeek.TUESDAY, LocalTime.of(14, 30), LocalTime.of(15, 30)));
            if (demoData == DemoData.LARGE) {
                timeslotRepository.save(new Timeslot(DayOfWeek.WEDNESDAY, LocalTime.of(8, 30), LocalTime.of(9, 30)));
                timeslotRepository.save(new Timeslot(DayOfWeek.WEDNESDAY, LocalTime.of(9, 30), LocalTime.of(10, 30)));
                timeslotRepository.save(new Timeslot(DayOfWeek.WEDNESDAY, LocalTime.of(10, 30), LocalTime.of(11, 30)));
                timeslotRepository.save(new Timeslot(DayOfWeek.WEDNESDAY, LocalTime.of(13, 30), LocalTime.of(14, 30)));
                timeslotRepository.save(new Timeslot(DayOfWeek.WEDNESDAY, LocalTime.of(14, 30), LocalTime.of(15, 30)));
                timeslotRepository.save(new Timeslot(DayOfWeek.THURSDAY, LocalTime.of(8, 30), LocalTime.of(9, 30)));
                timeslotRepository.save(new Timeslot(DayOfWeek.THURSDAY, LocalTime.of(9, 30), LocalTime.of(10, 30)));
                timeslotRepository.save(new Timeslot(DayOfWeek.THURSDAY, LocalTime.of(10, 30), LocalTime.of(11, 30)));
                timeslotRepository.save(new Timeslot(DayOfWeek.THURSDAY, LocalTime.of(13, 30), LocalTime.of(14, 30)));
                timeslotRepository.save(new Timeslot(DayOfWeek.THURSDAY, LocalTime.of(14, 30), LocalTime.of(15, 30)));
                timeslotRepository.save(new Timeslot(DayOfWeek.FRIDAY, LocalTime.of(8, 30), LocalTime.of(9, 30)));
                timeslotRepository.save(new Timeslot(DayOfWeek.FRIDAY, LocalTime.of(9, 30), LocalTime.of(10, 30)));
                timeslotRepository.save(new Timeslot(DayOfWeek.FRIDAY, LocalTime.of(10, 30), LocalTime.of(11, 30)));
                timeslotRepository.save(new Timeslot(DayOfWeek.FRIDAY, LocalTime.of(13, 30), LocalTime.of(14, 30)));
                timeslotRepository.save(new Timeslot(DayOfWeek.FRIDAY, LocalTime.of(14, 30), LocalTime.of(15, 30)));
            }

            roomRepository.save(new Room("Room A"));
            roomRepository.save(new Room("Room B"));
            roomRepository.save(new Room("Room C"));
            if (demoData == DemoData.LARGE) {
                roomRepository.save(new Room("Room D"));
                roomRepository.save(new Room("Room E"));
                roomRepository.save(new Room("Room F"));
            }

            lessonRepository.save(new Lesson("Math", "A. Turing", "9th grade"));
            lessonRepository.save(new Lesson("Math", "A. Turing", "9th grade"));
            lessonRepository.save(new Lesson("Physics", "M. Curie", "9th grade"));
            lessonRepository.save(new Lesson("Chemistry", "M. Curie", "9th grade"));
            lessonRepository.save(new Lesson("Biology", "C. Darwin", "9th grade"));
            lessonRepository.save(new Lesson("History", "I. Jones", "9th grade"));
            lessonRepository.save(new Lesson("English", "I. Jones", "9th grade"));
            lessonRepository.save(new Lesson("English", "I. Jones", "9th grade"));
            lessonRepository.save(new Lesson("Spanish", "P. Cruz", "9th grade"));
            lessonRepository.save(new Lesson("Spanish", "P. Cruz", "9th grade"));
            if (demoData == DemoData.LARGE) {
                lessonRepository.save(new Lesson("Math", "A. Turing", "9th grade"));
                lessonRepository.save(new Lesson("Math", "A. Turing", "9th grade"));
                lessonRepository.save(new Lesson("Math", "A. Turing", "9th grade"));
                lessonRepository.save(new Lesson("ICT", "A. Turing", "9th grade"));
                lessonRepository.save(new Lesson("Physics", "M. Curie", "9th grade"));
                lessonRepository.save(new Lesson("Geography", "C. Darwin", "9th grade"));
                lessonRepository.save(new Lesson("Geology", "C. Darwin", "9th grade"));
                lessonRepository.save(new Lesson("History", "I. Jones", "9th grade"));
                lessonRepository.save(new Lesson("English", "I. Jones", "9th grade"));
                lessonRepository.save(new Lesson("Drama", "I. Jones", "9th grade"));
                lessonRepository.save(new Lesson("Art", "S. Dali", "9th grade"));
                lessonRepository.save(new Lesson("Art", "S. Dali", "9th grade"));
                lessonRepository.save(new Lesson("Physical education", "C. Lewis", "9th grade"));
                lessonRepository.save(new Lesson("Physical education", "C. Lewis", "9th grade"));
                lessonRepository.save(new Lesson("Physical education", "C. Lewis", "9th grade"));
            }

            lessonRepository.save(new Lesson("Math", "A. Turing", "10th grade"));
            lessonRepository.save(new Lesson("Math", "A. Turing", "10th grade"));
            lessonRepository.save(new Lesson("Math", "A. Turing", "10th grade"));
            lessonRepository.save(new Lesson("Physics", "M. Curie", "10th grade"));
            lessonRepository.save(new Lesson("Chemistry", "M. Curie", "10th grade"));
            lessonRepository.save(new Lesson("French", "M. Curie", "10th grade"));
            lessonRepository.save(new Lesson("Geography", "C. Darwin", "10th grade"));
            lessonRepository.save(new Lesson("History", "I. Jones", "10th grade"));
            lessonRepository.save(new Lesson("English", "P. Cruz", "10th grade"));
            lessonRepository.save(new Lesson("Spanish", "P. Cruz", "10th grade"));
            if (demoData == DemoData.LARGE) {
                lessonRepository.save(new Lesson("Math", "A. Turing", "10th grade"));
                lessonRepository.save(new Lesson("Math", "A. Turing", "10th grade"));
                lessonRepository.save(new Lesson("ICT", "A. Turing", "10th grade"));
                lessonRepository.save(new Lesson("Physics", "M. Curie", "10th grade"));
                lessonRepository.save(new Lesson("Biology", "C. Darwin", "10th grade"));
                lessonRepository.save(new Lesson("Geology", "C. Darwin", "10th grade"));
                lessonRepository.save(new Lesson("History", "I. Jones", "10th grade"));
                lessonRepository.save(new Lesson("English", "P. Cruz", "10th grade"));
                lessonRepository.save(new Lesson("English", "P. Cruz", "10th grade"));
                lessonRepository.save(new Lesson("Drama", "I. Jones", "10th grade"));
                lessonRepository.save(new Lesson("Art", "S. Dali", "10th grade"));
                lessonRepository.save(new Lesson("Art", "S. Dali", "10th grade"));
                lessonRepository.save(new Lesson("Physical education", "C. Lewis", "10th grade"));
                lessonRepository.save(new Lesson("Physical education", "C. Lewis", "10th grade"));
                lessonRepository.save(new Lesson("Physical education", "C. Lewis", "10th grade"));

                lessonRepository.save(new Lesson("Math", "A. Turing", "11th grade"));
                lessonRepository.save(new Lesson("Math", "A. Turing", "11th grade"));
                lessonRepository.save(new Lesson("Math", "A. Turing", "11th grade"));
                lessonRepository.save(new Lesson("Math", "A. Turing", "11th grade"));
                lessonRepository.save(new Lesson("Math", "A. Turing", "11th grade"));
                lessonRepository.save(new Lesson("ICT", "A. Turing", "11th grade"));
                lessonRepository.save(new Lesson("Physics", "M. Curie", "11th grade"));
                lessonRepository.save(new Lesson("Chemistry", "M. Curie", "11th grade"));
                lessonRepository.save(new Lesson("French", "M. Curie", "11th grade"));
                lessonRepository.save(new Lesson("Physics", "M. Curie", "11th grade"));
                lessonRepository.save(new Lesson("Geography", "C. Darwin", "11th grade"));
                lessonRepository.save(new Lesson("Biology", "C. Darwin", "11th grade"));
                lessonRepository.save(new Lesson("Geology", "C. Darwin", "11th grade"));
                lessonRepository.save(new Lesson("History", "I. Jones", "11th grade"));
                lessonRepository.save(new Lesson("History", "I. Jones", "11th grade"));
                lessonRepository.save(new Lesson("English", "P. Cruz", "11th grade"));
                lessonRepository.save(new Lesson("English", "P. Cruz", "11th grade"));
                lessonRepository.save(new Lesson("English", "P. Cruz", "11th grade"));
                lessonRepository.save(new Lesson("Spanish", "P. Cruz", "11th grade"));
                lessonRepository.save(new Lesson("Drama", "P. Cruz", "11th grade"));
                lessonRepository.save(new Lesson("Art", "S. Dali", "11th grade"));
                lessonRepository.save(new Lesson("Art", "S. Dali", "11th grade"));
                lessonRepository.save(new Lesson("Physical education", "C. Lewis", "11th grade"));
                lessonRepository.save(new Lesson("Physical education", "C. Lewis", "11th grade"));
                lessonRepository.save(new Lesson("Physical education", "C. Lewis", "11th grade"));

                lessonRepository.save(new Lesson("Math", "A. Turing", "12th grade"));
                lessonRepository.save(new Lesson("Math", "A. Turing", "12th grade"));
                lessonRepository.save(new Lesson("Math", "A. Turing", "12th grade"));
                lessonRepository.save(new Lesson("Math", "A. Turing", "12th grade"));
                lessonRepository.save(new Lesson("Math", "A. Turing", "12th grade"));
                lessonRepository.save(new Lesson("ICT", "A. Turing", "12th grade"));
                lessonRepository.save(new Lesson("Physics", "M. Curie", "12th grade"));
                lessonRepository.save(new Lesson("Chemistry", "M. Curie", "12th grade"));
                lessonRepository.save(new Lesson("French", "M. Curie", "12th grade"));
                lessonRepository.save(new Lesson("Physics", "M. Curie", "12th grade"));
                lessonRepository.save(new Lesson("Geography", "C. Darwin", "12th grade"));
                lessonRepository.save(new Lesson("Biology", "C. Darwin", "12th grade"));
                lessonRepository.save(new Lesson("Geology", "C. Darwin", "12th grade"));
                lessonRepository.save(new Lesson("History", "I. Jones", "12th grade"));
                lessonRepository.save(new Lesson("History", "I. Jones", "12th grade"));
                lessonRepository.save(new Lesson("English", "P. Cruz", "12th grade"));
                lessonRepository.save(new Lesson("English", "P. Cruz", "12th grade"));
                lessonRepository.save(new Lesson("English", "P. Cruz", "12th grade"));
                lessonRepository.save(new Lesson("Spanish", "P. Cruz", "12th grade"));
                lessonRepository.save(new Lesson("Drama", "P. Cruz", "12th grade"));
                lessonRepository.save(new Lesson("Art", "S. Dali", "12th grade"));
                lessonRepository.save(new Lesson("Art", "S. Dali", "12th grade"));
                lessonRepository.save(new Lesson("Physical education", "C. Lewis", "12th grade"));
                lessonRepository.save(new Lesson("Physical education", "C. Lewis", "12th grade"));
                lessonRepository.save(new Lesson("Physical education", "C. Lewis", "12th grade"));
            }
        };
    }

    public enum DemoData {
        NONE,
        SMALL,
        LARGE
    }

}
