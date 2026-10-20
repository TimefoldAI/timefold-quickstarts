package org.acme.schooltimetabling.service;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import jakarta.enterprise.context.ApplicationScoped;

import ai.timefold.solver.service.definition.api.domain.ModelConfig;
import ai.timefold.solver.service.definition.api.validation.ModelValidator;
import ai.timefold.solver.service.definition.api.validation.ValidationBuilder;

import org.acme.schooltimetabling.dto.input.LessonInputDTO;
import org.acme.schooltimetabling.dto.input.RoomInputDTO;
import org.acme.schooltimetabling.dto.input.TimeslotInputDTO;
import org.acme.schooltimetabling.dto.input.TimetableConfigOverrides;
import org.acme.schooltimetabling.dto.input.TimetableInput;
import org.acme.schooltimetabling.service.validation.TimetableIssue.DuplicateLessonIdIssue;
import org.acme.schooltimetabling.service.validation.TimetableIssue.DuplicateRoomIdIssue;
import org.acme.schooltimetabling.service.validation.TimetableIssue.DuplicateTimeslotIdIssue;
import org.acme.schooltimetabling.service.validation.TimetableIssue.InsufficientRoomCapacityIssue;
import org.acme.schooltimetabling.service.validation.TimetableIssue.InvalidTimeslotTimeRangeIssue;
import org.acme.schooltimetabling.service.validation.TimetableIssue.NonExistingRoomReferenceIssue;
import org.acme.schooltimetabling.service.validation.TimetableIssue.NonExistingTimeslotReferenceIssue;
import org.acme.schooltimetabling.service.validation.TimetableIssue.OverloadedStudentGroupIssue;
import org.acme.schooltimetabling.service.validation.TimetableIssue.OverloadedTeacherIssue;

@ApplicationScoped
public class TimetableValidator implements ModelValidator<TimetableInput, TimetableConfigOverrides> {

    @Override
    public void validate(ValidationBuilder validationBuilder, TimetableInput modelInput,
            ModelConfig<TimetableConfigOverrides> modelConfig) {
        // OpenAPI spec compliance is enforced by the Service module at the REST layer, before this validator ever
        // runs; only domain-specific checks belong here.
        List<TimeslotInputDTO> timeslots = orEmpty(modelInput.timeslots());
        List<RoomInputDTO> rooms = orEmpty(modelInput.rooms());
        List<LessonInputDTO> lessons = orEmpty(modelInput.lessons());
        Set<String> timeslotIds = validateTimeslots(validationBuilder, timeslots);
        Set<String> roomIds = validateRooms(validationBuilder, rooms);
        validateLessons(validationBuilder, lessons, timeslotIds, roomIds);
        validateCapacity(validationBuilder, lessons, timeslotIds.size(), roomIds.size());
    }

    private static Set<String> validateTimeslots(ValidationBuilder validationBuilder,
            List<TimeslotInputDTO> timeslots) {
        Set<String> timeslotIds = new HashSet<>();
        for (TimeslotInputDTO timeslot : timeslots) {
            if (hasId(timeslot.id()) && !timeslotIds.add(timeslot.id())) {
                validationBuilder.addIssue(new DuplicateTimeslotIdIssue(timeslot.id()));
            }
            if (timeslot.startTime() != null && timeslot.endTime() != null
                    && !timeslot.endTime().isAfter(timeslot.startTime())) {
                validationBuilder.addIssue(new InvalidTimeslotTimeRangeIssue(timeslot.id()));
            }
        }
        return timeslotIds;
    }

    private static Set<String> validateRooms(ValidationBuilder validationBuilder, List<RoomInputDTO> rooms) {
        Set<String> roomIds = new HashSet<>();
        for (RoomInputDTO room : rooms) {
            if (hasId(room.id()) && !roomIds.add(room.id())) {
                validationBuilder.addIssue(new DuplicateRoomIdIssue(room.id()));
            }
        }
        return roomIds;
    }

    private static void validateLessons(ValidationBuilder validationBuilder, List<LessonInputDTO> lessons,
            Set<String> timeslotIds, Set<String> roomIds) {
        Set<String> lessonIds = new HashSet<>();
        for (LessonInputDTO lesson : lessons) {
            if (hasId(lesson.id()) && !lessonIds.add(lesson.id())) {
                validationBuilder.addIssue(new DuplicateLessonIdIssue(lesson.id()));
            }
            if (lesson.timeslotId() != null && !timeslotIds.contains(lesson.timeslotId())) {
                validationBuilder.addIssue(new NonExistingTimeslotReferenceIssue(lesson.id()));
            }
            if (lesson.roomId() != null && !roomIds.contains(lesson.roomId())) {
                validationBuilder.addIssue(new NonExistingRoomReferenceIssue(lesson.id()));
            }
        }
    }

    /**
     * Catches the datasets that can never reach a feasible score, because there are not enough timeslots to
     * keep the rooms, a teacher or a student group from being double-booked. One issue per overloaded teacher
     * or student group, so the issue list stays bounded by the number of distinct names.
     */
    private static void validateCapacity(ValidationBuilder validationBuilder, List<LessonInputDTO> lessons,
            int timeslotCount, int roomCount) {
        int capacity = timeslotCount * roomCount;
        if (lessons.size() > capacity) {
            validationBuilder.addIssue(new InsufficientRoomCapacityIssue(lessons.size(), capacity));
        }
        countLessonsBy(lessons, LessonInputDTO::teacher).forEach((teacher, lessonCount) -> {
            if (lessonCount > timeslotCount) {
                validationBuilder.addIssue(new OverloadedTeacherIssue(teacher, lessonCount));
            }
        });
        countLessonsBy(lessons, LessonInputDTO::studentGroup).forEach((studentGroup, lessonCount) -> {
            if (lessonCount > timeslotCount) {
                validationBuilder.addIssue(new OverloadedStudentGroupIssue(studentGroup, lessonCount));
            }
        });
    }

    private static Map<String, Integer> countLessonsBy(List<LessonInputDTO> lessons,
            Function<LessonInputDTO, String> keyFunction) {
        Map<String, Integer> lessonCountByKey = new LinkedHashMap<>();
        for (LessonInputDTO lesson : lessons) {
            String key = keyFunction.apply(lesson);
            if (key != null) {
                lessonCountByKey.merge(key, 1, Integer::sum);
            }
        }
        return lessonCountByKey;
    }

    private static <T> List<T> orEmpty(List<T> list) {
        return list == null ? List.of() : list;
    }

    private static boolean hasId(String id) {
        return id != null && !id.isEmpty();
    }
}
