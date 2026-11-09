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
import static org.junit.jupiter.api.Assertions.*;

@Timeout(10)
class ProductArticleTest {
    private Headphones product(long article) {
        return new Headphones(article, "Sound", "Brand", BigDecimal.TEN,
                HeadphoneType.IN_EAR, true, false, 20, 20000);
    }

    // Артикул используется как ключ хранения и поиска.
    @Test
    void numericArticleIsTheOnlyRepositoryKey() {
        InMemoryProductRepository repository = new InMemoryProductRepository();
        CatalogService catalog = new CatalogService(repository);
        Headphones product = product(100001L);
        catalog.add(product);
        assertEquals(100001L, product.getArticle());
        assertEquals(product.getArticle(), product.getId());
        assertSame(product, repository.findById(100001L).orElseThrow());
        assertSame(product, catalog.findByArticle(100001L).orElseThrow());
        assertEquals(List.of(product), catalog.search(" 100001 "));
        assertTrue(catalog.findByArticle(100002L).isEmpty());
    }

    // Один артикул нельзя добавить дважды даже из разных потоков.
    @Test
    void concurrentDuplicateArticlesCreateOnlyOneProduct() throws Exception {
        InMemoryProductRepository repository = new InMemoryProductRepository();
        ExecutorService executor = Executors.newFixedThreadPool(3);
        try {
            Callable<Boolean> add = () -> {
                try { repository.add(product(100001L)); return true; }
                catch (IllegalArgumentException exception) { return false; }
            };
            int successes = 0;
            for (Future<Boolean> result : executor.invokeAll(List.of(add, add, add))) {
                if (result.get()) { successes++; }
            }
            assertEquals(1, successes);
            assertEquals(1, repository.findAll().size());
        } finally { executor.shutdownNow(); }
    }

    // Номера идут подряд и не повторяются после удаления.
    @Test
    void generatesSequentialArticlesWithoutReusingDeletedOnes() {
        CatalogService catalog = new CatalogService(new InMemoryProductRepository());
        assertEquals(100001L, catalog.nextArticle());
        catalog.add(product(100004L));
        assertEquals(100005L, catalog.nextArticle());
        catalog.delete(100004L);
        assertEquals(100006L, catalog.nextArticle());
    }

    // Проверка границ артикула и переполнения счётчика.
    @Test
    void rejectsInvalidArticlesAndSequenceOverflow() {
        assertThrows(IllegalArgumentException.class, () -> product(0));
        assertThrows(IllegalArgumentException.class, () -> product(-1));
        InMemoryProductRepository repository = new InMemoryProductRepository();
        repository.add(product(Long.MAX_VALUE));
        assertThrows(IllegalStateException.class, repository::nextArticle);
    }

    // Сто артикулов из четырёх потоков не повторяются.
    @Test
    void concurrentGenerationProducesUniqueArticles() throws Exception {
        InMemoryProductRepository repository = new InMemoryProductRepository();
        ExecutorService executor = Executors.newFixedThreadPool(4);
        try {
            java.util.ArrayList<Callable<Long>> tasks = new java.util.ArrayList<>();
            for (int i = 0; i < 100; i++) { tasks.add(repository::nextArticle); }
            java.util.Set<Long> articles = new java.util.HashSet<>();
            for (Future<Long> result : executor.invokeAll(tasks)) { articles.add(result.get()); }
            assertEquals(100, articles.size());
            assertTrue(articles.contains(100001L));
            assertTrue(articles.contains(100100L));
        } finally { executor.shutdownNow(); }
    }
}
