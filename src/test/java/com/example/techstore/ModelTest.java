package com.example.techstore;
import com.example.techstore.model.product.FrontCamera;
import com.example.techstore.model.product.CameraSetup;
import com.example.techstore.model.product.CameraType;
import com.example.techstore.model.product.Camera;
import java.util.List;
import org.junit.jupiter.api.Test;
import com.example.techstore.model.product.Product;
import com.example.techstore.model.product.Smartphone;
import com.example.techstore.model.product.Laptop;
import com.example.techstore.model.product.Headphones;
import com.example.techstore.model.user.AdminUser;
import com.example.techstore.presentation.ProductFormatter;
import com.example.techstore.model.product.StorageType;
import com.example.techstore.model.product.HeadphoneType;
import java.math.BigDecimal;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
class ModelTest {
    // Неверные поля товара отклоняются.
    @Test void rejectsInvalidProductFields() {
        long id = TestArticles.next();
        assertThrows(IllegalArgumentException.class, () -> new Smartphone(id, " ", "Brand", BigDecimal.ONE, "CPU", 8, 128, 6.1, 4500, true, new CameraSetup(new FrontCamera(12.0),
                List.of(new Camera(CameraType.WIDE, 50.0), new Camera(CameraType.ULTRA_WIDE, 12.0), new Camera(CameraType.TELEPHOTO, 10.0)))));
        assertThrows(IllegalArgumentException.class, () -> new Smartphone(id, "Phone", "Brand", new BigDecimal("-1"), "CPU", 8, 128, 6.1, 4500, true, new CameraSetup(new FrontCamera(12.0),
                List.of(new Camera(CameraType.WIDE, 50.0), new Camera(CameraType.ULTRA_WIDE, 12.0), new Camera(CameraType.TELEPHOTO, 10.0)))));
        assertThrows(IllegalArgumentException.class, () -> new Smartphone(id, "Phone", "Brand", new BigDecimal("1.001"), "CPU", 8, 128, 6.1, 4500, true, new CameraSetup(new FrontCamera(12.0),
                List.of(new Camera(CameraType.WIDE, 50.0), new Camera(CameraType.ULTRA_WIDE, 12.0), new Camera(CameraType.TELEPHOTO, 10.0)))));
        assertThrows(IllegalArgumentException.class, () -> new Smartphone(id, "Phone", "Brand", BigDecimal.ONE, "CPU", 8, 0, 6.1, 4500, true, new CameraSetup(new FrontCamera(12.0),
                List.of(new Camera(CameraType.WIDE, 50.0), new Camera(CameraType.ULTRA_WIDE, 12.0), new Camera(CameraType.TELEPHOTO, 10.0)))));
        assertThrows(IllegalArgumentException.class, () -> new Laptop(id, "PC", "Brand", BigDecimal.ONE, " ", 16, 512, 14.0, StorageType.SSD, new FrontCamera(2.0)));
    }
    // Неверный email отклоняется.
    @Test void rejectsMalformedEmail() {
        assertThrows(IllegalArgumentException.class, () -> new AdminUser(UUID.randomUUID(), "Anna", "not-an-email"));
        assertThrows(IllegalArgumentException.class, () -> new AdminUser(UUID.randomUUID(), "Anna", "a b@example.org"));
    }
    // Равенство и hashCode товара зависят от артикула.
    @Test void identityIsBasedOnArticle() {
        long id = TestArticles.next();
        Product first = new Headphones(id, "A", "Brand", BigDecimal.ONE, HeadphoneType.IN_EAR, true, false, 20, 20000);
        Product second = new Headphones(id, "B", "Brand", BigDecimal.TEN, HeadphoneType.OVER_EAR, false, false, 20, 20000);
        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }
    // Карточка содержит характеристики ноутбука.
    @Test void formatterUsesSubtypeSpecifications() {
        Product laptop = new Laptop(TestArticles.next(), "E14", "Lenovo", BigDecimal.TEN, "Intel", 16, 512, 14.0, StorageType.SSD, new FrontCamera(2.0));
        String result = new ProductFormatter().format(laptop);
        assertTrue(result.contains("Lenovo E14"));
        assertTrue(result.contains("16 ГБ"));
        assertTrue(result.contains("10.00"));
        assertTrue(result.contains("Intel"));
    }
}
