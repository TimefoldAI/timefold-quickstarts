package org.acme.foodpackaging.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import org.acme.foodpackaging.domain.Product;
import org.acme.foodpackaging.domain.WorkCalendar;
import org.acme.foodpackaging.dto.JobDTO;
import org.acme.foodpackaging.dto.LineDTO;
import org.acme.foodpackaging.dto.OperatorDTO;
import org.acme.foodpackaging.dto.PackagingScheduleConfigOverrides;
import org.acme.foodpackaging.dto.PackagingScheduleInput;
import org.acme.foodpackaging.dto.PackagingScheduleOutput;
import org.acme.foodpackaging.dto.ProductDTO;
import org.acme.foodpackaging.dto.WorkCalendarDTO;
import org.acme.foodpackaging.solver.FoodPackagingConstraintProvider;

@ApplicationScoped
public class PackagingScheduleModelConvertor
        implements
        ModelConvertor<HardMediumSoftScore, PackagingScheduleInput, PackagingScheduleConfigOverrides, PackagingSchedule, PackagingScheduleOutput> {

    @Override
    public PackagingScheduleInput applyOutputToInput(PackagingScheduleInput modelInput,
            PackagingScheduleOutput modelOutput) {
        Map<String, JobDTO> outputJobs = modelOutput.jobs().stream()
                .collect(Collectors.toMap(JobDTO::id, job -> job));
        Map<String, LineDTO> outputLines = modelOutput.lines().stream()
                .collect(Collectors.toMap(LineDTO::id, line -> line));
        List<JobDTO> updatedJobs = modelInput.jobs().stream()
                .map(job -> {
                    JobDTO solved = outputJobs.get(job.id());
                    return solved == null ? job : job.withLineId(solved.lineId());
                })
                .collect(Collectors.toList());
        List<LineDTO> updatedLines = modelInput.lines().stream()
                .map(line -> {
                    LineDTO solved = outputLines.get(line.id());
                    return solved == null ? line : line.withOperatorId(solved.operatorId()).withJobIds(solved.jobIds());
                })
                .collect(Collectors.toList());
        return new PackagingScheduleInput(modelInput.workCalendar(), modelInput.products(), modelInput.operators(),
                updatedLines, updatedJobs);
    }

    @Override
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public PackagingSchedule toSolverModel(PackagingScheduleInput modelInput,
            ModelConfig<PackagingScheduleConfigOverrides> modelConfig,
            Optional<PackagingScheduleOutput> lastModelOutput) {
        Map<String, Product> productMap = new HashMap<>();
        List<Product> products = modelInput.products().stream()
                .map(dto -> {
                    Product product = new Product(dto.id(), dto.name());
                    productMap.put(product.getId(), product);
                    return product;
                })
                .collect(Collectors.toList());
        for (ProductDTO dto : modelInput.products()) {
            Product product = productMap.get(dto.id());
            Map<Product, Duration> cleaningDurations = new HashMap<>();
            dto.cleaningDurations().forEach((previousProductId, minutes) -> {
                Product previousProduct = productMap.get(previousProductId);
                if (previousProduct != null) {
                    cleaningDurations.put(previousProduct, Duration.ofMinutes(minutes));
                }
            });
            product.setCleaningDurations(cleaningDurations);
        }

        Map<String, Operator> operatorMap = new HashMap<>();
        List<Operator> operators = modelInput.operators().stream()
                .map(dto -> {
                    Operator operator = new Operator(dto.id());
                    operatorMap.put(operator.getId(), operator);
                    return operator;
                })
                .collect(Collectors.toList());

        Map<String, Job> jobMap = new HashMap<>();
        List<Job> jobs = modelInput.jobs().stream()
                .map(dto -> {
                    Job job = toJob(dto, productMap);
                    jobMap.put(job.getId(), job);
                    return job;
                })
                .collect(Collectors.toList());

        List<Line> lines = modelInput.lines().stream()
                .map(dto -> toLine(dto, operatorMap, jobMap))
                .collect(Collectors.toList());

        WorkCalendar workCalendar = toWorkCalendar(modelInput.workCalendar());
        PackagingSchedule schedule = new PackagingSchedule(workCalendar, products, operators, lines, jobs);
        applyConstraintWeightOverrides(schedule, modelConfig);
        applyLastOutput(lines, jobMap, lastModelOutput);
        return schedule;
    }

    private static Job toJob(JobDTO dto, Map<String, Product> productMap) {
        Product product = dto.productId() == null ? null : productMap.get(dto.productId());
        return new Job(dto.id(), dto.name(), product, Duration.ofMinutes(dto.durationMinutes()),
                parseDateTime(dto.minStartTime()), parseDateTime(dto.idealEndTime()), parseDateTime(dto.maxEndTime()),
                dto.priority(), dto.pinned());
    }

    private static Line toLine(LineDTO dto, Map<String, Operator> operatorMap, Map<String, Job> jobMap) {
        Operator operator = dto.operatorId() == null ? null : operatorMap.get(dto.operatorId());
        Line line = new Line(dto.id(), dto.name(), operator, parseDateTime(dto.startDateTime()));
        List<Job> orderedJobs = dto.jobIds().stream()
                .map(jobMap::get)
                .filter(job -> job != null)
                .collect(Collectors.toList());
        line.setJobs(orderedJobs);
        return line;
    }

    private static WorkCalendar toWorkCalendar(WorkCalendarDTO dto) {
        if (dto == null) {
            return null;
        }
        LocalDate fromDate = dto.fromDate() == null ? null : LocalDate.parse(dto.fromDate());
        LocalDate toDate = dto.toDate() == null ? null : LocalDate.parse(dto.toDate());
        return new WorkCalendar(fromDate, toDate);
    }

    private static LocalDateTime parseDateTime(String value) {
        return value == null || value.isBlank() ? null : LocalDateTime.parse(value);
    }

    private static void applyConstraintWeightOverrides(PackagingSchedule schedule,
            ModelConfig<PackagingScheduleConfigOverrides> modelConfig) {
        if (modelConfig == null || modelConfig.overrides() == null) {
            return;
        }
        PackagingScheduleConfigOverrides overrides = modelConfig.overrides();
        // Only apply weights that are actually set (non-null) in the merged overrides. A null weight means the
        // input did not override it, so the configuration profile value (or the constraint's default) is kept.
        Map<String, HardMediumSoftScore> weightOverrides = new HashMap<>();
        putMediumIfPresent(weightOverrides, FoodPackagingConstraintProvider.IDEAL_END_DATE_TIME,
                overrides.idealEndDateTimeWeight());
        putMediumIfPresent(weightOverrides, FoodPackagingConstraintProvider.MAXIMIZE_JOBS_ASSIGNED,
                overrides.maximizeJobsAssignedWeight());
        putSoftIfPresent(weightOverrides, FoodPackagingConstraintProvider.MINIMIZE_MAKESPAN,
                overrides.minimizeMakespanWeight());
        if (!weightOverrides.isEmpty()) {
            schedule.setConstraintWeightOverrides(ConstraintWeightOverrides.of(weightOverrides));
        }
    }

    private static void putMediumIfPresent(Map<String, HardMediumSoftScore> weights, String constraintName, Long weight) {
        if (weight != null) {
            weights.put(constraintName, HardMediumSoftScore.ofMedium(weight));
        }
    }

    private static void putSoftIfPresent(Map<String, HardMediumSoftScore> weights, String constraintName, Long weight) {
        if (weight != null) {
            weights.put(constraintName, HardMediumSoftScore.ofSoft(weight));
        }
    }

    private static void applyLastOutput(List<Line> lines, Map<String, Job> jobMap,
            Optional<PackagingScheduleOutput> lastModelOutput) {
        if (lastModelOutput.isEmpty()) {
            return;
        }
        Map<String, LineDTO> assignmentMap = lastModelOutput.get().lines().stream()
                .collect(Collectors.toMap(LineDTO::id, line -> line));
        for (Line line : lines) {
            LineDTO solved = assignmentMap.get(line.getId());
            if (solved == null) {
                continue;
            }
            List<Job> orderedJobs = solved.jobIds().stream()
                    .map(jobMap::get)
                    .filter(job -> job != null)
                    .collect(Collectors.toList());
            line.setJobs(orderedJobs);
        }
    }

    @Override
    public PackagingScheduleOutput toModelOutput(PackagingSchedule solverModel) {
        List<ProductDTO> products = solverModel.getProducts().stream().map(this::toDTO).collect(Collectors.toList());
        List<OperatorDTO> operators =
                solverModel.getOperators().stream().map(this::toDTO).collect(Collectors.toList());
        List<LineDTO> lines = solverModel.getLines().stream().map(this::toDTO).collect(Collectors.toList());
        List<JobDTO> jobs = solverModel.getJobs().stream().map(this::toDTO).collect(Collectors.toList());
        WorkCalendarDTO workCalendar = toDTO(solverModel.getWorkCalendar());
        String score = solverModel.getScore() == null ? "" : solverModel.getScore().toString();
        return new PackagingScheduleOutput(workCalendar, products, operators, lines, jobs, score);
    }

    private WorkCalendarDTO toDTO(WorkCalendar workCalendar) {
        String fromDate = workCalendar == null || workCalendar.getFromDate() == null ? null
                : workCalendar.getFromDate().toString();
        String toDate = workCalendar == null || workCalendar.getToDate() == null ? null
                : workCalendar.getToDate().toString();
        return new WorkCalendarDTO(fromDate, toDate);
    }

    private ProductDTO toDTO(Product product) {
        Map<String, Long> cleaningDurations = new HashMap<>();
        if (product.getCleaningDurations() != null) {
            product.getCleaningDurations()
                    .forEach((previousProduct, duration) -> cleaningDurations.put(previousProduct.getId(),
                            duration.toMinutes()));
        }
        return new ProductDTO(product.getId(), product.getName(), cleaningDurations);
    }

    private OperatorDTO toDTO(Operator operator) {
        return new OperatorDTO(operator.getId());
    }

    private LineDTO toDTO(Line line) {
        String operatorId = line.getOperator() == null ? null : line.getOperator().getId();
        List<String> jobIds = line.getJobs().stream().map(Job::getId).collect(Collectors.toList());
        String startDateTime = line.getStartDateTime() == null ? null : line.getStartDateTime().toString();
        return new LineDTO(line.getId(), line.getName(), startDateTime, operatorId, jobIds);
    }

    private JobDTO toDTO(Job job) {
        String productId = job.getProduct() == null ? null : job.getProduct().getId();
        String lineId = job.getLine() == null ? null : job.getLine().getId();
        String operatorId = job.getLineOperator() == null ? null : job.getLineOperator().getId();
        return new JobDTO(job.getId(), job.getName(), productId, job.getDuration().toMinutes(),
                toStringOrNull(job.getMinStartTime()), toStringOrNull(job.getIdealEndTime()),
                toStringOrNull(job.getMaxEndTime()), job.getPriority(), job.isPinned(), lineId,
                toStringOrNull(job.getStartCleaningDateTime()), toStringOrNull(job.getStartProductionDateTime()),
                toStringOrNull(job.getEndDateTime()), operatorId);
    }

    private static String toStringOrNull(LocalDateTime value) {
        return value == null ? null : value.toString();
    }
}
