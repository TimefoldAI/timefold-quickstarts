package org.acme.taskassigning.rest;

import static org.acme.taskassigning.domain.Affinity.HIGH;
import static org.acme.taskassigning.domain.Affinity.LOW;
import static org.acme.taskassigning.domain.Affinity.MEDIUM;
import static org.acme.taskassigning.domain.Affinity.NONE;
import static org.acme.taskassigning.domain.Priority.CRITICAL;
import static org.acme.taskassigning.domain.Priority.MAJOR;
import static org.acme.taskassigning.domain.Priority.MINOR;

import java.util.List;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;

import org.acme.taskassigning.domain.Customer;
import org.acme.taskassigning.domain.Employee;
import org.acme.taskassigning.domain.Task;
import org.acme.taskassigning.domain.TaskAssigningSolution;
import org.acme.taskassigning.domain.TaskType;

@ApplicationScoped
public class DemoDataGenerator {

    private static final String PROBLEM_SOLVING_SKILL = "Problem Solving";
    private static final String TEAM_BUILDING_SKILL = "Team Building";
    private static final String BUSINESS_STORYTELLING_SKILL = "Business Storytelling";
    private static final String RISK_MANAGEMENT_SKILL = "Risk Management";
    private static final String CREATIVE_THINKING_SKILL = "Creative Thinking";
    private static final String STRATEGIC_PLANNING_SKILL = "Strategic Planning";

    private static final TaskType IMPROVE_SALES_TASK_TYPE =
            new TaskType("IS", "Improve Sales", 46, List.of(STRATEGIC_PLANNING_SKILL));
    private static final TaskType EXPAND_TAX_TASK_TYPE =
            new TaskType("ET", "Expand Tax", 63, List.of(PROBLEM_SOLVING_SKILL, TEAM_BUILDING_SKILL));
    private static final TaskType SHRINK_VAT_TASK_TYPE =
            new TaskType("SV", "Shrink VAT", 63, List.of(STRATEGIC_PLANNING_SKILL));
    private static final TaskType APPROVE_LEGAL_TASK_TYPE =
            new TaskType("AL", "Approve Legal", 40, List.of(RISK_MANAGEMENT_SKILL));

    private static final Customer STEEL_INC_CUSTOMER = new Customer("1", "Steel Inc");
    private static final Customer PAPER_CORP_CUSTOMER = new Customer("2", "Paper Corp");
    private static final Customer STONE_LIMITED_CUSTOMER = new Customer("3", "Stone Limited");
    private static final Customer WOOD_EXPRESS_CUSTOMER = new Customer("4", "Wood Express");

    public TaskAssigningSolution generateDemoData() {
        TaskAssigningSolution plan = new TaskAssigningSolution();
        // Customers
        List<Customer> customers =
                List.of(STEEL_INC_CUSTOMER, PAPER_CORP_CUSTOMER, STONE_LIMITED_CUSTOMER, WOOD_EXPRESS_CUSTOMER);
        // Employees
        List<Employee> employees = generateEmployees();
        // Tasks
        List<Task> tasks = generateTasks();
        // Update the plan
        plan.setTaskTypes(
                List.of(IMPROVE_SALES_TASK_TYPE, EXPAND_TAX_TASK_TYPE, SHRINK_VAT_TASK_TYPE, APPROVE_LEGAL_TASK_TYPE));
        plan.setCustomers(customers);
        plan.setEmployees(employees);
        plan.setTasks(tasks);
        return plan;
    }

    private List<Task> generateTasks() {
        return List.of(
                new Task("1", SHRINK_VAT_TASK_TYPE, 1, PAPER_CORP_CUSTOMER, MINOR),
                new Task("2", APPROVE_LEGAL_TASK_TYPE, 1, WOOD_EXPRESS_CUSTOMER, MAJOR),
                new Task("3", SHRINK_VAT_TASK_TYPE, 2, WOOD_EXPRESS_CUSTOMER, MAJOR),
                new Task("4", IMPROVE_SALES_TASK_TYPE, 1, WOOD_EXPRESS_CUSTOMER, CRITICAL),
                new Task("5", APPROVE_LEGAL_TASK_TYPE, 2, STONE_LIMITED_CUSTOMER, MINOR),
                new Task("6", APPROVE_LEGAL_TASK_TYPE, 3, STONE_LIMITED_CUSTOMER, MINOR),
                new Task("7", EXPAND_TAX_TASK_TYPE, 1, PAPER_CORP_CUSTOMER, MINOR),
                new Task("8", EXPAND_TAX_TASK_TYPE, 2, STEEL_INC_CUSTOMER, MINOR),
                new Task("9", EXPAND_TAX_TASK_TYPE, 3, STEEL_INC_CUSTOMER, MINOR),
                new Task("10", SHRINK_VAT_TASK_TYPE, 3, WOOD_EXPRESS_CUSTOMER, MAJOR),
                new Task("11", IMPROVE_SALES_TASK_TYPE, 2, WOOD_EXPRESS_CUSTOMER, MINOR),
                new Task("12", EXPAND_TAX_TASK_TYPE, 4, STEEL_INC_CUSTOMER, MAJOR),
                new Task("13", IMPROVE_SALES_TASK_TYPE, 3, STONE_LIMITED_CUSTOMER, MINOR),
                new Task("14", EXPAND_TAX_TASK_TYPE, 5, WOOD_EXPRESS_CUSTOMER, MAJOR),
                new Task("15", EXPAND_TAX_TASK_TYPE, 6, STONE_LIMITED_CUSTOMER, CRITICAL),
                new Task("16", EXPAND_TAX_TASK_TYPE, 7, PAPER_CORP_CUSTOMER, MINOR),
                new Task("17", APPROVE_LEGAL_TASK_TYPE, 4, STONE_LIMITED_CUSTOMER, MAJOR),
                new Task("18", APPROVE_LEGAL_TASK_TYPE, 5, STEEL_INC_CUSTOMER, MAJOR),
                new Task("19", IMPROVE_SALES_TASK_TYPE, 4, WOOD_EXPRESS_CUSTOMER, MAJOR),
                new Task("20", IMPROVE_SALES_TASK_TYPE, 5, WOOD_EXPRESS_CUSTOMER, CRITICAL),
                new Task("21", IMPROVE_SALES_TASK_TYPE, 6, STEEL_INC_CUSTOMER, MINOR),
                new Task("22", IMPROVE_SALES_TASK_TYPE, 7, PAPER_CORP_CUSTOMER, MAJOR),
                new Task("23", IMPROVE_SALES_TASK_TYPE, 8, WOOD_EXPRESS_CUSTOMER, CRITICAL),
                new Task("24", APPROVE_LEGAL_TASK_TYPE, 6, WOOD_EXPRESS_CUSTOMER, MINOR),
                new Task("25", APPROVE_LEGAL_TASK_TYPE, 7, STEEL_INC_CUSTOMER, CRITICAL),
                new Task("26", IMPROVE_SALES_TASK_TYPE, 9, WOOD_EXPRESS_CUSTOMER, MAJOR),
                new Task("27", IMPROVE_SALES_TASK_TYPE, 10, STEEL_INC_CUSTOMER, CRITICAL),
                new Task("28", IMPROVE_SALES_TASK_TYPE, 11, WOOD_EXPRESS_CUSTOMER, MAJOR));
    }

