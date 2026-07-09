package org.acme.taskassigning.demo;

import java.util.List;

import org.acme.taskassigning.dto.CustomerAffinityDTO;
import org.acme.taskassigning.dto.CustomerDTO;
import org.acme.taskassigning.dto.EmployeeDTO;
import org.acme.taskassigning.dto.TaskAssigningInput;
import org.acme.taskassigning.dto.TaskDTO;
import org.acme.taskassigning.dto.TaskTypeDTO;

/**
 * Builds a deterministic demo task assigning dataset: a fixed set of task types, customers, employees and tasks.
 */
public final class DemoDataBuilder {

    private static final String PROBLEM_SOLVING_SKILL = "Problem Solving";
    private static final String TEAM_BUILDING_SKILL = "Team Building";
    private static final String BUSINESS_STORYTELLING_SKILL = "Business Storytelling";
    private static final String RISK_MANAGEMENT_SKILL = "Risk Management";
    private static final String CREATIVE_THINKING_SKILL = "Creative Thinking";
    private static final String STRATEGIC_PLANNING_SKILL = "Strategic Planning";

    private static final String IMPROVE_SALES = "IS";
    private static final String EXPAND_TAX = "ET";
    private static final String SHRINK_VAT = "SV";
    private static final String APPROVE_LEGAL = "AL";

    private static final String STEEL = "1";
    private static final String PAPER = "2";
    private static final String STONE = "3";
    private static final String WOOD = "4";

    private static final String NONE = "NONE";
    private static final String LOW = "LOW";
    private static final String MEDIUM = "MEDIUM";
    private static final String HIGH = "HIGH";

    private static final String MINOR = "MINOR";
    private static final String MAJOR = "MAJOR";
    private static final String CRITICAL = "CRITICAL";

    private DemoDataBuilder() {
    }

    public static DemoDataBuilder builder() {
        return new DemoDataBuilder();
    }

    public TaskAssigningInput build() {
        return new TaskAssigningInput(buildTaskTypes(), buildCustomers(), buildEmployees(), buildTasks());
    }

    private List<TaskTypeDTO> buildTaskTypes() {
        return List.of(
                new TaskTypeDTO(IMPROVE_SALES, "Improve Sales", 46, List.of(STRATEGIC_PLANNING_SKILL)),
                new TaskTypeDTO(EXPAND_TAX, "Expand Tax", 63, List.of(PROBLEM_SOLVING_SKILL, TEAM_BUILDING_SKILL)),
                new TaskTypeDTO(SHRINK_VAT, "Shrink VAT", 63, List.of(STRATEGIC_PLANNING_SKILL)),
                new TaskTypeDTO(APPROVE_LEGAL, "Approve Legal", 40, List.of(RISK_MANAGEMENT_SKILL)));
    }

    private List<CustomerDTO> buildCustomers() {
        return List.of(
                new CustomerDTO(STEEL, "Steel Inc"),
                new CustomerDTO(PAPER, "Paper Corp"),
                new CustomerDTO(STONE, "Stone Limited"),
                new CustomerDTO(WOOD, "Wood Express"));
    }

    private List<EmployeeDTO> buildEmployees() {
        return List.of(
                employee("1", "Amy", List.of(PROBLEM_SOLVING_SKILL, BUSINESS_STORYTELLING_SKILL, TEAM_BUILDING_SKILL),
                        HIGH, MEDIUM, HIGH, MEDIUM),
                employee("2", "Beth", List.of(RISK_MANAGEMENT_SKILL, CREATIVE_THINKING_SKILL),
                        LOW, HIGH, LOW, MEDIUM),
                employee("3", "Carl", List.of(STRATEGIC_PLANNING_SKILL, PROBLEM_SOLVING_SKILL),
                        MEDIUM, HIGH, MEDIUM, LOW),
                employee("4", "Dan", List.of(BUSINESS_STORYTELLING_SKILL, TEAM_BUILDING_SKILL),
                        LOW, HIGH, HIGH, MEDIUM),
                employee("5", "Elsa", List.of(RISK_MANAGEMENT_SKILL, STRATEGIC_PLANNING_SKILL, CREATIVE_THINKING_SKILL),
                        MEDIUM, HIGH, HIGH, MEDIUM),
                employee("6", "Flo", List.of(PROBLEM_SOLVING_SKILL, TEAM_BUILDING_SKILL),
                        HIGH, HIGH, NONE, MEDIUM),
                employee("7", "Gus", List.of(RISK_MANAGEMENT_SKILL, CREATIVE_THINKING_SKILL, BUSINESS_STORYTELLING_SKILL),
                        LOW, MEDIUM, MEDIUM, LOW),
                employee("8", "Hugo", List.of(STRATEGIC_PLANNING_SKILL, PROBLEM_SOLVING_SKILL, TEAM_BUILDING_SKILL),
                        NONE, MEDIUM, NONE, LOW));
    }

