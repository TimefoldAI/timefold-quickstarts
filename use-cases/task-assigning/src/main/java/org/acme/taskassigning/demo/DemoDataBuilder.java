package org.acme.taskassigning.demo;

import static org.acme.taskassigning.domain.Affinity.HIGH;
import static org.acme.taskassigning.domain.Affinity.LOW;
import static org.acme.taskassigning.domain.Affinity.MEDIUM;
import static org.acme.taskassigning.domain.Affinity.NONE;
import static org.acme.taskassigning.domain.Priority.CRITICAL;
import static org.acme.taskassigning.domain.Priority.MAJOR;
import static org.acme.taskassigning.domain.Priority.MINOR;

import java.util.List;
import java.util.Map;

import org.acme.taskassigning.dto.input.CustomerInputDTO;
import org.acme.taskassigning.dto.input.EmployeeInputDTO;
import org.acme.taskassigning.dto.input.TaskAssigningInput;
import org.acme.taskassigning.dto.input.TaskInputDTO;
import org.acme.taskassigning.dto.input.TaskTypeInputDTO;

/**
 * Builds a fixed demo dataset: 28 tasks of 4 kinds, spread over 4 customers, that 8 employees with varying skills and
 * customer affinities can be assigned to. Every task starts out unassigned.
 */
public final class DemoDataBuilder {

    private static final String PROBLEM_SOLVING_SKILL = "Problem Solving";
    private static final String TEAM_BUILDING_SKILL = "Team Building";
    private static final String BUSINESS_STORYTELLING_SKILL = "Business Storytelling";
    private static final String RISK_MANAGEMENT_SKILL = "Risk Management";
    private static final String CREATIVE_THINKING_SKILL = "Creative Thinking";
    private static final String STRATEGIC_PLANNING_SKILL = "Strategic Planning";

    private static final TaskTypeInputDTO IMPROVE_SALES_TASK_TYPE =
            new TaskTypeInputDTO("IS", "Improve Sales", 46, List.of(STRATEGIC_PLANNING_SKILL));
    private static final TaskTypeInputDTO EXPAND_TAX_TASK_TYPE =
            new TaskTypeInputDTO("ET", "Expand Tax", 63, List.of(PROBLEM_SOLVING_SKILL, TEAM_BUILDING_SKILL));
    private static final TaskTypeInputDTO SHRINK_VAT_TASK_TYPE =
            new TaskTypeInputDTO("SV", "Shrink VAT", 63, List.of(STRATEGIC_PLANNING_SKILL));
    private static final TaskTypeInputDTO APPROVE_LEGAL_TASK_TYPE =
            new TaskTypeInputDTO("AL", "Approve Legal", 40, List.of(RISK_MANAGEMENT_SKILL));

    private static final CustomerInputDTO STEEL_INC_CUSTOMER = new CustomerInputDTO("1", "Steel Inc");
    private static final CustomerInputDTO PAPER_CORP_CUSTOMER = new CustomerInputDTO("2", "Paper Corp");
    private static final CustomerInputDTO STONE_LIMITED_CUSTOMER = new CustomerInputDTO("3", "Stone Limited");
    private static final CustomerInputDTO WOOD_EXPRESS_CUSTOMER = new CustomerInputDTO("4", "Wood Express");

    private DemoDataBuilder() {
    }

    public static TaskAssigningInput basic() {
        List<CustomerInputDTO> customers =
                List.of(STEEL_INC_CUSTOMER, PAPER_CORP_CUSTOMER, STONE_LIMITED_CUSTOMER, WOOD_EXPRESS_CUSTOMER);
        List<TaskTypeInputDTO> taskTypes =
                List.of(IMPROVE_SALES_TASK_TYPE, EXPAND_TAX_TASK_TYPE, SHRINK_VAT_TASK_TYPE, APPROVE_LEGAL_TASK_TYPE);
        return new TaskAssigningInput(customers, taskTypes, buildEmployees(), buildTasks());
    }

