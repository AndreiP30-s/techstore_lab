package com.example.techstore.console;

import java.util.List;
import com.example.techstore.model.user.User;
import com.example.techstore.service.SessionService;

public class UserSelectionMenu {
    private final ConsoleIO io;
    private final SessionService session;

    public UserSelectionMenu(ConsoleIO io, SessionService session) {
        this.io = io; this.session = session;
    }

    public boolean run() {
        while (session.currentUser().isEmpty()) {
            List<User> users = session.availableUsers();
            io.println("\nВЫБОР ПОЛЬЗОВАТЕЛЯ");
            for (int i = 0; i < users.size(); i++) {
                User user = users.get(i);
                io.println((i + 1) + ". " + user.getName() + " (" + user.getRole() + ")");
            }
            io.println("0. Выход");
            try {
                int choice = io.integer("Выберите пользователя:", 0, users.size());
                if (choice == 0) { return false; }
                session.select(users.get(choice - 1).getId());
            } catch (InputCancelledException exception) { io.println("Выбор отменён."); }
        }
        return true;
    }
}
