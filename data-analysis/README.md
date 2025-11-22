# Intuit Sales Analysis Platform (Java)

A robust, enterprise-grade financial analysis tool designed to provide small businesses with actionable insights. Built with **Java 8 Streams** and **BigDecimal precision**, it aligns with Intuit's mission to empower customers with data-driven decision-making capabilities.

## Features

### 1. Intuit Prosperity Insights
Delivering value across the Intuit ecosystem:
- **TurboTax Integration**: Automatically estimates tax liability by region to help businesses prepare for tax season.
- **QuickBooks Growth Tracking**: Calculates Month-over-Month (MoM) revenue growth to answer the critical question: "Is my business growing?"

### 2. Financial Precision
- **BigDecimal Architecture**: All monetary calculations use `BigDecimal` to ensure 100% accuracy, eliminating floating-point errors common in standard floating-point arithmetic.
- **Precision Guarantees**: Handles the "penny problem" (0.1 + 0.2 = 0.3) correctly, critical for financial reconciliation.

### 3. Functional Programming Excellence
- **Pure Functions**: Immutable data structures and side-effect-free operations
- **Stream Operations**: Advanced use of Java 8 Streams with `groupingBy`, `mapping`, `reducing`
- **Higher-Order Functions**: Composable filters and generic aggregation functions
- **Performance Optimized**: Single-pass O(n) algorithms instead of nested O(n²) operations

### 4. Production-Ready Code Quality
- **Professional Logging**: Comprehensive logging with `java.util.logging.Logger`
- **Error Handling**: Graceful degradation with malformed data
- **Flexible CSV Parsing**: Dynamic header mapping supports any column order
- **Comprehensive Testing**: 17 test cases covering happy paths, edge cases, and finance scenarios

## Basic Workflow

```
┌─────────────┐
│  CSV File   │ (sales_data.csv with transaction records)
└──────┬──────┘
       │
       │ CsvLoader.loadSalesData()
       │ - Dynamic header mapping
       │ - Validates required fields
       ▼
┌──────────────────┐
│   List<Sale>     │ (Immutable POJOs with BigDecimal)
└──────┬───────────┘
       │
       │ new SalesAnalyzer(sales)
       ▼
┌─────────────────────────────────────────────────────┐
│              SalesAnalyzer (Stream Pipeline)        │
│  ┌────────────────────────────────────────────┐     │
│  │  groupingBy() → mapping() → reducing()     │     │
│  └────────────────────────────────────────────┘     │
└──────┬──────────────────────────────────────────────┘
       │
       │ Analysis Methods
       ├──► getTotalSalesByCategory()
       ├──► getAverageSalesByRegion()
       ├──► getTopSellingProducts()
       ├──► getSalesTrendByMonth()
       ├──► getTaxLiabilityByRegion()  [TurboTax]
       └──► getMonthOverMonthGrowth()  [QuickBooks]
       │
       ▼
┌──────────────────────┐
│  Prosperity Report   │ (Console output with insights)
└──────────────────────┘
```

### Step-by-Step Execution Flow

1. **Initialization**:
   - Determine file path (from args or default)
   - Generate sample data if needed (1000 records)
   - Configure logging

2. **Data Loading**:
   - Parse CSV header and create column index map
   - Validate required headers exist
   - Parse each row into `Sale` objects (skip malformed rows)
   - Initialize `SalesAnalyzer` with validated data

3. **Analysis Phase**:
   - Execute stream-based aggregations (category, region, product, time)
   - Calculate Intuit-specific metrics (tax liability, MoM growth)
   - Sort and format results

4. **Report Generation**:
   - Display core analytics (sales by category, region, products, trends)
   - Display Intuit Prosperity Insights (TurboTax tax estimates, QuickBooks growth)
   - Format with currency and percentage symbols

## Project Structure

```
data-analysis/
├── src/
│   ├── Main.java              # Entry point with functional report generation
│   ├── Sale.java              # Immutable POJO with BigDecimal precision
│   ├── SalesAnalyzer.java     # Core analysis engine using Java Streams
│   ├── CsvLoader.java         # Flexible CSV parser with error handling
│   └── DataGenerator.java     # Test data generator
├── test/
│   ├── TestRunner.java        # Dependency-free test runner
│   └── SalesAnalysisTest.java # 17 comprehensive test cases
├── bin/                       # Compiled classes (not committed)
└── README.md                  # This file
```

## How to Run

### Prerequisites
- **Java 8 or higher** (uses Java 8 Streams API)
- No external dependencies required

### Basic Usage

1. **Navigate to the project directory:**
   ```bash
   cd data-analysis
   ```

2. **Compile the application:**
   ```bash
   javac -d bin src/*.java
   ```

