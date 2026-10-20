package org.acme.orderpicking.demo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.acme.orderpicking.domain.ShelvingSide;
import org.acme.orderpicking.domain.Warehouse;
import org.acme.orderpicking.dto.input.PickTaskInputDTO;
import org.acme.orderpicking.dto.input.PickingPlanInput;
import org.acme.orderpicking.dto.input.ProductInputDTO;
import org.acme.orderpicking.dto.input.TrolleyInputDTO;
import org.acme.orderpicking.dto.input.WarehouseLocationInputDTO;

/**
 * Builds the demo dataset: 8 orders of a random selection of 40 grocery products, stored per product family in
 * the warehouse, to be picked by 5 trolleys that all start from the same location. Every pick task starts
 * unassigned. The randomness is seeded, so the dataset is the same on every build.
 */
public final class DemoDataBuilder {

    private static final long SEED = 37;
    private static final int TROLLEY_COUNT = 5;
    private static final int BUCKET_COUNT = 4;
    /** A bucket of 60 x 40 x 20 cm. */
    private static final int BUCKET_CAPACITY = 60 * 40 * 20;
    private static final int ORDER_COUNT = 8;
    private static final int ORDER_ITEMS_SIZE_MINIMUM = 1;
    private static final WarehouseLocationInputDTO START_LOCATION =
            new WarehouseLocationInputDTO(Warehouse.shelvingId("A", 1), ShelvingSide.LEFT, 0);

    private enum ProductFamily {
        GENERAL_FOOD,
        FRESH_FOOD,
        MEAT_AND_FISH,
        FROZEN_PRODUCTS,
        FRUITS_AND_VEGETABLES,
        HOUSE_CLEANING,
        DRINKS,
        SNACKS,
        PETS
    }

    /**
     * A product before it gets a place in the warehouse.
     *
     * @param volume in cm3
     */
    private record ProductTemplate(String name, int volume, ProductFamily family) {
    }

    private static final List<ProductTemplate> PRODUCTS = List.of(
            new ProductTemplate("Kelloggs Cornflakes", 30 * 12 * 35, ProductFamily.GENERAL_FOOD),
            new ProductTemplate("Cream Crackers", 23 * 7 * 2, ProductFamily.GENERAL_FOOD),
            new ProductTemplate("Tea Bags 240 packet", 2 * 6 * 15, ProductFamily.GENERAL_FOOD),
            new ProductTemplate("Tomato Soup Can", 10 * 10 * 10, ProductFamily.GENERAL_FOOD),
            new ProductTemplate("Baked Beans in Tomato Sauce", 10 * 10 * 10, ProductFamily.GENERAL_FOOD),
            new ProductTemplate("Classic Mint Sauce", 8 * 10 * 8, ProductFamily.GENERAL_FOOD),
            new ProductTemplate("Raspberry Conserve", 8 * 10 * 8, ProductFamily.GENERAL_FOOD),
            new ProductTemplate("Orange Fine Shred Marmalade", 7 * 8 * 7, ProductFamily.GENERAL_FOOD),

            new ProductTemplate("Free Range Eggs 6 Pack", 15 * 10 * 8, ProductFamily.FRESH_FOOD),
            new ProductTemplate("Mature Cheddar 400G", 10 * 9 * 5, ProductFamily.FRESH_FOOD),
            new ProductTemplate("Butter Packet", 12 * 5 * 5, ProductFamily.FRESH_FOOD),

            new ProductTemplate("Iceberg Lettuce Each", 2500, ProductFamily.FRUITS_AND_VEGETABLES),
            new ProductTemplate("Carrots 1Kg", 1000, ProductFamily.FRUITS_AND_VEGETABLES),
            new ProductTemplate("Organic Fair Trade Bananas 5 Pack", 1800, ProductFamily.FRUITS_AND_VEGETABLES),
            new ProductTemplate("Gala Apple Minimum 5 Pack", 25 * 20 * 10, ProductFamily.FRUITS_AND_VEGETABLES),
            new ProductTemplate("Orange Bag 3kg", 29 * 20 * 15, ProductFamily.FRUITS_AND_VEGETABLES),

            new ProductTemplate("Fairy Non Biological Laundry Liquid 4.55L", 5000, ProductFamily.HOUSE_CLEANING),
            new ProductTemplate("Toilet Tissue 8 Roll White", 50 * 20 * 20, ProductFamily.HOUSE_CLEANING),
            new ProductTemplate("Kitchen Roll 200 Sheets x 2", 30 * 30 * 15, ProductFamily.HOUSE_CLEANING),
            new ProductTemplate("Stainless Steel Cleaner 500Ml", 500, ProductFamily.HOUSE_CLEANING),
            new ProductTemplate("Antibacterial Surface Spray", 12 * 4 * 25, ProductFamily.HOUSE_CLEANING),

            new ProductTemplate("Beef Lean Steak Mince 500g", 500, ProductFamily.MEAT_AND_FISH),
            new ProductTemplate("Smoked Salmon 120G", 150, ProductFamily.MEAT_AND_FISH),
            new ProductTemplate("Steak Burgers 454G", 450, ProductFamily.MEAT_AND_FISH),
            new ProductTemplate("Pork Cooked Ham 125G", 125, ProductFamily.MEAT_AND_FISH),
            new ProductTemplate("Chicken Breast Fillets 300G", 300, ProductFamily.MEAT_AND_FISH),

            new ProductTemplate("6 Milk Bricks Pack", 22 * 16 * 21, ProductFamily.DRINKS),
            new ProductTemplate("Milk Brick", 1232, ProductFamily.DRINKS),
            new ProductTemplate("Skimmed Milk 2.5L", 2500, ProductFamily.DRINKS),
            new ProductTemplate("3L Orange Juice", 3 * 1000, ProductFamily.DRINKS),
            new ProductTemplate("Alcohol Free Beer 4 Pack", 30 * 15 * 30, ProductFamily.DRINKS),
            new ProductTemplate("Pepsi Regular Bottle", 1000, ProductFamily.DRINKS),
            new ProductTemplate("Pepsi Diet 6 x 330ml", 35 * 12 * 12, ProductFamily.DRINKS),
            new ProductTemplate("Schweppes Lemonade 2L", 2000, ProductFamily.DRINKS),
            new ProductTemplate("Coke Zero 8 x 330ml", 40 * 12 * 12, ProductFamily.DRINKS),
            new ProductTemplate("Natural Mineral Water Still 6 X 1.5Ltr", 6 * 1500, ProductFamily.DRINKS),

            new ProductTemplate("Cocktail Crisps 6 Pack", 20 * 10 * 10, ProductFamily.SNACKS));

