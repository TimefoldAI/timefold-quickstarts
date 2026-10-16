package org.acme.schooltimetabling.domain;

import java.time.Duration;
import java.util.Objects;

import ai.timefold.solver.core.api.domain.common.PlanningId;
import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;

/**
 * A lesson of one subject, taught by one teacher to one student group. The solver picks the
 * {@link Timeslot} and the {@link Room} it takes place in.
 */
@PlanningEntity
public class Lesson {

    /** Two lessons on the same day with at most this long a break between them count as consecutive. */
    private static final Duration MAX_BREAK_BETWEEN_CONSECUTIVE_LESSONS = Duration.ofMinutes(30);

    @PlanningId
    private String id;

    private String subject;
    private String teacher;
    private String studentGroup;

    @PlanningVariable
    private Timeslot timeslot;
    @PlanningVariable
    private Room room;

    public Lesson() {
    }

    public Lesson(String id, String subject, String teacher, String studentGroup, Timeslot timeslot, Room room) {
        this.id = id;
        this.subject = subject;
        this.teacher = teacher;
        this.studentGroup = studentGroup;
        this.timeslot = timeslot;
        this.room = room;
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

    // ************************************************************************
    // Complex methods
    // ************************************************************************

    public boolean isAssigned() {
        return timeslot != null && room != null;
    }

    /**
     * @param other never null, assigned to a timeslot on the same day as this lesson
     * @return true if the other lesson starts after this one ends, with at most a short break in between
     */
    public boolean isDirectlyFollowedBy(Lesson other) {
        Duration between = Duration.between(timeslot.endTime(), other.getTimeslot().startTime());
        return !between.isNegative() && between.compareTo(MAX_BREAK_BETWEEN_CONSECUTIVE_LESSONS) <= 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Lesson lesson)) {
            return false;
        }
        return Objects.equals(id, lesson.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return subject + "(" + id + ")";
    }
}
