package org.acme.orderpicking.persistence;

import jakarta.enterprise.context.ApplicationScoped;

import org.acme.orderpicking.domain.OrderPickingSolution;

@ApplicationScoped
public class OrderPickingRepository {
    private OrderPickingSolution orderPickingSolution;

    public OrderPickingSolution find() {
        return orderPickingSolution;
    }

    public void save(OrderPickingSolution orderPickingSolution) {
        this.orderPickingSolution = orderPickingSolution;
    }
}
