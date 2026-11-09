package com.example.techstore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.example.techstore.service.CatalogService;
import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest(properties = {"app.demo.enabled=false", "app.console.enabled=false"})
class TechStoreApplicationTest {
    @Autowired private CatalogService catalog;
    // Spring запускается без меню и стартовых товаров.
    @Test void contextLoadsWithoutDemoData() { assertTrue(catalog.findAll().isEmpty()); }
}
