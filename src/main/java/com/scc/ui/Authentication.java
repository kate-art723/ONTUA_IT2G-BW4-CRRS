package com.scc.ui;

import com.scc.Model.User;
import com.scc.dao.ActivityLogDao;
import com.scc.dao.UserDao;
import java.util.Scanner;

public class Authentication {
    private final UserDao userDao = new UserDao();
    private final ActivityLogDao logDao = new ActivityLogDao();
    private final Scanner scanner;

    public Authentication(Scanner scanner) {
        this.scanner = scanner;
    }

    public User showAuthMenu() {
        while (true) {
            System.out.println("\n=== SYSTEM AUTHENTICATION ===");
            System.out.println("1. Login");
            System.out.println("2. Register (Resident)");
            System.out.println("0. Exit");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> {
                    User user = login();
                    if (user != null) return user;
                }
                case "2" -> registerResident();
                case "0" -> {
                    System.out.println("Exiting system...");
                    return null;
                }
                default -> System.out.println("Invalid option. Try again.");
            }
        }
    }

    private User login() {
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        if (username.isEmpty() || password.isEmpty()) {
            System.out.println("Username and password cannot be empty.");
            return null;
        }

        User user = userDao.authenticate(username, password);
        if (user != null) {
            System.out.println("Welcome, " + user.getFullName() + " (" + user.getRole() + ")!");
            logDao.logActivity(user.getId(), "Logged into the system");
            return user;
        } else {
            System.out.println("Invalid username or password.");
            return null;
        }
    }

    private void registerResident() {
        System.out.println("\n--- Resident Registration ---");
        System.out.print("Full Name: ");
        String fullName = scanner.nextLine().trim();
        if (fullName.isEmpty()) {
            System.out.println("Full name cannot be empty.");
            return;
        }

        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        if (username.isEmpty()) {
            System.out.println("Username cannot be empty.");
            return;
        }
        if (userDao.usernameExists(username)) {
            System.out.println("Username '" + username + "' is already taken. Please choose another.");
            return;
        }

        System.out.print("Password: ");
        String password = scanner.nextLine();
        if (password.length() < 4) {
            System.out.println("Password must be at least 4 characters long.");
            return;
        }

        System.out.print("Confirm Password: ");
        String confirmPassword = scanner.nextLine();
        if (!password.equals(confirmPassword)) {
            System.out.println("Passwords do not match. Registration cancelled.");
            return;
        }

        User newUser = new User(0, username, password, fullName, "RESIDENT");
        if (userDao.registerUser(newUser)) {
            System.out.println("Registration successful! You can now log in.");
        } else {
            System.out.println("Registration failed. Please try again.");
        }
    }
}