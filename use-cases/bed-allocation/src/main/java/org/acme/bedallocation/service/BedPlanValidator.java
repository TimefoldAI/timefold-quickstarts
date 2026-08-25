package org.acme.bedallocation.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import ai.timefold.solver.service.definition.api.domain.ModelConfig;
import ai.timefold.solver.service.definition.api.validation.ModelValidator;
import ai.timefold.solver.service.definition.api.validation.ValidationBuilder;

import org.acme.bedallocation.dto.BedDTO;
import org.acme.bedallocation.dto.BedPlanConfigOverrides;
import org.acme.bedallocation.dto.BedPlanInput;
import org.acme.bedallocation.dto.DepartmentDTO;
import org.acme.bedallocation.dto.RoomDTO;
import org.acme.bedallocation.dto.StayDTO;
import org.acme.bedallocation.service.validation.BedPlanIssue.DuplicateBedIdIssue;
import org.acme.bedallocation.service.validation.BedPlanIssue.DuplicateDepartmentIdIssue;
import org.acme.bedallocation.service.validation.BedPlanIssue.DuplicateRoomIdIssue;
import org.acme.bedallocation.service.validation.BedPlanIssue.DuplicateStayIdIssue;
import org.acme.bedallocation.service.validation.BedPlanIssue.NonExistingBedReferenceIssue;
import org.acme.bedallocation.service.validation.OpenApiSpecIssue;

@ApplicationScoped
public class BedPlanValidator implements ModelValidator<BedPlanInput, BedPlanConfigOverrides> {

    @Inject
    Validator validator;

    @Override
    public void validate(ValidationBuilder validationBuilder, BedPlanInput modelInput,
            ModelConfig<BedPlanConfigOverrides> modelConfig) {
        // This Bean Validation API call will be moved to the Service module.
        for (ConstraintViolation<BedPlanInput> violation : validator.validate(modelInput)) {
            validationBuilder.addIssue(new OpenApiSpecIssue(violation.getPropertyPath() + ": " + violation.getMessage()));
        }
        Set<String> bedIds = validateDepartments(validationBuilder, orEmpty(modelInput.departments()));
        validateStays(validationBuilder, orEmpty(modelInput.stays()), bedIds);
    }

    private Set<String> validateDepartments(ValidationBuilder validationBuilder, List<DepartmentDTO> departments) {
        Set<String> departmentIds = new HashSet<>();
        Set<String> roomIds = new HashSet<>();
        Set<String> bedIds = new HashSet<>();
        for (DepartmentDTO department : departments) {
            if (hasId(department.id()) && !departmentIds.add(department.id())) {
                validationBuilder.addIssue(new DuplicateDepartmentIdIssue(department.id()));
                continue;
            }
            for (RoomDTO room : orEmpty(department.rooms())) {
                if (hasId(room.id()) && !roomIds.add(room.id())) {
                    validationBuilder.addIssue(new DuplicateRoomIdIssue(room.id()));
                    continue;
                }
                for (BedDTO bed : orEmpty(room.beds())) {
                    if (hasId(bed.id()) && !bedIds.add(bed.id())) {
                        validationBuilder.addIssue(new DuplicateBedIdIssue(bed.id()));
                    }
                }
            }
        }
        return bedIds;
    }

    private static <T> List<T> orEmpty(List<T> list) {
        return list == null ? List.of() : list;
    }

    private void validateStays(ValidationBuilder validationBuilder, List<StayDTO> stays, Set<String> bedIds) {
        Set<String> stayIds = new HashSet<>();
        for (StayDTO stay : stays) {
            if (hasId(stay.id()) && !stayIds.add(stay.id())) {
                validationBuilder.addIssue(new DuplicateStayIdIssue(stay.id()));
            }
            if (stay.bedId() != null && !bedIds.contains(stay.bedId())) {
                validationBuilder.addIssue(new NonExistingBedReferenceIssue(stay.id()));
            }
        }
    }

    private static boolean hasId(String id) {
        return id != null && !id.isBlank();
    }
}
