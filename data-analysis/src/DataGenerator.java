
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Generates realistic sales data for testing.
 */
public class DataGenerator {
    private static final Logger LOGGER = Logger.getLogger(DataGenerator.class.getName());
    private static final String[] PRODUCTS = {
            "Laptop", "Electronics", "1200.00",
            "Smartphone", "Electronics", "800.00",
            "Headphones", "Electronics", "150.00",
            "Monitor", "Electronics", "300.00",
            "Keyboard", "Electronics", "50.00",
            "Mouse", "Electronics", "30.00",
            "T-Shirt", "Clothing", "20.00",
            "Jeans", "Clothing", "50.00",
            "Sneakers", "Clothing", "80.00",
            "Backpack", "Accessories", "60.00",
            "Coffee Maker", "Home", "100.00",
            "Blender", "Home", "80.00",
            "Desk Lamp", "Home", "40.00"
    };

    private static final String[] REGIONS = { "North", "South", "East", "West" };

    public static void generateSalesData(String filename, int numRecords) {
        LOGGER.info("Starting data generation: " + numRecords + " records to " + filename);
        Random random = new Random();
        LocalDate startDate = LocalDate.of(2023, 1, 1);

        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println("transaction_id,date,product_name,category,region,quantity,unit_price");

            for (int i = 1; i <= numRecords; i++) {
                int productIndex = random.nextInt(PRODUCTS.length / 3) * 3;
                String productName = PRODUCTS[productIndex];
                String category = PRODUCTS[productIndex + 1];
                double basePrice = Double.parseDouble(PRODUCTS[productIndex + 2]);

                String region = REGIONS[random.nextInt(REGIONS.length)];
                int quantity = random.nextInt(5) + 1;
                double unitPrice = Math.round(basePrice * (0.9 + random.nextDouble() * 0.2) * 100.0) / 100.0;
                LocalDate date = startDate.plusDays(random.nextInt(365));

                writer.printf("%d,%s,%s,%s,%s,%d,%.2f%n",
                        i, date, productName, category, region, quantity, unitPrice);
            }
            LOGGER.info("Successfully generated " + numRecords + " records in " + filename);
            System.out.println("Successfully generated " + numRecords + " records in " + filename);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error generating data file: " + filename, e);
            e.printStackTrace();
        }
    }
}
