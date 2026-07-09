package org.acme.taskassigning.solver;

import java.util.List;

import org.acme.taskassigning.dto.CustomerAffinityDTO;
import org.acme.taskassigning.dto.CustomerDTO;
import org.acme.taskassigning.dto.EmployeeDTO;
import org.acme.taskassigning.dto.TaskAssigningInput;
import org.acme.taskassigning.dto.TaskDTO;
import org.acme.taskassigning.dto.TaskTypeDTO;

final class SolverTestDataFactory {

    private SolverTestDataFactory() {
    }

    static TaskAssigningInput createProblem() {
        List<TaskTypeDTO> taskTypes = List.of(
                new TaskTypeDTO("T1", "Type 1", 10, List.of("skill1")),
                new TaskTypeDTO("T2", "Type 2", 20, List.of("skill2")));
        List<CustomerDTO> customers = List.of(
                new CustomerDTO("c1", "Customer 1"),
                new CustomerDTO("c2", "Customer 2"));
        List<EmployeeDTO> employees = List.of(
                new EmployeeDTO("e1", "Employee 1", List.of("skill1", "skill2"),
                        List.of(new CustomerAffinityDTO("c1", "HIGH"), new CustomerAffinityDTO("c2", "MEDIUM")),
                        List.of()),
                new EmployeeDTO("e2", "Employee 2", List.of("skill1", "skill2"),
                        List.of(new CustomerAffinityDTO("c1", "MEDIUM"), new CustomerAffinityDTO("c2", "HIGH")),
                        List.of()));
        Long startTime = null;
        List<TaskDTO> tasks = List.of(
                new TaskDTO("t1", "T1", 1, "c1", 0L, "CRITICAL", startTime),
                new TaskDTO("t2", "T2", 1, "c2", 0L, "MAJOR", startTime),
                new TaskDTO("t3", "T1", 2, "c1", 0L, "MINOR", startTime),
                new TaskDTO("t4", "T2", 2, "c2", 0L, "MINOR", startTime));
        return new TaskAssigningInput(taskTypes, customers, employees, tasks);
    }
}