    private List<Employee> generateEmployees() {
        return List.of(
                new Employee("1", "Amy", List.of(PROBLEM_SOLVING_SKILL, BUSINESS_STORYTELLING_SKILL, TEAM_BUILDING_SKILL),
                        Map.of(STEEL_INC_CUSTOMER, HIGH,
                                PAPER_CORP_CUSTOMER, MEDIUM,
                                STONE_LIMITED_CUSTOMER, HIGH,
                                WOOD_EXPRESS_CUSTOMER, MEDIUM)),
                new Employee("2", "Beth", List.of(RISK_MANAGEMENT_SKILL, CREATIVE_THINKING_SKILL),
                        Map.of(STEEL_INC_CUSTOMER, LOW,
                                PAPER_CORP_CUSTOMER, HIGH,
                                STONE_LIMITED_CUSTOMER, LOW,
                                WOOD_EXPRESS_CUSTOMER, MEDIUM)),
                new Employee("3", "Carl", List.of(STRATEGIC_PLANNING_SKILL, PROBLEM_SOLVING_SKILL),
                        Map.of(STEEL_INC_CUSTOMER, MEDIUM,
                                PAPER_CORP_CUSTOMER, HIGH,
                                STONE_LIMITED_CUSTOMER, MEDIUM,
                                WOOD_EXPRESS_CUSTOMER, LOW)),
                new Employee("4", "Dan", List.of(BUSINESS_STORYTELLING_SKILL, TEAM_BUILDING_SKILL),
                        Map.of(STEEL_INC_CUSTOMER, LOW,
                                PAPER_CORP_CUSTOMER, HIGH,
                                STONE_LIMITED_CUSTOMER, HIGH,
                                WOOD_EXPRESS_CUSTOMER, MEDIUM)),
                new Employee("5", "Elsa", List.of(RISK_MANAGEMENT_SKILL, STRATEGIC_PLANNING_SKILL, CREATIVE_THINKING_SKILL),
                        Map.of(STEEL_INC_CUSTOMER, MEDIUM,
                                PAPER_CORP_CUSTOMER, HIGH,
                                STONE_LIMITED_CUSTOMER, HIGH,
                                WOOD_EXPRESS_CUSTOMER, MEDIUM)),
                new Employee("6", "Flo", List.of(PROBLEM_SOLVING_SKILL, TEAM_BUILDING_SKILL),
                        Map.of(STEEL_INC_CUSTOMER, HIGH,
                                PAPER_CORP_CUSTOMER, HIGH,
                                STONE_LIMITED_CUSTOMER, NONE,
                                WOOD_EXPRESS_CUSTOMER, MEDIUM)),
                new Employee("7", "Gus", List.of(RISK_MANAGEMENT_SKILL, CREATIVE_THINKING_SKILL, BUSINESS_STORYTELLING_SKILL),
                        Map.of(STEEL_INC_CUSTOMER, LOW,
                                PAPER_CORP_CUSTOMER, MEDIUM,
                                STONE_LIMITED_CUSTOMER, MEDIUM,
                                WOOD_EXPRESS_CUSTOMER, LOW)),
                new Employee("8", "Hugo", List.of(STRATEGIC_PLANNING_SKILL, PROBLEM_SOLVING_SKILL, TEAM_BUILDING_SKILL),
                        Map.of(STEEL_INC_CUSTOMER, NONE,
                                PAPER_CORP_CUSTOMER, MEDIUM,
                                STONE_LIMITED_CUSTOMER, NONE,
                                WOOD_EXPRESS_CUSTOMER, LOW)));
    }
}
