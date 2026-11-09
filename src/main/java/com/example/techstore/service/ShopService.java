package com.example.techstore.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import com.example.techstore.model.product.Product;
import com.example.techstore.model.product.ProductCategory;
import com.example.techstore.model.user.UserRole;

@Service
public class ShopService {
    private final SessionService session;
    private final CatalogService catalog;
    private final ParallelPriceService prices;
    private final DiscountService discounts;

    public ShopService(SessionService session, CatalogService catalog, ParallelPriceService prices, DiscountService discounts) {
        this.session = session; this.catalog = catalog; this.prices = prices; this.discounts = discounts;
    }

    public void requireManagement() { session.requireRole(UserRole.ADMIN, UserRole.MANAGER); }
    public List<Product> findAll() { session.requireUser(); return catalog.findAll(); }
    public List<Product> search(String query) { session.requireUser(); return catalog.search(query); }
    public List<Product> filter(ProductCategory category, BigDecimal min, BigDecimal max) {
        session.requireUser(); return catalog.filter(category, min, max);
    }
    public Set<String> brands() { session.requireUser(); return catalog.brands(); }
    public long nextArticle() { requireManagement(); return catalog.nextArticle(); }
    public void add(Product product) { requireManagement(); catalog.add(product); }
    public boolean delete(long article) { requireManagement(); return catalog.delete(article); }
    public List<PriceQuote> calculate(List<Product> products, BigDecimal discount, int threads) {
        requireManagement(); return prices.calculate(products, discount, threads);
    }
    public boolean hasActiveDiscount() { requireManagement(); return discounts.isActive(); }
    public int applyDiscount(BigDecimal percent, int threads) {
        requireManagement(); return discounts.apply(percent, threads);
    }
    public int cancelDiscount() { requireManagement(); return discounts.cancel(); }
}
