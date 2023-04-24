package org.acme.kotlin.schooltimetabling.bootstrap

import io.quarkus.runtime.StartupEvent
import org.acme.kotlin.schooltimetabling.domain.Lesson
import org.acme.kotlin.schooltimetabling.domain.Room
import org.acme.kotlin.schooltimetabling.domain.Timeslot
import org.acme.kotlin.schooltimetabling.persistence.LessonRepository
import org.acme.kotlin.schooltimetabling.persistence.RoomRepository
import org.acme.kotlin.schooltimetabling.persistence.TimeslotRepository
import org.eclipse.microprofile.config.inject.ConfigProperty
import java.time.DayOfWeek
import java.time.LocalTime
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.event.Observes
import jakarta.inject.Inject
import jakarta.transaction.Transactional


@ApplicationScoped
class DemoDataGenerator {

    @ConfigProperty(name = "timeTable.demoData", defaultValue = "SMALL")
    lateinit var demoData: DemoData

    @Inject
    lateinit var timeslotRepository: TimeslotRepository
    @Inject
    lateinit var roomRepository: RoomRepository
    @Inject
    lateinit var lessonRepository: LessonRepository

    @Transactional
    fun generateDemoData(@Observes startupEvent: StartupEvent) {
        if (demoData == DemoData.NONE) {
            return
        }

        val timeslotList: MutableList<Timeslot> = mutableListOf(
                Timeslot(DayOfWeek.MONDAY, LocalTime.of(8, 30), LocalTime.of(9, 30)),
                Timeslot(DayOfWeek.MONDAY, LocalTime.of(9, 30), LocalTime.of(10, 30)),
                Timeslot(DayOfWeek.MONDAY, LocalTime.of(10, 30), LocalTime.of(11, 30)),
                Timeslot(DayOfWeek.MONDAY, LocalTime.of(13, 30), LocalTime.of(14, 30)),
                Timeslot(DayOfWeek.MONDAY, LocalTime.of(14, 30), LocalTime.of(15, 30)),

                Timeslot(DayOfWeek.TUESDAY, LocalTime.of(8, 30), LocalTime.of(9, 30)),
                Timeslot(DayOfWeek.TUESDAY, LocalTime.of(9, 30), LocalTime.of(10, 30)),
                Timeslot(DayOfWeek.TUESDAY, LocalTime.of(10, 30), LocalTime.of(11, 30)),
                Timeslot(DayOfWeek.TUESDAY, LocalTime.of(13, 30), LocalTime.of(14, 30)),
                Timeslot(DayOfWeek.TUESDAY, LocalTime.of(14, 30), LocalTime.of(15, 30)))

        if (demoData == DemoData.LARGE) {
            timeslotList.addAll(listOf(
                    Timeslot(DayOfWeek.WEDNESDAY, LocalTime.of(8, 30), LocalTime.of(9, 30)),
                    Timeslot(DayOfWeek.WEDNESDAY, LocalTime.of(9, 30), LocalTime.of(10, 30)),
                    Timeslot(DayOfWeek.WEDNESDAY, LocalTime.of(10, 30), LocalTime.of(11, 30)),
                    Timeslot(DayOfWeek.WEDNESDAY, LocalTime.of(13, 30), LocalTime.of(14, 30)),
                    Timeslot(DayOfWeek.WEDNESDAY, LocalTime.of(14, 30), LocalTime.of(15, 30)),
                    Timeslot(DayOfWeek.THURSDAY, LocalTime.of(8, 30), LocalTime.of(9, 30)),
                    Timeslot(DayOfWeek.THURSDAY, LocalTime.of(9, 30), LocalTime.of(10, 30)),
                    Timeslot(DayOfWeek.THURSDAY, LocalTime.of(10, 30), LocalTime.of(11, 30)),
                    Timeslot(DayOfWeek.THURSDAY, LocalTime.of(13, 30), LocalTime.of(14, 30)),
                    Timeslot(DayOfWeek.THURSDAY, LocalTime.of(14, 30), LocalTime.of(15, 30)),
                    Timeslot(DayOfWeek.FRIDAY, LocalTime.of(8, 30), LocalTime.of(9, 30)),
                    Timeslot(DayOfWeek.FRIDAY, LocalTime.of(9, 30), LocalTime.of(10, 30)),
                    Timeslot(DayOfWeek.FRIDAY, LocalTime.of(10, 30), LocalTime.of(11, 30)),
                    Timeslot(DayOfWeek.FRIDAY, LocalTime.of(13, 30), LocalTime.of(14, 30)),
                    Timeslot(DayOfWeek.FRIDAY, LocalTime.of(14, 30), LocalTime.of(15, 30))))
        }
        timeslotRepository.persist(timeslotList)


        val roomList: MutableList<Room> = mutableListOf(
                Room("Room A"),
                Room("Room B"),
                Room("Room C"))
        if (demoData == DemoData.LARGE) {
            roomList.addAll(listOf(
                    Room("Room D"),
                    Room("Room E"),
                    Room("Room F")))
        }
        roomRepository.persist(roomList)


        val lessonList: MutableList<Lesson> = mutableListOf(
                Lesson("Math", "A. Turing", "9th grade"),
                Lesson("Math", "A. Turing", "9th grade"),
                Lesson("Physics", "M. Curie", "9th grade"),
                Lesson("Chemistry", "M. Curie", "9th grade"),
                Lesson("Biology", "C. Darwin", "9th grade"),
                Lesson("History", "I. Jones", "9th grade"),
                Lesson("English", "I. Jones", "9th grade"),
                Lesson("English", "I. Jones", "9th grade"),
                Lesson("Spanish", "P. Cruz", "9th grade"),
                Lesson("Spanish", "P. Cruz", "9th grade"))
        if (demoData == DemoData.LARGE) {
            lessonList.addAll(listOf(
                    Lesson("Math", "A. Turing", "9th grade"),
                    Lesson("Math", "A. Turing", "9th grade"),
                    Lesson("Math", "A. Turing", "9th grade"),
                    Lesson("ICT", "A. Turing", "9th grade"),
                    Lesson("Physics", "M. Curie", "9th grade"),
                    Lesson("Geography", "C. Darwin", "9th grade"),
                    Lesson("Geology", "C. Darwin", "9th grade"),
                    Lesson("History", "I. Jones", "9th grade"),
                    Lesson("English", "I. Jones", "9th grade"),
                    Lesson("Drama", "I. Jones", "9th grade"),
                    Lesson("Art", "S. Dali", "9th grade"),
                    Lesson("Art", "S. Dali", "9th grade"),
                    Lesson("Physical education", "C. Lewis", "9th grade"),
                    Lesson("Physical education", "C. Lewis", "9th grade"),
                    Lesson("Physical education", "C. Lewis", "9th grade")))
        }

        lessonList.addAll(listOf(
                Lesson("Math", "A. Turing", "10th grade"),
                Lesson("Math", "A. Turing", "10th grade"),
                Lesson("Math", "A. Turing", "10th grade"),
                Lesson("Physics", "M. Curie", "10th grade"),
                Lesson("Chemistry", "M. Curie", "10th grade"),
                Lesson("French", "M. Curie", "10th grade"),
                Lesson("Geography", "C. Darwin", "10th grade"),
                Lesson("History", "I. Jones", "10th grade"),
                Lesson("English", "P. Cruz", "10th grade"),
                Lesson("Spanish", "P. Cruz", "10th grade")))
        if (demoData == DemoData.LARGE) {
            lessonList.addAll(listOf(
                    Lesson("Math", "A. Turing", "10th grade"),
                    Lesson("Math", "A. Turing", "10th grade"),
                    Lesson("ICT", "A. Turing", "10th grade"),
                    Lesson("Physics", "M. Curie", "10th grade"),
                    Lesson("Biology", "C. Darwin", "10th grade"),
                    Lesson("Geology", "C. Darwin", "10th grade"),
                    Lesson("History", "I. Jones", "10th grade"),
                    Lesson("English", "P. Cruz", "10th grade"),
                    Lesson("English", "P. Cruz", "10th grade"),
                    Lesson("Drama", "I. Jones", "10th grade"),
                    Lesson("Art", "S. Dali", "10th grade"),
                    Lesson("Art", "S. Dali", "10th grade"),
                    Lesson("Physical education", "C. Lewis", "10th grade"),
                    Lesson("Physical education", "C. Lewis", "10th grade"),
                    Lesson("Physical education", "C. Lewis", "10th grade"),

                    Lesson("Math", "A. Turing", "11th grade"),
                    Lesson("Math", "A. Turing", "11th grade"),
                    Lesson("Math", "A. Turing", "11th grade"),
                    Lesson("Math", "A. Turing", "11th grade"),
                    Lesson("Math", "A. Turing", "11th grade"),
                    Lesson("ICT", "A. Turing", "11th grade"),
                    Lesson("Physics", "M. Curie", "11th grade"),
                    Lesson("Chemistry", "M. Curie", "11th grade"),
                    Lesson("French", "M. Curie", "11th grade"),
                    Lesson("Physics", "M. Curie", "11th grade"),
                    Lesson("Geography", "C. Darwin", "11th grade"),
                    Lesson("Biology", "C. Darwin", "11th grade"),
                    Lesson("Geology", "C. Darwin", "11th grade"),
                    Lesson("History", "I. Jones", "11th grade"),
                    Lesson("History", "I. Jones", "11th grade"),
                    Lesson("English", "P. Cruz", "11th grade"),
                    Lesson("English", "P. Cruz", "11th grade"),
                    Lesson("English", "P. Cruz", "11th grade"),
                    Lesson("Spanish", "P. Cruz", "11th grade"),
                    Lesson("Drama", "P. Cruz", "11th grade"),
                    Lesson("Art", "S. Dali", "11th grade"),
                    Lesson("Art", "S. Dali", "11th grade"),
                    Lesson("Physical education", "C. Lewis", "11th grade"),
                    Lesson("Physical education", "C. Lewis", "11th grade"),
                    Lesson("Physical education", "C. Lewis", "11th grade"),

                    Lesson("Math", "A. Turing", "12th grade"),
                    Lesson("Math", "A. Turing", "12th grade"),
                    Lesson("Math", "A. Turing", "12th grade"),
                    Lesson("Math", "A. Turing", "12th grade"),
                    Lesson("Math", "A. Turing", "12th grade"),
                    Lesson("ICT", "A. Turing", "12th grade"),
                    Lesson("Physics", "M. Curie", "12th grade"),
                    Lesson("Chemistry", "M. Curie", "12th grade"),
                    Lesson("French", "M. Curie", "12th grade"),
                    Lesson("Physics", "M. Curie", "12th grade"),
                    Lesson("Geography", "C. Darwin", "12th grade"),
                    Lesson("Biology", "C. Darwin", "12th grade"),
                    Lesson("Geology", "C. Darwin", "12th grade"),
                    Lesson("History", "I. Jones", "12th grade"),
                    Lesson("History", "I. Jones", "12th grade"),
                    Lesson("English", "P. Cruz", "12th grade"),
                    Lesson("English", "P. Cruz", "12th grade"),
                    Lesson("English", "P. Cruz", "12th grade"),
                    Lesson("Spanish", "P. Cruz", "12th grade"),
                    Lesson("Drama", "P. Cruz", "12th grade"),
                    Lesson("Art", "S. Dali", "12th grade"),
                    Lesson("Art", "S. Dali", "12th grade"),
                    Lesson("Physical education", "C. Lewis", "12th grade"),
                    Lesson("Physical education", "C. Lewis", "12th grade"),
                    Lesson("Physical education", "C. Lewis", "12th grade")))
        }

        val lesson: Lesson = lessonList[0]
        lesson.timeslot = timeslotList[0]
        lesson.room = roomList[0]

        lessonRepository.persist(lessonList)
    }

    enum class DemoData {
        NONE, SMALL, LARGE
    }

}
