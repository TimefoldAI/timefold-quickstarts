package org.acme.orderpicking.bootstrap;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

import io.quarkus.runtime.StartupEvent;
import org.acme.orderpicking.domain.Order;
import org.acme.orderpicking.domain.OrderItem;
import org.acme.orderpicking.domain.OrderPickingSolution;
import org.acme.orderpicking.domain.Product;
import org.acme.orderpicking.domain.Shelving;
import org.acme.orderpicking.domain.Trolley;
import org.acme.orderpicking.domain.TrolleyStep;
import org.acme.orderpicking.domain.WarehouseLocation;
import org.acme.orderpicking.persistence.OrderPickingRepository;

import static org.acme.orderpicking.domain.Shelving.newShelvingId;
import static org.acme.orderpicking.domain.Warehouse.Column.COL_A;
import static org.acme.orderpicking.domain.Warehouse.Column.COL_B;
import static org.acme.orderpicking.domain.Warehouse.Column.COL_C;
import static org.acme.orderpicking.domain.Warehouse.Column.COL_D;
import static org.acme.orderpicking.domain.Warehouse.Column.COL_E;
import static org.acme.orderpicking.domain.Warehouse.Row.ROW_1;
import static org.acme.orderpicking.domain.Warehouse.Row.ROW_2;
import static org.acme.orderpicking.domain.Warehouse.Row.ROW_3;

/**
 * Helper class for generating data sets.
 */
@ApplicationScoped
public class DemoDataGenerator {

    private static final int ORDER_ITEMS_SIZE_MINIMUM = 1;

    /**
     * Number of trolleys for the simulation.
     */
    private static final int TROLLEYS_COUNT = 5;

    /**
     * Number of buckets on each trolley.
     */
    private static final int BUCKET_COUNT = 4;

    /**
     * Buckets capacity.
     */
    private static final int BUCKET_CAPACITY = 60 * 40 * 20;

    /**
     * Number of orders for the simulation.
     */
    private static final int ORDERS_COUNT = 8;

    /**
     * Start location for the trolleys.
     */
    private static final WarehouseLocation START_LOCATION = new WarehouseLocation(Shelving.newShelvingId(COL_A, ROW_1), Shelving.Side.LEFT, 0);

    public enum ProductFamily {
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
    
    public static class ProductFamilyPair {
        private final Product product;
        private final ProductFamily family;

        public ProductFamilyPair(Product product, ProductFamily family) {
            this.product = product;
            this.family = family;
        }

        public Product getProduct() {
            return product;
        }

        public ProductFamily getFamily() {
            return family;
        }
    }

