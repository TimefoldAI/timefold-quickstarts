package org.acme.orderpicking.demo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.acme.orderpicking.domain.Shelving;
import org.acme.orderpicking.domain.Warehouse;
import org.acme.orderpicking.dto.OrderPickingInput;
import org.acme.orderpicking.dto.PickTaskDTO;
import org.acme.orderpicking.dto.TrolleyDTO;
import org.acme.orderpicking.dto.WarehouseLocationDTO;

/**
 * Builds a deterministic demo order picking dataset: a fixed set of trolleys plus a pseudo-random set of orders whose
 * items are scattered across the warehouse shelvings.
 */
public final class DemoDataBuilder {

    private static final int RANDOM_SEED = 37;
    private static final int MINIMUM_COUNT = 1;
    private static final int TROLLEY_START_ROW = 0;

    private static final List<ProductDef> PRODUCTS = List.of(
            new ProductDef("Kelloggs Cornflakes", 30 * 12 * 35, ProductFamily.GENERAL_FOOD),
            new ProductDef("Cream Crackers", 23 * 7 * 2, ProductFamily.GENERAL_FOOD),
            new ProductDef("Tea Bags 240 packet", 2 * 6 * 15, ProductFamily.GENERAL_FOOD),
            new ProductDef("Tomato Soup Can", 10 * 10 * 10, ProductFamily.GENERAL_FOOD),
            new ProductDef("Baked Beans in Tomato Sauce", 10 * 10 * 11, ProductFamily.GENERAL_FOOD),
            new ProductDef("Classic Mint Sauce", 8 * 10 * 8, ProductFamily.GENERAL_FOOD),
            new ProductDef("Raspberry Conserve", 8 * 11 * 8, ProductFamily.GENERAL_FOOD),
            new ProductDef("Orange Fine Shred Marmalade", 7 * 8 * 7, ProductFamily.GENERAL_FOOD),
            new ProductDef("Free Range Eggs 6 Pack", 15 * 10 * 8, ProductFamily.FRESH_FOOD),
            new ProductDef("Mature Cheddar 400G", 10 * 9 * 5, ProductFamily.FRESH_FOOD),
            new ProductDef("Butter Packet", 12 * 5 * 5, ProductFamily.FRESH_FOOD),
            new ProductDef("Iceberg Lettuce Each", 2500, ProductFamily.FRUITS_AND_VEGETABLES),
            new ProductDef("Carrots 1Kg", 1000, ProductFamily.FRUITS_AND_VEGETABLES),
            new ProductDef("Organic Fair Trade Bananas 5 Pack", 1800, ProductFamily.FRUITS_AND_VEGETABLES),
            new ProductDef("Gala Apple Minimum 5 Pack", 25 * 20 * 10, ProductFamily.FRUITS_AND_VEGETABLES),
            new ProductDef("Orange Bag 3kg", 29 * 20 * 15, ProductFamily.FRUITS_AND_VEGETABLES),
            new ProductDef("Fairy Non Biological Laundry Liquid 4.55L", 5000, ProductFamily.HOUSE_CLEANING),
            new ProductDef("Toilet Tissue 8 Roll White", 50 * 20 * 20, ProductFamily.HOUSE_CLEANING),
            new ProductDef("Kitchen Roll 200 Sheets x 2", 30 * 30 * 15, ProductFamily.HOUSE_CLEANING),
            new ProductDef("Stainless Steel Cleaner 500Ml", 500, ProductFamily.HOUSE_CLEANING),
            new ProductDef("Antibacterial Surface Spray", 12 * 4 * 25, ProductFamily.HOUSE_CLEANING),
            new ProductDef("Beef Lean Steak Mince 500g", 500, ProductFamily.MEET_AND_FISH),
            new ProductDef("Smoked Salmon 120G", 150, ProductFamily.MEET_AND_FISH),
            new ProductDef("Steak Burgers 454G", 450, ProductFamily.MEET_AND_FISH),
            new ProductDef("Pork Cooked Ham 125G", 125, ProductFamily.MEET_AND_FISH),
            new ProductDef("Chicken Breast Fillets 300G", 300, ProductFamily.MEET_AND_FISH),
            new ProductDef("6 Milk Bricks Pack", 22 * 16 * 21, ProductFamily.DRINKS),
            new ProductDef("Milk Brick", 1232, ProductFamily.DRINKS),
            new ProductDef("Skimmed Milk 2.5L", 2500, ProductFamily.DRINKS),
            new ProductDef("3L Orange Juice", 3 * 1000, ProductFamily.DRINKS),
            new ProductDef("Alcohol Free Beer 4 Pack", 30 * 15 * 30, ProductFamily.DRINKS),
            new ProductDef("Pepsi Regular Bottle", 1000, ProductFamily.DRINKS),
            new ProductDef("Pepsi Diet 6 x 330ml", 35 * 12 * 12, ProductFamily.DRINKS),
            new ProductDef("Schweppes Lemonade 2L", 2000, ProductFamily.DRINKS),
            new ProductDef("Coke Zero 8 x 330ml", 40 * 12 * 12, ProductFamily.DRINKS),
            new ProductDef("Natural Mineral Water Still 6 X 1.5Ltr", 6 * 1500, ProductFamily.DRINKS),
            new ProductDef("Cocktail Crisps 6 Pack", 20 * 10 * 10, ProductFamily.SNACKS));

