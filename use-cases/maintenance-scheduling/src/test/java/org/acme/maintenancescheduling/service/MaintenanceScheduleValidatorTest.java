package org.acme.maintenancescheduling.service;

import static org.acme.maintenancescheduling.support.TestHelper.FIRST_MONDAY;
import static org.acme.maintenancescheduling.support.TestHelper.aCrewDTO;
import static org.acme.maintenancescheduling.support.TestHelper.aJobDTO;
import static org.acme.maintenancescheduling.support.TestHelper.aWorkCalendarDTO;
import static org.acme.maintenancescheduling.support.TestHelper.input;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.List;

import ai.timefold.solver.service.definition.api.domain.ModelConfig;
import ai.timefold.solver.service.definition.api.validation.Issue;
import ai.timefold.solver.service.definition.api.validation.ValidationBuilder;
import ai.timefold.solver.service.definition.api.validation.dto.ValidationResult;

import org.acme.maintenancescheduling.demo.DemoDataBuilder;
import org.acme.maintenancescheduling.dto.input.CrewInputDTO;
import org.acme.maintenancescheduling.dto.input.JobInputDTO;
import org.acme.maintenancescheduling.dto.input.MaintenanceScheduleInput;
import org.acme.maintenancescheduling.dto.input.WorkCalendarInputDTO;
import org.acme.maintenancescheduling.service.validation.MaintenanceScheduleIssue.DuplicateCrewIdIssue;
import org.acme.maintenancescheduling.service.validation.MaintenanceScheduleIssue.DuplicateJobIdIssue;
import org.acme.maintenancescheduling.service.validation.MaintenanceScheduleIssue.EmptyWorkCalendarIssue;
import org.acme.maintenancescheduling.service.validation.MaintenanceScheduleIssue.JobWindowTooShortIssue;
import org.acme.maintenancescheduling.service.validation.MaintenanceScheduleIssue.NonExistingCrewReferenceIssue;
import org.acme.maintenancescheduling.service.validation.MaintenanceScheduleIssue.StartDateOutsideWorkCalendarIssue;
import org.junit.jupiter.api.Test;

// OpenAPI spec compliance (Bean Validation) is enforced by the Service module at the REST layer, so it's
// covered by org.acme.maintenancescheduling.rest.MaintenanceScheduleOpenApiValidationTest instead. This
// class only covers the domain-specific checks MaintenanceScheduleValidator implements itself.
class MaintenanceScheduleValidatorTest {

    private static final WorkCalendarInputDTO WORK_CALENDAR = aWorkCalendarDTO("1").build();
    private static final List<CrewInputDTO> CREWS = List.of(aCrewDTO("1").build(), aCrewDTO("2").build());
    private static final List<JobInputDTO> VALID_JOBS = List.of(aJobDTO("1").build());

    private final MaintenanceScheduleValidator validator = new MaintenanceScheduleValidator();

    @Test
    void validInputHasNoIssues() {
        MaintenanceScheduleInput schedule = input(WORK_CALENDAR, CREWS, List.of(
                aJobDTO("1").build(),
                aJobDTO("2").crewId("1").startDate(FIRST_MONDAY).build(),
                aJobDTO("3").build(),
                aJobDTO("4").build()));

        assertThat(validate(schedule).issues()).isEmpty();
    }

    @Test
    void demoDatasetHasNoIssues() {
        // Otherwise the service would ship demo data that its own validator rejects.
        assertThat(validate(DemoDataBuilder.basic()).issues()).isEmpty();
    }

    @Test
    void duplicateCrewId() {
        CrewInputDTO crew = aCrewDTO("1").build();
        MaintenanceScheduleInput schedule = input(WORK_CALENDAR, List.of(crew, crew), VALID_JOBS);
        assertSingleIssue(validate(schedule), DuplicateCrewIdIssue.class);
    }

    @Test
    void duplicateJobId() {
        JobInputDTO job = aJobDTO("1").build();
        MaintenanceScheduleInput schedule = input(WORK_CALENDAR, CREWS, List.of(job, job));
        assertSingleIssue(validate(schedule), DuplicateJobIdIssue.class);
    }

