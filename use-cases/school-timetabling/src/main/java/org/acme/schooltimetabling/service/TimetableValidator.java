package org.acme.schooltimetabling.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;

import ai.timefold.solver.service.definition.api.domain.ModelConfig;
import ai.timefold.solver.service.definition.api.validation.ModelValidator;
import ai.timefold.solver.service.definition.api.validation.ValidationBuilder;

import org.acme.schooltimetabling.dto.LessonDTO;
import org.acme.schooltimetabling.dto.LessonIdDetail;
import org.acme.schooltimetabling.dto.RoomDTO;
import org.acme.schooltimetabling.dto.RoomIdDetail;
import org.acme.schooltimetabling.dto.TimeslotDTO;
import org.acme.schooltimetabling.dto.TimeslotIdDetail;
import org.acme.schooltimetabling.dto.TimetableConfigOverrides;
import org.acme.schooltimetabling.dto.TimetableInput;
import org.acme.schooltimetabling.service.TimetableIssues.DuplicateLessonIdIssue;
import org.acme.schooltimetabling.service.TimetableIssues.DuplicateRoomIdIssue;
import org.acme.schooltimetabling.service.TimetableIssues.DuplicateTimeslotIdIssue;
import org.acme.schooltimetabling.service.TimetableIssues.LessonIdMissingIssue;
import org.acme.schooltimetabling.service.TimetableIssues.NonExistingRoomReferenceIssue;
import org.acme.schooltimetabling.service.TimetableIssues.NonExistingTimeslotReferenceIssue;
import org.acme.schooltimetabling.service.TimetableIssues.RoomIdMissingIssue;
import org.acme.schooltimetabling.service.TimetableIssues.TimeslotIdMissingIssue;

@ApplicationScoped
public class TimetableValidator implements ModelValidator<TimetableInput, TimetableConfigOverrides> {

    @Override
    public void validate(ValidationBuilder validationBuilder, TimetableInput modelInput,
            ModelConfig<TimetableConfigOverrides> modelConfig) {
        Set<String> timeslotIds = validateTimeslots(validationBuilder, modelInput.timeslots());
        Set<String> roomIds = validateRooms(validationBuilder, modelInput.rooms());
        validateLessons(validationBuilder, modelInput.lessons(), timeslotIds, roomIds);
    }

    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private Set<String> validateTimeslots(ValidationBuilder validationBuilder, List<TimeslotDTO> timeslots) {
        Set<String> timeslotIds = new HashSet<>();
        for (TimeslotDTO timeslot : timeslots) {
            if (timeslot.id() == null || timeslot.id().isBlank()) {
                validationBuilder.addIssue(new TimeslotIdMissingIssue());
            } else if (!timeslotIds.add(timeslot.id())) {
                validationBuilder.addIssue(new DuplicateTimeslotIdIssue(new TimeslotIdDetail(timeslot.id())));
            }
        }
        return timeslotIds;
    }

    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private Set<String> validateRooms(ValidationBuilder validationBuilder, List<RoomDTO> rooms) {
        Set<String> roomIds = new HashSet<>();
        for (RoomDTO room : rooms) {
            if (room.id() == null || room.id().isBlank()) {
                validationBuilder.addIssue(new RoomIdMissingIssue());
            } else if (!roomIds.add(room.id())) {
                validationBuilder.addIssue(new DuplicateRoomIdIssue(new RoomIdDetail(room.id())));
            }
        }
        return roomIds;
    }

    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private void validateLessons(ValidationBuilder validationBuilder, List<LessonDTO> lessons, Set<String> timeslotIds,
            Set<String> roomIds) {
        Set<String> lessonIds = new HashSet<>();
        for (LessonDTO lesson : lessons) {
            if (lesson.id() == null || lesson.id().isBlank()) {
                validationBuilder.addIssue(new LessonIdMissingIssue());
            } else if (!lessonIds.add(lesson.id())) {
                validationBuilder.addIssue(new DuplicateLessonIdIssue(new LessonIdDetail(lesson.id())));
            }
            if (lesson.timeslotId() != null && !timeslotIds.contains(lesson.timeslotId())) {
                validationBuilder.addIssue(new NonExistingTimeslotReferenceIssue(new LessonIdDetail(lesson.id())));
            }
            if (lesson.roomId() != null && !roomIds.contains(lesson.roomId())) {
                validationBuilder.addIssue(new NonExistingRoomReferenceIssue(new LessonIdDetail(lesson.id())));
            }
        }
    }
}
