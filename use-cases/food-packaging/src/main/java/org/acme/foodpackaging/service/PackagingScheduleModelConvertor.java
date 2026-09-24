package org.acme.foodpackaging.service;

import java.time.Duration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.LongFunction;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;

import ai.timefold.solver.core.api.domain.solution.ConstraintWeightOverrides;
import ai.timefold.solver.core.api.score.HardMediumSoftScore;
import ai.timefold.solver.service.definition.api.ModelConvertor;
import ai.timefold.solver.service.definition.api.domain.ModelConfig;

import org.acme.foodpackaging.domain.Job;
import org.acme.foodpackaging.domain.Line;
import org.acme.foodpackaging.domain.Operator;
import org.acme.foodpackaging.domain.PackagingSchedule;
import org.acme.foodpackaging.domain.PackagingScheduleConstraintProperties;
import org.acme.foodpackaging.domain.Product;
import org.acme.foodpackaging.domain.WorkCalendar;
import org.acme.foodpackaging.dto.input.CleaningDurationDTO;
import org.acme.foodpackaging.dto.input.JobDTO;
import org.acme.foodpackaging.dto.input.LineDTO;
import org.acme.foodpackaging.dto.input.PackagingScheduleConfigOverrides;
import org.acme.foodpackaging.dto.input.PackagingScheduleInput;
import org.acme.foodpackaging.dto.input.ProductDTO;
import org.acme.foodpackaging.dto.output.JobAssignmentDTO;
import org.acme.foodpackaging.dto.output.LineAssignmentDTO;
import org.acme.foodpackaging.dto.output.PackagingScheduleOutput;

