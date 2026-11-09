package com.example.techstore.console;

import com.example.techstore.model.user.User;
import com.example.techstore.model.user.UserRole;
import com.example.techstore.service.SessionService;

public class UserMenu {
    private final ConsoleIO io;
    private final SessionService session;
    public UserMenu(ConsoleIO io, SessionService session) { this.io = io; this.session = session; }

    public void run() {
        session.requireRole(UserRole.ADMIN);
        io.println("ПОЛЬЗОВАТЕЛИ");
        for (User user : session.availableUsers()) {
            io.println(user.getName() + " | " + user.getEmail() + " | " + user.getRole());
        }
    }
}
