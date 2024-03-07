package org.acme.schooltimetabling.solver;

import java.time.Duration;
import java.util.Arrays;
//import java.util.stream.Collectors;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import ai.timefold.solver.core.api.score.Score;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.score.buildin.simple.SimpleScore;
import ai.timefold.solver.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.core.api.score.stream.Joiners;
//import ai.timefold.solver.core.api.score.stream.bi.BiConstraintStream;
//import ai.timefold.solver.core.api.score.stream.uni.UniConstraintCollector;
import static ai.timefold.solver.core.api.score.stream.ConstraintCollectors.sum;
import static ai.timefold.solver.core.api.score.stream.Joiners.filtering;
import static ai.timefold.solver.core.api.score.stream.Joiners.overlapping;

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
import org.hibernate.mapping.Join;

public class TimetableConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
                // Hard constraints
                //roomConflict(constraintFactory),
                //teacherConflict(constraintFactory),
                scoutKPConflict(constraintFactory),
                //studentGroupConflict(constraintFactory),
                // Soft constraints
                keepMonkeyPatrolTogether(constraintFactory),
                keepDogPatrolTogether(constraintFactory),
                blockDayOneAndDayTenKP(constraintFactory),
                //groupACanNotKPBreakfast(constraintFactory),
                day2LunchKPNotAvailable(constraintFactory),
                day2SupperKPNotAvailable(constraintFactory),
                day3LunchKPNotAvailable(constraintFactory),
                day3SupperKPNotAvailable(constraintFactory),
                kpPerShiftMaxGtFivePenalize(constraintFactory)
                //teacherRoomStability(constraintFactory),
                //teacherTimeEfficiency(constraintFactory),
                //studentGroupSubjectVariety(constraintFactory),
                //kpPerShiftMaxGtFive(constraintFactory)
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
                .filter((timeslot,  room, getCount) -> getCount < 4)
                .reward(HardSoftScore.ONE_SOFT)
                .asConstraint("Timeslot capacity exceeded");

    }

    Constraint kpPerShiftMaxGtFivePenalize(ConstraintFactory constraintFactory) {
        // A room can accommodate at most one lesson at the same time.
        return constraintFactory

                .forEach(Lesson.class)
                .groupBy(lesson -> lesson.getTimeslot(),
                         lesson -> lesson.getRoom(),
                 sum(lesson -> Lesson.getCount()))
                .filter((timeslot,  room, getCount) -> getCount > 4)
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Timeslot capacity exceeded");

    }

    Constraint keepMonkeyPatrolTogether(ConstraintFactory constraintFactory) {
        return constraintFactory
        .forEach(Lesson.class)
        //.groupBy(lesson -> lesson.getTimeslot(),
        //         lesson -> lesson.getRoom(),
        //         lesson -> lesson.getSubject())
        .filter(lesson -> (lesson.getTimeslot().getName().equals("Breakfast") && lesson.getRoom().getName().equals("Day 2") && lesson.getSubject().equals("Monkeys"))
                           || (lesson.getTimeslot().getName().equals("Breakfast") && lesson.getRoom().getName().equals("Day 5") && lesson.getSubject().equals("Monkeys"))
                           || (lesson.getTimeslot().getName().equals("Breakfast") && lesson.getRoom().getName().equals("Day 8") && lesson.getSubject().equals("Monkeys"))
                           || (lesson.getTimeslot().getName().equals("Supper") && lesson.getRoom().getName().equals("Day 2") && lesson.getSubject().equals("Monkeys"))
                           || (lesson.getTimeslot().getName().equals("Supper") && lesson.getRoom().getName().equals("Day 5") && lesson.getSubject().equals("Monkeys"))
                           || (lesson.getTimeslot().getName().equals("Supper") && lesson.getRoom().getName().equals("Day 8") && lesson.getSubject().equals("Monkeys")))
                .reward(HardSoftScore.ONE_SOFT)
                //                                     || (timeslot.getName().equals("Breakfast") && room.getName().equals("Day 5") && subject.equals("Monkeys"))))
                //.justifyWith((lesson1, lesson2, score) -> new StudentGroupSubjectVarietyJustification(lesson1.getStudentGroup(), lesson1, lesson2))
                .asConstraint("Monkey Patrol Together");
    }

    Constraint keepDogPatrolTogether(ConstraintFactory constraintFactory) {
        return constraintFactory
        .forEach(Lesson.class)
        //.groupBy(lesson -> lesson.getTimeslot(),
        //         lesson -> lesson.getRoom(),
        //         lesson -> lesson.getSubject())
        .filter(lesson -> (lesson.getTimeslot().getName().equals("Breakfast") && lesson.getRoom().getName().equals("Day 3") && lesson.getSubject().equals("Dogs"))
                           || (lesson.getTimeslot().getName().equals("Breakfast") && lesson.getRoom().getName().equals("Day 6") && lesson.getSubject().equals("Dogs"))
                           || (lesson.getTimeslot().getName().equals("Breakfast") && lesson.getRoom().getName().equals("Day 9") && lesson.getSubject().equals("Dogs"))
                           || (lesson.getTimeslot().getName().equals("Supper") && lesson.getRoom().getName().equals("Day 3") && lesson.getSubject().equals("Dogs"))
                           || (lesson.getTimeslot().getName().equals("Supper") && lesson.getRoom().getName().equals("Day 6") && lesson.getSubject().equals("Dogs"))
                           || (lesson.getTimeslot().getName().equals("Supper") && lesson.getRoom().getName().equals("Day 9") && lesson.getSubject().equals("Dogs")))
                .reward(HardSoftScore.ONE_SOFT)
                //                                     || (timeslot.getName().equals("Breakfast") && room.getName().equals("Day 5") && subject.equals("Monkeys"))))
                //.justifyWith((lesson1, lesson2, score) -> new StudentGroupSubjectVarietyJustification(lesson1.getStudentGroup(), lesson1, lesson2))
                .asConstraint("Dogs Patrol Together");
    }

    Constraint groupACanNotKPBreakfast(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEach(Lesson.class)
                .groupBy(lesson -> lesson.getTimeslot(),
                         lesson -> lesson.getRoom(),
                         lesson -> lesson.getStudentGroup())
                .filter((timeslot, room, studentGroup) -> (timeslot.getName().equals("Breakfast") && studentGroup.equals("Group A")))
                //.filter((lesson1, lesson2) -> {
                //    Duration between = Duration.between(lesson1.getTimeslot().getEndTime(),
                //            lesson2.getTimeslot().getStartTime());
                //    return !between.isNegative() && between.compareTo(Duration.ofMinutes(30)) <= 0;
                .penalize(HardSoftScore.ONE_SOFT)
                //.justifyWith((lesson1, lesson2, score) -> new StudentGroupSubjectVarietyJustification(lesson1.getStudentGroup(), lesson1, lesson2))
                .asConstraint("Student group subject variety");
    }
