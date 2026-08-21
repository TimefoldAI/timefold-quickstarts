package org.acme.bedallocation.service;

import static org.acme.bedallocation.support.BedPlanTestDataFactory.ROOMS;
import static org.acme.bedallocation.support.BedPlanTestDataFactory.bed;
import static org.acme.bedallocation.support.BedPlanTestDataFactory.createProblem;
import static org.acme.bedallocation.support.BedPlanTestDataFactory.department;
import static org.acme.bedallocation.support.BedPlanTestDataFactory.inputWithDepartments;
import static org.acme.bedallocation.support.BedPlanTestDataFactory.inputWithStays;
import static org.acme.bedallocation.support.BedPlanTestDataFactory.room;
import static org.acme.bedallocation.support.BedPlanTestDataFactory.roomWithBeds;
import static org.acme.bedallocation.support.BedPlanTestDataFactory.stay;
import static org.acme.bedallocation.support.BedPlanTestDataFactory.stayWithBedId;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.List;

import ai.timefold.solver.service.definition.api.domain.ModelConfig;
import ai.timefold.solver.service.definition.api.validation.Issue;
import ai.timefold.solver.service.definition.api.validation.ValidationBuilder;
import ai.timefold.solver.service.definition.api.validation.dto.ValidationResult;

import org.acme.bedallocation.dto.BedPlanInput;
import org.acme.bedallocation.dto.DepartmentDTO;
import org.acme.bedallocation.dto.RoomDTO;
import org.acme.bedallocation.dto.StayDTO;
import org.acme.bedallocation.service.validation.BedIdMissingIssue;
import org.acme.bedallocation.service.validation.DepartmentIdMissingIssue;
import org.acme.bedallocation.service.validation.DuplicateBedIdIssue;
import org.acme.bedallocation.service.validation.DuplicateDepartmentIdIssue;
import org.acme.bedallocation.service.validation.DuplicateRoomIdIssue;
import org.acme.bedallocation.service.validation.DuplicateStayIdIssue;
import org.acme.bedallocation.service.validation.NonExistingBedReferenceIssue;
import org.acme.bedallocation.service.validation.RoomIdMissingIssue;
import org.acme.bedallocation.service.validation.StayIdMissingIssue;
import org.junit.jupiter.api.Test;

class BedPlanValidatorTest {

    private final BedPlanValidator validator = new BedPlanValidator();

    @Test
    void validInputHasNoIssues() {
        ValidationResult<Issue> result = validate(createProblem());

        assertThat(result.issues()).isEmpty();
    }

    @Test
    void missingDepartmentId() {
        DepartmentDTO department = department(null, ROOMS);
        singleIssue(validate(inputWithDepartments(department)), DepartmentIdMissingIssue.class);
    }

    @Test
    void duplicateDepartmentId() {
        DepartmentDTO department = department(ROOMS);
        singleIssue(validate(inputWithDepartments(department, department)), DuplicateDepartmentIdIssue.class);
    }

    @Test
    void missingRoomId() {
        RoomDTO room = room(null);
        singleIssue(validate(inputWithDepartments(department(List.of(room)))), RoomIdMissingIssue.class);
    }

    @Test
    void duplicateRoomId() {
        RoomDTO room = room("r1");
        singleIssue(validate(inputWithDepartments(department(List.of(room, room)))), DuplicateRoomIdIssue.class);
    }

    @Test
    void missingBedId() {
        RoomDTO room = roomWithBeds("r1", List.of(bed(null, 0)));
        singleIssue(validate(inputWithDepartments(department(List.of(room)))), BedIdMissingIssue.class);
    }

    @Test
    void duplicateBedId() {
        RoomDTO room = roomWithBeds("r1", List.of(bed("b1", 0), bed("b1", 1)));
        singleIssue(validate(inputWithDepartments(department(List.of(room)))), DuplicateBedIdIssue.class);
    }

    @Test
    void missingStayId() {
        StayDTO stayWithoutId = stay(null);
        singleIssue(validate(inputWithStays(stayWithoutId)), StayIdMissingIssue.class);
    }

    @Test
    void duplicateStayId() {
        StayDTO stay = stay("s1");
        singleIssue(validate(inputWithStays(stay, stay)), DuplicateStayIdIssue.class);
    }

    @Test
    void nonExistingBedReference() {
        singleIssue(validate(inputWithStays(stayWithBedId("s1", "does-not-exist"))), NonExistingBedReferenceIssue.class);
    }

    // ------------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------------

    private ValidationResult<Issue> validate(BedPlanInput input) {
        ValidationBuilder validationBuilder = new ValidationBuilder();
        validator.validate(validationBuilder, input, ModelConfig.empty());
        return validationBuilder.build();
    }

    private static <T extends Issue> T singleIssue(ValidationResult<Issue> result, Class<T> expectedType) {
        Collection<Issue> issues = result.issues();
        assertThat(issues).hasSize(1);
        Issue issue = issues.iterator().next();
        assertThat(issue).isInstanceOf(expectedType);
        return expectedType.cast(issue);
    }
}
