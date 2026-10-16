package org.acme.schooltimetabling.service;

import static org.acme.schooltimetabling.support.TestHelper.aLessonDTO;
import static org.acme.schooltimetabling.support.TestHelper.configOverrides;
import static org.acme.schooltimetabling.support.TestHelper.createProblem;
import static org.acme.schooltimetabling.support.TestHelper.lessonOutputDTO;
import static org.acme.schooltimetabling.support.TestHelper.output;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import ai.timefold.solver.core.api.score.HardSoftScore;
import ai.timefold.solver.service.definition.api.domain.ModelConfig;

import org.acme.schooltimetabling.domain.Lesson;
import org.acme.schooltimetabling.domain.Timetable;
import org.acme.schooltimetabling.domain.TimetableConstraintProperties;
import org.acme.schooltimetabling.dto.input.LessonInputDTO;
import org.acme.schooltimetabling.dto.input.TimetableConfigOverrides;
import org.acme.schooltimetabling.dto.input.TimetableInput;
import org.acme.schooltimetabling.dto.output.LessonOutputDTO;
import org.acme.schooltimetabling.dto.output.TimetableOutput;
import org.junit.jupiter.api.Test;

class TimetableModelConvertorTest {

    private static final TimetableInput PROBLEM = createProblem();

    private final TimetableModelConvertor convertor = new TimetableModelConvertor();

    @Test
    void unassignedInputLeavesEveryLessonToTheSolver() {
        Timetable timetable = toSolverModel(PROBLEM, Optional.empty());

        assertThat(timetable.getTimeslots()).hasSize(4);
        assertThat(timetable.getRooms()).hasSize(2);
        assertThat(timetable.getLessons()).hasSize(4).noneMatch(Lesson::isAssigned);
    }

    @Test
    void assignedInputIsKept() {
        TimetableInput input = withFirstLesson(aLessonDTO("l1").timeslotId("t2").roomId("r2").build());

        Lesson lesson = lessonById(toSolverModel(input, Optional.empty()), "l1");
        assertThat(lesson.getTimeslot().id()).isEqualTo("t2");
        assertThat(lesson.getRoom().id()).isEqualTo("r2");
    }

    @Test
    void lastModelOutputOverridesTheInputAssignment() {
        TimetableInput input = withFirstLesson(aLessonDTO("l1").timeslotId("t2").roomId("r2").build());
        TimetableOutput lastOutput = output(List.of(lessonOutputDTO("l1", "t3", "r1")));

        Lesson lesson = lessonById(toSolverModel(input, Optional.of(lastOutput)), "l1");
        assertThat(lesson.getTimeslot().id()).isEqualTo("t3");
        assertThat(lesson.getRoom().id()).isEqualTo("r1");
    }

    @Test
    void unknownReferenceFailsFast() {
        TimetableInput input = withFirstLesson(aLessonDTO("l1").timeslotId("does-not-exist").build());

        assertThatIllegalArgumentException()
                .isThrownBy(() -> toSolverModel(input, Optional.empty()))
                .withMessageContaining("timeslot 'does-not-exist'");
    }

    @Test
    void outputRoundTripsThroughTheInput() {
        Timetable timetable = toSolverModel(PROBLEM, Optional.empty());
        for (Lesson lesson : timetable.getLessons()) {
            lesson.setTimeslot(timetable.getTimeslots().getFirst());
            lesson.setRoom(timetable.getRooms().getLast());
        }

        TimetableOutput output = convertor.toModelOutput(timetable);
        assertThat(output.lessons()).extracting(LessonOutputDTO::timeslotId).containsOnly("t1");
        assertThat(output.lessons()).extracting(LessonOutputDTO::roomId).containsOnly("r2");

        TimetableInput updatedInput = convertor.applyOutputToInput(PROBLEM, output);
        assertThat(updatedInput.lessons()).extracting(LessonInputDTO::timeslotId).containsOnly("t1");
        assertThat(updatedInput.lessons()).extracting(LessonInputDTO::roomId).containsOnly("r2");
        assertThat(updatedInput.lessons()).extracting(LessonInputDTO::subject)
                .containsExactlyElementsOf(PROBLEM.lessons().stream().map(LessonInputDTO::subject).toList());
    }

    @Test
    void onlySetWeightsAreOverridden() {
        ModelConfig<TimetableConfigOverrides> modelConfig = new ModelConfig<>(configOverrides(5L, null, 0L));

        Timetable timetable = convertor.toSolverModel(PROBLEM, modelConfig, Optional.empty());
        var overrides = timetable.getConstraintWeightOverrides();
        assertThat(overrides.getKnownConstraintIds()).containsExactlyInAnyOrder(
                TimetableConstraintProperties.TEACHER_ROOM_STABILITY,
                TimetableConstraintProperties.STUDENT_GROUP_SUBJECT_VARIETY);
        assertThat(overrides.getConstraintWeight(TimetableConstraintProperties.TEACHER_ROOM_STABILITY))
                .isEqualTo(HardSoftScore.ofSoft(5));
        assertThat(overrides.getConstraintWeight(TimetableConstraintProperties.STUDENT_GROUP_SUBJECT_VARIETY))
                .isEqualTo(HardSoftScore.ZERO);
    }

    private Timetable toSolverModel(TimetableInput input, Optional<TimetableOutput> lastOutput) {
        return convertor.toSolverModel(input, ModelConfig.empty(), lastOutput);
    }

    private static TimetableInput withFirstLesson(LessonInputDTO lesson) {
        List<LessonInputDTO> lessons = new ArrayList<>(PROBLEM.lessons());
        lessons.set(0, lesson);
        return PROBLEM.withLessons(lessons);
    }

    private static Lesson lessonById(Timetable timetable, String id) {
        return timetable.getLessons().stream().filter(lesson -> lesson.getId().equals(id)).findFirst().orElseThrow();
    }
}
