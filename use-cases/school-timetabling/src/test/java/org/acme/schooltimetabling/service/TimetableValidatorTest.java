package org.acme.schooltimetabling.service;

import static org.acme.schooltimetabling.support.TestHelper.aLessonDTO;
import static org.acme.schooltimetabling.support.TestHelper.createProblem;
import static org.acme.schooltimetabling.support.TestHelper.input;
import static org.acme.schooltimetabling.support.TestHelper.roomDTO;
import static org.acme.schooltimetabling.support.TestHelper.timeslotDTO;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.stream.IntStream;

import ai.timefold.solver.service.definition.api.domain.ModelConfig;
import ai.timefold.solver.service.definition.api.validation.Issue;
import ai.timefold.solver.service.definition.api.validation.ValidationBuilder;
import ai.timefold.solver.service.definition.api.validation.dto.ValidationResult;

import org.acme.schooltimetabling.demo.DemoDataBuilder;
import org.acme.schooltimetabling.demo.DemoDataBuilder.DemoDataSize;
import org.acme.schooltimetabling.dto.input.LessonInputDTO;
import org.acme.schooltimetabling.dto.input.RoomInputDTO;
import org.acme.schooltimetabling.dto.input.TimeslotInputDTO;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

// OpenAPI spec compliance is enforced by the Service module at the REST layer, so it's covered by
// org.acme.schooltimetabling.rest.TimetableOpenApiValidationTest instead. This class only covers the
// domain-specific checks TimetableValidator implements itself.
class TimetableValidatorTest {

    private static final TimeslotInputDTO TIMESLOT1 = timeslotDTO("t1", DayOfWeek.MONDAY, LocalTime.of(8, 30));
    private static final TimeslotInputDTO TIMESLOT2 = timeslotDTO("t2", DayOfWeek.MONDAY, LocalTime.of(9, 30));
    private static final List<TimeslotInputDTO> TIMESLOTS = List.of(TIMESLOT1, TIMESLOT2);
    private static final RoomInputDTO ROOM = roomDTO("r1");
    private static final List<RoomInputDTO> ROOMS = List.of(ROOM, roomDTO("r2"));

    private final TimetableValidator validator = new TimetableValidator();

    @Test
    void validInputHasNoIssues() {
        ValidationResult<Issue> result = validate(createProblem());
        assertThat(result.issues()).isEmpty();
    }

    // A service that ships demo data its own validator rejects is broken out of the box.
    @ParameterizedTest
    @EnumSource(DemoDataSize.class)
    void demoDataHasNoIssues(DemoDataSize size) {
        ValidationResult<Issue> result = validate(DemoDataBuilder.builder().withSize(size).build());
        assertThat(result.issues()).isEmpty();
    }

    @Test
    void duplicateTimeslotId() {
        TimetableInput input = input(List.of(TIMESLOT1, TIMESLOT1), ROOMS, lessons(1));
        assertSingleIssue(validate(input), DuplicateTimeslotIdIssue.class);
    }

    @Test
    void duplicateRoomId() {
        TimetableInput input = input(TIMESLOTS, List.of(ROOM, ROOM), lessons(1));
        assertSingleIssue(validate(input), DuplicateRoomIdIssue.class);
    }

    @Test
    void duplicateLessonId() {
        LessonInputDTO lesson = aLessonDTO("l1").build();
        TimetableInput input = input(TIMESLOTS, ROOMS, List.of(lesson, lesson));
        assertSingleIssue(validate(input), DuplicateLessonIdIssue.class);
    }

    @Test
    void invalidTimeslotTimeRange() {
        TimeslotInputDTO endsBeforeItStarts =
                timeslotDTO("t3", DayOfWeek.MONDAY, LocalTime.of(11, 0), LocalTime.of(10, 0));
        TimetableInput input = input(List.of(TIMESLOT1, endsBeforeItStarts), ROOMS, lessons(1));

        ValidationResult<Issue> result = validate(input);
        assertSingleIssue(result, InvalidTimeslotTimeRangeIssue.class);
        assertThat(((InvalidTimeslotTimeRangeIssue) result.issues().iterator().next()).getTimeslotId()).isEqualTo("t3");
    }

    @Test
    void nonExistingTimeslotReference() {
        LessonInputDTO lesson = aLessonDTO("l1").timeslotId("does-not-exist").roomId("r1").build();
        TimetableInput input = input(TIMESLOTS, ROOMS, List.of(lesson));
        assertSingleIssue(validate(input), NonExistingTimeslotReferenceIssue.class);
    }

    @Test
    void nonExistingRoomReference() {
        LessonInputDTO lesson = aLessonDTO("l1").timeslotId("t1").roomId("does-not-exist").build();
        TimetableInput input = input(TIMESLOTS, ROOMS, List.of(lesson));
        assertSingleIssue(validate(input), NonExistingRoomReferenceIssue.class);
    }

