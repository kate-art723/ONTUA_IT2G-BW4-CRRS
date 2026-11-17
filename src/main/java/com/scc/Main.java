package com.scc;

import com.scc.Model.User;
import com.scc.dao.DatabaseInitializer;
import com.scc.ui.AdminMenu;
import com.scc.ui.Authentication;
import com.scc.ui.ResidentMenu;
import com.scc.ui.StaffMenu;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        // Initialize database tables and default admin user
        DatabaseInitializer.initialize();

        try (Scanner scanner = new Scanner(System.in)) {
            Authentication auth = new Authentication(scanner);

            while (true) {
                User loggedInUser = auth.showAuthMenu();

                // Exit loop cleanly if user selects "0. Exit" in Authentication
                if (loggedInUser == null) {
                    break;
                }

                switch (loggedInUser.getRole().toUpperCase()) {
                    case "ADMIN" -> new AdminMenu(loggedInUser, scanner).show();
                    case "STAFF" -> new StaffMenu(loggedInUser, scanner).show();
                    case "RESIDENT" -> new ResidentMenu(loggedInUser, scanner).show();
                    default -> System.out.println("Unknown role assigned to user.");
                }
            }
        }
    }
}