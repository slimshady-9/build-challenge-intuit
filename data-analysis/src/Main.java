
import java.io.File;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Main entry point for the Sales Analysis application.
 * Demonstrates functional programming with reusable report generation.
 */
public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        String filePath = determineFilePath(args);

        // Load Data
        LOGGER.info("Loading data from " + filePath + "...");
        System.out.println("Loading data from " + filePath + "...");
        List<Sale> sales = CsvLoader.loadSalesData(filePath);

        if (sales.isEmpty()) {
            LOGGER.severe("No valid sales data found in file: " + filePath);
            System.err.println(
                    "Error: No valid sales data found. Please check the file path and ensure required headers are present.");
            return;
        }

        LOGGER.info("Successfully loaded " + sales.size() + " sales records");
        SalesAnalyzer analyzer = new SalesAnalyzer(sales);

        System.out.println("\n--- Sales Analysis Report ---\n");

        // Core Analytics
        printCurrencyReport("1. Total Sales by Category:", analyzer.getTotalSalesByCategory());
        printCurrencyReport("\n2. Average Sales by Region:", analyzer.getAverageSalesByRegion());
        printTopProducts("\n3. Top 5 Selling Products (by Quantity):", analyzer.getTopSellingProducts(5));
        printCurrencyReport("\n4. Sales Trend by Month:", analyzer.getSalesTrendByMonth());

        // Intuit Prosperity Insights
        System.out.println("\n=========================================");
        System.out.println("   INTUIT PROSPERITY INSIGHTS");
        System.out.println("=========================================");

        printCurrencyReport("\n[TurboTax] Estimated Tax Liability (10% Rate):",
                analyzer.getTaxLiabilityByRegion(new BigDecimal("0.10")));
        printPercentageReport("\n[QuickBooks] Month-over-Month Growth:", analyzer.getMonthOverMonthGrowth());
    }

    /**
     * Functional helper: Determine file path with default fallback.
     */
    private static String determineFilePath(String[] args) {
        if (args.length > 0) {
            LOGGER.info("Using provided file path: " + args[0]);
            return args[0];
        }

        System.out.println("Usage: java Main <path_to_csv>");
        System.out.println("No file provided. Using default 'sales_data.csv' for demonstration.");
        String defaultPath = "sales_data.csv";

        File file = new File(defaultPath);
        if (!file.exists()) {
            LOGGER.info("Default file not found. Generating sample data...");
            System.out.println("Generating default data...");
            DataGenerator.generateSalesData(defaultPath, 1000);
        }

        return defaultPath;
    }

    /**
     * Functional report generator: Currency values sorted by value descending.
     */
    private static void printCurrencyReport(String title, Map<String, BigDecimal> data) {
        System.out.println(title);
        data.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEach(e -> System.out.printf("  - %s: $%.2f%n", e.getKey(), e.getValue()));
    }

    /**
     * Functional report generator: Percentage values.
     */
    private static void printPercentageReport(String title, Map<String, BigDecimal> data) {
        System.out.println(title);
        data.entrySet().stream()
                .forEach(e -> System.out.printf("  - %s: %+.2f%%%n", e.getKey(), e.getValue()));
    }

    /**
     * Functional report generator: Top products by quantity.
     */
    private static void printTopProducts(String title, List<Map.Entry<String, Integer>> products) {
        System.out.println(title);
        products.forEach(entry -> System.out.printf("  - %s: %d units%n", entry.getKey(), entry.getValue()));
    }
}
