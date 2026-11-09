package com.example.techstore;
import com.example.techstore.model.product.FrontCamera;
import com.example.techstore.model.product.CameraSetup;
import com.example.techstore.model.product.CameraType;
import com.example.techstore.model.product.Camera;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.example.techstore.model.product.Product;
import com.example.techstore.model.product.ProductCategory;
import com.example.techstore.model.product.Smartphone;
import com.example.techstore.model.product.Laptop;
import com.example.techstore.model.product.Headphones;
import com.example.techstore.repository.memory.InMemoryProductRepository;
import com.example.techstore.service.CatalogService;
import com.example.techstore.model.product.StorageType;
import com.example.techstore.model.product.HeadphoneType;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
class CatalogServiceTest {
    private CatalogService catalog;
    private Product phone;
    @BeforeEach void setUp() {
        catalog = new CatalogService(new InMemoryProductRepository());
        phone = new Smartphone(TestArticles.next(), "Galaxy", "Samsung", new BigDecimal("100.00"), "CPU", 8, 128, 6.1, 4500, true, new CameraSetup(new FrontCamera(12.0),
                List.of(new Camera(CameraType.WIDE, 50.0), new Camera(CameraType.ULTRA_WIDE, 12.0), new Camera(CameraType.TELEPHOTO, 10.0))));
        catalog.add(phone);
    }
    // Поиск и удаление добавленного товара.
    @Test void addFindAndDelete() {
        assertEquals(phone, catalog.findByArticle(phone.getArticle()).orElseThrow());
        assertTrue(catalog.delete(phone.getArticle()));
        assertFalse(catalog.delete(phone.getArticle()));
        assertTrue(catalog.findByArticle(phone.getArticle()).isEmpty());
    }
    // Повторный артикул не перезаписывает товар.
    @Test void duplicateArticleDoesNotOverwriteProduct() {
        Product duplicate = new Laptop(phone.getArticle(), "Other", "Brand", BigDecimal.TEN, "CPU", 8, 256, 14.0, StorageType.SSD, new FrontCamera(2.0));
        assertThrows(IllegalArgumentException.class, () -> catalog.add(duplicate));
        assertSame(phone, catalog.findByArticle(phone.getArticle()).orElseThrow());
    }
    // Поиск по названию и бренду без учёта регистра и внешних пробелов.
    @Test void searchIgnoresCaseAndOuterWhitespace() {
        assertEquals(List.of(phone), catalog.search("  sAmSuNg  "));
        assertEquals(List.of(phone), catalog.search("ALAX"));
        assertTrue(catalog.search("missing").isEmpty());
        assertEquals(List.of(phone), catalog.search(" "));
    }
    // Фильтр учитывает категорию и границы цены.
    @Test void filterIncludesBoundsAndChecksCategory() {
        catalog.add(new Laptop(TestArticles.next(), "Laptop", "Brand", new BigDecimal("100"), "CPU", 8, 256, 14.0, StorageType.SSD, new FrontCamera(2.0)));
        assertEquals(List.of(phone), catalog.filter(ProductCategory.SMARTPHONE, new BigDecimal("100"), new BigDecimal("100")));
        assertTrue(catalog.filter(ProductCategory.SMARTPHONE, BigDecimal.ZERO, new BigDecimal("99.99")).isEmpty());
        assertThrows(IllegalArgumentException.class, () -> catalog.filter(ProductCategory.LAPTOP, BigDecimal.TEN, BigDecimal.ONE));
    }
    // Бренды не повторяются при разном регистре.
    @Test void brandsAreUniqueIgnoringCase() {
        catalog.add(new Headphones(TestArticles.next(), "Buds", "SAMSUNG", BigDecimal.TEN, HeadphoneType.IN_EAR, true, false, 20, 20000));
        assertEquals(1, catalog.brands().size());
    }
    // Состав полученного списка не меняется вместе с каталогом.
    @Test void returnedListIsImmutableSnapshot() {
        List<Product> snapshot = catalog.findAll();
        assertThrows(UnsupportedOperationException.class, snapshot::clear);
        catalog.delete(phone.getArticle());
        assertEquals(1, snapshot.size());
    }
}