    private static final Map<ProductFamily, List<String>> SHELVINGS_PER_FAMILY = Map.of(
            ProductFamily.FRUITS_AND_VEGETABLES, List.of("A1", "A2"),
            ProductFamily.FRESH_FOOD, List.of("A3"),
            ProductFamily.MEAT_AND_FISH, List.of("B2", "B3"),
            ProductFamily.FROZEN_PRODUCTS, List.of("B2", "B1"),
            ProductFamily.DRINKS, List.of("D1"),
            ProductFamily.SNACKS, List.of("D2"),
            ProductFamily.GENERAL_FOOD, List.of("B2", "C3", "D2", "D3"),
            ProductFamily.HOUSE_CLEANING, List.of("E2", "E1"),
            ProductFamily.PETS, List.of("E3"));

    private DemoDataBuilder() {
    }

    public static DemoDataBuilder builder() {
        return new DemoDataBuilder();
    }

    public PickingPlanInput build() {
        Random random = new Random(SEED);
        List<ProductInputDTO> products = generateProducts(random);
        List<PickTaskInputDTO> pickTasks = generatePickTasks(products, random);
        return new PickingPlanInput(products, pickTasks, generateTrolleys());
    }

    /**
     * Gives every product a random place on one of the shelvings of its family.
     */
    private static List<ProductInputDTO> generateProducts(Random random) {
        List<ProductInputDTO> products = new ArrayList<>(PRODUCTS.size());
        for (ProductTemplate template : PRODUCTS) {
            List<String> shelvingIds = SHELVINGS_PER_FAMILY.get(template.family());
            String shelvingId = shelvingIds.get(random.nextInt(shelvingIds.size()));
            ShelvingSide side = ShelvingSide.values()[random.nextInt(ShelvingSide.values().length)];
            int row = random.nextInt(Warehouse.SHELVING_ROWS) + 1;
            products.add(new ProductInputDTO(String.valueOf(products.size()), template.name(), template.volume(),
                    new WarehouseLocationInputDTO(shelvingId, side, row)));
        }
        return List.copyOf(products);
    }

    /**
     * Fills every order with a random number of distinct products, one pick task per product.
     */
    private static List<PickTaskInputDTO> generatePickTasks(List<ProductInputDTO> products, Random random) {
        List<PickTaskInputDTO> pickTasks = new ArrayList<>();
        for (int orderNumber = 1; orderNumber <= ORDER_COUNT; orderNumber++) {
            String orderId = Integer.toString(orderNumber);
            int orderItemsSize = ORDER_ITEMS_SIZE_MINIMUM + random.nextInt(products.size() - ORDER_ITEMS_SIZE_MINIMUM);
            Set<String> orderProductIds = new HashSet<>();
            for (int i = 0; i < orderItemsSize; i++) {
                ProductInputDTO product = products.get(random.nextInt(products.size()));
                if (orderProductIds.add(product.id())) {
                    pickTasks.add(new PickTaskInputDTO(orderId + "-" + (orderProductIds.size() - 1), orderId,
                            product.id()));
                }
            }
        }
        return List.copyOf(pickTasks);
    }

    private static List<TrolleyInputDTO> generateTrolleys() {
        List<TrolleyInputDTO> trolleys = new ArrayList<>(TROLLEY_COUNT);
        for (int i = 1; i <= TROLLEY_COUNT; i++) {
            trolleys.add(new TrolleyInputDTO(Integer.toString(i), BUCKET_COUNT, BUCKET_CAPACITY, START_LOCATION, null));
        }
        return List.copyOf(trolleys);
    }
}
