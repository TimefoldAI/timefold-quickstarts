package org.acme.foodpackaging.solver;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.acme.foodpackaging.dto.JobDTO;
import org.acme.foodpackaging.dto.LineDTO;
import org.acme.foodpackaging.dto.OperatorDTO;
import org.acme.foodpackaging.dto.PackagingScheduleInput;
import org.acme.foodpackaging.dto.ProductDTO;
import org.acme.foodpackaging.dto.WorkCalendarDTO;

final class SolverTestDataFactory {

    private static final LocalDate START_DATE = LocalDate.of(2024, 1, 1);
    private static final LocalDateTime START_DATE_TIME = LocalDateTime.of(START_DATE, LocalTime.MIDNIGHT);
    private static final String UNASSIGNED = "";

    private SolverTestDataFactory() {
    }

    static PackagingScheduleInput createProblem() {
        int productCount = 2;
        int lineCount = 2;
        int jobCount = 4;

        List<ProductDTO> products = new ArrayList<>(productCount);
        for (int p = 0; p < productCount; p++) {
            products.add(new ProductDTO(Integer.toString(p), "Product " + p, Map.of()));
        }
        List<ProductDTO> productsWithCleaning = new ArrayList<>(productCount);
        for (ProductDTO product : products) {
            Map<String, Long> cleaningDurations = new HashMap<>();
            for (ProductDTO previous : products) {
                cleaningDurations.put(previous.id(), product.id().equals(previous.id()) ? 0L : 10L);
            }
            productsWithCleaning.add(product.withCleaningDurations(cleaningDurations));
        }

        List<OperatorDTO> operators = new ArrayList<>(lineCount);
        List<LineDTO> lines = new ArrayList<>(lineCount);
        for (int l = 0; l < lineCount; l++) {
            operators.add(new OperatorDTO("Operator " + l));
            lines.add(new LineDTO(Integer.toString(l), "Line " + l, START_DATE_TIME.toString(), UNASSIGNED, List.of()));
        }

        List<JobDTO> jobs = new ArrayList<>(jobCount);
        for (int i = 0; i < jobCount; i++) {
            ProductDTO product = productsWithCleaning.get(i % productCount);
            LocalDateTime idealEndTime = START_DATE_TIME.plusDays(14);
            LocalDateTime maxEndTime = START_DATE_TIME.plusDays(21);
            jobs.add(new JobDTO(Integer.toString(i), product.name(), product.id(), 60L,
                    START_DATE_TIME.toString(), idealEndTime.toString(), maxEndTime.toString(), 1, false,
                    UNASSIGNED, UNASSIGNED, UNASSIGNED, UNASSIGNED, UNASSIGNED));
        }

        WorkCalendarDTO workCalendar = new WorkCalendarDTO(START_DATE.toString(), START_DATE.plusWeeks(4).toString());
        return new PackagingScheduleInput(workCalendar, productsWithCleaning, operators, lines, jobs);
    }
}
