package org.acme.employeescheduling.service;

import static org.acme.employeescheduling.support.TestHelper.DAY_1;
import static org.acme.employeescheduling.support.TestHelper.aShiftDTO;
import static org.acme.employeescheduling.support.TestHelper.anEmployeeDTO;
import static org.acme.employeescheduling.support.TestHelper.at;
import static org.acme.employeescheduling.support.TestHelper.createProblem;
import static org.acme.employeescheduling.support.TestHelper.input;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.List;

import ai.timefold.solver.service.definition.api.domain.ModelConfig;
import ai.timefold.solver.service.definition.api.validation.Issue;
import ai.timefold.solver.service.definition.api.validation.ValidationBuilder;
import ai.timefold.solver.service.definition.api.validation.dto.ValidationResult;

import org.acme.employeescheduling.demo.DemoDataBuilder;
import org.acme.employeescheduling.demo.DemoDataBuilder.DemoDataSize;
import org.acme.employeescheduling.dto.input.EmployeeInputDTO;
import org.acme.employeescheduling.dto.input.EmployeeScheduleInput;
import org.acme.employeescheduling.dto.input.ShiftInputDTO;
import org.acme.employeescheduling.service.validation.EmployeeScheduleIssue.DuplicateEmployeeNameIssue;
import org.acme.employeescheduling.service.validation.EmployeeScheduleIssue.DuplicateShiftIdIssue;
import org.acme.employeescheduling.service.validation.EmployeeScheduleIssue.InsufficientEmployeesIssue;
import org.acme.employeescheduling.service.validation.EmployeeScheduleIssue.InvalidShiftTimeRangeIssue;
import org.acme.employeescheduling.service.validation.EmployeeScheduleIssue.NoQualifiedEmployeeIssue;
import org.acme.employeescheduling.service.validation.EmployeeScheduleIssue.NonExistingEmployeeReferenceIssue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

// OpenAPI spec compliance is enforced by the Service module at the REST layer, so it's covered by
// org.acme.employeescheduling.rest.EmployeeScheduleOpenApiValidationTest instead. This class only covers the
// domain-specific checks EmployeeScheduleValidator implements itself.
class EmployeeScheduleValidatorTest {

    private static final EmployeeInputDTO EMPLOYEE = anEmployeeDTO("Amy").build();
    private static final List<EmployeeInputDTO> EMPLOYEES = List.of(EMPLOYEE, anEmployeeDTO("Beth").build());
    private static final ShiftInputDTO SHIFT = aShiftDTO("s1").build();

    private final EmployeeScheduleValidator validator = new EmployeeScheduleValidator();

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
    void duplicateEmployeeName() {
        EmployeeScheduleInput input = input(List.of(EMPLOYEE, EMPLOYEE), List.of(SHIFT));
        assertSingleIssue(validate(input), DuplicateEmployeeNameIssue.class);
    }

    @Test
    void duplicateShiftId() {
        EmployeeScheduleInput input = input(EMPLOYEES, List.of(SHIFT, SHIFT));
        assertSingleIssue(validate(input), DuplicateShiftIdIssue.class);
    }

    @Test
    void invalidShiftTimeRange() {
        ShiftInputDTO endsBeforeItStarts = aShiftDTO("s1").start(at(DAY_1, 10)).end(at(DAY_1, 9)).build();
        EmployeeScheduleInput input = input(EMPLOYEES, List.of(endsBeforeItStarts));

        ValidationResult<Issue> result = validate(input);
        assertSingleIssue(result, InvalidShiftTimeRangeIssue.class);
        assertThat(((InvalidShiftTimeRangeIssue) result.issues().iterator().next()).getShiftId()).isEqualTo("s1");
    }

    @Test
    void nonExistingEmployeeReference() {
        ShiftInputDTO shift = aShiftDTO("s1").employeeName("does-not-exist").build();
        EmployeeScheduleInput input = input(EMPLOYEES, List.of(shift));
        assertSingleIssue(validate(input), NonExistingEmployeeReferenceIssue.class);
    }

    @Test
    void noQualifiedEmployee() {
        List<ShiftInputDTO> shifts = List.of(
                aShiftDTO("s1").requiredSkill("Surgeon").build(),
                aShiftDTO("s2").start(at(DAY_1.plusDays(1), 9)).requiredSkill("Surgeon").build());
        EmployeeScheduleInput input = input(EMPLOYEES, shifts);

        ValidationResult<Issue> result = validate(input);
        assertSingleIssue(result, NoQualifiedEmployeeIssue.class);
        NoQualifiedEmployeeIssue issue = (NoQualifiedEmployeeIssue) result.issues().iterator().next();
        assertThat(issue.getRequiredSkill()).isEqualTo("Surgeon");
        assertThat(issue.getShiftCount()).isEqualTo(2);
    }

    @Test
    void insufficientEmployees() {
        List<ShiftInputDTO> shifts = List.of(
                aShiftDTO("s1").start(at(DAY_1, 6)).build(),
                aShiftDTO("s2").start(at(DAY_1, 14)).build(),
                aShiftDTO("s3").start(at(DAY_1, 22)).build());
        EmployeeScheduleInput input = input(EMPLOYEES, shifts);

        ValidationResult<Issue> result = validate(input);
        assertSingleIssue(result, InsufficientEmployeesIssue.class);
        InsufficientEmployeesIssue issue = (InsufficientEmployeesIssue) result.issues().iterator().next();
        assertThat(issue.getDate()).isEqualTo(DAY_1.toString());
        assertThat(issue.getShiftCount()).isEqualTo(3);
        assertThat(issue.getEmployeeCount()).isEqualTo(2);
    }

    @Test
    void mixedDatasetReportsEveryIssue() {
        ShiftInputDTO dangling = aShiftDTO("s2").start(at(DAY_1, 10)).end(at(DAY_1, 9)).requiredSkill("Surgeon")
                .employeeName("does-not-exist").build();
        EmployeeScheduleInput input = input(List.of(EMPLOYEE, EMPLOYEE), List.of(SHIFT, SHIFT, dangling));

        ValidationResult<Issue> result = validate(input);
        assertThat(result.issues()).hasAtLeastOneElementOfType(DuplicateEmployeeNameIssue.class)
                .hasAtLeastOneElementOfType(DuplicateShiftIdIssue.class)
                .hasAtLeastOneElementOfType(InvalidShiftTimeRangeIssue.class)
                .hasAtLeastOneElementOfType(NonExistingEmployeeReferenceIssue.class)
                .hasAtLeastOneElementOfType(NoQualifiedEmployeeIssue.class)
                .hasAtLeastOneElementOfType(InsufficientEmployeesIssue.class);
    }

    private ValidationResult<Issue> validate(EmployeeScheduleInput input) {
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
