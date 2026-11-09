package com.example.techstore;
import com.example.techstore.model.product.FrontCamera;
import com.example.techstore.model.product.CameraSetup;
import com.example.techstore.model.product.CameraType;
import com.example.techstore.model.product.Camera;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import com.example.techstore.model.product.*;
import com.example.techstore.presentation.ProductFormatter;
import com.example.techstore.repository.memory.InMemoryProductRepository;
import com.example.techstore.service.CatalogService;
import com.example.techstore.service.ParallelPriceService;
import com.example.techstore.service.PriceQuote;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class DeviceSpecificationsTest {
    private Tablet tablet() {
        return new Tablet(TestArticles.next(), "Tab 11", "DemoTech", new BigDecimal("39990"),
                "Chip 7", 8, 128, 11.0, 8000, true, new CameraSetup(new FrontCamera(12.0),
                List.of(new Camera(CameraType.WIDE, 13.0))));
    }

    // Планшет работает с общими операциями каталога.
    @Test
    void tabletWorksThroughTheSameCatalogContract() {
        CatalogService catalog = new CatalogService(new InMemoryProductRepository());
        Tablet tablet = tablet();
        catalog.add(tablet);
        catalog.add(new Smartphone(TestArticles.next(), "Phone", "DemoTech", new BigDecimal("39990"),
                "Chip 8", 8, 256, 6.2, 4000, true, new CameraSetup(new FrontCamera(12.0),
                List.of(new Camera(CameraType.WIDE, 50.0), new Camera(CameraType.ULTRA_WIDE, 12.0), new Camera(CameraType.TELEPHOTO, 10.0)))));
        assertEquals(List.of(tablet), catalog.search("tab"));
        assertEquals(List.of(tablet), catalog.filter(ProductCategory.TABLET, BigDecimal.ZERO, new BigDecimal("40000")));
        assertEquals(tablet, catalog.findByArticle(tablet.getArticle()).orElseThrow());
        List<PriceQuote> quotes = new ParallelPriceService().calculate(catalog.findAll(), BigDecimal.TEN, 2);
        assertEquals(2, quotes.size());
        assertEquals(tablet.getArticle(), quotes.get(0).article());
        assertEquals(new BigDecimal("35991.00"), quotes.get(0).discountedPrice());
        assertTrue(catalog.delete(tablet.getArticle()));
        assertTrue(catalog.findByArticle(tablet.getArticle()).isEmpty());
    }

    // Хранение и вывод характеристик планшета.
    @Test
    void storesAndDisplaysTabletSpecifications() {
        Tablet tablet = tablet();
        assertEquals("Chip 7", tablet.getProcessor());
        assertEquals(8, tablet.getRamGb());
        assertEquals(128, tablet.getStorageGb());
        assertEquals(11.0, tablet.getScreenDiagonalInches());
        assertEquals(8000, tablet.getBatteryCapacityMah());
        assertTrue(tablet.isCellular());
        String text = new ProductFormatter().format(tablet);
        for (String expected : List.of("Планшет", "Chip 7", "ОЗУ: 8 ГБ", "128 ГБ", "11.0", "8000 мА·ч", "Мобильная связь: да")) {
            assertTrue(text.contains(expected), expected);
        }
    }

    // Характеристики ноутбука, смартфона и наушников.
    @Test
    void displaysSpecificFeaturesOfOtherCategories() {
        Laptop laptop = new Laptop(TestArticles.next(), "Book", "DemoTech", BigDecimal.TEN,
                "CPU", 16, 512, 14.0, StorageType.SSD, new FrontCamera(2.0));
        assertEquals(StorageType.SSD, laptop.getStorageType());
        assertTrue(laptop.getSpecifications().contains("512 ГБ"));
        assertTrue(laptop.getSpecifications().contains("SSD"));
        Smartphone phone = new Smartphone(TestArticles.next(), "Phone", "DemoTech", BigDecimal.TEN,
                "CPU", 8, 256, 6.2, 4000, false, new CameraSetup(new FrontCamera(12.0),
                List.of(new Camera(CameraType.WIDE, 50.0), new Camera(CameraType.ULTRA_WIDE, 12.0), new Camera(CameraType.TELEPHOTO, 10.0))));
        assertFalse(phone.supports5g());
        assertEquals(4000, phone.getBatteryCapacityMah());
        assertTrue(phone.getSpecifications().contains("5G: нет"));
        Headphones headphones = new Headphones(TestArticles.next(), "Sound", "DemoAudio", BigDecimal.TEN,
                HeadphoneType.OVER_EAR, true, true, 20, 20000);
        assertEquals(HeadphoneType.OVER_EAR, headphones.getType());
        assertTrue(headphones.getSpecifications().contains("полноразмерные"));
    }

    // Недопустимые значения диагонали.
    @ParameterizedTest
    @ValueSource(doubles = {0.0, -1.0, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY})
    void rejectsInvalidDiagonal(double diagonal) {
        assertThrows(IllegalArgumentException.class, () -> new Tablet(TestArticles.next(), "Tab", "Brand",
                BigDecimal.TEN, "CPU", 8, 128, diagonal, 8000, false, new CameraSetup(new FrontCamera(12.0),
                List.of(new Camera(CameraType.WIDE, 13.0)))));
    }

    // ОЗУ, накопитель и батарея должны быть больше нуля.
    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    void rejectsInvalidCapacity(int capacity) {
        assertThrows(IllegalArgumentException.class, () -> new Tablet(TestArticles.next(), "Tab", "Brand",
                BigDecimal.TEN, "CPU", capacity, 128, 11.0, 8000, false, new CameraSetup(new FrontCamera(12.0),
                List.of(new Camera(CameraType.WIDE, 13.0)))));
        assertThrows(IllegalArgumentException.class, () -> new Tablet(TestArticles.next(), "Tab", "Brand",
                BigDecimal.TEN, "CPU", 8, capacity, 11.0, 8000, false, new CameraSetup(new FrontCamera(12.0),
                List.of(new Camera(CameraType.WIDE, 13.0)))));
        assertThrows(IllegalArgumentException.class, () -> new Tablet(TestArticles.next(), "Tab", "Brand",
                BigDecimal.TEN, "CPU", 8, 128, 11.0, capacity, false, new CameraSetup(new FrontCamera(12.0),
                List.of(new Camera(CameraType.WIDE, 13.0)))));
        assertThrows(IllegalArgumentException.class, () -> new Smartphone(TestArticles.next(), "Phone", "Brand",
                BigDecimal.TEN, "CPU", 8, 128, 6.2, capacity, true, new CameraSetup(new FrontCamera(12.0),
                List.of(new Camera(CameraType.WIDE, 50.0), new Camera(CameraType.ULTRA_WIDE, 12.0), new Camera(CameraType.TELEPHOTO, 10.0)))));
    }

    // Тип накопителя и конструкция наушников обязательны.
    @Test
    void rejectsMissingTypes() {
        assertThrows(NullPointerException.class, () -> new Laptop(TestArticles.next(), "Book", "Brand",
                BigDecimal.TEN, "CPU", 16, 512, 14.0, null, new FrontCamera(2.0)));
        assertThrows(NullPointerException.class, () -> new Headphones(TestArticles.next(), "Sound", "Brand",
                BigDecimal.TEN, null, true, true, 20, 20000));
    }
}
