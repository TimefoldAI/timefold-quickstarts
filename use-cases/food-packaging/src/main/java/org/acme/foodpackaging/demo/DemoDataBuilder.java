package org.acme.foodpackaging.demo;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.acme.foodpackaging.dto.input.CleaningDurationDTO;
import org.acme.foodpackaging.dto.input.JobDTO;
import org.acme.foodpackaging.dto.input.LineDTO;
import org.acme.foodpackaging.dto.input.OperatorDTO;
import org.acme.foodpackaging.dto.input.PackagingScheduleInput;
import org.acme.foodpackaging.dto.input.ProductDTO;
import org.acme.foodpackaging.dto.input.WorkCalendarDTO;

/**
 * Builds a demo dataset of vegetable bags to be packaged on a handful of production lines.
 * <p>
 * The randomness is seeded, so the same dataset comes out every time (apart from the schedule window,
 * which is anchored to the upcoming Monday). Products that share ingredients need only a short
 * changeover cleaning, so the interesting part of the schedule is the order the jobs end up in.
 */
public final class DemoDataBuilder {

    private static final int LINE_COUNT = 5;
    private static final int JOB_COUNT = 100;

    private static final int NO_CLEANING_MINUTES = 10;
    private static final int CLEANING_MINUTES_MINIMUM = 30;
    private static final int CLEANING_MINUTES_MAXIMUM = 60;
    private static final int JOB_DURATION_MINUTES_MINIMUM = 120;
    private static final int JOB_DURATION_MINUTES_MAXIMUM = 300;

    private static final List<String> INGREDIENTS = List.of(
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
    private static final List<String> PRODUCT_VARIATIONS = List.of(
            "small bag",
            "medium bag",
            "large bag");

    private DemoDataBuilder() {
    }

    public static DemoDataBuilder builder() {
        return new DemoDataBuilder();
    }

    public PackagingScheduleInput build() {
        LocalDate startDate = LocalDate.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));
        OffsetDateTime startDateTime = at(startDate, LocalTime.MIDNIGHT);
        Random random = new Random(37);