    private static final List<ProductFamilyPair> PRODUCTS = List.of(
            new ProductFamilyPair(new Product(nextId(), "Kelloggs Cornflakes", 30 * 12 * 35, null), ProductFamily.GENERAL_FOOD),
            new ProductFamilyPair(new Product(nextId(), "Cream Crackers", 23 * 7 * 2, null), ProductFamily.GENERAL_FOOD),

            new ProductFamilyPair(new Product(nextId(), "Tea Bags 240 packet", 2 * 6 * 15, null), ProductFamily.GENERAL_FOOD),
            new ProductFamilyPair(new Product(nextId(), "Tomato Soup Can", 10 * 10 * 10, null), ProductFamily.GENERAL_FOOD),
            new ProductFamilyPair(new Product(nextId(), "Baked Beans in Tomato Sauce", 10 * 10 * 10, null), ProductFamily.GENERAL_FOOD),

            new ProductFamilyPair(new Product(nextId(), "Classic Mint Sauce", 8 * 10 * 8, null), ProductFamily.GENERAL_FOOD),
            new ProductFamilyPair(new Product(nextId(), "Raspberry Conserve", 8 * 10 * 8, null), ProductFamily.GENERAL_FOOD),
            new ProductFamilyPair(new Product(nextId(), "Orange Fine Shred Marmalade", 7 * 8 * 7, null), ProductFamily.GENERAL_FOOD),

            new ProductFamilyPair(new Product(nextId(), "Free Range Eggs 6 Pack", 15 * 10 * 8, null), ProductFamily.FRESH_FOOD),
            new ProductFamilyPair(new Product(nextId(), "Mature Cheddar 400G", 10 * 9 * 5, null), ProductFamily.FRESH_FOOD),
            new ProductFamilyPair(new Product(nextId(), "Butter Packet", 12 * 5 * 5, null), ProductFamily.FRESH_FOOD),

            new ProductFamilyPair(new Product(nextId(), "Iceberg Lettuce Each", 2500, null), ProductFamily.FRUITS_AND_VEGETABLES),
            new ProductFamilyPair(new Product(nextId(), "Carrots 1Kg", 1000, null), ProductFamily.FRUITS_AND_VEGETABLES),
            new ProductFamilyPair(new Product(nextId(), "Organic Fair Trade Bananas 5 Pack", 1800, null), ProductFamily.FRUITS_AND_VEGETABLES),
            new ProductFamilyPair(new Product(nextId(), "Gala Apple Minimum 5 Pack", 25 * 20 * 10, null), ProductFamily.FRUITS_AND_VEGETABLES),
            new ProductFamilyPair(new Product(nextId(), "Orange Bag 3kg", 29 * 20 * 15, null), ProductFamily.FRUITS_AND_VEGETABLES),

            new ProductFamilyPair(new Product(nextId(), "Fairy Non Biological Laundry Liquid 4.55L", 5000, null), ProductFamily.HOUSE_CLEANING),
            new ProductFamilyPair(new Product(nextId(), "Toilet Tissue 8 Roll White", 50 * 20 * 20, null), ProductFamily.HOUSE_CLEANING),
            new ProductFamilyPair(new Product(nextId(), "Kitchen Roll 200 Sheets x 2", 30 * 30 * 15, null), ProductFamily.HOUSE_CLEANING),
            new ProductFamilyPair(new Product(nextId(), "Stainless Steel Cleaner 500Ml", 500, null), ProductFamily.HOUSE_CLEANING),
            new ProductFamilyPair(new Product(nextId(), "Antibacterial Surface Spray", 12 * 4 * 25, null), ProductFamily.HOUSE_CLEANING),

            new ProductFamilyPair(new Product(nextId(), "Beef Lean Steak Mince 500g", 500, null), ProductFamily.MEET_AND_FISH),
            new ProductFamilyPair(new Product(nextId(), "Smoked Salmon 120G", 150, null), ProductFamily.MEET_AND_FISH),
            new ProductFamilyPair(new Product(nextId(), "Steak Burgers 454G", 450, null), ProductFamily.MEET_AND_FISH),
            new ProductFamilyPair(new Product(nextId(), "Pork Cooked Ham 125G", 125, null), ProductFamily.MEET_AND_FISH),
            new ProductFamilyPair(new Product(nextId(), "Chicken Breast Fillets 300G", 300, null), ProductFamily.MEET_AND_FISH),

            new ProductFamilyPair(new Product(nextId(), "6 Milk Bricks Pack", 22 * 16 * 21, null), ProductFamily.DRINKS),
            new ProductFamilyPair(new Product(nextId(), "Milk Brick", 1232, null), ProductFamily.DRINKS),
            new ProductFamilyPair(new Product(nextId(), "Skimmed Milk 2.5L", 2500, null), ProductFamily.DRINKS),
            new ProductFamilyPair(new Product(nextId(), "3L Orange Juice", 3 * 1000, null), ProductFamily.DRINKS),

            new ProductFamilyPair(new Product(nextId(), "Alcohol Free Beer 4 Pack", 30 * 15 * 30, null), ProductFamily.DRINKS),
            new ProductFamilyPair(new Product(nextId(), "Pepsi Regular Bottle", 1000, null), ProductFamily.DRINKS),
            new ProductFamilyPair(new Product(nextId(), "Pepsi Diet 6 x 330ml", 35 * 12 * 12, null), ProductFamily.DRINKS),

            new ProductFamilyPair(new Product(nextId(), "Schweppes Lemonade 2L", 2000, null), ProductFamily.DRINKS),
            new ProductFamilyPair(new Product(nextId(), "Coke Zero 8 x 330ml", 40 * 12 * 12, null), ProductFamily.DRINKS),
            new ProductFamilyPair(new Product(nextId(), "Natural Mineral Water Still 6 X 1.5Ltr", 6 * 1500, null), ProductFamily.DRINKS),

            new ProductFamilyPair(new Product(nextId(), "Cocktail Crisps 6 Pack", 20 * 10 * 10, null), ProductFamily.SNACKS)
    );

    private static final Map<ProductFamily, List<String>> SHELVINGS_PER_FAMILY = Map.of(
            ProductFamily.FRUITS_AND_VEGETABLES, List.of(
                    newShelvingId(COL_A, ROW_1),
                    newShelvingId(COL_A, ROW_2)),

            ProductFamily.FRESH_FOOD, List.of(
                    newShelvingId(COL_A, ROW_3)),

            ProductFamily.MEET_AND_FISH, List.of(
                    newShelvingId(COL_B, ROW_2),
                    newShelvingId(COL_B, ROW_3)),

            ProductFamily.FROZEN_PRODUCTS, List.of(
                    newShelvingId(COL_B, ROW_2),
                    newShelvingId(COL_B, ROW_1)),

            ProductFamily.DRINKS, List.of(
                    newShelvingId(COL_D, ROW_1)),

            ProductFamily.SNACKS, List.of(
                    newShelvingId(COL_D, ROW_2)),

            ProductFamily.GENERAL_FOOD, List.of(newShelvingId(COL_B, ROW_2),
                    newShelvingId(COL_C, ROW_3),
                    newShelvingId(COL_D, ROW_2),
                    newShelvingId(COL_D, ROW_3)),

            ProductFamily.HOUSE_CLEANING, List.of(newShelvingId(COL_E, ROW_2),
                    newShelvingId(COL_E, ROW_1)),

            ProductFamily.PETS, List.of(newShelvingId(COL_E, ROW_3))
    );

