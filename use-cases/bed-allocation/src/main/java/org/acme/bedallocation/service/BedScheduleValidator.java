package org.acme.bedallocation.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;

import ai.timefold.solver.service.definition.api.domain.ModelConfig;
import ai.timefold.solver.service.definition.api.validation.ModelValidator;
import ai.timefold.solver.service.definition.api.validation.ValidationBuilder;

import org.acme.bedallocation.dto.BedDTO;
import org.acme.bedallocation.dto.BedIdDetail;
import org.acme.bedallocation.dto.BedScheduleConfigOverrides;
import org.acme.bedallocation.dto.BedScheduleInput;
import org.acme.bedallocation.dto.StayDTO;
import org.acme.bedallocation.dto.StayIdDetail;
import org.acme.bedallocation.service.BedScheduleIssues.BedIdMissingIssue;
import org.acme.bedallocation.service.BedScheduleIssues.DuplicateBedIdIssue;
import org.acme.bedallocation.service.BedScheduleIssues.DuplicateStayIdIssue;
import org.acme.bedallocation.service.BedScheduleIssues.NonExistingBedReferenceIssue;
import org.acme.bedallocation.service.BedScheduleIssues.StayIdMissingIssue;

@ApplicationScoped
public class BedScheduleValidator
        implements ModelValidator<BedScheduleInput, BedScheduleConfigOverrides> {

    @Override
    public void validate(ValidationBuilder validationBuilder, BedScheduleInput modelInput,
            ModelConfig<BedScheduleConfigOverrides> modelConfig) {
        Set<String> bedIds = validateBeds(validationBuilder, modelInput.beds());
        validateStays(validationBuilder, modelInput.stays(), bedIds);
    }

    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private Set<String> validateBeds(ValidationBuilder validationBuilder, List<BedDTO> beds) {
        Set<String> bedIds = new HashSet<>();
        for (BedDTO bed : beds) {
            if (bed.id() == null || bed.id().isBlank()) {
                validationBuilder.addIssue(new BedIdMissingIssue());
            } else if (!bedIds.add(bed.id())) {
                validationBuilder.addIssue(new DuplicateBedIdIssue(new BedIdDetail(bed.id())));
            }
        }
        return bedIds;
    }

    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private void validateStays(ValidationBuilder validationBuilder, List<StayDTO> stays, Set<String> bedIds) {
        Set<String> stayIds = new HashSet<>();
        for (StayDTO stay : stays) {
            if (stay.id() == null || stay.id().isBlank()) {
                validationBuilder.addIssue(new StayIdMissingIssue());
            } else if (!stayIds.add(stay.id())) {
                validationBuilder.addIssue(new DuplicateStayIdIssue(new StayIdDetail(stay.id())));
            }
            if (stay.bedId() != null && !bedIds.contains(stay.bedId())) {
                validationBuilder.addIssue(new NonExistingBedReferenceIssue(new StayIdDetail(stay.id())));
            }
        }
    }
}
