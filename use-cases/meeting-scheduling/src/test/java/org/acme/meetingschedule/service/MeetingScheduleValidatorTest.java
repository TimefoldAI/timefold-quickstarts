package org.acme.meetingschedule.service;

import static org.acme.meetingschedule.support.TestHelper.aMeetingDTO;
import static org.acme.meetingschedule.support.TestHelper.aPersonDTO;
import static org.acme.meetingschedule.support.TestHelper.aRoomDTO;
import static org.acme.meetingschedule.support.TestHelper.aTimeConfigurationDTO;
import static org.acme.meetingschedule.support.TestHelper.anOfficeHoursDTO;
import static org.acme.meetingschedule.support.TestHelper.createProblem;
import static org.acme.meetingschedule.support.TestHelper.input;
import static org.acme.meetingschedule.support.TestHelper.officeDay;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.List;

import ai.timefold.solver.service.definition.api.domain.ModelConfig;
import ai.timefold.solver.service.definition.api.validation.Issue;
import ai.timefold.solver.service.definition.api.validation.ValidationBuilder;
import ai.timefold.solver.service.definition.api.validation.dto.ValidationResult;

import org.acme.meetingschedule.demo.DemoDataBuilder;
import org.acme.meetingschedule.dto.input.MeetingInputDTO;
import org.acme.meetingschedule.dto.input.MeetingScheduleInput;
import org.acme.meetingschedule.dto.input.PersonInputDTO;
import org.acme.meetingschedule.dto.input.RoomInputDTO;
import org.acme.meetingschedule.dto.input.TimeConfigurationDTO;
import org.acme.meetingschedule.service.validation.MeetingScheduleIssue.DuplicateMeetingIdIssue;
import org.acme.meetingschedule.service.validation.MeetingScheduleIssue.DuplicatePersonIdIssue;
import org.acme.meetingschedule.service.validation.MeetingScheduleIssue.DuplicateRoomIdIssue;
import org.acme.meetingschedule.service.validation.MeetingScheduleIssue.InvalidOfficeHoursIssue;
import org.acme.meetingschedule.service.validation.MeetingScheduleIssue.MeetingDurationNotAMultipleOfGranularityIssue;
import org.acme.meetingschedule.service.validation.MeetingScheduleIssue.MeetingLongerThanOfficeDayIssue;
import org.acme.meetingschedule.service.validation.MeetingScheduleIssue.MeetingStartOutsideOfficeHoursIssue;
import org.acme.meetingschedule.service.validation.MeetingScheduleIssue.NonExistingPersonReferenceIssue;
import org.acme.meetingschedule.service.validation.MeetingScheduleIssue.NonExistingRoomReferenceIssue;
import org.junit.jupiter.api.Test;

// OpenAPI spec compliance (Bean Validation) is enforced by the Service module at the REST layer, so it's
// covered by org.acme.meetingschedule.rest.MeetingScheduleOpenApiValidationTest instead. This class only
// covers the domain-specific checks MeetingScheduleValidator implements itself.
class MeetingScheduleValidatorTest {

    private static final List<PersonInputDTO> PEOPLE = List.of(aPersonDTO("P1").build(), aPersonDTO("P2").build());
    private static final List<RoomInputDTO> ROOMS = List.of(aRoomDTO("R1").build());
    private static final TimeConfigurationDTO OFFICE_DAY = officeDay();
    private static final List<MeetingInputDTO> VALID_MEETINGS =
            List.of(aMeetingDTO("M1").requiredAttendeeIds(List.of("P1")).build());

    private final MeetingScheduleValidator validator = new MeetingScheduleValidator();

    @Test
    void validInputHasNoIssues() {
        ValidationResult<Issue> result = validate(createProblem());
        assertThat(result.issues()).isEmpty();
    }

    @Test
    void demoDataHasNoIssues() {
        // The service must never ship demo data it would reject itself.
        ValidationResult<Issue> result = validate(DemoDataBuilder.basic());
        assertThat(result.issues()).isEmpty();
    }

    @Test
    void duplicatePersonId() {
        PersonInputDTO person = aPersonDTO("P1").build();
        MeetingScheduleInput problem = input(List.of(person, person), ROOMS, OFFICE_DAY, VALID_MEETINGS);
        assertSingleIssue(validate(problem), DuplicatePersonIdIssue.class);
    }

    @Test
    void duplicateRoomId() {
        RoomInputDTO room = aRoomDTO("R1").build();
        MeetingScheduleInput problem = input(PEOPLE, List.of(room, room), OFFICE_DAY, VALID_MEETINGS);
        assertSingleIssue(validate(problem), DuplicateRoomIdIssue.class);
    }

    @Test
    void duplicateMeetingId() {
        MeetingInputDTO meeting = aMeetingDTO("M1").build();
        MeetingScheduleInput problem = input(PEOPLE, ROOMS, OFFICE_DAY, List.of(meeting, meeting));
        assertSingleIssue(validate(problem), DuplicateMeetingIdIssue.class);
    }

    @Test
    void officeHoursEndingBeforeTheyStart() {
        TimeConfigurationDTO timeConfiguration = aTimeConfigurationDTO()
                .days(List.of(anOfficeHoursDTO().startTime(LocalTime.of(17, 0)).endTime(LocalTime.of(9, 0))))
                .build();
        MeetingScheduleInput problem = input(PEOPLE, ROOMS, timeConfiguration, VALID_MEETINGS);
        // The day holds no slot at all, so the only meeting can no longer fit in a day either.
        ValidationResult<Issue> result = validate(problem);
        assertThat(result.issues()).hasSize(2);
        assertThat(result.issues()).hasAtLeastOneElementOfType(InvalidOfficeHoursIssue.class);
        assertThat(result.issues()).hasAtLeastOneElementOfType(MeetingLongerThanOfficeDayIssue.class);
    }

