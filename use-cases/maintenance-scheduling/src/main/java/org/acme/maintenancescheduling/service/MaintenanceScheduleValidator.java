package org.acme.maintenancescheduling.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;

import ai.timefold.solver.service.definition.api.domain.ModelConfig;
import ai.timefold.solver.service.definition.api.validation.ModelValidator;
import ai.timefold.solver.service.definition.api.validation.ValidationBuilder;

import org.acme.maintenancescheduling.domain.Job;
import org.acme.maintenancescheduling.dto.input.CrewInputDTO;
import org.acme.maintenancescheduling.dto.input.JobInputDTO;
import org.acme.maintenancescheduling.dto.input.MaintenanceScheduleConfigOverrides;
import org.acme.maintenancescheduling.dto.input.MaintenanceScheduleInput;
import org.acme.maintenancescheduling.dto.input.WorkCalendarInputDTO;
import org.acme.maintenancescheduling.service.validation.MaintenanceScheduleIssue.DuplicateCrewIdIssue;
import org.acme.maintenancescheduling.service.validation.MaintenanceScheduleIssue.DuplicateJobIdIssue;
import org.acme.maintenancescheduling.service.validation.MaintenanceScheduleIssue.EmptyWorkCalendarIssue;
import org.acme.maintenancescheduling.service.validation.MaintenanceScheduleIssue.JobWindowTooShortIssue;
import org.acme.maintenancescheduling.service.validation.MaintenanceScheduleIssue.NonExistingCrewReferenceIssue;
import org.acme.maintenancescheduling.service.validation.MaintenanceScheduleIssue.StartDateOutsideWorkCalendarIssue;

@ApplicationScoped
public class MaintenanceScheduleValidator
        implements ModelValidator<MaintenanceScheduleInput, MaintenanceScheduleConfigOverrides> {

    @Override
    public void validate(ValidationBuilder validationBuilder, MaintenanceScheduleInput modelInput,
            ModelConfig<MaintenanceScheduleConfigOverrides> modelConfig) {
        // OpenAPI spec (Bean Validation) compliance is enforced by the Service module at the REST layer,
        // before this validator ever runs; only domain-specific checks belong here.
        validateWorkCalendar(validationBuilder, modelInput.workCalendar());
        Set<String> crewIds = validateCrews(validationBuilder, orEmpty(modelInput.crews()));
        validateJobs(validationBuilder, orEmpty(modelInput.jobs()), crewIds, modelInput.workCalendar());
    }

    private void validateWorkCalendar(ValidationBuilder validationBuilder, WorkCalendarInputDTO workCalendar) {
        if (workCalendar == null || countWorkdays(workCalendar) > 0) {
            return;
        }
        validationBuilder.addIssue(new EmptyWorkCalendarIssue(workCalendar.id()));
    }

    private Set<String> validateCrews(ValidationBuilder validationBuilder, List<CrewInputDTO> crews) {
        Set<String> crewIds = new HashSet<>();
        for (var crew : crews) {
            if (hasId(crew.id()) && !crewIds.add(crew.id())) {
                validationBuilder.addIssue(new DuplicateCrewIdIssue(crew.id()));
            }
        }
        return crewIds;
    }

    private void validateJobs(ValidationBuilder validationBuilder, List<JobInputDTO> jobs, Set<String> crewIds,
            WorkCalendarInputDTO workCalendar) {
        Set<String> jobIds = new HashSet<>();
        for (var job : jobs) {
            // At most one issue per job, so a single misconfigured job cannot flood the report.
            if (hasId(job.id()) && !jobIds.add(job.id())) {
                validationBuilder.addIssue(new DuplicateJobIdIssue(job.id()));
            } else if (job.crewId() != null && !crewIds.contains(job.crewId())) {
                validationBuilder.addIssue(new NonExistingCrewReferenceIssue(job.id()));
            } else if (isWindowTooShort(job)) {
                validationBuilder.addIssue(new JobWindowTooShortIssue(job.id()));
            } else if (isStartDateOutsideWorkCalendar(job, workCalendar)) {
                validationBuilder.addIssue(new StartDateOutsideWorkCalendarIssue(job.id()));
            }
        }
    }

    /**
     * A job started on its earliest allowed day still has to be finished by its maximum end date;
     * otherwise no assignment can ever satisfy both hard date constraints.
     */
    private static boolean isWindowTooShort(JobInputDTO job) {
        if (job.minStartDate() == null || job.maxEndDate() == null || job.durationInDays() == null) {
            return false;
        }
        LocalDate earliestEndDate = Job.calculateEndDate(job.minStartDate(), job.durationInDays());
        return earliestEndDate.isAfter(job.maxEndDate());
    }

    /**
     * A pre-assigned start date must be one of the workdays the solver can pick from, which is exactly
     * the set of non-weekend days inside the work calendar.
     */
    private static boolean isStartDateOutsideWorkCalendar(JobInputDTO job, WorkCalendarInputDTO workCalendar) {
        if (job.startDate() == null || workCalendar == null
                || workCalendar.fromDate() == null || workCalendar.toDate() == null) {
            return false;
        }
        return job.startDate().isBefore(workCalendar.fromDate())
                || !job.startDate().isBefore(workCalendar.toDate())
                || isWeekend(job.startDate());
    }

    private static long countWorkdays(WorkCalendarInputDTO workCalendar) {
        if (workCalendar.fromDate() == null || workCalendar.toDate() == null
                || !workCalendar.fromDate().isBefore(workCalendar.toDate())) {
            return 0;
        }
        return workCalendar.fromDate().datesUntil(workCalendar.toDate())
                .filter(date -> !isWeekend(date))
                .count();
    }

    private static boolean isWeekend(LocalDate date) {
        return date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY;
    }

    private static <T> List<T> orEmpty(List<T> list) {
        return list == null ? List.of() : list;
    }

    private static boolean hasId(String id) {
        return id != null && !id.isBlank();
    }
}
