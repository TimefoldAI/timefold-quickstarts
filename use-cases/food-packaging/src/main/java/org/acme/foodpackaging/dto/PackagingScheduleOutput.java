package org.acme.foodpackaging.dto;

import java.util.List;

import ai.timefold.solver.service.definition.api.ModelOutput;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "The food packaging planning problem output.")
public record PackagingScheduleOutput(
        @Schema(description = "The planning horizon during which jobs may be scheduled.") WorkCalendarDTO workCalendar,
        @Schema(description = "Products that can be packaged.") List<ProductDTO> products,
        @Schema(description = "Operators that can staff the lines.") List<OperatorDTO> operators,
        @Schema(description = "Packaging lines with their assigned operator and ordered jobs.") List<LineDTO> lines,
        @Schema(description = "Jobs with their assigned line and computed schedule.") List<JobDTO> jobs,
        @Schema(description = "The score of the solution.") String score) implements ModelOutput {

    public PackagingScheduleOutput {
        products = List.copyOf(products);
        operators = List.copyOf(operators);
        lines = List.copyOf(lines);
        jobs = List.copyOf(jobs);
    }

    public PackagingScheduleOutput withWorkCalendar(WorkCalendarDTO workCalendar) {
        return new PackagingScheduleOutput(workCalendar, products, operators, lines, jobs, score);
    }

    public PackagingScheduleOutput withProducts(List<ProductDTO> products) {
        return new PackagingScheduleOutput(workCalendar, products, operators, lines, jobs, score);
    }

    public PackagingScheduleOutput withOperators(List<OperatorDTO> operators) {
        return new PackagingScheduleOutput(workCalendar, products, operators, lines, jobs, score);
    }

    public PackagingScheduleOutput withLines(List<LineDTO> lines) {
        return new PackagingScheduleOutput(workCalendar, products, operators, lines, jobs, score);
    }

    public PackagingScheduleOutput withJobs(List<JobDTO> jobs) {
        return new PackagingScheduleOutput(workCalendar, products, operators, lines, jobs, score);
    }

    public PackagingScheduleOutput withScore(String score) {
        return new PackagingScheduleOutput(workCalendar, products, operators, lines, jobs, score);
    }
}
