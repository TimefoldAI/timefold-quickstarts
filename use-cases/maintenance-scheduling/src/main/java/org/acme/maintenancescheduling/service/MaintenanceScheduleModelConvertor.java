package org.acme.maintenancescheduling.service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SequencedSet;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;

import ai.timefold.solver.core.api.domain.solution.ConstraintWeightOverrides;
import ai.timefold.solver.core.api.score.HardMediumSoftScore;
import ai.timefold.solver.service.definition.api.ModelConvertor;
import ai.timefold.solver.service.definition.api.domain.ModelConfig;

import org.acme.maintenancescheduling.domain.Crew;
import org.acme.maintenancescheduling.domain.Job;
import org.acme.maintenancescheduling.domain.MaintenanceSchedule;
import org.acme.maintenancescheduling.domain.WorkCalendar;
import org.acme.maintenancescheduling.dto.CrewDTO;
import org.acme.maintenancescheduling.dto.JobDTO;
import org.acme.maintenancescheduling.dto.MaintenanceScheduleConfigOverrides;
import org.acme.maintenancescheduling.dto.MaintenanceScheduleInput;
import org.acme.maintenancescheduling.dto.MaintenanceScheduleOutput;
import org.acme.maintenancescheduling.dto.WorkCalendarDTO;
import org.acme.maintenancescheduling.solver.MaintenanceScheduleConstraintProvider;

