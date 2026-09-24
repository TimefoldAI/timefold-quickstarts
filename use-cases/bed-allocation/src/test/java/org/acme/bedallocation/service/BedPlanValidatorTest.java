package org.acme.bedallocation.service;

import static org.acme.bedallocation.support.TestHelper.aBedDTO;
import static org.acme.bedallocation.support.TestHelper.aDepartmentDTO;
import static org.acme.bedallocation.support.TestHelper.aRoomDTO;
import static org.acme.bedallocation.support.TestHelper.aStayDTO;
import static org.acme.bedallocation.support.TestHelper.input;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.List;

import ai.timefold.solver.service.definition.api.domain.ModelConfig;
import ai.timefold.solver.service.definition.api.validation.Issue;
import ai.timefold.solver.service.definition.api.validation.ValidationBuilder;
import ai.timefold.solver.service.definition.api.validation.dto.ValidationResult;

import org.acme.bedallocation.dto.input.BedPlanInput;
import org.acme.bedallocation.dto.input.DepartmentInputDTO;
import org.acme.bedallocation.dto.input.StayInputDTO;
import org.acme.bedallocation.service.validation.BedPlanIssue.DuplicateBedIdIssue;
import org.acme.bedallocation.service.validation.BedPlanIssue.DuplicateDepartmentIdIssue;
import org.acme.bedallocation.service.validation.BedPlanIssue.DuplicateRoomIdIssue;
import org.acme.bedallocation.service.validation.BedPlanIssue.DuplicateStayIdIssue;
import org.acme.bedallocation.service.validation.BedPlanIssue.NonExistingBedReferenceIssue;
import org.acme.bedallocation.support.TestHelper.RoomDTOBuilder;
import org.junit.jupiter.api.Test;

// OpenAPI spec compliance (Bean Validation) is enforced by the Service module at the REST layer, so it's
// covered by org.acme.bedallocation.rest.BedPlanOpenApiValidationTest instead. This class only
// covers the domain-specific checks BedPlanValidator implements itself.
class BedPlanValidatorTest {

    private static final List<RoomDTOBuilder> ROOMS = List.of(
            aRoomDTO("r1").beds(List.of(aBedDTO("r1-bed0"), aBedDTO("r1-bed1"))),
            aRoomDTO("r2").beds(List.of(aBedDTO("r2-bed0"), aBedDTO("r2-bed1"))),
            aRoomDTO("r3").beds(List.of(aBedDTO("r3-bed0"), aBedDTO("r3-bed1"))));

    private static final DepartmentInputDTO DEPARTMENT = aDepartmentDTO("d1").rooms(ROOMS).build();

    private static final List<StayInputDTO> VALID_STAYS = List.of(aStayDTO("s1").build());

    private final BedPlanValidator validator = new BedPlanValidator();

    @Test
    void validInputHasNoIssues() {
        List<StayInputDTO> stays = List.of(
                aStayDTO("s1").build(),
                aStayDTO("s2").build(),
                aStayDTO("s3").build(),
                aStayDTO("s4").build());
        BedPlanInput input = input(List.of(DEPARTMENT), stays);

        ValidationResult<Issue> result = validate(input);
        assertThat(result.issues()).isEmpty();
    }

    @Test
    void duplicateDepartmentId() {
        BedPlanInput input = input(List.of(DEPARTMENT, DEPARTMENT), VALID_STAYS);
        assertSingleIssue(validate(input), DuplicateDepartmentIdIssue.class);
    }

    @Test
    void duplicateRoomId() {
        RoomDTOBuilder room = aRoomDTO("r1");
        DepartmentInputDTO department = aDepartmentDTO("d1").rooms(List.of(room, room)).build();
        BedPlanInput input = input(List.of(department), VALID_STAYS);
        assertSingleIssue(validate(input), DuplicateRoomIdIssue.class);
    }

    @Test
    void duplicateBedId() {
        RoomDTOBuilder room = aRoomDTO("r1").beds(List.of(aBedDTO("b1"), aBedDTO("b1")));
        DepartmentInputDTO department = aDepartmentDTO("d1").rooms(List.of(room)).build();
        BedPlanInput input = input(List.of(department), VALID_STAYS);
        assertSingleIssue(validate(input), DuplicateBedIdIssue.class);
    }

    @Test
    void duplicateStayId() {
        StayInputDTO stay = aStayDTO("s1").build();
        BedPlanInput input = input(List.of(DEPARTMENT), List.of(stay, stay));
        assertSingleIssue(validate(input), DuplicateStayIdIssue.class);
    }

    @Test
    void nonExistingBedReference() {
        StayInputDTO stay = aStayDTO("s1").bedId("does-not-exist").build();
        BedPlanInput input = input(List.of(DEPARTMENT), List.of(stay));
        assertSingleIssue(validate(input), NonExistingBedReferenceIssue.class);
    }

    private ValidationResult<Issue> validate(BedPlanInput input) {
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
