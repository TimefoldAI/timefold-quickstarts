package org.acme.foodpackaging.dto;

import java.util.List;

import ai.timefold.solver.service.definition.api.ModelInput;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "The food packaging planning problem input.")
public record PackagingScheduleInput(
        @Schema(description = "The planning horizon during which jobs may be scheduled.") WorkCalendarDTO workCalendar,
        @Schema(description = "Products that can be packaged.") List<ProductDTO> products,
        @Schema(description = "Operators that can staff the lines.") List<OperatorDTO> operators,
        @Schema(description = "Packaging lines available to process jobs.") List<LineDTO> lines,
        @Schema(description = "Jobs that must each be assigned to a line.") List<JobDTO> jobs) implements ModelInput {

    public PackagingScheduleInput {
        products = List.copyOf(products);
        operators = List.copyOf(operators);
        lines = List.copyOf(lines);
        jobs = List.copyOf(jobs);
    }

    public PackagingScheduleInput withWorkCalendar(WorkCalendarDTO workCalendar) {
        return new PackagingScheduleInput(workCalendar, products, operators, lines, jobs);
    }

    public PackagingScheduleInput withProducts(List<ProductDTO> products) {
        return new PackagingScheduleInput(workCalendar, products, operators, lines, jobs);
    }

    public PackagingScheduleInput withOperators(List<OperatorDTO> operators) {
        return new PackagingScheduleInput(workCalendar, products, operators, lines, jobs);
    }

    public PackagingScheduleInput withLines(List<LineDTO> lines) {
        return new PackagingScheduleInput(workCalendar, products, operators, lines, jobs);
    }

    public PackagingScheduleInput withJobs(List<JobDTO> jobs) {
        return new PackagingScheduleInput(workCalendar, products, operators, lines, jobs);
    }
}