    private static final Map<ProductFamily, List<String>> SHELVINGS_PER_FAMILY = Map.of(
            ProductFamily.FRUITS_AND_VEGETABLES, List.of(
                    Shelving.newShelvingId(Warehouse.Column.COL_A, Warehouse.Row.ROW_1),
                    Shelving.newShelvingId(Warehouse.Column.COL_A, Warehouse.Row.ROW_2)),
            ProductFamily.FRESH_FOOD, List.of(
                    Shelving.newShelvingId(Warehouse.Column.COL_A, Warehouse.Row.ROW_3)),
            ProductFamily.MEET_AND_FISH, List.of(
                    Shelving.newShelvingId(Warehouse.Column.COL_B, Warehouse.Row.ROW_2),
                    Shelving.newShelvingId(Warehouse.Column.COL_B, Warehouse.Row.ROW_3)),
            ProductFamily.FROZEN_PRODUCTS, List.of(
                    Shelving.newShelvingId(Warehouse.Column.COL_B, Warehouse.Row.ROW_2),
                    Shelving.newShelvingId(Warehouse.Column.COL_B, Warehouse.Row.ROW_1)),
            ProductFamily.DRINKS, List.of(
                    Shelving.newShelvingId(Warehouse.Column.COL_D, Warehouse.Row.ROW_1)),
            ProductFamily.SNACKS, List.of(
                    Shelving.newShelvingId(Warehouse.Column.COL_D, Warehouse.Row.ROW_2)),
            ProductFamily.GENERAL_FOOD, List.of(
                    Shelving.newShelvingId(Warehouse.Column.COL_B, Warehouse.Row.ROW_2),
                    Shelving.newShelvingId(Warehouse.Column.COL_C, Warehouse.Row.ROW_3),
                    Shelving.newShelvingId(Warehouse.Column.COL_D, Warehouse.Row.ROW_2),
                    Shelving.newShelvingId(Warehouse.Column.COL_D, Warehouse.Row.ROW_3)),
            ProductFamily.HOUSE_CLEANING, List.of(
                    Shelving.newShelvingId(Warehouse.Column.COL_E, Warehouse.Row.ROW_2),
                    Shelving.newShelvingId(Warehouse.Column.COL_E, Warehouse.Row.ROW_1)),
            ProductFamily.PETS, List.of(
                    Shelving.newShelvingId(Warehouse.Column.COL_E, Warehouse.Row.ROW_3)));

    private int trolleyCount;
    private int bucketCount;
    private int bucketCapacity;
    private int orderCount;

    private DemoDataBuilder() {
    }

    public static DemoDataBuilder builder() {
        return new DemoDataBuilder();
    }

    static int getMaxProductSize() {
        return PRODUCTS.stream().mapToInt(ProductDef::getVolume).max().orElse(0);
    }

    public DemoDataBuilder setTrolleyCount(int trolleyCount) {
        this.trolleyCount = trolleyCount;
        return this;
    }

    public DemoDataBuilder setBucketCount(int bucketCount) {
        this.bucketCount = bucketCount;
        return this;
    }

    public DemoDataBuilder setBucketCapacity(int bucketCapacity) {
        this.bucketCapacity = bucketCapacity;
        return this;
    }

    public DemoDataBuilder setOrderCount(int orderCount) {
        this.orderCount = orderCount;
        return this;
    }