    private EmployeeDTO employee(String id, String fullName, List<String> skills,
            String steelAffinity, String paperAffinity, String stoneAffinity, String woodAffinity) {
        List<CustomerAffinityDTO> affinities = List.of(
                new CustomerAffinityDTO(STEEL, steelAffinity),
                new CustomerAffinityDTO(PAPER, paperAffinity),
                new CustomerAffinityDTO(STONE, stoneAffinity),
                new CustomerAffinityDTO(WOOD, woodAffinity));
        return new EmployeeDTO(id, fullName, skills, affinities, List.of());
    }

    private List<TaskDTO> buildTasks() {
        Long startTime = null;
        return List.of(
                new TaskDTO("1", SHRINK_VAT, 1, PAPER, 0L, MINOR, startTime),
                new TaskDTO("2", APPROVE_LEGAL, 1, WOOD, 0L, MAJOR, startTime),
                new TaskDTO("3", SHRINK_VAT, 2, WOOD, 0L, MAJOR, startTime),
                new TaskDTO("4", IMPROVE_SALES, 1, WOOD, 0L, CRITICAL, startTime),
                new TaskDTO("5", APPROVE_LEGAL, 2, STONE, 0L, MINOR, startTime),
                new TaskDTO("6", APPROVE_LEGAL, 3, STONE, 0L, MINOR, startTime),
                new TaskDTO("7", EXPAND_TAX, 1, PAPER, 0L, MINOR, startTime),
                new TaskDTO("8", EXPAND_TAX, 2, STEEL, 0L, MINOR, startTime),
                new TaskDTO("9", EXPAND_TAX, 3, STEEL, 0L, MINOR, startTime),
                new TaskDTO("10", SHRINK_VAT, 3, WOOD, 0L, MAJOR, startTime),
                new TaskDTO("11", IMPROVE_SALES, 2, WOOD, 0L, MINOR, startTime),
                new TaskDTO("12", EXPAND_TAX, 4, STEEL, 0L, MAJOR, startTime),
                new TaskDTO("13", IMPROVE_SALES, 3, STONE, 0L, MINOR, startTime),
                new TaskDTO("14", EXPAND_TAX, 5, WOOD, 0L, MAJOR, startTime),
                new TaskDTO("15", EXPAND_TAX, 6, STONE, 0L, CRITICAL, startTime),
                new TaskDTO("16", EXPAND_TAX, 7, PAPER, 0L, MINOR, startTime),
                new TaskDTO("17", APPROVE_LEGAL, 4, STONE, 0L, MAJOR, startTime),
                new TaskDTO("18", APPROVE_LEGAL, 5, STEEL, 0L, MAJOR, startTime),
                new TaskDTO("19", IMPROVE_SALES, 4, WOOD, 0L, MAJOR, startTime),
                new TaskDTO("20", IMPROVE_SALES, 5, WOOD, 0L, CRITICAL, startTime),
                new TaskDTO("21", IMPROVE_SALES, 6, STEEL, 0L, MINOR, startTime),
                new TaskDTO("22", IMPROVE_SALES, 7, PAPER, 0L, MAJOR, startTime),
                new TaskDTO("23", IMPROVE_SALES, 8, WOOD, 0L, CRITICAL, startTime),
                new TaskDTO("24", APPROVE_LEGAL, 6, WOOD, 0L, MINOR, startTime),
                new TaskDTO("25", APPROVE_LEGAL, 7, STEEL, 0L, CRITICAL, startTime),
                new TaskDTO("26", IMPROVE_SALES, 9, WOOD, 0L, MAJOR, startTime),
                new TaskDTO("27", IMPROVE_SALES, 10, STEEL, 0L, CRITICAL, startTime),
                new TaskDTO("28", IMPROVE_SALES, 11, WOOD, 0L, MAJOR, startTime));
    }
}
