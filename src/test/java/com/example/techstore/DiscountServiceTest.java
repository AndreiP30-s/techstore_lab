package com.example.techstore;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import com.example.techstore.model.product.Headphones;
import com.example.techstore.model.product.HeadphoneType;
import com.example.techstore.repository.memory.InMemoryProductRepository;
import com.example.techstore.service.CatalogService;
import com.example.techstore.service.DiscountService;
import com.example.techstore.service.ParallelPriceService;
import static org.junit.jupiter.api.Assertions.*;

@Timeout(10)
class DiscountServiceTest {
    private final CatalogService catalog = new CatalogService(new InMemoryProductRepository());
    private final DiscountService discounts = new DiscountService(catalog, new ParallelPriceService());

    private Headphones add(long article, String price) {
        Headphones product = new Headphones(article, "Buds", "Brand", new BigDecimal(price),
                HeadphoneType.IN_EAR, true, false, 20, 20000);
        catalog.add(product);
        return product;
    }

    // Применение скидки и точное восстановление цены с копейками.
    @Test
    void appliesPricesAndRestoresExactOriginalValues() {
        Headphones first = add(100001L, "99.99");
        Headphones second = add(100002L, "0.05");
        assertEquals(2, discounts.apply(new BigDecimal("50"), 2));
        assertTrue(discounts.isActive());
        assertEquals(new BigDecimal("50.00"), catalog.findByArticle(100001L).orElseThrow().getPrice());
        assertEquals(new BigDecimal("0.03"), second.getPrice());
        assertEquals(2, discounts.cancel());
        assertEquals(new BigDecimal("99.99"), first.getPrice());
        assertEquals(new BigDecimal("0.05"), second.getPrice());
        assertFalse(discounts.isActive());
    }

    // Скидки не складываются; после отмены можно применить новую.
    @Test
    void preventsStackingAndAllowsNewDiscountAfterCancellation() {
        Headphones product = add(100001L, "100");
        discounts.apply(BigDecimal.TEN, 2);
        assertThrows(IllegalArgumentException.class, () -> discounts.apply(BigDecimal.TEN, 2));
        assertEquals(new BigDecimal("90.00"), product.getPrice());
        discounts.cancel();
        discounts.apply(new BigDecimal("100"), 1);
        assertEquals(new BigDecimal("0.00"), product.getPrice());
        discounts.cancel();
        assertEquals(new BigDecimal("100.00"), product.getPrice());
        assertThrows(IllegalArgumentException.class, discounts::cancel);
    }

    // Отмена не затрагивает новые товары и не возвращает удалённые.
    @Test
    void cancellationSkipsDeletedAndNewProductsEvenWithReusedArticle() {
        add(100001L, "100");
        Headphones retained = add(100002L, "50");
        discounts.apply(BigDecimal.TEN, 2);
        catalog.delete(100001L);
        Headphones replacement = add(100001L, "200");
        Headphones added = add(100003L, "300");
        assertEquals(1, discounts.cancel());
        assertEquals(new BigDecimal("50.00"), retained.getPrice());
        assertEquals(new BigDecimal("200.00"), replacement.getPrice());
        assertEquals(new BigDecimal("300.00"), added.getPrice());
    }

    // Ошибочный запрос не меняет цены и состояние скидки.
    @Test
    void invalidRequestsLeavePricesAndStateUntouched() {
        assertThrows(IllegalArgumentException.class, () -> discounts.apply(BigDecimal.TEN, 2));
        Headphones product = add(100001L, "100");
        for (String value : List.of("0", "-1", "101")) {
            assertThrows(IllegalArgumentException.class, () -> discounts.apply(new BigDecimal(value), 2));
        }
        assertThrows(IllegalArgumentException.class, () -> discounts.apply(BigDecimal.TEN, 0));
        assertFalse(discounts.isActive());
        assertEquals(new BigDecimal("100.00"), product.getPrice());
    }

    // Прерванный расчёт не меняет цены.
    @Test
    void interruptedCalculationDoesNotPartiallyApplyDiscount() {
        Headphones product = add(100001L, "100");
        Thread.currentThread().interrupt();
        try {
            assertThrows(IllegalStateException.class, () -> discounts.apply(BigDecimal.TEN, 2));
            assertTrue(Thread.currentThread().isInterrupted());
            assertFalse(discounts.isActive());
            assertEquals(new BigDecimal("100.00"), product.getPrice());
        } finally { Thread.interrupted(); }
    }

    // Из двух одновременных запросов скидку применяет один.
    @Test
    void concurrentRequestsApplyOnlyOneDiscount() throws Exception {
        Headphones product = add(100001L, "100");
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Callable<Boolean> apply = () -> {
                try { discounts.apply(BigDecimal.TEN, 2); return true; }
                catch (IllegalArgumentException exception) { return false; }
            };
            int successes = 0;
            for (Future<Boolean> result : executor.invokeAll(List.of(apply, apply))) {
                if (result.get()) { successes++; }
            }
            assertEquals(1, successes);
            assertEquals(new BigDecimal("90.00"), product.getPrice());
        } finally { executor.shutdownNow(); }
    }
}
