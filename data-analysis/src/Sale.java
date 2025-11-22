
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Represents a single sales transaction.
 * Immutable POJO to ensure thread safety and predictability in stream
 * operations.
 */
public class Sale {
    private final int transactionId;
    private final LocalDate date;
    private final String productName;
    private final String category;
    private final String region;
    private final int quantity;
    private final BigDecimal unitPrice;

    public Sale(int transactionId, LocalDate date, String productName, String category, String region, int quantity,
            BigDecimal unitPrice) {
        this.transactionId = transactionId;
        this.date = date;
        this.productName = productName;
        this.category = category;
        this.region = region;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public int getTransactionId() {
        return transactionId;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getProductName() {
        return productName;
    }

    public String getCategory() {
        return category;
    }

    public String getRegion() {
        return region;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public BigDecimal getTotalRevenue() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Sale{");
        sb.append("id=").append(transactionId)
                .append(", date=").append(date)
                .append(", product='").append(productName).append('\'')
                .append(", category='").append(category).append('\'')
                .append(", region='").append(region).append('\'')
                .append(", qty=").append(quantity)
                .append(", price=").append(unitPrice)
                .append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Sale sale = (Sale) o;
        return transactionId == sale.transactionId &&
                quantity == sale.quantity &&
                Objects.equals(date, sale.date) &&
                Objects.equals(productName, sale.productName) &&
                Objects.equals(category, sale.category) &&
                Objects.equals(region, sale.region) &&
                Objects.equals(unitPrice, sale.unitPrice);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionId, date, productName, category, region, quantity, unitPrice);
    }
}
