package org.acme.orderpicking.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * In the order picking problem context, the order item represents an indivisible product that was added to the order.
 * 
 * @see Product
 */
public class OrderItem {

    private String id;
    @JsonIgnore
    private Order order;
    private Product product;

    public OrderItem() {
        //marshalling constructor
    }

    public OrderItem(String id, Order order, Product product) {
        this.id = id;
        this.order = order;
        this.product = product;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getVolume() {
        return product.getVolume();
    }

    /**
     * Helper method, facilitates UI building.
     */
    public String getOrderId() {
        return order != null ? order.getId() : null;
    }

    @Override
    public String toString() {
        return "OrderItem{" +
                "id='" + id + '\'' +
                ", order=" + order +
                ", product=" + product +
                '}';
    }
}
