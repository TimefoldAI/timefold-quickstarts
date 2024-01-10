package org.acme.vehiclerouting.solver;

import java.time.LocalDateTime;
import java.util.Objects;

import ai.timefold.solver.core.api.domain.variable.VariableListener;
import ai.timefold.solver.core.api.score.director.ScoreDirector;

import org.acme.vehiclerouting.domain.Customer;
import org.acme.vehiclerouting.domain.VehicleRoutePlan;

public class ArrivalTimeUpdatingVariableListener implements VariableListener<VehicleRoutePlan, Customer> {

    private static final String ARRIVAL_TIME_FIELD = "arrivalTime";

    @Override
    public void beforeVariableChanged(ScoreDirector<VehicleRoutePlan> scoreDirector, Customer customer) {

    }

    @Override
    public void afterVariableChanged(ScoreDirector<VehicleRoutePlan> scoreDirector, Customer customer) {
        if (customer.getVehicle() == null) {
            if (customer.getArrivalTime() != null) {
                scoreDirector.beforeVariableChanged(customer, ARRIVAL_TIME_FIELD);
                customer.setArrivalTime(null);
                scoreDirector.afterVariableChanged(customer, ARRIVAL_TIME_FIELD);
            }
            return;
        }

        Customer previousCustomer = customer.getPreviousCustomer();
        LocalDateTime departureTime =
                previousCustomer == null ? customer.getVehicle().getDepartureTime() : previousCustomer.getDepartureTime();

        Customer nextCustomer = customer;
        LocalDateTime arrivalTime = calculateArrivalTime(nextCustomer, departureTime);
        while (nextCustomer != null && !Objects.equals(nextCustomer.getArrivalTime(), arrivalTime)) {
            scoreDirector.beforeVariableChanged(nextCustomer, ARRIVAL_TIME_FIELD);
            nextCustomer.setArrivalTime(arrivalTime);
            scoreDirector.afterVariableChanged(nextCustomer, ARRIVAL_TIME_FIELD);
            departureTime = nextCustomer.getDepartureTime();
            nextCustomer = nextCustomer.getNextCustomer();
            arrivalTime = calculateArrivalTime(nextCustomer, departureTime);
        }
    }

    @Override
    public void beforeEntityAdded(ScoreDirector<VehicleRoutePlan> scoreDirector, Customer customer) {

    }

    @Override
    public void afterEntityAdded(ScoreDirector<VehicleRoutePlan> scoreDirector, Customer customer) {

    }

    @Override
    public void beforeEntityRemoved(ScoreDirector<VehicleRoutePlan> scoreDirector, Customer customer) {

    }

    @Override
    public void afterEntityRemoved(ScoreDirector<VehicleRoutePlan> scoreDirector, Customer customer) {

    }

    private LocalDateTime calculateArrivalTime(Customer customer, LocalDateTime previousDepartureTime) {
        if (customer == null || previousDepartureTime == null) {
            return null;
        }
        return previousDepartureTime.plusSeconds(customer.getDrivingTimeSecondsFromPreviousStandstill());
    }
}
