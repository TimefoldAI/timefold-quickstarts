package org.acme.orderpicking.solver;

import java.util.ArrayList;
import java.util.List;

import org.acme.orderpicking.dto.OrderPickingInput;
import org.acme.orderpicking.dto.PickTaskDTO;
import org.acme.orderpicking.dto.TrolleyDTO;
import org.acme.orderpicking.dto.WarehouseLocationDTO;

final class SolverTestDataFactory {

    private static final int BUCKET_COUNT = 4;
    private static final int BUCKET_CAPACITY = 50_000;
    private static final String LEFT = "LEFT";
    private static final String RIGHT = "RIGHT";

    private SolverTestDataFactory() {
    }

    static OrderPickingInput createProblem() {
        WarehouseLocationDTO origin = new WarehouseLocationDTO("(A,1)", LEFT, 0);
        List<TrolleyDTO> trolleys = new ArrayList<>();
        trolleys.add(new TrolleyDTO("1", BUCKET_COUNT, BUCKET_CAPACITY, origin, List.of()));
        trolleys.add(new TrolleyDTO("2", BUCKET_COUNT, BUCKET_CAPACITY, origin, List.of()));

        String[][] definitions = {
                { "1-0", "1", "p1", "Milk", "1200", "(A,1)", LEFT, "3" },
                { "1-1", "1", "p2", "Bread", "800", "(B,2)", RIGHT, "5" },
                { "1-2", "1", "p3", "Eggs", "1000", "(C,3)", LEFT, "2" },
                { "2-0", "2", "p4", "Apples", "1800", "(D,1)", RIGHT, "7" },
                { "2-1", "2", "p5", "Carrots", "1000", "(E,2)", LEFT, "1" },
                { "2-2", "2", "p6", "Soup", "1000", "(B,3)", RIGHT, "8" }
        };
        List<PickTaskDTO> pickTasks = new ArrayList<>();
        for (String[] definition : definitions) {
            WarehouseLocationDTO location =
                    new WarehouseLocationDTO(definition[5], definition[6], Integer.parseInt(definition[7]));
            pickTasks.add(new PickTaskDTO(definition[0], definition[1], definition[2], definition[3],
                    Integer.parseInt(definition[4]), location));
        }

        return new OrderPickingInput(trolleys, pickTasks);
    }
}
