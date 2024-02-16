package org.acme.foodpackaging.bootstrap;

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
import java.util.Map;
import java.util.Random;
import java.util.Set;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.foodpackaging.domain.Job;
import org.acme.foodpackaging.domain.Line;
import org.acme.foodpackaging.domain.PackagingSchedule;
import org.acme.foodpackaging.domain.Product;
import org.acme.foodpackaging.domain.WorkCalendar;
import org.acme.foodpackaging.persistence.PackagingScheduleRepository;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class DemoDataGenerator {

    @Inject
    PackagingScheduleRepository repository;

    @ConfigProperty(name = "demo-data.line-count", defaultValue = "5")
    int lineCount;
    @ConfigProperty(name = "demo-data.job-count", defaultValue = "100")
    int jobCount;

    @Transactional
    public void generateDemoData(@Observes StartupEvent startupEvent) {
        int noCleaningMinutes = 10;
        int cleaningMinutesMinimum = 30;
        int cleaningMinutesMaximum = 60;
        int jobDurationMinutesMinimum = 120;
        int jobDurationMinutesMaximum = 300;
        int averageCleaningAndJobDurationMinutes =
                (2 * noCleaningMinutes + cleaningMinutesMinimum + cleaningMinutesMaximum) / 4
                + (jobDurationMinutesMinimum + jobDurationMinutesMaximum) / 2;

        final LocalDate START_DATE = LocalDate.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));
        final LocalDateTime START_DATE_TIME = LocalDateTime.of(START_DATE, LocalTime.MIDNIGHT);
        final LocalDate END_DATE = START_DATE.plusWeeks(2);
        final LocalDateTime END_DATE_TIME = LocalDateTime.of(END_DATE, LocalTime.MIDNIGHT);

        Random random = new Random(37);
        PackagingSchedule solution = new PackagingSchedule();

        solution.setWorkCalendar(new WorkCalendar(START_DATE, END_DATE));

        Map<Product, Set<String>> ingredientMap = new HashMap<>(INGREDIENT_LIST.size() * PRODUCT_VARIATION_LIST.size() * 3);
        long productId = 0;
        for (int i = 0; i < INGREDIENT_LIST.size(); i++) {
            String ingredient = INGREDIENT_LIST.get(i);
            int r = random.nextInt(INGREDIENT_LIST.size() - 4);
            String ingredientA = INGREDIENT_LIST.get((i + r + 1) % INGREDIENT_LIST.size());
            String ingredientB = INGREDIENT_LIST.get((i + r + 2) % INGREDIENT_LIST.size());
            String ingredientC = INGREDIENT_LIST.get((i + r + 3) % INGREDIENT_LIST.size());
            for (String productVariation : PRODUCT_VARIATION_LIST) {
                ingredientMap.put(new Product(Long.toString(productId++), ingredient + " " + productVariation), Set.of(ingredient));
            }
            ingredientMap.put(new Product(Long.toString(productId++), ingredient + " and " + ingredientA + " " + PRODUCT_VARIATION_LIST.get(1)), Set.of(ingredient, ingredientA));
            ingredientMap.put(new Product(Long.toString(productId++), ingredient + " and " + ingredientB + " " + PRODUCT_VARIATION_LIST.get(2)), Set.of(ingredient, ingredientB));
            ingredientMap.put(new Product(Long.toString(productId++), ingredient + ", " + ingredientA + " and " + ingredientC + " " + PRODUCT_VARIATION_LIST.get(1)), Set.of(ingredient, ingredientA, ingredientC));
        }
        List<Product> products = new ArrayList<>(ingredientMap.keySet());
        for (Product product : products) {
            Map<Product, Duration> cleaningDurationMap = new HashMap<>(products.size());
            Set<String> ingredients = ingredientMap.get(product);
            for (Product previousProduct : products) {
                boolean noCleaning = ingredients.containsAll(ingredientMap.get(previousProduct));
                Duration cleaningDuration = Duration.ofMinutes(product == previousProduct ? 0
                        : noCleaning ? noCleaningMinutes
                        : cleaningMinutesMinimum + random.nextInt(cleaningMinutesMaximum - cleaningMinutesMinimum));
                cleaningDurationMap.put(previousProduct, cleaningDuration);
            }
            product.setCleaningDurations(cleaningDurationMap);
        }
        solution.setProducts(products);

        List<Line> lines = new ArrayList<>(lineCount);
        for (int i = 0; i < lineCount; i++) {
            String name = "Line " + (i + 1);
            String operator = "Operator " + ((char) ('A' + (i / 2)));
            lines.add(new Line(Integer.toString(i), name, operator, START_DATE_TIME));
        }
        solution.setLines(lines);

        List<Job> jobs = new ArrayList<>(jobCount);
        for (int i = 0; i < jobCount; i++) {
            Product product = products.get(random.nextInt(products.size()));
            String name = product.getName();
            Duration duration = Duration.ofMinutes(jobDurationMinutesMinimum
                    + random.nextInt(jobDurationMinutesMaximum - jobDurationMinutesMinimum));
            int targetDayIndex = (i / lineCount) * averageCleaningAndJobDurationMinutes / (24 * 60);
            LocalDateTime readyDateTime = START_DATE.plusDays(random.nextInt(Math.max(1, targetDayIndex - 2))).atTime(LocalTime.MIDNIGHT);
            LocalDateTime idealEndDateTime = START_DATE.plusDays(targetDayIndex + random.nextInt(3)).atTime(16, 0);
            LocalDateTime dueDateTime = idealEndDateTime.plusDays(1 + random.nextInt(3));
            jobs.add(new Job(Integer.toString(i), name, product, duration, readyDateTime, idealEndDateTime, dueDateTime, 1, false));
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