@ApplicationScoped
public class PackagingScheduleModelConvertor
        implements
        ModelConvertor<HardMediumSoftScore, PackagingScheduleInput, PackagingScheduleConfigOverrides, PackagingSchedule, PackagingScheduleOutput> {

    @Override
    public PackagingSchedule toSolverModel(PackagingScheduleInput modelInput,
            ModelConfig<PackagingScheduleConfigOverrides> modelConfig,
            Optional<PackagingScheduleOutput> lastModelOutput) {
        Map<String, Product> productMap = toProducts(modelInput.products());
        Map<String, Operator> operatorMap = new LinkedHashMap<>();
        for (var dto : modelInput.operators()) {
            operatorMap.put(dto.id(), new Operator(dto.id(), dto.name()));
        }
        Map<String, Job> jobMap = new LinkedHashMap<>();
        for (JobDTO dto : modelInput.jobs()) {
            jobMap.put(dto.id(), toJob(dto, productMap));
        }
        Map<String, Line> lineMap = new LinkedHashMap<>();
        for (LineDTO dto : modelInput.lines()) {
            lineMap.put(dto.id(), toLine(dto, operatorMap, jobMap));
        }

        PackagingSchedule schedule = new PackagingSchedule(
                new WorkCalendar(modelInput.workCalendar().fromDate(), modelInput.workCalendar().toDate()),
                List.copyOf(productMap.values()), List.copyOf(operatorMap.values()), List.copyOf(lineMap.values()),
                List.copyOf(jobMap.values()));
        applyConstraintWeightOverrides(schedule, modelConfig);
        applyLastOutput(lineMap, operatorMap, jobMap, lastModelOutput);
        return schedule;
    }

    /**
     * Products reference each other through their cleaning durations, so every product is created with an
     * empty (but mutable) cleaning duration map first, and the maps are filled in once they all exist.
     */
    private static Map<String, Product> toProducts(List<ProductDTO> productDtos) {
        Map<String, Product> productMap = new LinkedHashMap<>();
        Map<String, Map<Product, Duration>> cleaningDurationsMap = new LinkedHashMap<>();
        for (ProductDTO dto : productDtos) {
            Map<Product, Duration> cleaningDurations = new LinkedHashMap<>();
            cleaningDurationsMap.put(dto.id(), cleaningDurations);
            productMap.put(dto.id(), new Product(dto.id(), dto.name(), cleaningDurations));
        }
        for (ProductDTO dto : productDtos) {
            Map<Product, Duration> cleaningDurations = cleaningDurationsMap.get(dto.id());
            for (CleaningDurationDTO cleaningDuration : dto.cleaningDurations()) {
                cleaningDurations.put(require(productMap, cleaningDuration.previousProductId(), "product"),
                        Duration.ofMinutes(cleaningDuration.durationMinutes()));
            }
        }
        return productMap;
    }

    private static Job toJob(JobDTO dto, Map<String, Product> productMap) {
        return new Job(dto.id(), dto.name(), require(productMap, dto.productId(), "product"),
                Duration.ofMinutes(dto.durationMinutes()), dto.minStartTime(), dto.idealEndTime(), dto.maxEndTime(),
                Boolean.TRUE.equals(dto.pinned()));
    }

    private static Line toLine(LineDTO dto, Map<String, Operator> operatorMap, Map<String, Job> jobMap) {
        Line line = new Line(dto.id(), dto.name(), dto.startDateTime());
        if (dto.operatorId() != null) {
            line.setOperator(require(operatorMap, dto.operatorId(), "operator"));
        }
        // Timefold moves jobs in and out of this list, so it must be mutable.
        dto.jobIds().stream()
                .map(jobId -> require(jobMap, jobId, "job"))
                .forEach(job -> line.getJobs().add(job));
        return line;
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

    private static void applyConstraintWeightOverrides(PackagingSchedule schedule,
            ModelConfig<PackagingScheduleConfigOverrides> modelConfig) {
        if (modelConfig == null || modelConfig.overrides() == null) {
            return;
        }
        PackagingScheduleConfigOverrides overrides = modelConfig.overrides();
        // Only apply weights that are actually set (non-null) in the merged overrides. A null weight means the
        // input did not override it, so the configuration profile value (or the constraint's default) is kept.
        Map<String, HardMediumSoftScore> weights = new HashMap<>();
        putIfPresent(weights, PackagingScheduleConstraintProperties.IDEAL_END_DATE_TIME,
                overrides.idealEndDateTimeWeight(), HardMediumSoftScore::ofMedium);
        putIfPresent(weights, PackagingScheduleConstraintProperties.MAXIMIZE_JOBS_ASSIGNED,
                overrides.maximizeJobsAssignedWeight(), HardMediumSoftScore::ofMedium);
        putIfPresent(weights, PackagingScheduleConstraintProperties.MINIMIZE_MAKESPAN,
                overrides.minimizeMakespanWeight(), HardMediumSoftScore::ofSoft);
        if (!weights.isEmpty()) {
            schedule.setConstraintWeightOverrides(ConstraintWeightOverrides.of(weights));
        }
    }

    private static void putIfPresent(Map<String, HardMediumSoftScore> weights, String constraintName, Long weight,
            LongFunction<HardMediumSoftScore> scoreFunction) {
        if (weight != null) {
            weights.put(constraintName, scoreFunction.apply(weight));
        }
    }

    private static void applyLastOutput(Map<String, Line> lineMap, Map<String, Operator> operatorMap,
            Map<String, Job> jobMap, Optional<PackagingScheduleOutput> lastModelOutput) {
        if (lastModelOutput.isEmpty()) {
            return;
        }
        for (LineAssignmentDTO solved : lastModelOutput.get().lines()) {
            Line line = lineMap.get(solved.id());
            if (line == null) {
                continue;
            }
            line.setOperator(solved.operatorId() == null ? null : operatorMap.get(solved.operatorId()));
            line.getJobs().clear();
            solved.jobIds().stream()
                    .map(jobMap::get)
                    .filter(Objects::nonNull)
                    .forEach(job -> line.getJobs().add(job));
        }
    }

    @Override
    public PackagingScheduleOutput toModelOutput(PackagingSchedule solverModel) {
        List<LineAssignmentDTO> lines = solverModel.getLines().stream()
                .map(PackagingScheduleModelConvertor::toLineAssignmentDTO)
                .toList();
        // Taken from the lines' job sequences rather than from Job.getLine(), so a job is reported on its
        // line even on a solution whose inverse relation shadow variables were never initialized.
        Map<String, String> lineIdByJobId = new HashMap<>();
        for (Line line : solverModel.getLines()) {
            line.getJobs().forEach(job -> lineIdByJobId.put(job.getId(), line.getId()));
        }
        List<JobAssignmentDTO> jobs = solverModel.getJobs().stream()
                .map(job -> toJobAssignmentDTO(job, lineIdByJobId.get(job.getId())))
                .toList();
        return new PackagingScheduleOutput(lines, jobs);
    }

    private static LineAssignmentDTO toLineAssignmentDTO(Line line) {
        String operatorId = line.getOperator() == null ? null : line.getOperator().id();
        return new LineAssignmentDTO(line.getId(), operatorId, line.getJobs().stream().map(Job::getId).toList());
    }

    private static JobAssignmentDTO toJobAssignmentDTO(Job job, String lineId) {
        return new JobAssignmentDTO(job.getId(), lineId, job.getStartCleaningDateTime(),
                job.getStartProductionDateTime(), job.getEndDateTime());
    }

    @Override
    public PackagingScheduleInput applyOutputToInput(PackagingScheduleInput modelInput,
            PackagingScheduleOutput modelOutput) {
        Map<String, LineAssignmentDTO> outputLines = modelOutput.lines().stream()
                .collect(Collectors.toMap(LineAssignmentDTO::id, line -> line));
        List<LineDTO> updatedLines = modelInput.lines().stream()
                .map(line -> {
                    LineAssignmentDTO solved = outputLines.get(line.id());
                    if (solved == null) {
                        return line;
                    }
                    return line.withAssignment(solved.operatorId(), solved.jobIds());
                })
                .toList();
        return modelInput.withLines(updatedLines);
    }
}
