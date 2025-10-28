package org.acme.schooltimetabling.domain;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;
import com.fasterxml.jackson.annotation.JsonIdentityReference;

@PlanningEntity
public class Lesson {

    @PlanningId
    private String id;

    private String subject;
    private Teacher teacher; // CHANGED from String to Teacher
    private StudentGroup studentGroup;

    @JsonIdentityReference
    @PlanningVariable
    private Timeslot timeslot;

    @JsonIdentityReference
    @PlanningVariable
    private Room room;

    public Lesson() {
    }

    // UPDATED CONSTRUCTOR - Teacher parameter changed to Teacher object
    public Lesson(String id, String subject, Teacher teacher, StudentGroup studentGroup) {
        this.id = id;
        this.subject = subject;
        this.teacher = teacher;
        this.studentGroup = studentGroup;
    }

    // UPDATED CONSTRUCTOR - Teacher parameter changed to Teacher object
    public Lesson(String id, String subject, Teacher teacher, StudentGroup studentGroup, Timeslot timeslot, Room room) {
        this(id, subject, teacher, studentGroup);
        this.timeslot = timeslot;
        this.room = room;
    }

    @Override
    public String toString() {
        return subject + "(" + id + ")";
    }

    // ************************************************************************
    // Getters and setters
    // ************************************************************************

    public String getId() {
        return id;
    }

    public String getSubject() {
        return subject;
    }

    // UPDATED GETTER - returns Teacher object instead of String
    public Teacher getTeacher() {
        return teacher;
    }

    public StudentGroup getStudentGroup() {
        return studentGroup;
    }

    public Timeslot getTimeslot() {
        return timeslot;
    }

    public void setTimeslot(Timeslot timeslot) {
        this.timeslot = timeslot;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }
}