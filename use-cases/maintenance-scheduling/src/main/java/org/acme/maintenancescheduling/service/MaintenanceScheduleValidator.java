package org.acme.maintenancescheduling.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;

import ai.timefold.solver.service.definition.api.domain.ModelConfig;
import ai.timefold.solver.service.definition.api.validation.ModelValidator;
import ai.timefold.solver.service.definition.api.validation.ValidationBuilder;

import org.acme.maintenancescheduling.dto.CrewDTO;
import org.acme.maintenancescheduling.dto.CrewIdDetail;
import org.acme.maintenancescheduling.dto.JobDTO;
import org.acme.maintenancescheduling.dto.JobIdDetail;
import org.acme.maintenancescheduling.dto.MaintenanceScheduleConfigOverrides;
import org.acme.maintenancescheduling.dto.MaintenanceScheduleInput;
import org.acme.maintenancescheduling.service.MaintenanceScheduleIssues.CrewIdMissingIssue;
import org.acme.maintenancescheduling.service.MaintenanceScheduleIssues.DuplicateCrewIdIssue;
import org.acme.maintenancescheduling.service.MaintenanceScheduleIssues.DuplicateJobIdIssue;
import org.acme.maintenancescheduling.service.MaintenanceScheduleIssues.JobIdMissingIssue;
import org.acme.maintenancescheduling.service.MaintenanceScheduleIssues.NonExistingCrewReferenceIssue;
import org.acme.maintenancescheduling.service.MaintenanceScheduleIssues.WorkCalendarMissingIssue;

@ApplicationScoped
public class MaintenanceScheduleValidator
        implements ModelValidator<MaintenanceScheduleInput, MaintenanceScheduleConfigOverrides> {

    @Override
    public void validate(ValidationBuilder validationBuilder, MaintenanceScheduleInput modelInput,
            ModelConfig<MaintenanceScheduleConfigOverrides> modelConfig) {
        if (modelInput.workCalendar() == null) {
            validationBuilder.addIssue(new WorkCalendarMissingIssue());
        }
        Set<String> crewIds = validateCrews(validationBuilder, modelInput.crews());
        validateJobs(validationBuilder, modelInput.jobs(), crewIds);
    }

    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private Set<String> validateCrews(ValidationBuilder validationBuilder, List<CrewDTO> crews) {
        Set<String> crewIds = new HashSet<>();
        for (CrewDTO crew : crews) {
            if (crew.id() == null || crew.id().isBlank()) {
                validationBuilder.addIssue(new CrewIdMissingIssue());
            } else if (!crewIds.add(crew.id())) {
                validationBuilder.addIssue(new DuplicateCrewIdIssue(new CrewIdDetail(crew.id())));
            }
        }
        return crewIds;
    }

    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private void validateJobs(ValidationBuilder validationBuilder, List<JobDTO> jobs, Set<String> crewIds) {
        Set<String> jobIds = new HashSet<>();
        for (JobDTO job : jobs) {
            if (job.id() == null || job.id().isBlank()) {
                validationBuilder.addIssue(new JobIdMissingIssue());
            } else if (!jobIds.add(job.id())) {
                validationBuilder.addIssue(new DuplicateJobIdIssue(new JobIdDetail(job.id())));
            }
            if (job.crewId() != null && !crewIds.contains(job.crewId())) {
                validationBuilder.addIssue(new NonExistingCrewReferenceIssue(new JobIdDetail(job.id())));
            }
        }
    }
}
