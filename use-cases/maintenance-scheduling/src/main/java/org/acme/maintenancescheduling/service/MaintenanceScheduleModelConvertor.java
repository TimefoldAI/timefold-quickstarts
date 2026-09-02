package org.acme.maintenancescheduling.service;

import static java.util.stream.Collectors.toCollection;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SequencedSet;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;

import ai.timefold.solver.core.api.domain.solution.ConstraintWeightOverrides;
import ai.timefold.solver.core.api.score.HardSoftScore;
import ai.timefold.solver.service.definition.api.ModelConvertor;
import ai.timefold.solver.service.definition.api.domain.ModelConfig;

import org.acme.maintenancescheduling.domain.Crew;
import org.acme.maintenancescheduling.domain.Job;
import org.acme.maintenancescheduling.domain.MaintenanceSchedule;
import org.acme.maintenancescheduling.domain.MaintenanceScheduleConstraintProperties;
import org.acme.maintenancescheduling.domain.WorkCalendar;
import org.acme.maintenancescheduling.dto.input.CrewInputDTO;
import org.acme.maintenancescheduling.dto.input.JobInputDTO;
import org.acme.maintenancescheduling.dto.input.MaintenanceScheduleConfigOverrides;
import org.acme.maintenancescheduling.dto.input.MaintenanceScheduleInput;
import org.acme.maintenancescheduling.dto.input.WorkCalendarInputDTO;
import org.acme.maintenancescheduling.dto.output.JobOutputDTO;
import org.acme.maintenancescheduling.dto.output.MaintenanceScheduleOutput;

@ApplicationScoped
public class MaintenanceScheduleModelConvertor implements
        ModelConvertor<HardSoftScore, MaintenanceScheduleInput, MaintenanceScheduleConfigOverrides, MaintenanceSchedule, MaintenanceScheduleOutput> {

    @Override
    public MaintenanceSchedule toSolverModel(MaintenanceScheduleInput modelInput,
            ModelConfig<MaintenanceScheduleConfigOverrides> modelConfig,
            Optional<MaintenanceScheduleOutput> lastModelOutput) {
        Map<String, Crew> crewMap = modelInput.crews().stream()
                .map(MaintenanceScheduleModelConvertor::toCrew)
                .collect(Collectors.toMap(Crew::id, crew -> crew, (first, second) -> first, LinkedHashMap::new));
        List<Job> jobs = modelInput.jobs().stream()
                .map(dto -> toJob(dto, crewMap))
                .toList();

        MaintenanceSchedule schedule = new MaintenanceSchedule(toWorkCalendar(modelInput.workCalendar()),
                List.copyOf(crewMap.values()), jobs);
        applyConstraintWeightOverrides(schedule, modelConfig);
        applyLastOutput(jobs, crewMap, lastModelOutput);
        return schedule;
    }

    @Override
    public MaintenanceScheduleOutput toModelOutput(MaintenanceSchedule solverModel) {
        List<JobOutputDTO> jobs = solverModel.getJobs().stream()
                .map(job -> new JobOutputDTO(job.getId(), job.getCrew() == null ? null : job.getCrew().id(),
                        job.getStartDate(), job.calculateEndDate()))
                .toList();
        return new MaintenanceScheduleOutput(jobs);
    }

    @Override
    public MaintenanceScheduleInput applyOutputToInput(MaintenanceScheduleInput modelInput,
            MaintenanceScheduleOutput modelOutput) {
        Map<String, JobOutputDTO> outputJobs =
                modelOutput.jobs().stream().collect(Collectors.toMap(JobOutputDTO::id, job -> job));
        List<JobInputDTO> updatedJobs = modelInput.jobs().stream()
                .map(job -> {
                    JobOutputDTO solved = outputJobs.get(job.id());
                    return solved == null ? job : job.withAssignment(solved.crewId(), solved.startDate());
                })
                .toList();
        return modelInput.withJobs(updatedJobs);
    }

    private static WorkCalendar toWorkCalendar(WorkCalendarInputDTO dto) {
        return new WorkCalendar(dto.id(), dto.fromDate(), dto.toDate());
    }

    private static Crew toCrew(CrewInputDTO dto) {
        return new Crew(dto.id(), dto.name());
    }

    private static Job toJob(JobInputDTO dto, Map<String, Crew> crewMap) {
        Crew crew = dto.crewId() == null ? null : require(crewMap, dto.crewId(), "crew");
        SequencedSet<String> tags = dto.tags().stream().collect(toCollection(LinkedHashSet::new));
        return new Job(dto.id(), dto.name(), dto.durationInDays(), dto.minStartDate(), dto.maxEndDate(),
                dto.idealEndDate(), tags, crew, dto.startDate());
    }

    /**
     * Fails fast with an actionable message instead of letting an unknown reference
     * turn into a null in the solver model and a delayed NullPointerException.
     */
    private static <T> T require(Map<String, T> map, String key, String kind) {
        T value = map.get(key);
        if (value == null) {
            throw new IllegalArgumentException("Unknown %s '%s'.".formatted(kind, key));
        }
        return value;
    }

    private static void applyConstraintWeightOverrides(MaintenanceSchedule schedule,
            ModelConfig<MaintenanceScheduleConfigOverrides> modelConfig) {
        if (modelConfig == null || modelConfig.overrides() == null) {
            return;
        }
        var overrides = modelConfig.overrides();
        // Only apply weights that are actually set (non-null) in the merged overrides. A null weight means the
        // input did not override it, so the configuration profile value (or the constraint's default) is kept.
        Map<String, HardSoftScore> weights = new HashMap<>();
        putIfPresent(weights, MaintenanceScheduleConstraintProperties.BEFORE_IDEAL_END_DATE,
                overrides.beforeIdealEndDateWeight());
        putIfPresent(weights, MaintenanceScheduleConstraintProperties.AFTER_IDEAL_END_DATE,
                overrides.afterIdealEndDateWeight());
        putIfPresent(weights, MaintenanceScheduleConstraintProperties.TAG_CONFLICT, overrides.tagConflictWeight());
        if (!weights.isEmpty()) {
            schedule.setConstraintWeightOverrides(ConstraintWeightOverrides.of(weights));
        }
    }

    private static void putIfPresent(Map<String, HardSoftScore> weights, String constraintName, Long weight) {
        if (weight != null) {
            weights.put(constraintName, HardSoftScore.ofSoft(weight));
        }
    }

    // lastModelOutput is used to recover a run that stopped halfway, so it overrides the input assignment.
    private static void applyLastOutput(List<Job> jobs, Map<String, Crew> crewMap,
            Optional<MaintenanceScheduleOutput> lastModelOutput) {
        if (lastModelOutput.isEmpty()) {
            return;
        }
        Map<String, Job> jobMap = jobs.stream().collect(Collectors.toMap(Job::getId, job -> job));
        for (var solved : lastModelOutput.get().jobs()) {
            Job job = jobMap.get(solved.id());
            if (job == null || solved.crewId() == null || solved.startDate() == null) {
                continue;
            }
            Crew crew = crewMap.get(solved.crewId());
            if (crew != null) {
                job.setCrew(crew);
                job.setStartDate(solved.startDate());
                job.setEndDate(Job.calculateEndDate(solved.startDate(), job.getDurationInDays()));
            }
        }
    }
}
