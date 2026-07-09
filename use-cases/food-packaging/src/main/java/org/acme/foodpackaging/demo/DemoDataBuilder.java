package org.acme.foodpackaging.demo;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.acme.foodpackaging.dto.JobDTO;
import org.acme.foodpackaging.dto.LineDTO;
import org.acme.foodpackaging.dto.OperatorDTO;
import org.acme.foodpackaging.dto.PackagingScheduleInput;
import org.acme.foodpackaging.dto.ProductDTO;
import org.acme.foodpackaging.dto.WorkCalendarDTO;

public final class DemoDataBuilder {

    private static final List<String> INGREDIENT_LIST = List.of(
            "Carrots", "Peas", "Cabbage", "Tomato", "Eggplant",
            "Broccoli", "Spinach", "Pumpkin", "Pepper", "Onions");
    private static final List<String> PRODUCT_VARIATION_LIST = List.of("small bag", "medium bag", "large bag");

    private static final int NO_CLEANING_MINUTES = 10;
    private static final int CLEANING_MINUTES_MINIMUM = 30;
    private static final int CLEANING_MINUTES_MAXIMUM = 60;
    private static final int JOB_DURATION_MINUTES_MINIMUM = 120;
    private static final int JOB_DURATION_MINUTES_MAXIMUM = 300;
    private static final int MINIMUM_COUNT = 1;

    private int lineCount = 5;
    private int jobCount = 30;
    private int weekCount = 2;

    private DemoDataBuilder() {
    }

    public static DemoDataBuilder builder() {
        return new DemoDataBuilder();
    }

    public DemoDataBuilder setLineCount(int lineCount) {
        this.lineCount = lineCount;
        return this;
    }

    public DemoDataBuilder setJobCount(int jobCount) {
        this.jobCount = jobCount;
        return this;
    }

    public DemoDataBuilder setWeekCount(int weekCount) {
        this.weekCount = weekCount;
        return this;
    }

