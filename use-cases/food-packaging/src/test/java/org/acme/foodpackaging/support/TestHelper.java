package org.acme.foodpackaging.support;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.acme.foodpackaging.domain.Job;
import org.acme.foodpackaging.domain.Line;
import org.acme.foodpackaging.domain.Operator;
import org.acme.foodpackaging.domain.Product;
import org.acme.foodpackaging.dto.input.CleaningDurationDTO;
import org.acme.foodpackaging.dto.input.JobDTO;
import org.acme.foodpackaging.dto.input.LineDTO;
import org.acme.foodpackaging.dto.input.OperatorDTO;
import org.acme.foodpackaging.dto.input.PackagingScheduleInput;
import org.acme.foodpackaging.dto.input.ProductDTO;
import org.acme.foodpackaging.dto.input.WorkCalendarDTO;

// To keep our production classes as simple as possible, we've added these methods to help construct the data needed for testing.
public final class TestHelper {

    public static final LocalDate FROM_DATE = LocalDate.of(2024, 1, 1);
    public static final OffsetDateTime LINE_START = at(FROM_DATE, 8);

    public static final WorkCalendarDTO WORK_CALENDAR = new WorkCalendarDTO(FROM_DATE, FROM_DATE.plusWeeks(2));

    public static final String PRODUCT_1 = "p1";
    public static final String PRODUCT_2 = "p2";

    public static final List<ProductDTO> PRODUCTS = List.of(
            product(PRODUCT_1, List.of(PRODUCT_1, PRODUCT_2)),
            product(PRODUCT_2, List.of(PRODUCT_1, PRODUCT_2)));

    public static final List<OperatorDTO> OPERATORS = List.of(operator("o1"), operator("o2"));

    public static final List<LineDTO> LINES = List.of(line("l1"), line("l2"));

    public static final List<JobDTO> JOBS = List.of(
            job("j1", PRODUCT_1),
            job("j2", PRODUCT_2),
            job("j3", PRODUCT_1),
            job("j4", PRODUCT_2));

    private TestHelper() {
    }

    // ------------------------------------------------------------------------
    // Input DTOs
    // ------------------------------------------------------------------------

    /**
     * @return a complete, feasible problem with every job still unscheduled: the four jobs fit on the two
     *         lines well before their ideal end time, even with a changeover cleaning in between
     */
    public static PackagingScheduleInput createProblem() {
        return input(PRODUCTS, OPERATORS, LINES, JOBS);
    }

    public static PackagingScheduleInput input(List<ProductDTO> products, List<OperatorDTO> operators,
            List<LineDTO> lines, List<JobDTO> jobs) {
        return new PackagingScheduleInput(WORK_CALENDAR, products, operators, lines, jobs);
    }

    /**
     * Uses a single job of the first given product, so overriding the products of a dataset never turns the
     * default jobs into dangling product references.
     */
    public static PackagingScheduleInput inputWithProducts(ProductDTO... products) {
        return input(List.of(products), OPERATORS, LINES, List.of(job("j1", products[0].id())));
    }

    public static PackagingScheduleInput inputWithOperators(OperatorDTO... operators) {
        return input(PRODUCTS, List.of(operators), LINES, JOBS);
    }

    public static PackagingScheduleInput inputWithLines(LineDTO... lines) {
        return input(PRODUCTS, OPERATORS, List.of(lines), JOBS);
    }

    public static PackagingScheduleInput inputWithJobs(JobDTO... jobs) {
        return input(PRODUCTS, OPERATORS, LINES, List.of(jobs));
    }

    /**
     * @return a product that only knows the cleaning duration from itself, so it is complete on its own
     */
    public static ProductDTO product(String id) {
        return product(id, List.of(id));
    }

    public static ProductDTO product(String id, List<String> previousProductIds) {
        List<CleaningDurationDTO> cleaningDurations = previousProductIds.stream()
                .map(previousProductId -> new CleaningDurationDTO(previousProductId, previousProductId.equals(id) ? 0L : 30L))
                .toList();
        return new ProductDTO(id, "Product " + id, cleaningDurations);
    }

    public static OperatorDTO operator(String id) {
        return new OperatorDTO(id, "Operator " + id);
    }

    public static LineDTO line(String id) {
        return new LineDTO(id, "Line " + id, LINE_START, null, List.of());
    }

    public static LineDTO scheduledLine(String id, String operatorId, String... jobIds) {
        return new LineDTO(id, "Line " + id, LINE_START, operatorId, List.of(jobIds));
    }

    public static JobDTO job(String id, String productId) {
        return new JobDTO(id, "Job " + id, productId, 120L, LINE_START, at(FROM_DATE.plusDays(2), 16),
                at(FROM_DATE.plusDays(4), 16), false);
    }

    /**
     * The whole model works in UTC, so that is the only offset the tests ever need.
     */
    public static OffsetDateTime at(LocalDate date, int hour) {
        return OffsetDateTime.of(date, LocalTime.of(hour, 0), ZoneOffset.UTC);
    }

    // ------------------------------------------------------------------------
    // Solver model
    // ------------------------------------------------------------------------

    public static ProductBuilder aProduct(String id) {
        return new ProductBuilder(id);
    }

    public static Operator anOperator(String id) {
        return new Operator(id, "Operator " + id);
    }

    public static LineBuilder aLine(String id) {
        return new LineBuilder(id);
    }

