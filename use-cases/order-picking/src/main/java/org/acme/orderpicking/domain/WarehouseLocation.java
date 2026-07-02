package org.acme.orderpicking.domain;

/**
 * Represents a location in the warehouse where a product can be stored. In the context of the order picking problem
 * the warehouse is modeled as set of shelvings. For picking a particular product the employees walks to the indicated
 * shelving side and row.
 * 
 * @see Warehouse
 * @see Shelving
 */
public class WarehouseLocation {

    private String shelvingId;
    private Shelving.Side side;
    private int row;

    public WarehouseLocation() {
        //marshalling constructor
    }

    public WarehouseLocation(String shelvingId, Shelving.Side side, int row) {
        this.shelvingId = shelvingId;
        this.side = side;
        this.row = row;
    }

    public String getShelvingId() {
        return shelvingId;
    }

    public void setShelvingId(String shelvingId) {
        this.shelvingId = shelvingId;
    }

    public Shelving.Side getSide() {
        return side;
    }

    public void setSide(Shelving.Side side) {
        this.side = side;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    @Override
    public String toString() {
        return "WarehouseLocation{" +
                "shelvingId='" + shelvingId + '\'' +
                ", side=" + side +
                ", row=" + row +
                '}';
    }
}