    public OrderPickingInput build() {
        if (trolleyCount < MINIMUM_COUNT) {
            throw new IllegalStateException("Number of trolleys (" + trolleyCount + ") must be greater than zero.");
        }
        if (bucketCount < MINIMUM_COUNT) {
            throw new IllegalStateException("Number of buckets (" + bucketCount + ") must be greater than zero.");
        }
        if (bucketCapacity < getMaxProductSize()) {
            throw new IllegalStateException("Bucket capacity (" + bucketCapacity
                    + ") must be at least the maximum product size (" + getMaxProductSize() + ").");
        }
        if (orderCount < MINIMUM_COUNT) {
            throw new IllegalStateException("Number of orders (" + orderCount + ") must be greater than zero.");
        }
        Random random = new Random(RANDOM_SEED);
        List<ProductWithLocation> products = buildProducts(random);
        return new OrderPickingInput(buildTrolleys(), buildPickTasks(products, random));
    }

    private List<TrolleyDTO> buildTrolleys() {
        WarehouseLocationDTO startLocation = new WarehouseLocationDTO(
                Shelving.newShelvingId(Warehouse.Column.COL_A, Warehouse.Row.ROW_1),
                Shelving.Side.LEFT.name(), TROLLEY_START_ROW);
        List<TrolleyDTO> trolleys = new ArrayList<>();
        for (int i = 1; i <= trolleyCount; i++) {
            trolleys.add(new TrolleyDTO(Integer.toString(i), bucketCount, bucketCapacity, startLocation, List.of()));
        }
        return trolleys;
    }

    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private List<PickTaskDTO> buildPickTasks(List<ProductWithLocation> products, Random random) {
        List<PickTaskDTO> pickTasks = new ArrayList<>();
        for (int orderNumber = 1; orderNumber <= orderCount; orderNumber++) {
            String orderId = Integer.toString(orderNumber);
            int orderItemsSize = MINIMUM_COUNT + random.nextInt(products.size() - MINIMUM_COUNT);
            Set<String> orderProducts = new HashSet<>();
            int itemNumber = 0;
            for (int i = 0; i < orderItemsSize; i++) {
                ProductWithLocation product = products.get(random.nextInt(products.size()));
                if (orderProducts.add(product.getName())) {
                    pickTasks.add(new PickTaskDTO(orderId + "-" + itemNumber, orderId, product.getName(),
                            product.getName(), product.getVolume(), product.getLocation()));
                    itemNumber += 1;
                }
            }
        }
        return pickTasks;
    }

    private List<ProductWithLocation> buildProducts(Random random) {
        List<ProductWithLocation> products = new ArrayList<>();
        for (ProductDef definition : PRODUCTS) {
            List<String> shelvingIds = SHELVINGS_PER_FAMILY.get(definition.getFamily());
            String shelvingId = shelvingIds.get(random.nextInt(shelvingIds.size()));
            String side = (random.nextBoolean() ? Shelving.Side.LEFT : Shelving.Side.RIGHT).name();
            int row = random.nextInt(Shelving.ROWS_SIZE) + 1;
            products.add(new ProductWithLocation(definition.getName(), definition.getVolume(),
                    new WarehouseLocationDTO(shelvingId, side, row)));
        }
        return products;
    }

    enum ProductFamily {
        GENERAL_FOOD,
        FRESH_FOOD,
        MEET_AND_FISH,
        FROZEN_PRODUCTS,
        FRUITS_AND_VEGETABLES,
        HOUSE_CLEANING,
        DRINKS,
        SNACKS,
        PETS
    }

    static final class ProductDef {
        private final String name;
        private final int volume;
        private final ProductFamily family;

        ProductDef(String name, int volume, ProductFamily family) {
            this.name = name;
            this.volume = volume;
            this.family = family;
        }

        String getName() {
            return name;
        }

        int getVolume() {
            return volume;
        }

        ProductFamily getFamily() {
            return family;
        }
    }

    static final class ProductWithLocation {
        private final String name;
        private final int volume;
        private final WarehouseLocationDTO location;

        ProductWithLocation(String name, int volume, WarehouseLocationDTO location) {
            this.name = name;
            this.volume = volume;
            this.location = location;
        }

        String getName() {
            return name;
        }

        int getVolume() {
            return volume;
        }

        WarehouseLocationDTO getLocation() {
            return location;
        }
    }
}
