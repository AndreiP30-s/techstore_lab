package com.example.techstore.service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import com.example.techstore.model.product.Product;

@Service
public class DiscountService {
    private final CatalogService catalog;
    private final ParallelPriceService calculator;
    private final Map<Product, BigDecimal> previousPrices = new LinkedHashMap<>();

    public DiscountService(CatalogService catalog, ParallelPriceService calculator) {
        this.catalog = catalog;
        this.calculator = calculator;
    }

    public synchronized boolean isActive() { return !previousPrices.isEmpty(); }

    public synchronized int apply(BigDecimal percent, int threads) {
        if (isActive()) { throw new IllegalArgumentException("Сначала отмените действующую скидку."); }
        if (percent == null || percent.signum() <= 0 || percent.compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("Скидка должна быть больше 0 и не больше 100%.");
        }
        List<Product> products = catalog.findAll();
        if (products.isEmpty()) { throw new IllegalArgumentException("Каталог пуст."); }
        // Сначала рассчитываем все цены, затем применяем скидку.
        List<PriceQuote> quotes = calculator.calculate(products, percent, threads);
        for (int i = 0; i < products.size(); i++) {
            Product product = products.get(i);
            previousPrices.put(product, quotes.get(i).originalPrice());
            product.changePrice(quotes.get(i).discountedPrice());
        }
        return products.size();
    }

    public synchronized int cancel() {
        if (!isActive()) { throw new IllegalArgumentException("Нет действующей скидки."); }
        int restored = 0;
        for (Map.Entry<Product, BigDecimal> entry : previousPrices.entrySet()) {
            Product product = entry.getKey();
            // Проверяем сам объект: артикул мог быть занят другим товаром.
            if (catalog.findByArticle(product.getArticle()).orElse(null) == product) {
                product.changePrice(entry.getValue());
                restored++;
            }
        }
        previousPrices.clear();
        return restored;
    }
}
