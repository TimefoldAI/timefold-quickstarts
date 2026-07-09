package org.acme.schooltimetabling.service;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;

import ai.timefold.solver.core.api.domain.solution.ConstraintWeightOverrides;
import ai.timefold.solver.core.api.score.HardMediumSoftScore;
import ai.timefold.solver.service.definition.api.ModelConvertor;
import ai.timefold.solver.service.definition.api.domain.ModelConfig;

import org.acme.schooltimetabling.domain.Lesson;
import org.acme.schooltimetabling.domain.Room;
import org.acme.schooltimetabling.domain.Timeslot;
import org.acme.schooltimetabling.domain.Timetable;
import org.acme.schooltimetabling.dto.LessonDTO;
import org.acme.schooltimetabling.dto.RoomDTO;
import org.acme.schooltimetabling.dto.TimeslotDTO;
import org.acme.schooltimetabling.dto.TimetableConfigOverrides;
import org.acme.schooltimetabling.dto.TimetableInput;
import org.acme.schooltimetabling.dto.TimetableOutput;
import org.acme.schooltimetabling.solver.TimetableConstraintProvider;

@ApplicationScoped
public class TimetableModelConvertor
        implements
        ModelConvertor<HardMediumSoftScore, TimetableInput, TimetableConfigOverrides, Timetable, TimetableOutput> {

    @Override
    public TimetableInput applyOutputToInput(TimetableInput modelInput, TimetableOutput modelOutput) {
        Map<String, LessonDTO> outputLessons = modelOutput.lessons().stream()
                .collect(Collectors.toMap(LessonDTO::id, lesson -> lesson));
        List<LessonDTO> updatedLessons = modelInput.lessons().stream()
                .map(lesson -> {
                    LessonDTO solved = outputLessons.get(lesson.id());
                    if (solved == null) {
                        return lesson;
                    }
                    return lesson.withTimeslotId(solved.timeslotId()).withRoomId(solved.roomId());
                })
                .collect(Collectors.toList());
        return new TimetableInput(updatedLessons, modelInput.timeslots(), modelInput.rooms());
    }

    @Override
    public Timetable toSolverModel(TimetableInput modelInput, ModelConfig<TimetableConfigOverrides> modelConfig,
            Optional<TimetableOutput> lastModelOutput) {
        Map<String, Timeslot> timeslotMap = new HashMap<>();
        List<Timeslot> timeslots = modelInput.timeslots().stream().map(dto -> {
            Timeslot timeslot = new Timeslot(dto.id(), DayOfWeek.valueOf(dto.dayOfWeek()),
                    LocalTime.parse(dto.startTime()), LocalTime.parse(dto.endTime()));
            timeslotMap.put(timeslot.getId(), timeslot);
            return timeslot;
        }).collect(Collectors.toList());

        Map<String, Room> roomMap = new HashMap<>();
        List<Room> rooms = modelInput.rooms().stream().map(dto -> {
            Room room = new Room(dto.id(), dto.name());
            roomMap.put(room.getId(), room);
            return room;
        }).collect(Collectors.toList());

        List<Lesson> lessons = modelInput.lessons().stream().map(dto -> {
            Lesson lesson = new Lesson(dto.id(), dto.subject(), dto.teacher(), dto.studentGroup());
            if (dto.timeslotId() != null) {
                lesson.setTimeslot(timeslotMap.get(dto.timeslotId()));
            }
            if (dto.roomId() != null) {
                lesson.setRoom(roomMap.get(dto.roomId()));
            }
            return lesson;
        }).collect(Collectors.toList());

        Timetable timetable = new Timetable(timeslots, rooms, lessons);
        applyConstraintWeightOverrides(timetable, modelConfig);
        applyLastOutput(lessons, timeslotMap, roomMap, lastModelOutput);
        return timetable;
    }

    private static void applyConstraintWeightOverrides(Timetable timetable,
            ModelConfig<TimetableConfigOverrides> modelConfig) {
        if (modelConfig == null || modelConfig.overrides() == null) {
            return;
        }
        TimetableConfigOverrides overrides = modelConfig.overrides();
        // Only apply weights that are actually set (non-null) in the merged overrides. A null weight means the
        // input did not override it, so the configuration profile value (or the constraint's default) is kept.
        Map<String, HardMediumSoftScore> weights = new HashMap<>();
        putIfPresent(weights, TimetableConstraintProvider.TEACHER_ROOM_STABILITY,
                overrides.teacherRoomStabilityWeight());
        putIfPresent(weights, TimetableConstraintProvider.TEACHER_TIME_EFFICIENCY,
                overrides.teacherTimeEfficiencyWeight());
        putIfPresent(weights, TimetableConstraintProvider.STUDENT_GROUP_SUBJECT_VARIETY,
                overrides.studentGroupSubjectVarietyWeight());
        if (!weights.isEmpty()) {
            timetable.setConstraintWeightOverrides(ConstraintWeightOverrides.of(weights));
        }
    }

    private static void putIfPresent(Map<String, HardMediumSoftScore> weights, String constraintName, Long weight) {
        if (weight != null) {
            weights.put(constraintName, HardMediumSoftScore.ofSoft(weight));
        }
    }

    private static void applyLastOutput(List<Lesson> lessons, Map<String, Timeslot> timeslotMap,
            Map<String, Room> roomMap, Optional<TimetableOutput> lastModelOutput) {
        if (lastModelOutput.isEmpty()) {
            return;
        }
        Map<String, LessonDTO> assignmentMap = lastModelOutput.get().lessons().stream()
                .collect(Collectors.toMap(LessonDTO::id, lesson -> lesson));
        for (Lesson lesson : lessons) {
            LessonDTO solved = assignmentMap.get(lesson.getId());
            if (solved == null) {
                continue;
            }
            if (solved.timeslotId() != null) {
                lesson.setTimeslot(timeslotMap.get(solved.timeslotId()));
            }
            if (solved.roomId() != null) {
                lesson.setRoom(roomMap.get(solved.roomId()));
            }
        }
    }

    @Override
    public TimetableOutput toModelOutput(Timetable solverModel) {
        List<LessonDTO> lessons = solverModel.getLessons().stream().map(this::toDTO).collect(Collectors.toList());
        List<TimeslotDTO> timeslots = solverModel.getTimeslots().stream().map(this::toDTO).collect(Collectors.toList());
        List<RoomDTO> rooms = solverModel.getRooms().stream().map(this::toDTO).collect(Collectors.toList());
        String score = solverModel.getScore() == null ? "" : solverModel.getScore().toString();
        return new TimetableOutput(lessons, timeslots, rooms, score);
    }

    private TimeslotDTO toDTO(Timeslot timeslot) {
        return new TimeslotDTO(timeslot.getId(), timeslot.getDayOfWeek().name(), timeslot.getStartTime().toString(),
                timeslot.getEndTime().toString());
    }

    private RoomDTO toDTO(Room room) {
        return new RoomDTO(room.getId(), room.getName());
    }

    private LessonDTO toDTO(Lesson lesson) {
        String timeslotId = lesson.getTimeslot() == null ? null : lesson.getTimeslot().getId();
        String roomId = lesson.getRoom() == null ? null : lesson.getRoom().getId();
        return new LessonDTO(lesson.getId(), lesson.getSubject(), lesson.getTeacher(), lesson.getStudentGroup(),
                timeslotId, roomId);
    }
}
