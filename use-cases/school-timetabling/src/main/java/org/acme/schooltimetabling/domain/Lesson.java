package org.acme.schooltimetabling.domain;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.FetchType;
import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIdentityReference;

@PlanningEntity
public class Lesson {

    @PlanningId
    private Long id;

    private String subject;
    private String teacher;
    private String studentGroup;

    @JsonIdentityReference
    @PlanningVariable
    private Timeslot timeslot;

    @JsonIdentityReference
    @PlanningVariable
    private Room room;

   @ElementCollection(fetch = FetchType.EAGER)
   private Set<String> tags;

    // No-arg constructor required for Hibernate and Timefold
    public Lesson() {
    }


    public Lesson(long id, String subject, String teacher, String studentGroup, Set<String> tags) {
        this.id = id;
        this.subject = subject;
        this.teacher = teacher;
        this.studentGroup = studentGroup;
        this.tags = tags;
    }

    public Lesson(long id, String subject, String teacher, String studentGroup, Timeslot timeslot, Room room, Set<String> tags) {
        this(id, subject, teacher, studentGroup, tags);
        this.timeslot = timeslot;
        this.room = room;
        this.tags = tags;
    }

    @Override
    public String toString() {
        return subject + "(" + id + ")";
    }

    // ************************************************************************
    // Getters and setters
    // ************************************************************************

    public Long getId() {
        return id;
    }

    public String getSubject() {
        return subject;
    }

    public String getTeacher() {
        return teacher;
    }

    public String getStudentGroup() {
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

    public static int getCount() {
        return 1;
    }

    public Set<String> getTags() {
        return tags;
    }
}