    public static JobBuilder aJob(String id) {
        return new JobBuilder(id);
    }

    /**
     * Puts the given jobs on the line, in the given order, and fills in the shadow variables the solver
     * would derive from that: the line, its operator and the previous/next job of every job.
     * <p>
     * The production times themselves are left to {@link JobBuilder}, so a test can state the exact
     * timing it wants to verify instead of having to work back from a cleaning duration matrix.
     */
    public static void assignJobs(Line line, Job... jobs) {
        for (int i = 0; i < jobs.length; i++) {
            Job job = jobs[i];
            job.setLine(line);
            job.setLineOperator(line.getOperator());
            line.getJobs().add(job);
            if (i > 0) {
                job.setPreviousJob(jobs[i - 1]);
            }
            if (i < jobs.length - 1) {
                job.setNextJob(jobs[i + 1]);
            }
        }
    }

    /**
     * Builds a {@link Product} for tests, so a test only has to state the fields it actually cares about.
     * <p>
     * Production code calls the {@link Product} constructor directly; this builder deliberately lives in the
     * test sources so the domain class stays free of construction scaffolding.
     */
    public static final class ProductBuilder {

        private final String id;
        private String name;
        private final Map<Product, Duration> cleaningDurations = new LinkedHashMap<>();

        private ProductBuilder(String id) {
            this.id = id;
            this.name = "Product " + id;
        }

        public ProductBuilder name(String name) {
            this.name = name;
            return this;
        }

        public Product build() {
            return new Product(id, name, cleaningDurations);
        }
    }

    /**
     * Builds a {@link Line} for tests, so a test only has to state the fields it actually cares about.
     * <p>
     * Production code calls the {@link Line} constructor directly; this builder deliberately lives in the
     * test sources so the domain class stays free of construction scaffolding.
     */
    public static final class LineBuilder {

        private final String id;
        private String name;
        private OffsetDateTime startDateTime = LINE_START;
        private Operator operator;

        private LineBuilder(String id) {
            this.id = id;
            this.name = "Line " + id;
        }

        public LineBuilder name(String name) {
            this.name = name;
            return this;
        }

        public LineBuilder startDateTime(OffsetDateTime startDateTime) {
            this.startDateTime = startDateTime;
            return this;
        }

        public LineBuilder operator(Operator operator) {
            this.operator = operator;
            return this;
        }

        public Line build() {
            Line line = new Line(id, name, startDateTime);
            line.setOperator(operator);
            return line;
        }
    }

    /**
     * Builds a {@link Job} for tests, so a test only has to state the fields it actually cares about.
     * <p>
     * Production code calls the {@link Job} constructor directly; this builder deliberately lives in the
     * test sources so the domain class stays free of construction scaffolding.
     */
    public static final class JobBuilder {

        private final String id;
        private String name;
        private Product product = aProduct("default").build();
        private Duration duration = Duration.ofMinutes(60);
        private OffsetDateTime minStartTime;
        private OffsetDateTime idealEndTime;
        private OffsetDateTime maxEndTime;
        private boolean pinned;
        private OffsetDateTime startCleaningDateTime;
        private OffsetDateTime startProductionDateTime;

        private JobBuilder(String id) {
            this.id = id;
            this.name = "Job " + id;
        }

        public JobBuilder name(String name) {
            this.name = name;
            return this;
        }

        public JobBuilder product(Product product) {
            this.product = product;
            return this;
        }

        public JobBuilder duration(Duration duration) {
            this.duration = duration;
            return this;
        }

        public JobBuilder minStartTime(OffsetDateTime minStartTime) {
            this.minStartTime = minStartTime;
            return this;
        }

        public JobBuilder idealEndTime(OffsetDateTime idealEndTime) {
            this.idealEndTime = idealEndTime;
            return this;
        }

        public JobBuilder maxEndTime(OffsetDateTime maxEndTime) {
            this.maxEndTime = maxEndTime;
            return this;
        }

        public JobBuilder pinned(boolean pinned) {
            this.pinned = pinned;
            return this;
        }

        /**
         * Sets the cleaning and production start of an already scheduled job; its end time follows from the
         * production start and the job's duration, exactly as the solver would derive it.
         */
        public JobBuilder producedFrom(OffsetDateTime startCleaningDateTime, OffsetDateTime startProductionDateTime) {
            this.startCleaningDateTime = startCleaningDateTime;
            this.startProductionDateTime = startProductionDateTime;
            return this;
        }

        public Job build() {
            Job job = new Job(id, name, product, duration, minStartTime, idealEndTime, maxEndTime, pinned);
            job.setStartCleaningDateTime(startCleaningDateTime);
            job.setStartProductionDateTime(startProductionDateTime);
            job.setEndDateTime(startProductionDateTime == null ? null : startProductionDateTime.plus(duration));
            return job;
        }
    }

    /**
     * @return the given products, each with a cleaning duration for every one of them: switching to a
     *         different product takes {@code cleaningDuration}, producing the same product again takes none
     */
    public static List<Product> productsWithCleaningMatrix(Duration cleaningDuration, String... ids) {
        List<Product> products = Arrays.stream(ids)
                .map(id -> aProduct(id).build())
                .toList();
        for (Product product : products) {
            for (Product previousProduct : products) {
                product.getCleaningDurations().put(previousProduct,
                        product.equals(previousProduct) ? Duration.ZERO : cleaningDuration);
            }
        }
        return products;
    }
}
