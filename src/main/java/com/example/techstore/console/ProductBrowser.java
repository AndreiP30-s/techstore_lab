package com.example.techstore.console;

import java.util.List;
import java.util.Optional;
import com.example.techstore.model.product.Product;
import com.example.techstore.presentation.ProductFormatter;

public class ProductBrowser {
    private static final int PAGE_SIZE = 10;
    private final ConsoleIO io;
    private final ProductFormatter formatter;

    public ProductBrowser(ConsoleIO io, ProductFormatter formatter) {
        this.io = io; this.formatter = formatter;
    }

    public Optional<Product> browse(List<Product> products, boolean selecting) {
        if (products.isEmpty()) { io.println("Товары не найдены."); return Optional.empty(); }
        int page = 0;
        int pages = (products.size() - 1) / PAGE_SIZE + 1;
        while (true) {
            int start = page * PAGE_SIZE;
            int end = Math.min(start + PAGE_SIZE, products.size());
            io.println("\nСтраница " + (page + 1) + "/" + pages + " | Всего товаров: " + products.size());
            io.println("№ | Артикул | Название | Категория | Цена");
            for (int i = start; i < end; i++) {
                io.println((i + 1) + ". " + formatter.shortLabel(products.get(i)));
            }
            io.println("n — следующая | p — предыдущая | 0 — назад");
            String choice = io.line(selecting ? "Номер товара для удаления:" : "Номер товара — открыть характеристики:");
            if (choice.equals("0")) { return Optional.empty(); }
            if (choice.equalsIgnoreCase("n")) {
                if (page + 1 < pages) { page++; } else { io.println("Это последняя страница."); }
            } else if (choice.equalsIgnoreCase("p")) {
                if (page > 0) { page--; } else { io.println("Это первая страница."); }
            } else {
                try {
                    int number = Integer.parseInt(choice);
                    if (number <= start || number > end) { throw new NumberFormatException(); }
                    Product product = products.get(number - 1);
                    if (selecting) { return Optional.of(product); }
                    io.println("\n" + formatter.format(product));
                    io.line("Нажмите Enter, чтобы вернуться к списку.");
                } catch (NumberFormatException exception) {
                    io.println("Введите номер с текущей страницы, n, p или 0.");
                }
            }
        }
    }
}
