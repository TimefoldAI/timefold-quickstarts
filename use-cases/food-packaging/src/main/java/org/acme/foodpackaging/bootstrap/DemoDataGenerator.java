package org.acme.foodpackaging.bootstrap;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.transaction.Transactional;
import org.acme.foodpackaging.domain.Job;
import org.acme.foodpackaging.domain.Line;
import org.acme.foodpackaging.domain.Operator;
import org.acme.foodpackaging.domain.PackagingSchedule;
import org.acme.foodpackaging.domain.Product;
import org.acme.foodpackaging.domain.WorkCalendar;
import org.acme.foodpackaging.persistence.PackagingScheduleRepository;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;

@ApplicationScoped
public class DemoDataGenerator {

    private final PackagingScheduleRepository repository;

    @ConfigProperty(name = "demo-data.line-count", defaultValue = "5")
    int lineCount;
    @ConfigProperty(name = "demo-data.job-count", defaultValue = "100")
    int jobCount;

    public DemoDataGenerator(PackagingScheduleRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void generateDemoData(@Observes StartupEvent startupEvent) {
        var noCleaningMinutes = 10;
        var cleaningMinutesMinimum = 30;
        var cleaningMinutesMaximum = 60;
        var jobDurationMinutesMinimum = 120;
        var jobDurationMinutesMaximum = 300;
        var averageCleaningAndJobDurationMinutes =
                (2 * noCleaningMinutes + cleaningMinutesMinimum + cleaningMinutesMaximum) / 4
                        + (jobDurationMinutesMinimum + jobDurationMinutesMaximum) / 2;

        final var START_DATE = LocalDate.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));
        final var START_DATE_TIME = LocalDateTime.of(START_DATE, LocalTime.MIDNIGHT);
        final var END_DATE = START_DATE.plusWeeks(2);

        var random = new Random(37);
        var solution = new PackagingSchedule();

        solution.setWorkCalendar(new WorkCalendar(START_DATE, END_DATE));

        var ingredientMap = new HashMap<Product, Set<String>>(INGREDIENT_LIST.size() * PRODUCT_VARIATION_LIST.size() * 3);
        var productId = 0L;
        for (var i = 0; i < INGREDIENT_LIST.size(); i++) {
            var ingredient = INGREDIENT_LIST.get(i);
            var r = random.nextInt(INGREDIENT_LIST.size() - 4);
            var ingredientA = INGREDIENT_LIST.get((i + r + 1) % INGREDIENT_LIST.size());
            var ingredientB = INGREDIENT_LIST.get((i + r + 2) % INGREDIENT_LIST.size());
            var ingredientC = INGREDIENT_LIST.get((i + r + 3) % INGREDIENT_LIST.size());
            for (var productVariation : PRODUCT_VARIATION_LIST) {
                ingredientMap.put(new Product(Long.toString(productId++), ingredient + " " + productVariation), java.util.Set.of(ingredient));
            }
            ingredientMap.put(new Product(Long.toString(productId++), ingredient + " and " + ingredientA + " " + PRODUCT_VARIATION_LIST.get(1)), java.util.Set.of(ingredient, ingredientA));
            ingredientMap.put(new Product(Long.toString(productId++), ingredient + " and " + ingredientB + " " + PRODUCT_VARIATION_LIST.get(2)), java.util.Set.of(ingredient, ingredientB));
            ingredientMap.put(new Product(Long.toString(productId++), ingredient + ", " + ingredientA + " and " + ingredientC + " " + PRODUCT_VARIATION_LIST.get(1)), java.util.Set.of(ingredient, ingredientA, ingredientC));
        }
        var products = new ArrayList<>(ingredientMap.keySet().stream().sorted(Comparator.comparing(Product::getId)).toList());
        for (var product : products) {
            var cleaningDurationMap = new HashMap<Product, Duration>(products.size());
            var ingredients = ingredientMap.get(product);
            for (var previousProduct : products) {
                var noCleaning = ingredients.containsAll(ingredientMap.get(previousProduct));
                var cleaningDuration = Duration.ofMinutes(product == previousProduct ? 0
                        : noCleaning ? noCleaningMinutes
                        : cleaningMinutesMinimum + random.nextInt(cleaningMinutesMaximum - cleaningMinutesMinimum));
                cleaningDurationMap.put(previousProduct, cleaningDuration);
            }
            product.setCleaningDurations(cleaningDurationMap);
        }
        solution.setProducts(products);

        var lines = new ArrayList<Line>(lineCount);
        var operators = new ArrayList<Operator>(lineCount);
        for (var i = 0; i < lineCount; i++) {
            lines.add(new Line(Integer.toString(i), "Line " + (i + 1), START_DATE_TIME));
            operators.add(new Operator("Operator " + (i + 1)));
        }
        solution.setLines(lines);
        solution.setOperators(operators);

        var jobs = new ArrayList<Job>(jobCount);
        for (var i = 0; i < jobCount; i++) {
            Product product = products.get(random.nextInt(products.size()));
            String name = product.getName();
            Duration duration = Duration.ofMinutes(jobDurationMinutesMinimum
                    + random.nextLong((long) jobDurationMinutesMaximum - jobDurationMinutesMinimum));
            int targetDayIndex = (i / lineCount) * averageCleaningAndJobDurationMinutes / (24 * 60);
            LocalDateTime minStartTime = START_DATE.plusDays(random.nextInt(Math.max(1, targetDayIndex - 2))).atTime(LocalTime.MIDNIGHT);
            LocalDateTime idealEndTime = START_DATE.plusDays(targetDayIndex + random.nextLong(3)).atTime(16, 0);
            LocalDateTime maxEndTime = idealEndTime.plusDays(1 + random.nextLong(3));
            jobs.add(new Job(Integer.toString(i), name, product, duration, minStartTime, idealEndTime, maxEndTime, 1, false));
        }
        jobs.sort(Comparator.comparing(Job::getName));
        solution.setJobs(jobs);

        repository.write(solution);
    }

    private static final List<String> INGREDIENT_LIST = List.of(
            "Carrots",
            "Peas",
            "Cabbage",
            "Tomato",
            "Eggplant",
            "Broccoli",
            "Spinach",
            "Pumpkin",
            "Pepper",
            "Onions");
    private static final List<String> PRODUCT_VARIATION_LIST = List.of(
            "small bag",
            "medium bag",
            "large bag");

}