3. **Run with default data (auto-generates 1000 records):**
   ```bash
   java -cp bin Main
   ```

4. **Run with your own CSV file:**
   ```bash
   java -cp bin Main path/to/your/sales_data.csv
   ```

### Running Tests

```bash
# From project root
javac -d bin src/*.java test/*.java

# Run all 17 tests
java -cp bin SalesAnalysisTest
```

**Expected output:**
```
Running tests for: SalesAnalysisTest
--------------------------------------------------
Running testCsvLoader_HappyPath... PASSED
Running testCsvLoader_FlexibleHeaders... PASSED
...
Summary: 17 run, 17 passed, 0 failed.
```

## CSV File Format

### Required Headers
Your CSV file must include these headers (order doesn't matter):
- `transaction_id` - Unique transaction identifier (integer)
- `date` - Transaction date in YYYY-MM-DD format
- `product_name` - Name of the product
- `category` - Product category
- `region` - Sales region
- `quantity` - Number of units sold (integer)
- `unit_price` - Price per unit (decimal)

### Example CSV
```csv
transaction_id,date,product_name,category,region,quantity,unit_price
1,2023-01-15,Laptop,Electronics,North,2,1200.00
2,2023-01-16,T-Shirt,Clothing,South,3,20.00
3,2023-02-10,Coffee Maker,Home,East,1,100.00
```

### Flexible Column Ordering
The CSV loader dynamically maps headers, so columns can be in any order:
```csv
region,quantity,unit_price,transaction_id,date,product_name,category
North,2,1200.00,1,2023-01-15,Laptop,Electronics
```

## Analysis Reports

The application generates a comprehensive **Prosperity Report** with the following sections:

### 1. Total Sales by Category
Aggregates revenue by product category, sorted by highest revenue.
```
1. Total Sales by Category:
  - Electronics: $581,433.09
  - Home: $53,380.70
  - Clothing: $32,295.52
```

### 2. Average Sales by Region
Calculates average transaction value per region.
```
2. Average Sales by Region:
  - East: $725.42
  - North: $682.30
  - West: $667.97
```

### 3. Top Selling Products
Identifies best-selling products by quantity.
```
3. Top 5 Selling Products (by Quantity):
  - Coffee Maker: 265 units
  - Desk Lamp: 257 units
  - T-Shirt: 249 units
```

### 4. Sales Trend by Month
Tracks revenue trends over time.
```
4. Sales Trend by Month:
  - 2023-01: $71,825.47
  - 2023-02: $70,295.77
  - 2023-03: $56,848.56
```

### 5. [TurboTax] Tax Liability Estimation
Calculates estimated sales tax by region (default 10% rate).
```
[TurboTax] Estimated Tax Liability (10% Rate):
  - East: $19,150.97
  - West: $17,634.51
  - North: $17,193.84
```

### 6. [QuickBooks] Month-over-Month Growth
Measures business growth with percentage change metrics.
```
[QuickBooks] Month-over-Month Growth:
  - 2023-02: -2.13%
  - 2023-03: -19.13%
  - 2023-04: -14.92%
  - 2023-05: +10.61%
```

## Design Decisions

### 1. BigDecimal for Financial Precision
**Problem:** Java's `double` type introduces rounding errors:
```java
double result = 0.1 + 0.2;  // 0.30000000000000004 X
```

**Solution:** Use `BigDecimal` for all monetary calculations:
```java
BigDecimal result = new BigDecimal("0.1").add(new BigDecimal("0.2"));  // 0.3 -
```

### 2. Functional Programming with Streams
**Single-Pass Aggregation:**
```java
// O(n) - Efficient single-pass grouping and reduction
public Map<String, BigDecimal> getTotalSalesByCategory() {
    return sales.stream()
        .collect(Collectors.groupingBy(
            Sale::getCategory,
            Collectors.mapping(
                Sale::getTotalRevenue,
                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))));
}
```

**Composable Filters:**
```java
// Chain operations functionally
analyzer.filterByDateRange(start, end)
        .filterByCategory("Electronics")
        .getTotalSalesByCategory();
```

### 3. Immutability
All `Sale` objects are immutable with `final` fields, ensuring thread safety and predictability in stream operations.

### 4. Error Handling
- **Malformed data**: Skipped with warning logs
- **Missing headers**: Returns empty list with error log
- **Invalid dates/numbers**: Logged and skipped, processing continues

## Test Coverage

### Test Categories (17 Total)

**CSV Loader Tests (4):**
- - Happy path with valid data
- - Flexible header ordering
- - Missing required headers
- - Malformed data handling

**Core Analytics Tests (4):**
- - Total sales by category
- - Average sales by region
- - Top selling products
- - Sales trend by month

**Analyst Scenarios (3):**
- - Duplicate transactions
- - Negative values (refunds)
- - Whitespace and case sensitivity

**Finance Scenarios (4):**
- - BigDecimal precision (0.1 + 0.2 = 0.3)
- - Reconciliation (sum of categories = grand total)
- - Refund handling (negative quantities)
- - Large numbers (millions)

**Intuit Features (2):**
- - Tax liability calculation
- - Month-over-Month growth

## Logging

The application uses `java.util.logging.Logger` with three levels:

### Log Levels
- **INFO**: Normal operations (data loading, initialization, filtering)
- **WARNING**: Recoverable issues (malformed lines, parsing errors)
- **SEVERE**: Critical errors (missing headers, file not found)

### Example Logs
```
Nov 21, 2025 6:10:36 PM Main main
INFO: Loading data from sales_data.csv...

Nov 21, 2025 6:10:36 PM Main main
INFO: Successfully loaded 1000 sales records

Nov 21, 2025 6:10:36 PM SalesAnalyzer <init>
INFO: SalesAnalyzer initialized with 1000 records
```

## Advanced Features

### Higher-Order Functions
Generic aggregation with custom grouping and mapping:
```java
// Flexible analysis by any dimension
Map<String, BigDecimal> result = analyzer.analyzeBy(
    Sale::getRegion,           // Group by region
    Sale::getTotalRevenue      // Aggregate revenue
);
```

### Composable Filters
Chain filters to create complex queries:
```java
SalesAnalyzer filtered = analyzer
    .filterByDateRange(LocalDate.of(2023, 1, 1), LocalDate.of(2023, 3, 31))
    .filterByCategory("Electronics");

Map<String, BigDecimal> q1Electronics = filtered.getTotalSalesByCategory();
```

## Future Enhancements

### Generative AI Integration
**Vision:** Natural language analysis of sales data

**Planned Features:**
- **Natural Language Queries**: "What were my top-selling products in Q1?"
- **Automated Insights**: AI-generated business recommendations
- **Trend Prediction**: ML-based forecasting for future sales
- **Anomaly Detection**: Identify unusual patterns or outliers

**Example Interaction:**
```
User: "Show me regions with declining sales"
AI: "The South region shows a 15% decline in Q2. Consider targeted promotions."
```

## Assumptions

1. **CSV Format**: Standard comma-separated values with header row
2. **Date Format**: ISO 8601 (YYYY-MM-DD)
3. **Numeric Precision**: Unit prices can have up to 2 decimal places
4. **Data Integrity**: Transaction IDs are unique (duplicates are counted separately)
5. **Negative Values**: Supported for refunds/returns (negative quantity or price)
6. **Case Sensitivity**: Categories and regions are case-sensitive
7. **Memory**: Dataset fits in memory (suitable for small to medium businesses)

## Challenges Addressed

### 1. Floating-Point Precision
**Challenge:** Standard `double` arithmetic introduces rounding errors in financial calculations.

**Solution:** Implemented `BigDecimal` throughout the codebase with proper scale and rounding modes.

### 2. Performance with Large Datasets
**Challenge:** Naive nested loops would be O(n²) for aggregations.

**Solution:** Single-pass stream operations with efficient collectors, achieving O(n) complexity.

### 3. Flexible Data Formats
**Challenge:** CSV files may have columns in different orders or extra columns.

**Solution:** Dynamic header mapping that validates required fields and ignores extras.

### 4. Graceful Error Handling
**Challenge:** Malformed data shouldn't crash the entire analysis.

**Solution:** Row-level error handling with logging, allowing valid data to be processed.

### 5. Code Maintainability
**Challenge:** Complex business logic can become difficult to understand and modify.

**Solution:** Functional programming with pure functions, immutability, and composable operations.

## Performance Characteristics

| Operation | Time Complexity | Space Complexity |
|-----------|----------------|------------------|
| Load CSV | O(n) | O(n) |
| Total Sales by Category | O(n) | O(k) where k = categories |
| Average Sales by Region | O(n) | O(r) where r = regions |
| Top Selling Products | O(n log n) | O(p) where p = products |
| Sales Trend by Month | O(n) | O(m) where m = months |
| Tax Liability | O(n) | O(r) |
| MoM Growth | O(m log m) | O(m) |

## Contributing

This project follows industry best practices:
- - **Code Style**: 4-space indentation, K&R brace style
- - **Documentation**: Javadoc on all public methods
- - **Testing**: Comprehensive test coverage
- - **Version Control**: `.gitignore` excludes build artifacts
- - **Logging**: Professional logging throughout

