package org.acme.orderpicking.domain;

/**
 * Represents the products container. In the order picking problem the warehouse is represented as a set of shelvings
 * that are organized into columns and rows. Each shelving has two sides where the products can be stored, and
 * a number of rows.
 * 
 * @see Warehouse
 */
public class Shelving {

    public static final int ROWS_SIZE = 10;

    /**
     * Available shelving sides where the products can be located.
     */
    public enum Side {
        LEFT,
        RIGHT
    }

    private String id;

    /**
     * Absolute x position of the shelving's left bottom corner within the warehouse.
     */
    private int x;
    /**
     * Absolute y position of the shelving's left bottom corner within the warehouse.
     */
    private int y;

    Shelving(String id, int x, int y) {
        this.id = id;
        this.x = x;
        this.y = y;
    }

    public static String newShelvingId(Warehouse.Column column, Warehouse.Row row) {
        return "(" + column.toString() + "," + row.toString() + ")";
    }

    public String getId() {
        return id;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