        Map<String, Set<String>> ingredientsByProductName = buildIngredientsByProductName(random);
        List<ProductDTO> products = buildProducts(ingredientsByProductName, random);
        return new PackagingScheduleInput(
                new WorkCalendarDTO(startDate, startDate.plusWeeks(2)),
                products,
                buildOperators(),
                buildLines(startDateTime),
                buildJobs(products, startDate, random));
    }

    /**
     * @return the ingredients of every product, keyed by the product name, in the order the products are created
     */
    private static Map<String, Set<String>> buildIngredientsByProductName(Random random) {
        Map<String, Set<String>> ingredientsByProductName = new LinkedHashMap<>();
        for (int i = 0; i < INGREDIENTS.size(); i++) {
            String ingredient = INGREDIENTS.get(i);
            int offset = random.nextInt(INGREDIENTS.size() - 4);
            String ingredientA = INGREDIENTS.get((i + offset + 1) % INGREDIENTS.size());
            String ingredientB = INGREDIENTS.get((i + offset + 2) % INGREDIENTS.size());
            String ingredientC = INGREDIENTS.get((i + offset + 3) % INGREDIENTS.size());
            for (String productVariation : PRODUCT_VARIATIONS) {
                ingredientsByProductName.put(ingredient + " " + productVariation, Set.of(ingredient));
            }
            ingredientsByProductName.put(ingredient + " and " + ingredientA + " " + PRODUCT_VARIATIONS.get(1),
                    Set.of(ingredient, ingredientA));
            ingredientsByProductName.put(ingredient + " and " + ingredientB + " " + PRODUCT_VARIATIONS.get(2),
                    Set.of(ingredient, ingredientB));
            ingredientsByProductName.put(
                    ingredient + ", " + ingredientA + " and " + ingredientC + " " + PRODUCT_VARIATIONS.get(1),
                    Set.of(ingredient, ingredientA, ingredientC));
        }
        return ingredientsByProductName;
    }

    /**
     * Switching a line to a product that already contains every ingredient of the previous product needs
     * only a rinse; switching to anything else needs a full cleaning.
     */
    private static List<ProductDTO> buildProducts(Map<String, Set<String>> ingredientsByProductName, Random random) {
        List<String> productNames = List.copyOf(ingredientsByProductName.keySet());
        Map<String, String> productIdByName = new LinkedHashMap<>();
        for (int i = 0; i < productNames.size(); i++) {
            productIdByName.put(productNames.get(i), Integer.toString(i));
        }

        List<ProductDTO> products = new ArrayList<>(productNames.size());
        for (String productName : productNames) {
            Set<String> ingredients = ingredientsByProductName.get(productName);
            List<CleaningDurationDTO> cleaningDurations = new ArrayList<>(productNames.size());
            for (String previousProductName : productNames) {
                long cleaningMinutes;
                if (productName.equals(previousProductName)) {
                    cleaningMinutes = 0;
                } else if (ingredients.containsAll(ingredientsByProductName.get(previousProductName))) {
                    cleaningMinutes = NO_CLEANING_MINUTES;
                } else {
                    cleaningMinutes = CLEANING_MINUTES_MINIMUM
                            + random.nextInt(CLEANING_MINUTES_MAXIMUM - CLEANING_MINUTES_MINIMUM);
                }
                cleaningDurations.add(new CleaningDurationDTO(productIdByName.get(previousProductName), cleaningMinutes));
            }
            products.add(new ProductDTO(productIdByName.get(productName), productName, cleaningDurations));
        }
        return products;
    }

    private static List<OperatorDTO> buildOperators() {
        List<OperatorDTO> operators = new ArrayList<>(LINE_COUNT);
        for (int i = 0; i < LINE_COUNT; i++) {
            operators.add(new OperatorDTO(Integer.toString(i), "Operator " + (i + 1)));
        }
        return operators;
    }

    private static List<LineDTO> buildLines(OffsetDateTime startDateTime) {
        List<LineDTO> lines = new ArrayList<>(LINE_COUNT);
        for (int i = 0; i < LINE_COUNT; i++) {
            lines.add(new LineDTO(Integer.toString(i), "Line " + (i + 1), startDateTime, null, List.of()));
        }
        return lines;
    }

    private static List<JobDTO> buildJobs(List<ProductDTO> products, LocalDate startDate, Random random) {
        // The jobs are spread over the schedule window by giving every batch of LINE_COUNT jobs an ideal end
        // time one average job (plus its cleaning) later than the previous batch.
        int averageCleaningAndJobDurationMinutes =
                (2 * NO_CLEANING_MINUTES + CLEANING_MINUTES_MINIMUM + CLEANING_MINUTES_MAXIMUM) / 4
                        + (JOB_DURATION_MINUTES_MINIMUM + JOB_DURATION_MINUTES_MAXIMUM) / 2;

        List<JobDTO> jobs = new ArrayList<>(JOB_COUNT);
        for (int i = 0; i < JOB_COUNT; i++) {
            ProductDTO product = products.get(random.nextInt(products.size()));
            long durationMinutes = JOB_DURATION_MINUTES_MINIMUM
                    + random.nextLong((long) JOB_DURATION_MINUTES_MAXIMUM - JOB_DURATION_MINUTES_MINIMUM);
            int targetDayIndex = (i / LINE_COUNT) * averageCleaningAndJobDurationMinutes / (24 * 60);
            OffsetDateTime minStartTime =
                    at(startDate.plusDays(random.nextInt(Math.max(1, targetDayIndex - 2))), LocalTime.MIDNIGHT);
            OffsetDateTime idealEndTime =
                    at(startDate.plusDays(targetDayIndex + random.nextLong(3)), LocalTime.of(16, 0));
            OffsetDateTime maxEndTime = idealEndTime.plusDays(1 + random.nextLong(3));
            jobs.add(new JobDTO(Integer.toString(i), product.name(), product.id(), durationMinutes, minStartTime,
                    idealEndTime, maxEndTime, false));
        }
        jobs.sort(Comparator.comparing(JobDTO::name));
        return jobs;
    }

    /**
     * The whole model works in UTC, so that is the only offset the demo data ever needs.
     */
    private static OffsetDateTime at(LocalDate date, LocalTime time) {
        return OffsetDateTime.of(date, time, ZoneOffset.UTC);
    }
}