    private static List<TaskInputDTO> buildTasks() {
        return List.of(
                aTask("1", SHRINK_VAT_TASK_TYPE, 1, PAPER_CORP_CUSTOMER, MINOR),
                aTask("2", APPROVE_LEGAL_TASK_TYPE, 1, WOOD_EXPRESS_CUSTOMER, MAJOR),
                aTask("3", SHRINK_VAT_TASK_TYPE, 2, WOOD_EXPRESS_CUSTOMER, MAJOR),
                aTask("4", IMPROVE_SALES_TASK_TYPE, 1, WOOD_EXPRESS_CUSTOMER, CRITICAL),
                aTask("5", APPROVE_LEGAL_TASK_TYPE, 2, STONE_LIMITED_CUSTOMER, MINOR),
                aTask("6", APPROVE_LEGAL_TASK_TYPE, 3, STONE_LIMITED_CUSTOMER, MINOR),
                aTask("7", EXPAND_TAX_TASK_TYPE, 1, PAPER_CORP_CUSTOMER, MINOR),
                aTask("8", EXPAND_TAX_TASK_TYPE, 2, STEEL_INC_CUSTOMER, MINOR),
                aTask("9", EXPAND_TAX_TASK_TYPE, 3, STEEL_INC_CUSTOMER, MINOR),
                aTask("10", SHRINK_VAT_TASK_TYPE, 3, WOOD_EXPRESS_CUSTOMER, MAJOR),
                aTask("11", IMPROVE_SALES_TASK_TYPE, 2, WOOD_EXPRESS_CUSTOMER, MINOR),
                aTask("12", EXPAND_TAX_TASK_TYPE, 4, STEEL_INC_CUSTOMER, MAJOR),
                aTask("13", IMPROVE_SALES_TASK_TYPE, 3, STONE_LIMITED_CUSTOMER, MINOR),
                aTask("14", EXPAND_TAX_TASK_TYPE, 5, WOOD_EXPRESS_CUSTOMER, MAJOR),
                aTask("15", EXPAND_TAX_TASK_TYPE, 6, STONE_LIMITED_CUSTOMER, CRITICAL),
                aTask("16", EXPAND_TAX_TASK_TYPE, 7, PAPER_CORP_CUSTOMER, MINOR),
                aTask("17", APPROVE_LEGAL_TASK_TYPE, 4, STONE_LIMITED_CUSTOMER, MAJOR),
                aTask("18", APPROVE_LEGAL_TASK_TYPE, 5, STEEL_INC_CUSTOMER, MAJOR),
                aTask("19", IMPROVE_SALES_TASK_TYPE, 4, WOOD_EXPRESS_CUSTOMER, MAJOR),
                aTask("20", IMPROVE_SALES_TASK_TYPE, 5, WOOD_EXPRESS_CUSTOMER, CRITICAL),
                aTask("21", IMPROVE_SALES_TASK_TYPE, 6, STEEL_INC_CUSTOMER, MINOR),
                aTask("22", IMPROVE_SALES_TASK_TYPE, 7, PAPER_CORP_CUSTOMER, MAJOR),
                aTask("23", IMPROVE_SALES_TASK_TYPE, 8, WOOD_EXPRESS_CUSTOMER, CRITICAL),
                aTask("24", APPROVE_LEGAL_TASK_TYPE, 6, WOOD_EXPRESS_CUSTOMER, MINOR),
                aTask("25", APPROVE_LEGAL_TASK_TYPE, 7, STEEL_INC_CUSTOMER, CRITICAL),
                aTask("26", IMPROVE_SALES_TASK_TYPE, 9, WOOD_EXPRESS_CUSTOMER, MAJOR),
                aTask("27", IMPROVE_SALES_TASK_TYPE, 10, STEEL_INC_CUSTOMER, CRITICAL),
                aTask("28", IMPROVE_SALES_TASK_TYPE, 11, WOOD_EXPRESS_CUSTOMER, MAJOR));
    }

    private static TaskInputDTO aTask(String id, TaskTypeInputDTO taskType, int indexInTaskType,
            CustomerInputDTO customer, org.acme.taskassigning.domain.Priority priority) {
        return new TaskInputDTO(id, taskType.code(), indexInTaskType, customer.id(), 0L, priority);
    }