    private static long currentId = 0;

    @Inject
    OrderPickingRepository orderPickingRepository;

    private final Random random = new Random(37);

    public void startup(@Observes StartupEvent startupEvent) {
        // Generate the random solution to work with.
        validateBucketCapacity(BUCKET_CAPACITY);
        List<Trolley> trolleys = buildTrolleys(TROLLEYS_COUNT, BUCKET_COUNT, BUCKET_CAPACITY, START_LOCATION);
        List<Order> orders = buildOrders(ORDERS_COUNT);
        List<TrolleyStep> trolleySteps = buildTrolleySteps(orders);
        orderPickingRepository.save(new OrderPickingSolution(trolleys, trolleySteps));
    }

    public List<Order> buildOrders(int size) {
        List<Product> products = buildProducts();
        return buildOrders(size, products);
    }

    public List<Trolley> buildTrolleys(int size, int bucketCount, int bucketCapacity, WarehouseLocation startLocation) {
        List<Trolley> result = new ArrayList<>(size);
        for (int i = 1; i <= size; i++) {
            result.add(new Trolley(Integer.toString(i), bucketCount, bucketCapacity, startLocation));
        }
        return result;
    }

    public List<TrolleyStep> buildTrolleySteps(List<Order> orders) {
        List<TrolleyStep> result = new ArrayList<>();
        for (Order order : orders) {
            result.addAll(buildTrolleySteps(order));
        }
        return result;
    }

    public List<TrolleyStep> buildTrolleySteps(Order order) {
        List<TrolleyStep> steps = new ArrayList<>();
        for (OrderItem item : order.getItems()) {
            TrolleyStep trolleyStep = new TrolleyStep(item);
            steps.add(trolleyStep);
        }
        return steps;
    }

    public void validateBucketCapacity(int bucketCapacity) {
        if (bucketCapacity < getMaxProductSize()) {
            throw new IllegalArgumentException("The selected bucketCapacity: " + bucketCapacity + ", is lower than the maximum product size: " + getMaxProductSize() + "." +
                    " However for a matter of simplicity the problem was simplified on the assumption that products can always fit in a trolley bucket." +
                    " Please use a higher value");
        }
    }

    private List<Order> buildOrders(int size, List<Product> products) {
        List<Order> orders = new ArrayList<>();
        Order order;
        for (int orderNumber = 1; orderNumber <= size; orderNumber++) {
            int orderItemsSize = ORDER_ITEMS_SIZE_MINIMUM + random.nextInt(products.size() - ORDER_ITEMS_SIZE_MINIMUM);
            List<OrderItem> orderItems = new ArrayList<>();
            Set<String> orderProducts = new HashSet<>();
            order = new Order(Integer.toString(orderNumber), orderItems);
            int itemNumber = 1;
            for (int i = 0; i < orderItemsSize; i++) {
                int productItemIndex = random.nextInt(products.size());
                Product product = products.get(productItemIndex);
                if (!orderProducts.contains(product.getId())) {
                    orderItems.add(new OrderItem(Integer.toString(itemNumber++), order, product));
                    orderProducts.add(product.getId());
                }
            }
            orders.add(order);
        }
        return orders;
    }

    private List<Product> buildProducts() {
        return PRODUCTS.stream()
                .map(productFamilyPair -> {
                    List<String> shelvingIds = SHELVINGS_PER_FAMILY.get(productFamilyPair.getFamily());
                    int shelvingIndex = random.nextInt(shelvingIds.size());
                    Shelving.Side shelvingSide = Shelving.Side.values()[random.nextInt(Shelving.Side.values().length)];
                    int shelvingRow = random.nextInt(Shelving.ROWS_SIZE) + 1;
                    WarehouseLocation warehouseLocation = new WarehouseLocation(shelvingIds.get(shelvingIndex), shelvingSide, shelvingRow);
                    return new Product(productFamilyPair.getProduct().getId(),
                            productFamilyPair.getProduct().getName(),
                            productFamilyPair.getProduct().getVolume(),
                            warehouseLocation);
                }).collect(Collectors.toList());
    }

    public static int getMaxProductSize() {
        return PRODUCTS.stream()
                .mapToInt(productFamilyPair -> productFamilyPair.getProduct().getVolume())
                .max().orElse(0);
    }

    private static String nextId() {
        return String.valueOf(currentId++);
    }
}
