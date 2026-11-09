package com.example.techstore.demo;
import com.example.techstore.model.product.FrontCamera;
import com.example.techstore.model.product.CameraSetup;
import com.example.techstore.model.product.CameraType;
import com.example.techstore.model.product.Camera;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.core.annotation.Order;
import com.example.techstore.model.product.Product;
import com.example.techstore.model.product.Tablet;
import com.example.techstore.model.product.Smartphone;
import com.example.techstore.model.product.Laptop;
import com.example.techstore.model.product.Headphones;
import com.example.techstore.service.CatalogService;
import com.example.techstore.model.product.StorageType;
import com.example.techstore.model.product.HeadphoneType;
import java.math.BigDecimal;
@Component
@Order(0)
@ConditionalOnProperty(name = "app.demo.enabled", havingValue = "true", matchIfMissing = true)
public class DemoRunner implements CommandLineRunner {
    private final CatalogService catalog;
    public DemoRunner(CatalogService catalog) {
        this.catalog = catalog;
    }
    @Override public void run(String... args) {
        catalog.add(new Smartphone(100001L, "Phone S", "DemoTech", new BigDecimal("69990"), "DemoChip 8", 8, 256, 6.2, 4000, true, new CameraSetup(new FrontCamera(12.0),
                List.of(new Camera(CameraType.WIDE, 50.0), new Camera(CameraType.ULTRA_WIDE, 12.0), new Camera(CameraType.TELEPHOTO, 10.0)))));
        catalog.add(new Laptop(100002L, "Book 14", "DemoTech", new BigDecimal("89990"), "DemoCore i5", 16, 512, 14.0, StorageType.SSD, new FrontCamera(2.0)));
        catalog.add(new Tablet(100003L, "Tab 11", "DemoTech", new BigDecimal("39990"),
                "DemoChip 7", 8, 128, 11.0, 8000, true, new CameraSetup(new FrontCamera(12.0),
                List.of(new Camera(CameraType.WIDE, 13.0)))));
        Product headphones = new Headphones(100004L, "Sound Pro", "DemoAudio", new BigDecimal("29990"), HeadphoneType.OVER_EAR, true, true, 20, 20000);
        catalog.add(headphones);
    }
}
