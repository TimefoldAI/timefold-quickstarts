package org.acme.orderpicking.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents the order submitted by a customer.
 */
public class Order {

    private String id;
    private List<OrderItem> items = new ArrayList<>();

    public Order() {
        //marshalling constructor
    }

    public Order(String id, List<OrderItem> items) {
        this.id = id;
        this.items = items;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = Objects.requireNonNullElseGet(items, ArrayList::new);
    }
}