/* 
    public Constraint tagConflict(ConstraintFactory constraintFactory) {
        // Avoid overlapping maintenance jobs with the same tag (for example road maintenance in the same area).
        return constraintFactory
                .forEachUniquePair(Job.class,
                        overlapping(Job::getStartDate, Job::getEndDate),
                        // TODO Use intersecting() when available https://github.com/TimefoldAI/timefold-solver/issues/8
                        filtering((job1, job2) -> !Collections.disjoint(
                                job1.getTags(), job2.getTags())))
                .penalizeLong(HardSoftLongScore.ofSoft(1_000),
                        (job1, job2) -> {
                            Set<String> intersection = new HashSet<>(job1.getTags());
                            intersection.retainAll(job2.getTags());
                            long overlap = DAYS.between(
                                    job1.getStartDate().isAfter(job2.getStartDate())
                                            ? job1.getStartDate()  : job2.getStartDate(),
                                    job1.getEndDate().isBefore(job2.getEndDate())
                                            ? job1.getEndDate() : job2.getEndDate());
                            return intersection.size() * overlap;
                        })
                .asConstraint("Tag conflict");
    } */

    Constraint day2LunchKPNotAvailable(ConstraintFactory constraintFactory) {
 
        return constraintFactory
                .forEach(Lesson.class)
                .groupBy(lesson -> lesson.getTimeslot(),
                         lesson -> lesson.getRoom(),
                         lesson -> lesson.getTags())
                .filter((timeslot, room, tags) ->  room.getName().equals("Day 2")
                                                && timeslot.getName().equals("Lunch"))
                .penalize(HardSoftScore.ONE_HARD,
                        (timeslot, room, tags) -> { 
                                
                                //timeslot.getName().equals("Lunch"); 
                                //room.getName().equals("Day 3");
                                //room.getName().equals("Day 5");
                                Set<String> intersection = new HashSet<>(tags);
                                Set<String> tagblock = new HashSet<>(Arrays.asList("D2L"));
                                int countEqualMatches = 0;
                                for(String a : intersection){
                                        for(String b : tagblock){
                                                if(a.equals(b)){
                                                        countEqualMatches++;
                                                }
                                        }
                                }
                                return countEqualMatches * intersection.size();
                        })

                .asConstraint("Day 2 - Lunch Not Available");
    }

    Constraint day2SupperKPNotAvailable(ConstraintFactory constraintFactory) {
 
        return constraintFactory
                .forEach(Lesson.class)
                .groupBy(lesson -> lesson.getTimeslot(),
                         lesson -> lesson.getRoom(),
                         lesson -> lesson.getTags())
                .filter((timeslot, room, tags) ->  room.getName().equals("Day 2")
                                                && timeslot.getName().equals("Supper"))
                .penalize(HardSoftScore.ONE_HARD,
                        (timeslot, room, tags) -> { 
                                
                                //timeslot.getName().equals("Lunch"); 
                                //room.getName().equals("Day 3");
                                //room.getName().equals("Day 5");
                                Set<String> intersection = new HashSet<>(tags);
                                Set<String> tagblock = new HashSet<>(Arrays.asList("D2S"));
                                int countEqualMatches = 0;
                                for(String a : intersection){
                                        for(String b : tagblock){
                                                if(a.equals(b)){
                                                        countEqualMatches++;
                                                }
                                        }
                                }
                                return countEqualMatches * intersection.size();
                        })

                .asConstraint("Day 2 - Supper Not Available");
    }

    Constraint day3LunchKPNotAvailable(ConstraintFactory constraintFactory) {
 
        return constraintFactory
                .forEach(Lesson.class)
                .groupBy(lesson -> lesson.getTimeslot(),
                         lesson -> lesson.getRoom(),
                         lesson -> lesson.getTags())
                .filter((timeslot, room, tags) ->  room.getName().equals("Day 3")
                                                && timeslot.getName().equals("Lunch"))
                .penalize(HardSoftScore.ONE_HARD,
                        (timeslot, room, tags) -> { 
                                
                                //timeslot.getName().equals("Lunch"); 
                                //room.getName().equals("Day 3");
                                //room.getName().equals("Day 5");
                                Set<String> intersection = new HashSet<>(tags);
                                Set<String> tagblock = new HashSet<>(Arrays.asList("D3L"));
                                int countEqualMatches = 0;
                                for(String a : intersection){
                                        for(String b : tagblock){
                                                if(a.equals(b)){
                                                        countEqualMatches++;
                                                }
                                        }
                                }
                                return countEqualMatches * intersection.size();
                        })

                .asConstraint("Day 3 - Lunch Not Available");
    }

    Constraint day3SupperKPNotAvailable(ConstraintFactory constraintFactory) {
 
        return constraintFactory
                .forEach(Lesson.class)
                .groupBy(lesson -> lesson.getTimeslot(),
                         lesson -> lesson.getRoom(),
                         lesson -> lesson.getTags())
                .filter((timeslot, room, tags) ->  room.getName().equals("Day 3")
                                                && timeslot.getName().equals("Supper"))
                .penalize(HardSoftScore.ONE_HARD,
                        (timeslot, room, tags) -> { 
                                
                                //timeslot.getName().equals("Lunch"); 
                                //room.getName().equals("Day 3");
                                //room.getName().equals("Day 5");
                                Set<String> intersection = new HashSet<>(tags);
                                Set<String> tagblock = new HashSet<>(Arrays.asList("D2S"));
                                int countEqualMatches = 0;
                                for(String a : intersection){
                                        for(String b : tagblock){
                                                if(a.equals(b)){
                                                        countEqualMatches++;
                                                }
                                        }
                                }
                                return countEqualMatches * intersection.size();
                        })

                .asConstraint("Day 3 - Supper Not Available");
    }

        

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

    Constraint blockDayOneAndDayTenKP(ConstraintFactory constraintFactory) {
        // No KP should be scheduled on Day One Breakfast or Lunch
        return constraintFactory
                .forEach(Lesson.class)
                .groupBy(lesson -> lesson.getTimeslot(),
                         lesson -> lesson.getRoom())
                .filter((timeslot,  room) -> (timeslot.getName().equals("Breakfast") && room.getName().equals("Day 1")) || (timeslot.getName().equals("Lunch") && room.getName().equals("Day 1")) || (timeslot.getName().equals("Lunch") && room.getName().equals("Day 10")) || (timeslot.getName().equals("Supper") && room.getName().equals("Day 10")))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Day 1 Breakfast and Lunch and Day 10 Lunch and Supper is blocked for KP");
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

    Constraint scoutPatrolSameDay(ConstraintFactory constraintFactory) {
        // A student group dislikes sequential lessons on the same subject.
        return constraintFactory
                .forEach(Lesson.class)
                .join(Lesson.class,
                        Joiners.equal(Lesson::getSubject),
                        Joiners.equal(Lesson::getRoom),
                        Joiners.equal((lesson::getTimeslot)) 
                .filter((lesson1, lesson2) -> {
                    Duration between = Duration.between(lesson1.getTimeslot().getEndTime(),
                            lesson2.getTimeslot().getStartTime());
                    return !between.isNegative() && between.compareTo(Duration.ofMinutes(30)) <= 0;
                })
                .penalize(HardSoftScore.ONE_SOFT)
                .justifyWith((lesson1, lesson2, score) -> new StudentGroupSubjectVarietyJustification(lesson1.getStudentGroup(), lesson1, lesson2))
                .asConstraint("Student group subject variety");
    }*/
    

} 