    @Test
    void officeHoursShorterThanOneSlot() {
        TimeConfigurationDTO timeConfiguration = aTimeConfigurationDTO()
                .granularityInMinutes(60)
                .days(List.of(anOfficeHoursDTO().startTime(LocalTime.of(9, 0)).endTime(LocalTime.of(9, 30))))
                .build();
        MeetingScheduleInput problem = input(PEOPLE, ROOMS, timeConfiguration, VALID_MEETINGS);
        ValidationResult<Issue> result = validate(problem);
        assertThat(result.issues()).hasAtLeastOneElementOfType(InvalidOfficeHoursIssue.class);
    }

    @Test
    void nonExistingPersonReference() {
        MeetingInputDTO meeting = aMeetingDTO("M1").preferredAttendeeIds(List.of("does-not-exist")).build();
        MeetingScheduleInput problem = input(PEOPLE, ROOMS, OFFICE_DAY, List.of(meeting));
        assertSingleIssue(validate(problem), NonExistingPersonReferenceIssue.class);
    }

    @Test
    void nonExistingRoomReference() {
        MeetingInputDTO meeting = aMeetingDTO("M1").roomId("does-not-exist").build();
        MeetingScheduleInput problem = input(PEOPLE, ROOMS, OFFICE_DAY, List.of(meeting));
        assertSingleIssue(validate(problem), NonExistingRoomReferenceIssue.class);
    }

    @Test
    void meetingStartOutsideOfficeHours() {
        // 07:00 is before the office opens, so it is not the start of any slot.
        MeetingInputDTO meeting = aMeetingDTO("M1")
                .startDateTime(OffsetDateTime.of(2024, 1, 1, 7, 0, 0, 0, ZoneOffset.UTC))
                .build();
        MeetingScheduleInput problem = input(PEOPLE, ROOMS, OFFICE_DAY, List.of(meeting));
        assertSingleIssue(validate(problem), MeetingStartOutsideOfficeHoursIssue.class);
    }

    @Test
    void meetingStartBetweenTwoSlots() {
        // 08:07 falls inside office hours, but the slots start on the quarter hour.
        MeetingInputDTO meeting = aMeetingDTO("M1")
                .startDateTime(OffsetDateTime.of(2024, 1, 1, 8, 7, 0, 0, ZoneOffset.UTC))
                .build();
        MeetingScheduleInput problem = input(PEOPLE, ROOMS, OFFICE_DAY, List.of(meeting));
        assertSingleIssue(validate(problem), MeetingStartOutsideOfficeHoursIssue.class);
    }

    @Test
    void meetingStartInAnotherOffsetIsAccepted() {
        // 09:00+01:00 is the same moment as the 08:00Z slot the office day starts with.
        MeetingInputDTO meeting = aMeetingDTO("M1")
                .startDateTime(OffsetDateTime.of(2024, 1, 1, 9, 0, 0, 0, ZoneOffset.ofHours(1)))
                .build();
        MeetingScheduleInput problem = input(PEOPLE, ROOMS, OFFICE_DAY, List.of(meeting));
        assertThat(validate(problem).issues()).isEmpty();
    }

    @Test
    void meetingDurationNotAMultipleOfGranularity() {
        MeetingInputDTO meeting = aMeetingDTO("M1").durationInMinutes(20).build();
        MeetingScheduleInput problem = input(PEOPLE, ROOMS, OFFICE_DAY, List.of(meeting));
        assertSingleIssue(validate(problem), MeetingDurationNotAMultipleOfGranularityIssue.class);
    }

    @Test
    void meetingLongerThanOfficeDay() {
        // The office day is 10 hours long, so an 11 hour meeting can never start and end on the same day.
        MeetingInputDTO meeting = aMeetingDTO("M1").durationInMinutes(11 * 60).build();
        MeetingScheduleInput problem = input(PEOPLE, ROOMS, OFFICE_DAY, List.of(meeting));
        assertSingleIssue(validate(problem), MeetingLongerThanOfficeDayIssue.class);
    }

    @Test
    void mixedDatasetReportsEveryIssue() {
        PersonInputDTO person = aPersonDTO("P1").build();
        RoomInputDTO room = aRoomDTO("R1").build();
        MeetingScheduleInput problem = input(List.of(person, person), List.of(room, room), OFFICE_DAY,
                List.of(aMeetingDTO("M1").requiredAttendeeIds(List.of("nobody")).build(),
                        aMeetingDTO("M2").roomId("no-room").durationInMinutes(20).build()));

        ValidationResult<Issue> result = validate(problem);
        assertThat(result.issues()).hasSize(5);
        assertThat(result.issues()).hasAtLeastOneElementOfType(DuplicatePersonIdIssue.class);
        assertThat(result.issues()).hasAtLeastOneElementOfType(DuplicateRoomIdIssue.class);
        assertThat(result.issues()).hasAtLeastOneElementOfType(NonExistingPersonReferenceIssue.class);
        assertThat(result.issues()).hasAtLeastOneElementOfType(NonExistingRoomReferenceIssue.class);
        assertThat(result.issues()).hasAtLeastOneElementOfType(MeetingDurationNotAMultipleOfGranularityIssue.class);
    }

    private ValidationResult<Issue> validate(MeetingScheduleInput input) {
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
