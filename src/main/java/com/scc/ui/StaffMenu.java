package com.scc.ui;

import com.scc.Model.ActivityLog;
import com.scc.Model.User;
import com.scc.dao.ActivityLogDao;
import com.scc.dao.UserDao;
import java.util.List;
import java.util.Scanner;

public class StaffMenu {
    private final User currentUser;
    private final Scanner scanner;
    private final UserDao userDao = new UserDao();
    private final ActivityLogDao logDao = new ActivityLogDao();

    public StaffMenu(User currentUser, Scanner scanner) {
        this.currentUser = currentUser;
        this.scanner = scanner;
    }

    public void show() {
        while (true) {
            System.out.println("\n=== STAFF DASHBOARD ===");
            System.out.println("1. View Resident Directory");
            System.out.println("2. Register New Resident");
            System.out.println("3. View Resident Requests & Concerns");
            System.out.println("4. Process Resident Request");
            System.out.println("5. Change Password");
            System.out.println("0. Logout");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> viewResidents();
                case "2" -> registerResident();
                case "3" -> viewResidentSubmissions();
                case "4" -> processRequest();
                case "5" -> changePassword();
                case "0" -> {
                    logDao.logActivity(currentUser.getId(), "Logged out");
                    return;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private void viewResidents() {
        List<User> users = userDao.getAllUsers();
        System.out.println("\n--- Resident List ---");
        boolean found = false;
        for (User u : users) {
            if ("RESIDENT".equalsIgnoreCase(u.getRole())) {
                System.out.printf("ID: %d | Name: %s | Username: %s%n",
                        u.getId(), u.getFullName(), u.getUsername());
                found = true;
            }
        }
        if (!found) {
            System.out.println("No registered residents found.");
        }
        logDao.logActivity(currentUser.getId(), "Viewed resident directory");
    }

    private void registerResident() {
        System.out.println("\n--- Register New Resident ---");
        System.out.print("Resident Full Name: ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) {
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
            System.out.println("Username '" + username + "' is already taken.");
            return;
        }

        System.out.print("Temporary Password (min 4 characters): ");
        String password = scanner.nextLine();
        if (password.length() < 4) {
            System.out.println("Password must be at least 4 characters long.");
            return;
        }

        if (userDao.registerUser(new User(0, username, password, name, "RESIDENT"))) {
            System.out.println("Resident registered successfully.");
            logDao.logActivity(currentUser.getId(), "Registered resident: " + username);
        } else {
            System.out.println("Registration failed.");
        }
    }

    private void viewResidentSubmissions() {
        List<ActivityLog> logs = logDao.getAllLogs();
        System.out.println("\n--- Resident Requests & Concerns ---");
        boolean found = false;
        for (ActivityLog log : logs) {
            String action = log.getAction();
            if (action.startsWith("Requested document:") || action.startsWith("Submitted concern:")) {
                System.out.println(log);
                found = true;
            }
        }
        if (!found) {
            System.out.println("No resident requests or concerns found.");
        }
        logDao.logActivity(currentUser.getId(), "Viewed resident requests and concerns");
    }

    private void processRequest() {
        System.out.print("Enter Request Reference / Resident Name to approve: ");
        String details = scanner.nextLine().trim();
        if (details.isEmpty()) {
            System.out.println("Request details cannot be empty.");
            return;
        }
        System.out.println("Request processed for: " + details);
        logDao.logActivity(currentUser.getId(), "Processed request: " + details);
    }

    private void changePassword() {
        System.out.println("\n--- Change Password ---");
        System.out.print("Enter current password: ");
        String currentPassword = scanner.nextLine();

        if (userDao.authenticate(currentUser.getUsername(), currentPassword) == null) {
            System.out.println("Incorrect current password.");
            return;
        }

        System.out.print("Enter new password (min 4 characters): ");
        String newPassword = scanner.nextLine();
        if (newPassword.length() < 4) {
            System.out.println("Password must be at least 4 characters long.");
            return;
        }

        System.out.print("Confirm new password: ");
        String confirmPassword = scanner.nextLine();
        if (!newPassword.equals(confirmPassword)) {
            System.out.println("Passwords do not match.");
            return;
        }

        if (userDao.updatePassword(currentUser.getId(), newPassword)) {
            System.out.println("Password updated successfully.");
            logDao.logActivity(currentUser.getId(), "Changed own password");
        } else {
            System.out.println("Failed to update password.");
        }
    }
}