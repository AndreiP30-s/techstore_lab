package com.example.techstore.repository.memory;
import org.springframework.stereotype.Repository;
import com.example.techstore.model.product.Product;
import com.example.techstore.repository.ProductRepository;
@Repository
public class InMemoryProductRepository extends InMemoryRepository<Long, Product> implements ProductRepository {
    private long lastArticle = 100000L;

    /** Не выдаём удалённые номера повторно. Счётчик защищён synchronized. */
    @Override public synchronized long nextArticle() {
        if (lastArticle == Long.MAX_VALUE) {
            throw new IllegalStateException("Закончились доступные артикулы.");
        }
        return ++lastArticle;
    }

    @Override public synchronized void add(Product product) {
        super.add(product);
        lastArticle = Math.max(lastArticle, product.getArticle());
    }
}
