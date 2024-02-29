package org.acme.schooltimetabling.solver;

import java.time.Duration;
//import java.util.stream.Collectors;

import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.score.buildin.simple.SimpleScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.core.api.score.stream.Joiners;
//import ai.timefold.solver.core.api.score.stream.bi.BiConstraintStream;
//import ai.timefold.solver.core.api.score.stream.uni.UniConstraintCollector;
import static ai.timefold.solver.core.api.score.stream.ConstraintCollectors.sum;

import org.acme.schooltimetabling.domain.Lesson;
import org.acme.schooltimetabling.domain.Timeslot;
//import org.acme.schooltimetabling.domain.Lesson.getCount;
//import org.acme.schooltimetabling.domain.Room;
//import org.acme.schooltimetabling.domain.Timeslot;
import org.acme.schooltimetabling.solver.justifications.RoomConflictJustification;
import org.acme.schooltimetabling.solver.justifications.StudentGroupConflictJustification;
import org.acme.schooltimetabling.solver.justifications.StudentGroupSubjectVarietyJustification;
import org.acme.schooltimetabling.solver.justifications.TeacherConflictJustification;
import org.acme.schooltimetabling.solver.justifications.TeacherRoomStabilityJustification;
import org.acme.schooltimetabling.solver.justifications.TeacherTimeEfficiencyJustification;

public class TimetableConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
                // Hard constraints
                //roomConflict(constraintFactory),
                //teacherConflict(constraintFactory),
                scoutKPConflict(constraintFactory),
                studentGroupConflict(constraintFactory),
                // Soft constraints
                blockDayOneBreakfastAndLunchKP(constraintFactory),
                //teacherRoomStability(constraintFactory),
                //teacherTimeEfficiency(constraintFactory),
                //studentGroupSubjectVariety(constraintFactory),
                kpPerShiftMaxGtFive(constraintFactory)
        };
    }

    Constraint roomConflict(ConstraintFactory constraintFactory) {
        // A room can accommodate at most one lesson at the same time.
        return constraintFactory
                // Select each pair of 2 different lessons ...
                .forEachUniquePair(Lesson.class,
                        // ... in the same timeslot ...
                        Joiners.equal(Lesson::getTimeslot),
                        // ... in the same room ...
                        Joiners.equal(Lesson::getRoom))
                // ... and penalize each pair with a hard weight.
                .penalize(HardSoftScore.ONE_HARD)
                .justifyWith((lesson1, lesson2, score) -> new RoomConflictJustification(lesson1.getRoom(), lesson1, lesson2))
                .asConstraint("Room conflict");
    }

    Constraint kpPerShiftMaxGtFive(ConstraintFactory constraintFactory) {
        // A room can accommodate at most one lesson at the same time.
        return constraintFactory

                .forEach(Lesson.class)
                .groupBy(lesson -> lesson.getTimeslot(),
                         lesson -> lesson.getRoom(),
                 sum(lesson -> Lesson.getCount()))
                .filter((timeslot,  room, getCount) -> getCount > 5)
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Timeslot capacity exceeded");

    }


      
        
        /* .forEach(Lesson.class)
        .groupBy(Lesson::getTimeslot, Lesson::getRoom, 
        toList()).filter(((timeslot, room, lessons) -> {var studentGroups = lessons.stream()
                .map(Lesson::getStudentGroup).collect(Collectors.toSet());                      
                return studentGroups.size() <= 5;}))
                .penalize("Room capacity exceeded", HardSoftScore.ONE_HARD); */
                
    Constraint scoutKPConflict(ConstraintFactory constraintFactory) {
        // A scout covers one KP duty for a timeslot
        return constraintFactory
                .forEach(Lesson.class)
                .join(Lesson.class,
                        Joiners.equal(Lesson::getRoom),
                        Joiners.equal(Lesson::getTimeslot),
                        Joiners.equal((lesson) -> lesson.getTeacher()))
                //.filter((lesson1, lesson2) -> lesson1.getRoom() != lesson2.getRoom() && lesson1.getTimeslot() != lesson2.getTimeslot())
                .penalize(HardSoftScore.ONE_HARD)
                .justifyWith(
                        (lesson1, lesson2, score) -> new TeacherConflictJustification(lesson1.getTeacher(), lesson1, lesson2))
                .asConstraint("Scout KP conflict");
    }