    @Test
    void insufficientRoomCapacity() {
        // Two timeslots and one room hold two lessons; every lesson has its own teacher and student group.
        List<LessonInputDTO> lessons = List.of(
                aLessonDTO("l1").teacher("T1").studentGroup("G1").build(),
                aLessonDTO("l2").teacher("T2").studentGroup("G2").build(),
                aLessonDTO("l3").teacher("T3").studentGroup("G3").build());
        TimetableInput input = input(TIMESLOTS, List.of(ROOM), lessons);

        ValidationResult<Issue> result = validate(input);
        assertSingleIssue(result, InsufficientRoomCapacityIssue.class);
        InsufficientRoomCapacityIssue issue = (InsufficientRoomCapacityIssue) result.issues().iterator().next();
        assertThat(issue.getLessonCount()).isEqualTo(3);
        assertThat(issue.getCapacity()).isEqualTo(2);
    }

    @Test
    void overloadedTeacher() {
        List<LessonInputDTO> lessons = List.of(
                aLessonDTO("l1").teacher("T1").studentGroup("G1").build(),
                aLessonDTO("l2").teacher("T1").studentGroup("G2").build(),
                aLessonDTO("l3").teacher("T1").studentGroup("G3").build());
        TimetableInput input = input(TIMESLOTS, ROOMS, lessons);

        ValidationResult<Issue> result = validate(input);
        assertSingleIssue(result, OverloadedTeacherIssue.class);
        OverloadedTeacherIssue issue = (OverloadedTeacherIssue) result.issues().iterator().next();
        assertThat(issue.getTeacher()).isEqualTo("T1");
        assertThat(issue.getLessonCount()).isEqualTo(3);
    }

    @Test
    void overloadedStudentGroup() {
        List<LessonInputDTO> lessons = List.of(
                aLessonDTO("l1").teacher("T1").studentGroup("G1").build(),
                aLessonDTO("l2").teacher("T2").studentGroup("G1").build(),
                aLessonDTO("l3").teacher("T3").studentGroup("G1").build());
        TimetableInput input = input(TIMESLOTS, ROOMS, lessons);

        ValidationResult<Issue> result = validate(input);
        assertSingleIssue(result, OverloadedStudentGroupIssue.class);
        OverloadedStudentGroupIssue issue = (OverloadedStudentGroupIssue) result.issues().iterator().next();
        assertThat(issue.getStudentGroup()).isEqualTo("G1");
        assertThat(issue.getLessonCount()).isEqualTo(3);
    }

    @Test
    void mixedDatasetReportsEveryIssue() {
        LessonInputDTO duplicated = aLessonDTO("l1").teacher("T1").studentGroup("G1").build();
        LessonInputDTO dangling = aLessonDTO("l2").teacher("T1").studentGroup("G1").timeslotId("does-not-exist")
                .roomId("does-not-exist").build();
        TimetableInput input = input(List.of(TIMESLOT1, TIMESLOT1), List.of(ROOM, ROOM),
                List.of(duplicated, duplicated, dangling));

        ValidationResult<Issue> result = validate(input);
        assertThat(result.issues()).hasAtLeastOneElementOfType(DuplicateTimeslotIdIssue.class)
                .hasAtLeastOneElementOfType(DuplicateRoomIdIssue.class)
                .hasAtLeastOneElementOfType(DuplicateLessonIdIssue.class)
                .hasAtLeastOneElementOfType(NonExistingTimeslotReferenceIssue.class)
                .hasAtLeastOneElementOfType(NonExistingRoomReferenceIssue.class)
                .hasAtLeastOneElementOfType(InsufficientRoomCapacityIssue.class)
                .hasAtLeastOneElementOfType(OverloadedTeacherIssue.class)
                .hasAtLeastOneElementOfType(OverloadedStudentGroupIssue.class);
    }

    /** Lessons that each have their own teacher and student group, so none of them is overloaded. */
    private static List<LessonInputDTO> lessons(int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> aLessonDTO("l" + i).teacher("T" + i).studentGroup("G" + i).build())
                .toList();
    }

    private ValidationResult<Issue> validate(TimetableInput input) {
        ValidationBuilder validationBuilder = new ValidationBuilder();
        validator.validate(validationBuilder, input, ModelConfig.empty());
        return validationBuilder.build();
    }

    private static <T extends Issue> void assertSingleIssue(ValidationResult<Issue> result, Class<T> expectedType) {
        Collection<Issue> issues = result.issues();
        assertThat(issues).hasSize(1);
        Issue issue = issues.iterator().next();
        assertThat(issue).isInstanceOf(expectedType);
    }
}
