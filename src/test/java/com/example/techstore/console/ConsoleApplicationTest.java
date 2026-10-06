package com.example.techstore.console;

import com.example.techstore.TestArticles;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import com.example.techstore.model.product.*;
import com.example.techstore.model.user.UserRole;
import com.example.techstore.presentation.ProductFormatter;
import com.example.techstore.repository.memory.InMemoryProductRepository;
import com.example.techstore.repository.memory.InMemoryUserRepository;
import com.example.techstore.service.CatalogService;
import com.example.techstore.service.ParallelPriceService;
import com.example.techstore.service.UserService;
import com.example.techstore.service.SessionService;
import com.example.techstore.service.ShopService;
import com.example.techstore.service.DiscountService;
import static org.junit.jupiter.api.Assertions.*;

@Timeout(10)
class ConsoleApplicationTest {
    private final CatalogService catalog = new CatalogService(new InMemoryProductRepository());
    private final UserService users = new UserService(new InMemoryUserRepository());

    private String run(String script) {
        StringWriter output = new StringWriter();
        ConsoleIO io = new ConsoleIO(new StringReader(script), new PrintWriter(output));
        new com.example.techstore.demo.DemoUsers(users).run();
        SessionService auth = new SessionService(users);
        auth.select(users.findAll().get(0).getId());
        ShopService shop = new ShopService(auth, catalog, new ParallelPriceService(),
                new DiscountService(catalog, new ParallelPriceService()));
        new ConsoleApplication(io, new CatalogMenu(io, shop, auth, new ProductFormatter()),
                new UserMenu(io, auth), auth).run();
        return output.toString();
    }

    private void addHeadphones() {
        catalog.add(new Headphones(TestArticles.next(), "Buds", "Brand", new BigDecimal("100"),
                HeadphoneType.IN_EAR, true, false, 20, 20000));
    }

    // Добавление наушников после исправления ошибок ввода.
    @Test
    void createsHeadphonesAfterInvalidInputsAndShowsThem() {
        String output = run("abc\n1\n2\n4\nBuds\nBrand\n-1\n99,90\n1\nmaybe\nда\nнет\n20\n10\n20000\n1\n0\n0\n0\n");
        assertEquals(1, catalog.findAll().size());
        Headphones product = (Headphones) catalog.findAll().get(0);
        assertEquals(new BigDecimal("99.90"), product.getPrice());
        assertEquals(20000, product.getMaxFrequencyHz());
        assertTrue(output.contains("Введите целое число"));
        assertTrue(output.contains("Товар добавлен: Buds"));
        assertTrue(output.contains("99.90 руб."));
        assertTrue(output.contains("До свидания!"));
    }

    // Отмена формы не добавляет товар.
    @Test
    void cancelsFormWithoutAddingPartialProduct() {
        String output = run("1\n2\n1\nPhone\n/cancel\n0\n0\n");
        assertTrue(catalog.findAll().isEmpty());
        assertTrue(output.contains("Действие отменено."));
    }

    // Конец ввода не сохраняет незавершённый товар.
    @Test
    void eofInsideFormExitsWithoutAddingPartialProduct() {
        String output = run("1\n2\n3\nTab\n");
        assertTrue(catalog.findAll().isEmpty());
        assertTrue(output.contains("Ввод завершён."));
    }

    // Удаление требует подтверждения.
    @Test
    void deletesOnlyAfterConfirmation() {
        addHeadphones();
        String output = run("1\n5\n1\nнет\n1\n0\n5\n1\nда\n0\n0\n");
        assertTrue(output.contains("Удаление отменено."));
        assertTrue(output.contains("100.00 руб."));
        assertTrue(output.contains("Товар удалён."));
        assertTrue(catalog.findAll().isEmpty());
    }

    // Поиск, фильтр, применение и отмена скидки через меню.
    @Test
    void searchesFiltersAppliesAndCancelsDiscount() {
        addHeadphones();
        String output = run("1\n3\nBUD\n0\n4\n4\n100\n90\n100\n0\n6\n101\n10\n2\nда\n1\n0\n7\nда\n8\n0\n0\n");
        assertTrue(output.contains("Верхняя цена должна быть не меньше нижней."));
        assertTrue(output.contains("90.00 руб."));
        assertTrue(output.contains("Скидка применена"));
        assertTrue(output.contains("Скидка отменена"));
        assertTrue(output.contains("Бренды: [Brand]"));
        assertEquals(new BigDecimal("100.00"), catalog.findAll().get(0).getPrice());
    }

    // Операции с пустым каталогом.
    @Test
    void handlesEmptyCatalog() {
        String output = run("1\n1\n5\n6\n0\n0\n");
        assertTrue(output.contains("Товары не найдены."));
        assertTrue(output.contains("Каталог пуст."));
    }

}
