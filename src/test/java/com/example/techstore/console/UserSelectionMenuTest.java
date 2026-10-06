package com.example.techstore.console;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import com.example.techstore.demo.DemoUsers;
import com.example.techstore.presentation.ProductFormatter;
import com.example.techstore.repository.memory.*;
import com.example.techstore.service.*;
import static org.junit.jupiter.api.Assertions.*;
@Timeout(5)
class UserSelectionMenuTest {
    private final UserService users = new UserService(new InMemoryUserRepository());
    private final SessionService session = new SessionService(users);
    private String run(String script) {
        new DemoUsers(users).run();
        StringWriter output = new StringWriter();
        ConsoleIO io = new ConsoleIO(new StringReader(script), new PrintWriter(output));
        CatalogService catalog = new CatalogService(new InMemoryProductRepository());
        ShopService shop = new ShopService(session, catalog, new ParallelPriceService(),
                new DiscountService(catalog, new ParallelPriceService()));
        new ConsoleApplication(io, new CatalogMenu(io, shop, session, new ProductFormatter()),
                new UserMenu(io, session), session).run();
        return output.toString();
    }
    // Покупатель видит только чтение каталога с последовательными номерами.
    @Test void customerMenu() {
        String output = run("3\n1\n0\n0\n");
        assertTrue(output.contains("ГЛАВНОЕ МЕНЮ — Анна (CUSTOMER)"));
        assertTrue(output.contains("2. Поиск"));
        assertTrue(output.contains("3. Фильтр"));
        assertTrue(output.contains("4. Бренды"));
        assertFalse(output.contains("Добавить товар"));
        assertFalse(output.contains("Пароль"));
        assertTrue(session.currentUser().isEmpty());
    }
    // Можно сменить администратора на менеджера, затем на покупателя.
    @Test void switchesRoles() {
        String output = run("1\n2\n4\n2\n1\n0\n3\n3\n0\n");
        assertTrue(output.contains("ГЛАВНОЕ МЕНЮ — Андрей (ADMIN)"));
        assertTrue(output.contains("ГЛАВНОЕ МЕНЮ — Иван (MANAGER)"));
        assertTrue(output.contains("ГЛАВНОЕ МЕНЮ — Анна (CUSTOMER)"));
        assertTrue(output.contains("Добавить товар"));
        assertTrue(output.contains("Андрей | admin@example.org | ADMIN"));
    }
    // Ошибку номера и отмену можно исправить следующим вводом.
    @Test void retriesSelection() {
        String output = run("99\n/cancel\n3\n0\n");
        assertTrue(output.contains("Введите целое число"));
        assertTrue(output.contains("Выбор отменён."));
        assertTrue(output.contains("ГЛАВНОЕ МЕНЮ — Анна"));
    }
    // Ноль завершает программу до выбора пользователя.
    @Test void exitsBeforeSelection() {
        assertFalse(run("0\n").contains("ГЛАВНОЕ МЕНЮ"));
        assertTrue(session.currentUser().isEmpty());
    }
    // Конец ввода на первом экране корректно завершает программу.
    @Test void handlesEof() {
        assertTrue(run("").contains("Ввод завершён."));
        assertTrue(session.currentUser().isEmpty());
    }
}
