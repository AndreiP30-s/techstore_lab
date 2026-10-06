package com.example.techstore.console;

import java.util.ArrayList;
import java.util.List;
import com.example.techstore.model.user.User;
import com.example.techstore.model.user.UserRole;
import com.example.techstore.service.SessionService;

public class ConsoleApplication {
    private final ConsoleIO io;
    private final CatalogMenu catalog;
    private final UserMenu users;
    private final SessionService session;

    public ConsoleApplication(ConsoleIO io, CatalogMenu catalog, UserMenu users, SessionService session) {
        this.io = io; this.catalog = catalog; this.users = users; this.session = session;
    }

    public void run() {
        io.println("МАГАЗИН ТЕХНИКИ\nДанные хранятся до выхода из программы.\n/cancel — отменить ввод.");
        try {
            while (true) {
                if (!new UserSelectionMenu(io, session).run()) { io.println("До свидания!"); return; }
                User current = session.requireUser();
                List<MenuItem> items = new ArrayList<>();
                items.add(new MenuItem("Товары", catalog::run));
                if (current.getRole() == UserRole.ADMIN) { items.add(new MenuItem("Пользователи", users::run)); }
                items.add(new MenuItem("Мой профиль", () ->
                        io.println(current.getName() + " | " + current.getEmail() + " | " + current.getRole())));
                items.add(new MenuItem("Сменить пользователя", () -> {
                    session.clear();
                    io.println("Выбор пользователя сброшен.");
                }));
                try {
                    if (!io.menu("ГЛАВНОЕ МЕНЮ — " + current.getName() + " (" + current.getRole() + ")",
                            items, "Завершить программу")) {
                        io.println("До свидания!");
                        return;
                    }
                } catch (InputCancelledException exception) { io.println("Действие отменено."); }
                catch (SecurityException exception) { io.println(exception.getMessage()); }
            }
        } catch (EndOfInputException exception) { io.println("\nВвод завершён. До свидания!"); }
        finally { session.clear(); }
    }
}
