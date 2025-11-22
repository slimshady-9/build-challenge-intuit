import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class to load sales data from a CSV file.
 */
public class CsvLoader {
    private static final Logger LOGGER = Logger.getLogger(CsvLoader.class.getName());
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Reads a CSV file and converts it into a list of Sale objects.
     * Handles flexible column ordering and extra columns.
     *
     * @param filepath The path to the CSV file.
     * @return A list of Sale objects.
     */
    public static List<Sale> loadSalesData(String filepath) {
        List<Sale> sales = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filepath))) {
            String headerLine = br.readLine();
            if (headerLine == null) {
                LOGGER.severe("CSV file is empty");
                return sales;
            }

            // Map header names to indices
            String[] headers = headerLine.split(",");
            Map<String, Integer> headerMap = new HashMap<>();
            for (int i = 0; i < headers.length; i++) {
                headerMap.put(headers[i].trim(), i);
            }

            // Validate required headers
            String[] requiredHeaders = { "transaction_id", "date", "product_name", "category", "region", "quantity",
                    "unit_price" };
            for (String required : requiredHeaders) {
                if (!headerMap.containsKey(required)) {
                    LOGGER.severe("Missing required header: " + required);
                    return sales;
                }
            }

            String line;
            while ((line = br.readLine()) != null) {
                try {
                    // Handle potential commas in quoted fields? For simplicity, assuming standard
                    // CSV without quoted commas for now
                    // as per the generator. If needed, a regex or CSV library would be better.
                    String[] values = line.split(",");

                    // Ensure we have enough columns for the max index we need
                    if (values.length <= Collections.max(headerMap.values())) {
                        LOGGER.warning("Skipping malformed line (not enough columns): " + line);
                        continue;
                    }

                    Sale sale = new Sale(
                            Integer.parseInt(values[headerMap.get("transaction_id")]),
                            LocalDate.parse(values[headerMap.get("date")], DATE_FORMATTER),
                            values[headerMap.get("product_name")],
                            values[headerMap.get("category")],
                            values[headerMap.get("region")],
                            Integer.parseInt(values[headerMap.get("quantity")]),
                            new BigDecimal(values[headerMap.get("unit_price")]));
                    sales.add(sale);
                } catch (Exception e) {
                    // Log the failure without dumping the full stack trace to keep test output clean
                    LOGGER.log(Level.WARNING, "Error parsing line (skipped): " + line + " | " + e.getMessage());
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error reading file: " + filepath, e);
        }
        return sales;
    }
}
