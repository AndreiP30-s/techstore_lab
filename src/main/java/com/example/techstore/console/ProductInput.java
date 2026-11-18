package com.example.techstore.console;

import com.example.techstore.model.product.FrontCamera;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.LongSupplier;
import com.example.techstore.model.product.Camera;
import com.example.techstore.model.product.CameraSetup;
import com.example.techstore.model.product.CameraType;
import com.example.techstore.model.product.HeadphoneType;
import com.example.techstore.model.product.Headphones;
import com.example.techstore.model.product.Laptop;
import com.example.techstore.model.product.Product;
import com.example.techstore.model.product.ProductCategory;
import com.example.techstore.model.product.Smartphone;
import com.example.techstore.model.product.StorageType;
import com.example.techstore.model.product.Tablet;

public class ProductInput {
    private final ConsoleIO io;
    private final LongSupplier articles;
    public ProductInput(ConsoleIO io, LongSupplier articles) {
        this.io = io; this.articles = articles;
    }

    public static String categoryLabel(ProductCategory category) {
        return switch (category) {
            case SMARTPHONE -> "Смартфон";
            case LAPTOP -> "Ноутбук";
            case TABLET -> "Планшет";
            case HEADPHONES -> "Наушники";
        };
    }

    public ProductCategory category() {
        return io.choose("Категория:", List.of(ProductCategory.values()), ProductInput::categoryLabel);
    }

    public Product read() {
        ProductCategory category = category();
        String name = io.text("Название:");
        String brand = io.text("Бренд:");
        BigDecimal price = io.money("Цена, руб.:");
        if (category == ProductCategory.HEADPHONES) {
            HeadphoneType type = io.choose("Конструкция:", List.of(HeadphoneType.values()), HeadphoneType::getDisplayName);
            boolean wireless = io.yesNo("Беспроводные?");
            boolean noiseCancelling = io.yesNo("Активное шумоподавление?");
            int min = io.integer("Нижняя частота, Гц:", 1, Integer.MAX_VALUE - 1);
            int max = io.integer("Верхняя частота, Гц:", min + 1, Integer.MAX_VALUE);
            return new Headphones(articles.getAsLong(), name, brand, price, type, wireless, noiseCancelling, min, max);
        }
        String processor = io.text("Процессор:");
        int ram = io.positiveInt("Оперативная память, ГБ:");
        int storage = io.positiveInt("Накопитель, ГБ:");
        double diagonal = io.positiveDouble("Диагональ, дюймы:");
        if (category == ProductCategory.LAPTOP) {
            StorageType type = io.choose("Тип накопителя:", List.of(StorageType.values()), Enum::name);
            FrontCamera front = new FrontCamera(io.positiveDouble("Фронтальная веб-камера, Мп:"));
            return new Laptop(articles.getAsLong(), name, brand, price, processor, ram, storage, diagonal, type, front);
        }
        int battery = io.positiveInt("Батарея, мА·ч:");
        boolean connection = io.yesNo(category == ProductCategory.SMARTPHONE ? "Поддержка 5G?" : "Мобильная связь?");
        FrontCamera front = new FrontCamera(io.positiveDouble("Фронтальная камера, Мп:"));
        int count = io.integer("Количество задних камер (1–10):", 1, 10);
        List<Camera> rear = new ArrayList<>();
        for (int i = 0; i < count; i++) { rear.add(camera("Задняя камера №" + (i + 1))); }
        CameraSetup cameras = new CameraSetup(front, rear);
        if (category == ProductCategory.SMARTPHONE) {
            return new Smartphone(articles.getAsLong(), name, brand, price, processor, ram, storage, diagonal, battery, connection, cameras);
        }
        return new Tablet(articles.getAsLong(), name, brand, price, processor, ram, storage, diagonal, battery, connection, cameras);
    }

    private Camera camera(String title) {
        CameraType type = io.choose(title + " — тип:", List.of(CameraType.values()), CameraType::getDisplayName);
        return new Camera(type, io.positiveDouble("Разрешение, Мп:"));
    }
}
