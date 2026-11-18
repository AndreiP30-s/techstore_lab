package com.example.techstore.console;

import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import com.example.techstore.model.product.Product;
import com.example.techstore.model.product.ProductCategory;
import com.example.techstore.presentation.ProductFormatter;
import com.example.techstore.service.ShopService;
import com.example.techstore.service.SessionService;
import com.example.techstore.model.user.UserRole;

public class CatalogMenu {
    private final ConsoleIO io;
    private final ShopService catalog;
    private final SessionService session;
    private final ProductBrowser browser;
    private final ProductInput input;

    public CatalogMenu(ConsoleIO io, ShopService catalog, SessionService session, ProductFormatter formatter) {
        this.io = io; this.catalog = catalog; this.session = session; this.browser = new ProductBrowser(io, formatter);
        this.input = new ProductInput(io, catalog::nextArticle);
    }

    public void run() {
        while (true) {
            boolean management = session.requireUser().getRole() != UserRole.CUSTOMER;
            List<MenuItem> items = new ArrayList<>();
            items.add(new MenuItem("Показать каталог", () -> show(catalog.findAll())));
            if (management) { items.add(new MenuItem("Добавить товар", this::add)); }
            items.add(new MenuItem("Поиск по артикулу, названию или бренду", () ->
                    show(catalog.search(io.line("Артикул, название или бренд (пусто — все):")))));
            items.add(new MenuItem("Фильтр по категории и цене", this::filter));
            if (management) {
                items.add(new MenuItem("Удалить товар", this::delete));
                items.add(new MenuItem("Применить скидку в потоках", this::discount));
                items.add(new MenuItem("Отменить скидку", this::cancelDiscount));
            }
            items.add(new MenuItem("Бренды", () -> io.println("Бренды: " + catalog.brands())));
            try {
                if (!io.menu("ТОВАРЫ", items, "Назад")) { return; }
            } catch (InputCancelledException exception) { io.println("Действие отменено."); }
            catch (SecurityException exception) { io.println(exception.getMessage()); }
            catch (IllegalArgumentException exception) { io.println(exception.getMessage()); }
        }
    }

    private void add() {
        catalog.requireManagement();
        Product product = input.read();
        catalog.add(product);
        io.println("Товар добавлен: " + product.getName() + " | Артикул: " + product.getArticle());
    }

    private void show(List<Product> products) { browser.browse(products, false); }

    private void filter() {
        ProductCategory category = input.category();
        BigDecimal min = io.money("Цена от, руб.:");
        BigDecimal max;
        do {
            max = io.money("Цена до, руб.:");
            if (max.compareTo(min) < 0) { io.println("Верхняя цена должна быть не меньше нижней."); }
        } while (max.compareTo(min) < 0);
        show(catalog.filter(category, min, max));
    }

    private void delete() {
        catalog.requireManagement();
        List<Product> products = catalog.findAll();
        if (products.isEmpty()) { io.println("Каталог пуст."); return; }
        Optional<Product> selected = browser.browse(products, true);
        if (selected.isEmpty()) { return; }
        Product product = selected.get();
        if (io.yesNo("Удалить «" + product.getArticle() + " — " + product.getName() + "»?")) {
            io.println(catalog.delete(product.getArticle()) ? "Товар удалён." : "Товар уже отсутствует.");
        } else { io.println("Удаление отменено."); }
    }

    private void discount() {
        catalog.requireManagement();
        if (catalog.hasActiveDiscount()) { io.println("Сначала отмените действующую скидку."); return; }
        if (catalog.findAll().isEmpty()) { io.println("Каталог пуст."); return; }
        BigDecimal percent;
        do {
            percent = io.percent("Скидка, %:");
            if (percent.signum() == 0) { io.println("Скидка должна быть больше нуля."); }
        } while (percent.signum() == 0);
        int threads = io.integer("Количество потоков (1–32):", 1, 32);
        if (!io.yesNo("Применить скидку " + percent + "% ко всем текущим товарам?")) {
            io.println("Цены не изменены."); return;
        }
        int changed = catalog.applyDiscount(percent, threads);
        io.println("Скидка применена. Обновлено товаров: " + changed + ". Новые цены видны в каталоге.");
    }

    private void cancelDiscount() {
        catalog.requireManagement();
        if (!catalog.hasActiveDiscount()) { io.println("Нет действующей скидки."); return; }
        if (io.yesNo("Отменить скидку и восстановить прежние цены?")) {
            io.println("Скидка отменена. Восстановлено товаров: " + catalog.cancelDiscount() + ".");
        } else { io.println("Скидка остаётся действующей."); }
    }
}
