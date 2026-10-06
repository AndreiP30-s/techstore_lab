package com.example.techstore.service;
import org.springframework.stereotype.Service;
import com.example.techstore.model.Validation;
import com.example.techstore.model.product.Product;
import com.example.techstore.model.product.ProductCategory;
import com.example.techstore.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.Collections;
@Service
public class CatalogService {
    private final ProductRepository repository;
    public CatalogService(ProductRepository repository) { this.repository = repository; }
    public void add(Product product) { repository.add(product); }
    public List<Product> findAll() { return repository.findAll(); }
    public Optional<Product> findByArticle(long article) { return repository.findById(Validation.article(article)); }
    public boolean delete(long article) { return repository.deleteById(Validation.article(article)); }
    public long nextArticle() { return repository.nextArticle(); }
    public List<Product> search(String query) {
        String normalized = Objects.requireNonNull(query, "query").strip().toLowerCase(Locale.ROOT);
        List<Product> result = new ArrayList<>();
        for (Product product : repository.findAll()) {
            if (Long.toString(product.getArticle()).contains(normalized)
                    || product.getName().toLowerCase(Locale.ROOT).contains(normalized)
                    || product.getBrand().toLowerCase(Locale.ROOT).contains(normalized)) {
                result.add(product);
            }
        }
        return List.copyOf(result);
    }
    public List<Product> filter(ProductCategory category, BigDecimal min, BigDecimal max) {
        Objects.requireNonNull(category, "category");
        Objects.requireNonNull(min, "min"); Objects.requireNonNull(max, "max");
        if (min.signum() < 0 || min.compareTo(max) > 0) { throw new IllegalArgumentException("Invalid price range"); }
        List<Product> result = new ArrayList<>();
        for (Product product : repository.findAll()) {
            if (product.getCategory() == category && product.getPrice().compareTo(min) >= 0
                    && product.getPrice().compareTo(max) <= 0) { result.add(product); }
        }
        return List.copyOf(result);
    }
    public Set<String> brands() {
        Set<String> brands = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (Product product : repository.findAll()) { brands.add(product.getBrand()); }
        return Collections.unmodifiableSet(brands);
    }
}
