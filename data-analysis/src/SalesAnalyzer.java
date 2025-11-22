
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Performs analysis on a list of Sale objects using Java Streams.
 * Demonstrates functional programming principles with immutability and pure
 * functions.
 */
public class SalesAnalyzer {
        private static final Logger LOGGER = Logger.getLogger(SalesAnalyzer.class.getName());
        private final List<Sale> sales;

        public SalesAnalyzer(List<Sale> sales) {
                this.sales = sales;
                LOGGER.info("SalesAnalyzer initialized with " + sales.size() + " records");
        }

        /**
         * 1. Calculate total sales by category.
         * 
         * @return Map of Category -> Total Revenue
         */
        public Map<String, BigDecimal> getTotalSalesByCategory() {
                return sales.stream()
                                .collect(Collectors.groupingBy(
                                                Sale::getCategory,
                                                Collectors.mapping(
                                                                Sale::getTotalRevenue,
                                                                Collectors.reducing(BigDecimal.ZERO,
                                                                                BigDecimal::add))));
        }

        /**
         * 2. Calculate average sales by region.
         * 
         * @return Map of Region -> Average Revenue
         */
        public Map<String, BigDecimal> getAverageSalesByRegion() {
                // Group by region, then map to revenue
                Map<String, List<BigDecimal>> revenuesByRegion = sales.stream()
                                .collect(Collectors.groupingBy(
                                                Sale::getRegion,
                                                Collectors.mapping(Sale::getTotalRevenue, Collectors.toList())));

                // Calculate average using streams
                return revenuesByRegion.entrySet().stream()
                                .collect(Collectors.toMap(
                                                Map.Entry::getKey,
                                                entry -> {
                                                        List<BigDecimal> revenues = entry.getValue();
                                                        if (revenues.isEmpty())
                                                                return BigDecimal.ZERO;
                                                        BigDecimal sum = revenues.stream().reduce(BigDecimal.ZERO,
                                                                        BigDecimal::add);
                                                        return sum.divide(BigDecimal.valueOf(revenues.size()), 2,
                                                                        RoundingMode.HALF_UP);
                                                },
                                                (a, b) -> a,
                                                LinkedHashMap::new));
        }

        /**
         * 3. Identify top-selling products by quantity.
         * 
         * @param n Number of top products to return
         * @return List of Map entries (Product Name -> Total Quantity)
         */
        public List<Map.Entry<String, Integer>> getTopSellingProducts(int n) {
                return sales.stream()
                                .collect(Collectors.groupingBy(
                                                Sale::getProductName,
                                                Collectors.summingInt(Sale::getQuantity)))
                                .entrySet().stream()
                                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                                .limit(n)
                                .collect(Collectors.toList());
        }

        /**
         * 4. Analyze sales trends by month.
         * 
         * @return Map of Month (YYYY-MM) -> Total Revenue
         */
        public Map<String, BigDecimal> getSalesTrendByMonth() {
                DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM");
                return sales.stream()
                                .collect(Collectors.groupingBy(
                                                sale -> sale.getDate().format(monthFormatter),
                                                Collectors.mapping(
                                                                Sale::getTotalRevenue,
                                                                Collectors.reducing(BigDecimal.ZERO,
                                                                                BigDecimal::add))));
        }

        // --- Intuit Prosperity Features ---

        /**
         * TurboTax Feature: Calculate estimated tax liability by region.
         * REFACTORED: Single-pass stream instead of nested streams.
         * 
         * @param taxRate The estimated tax rate (e.g., 0.10 for 10%)
         * @return Map of Region -> Estimated Tax
         */
        public Map<String, BigDecimal> getTaxLiabilityByRegion(BigDecimal taxRate) {
                return sales.stream()
                                .collect(Collectors.groupingBy(
                                                Sale::getRegion,
                                                Collectors.mapping(
                                                                Sale::getTotalRevenue,
                                                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))))
                                .entrySet().stream()
                                .collect(Collectors.toMap(
                                                Map.Entry::getKey,
                                                entry -> entry.getValue().multiply(taxRate).setScale(2,
                                                                RoundingMode.HALF_UP)));
        }

        /**
         * QuickBooks Feature: Calculate Month-over-Month growth percentage.
         * REFACTORED: Uses IntStream instead of imperative for-loop.
         * 
         * @return Map of Month -> Growth Percentage (vs previous month)
         */
        public Map<String, BigDecimal> getMonthOverMonthGrowth() {
                Map<String, BigDecimal> monthlySales = getSalesTrendByMonth();
                List<String> sortedMonths = monthlySales.keySet().stream()
                                .sorted()
                                .collect(Collectors.toList());

                return IntStream.range(1, sortedMonths.size())
                                .boxed()
                                .collect(Collectors.toMap(
                                                i -> sortedMonths.get(i),
                                                i -> calculateGrowthPercentage(
                                                                monthlySales.get(sortedMonths.get(i)),
                                                                monthlySales.get(sortedMonths.get(i - 1))),
                                                (a, b) -> a,
                                                LinkedHashMap::new));
        }

        /**
         * Helper: Calculate growth percentage between two values.
         * Pure function - no side effects.
         */
        private BigDecimal calculateGrowthPercentage(BigDecimal current, BigDecimal previous) {
                if (previous.compareTo(BigDecimal.ZERO) <= 0) {
                        return BigDecimal.ZERO;
                }
                return current.subtract(previous)
                                .divide(previous, 4, RoundingMode.HALF_UP)
                                .multiply(new BigDecimal("100"))
                                .setScale(2, RoundingMode.HALF_UP);
        }

        // --- Higher-Order Functions (Advanced FP) ---

        /**
         * Generic aggregation function - demonstrates higher-order function pattern.
         * Allows custom grouping and mapping strategies.
         * 
         * @param grouper Function to extract grouping key
         * @param mapper  Function to extract value to aggregate
         * @return Map of grouped results
         */
        public Map<String, BigDecimal> analyzeBy(
                        Function<Sale, String> grouper,
                        Function<Sale, BigDecimal> mapper) {
                return sales.stream()
                                .collect(Collectors.groupingBy(
                                                grouper,
                                                Collectors.mapping(
                                                                mapper,
                                                                Collectors.reducing(BigDecimal.ZERO,
                                                                                BigDecimal::add))));
        }

        /**
         * Functional filter - returns new analyzer with filtered data.
         * Demonstrates immutability and composability.
         * 
         * @param start Start date (inclusive)
         * @param end   End date (inclusive)
         * @return New SalesAnalyzer with filtered data
         */
        public SalesAnalyzer filterByDateRange(LocalDate start, LocalDate end) {
                List<Sale> filtered = sales.stream()
                                .filter(s -> !s.getDate().isBefore(start) && !s.getDate().isAfter(end))
                                .collect(Collectors.toList());
                LOGGER.info("Filtered by date range [" + start + " to " + end + "]: " + filtered.size() + " records");
                return new SalesAnalyzer(filtered);
        }

        /**
         * Functional filter by category.
         * 
         * @param category Category to filter by
         * @return New SalesAnalyzer with filtered data
         */
        public SalesAnalyzer filterByCategory(String category) {
                List<Sale> filtered = sales.stream()
                                .filter(s -> s.getCategory().equalsIgnoreCase(category))
                                .collect(Collectors.toList());
                LOGGER.info("Filtered by category '" + category + "': " + filtered.size() + " records");
                return new SalesAnalyzer(filtered);
        }

        /**
         * Get total count of sales records.
         * 
         * @return Number of sales
         */
        public long getCount() {
                return sales.size();
        }
}