    @Test
    void nonExistingCrewReference() {
        JobInputDTO job = aJobDTO("1").crewId("does-not-exist").build();
        MaintenanceScheduleInput schedule = input(WORK_CALENDAR, CREWS, List.of(job));
        assertSingleIssue(validate(schedule), NonExistingCrewReferenceIssue.class);
    }

    @Test
    void emptyWorkCalendar() {
        // A window of a single Saturday holds no workday at all.
        WorkCalendarInputDTO weekendOnly = aWorkCalendarDTO("1")
                .fromDate(FIRST_MONDAY.plusDays(5))
                .toDate(FIRST_MONDAY.plusDays(6))
                .build();
        MaintenanceScheduleInput schedule = input(weekendOnly, CREWS, VALID_JOBS);
        assertSingleIssue(validate(schedule), EmptyWorkCalendarIssue.class);
    }

    @Test
    void jobWindowTooShort() {
        // Three workdays of work that has to be finished within two.
        JobInputDTO job = aJobDTO("1")
                .durationInDays(3)
                .minStartDate(FIRST_MONDAY)
                .maxEndDate(FIRST_MONDAY.plusDays(2))
                .build();
        MaintenanceScheduleInput schedule = input(WORK_CALENDAR, CREWS, List.of(job));
        assertSingleIssue(validate(schedule), JobWindowTooShortIssue.class);
    }

    @Test
    void startDateOutsideWorkCalendar() {
        JobInputDTO job = aJobDTO("1").crewId("1").startDate(FIRST_MONDAY.minusWeeks(1)).build();
        MaintenanceScheduleInput schedule = input(WORK_CALENDAR, CREWS, List.of(job));
        assertSingleIssue(validate(schedule), StartDateOutsideWorkCalendarIssue.class);
    }

    @Test
    void startDateOnWeekendIsOutsideWorkCalendar() {
        JobInputDTO job = aJobDTO("1").crewId("1").startDate(FIRST_MONDAY.plusDays(5)).build();
        MaintenanceScheduleInput schedule = input(WORK_CALENDAR, CREWS, List.of(job));
        assertSingleIssue(validate(schedule), StartDateOutsideWorkCalendarIssue.class);
    }

    @Test
    void mixedDatasetReportsEveryIssue() {
        CrewInputDTO crew = aCrewDTO("1").build();
        JobInputDTO duplicatedJob = aJobDTO("1").build();
        MaintenanceScheduleInput schedule = input(WORK_CALENDAR, List.of(crew, crew), List.of(
                duplicatedJob,
                duplicatedJob,
                aJobDTO("2").crewId("does-not-exist").build(),
                aJobDTO("3").durationInDays(3).minStartDate(FIRST_MONDAY).maxEndDate(FIRST_MONDAY.plusDays(2)).build(),
                aJobDTO("4").startDate(FIRST_MONDAY.plusYears(1)).build()));

        Collection<Issue> issues = validate(schedule).issues();
        assertThat(issues).hasSize(5);
        assertThat(issues).hasAtLeastOneElementOfType(DuplicateCrewIdIssue.class)
                .hasAtLeastOneElementOfType(DuplicateJobIdIssue.class)
                .hasAtLeastOneElementOfType(NonExistingCrewReferenceIssue.class)
                .hasAtLeastOneElementOfType(JobWindowTooShortIssue.class)
                .hasAtLeastOneElementOfType(StartDateOutsideWorkCalendarIssue.class);
    }

    private ValidationResult<Issue> validate(MaintenanceScheduleInput schedule) {
        ValidationBuilder validationBuilder = new ValidationBuilder();
        validator.validate(validationBuilder, schedule, ModelConfig.empty());
        return validationBuilder.build();
    }

    private static <T extends Issue> void assertSingleIssue(ValidationResult<Issue> result, Class<T> expectedType) {
        Collection<Issue> issues = result.issues();
        assertThat(issues).hasSize(1);
        Issue issue = issues.iterator().next();
        assertThat(issue).isInstanceOf(expectedType);
    }
}