Constraint teacherConflict(ConstraintFactory constraintFactory) {
        // A teacher can teach at most one lesson at the same time.
        return constraintFactory
                .forEachUniquePair(Lesson.class,
                        Joiners.equal(Lesson::getTimeslot),
                        Joiners.equal(Lesson::getTeacher))
                .penalize(HardSoftScore.ONE_HARD)
                .justifyWith(
                        (lesson1, lesson2, score) -> new TeacherConflictJustification(lesson1.getTeacher(), lesson1, lesson2))
                .asConstraint("Teacher conflict");
    }

    Constraint studentGroupConflict(ConstraintFactory constraintFactory) {
        // A student can attend at most one lesson at the same time.
        return constraintFactory
                .forEachUniquePair(Lesson.class,
                        Joiners.equal(Lesson::getTimeslot),
                        Joiners.equal(Lesson::getStudentGroup))
                .penalize(HardSoftScore.ONE_HARD)
                .justifyWith((lesson1, lesson2, score) -> new StudentGroupConflictJustification(lesson1.getStudentGroup(), lesson1, lesson2))
                .asConstraint("Student group conflict");
    }

    Constraint teacherRoomStability(ConstraintFactory constraintFactory) {
        // A teacher prefers to teach in a single room.
        return constraintFactory
                .forEachUniquePair(Lesson.class,
                        Joiners.equal(Lesson::getTeacher))
                .filter((lesson1, lesson2) -> lesson1.getRoom() != lesson2.getRoom())
                .penalize(HardSoftScore.ONE_SOFT)
                .justifyWith((lesson1, lesson2, score) -> new TeacherRoomStabilityJustification(lesson1.getTeacher(), lesson1, lesson2))
                .asConstraint("Teacher room stability");
    }

    Constraint blockDayOneBreakfastAndLunchKP(ConstraintFactory constraintFactory) {
        // No KP should be scheduled on Day One Breakfast or Lunch
        return constraintFactory
                .forEach(Lesson.class)
                .groupBy(lesson -> lesson.getTimeslot(),
                         lesson -> lesson.getRoom())
                .filter((timeslot,  room) -> (timeslot.getName().equals("Breakfast") && room.getName().equals("Day 1")) || (timeslot.getName().equals("Lunch") && room.getName().equals("Day 1")))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Day 1 is blocked for KP");
    }

    /* 
    Constraint teacherTimeEfficiency(ConstraintFactory constraintFactory) {
        // A teacher prefers to teach sequential lessons and dislikes gaps between lessons.
        return constraintFactory
                .forEachUniquePair(Lesson.class,
                        Joiners.equal(Lesson::getTeacher),
                        /* Joiners.equal((lesson) -> lesson.getTimeslot().getDayOfWeek())) 
                        Joiners.equal((lesson) -> lesson.getTimeslot().getLocalDate()))
                .filter((lesson1, lesson2) -> {
                    Duration between = Duration.between(lesson1.getTimeslot().getEndTime(),
                            lesson2.getTimeslot().getStartTime());
                    return !between.isNegative() && between.compareTo(Duration.ofMinutes(30)) <= 0;
                })
                .reward(HardSoftScore.ONE_SOFT)
                .justifyWith((lesson1, lesson2, score) -> new TeacherTimeEfficiencyJustification(lesson1.getTeacher(), lesson1, lesson2))
                .asConstraint("Teacher time efficiency");
    } 

    Constraint studentGroupSubjectVariety(ConstraintFactory constraintFactory) {
        // A student group dislikes sequential lessons on the same subject.
        return constraintFactory
                .forEach(Lesson.class)
                .join(Lesson.class,
                        Joiners.equal(Lesson::getSubject),
                        Joiners.equal(Lesson::getStudentGroup),
                        /*Joiners.equal((lesson) -> lesson.getTimeslot().getDayOfWeek())) 
                        Joiners.equal((lesson) -> lesson.getTimeslot().getLocalDate()))
                .filter((lesson1, lesson2) -> {
                    Duration between = Duration.between(lesson1.getTimeslot().getEndTime(),
                            lesson2.getTimeslot().getStartTime());
                    return !between.isNegative() && between.compareTo(Duration.ofMinutes(30)) <= 0;
                })
                .penalize(HardSoftScore.ONE_SOFT)
                .justifyWith((lesson1, lesson2, score) -> new StudentGroupSubjectVarietyJustification(lesson1.getStudentGroup(), lesson1, lesson2))
                .asConstraint("Student group subject variety");
    }
    */

}
