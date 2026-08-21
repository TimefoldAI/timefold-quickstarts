package org.acme.bedallocation.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;

import ai.timefold.solver.service.definition.api.domain.ModelConfig;
import ai.timefold.solver.service.definition.api.validation.ModelValidator;
import ai.timefold.solver.service.definition.api.validation.ValidationBuilder;

import org.acme.bedallocation.dto.BedDTO;
import org.acme.bedallocation.dto.BedPlanConfigOverrides;
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

@ApplicationScoped
public class BedPlanValidator implements ModelValidator<BedPlanInput, BedPlanConfigOverrides> {

    @Override
    public void validate(ValidationBuilder validationBuilder, BedPlanInput modelInput,
            ModelConfig<BedPlanConfigOverrides> modelConfig) {
        Set<String> bedIds = validateDepartments(validationBuilder, modelInput.departments());
        validateStays(validationBuilder, modelInput.stays(), bedIds);
    }

    private static Set<String> validateDepartments(ValidationBuilder validationBuilder, List<DepartmentDTO> departments) {
        Set<String> departmentIds = new HashSet<>();
        Set<String> roomIds = new HashSet<>();
        Set<String> bedIds = new HashSet<>();
        for (DepartmentDTO department : departments) {
            if (department.id() == null || department.id().isBlank()) {
                validationBuilder.addIssue(new DepartmentIdMissingIssue());
            } else if (!departmentIds.add(department.id())) {
                validationBuilder.addIssue(new DuplicateDepartmentIdIssue(department.id()));
                // A duplicate department is a repeated entry (e.g. the same department submitted twice), so
                // its rooms/beds were already validated the first time around - re-validating them here would
                // just report the same rooms/beds as duplicates too, drowning out the actual issue.
                continue;
            }
            for (RoomDTO room : department.rooms()) {
                if (room.id() == null || room.id().isBlank()) {
                    validationBuilder.addIssue(new RoomIdMissingIssue());
                } else if (!roomIds.add(room.id())) {
                    validationBuilder.addIssue(new DuplicateRoomIdIssue(room.id()));
                    // Same reasoning as above, one level down: skip this duplicate room's beds.
                    continue;
                }
                for (BedDTO bed : room.beds()) {
                    if (bed.id() == null || bed.id().isBlank()) {
                        validationBuilder.addIssue(new BedIdMissingIssue());
                    } else if (!bedIds.add(bed.id())) {
                        validationBuilder.addIssue(new DuplicateBedIdIssue(bed.id()));
                    }
                }
            }
        }
        return bedIds;
    }

    private static void validateStays(ValidationBuilder validationBuilder, List<StayDTO> stays, Set<String> bedIds) {
        Set<String> stayIds = new HashSet<>();
        for (StayDTO stay : stays) {
            if (stay.id() == null || stay.id().isBlank()) {
                validationBuilder.addIssue(new StayIdMissingIssue());
            } else if (!stayIds.add(stay.id())) {
                validationBuilder.addIssue(new DuplicateStayIdIssue(stay.id()));
            }
            if (stay.bedId() != null && !bedIds.contains(stay.bedId())) {
                validationBuilder.addIssue(new NonExistingBedReferenceIssue(stay.id()));
            }
        }
    }
}
