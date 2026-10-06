package com.example.techstore;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.example.techstore.demo.DemoUsers;
import com.example.techstore.model.product.*;
import com.example.techstore.model.user.UserRole;
import com.example.techstore.repository.memory.*;
import com.example.techstore.service.*;
import static org.junit.jupiter.api.Assertions.*;
class SessionServiceTest {
    private final UserService users = new UserService(new InMemoryUserRepository());
    private final SessionService session = new SessionService(users);
    private final CatalogService catalog = new CatalogService(new InMemoryProductRepository());
    private final ShopService shop = new ShopService(session, catalog, new ParallelPriceService(),
            new DiscountService(catalog, new ParallelPriceService()));
    @BeforeEach void prepareUsers() { new DemoUsers(users).run(); }
    private void select(UserRole role) { session.select(users.findByRole(role).get(0).getId()); }
    // Без выбранного пользователя операции недоступны.
    @Test void requiresSelection() {
        assertEquals(3, session.availableUsers().size());
        assertThrows(SecurityException.class, shop::findAll);
        assertThrows(SecurityException.class, shop::nextArticle);
    }
    // Смена и сброс пользователя убирают прежние права.
    @Test void switchesAndClears() {
        select(UserRole.ADMIN);
        session.requireRole(UserRole.ADMIN);
        select(UserRole.CUSTOMER);
        assertThrows(SecurityException.class, () -> session.requireRole(UserRole.ADMIN));
        session.clear();
        assertThrows(SecurityException.class, session::requireUser);
        select(UserRole.ADMIN);
        assertThrows(IllegalArgumentException.class, () -> session.select(UUID.randomUUID()));
        assertTrue(session.currentUser().isEmpty());
    }
    // Покупатель не меняет товары и скидки даже при обращении к сервису напрямую.
    @Test void customerCannotModify() {
        Product product = new Headphones(TestArticles.next(), "Buds", "Brand", new BigDecimal("100"),
                HeadphoneType.IN_EAR, true, false, 20, 20000);
        catalog.add(product);
        select(UserRole.CUSTOMER);
        assertEquals(1, shop.findAll().size());
        assertEquals(1, shop.search("Buds").size());
        assertThrows(SecurityException.class, () -> shop.add(product));
        assertThrows(SecurityException.class, () -> shop.delete(product.getArticle()));
        assertThrows(SecurityException.class, () -> shop.applyDiscount(BigDecimal.TEN, 2));
        assertThrows(SecurityException.class, shop::cancelDiscount);
        assertEquals(new BigDecimal("100.00"), product.getPrice());
    }
    // Менеджер и администратор могут добавлять, удалять товары и менять скидки.
    @Test void managementCanModify() {
        for (UserRole role : new UserRole[]{UserRole.ADMIN, UserRole.MANAGER}) {
            select(role);
            Product product = new Headphones(shop.nextArticle(), "Buds", "Brand", new BigDecimal("100"),
                    HeadphoneType.IN_EAR, true, false, 20, 20000);
            shop.add(product);
            assertEquals(1, shop.applyDiscount(BigDecimal.TEN, 2));
            assertEquals(new BigDecimal("90.00"), product.getPrice());
            assertEquals(1, shop.cancelDiscount());
            assertEquals(new BigDecimal("100.00"), product.getPrice());
            assertTrue(shop.delete(product.getArticle()));
        }
    }
}