    public PackagingScheduleInput build() {
        if (lineCount < MINIMUM_COUNT) {
            throw new IllegalStateException("Number of lines (" + lineCount + ") must be greater than zero.");
        }
        if (jobCount < MINIMUM_COUNT) {
            throw new IllegalStateException("Number of jobs (" + jobCount + ") must be greater than zero.");
        }

        Random random = new Random(37);
        LocalDate startDate =
                LocalDate.now(ZoneId.systemDefault()).with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));
        LocalDateTime startDateTime = LocalDateTime.of(startDate, LocalTime.MIDNIGHT);
        LocalDate endDate = startDate.plusWeeks(weekCount);

        int averageCleaningAndJobDurationMinutes =
                (2 * NO_CLEANING_MINUTES + CLEANING_MINUTES_MINIMUM + CLEANING_MINUTES_MAXIMUM) / 4
                        + (JOB_DURATION_MINUTES_MINIMUM + JOB_DURATION_MINUTES_MAXIMUM) / 2;

        List<ProductDTO> products = buildProducts(random);
        List<OperatorDTO> operators = new ArrayList<>(lineCount);
        List<LineDTO> lines = new ArrayList<>(lineCount);
        for (int i = 0; i < lineCount; i++) {
            operators.add(new OperatorDTO("Operator " + (i + 1)));
            lines.add(new LineDTO(Integer.toString(i), "Line " + (i + 1), startDateTime.toString(), "", List.of()));
        }

        List<JobDTO> jobs = buildJobs(random, products, startDate, averageCleaningAndJobDurationMinutes);

        WorkCalendarDTO workCalendar = new WorkCalendarDTO(startDate.toString(), endDate.toString());
        return new PackagingScheduleInput(workCalendar, products, operators, lines, jobs);
    }

    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private List<ProductDTO> buildProducts(Random random) {
        Map<String, String> idToName = new LinkedHashMap<>();
        Map<String, Set<String>> idToIngredients = new LinkedHashMap<>();
        long productId = 0L;
        for (int i = 0; i < INGREDIENT_LIST.size(); i++) {
            String ingredient = INGREDIENT_LIST.get(i);
            int r = random.nextInt(INGREDIENT_LIST.size() - 4);
            String ingredientA = INGREDIENT_LIST.get((i + r + 1) % INGREDIENT_LIST.size());
            for (String productVariation : PRODUCT_VARIATION_LIST) {
                String id = Long.toString(productId);
                productId += 1;
                idToName.put(id, ingredient + " " + productVariation);
                idToIngredients.put(id, Set.of(ingredient));
            }
            String idA = Long.toString(productId);
            productId += 1;
            idToName.put(idA, ingredient + " and " + ingredientA + " " + PRODUCT_VARIATION_LIST.get(1));
            idToIngredients.put(idA, Set.of(ingredient, ingredientA));
            String idB = Long.toString(productId);
            productId += 1;
            String ingredientB = INGREDIENT_LIST.get((i + r + 2) % INGREDIENT_LIST.size());
            idToName.put(idB, ingredient + " and " + ingredientB + " " + PRODUCT_VARIATION_LIST.get(2));
            idToIngredients.put(idB, Set.of(ingredient, ingredientB));
            String idC = Long.toString(productId);
            productId += 1;
            String ingredientC = INGREDIENT_LIST.get((i + r + 3) % INGREDIENT_LIST.size());
            idToName.put(idC, ingredient + ", " + ingredientA + " and " + ingredientC + " "
                    + PRODUCT_VARIATION_LIST.get(1));
            idToIngredients.put(idC, Set.of(ingredient, ingredientA, ingredientC));
        }

        List<String> productIds = new ArrayList<>(idToName.keySet());
        productIds.sort(Comparator.comparingLong(Long::parseLong));
        List<ProductDTO> products = new ArrayList<>(productIds.size());
        for (String id : productIds) {
            Map<String, Long> cleaningDurations = new HashMap<>(productIds.size());
            Set<String> ingredients = idToIngredients.get(id);
            for (String previousId : productIds) {
                boolean noCleaning = ingredients.containsAll(idToIngredients.get(previousId));
                long cleaningMinutes = id.equals(previousId) ? 0L
                        : noCleaning ? NO_CLEANING_MINUTES
                                : CLEANING_MINUTES_MINIMUM
                                        + random.nextInt(CLEANING_MINUTES_MAXIMUM - CLEANING_MINUTES_MINIMUM);
                cleaningDurations.put(previousId, cleaningMinutes);
            }
            products.add(new ProductDTO(id, idToName.get(id), cleaningDurations));
        }
        return products;
    }

    private List<JobDTO> buildJobs(Random random, List<ProductDTO> products, LocalDate startDate,
            int averageCleaningAndJobDurationMinutes) {
        List<JobDTO> jobs = new ArrayList<>(jobCount);
        for (int i = 0; i < jobCount; i++) {
            ProductDTO product = products.get(random.nextInt(products.size()));
            long durationMinutes = JOB_DURATION_MINUTES_MINIMUM
                    + random.nextLong((long) JOB_DURATION_MINUTES_MAXIMUM - JOB_DURATION_MINUTES_MINIMUM);
            int targetDayIndex = i / lineCount * averageCleaningAndJobDurationMinutes / (24 * 60);
            LocalDateTime minStartTime =
                    startDate.plusDays(random.nextInt(Math.max(1, targetDayIndex - 2))).atTime(LocalTime.MIDNIGHT);
            LocalDateTime idealEndTime = startDate.plusDays(targetDayIndex + random.nextLong(3)).atTime(16, 0);
            LocalDateTime maxEndTime = idealEndTime.plusDays(1 + random.nextLong(3));
            jobs.add(new JobDTO(Integer.toString(i), product.name(), product.id(), durationMinutes,
                    minStartTime.toString(), idealEndTime.toString(), maxEndTime.toString(), 1, false,
                    "", "", "", "", ""));
        }
        jobs.sort(Comparator.comparing(JobDTO::name));
        return jobs;
    }
}
