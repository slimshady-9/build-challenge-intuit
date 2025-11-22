import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class SalesAnalysisTest {

    public static void main(String[] args) {
        TestRunner.runTests(SalesAnalysisTest.class);
    }

    // --- CsvLoader Tests ---

    public void testCsvLoader_HappyPath() throws IOException {
        createCsv("test_happy.csv",
                "transaction_id,date,product_name,category,region,quantity,unit_price",
                "1,2023-01-01,Laptop,Electronics,North,2,1200.00",
                "2,2023-01-02,T-Shirt,Clothing,South,3,20.00");

        List<Sale> sales = CsvLoader.loadSalesData("test_happy.csv");
        TestRunner.assertEquals(2, sales.size());
        TestRunner.assertEquals("Laptop", sales.get(0).getProductName());
        TestRunner.assertEquals(new BigDecimal("1200.00"), sales.get(0).getUnitPrice());

        new File("test_happy.csv").delete();
    }

    public void testCsvLoader_FlexibleHeaders() throws IOException {
        createCsv("test_flex.csv",
                "region,quantity,unit_price,transaction_id,date,product_name,category",
                "North,2,1200.00,1,2023-01-01,Laptop,Electronics");

        List<Sale> sales = CsvLoader.loadSalesData("test_flex.csv");
        TestRunner.assertEquals(1, sales.size());
        TestRunner.assertEquals("Laptop", sales.get(0).getProductName());
        TestRunner.assertEquals(new BigDecimal("1200.00"), sales.get(0).getUnitPrice());

        new File("test_flex.csv").delete();
    }

    public void testCsvLoader_MissingRequiredHeader() throws IOException {
        createCsv("test_missing.csv",
                "transaction_id,date,product_name,category,region,quantity",
                "1,2023-01-01,Laptop,Electronics,North,2");

        List<Sale> sales = CsvLoader.loadSalesData("test_missing.csv");
        TestRunner.assertTrue(sales.isEmpty(), "Should return empty list when required header is missing");

        new File("test_missing.csv").delete();
    }

    public void testCsvLoader_MalformedData() throws IOException {
        createCsv("test_malformed.csv",
                "transaction_id,date,product_name,category,region,quantity,unit_price",
                "1,2023-01-01,Laptop,Electronics,North,2,1200.00",
                "2,INVALID_DATE,T-Shirt,Clothing,South,3,20.00",
                "3,2023-01-03,Jeans,Clothing,East,1,50.00");

        List<Sale> sales = CsvLoader.loadSalesData("test_malformed.csv");
        TestRunner.assertEquals(2, sales.size());

        new File("test_malformed.csv").delete();
    }

    // --- SalesAnalyzer Tests ---

    public void testAnalyzer_TotalSalesByCategory() {
        List<Sale> sales = List.of(
                new Sale(1, LocalDate.now(), "Laptop", "Electronics", "North", 2, new BigDecimal("1200.00")),
                new Sale(2, LocalDate.now(), "T-Shirt", "Clothing", "South", 3, new BigDecimal("20.00")),
                new Sale(3, LocalDate.now(), "Monitor", "Electronics", "East", 1, new BigDecimal("300.00")));

        SalesAnalyzer analyzer = new SalesAnalyzer(sales);
        Map<String, BigDecimal> result = analyzer.getTotalSalesByCategory();

        TestRunner.assertEquals(new BigDecimal("2700.00"), result.get("Electronics"));
        TestRunner.assertEquals(new BigDecimal("60.00"), result.get("Clothing"));
    }

    public void testAnalyzer_AverageSalesByRegion() {
        List<Sale> sales = List.of(
                new Sale(1, LocalDate.now(), "Laptop", "Electronics", "North", 2, new BigDecimal("1200.00")),
                new Sale(2, LocalDate.now(), "T-Shirt", "Clothing", "North", 3, new BigDecimal("20.00")),
                new Sale(3, LocalDate.now(), "Monitor", "Electronics", "South", 1, new BigDecimal("300.00")));

        SalesAnalyzer analyzer = new SalesAnalyzer(sales);
        Map<String, BigDecimal> result = analyzer.getAverageSalesByRegion();

        TestRunner.assertEquals(new BigDecimal("1230.00"), result.get("North"));
        TestRunner.assertEquals(new BigDecimal("300.00"), result.get("South"));
    }

    public void testAnalyzer_TopSellingProducts() {
        List<Sale> sales = List.of(
                new Sale(1, LocalDate.now(), "Laptop", "Electronics", "North", 5, new BigDecimal("1200.00")),
                new Sale(2, LocalDate.now(), "T-Shirt", "Clothing", "South", 10, new BigDecimal("20.00")),
                new Sale(3, LocalDate.now(), "Monitor", "Electronics", "East", 3, new BigDecimal("300.00")));

        SalesAnalyzer analyzer = new SalesAnalyzer(sales);
        List<Map.Entry<String, Integer>> result = analyzer.getTopSellingProducts(2);

        TestRunner.assertEquals(2, result.size());
        TestRunner.assertEquals("T-Shirt", result.get(0).getKey());
        TestRunner.assertEquals(10, result.get(0).getValue().intValue());
    }

    public void testAnalyzer_SalesTrendByMonth() throws IOException {
        createCsv("test_trend.csv",
                "transaction_id,date,product_name,category,region,quantity,unit_price",
                "1,2023-01-15,Laptop,Electronics,North,2,1200.00",
                "2,2023-01-20,T-Shirt,Clothing,South,3,20.00",
                "3,2023-02-10,Monitor,Electronics,East,1,300.00");

        List<Sale> sales = CsvLoader.loadSalesData("test_trend.csv");
        SalesAnalyzer analyzer = new SalesAnalyzer(sales);
        Map<String, BigDecimal> result = analyzer.getSalesTrendByMonth();

        TestRunner.assertEquals(2, result.size());
        TestRunner.assertTrue(result.containsKey("2023-01"), "Should have January data");
        TestRunner.assertTrue(result.containsKey("2023-02"), "Should have February data");

        new File("test_trend.csv").delete();
    }

    // --- Analyst Scenarios ---

    public void testAnalyst_DuplicateTransactions() {
        List<Sale> sales = List.of(
                new Sale(1, LocalDate.now(), "Laptop", "Electronics", "North", 2, new BigDecimal("1200.00")),
                new Sale(1, LocalDate.now(), "Laptop", "Electronics", "North", 2, new BigDecimal("1200.00")));

        SalesAnalyzer analyzer = new SalesAnalyzer(sales);
        Map<String, BigDecimal> result = analyzer.getTotalSalesByCategory();

        TestRunner.assertEquals(new BigDecimal("4800.00"), result.get("Electronics"));
    }

    public void testAnalyst_NegativeValues() {
        List<Sale> sales = List.of(
                new Sale(1, LocalDate.now(), "Laptop", "Electronics", "North", -2, new BigDecimal("1200.00")),
                new Sale(2, LocalDate.now(), "T-Shirt", "Clothing", "South", 3, new BigDecimal("-20.00")),
                new Sale(3, LocalDate.now(), "Monitor", "Electronics", "East", 1, new BigDecimal("300.00")));

        SalesAnalyzer analyzer = new SalesAnalyzer(sales);
        Map<String, BigDecimal> result = analyzer.getTotalSalesByCategory();

        TestRunner.assertEquals(new BigDecimal("-2100.00"), result.get("Electronics"));
        TestRunner.assertEquals(new BigDecimal("-60.00"), result.get("Clothing"));
    }

    public void testAnalyst_WhitespaceAndCase() {
        List<Sale> sales = List.of(
                new Sale(1, LocalDate.now(), "Laptop", "Electronics", "North", 2, new BigDecimal("1200.00")),
                new Sale(2, LocalDate.now(), "Laptop", "electronics", "North", 1, new BigDecimal("1200.00")));

        SalesAnalyzer analyzer = new SalesAnalyzer(sales);
        Map<String, BigDecimal> result = analyzer.getTotalSalesByCategory();

        TestRunner.assertEquals(2, result.size());
    }

    // --- Finance Scenarios ---

    public void testFinance_Precision() {
        List<Sale> sales = List.of(
                new Sale(1, LocalDate.now(), "A", "C", "R", 1, new BigDecimal("0.10")),
                new Sale(2, LocalDate.now(), "B", "C", "R", 1, new BigDecimal("0.20")));

        SalesAnalyzer analyzer = new SalesAnalyzer(sales);
        Map<String, BigDecimal> result = analyzer.getTotalSalesByCategory();
        BigDecimal total = result.get("C");

        TestRunner.assertEquals(new BigDecimal("0.30"), total);
    }

    public void testFinance_Reconciliation() {
        List<Sale> sales = List.of(
                new Sale(1, LocalDate.now(), "A", "Electronics", "R", 1, new BigDecimal("100.00")),
                new Sale(2, LocalDate.now(), "B", "Clothing", "R", 1, new BigDecimal("50.00")),
                new Sale(3, LocalDate.now(), "C", "Home", "R", 1, new BigDecimal("75.00")));

        SalesAnalyzer analyzer = new SalesAnalyzer(sales);
        Map<String, BigDecimal> byCategory = analyzer.getTotalSalesByCategory();

        BigDecimal categorySum = byCategory.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal grandTotal = sales.stream()
                .map(Sale::getTotalRevenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        TestRunner.assertEquals(grandTotal, categorySum);
    }

    public void testFinance_Refunds() {
        List<Sale> sales = List.of(
                new Sale(1, LocalDate.now(), "A", "C", "R", 5, new BigDecimal("100.00")),
                new Sale(2, LocalDate.now(), "B", "C", "R", -2, new BigDecimal("100.00")));

        SalesAnalyzer analyzer = new SalesAnalyzer(sales);
        Map<String, BigDecimal> result = analyzer.getTotalSalesByCategory();

        TestRunner.assertEquals(new BigDecimal("300.00"), result.get("C"));
    }

    public void testFinance_LargeNumbers() {
        List<Sale> sales = List.of(
                new Sale(1, LocalDate.now(), "A", "C", "R", 100000, new BigDecimal("100.00")),
                new Sale(2, LocalDate.now(), "B", "C", "R", 100000, new BigDecimal("200.00")));

        SalesAnalyzer analyzer = new SalesAnalyzer(sales);
        Map<String, BigDecimal> result = analyzer.getTotalSalesByCategory();

        TestRunner.assertEquals(new BigDecimal("30000000.00"), result.get("C"));
    }

    // --- Intuit Prosperity Features Tests ---

    public void testIntuit_TaxLiability() {
        List<Sale> sales = List.of(
                new Sale(1, LocalDate.now(), "A", "C", "RegionA", 1, new BigDecimal("100.00")));
        SalesAnalyzer analyzer = new SalesAnalyzer(sales);
        Map<String, BigDecimal> tax = analyzer.getTaxLiabilityByRegion(new BigDecimal("0.10"));

        TestRunner.assertEquals(new BigDecimal("10.00"), tax.get("RegionA"));
    }

    public void testIntuit_MoMGrowth() {
        List<Sale> sales = List.of(
                new Sale(1, LocalDate.of(2023, 1, 1), "A", "C", "R", 1, new BigDecimal("100.00")),
                new Sale(2, LocalDate.of(2023, 2, 1), "A", "C", "R", 1, new BigDecimal("110.00")),
                new Sale(3, LocalDate.of(2023, 3, 1), "A", "C", "R", 1, new BigDecimal("110.00")));
        SalesAnalyzer analyzer = new SalesAnalyzer(sales);
        Map<String, BigDecimal> growth = analyzer.getMonthOverMonthGrowth();

        TestRunner.assertEquals(new BigDecimal("10.00"), growth.get("2023-02"));
        TestRunner.assertEquals(new BigDecimal("0.00"), growth.get("2023-03"));
    }

    // Helper
    private void createCsv(String filename, String... lines) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
            for (String line : lines) {
                pw.println(line);
            }
        }
    }
}
