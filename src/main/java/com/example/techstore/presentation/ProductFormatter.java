package com.example.techstore.presentation;

import org.springframework.stereotype.Component;
import com.example.techstore.model.product.Camera;
import com.example.techstore.model.product.CameraSetup;
import com.example.techstore.model.product.ComputingDevice;
import com.example.techstore.model.product.Headphones;
import com.example.techstore.model.product.Laptop;
import com.example.techstore.model.product.Product;
import com.example.techstore.model.product.ProductCategory;
import com.example.techstore.model.product.Smartphone;
import com.example.techstore.model.product.Tablet;

@Component
public class ProductFormatter {
    public String format(Product product) {
        StringBuilder card = new StringBuilder(product.getBrand() + " " + product.getName());
        field(card, "Артикул", Long.toString(product.getArticle()));
        field(card, "Категория", category(product.getCategory()));
        field(card, "Цена", product.getPrice() + " руб.");
        card.append("\n\n  Характеристики");

        if (product instanceof ComputingDevice device) {
            field(card, "Процессор", device.getProcessor());
            field(card, "ОЗУ", device.getRamGb() + " ГБ");
            field(card, "Накопитель", device.getStorageGb() + " ГБ");
            field(card, "Экран", device.getScreenDiagonalInches() + " дюйма");
        }
        if (product instanceof Smartphone phone) {
            field(card, "Батарея", phone.getBatteryCapacityMah() + " мА·ч");
            field(card, "5G", yesNo(phone.supports5g()));
            cameras(card, phone.getCameras());
        } else if (product instanceof Tablet tablet) {
            field(card, "Батарея", tablet.getBatteryCapacityMah() + " мА·ч");
            field(card, "Мобильная связь", yesNo(tablet.isCellular()));
            cameras(card, tablet.getCameras());
        } else if (product instanceof Laptop laptop) {
            field(card, "Тип накопителя", laptop.getStorageType().name());
            card.append("\n\n  Камера");
            field(card, "Фронтальная", laptop.getFrontCamera().getSpecifications());
        } else if (product instanceof Headphones headphones) {
            field(card, "Конструкция", headphones.getType().getDisplayName());
            field(card, "Беспроводные", yesNo(headphones.isWireless()));
            field(card, "Шумоподавление", yesNo(headphones.isNoiseCancelling()));
            field(card, "Диапазон частот", headphones.getMinFrequencyHz() + "–" + headphones.getMaxFrequencyHz() + " Гц");
        }
        return card.toString();
    }

    public String shortLabel(Product product) {
        String name = product.getBrand() + " " + product.getName();
        if (name.length() > 32) { name = name.substring(0, 29) + "..."; }
        return "%s | %s | %s | %s руб.".formatted(
                product.getArticle(), name, category(product.getCategory()), product.getPrice());
    }

    private void cameras(StringBuilder card, CameraSetup cameras) {
        card.append("\n\n  Камеры");
        field(card, "Фронтальная", cameras.frontCamera().getSpecifications());
        field(card, "Задние", Integer.toString(cameras.getRearCameraCount()));
        for (Camera camera : cameras.rearCameras()) {
            card.append("\n    • ").append(camera.getSpecifications());
        }
        field(card, "Всего камер", Integer.toString(cameras.getTotalCameraCount()));
    }

    private void field(StringBuilder card, String label, String value) {
        card.append("\n  ").append(label).append(": ").append(value);
    }

    private String yesNo(boolean value) { return value ? "да" : "нет"; }

    private String category(ProductCategory category) {
        return switch (category) {
            case SMARTPHONE -> "Смартфон";
            case LAPTOP -> "Ноутбук";
            case TABLET -> "Планшет";
            case HEADPHONES -> "Наушники";
        };
    }
}