@ApplicationScoped
public class MaintenanceScheduleModelConvertor
        implements
        ModelConvertor<HardMediumSoftScore, MaintenanceScheduleInput, MaintenanceScheduleConfigOverrides, MaintenanceSchedule, MaintenanceScheduleOutput> {

    @Override
    public MaintenanceScheduleInput applyOutputToInput(MaintenanceScheduleInput modelInput,
            MaintenanceScheduleOutput modelOutput) {
        Map<String, JobDTO> outputJobs = modelOutput.jobs().stream()
                .collect(Collectors.toMap(JobDTO::id, job -> job));
        List<JobDTO> updatedJobs = modelInput.jobs().stream()
                .map(job -> {
                    JobDTO solved = outputJobs.get(job.id());
                    if (solved == null) {
                        return job;
                    }
                    return job.withCrewId(solved.crewId()).withStartDate(solved.startDate()).withEndDate(solved.endDate());
                })
                .collect(Collectors.toList());
        return new MaintenanceScheduleInput(modelInput.workCalendar(), modelInput.crews(), updatedJobs);
    }

    @Override
    public MaintenanceSchedule toSolverModel(MaintenanceScheduleInput modelInput,
            ModelConfig<MaintenanceScheduleConfigOverrides> modelConfig,
            Optional<MaintenanceScheduleOutput> lastModelOutput) {
        WorkCalendarDTO calendarDto = modelInput.workCalendar();
        WorkCalendar workCalendar = new WorkCalendar(calendarDto.id(),
                LocalDate.parse(calendarDto.fromDate()), LocalDate.parse(calendarDto.toDate()));

        Map<String, Crew> crewMap = new HashMap<>();
        List<Crew> crews = modelInput.crews().stream().map(dto -> {
            Crew crew = new Crew(dto.id(), dto.name());
            crewMap.put(crew.getId(), crew);
            return crew;
        }).collect(Collectors.toList());

        List<Job> jobs = modelInput.jobs().stream().map(dto -> toJob(dto, crewMap)).collect(Collectors.toList());

        MaintenanceSchedule schedule = new MaintenanceSchedule(workCalendar, crews, jobs);
        applyConstraintWeightOverrides(schedule, modelConfig);
        applyLastOutput(jobs, crewMap, lastModelOutput);
        return schedule;
    }

    private static Job toJob(JobDTO dto, Map<String, Crew> crewMap) {
        SequencedSet<String> tags = new LinkedHashSet<>(dto.tags());
        Job job = new Job(dto.id(), dto.name(), dto.durationInDays(),
                parseDate(dto.minStartDate()), parseDate(dto.maxEndDate()), parseDate(dto.idealEndDate()), tags);
        if (dto.crewId() != null) {
            job.setCrew(crewMap.get(dto.crewId()));
        }
        if (dto.startDate() != null) {
            job.setStartDate(LocalDate.parse(dto.startDate()));
            job.setEndDate(Job.calculateEndDate(job.getStartDate(), job.getDurationInDays()));
        }
        return job;
    }

    private static LocalDate parseDate(String value) {
        return value == null ? null : LocalDate.parse(value);
    }

    private static void applyConstraintWeightOverrides(MaintenanceSchedule schedule,
            ModelConfig<MaintenanceScheduleConfigOverrides> modelConfig) {
        if (modelConfig == null || modelConfig.overrides() == null) {
            return;
        }
        MaintenanceScheduleConfigOverrides overrides = modelConfig.overrides();
        // Only apply weights that are actually set (non-null) in the merged overrides. A null weight means the
        // input did not override it, so the configuration profile value (or the constraint's default) is kept.
        Map<String, HardMediumSoftScore> weights = new HashMap<>();
        putIfPresent(weights, MaintenanceScheduleConstraintProvider.BEFORE_IDEAL_END_DATE,
                overrides.beforeIdealEndDateWeight());
        putIfPresent(weights, MaintenanceScheduleConstraintProvider.AFTER_IDEAL_END_DATE,
                overrides.afterIdealEndDateWeight());
        putIfPresent(weights, MaintenanceScheduleConstraintProvider.TAG_CONFLICT, overrides.tagConflictWeight());
        if (!weights.isEmpty()) {
            schedule.setConstraintWeightOverrides(ConstraintWeightOverrides.of(weights));
        }
    }

    private static void putIfPresent(Map<String, HardMediumSoftScore> weights, String constraintName, Long weight) {
        if (weight != null) {
            weights.put(constraintName, HardMediumSoftScore.ofSoft(weight));
        }
    }

    private static void applyLastOutput(List<Job> jobs, Map<String, Crew> crewMap,
            Optional<MaintenanceScheduleOutput> lastModelOutput) {
        if (lastModelOutput.isEmpty()) {
            return;
        }
        Map<String, JobDTO> assignmentMap = lastModelOutput.get().jobs().stream()
                .collect(Collectors.toMap(JobDTO::id, job -> job));
        for (Job job : jobs) {
            JobDTO solved = assignmentMap.get(job.getId());
            if (solved == null) {
                continue;
            }
            if (solved.crewId() != null) {
                job.setCrew(crewMap.get(solved.crewId()));
            }
            if (solved.startDate() != null) {
                job.setStartDate(LocalDate.parse(solved.startDate()));
                job.setEndDate(Job.calculateEndDate(job.getStartDate(), job.getDurationInDays()));
            }
        }
    }

    @Override
    public MaintenanceScheduleOutput toModelOutput(MaintenanceSchedule solverModel) {
        List<JobDTO> jobs = solverModel.getJobs().stream().map(this::toDTO).collect(Collectors.toList());
        List<CrewDTO> crews = solverModel.getCrews().stream().map(this::toDTO).collect(Collectors.toList());
        WorkCalendarDTO workCalendar = toDTO(solverModel.getWorkCalendar());
        String score = solverModel.getScore() == null ? "" : solverModel.getScore().toString();
        return new MaintenanceScheduleOutput(workCalendar, crews, jobs, score);
    }

    private WorkCalendarDTO toDTO(WorkCalendar workCalendar) {
        return new WorkCalendarDTO(workCalendar.getId(), workCalendar.getFromDate().toString(),
                workCalendar.getToDate().toString());
    }

    private CrewDTO toDTO(Crew crew) {
        return new CrewDTO(crew.getId(), crew.getName());
    }

    private JobDTO toDTO(Job job) {
        String crewId = job.getCrew() == null ? null : job.getCrew().getId();
        String startDate = job.getStartDate() == null ? null : job.getStartDate().toString();
        String endDate = job.getEndDate() == null ? null : job.getEndDate().toString();
        return new JobDTO(job.getId(), job.getName(), job.getDurationInDays(),
                dateToString(job.getMinStartDate()), dateToString(job.getMaxEndDate()), dateToString(job.getIdealEndDate()),
                List.copyOf(job.getTags()), crewId, startDate, endDate);
    }

    private static String dateToString(LocalDate date) {
        return date == null ? null : date.toString();
    }
}
