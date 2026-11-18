package com.example.techstore.console;

import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import com.example.techstore.presentation.ProductFormatter;
import com.example.techstore.service.SessionService;
import com.example.techstore.service.ShopService;

@Component
@Order(1)
@ConditionalOnProperty(name = "app.console.enabled", havingValue = "true", matchIfMissing = true)
public class ConsoleRunner implements CommandLineRunner {
    private final ShopService shop;
    private final SessionService session;
    private final ProductFormatter formatter;

    public ConsoleRunner(ShopService shop, SessionService session, ProductFormatter formatter) {
        this.shop = shop; this.session = session; this.formatter = formatter;
    }

    @Override
    public void run(String... args) {
        ConsoleIO io = new ConsoleIO(new InputStreamReader(System.in, StandardCharsets.UTF_8),
                new PrintWriter(System.out, true, StandardCharsets.UTF_8));
        new ConsoleApplication(io, new CatalogMenu(io, shop, session, formatter), new UserMenu(io, session), session).run();
    }
}