    private static List<EmployeeInputDTO> buildEmployees() {
        return List.of(
                anEmployee("1", "Amy", List.of(PROBLEM_SOLVING_SKILL, BUSINESS_STORYTELLING_SKILL, TEAM_BUILDING_SKILL),
                        Map.of(STEEL_INC_CUSTOMER.id(), HIGH,
                                PAPER_CORP_CUSTOMER.id(), MEDIUM,
                                STONE_LIMITED_CUSTOMER.id(), HIGH,
                                WOOD_EXPRESS_CUSTOMER.id(), MEDIUM)),
                anEmployee("2", "Beth", List.of(RISK_MANAGEMENT_SKILL, CREATIVE_THINKING_SKILL),
                        Map.of(STEEL_INC_CUSTOMER.id(), LOW,
                                PAPER_CORP_CUSTOMER.id(), HIGH,
                                STONE_LIMITED_CUSTOMER.id(), LOW,
                                WOOD_EXPRESS_CUSTOMER.id(), MEDIUM)),
                anEmployee("3", "Carl", List.of(STRATEGIC_PLANNING_SKILL, PROBLEM_SOLVING_SKILL),
                        Map.of(STEEL_INC_CUSTOMER.id(), MEDIUM,
                                PAPER_CORP_CUSTOMER.id(), HIGH,
                                STONE_LIMITED_CUSTOMER.id(), MEDIUM,
                                WOOD_EXPRESS_CUSTOMER.id(), LOW)),
                anEmployee("4", "Dan", List.of(BUSINESS_STORYTELLING_SKILL, TEAM_BUILDING_SKILL),
                        Map.of(STEEL_INC_CUSTOMER.id(), LOW,
                                PAPER_CORP_CUSTOMER.id(), HIGH,
                                STONE_LIMITED_CUSTOMER.id(), HIGH,
                                WOOD_EXPRESS_CUSTOMER.id(), MEDIUM)),
                anEmployee("5", "Elsa", List.of(RISK_MANAGEMENT_SKILL, STRATEGIC_PLANNING_SKILL, CREATIVE_THINKING_SKILL),
                        Map.of(STEEL_INC_CUSTOMER.id(), MEDIUM,
                                PAPER_CORP_CUSTOMER.id(), HIGH,
                                STONE_LIMITED_CUSTOMER.id(), HIGH,
                                WOOD_EXPRESS_CUSTOMER.id(), MEDIUM)),
                anEmployee("6", "Flo", List.of(PROBLEM_SOLVING_SKILL, TEAM_BUILDING_SKILL),
                        Map.of(STEEL_INC_CUSTOMER.id(), HIGH,
                                PAPER_CORP_CUSTOMER.id(), HIGH,
                                STONE_LIMITED_CUSTOMER.id(), NONE,
                                WOOD_EXPRESS_CUSTOMER.id(), MEDIUM)),
                anEmployee("7", "Gus", List.of(RISK_MANAGEMENT_SKILL, CREATIVE_THINKING_SKILL, BUSINESS_STORYTELLING_SKILL),
                        Map.of(STEEL_INC_CUSTOMER.id(), LOW,
                                PAPER_CORP_CUSTOMER.id(), MEDIUM,
                                STONE_LIMITED_CUSTOMER.id(), MEDIUM,
                                WOOD_EXPRESS_CUSTOMER.id(), LOW)),
                anEmployee("8", "Hugo", List.of(STRATEGIC_PLANNING_SKILL, PROBLEM_SOLVING_SKILL, TEAM_BUILDING_SKILL),
                        Map.of(STEEL_INC_CUSTOMER.id(), NONE,
                                PAPER_CORP_CUSTOMER.id(), MEDIUM,
                                STONE_LIMITED_CUSTOMER.id(), NONE,
                                WOOD_EXPRESS_CUSTOMER.id(), LOW)));
    }

    private static EmployeeInputDTO anEmployee(String id, String fullName, List<String> skills,
            Map<String, org.acme.taskassigning.domain.Affinity> customerAffinities) {
        return new EmployeeInputDTO(id, fullName, skills, customerAffinities, null);
    }
}
