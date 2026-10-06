package com.example.techstore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import com.example.techstore.model.product.Product;
import com.example.techstore.model.product.Headphones;
import com.example.techstore.service.ParallelPriceService;
import com.example.techstore.service.PriceQuote;
import com.example.techstore.model.product.HeadphoneType;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;
@Timeout(10)
class ParallelPriceServiceTest {
    private final ParallelPriceService service = new ParallelPriceService();
    private Product product(String price) {
        return new Headphones(TestArticles.next(), "Buds", "Brand", new BigDecimal(price), HeadphoneType.IN_EAR, true, false, 20, 20000);
    }
    // 12 товаров обрабатываются в трёх потоках без изменения исходных цен.
    @Test void processesEveryProductOnMultipleNamedThreadsWithoutChangingPrices() {
        List<Product> products = new ArrayList<>();
        for (int i = 0; i < 12; i++) { products.add(product("99.99")); }
        List<PriceQuote> quotes = service.calculate(products, BigDecimal.TEN, 3);
        Set<String> threads = new HashSet<>();
        Set<Long> ids = new HashSet<>();
        assertEquals(products.size(), quotes.size());
        for (int i = 0; i < quotes.size(); i++) {
            PriceQuote quote = quotes.get(i);
            assertEquals(products.get(i).getArticle(), quote.article());
            assertEquals(new BigDecimal("89.99"), quote.discountedPrice());
            assertEquals(new BigDecimal("99.99"), products.get(i).getPrice());
            assertTrue(quote.threadName().startsWith("price-worker-"));
            threads.add(quote.threadName()); ids.add(quote.article());
        }
        assertEquals(3, threads.size());
        assertEquals(products.size(), ids.size());
    }
    // Пустой список, крайние значения скидки и округление.
    @Test void handlesEmptyCollectionAndDiscountBoundaries() {
        assertTrue(service.calculate(List.of(), BigDecimal.TEN, 2).isEmpty());
        Product item = product("0.05");
        assertEquals(new BigDecimal("0.03"), service.calculate(List.of(item), new BigDecimal("50"), 1).get(0).discountedPrice());
        assertEquals(new BigDecimal("0.05"), service.calculate(List.of(item), BigDecimal.ZERO, 1).get(0).discountedPrice());
        assertEquals(new BigDecimal("0.00"), service.calculate(List.of(item), new BigDecimal("100"), 1).get(0).discountedPrice());
    }
    // Неверный процент и число потоков отклоняются.
    @Test void rejectsInvalidArguments() {
        assertThrows(IllegalArgumentException.class, () -> service.calculate(List.of(), new BigDecimal("101"), 2));
        assertThrows(IllegalArgumentException.class, () -> service.calculate(List.of(), new BigDecimal("-1"), 2));
        assertThrows(IllegalArgumentException.class, () -> service.calculate(List.of(), BigDecimal.TEN, 0));
        assertThrows(IllegalArgumentException.class, () -> service.calculate(List.of(), BigDecimal.TEN, 33));
    }
    // Флаг прерывания сохраняется после ошибки расчёта.
    @Test void preservesInterruptFlag() {
        Thread.currentThread().interrupt();
        try {
            assertThrows(IllegalStateException.class, () -> service.calculate(List.of(product("1")), BigDecimal.TEN, 1));
            assertTrue(Thread.currentThread().isInterrupted());
        } finally { Thread.interrupted(); }
    }
}
