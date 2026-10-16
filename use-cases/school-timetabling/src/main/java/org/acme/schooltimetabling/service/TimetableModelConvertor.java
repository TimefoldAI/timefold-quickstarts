package org.acme.schooltimetabling.service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;

import ai.timefold.solver.core.api.domain.solution.ConstraintWeightOverrides;
import ai.timefold.solver.core.api.score.HardSoftScore;
import ai.timefold.solver.service.definition.api.ModelConvertor;
import ai.timefold.solver.service.definition.api.domain.ModelConfig;

import org.acme.schooltimetabling.domain.Lesson;
import org.acme.schooltimetabling.domain.Room;
import org.acme.schooltimetabling.domain.Timeslot;
import org.acme.schooltimetabling.domain.Timetable;
import org.acme.schooltimetabling.domain.TimetableConstraintProperties;
import org.acme.schooltimetabling.dto.input.LessonInputDTO;
import org.acme.schooltimetabling.dto.input.RoomInputDTO;
import org.acme.schooltimetabling.dto.input.TimeslotInputDTO;
import org.acme.schooltimetabling.dto.input.TimetableConfigOverrides;
import org.acme.schooltimetabling.dto.input.TimetableInput;
import org.acme.schooltimetabling.dto.output.LessonOutputDTO;
import org.acme.schooltimetabling.dto.output.TimetableOutput;

@ApplicationScoped
public class TimetableModelConvertor
        implements ModelConvertor<HardSoftScore, TimetableInput, TimetableConfigOverrides, Timetable, TimetableOutput> {

    @Override
    public TimetableInput applyOutputToInput(TimetableInput modelInput, TimetableOutput modelOutput) {
        Map<String, LessonOutputDTO> outputLessons = new LinkedHashMap<>();
        for (LessonOutputDTO lesson : modelOutput.lessons()) {
            outputLessons.put(lesson.id(), lesson);
        }
        List<LessonInputDTO> updatedLessons = modelInput.lessons().stream()
                .map(lesson -> {
                    LessonOutputDTO solved = outputLessons.get(lesson.id());
                    return solved == null ? lesson : lesson.withAssignment(solved.timeslotId(), solved.roomId());
                })
                .toList();
        return modelInput.withLessons(updatedLessons);
    }

    @Override
    public Timetable toSolverModel(TimetableInput modelInput, ModelConfig<TimetableConfigOverrides> modelConfig,
            Optional<TimetableOutput> lastModelOutput) {
        Map<String, Timeslot> timeslotMap = new LinkedHashMap<>();
        for (TimeslotInputDTO dto : modelInput.timeslots()) {
            timeslotMap.put(dto.id(), new Timeslot(dto.id(), dto.dayOfWeek(), dto.startTime(), dto.endTime()));
        }
        Map<String, Room> roomMap = new LinkedHashMap<>();
        for (RoomInputDTO dto : modelInput.rooms()) {
            roomMap.put(dto.id(), new Room(dto.id(), dto.name()));
        }
        List<Lesson> lessons = modelInput.lessons().stream()
                .map(dto -> new Lesson(dto.id(), dto.subject(), dto.teacher(), dto.studentGroup(),
                        dto.timeslotId() == null ? null : require(timeslotMap, dto.timeslotId(), "timeslot"),
                        dto.roomId() == null ? null : require(roomMap, dto.roomId(), "room")))
                .toList();

        Timetable timetable = new Timetable(List.copyOf(timeslotMap.values()), List.copyOf(roomMap.values()), lessons);
        applyLastOutput(lessons, timeslotMap, roomMap, lastModelOutput);
        applyConstraintWeightOverrides(timetable, modelConfig);
        return timetable;
    }

    @Override
    public TimetableOutput toModelOutput(Timetable solverModel) {
        List<LessonOutputDTO> lessons = solverModel.getLessons().stream()
                .map(lesson -> new LessonOutputDTO(lesson.getId(),
                        lesson.getTimeslot() == null ? null : lesson.getTimeslot().id(),
                        lesson.getRoom() == null ? null : lesson.getRoom().id()))
                .toList();
        return new TimetableOutput(lessons);
    }

    /**
     * Fails fast with an actionable message instead of letting an unknown reference
     * turn into a null in the solver model and a delayed NullPointerException.
     */
    private static <T> T require(Map<String, T> map, String key, String kind) {
        T value = map.get(key);
        if (value == null) {
            throw new IllegalArgumentException("Unknown %s '%s'.".formatted(kind, key));
        }
        return value;
    }

    private static void applyConstraintWeightOverrides(Timetable timetable,
            ModelConfig<TimetableConfigOverrides> modelConfig) {
        if (modelConfig == null || modelConfig.overrides() == null) {
            return;
        }
        TimetableConfigOverrides overrides = modelConfig.overrides();
        // Only apply weights that are actually set (non-null) in the merged overrides. A null weight means the
        // input did not override it, so the configuration profile value (or the constraint's default) is kept.
        Map<String, HardSoftScore> weights = new HashMap<>();
        if (overrides.teacherRoomStabilityWeight() != null) {
            weights.put(TimetableConstraintProperties.TEACHER_ROOM_STABILITY,
                    HardSoftScore.ofSoft(overrides.teacherRoomStabilityWeight()));
        }
        if (overrides.teacherTimeEfficiencyWeight() != null) {
            weights.put(TimetableConstraintProperties.TEACHER_TIME_EFFICIENCY,
                    HardSoftScore.ofSoft(overrides.teacherTimeEfficiencyWeight()));
        }
        if (overrides.studentGroupSubjectVarietyWeight() != null) {
            weights.put(TimetableConstraintProperties.STUDENT_GROUP_SUBJECT_VARIETY,
                    HardSoftScore.ofSoft(overrides.studentGroupSubjectVarietyWeight()));
        }
        if (!weights.isEmpty()) {
            timetable.setConstraintWeightOverrides(ConstraintWeightOverrides.of(weights));
        }
    }

    // lastModelOutput is used to recover a run that stopped halfway; it overrides the input assignment.
    private static void applyLastOutput(List<Lesson> lessons, Map<String, Timeslot> timeslotMap,
            Map<String, Room> roomMap, Optional<TimetableOutput> lastModelOutput) {
        if (lastModelOutput.isEmpty()) {
            return;
        }
        Map<String, Lesson> lessonMap = new LinkedHashMap<>();
        for (Lesson lesson : lessons) {
            lessonMap.put(lesson.getId(), lesson);
        }
        for (LessonOutputDTO solved : lastModelOutput.get().lessons()) {
            Lesson lesson = lessonMap.get(solved.id());
            if (lesson == null || solved.timeslotId() == null || solved.roomId() == null) {
                continue;
            }
            Timeslot timeslot = timeslotMap.get(solved.timeslotId());
            Room room = roomMap.get(solved.roomId());
            if (timeslot != null && room != null) {
                lesson.setTimeslot(timeslot);
                lesson.setRoom(room);
            }
        }
    }
}
