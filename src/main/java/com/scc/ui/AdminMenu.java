package com.scc.ui;

import com.scc.Model.ActivityLog;
import com.scc.Model.User;
import com.scc.dao.ActivityLogDao;
import com.scc.dao.UserDao;
import java.util.List;
import java.util.Scanner;

public class AdminMenu {
    private final User currentUser;
    private final Scanner scanner;
    private final UserDao userDao = new UserDao();
    private final ActivityLogDao logDao = new ActivityLogDao();

    public AdminMenu(User currentUser, Scanner scanner) {
        this.currentUser = currentUser;
        this.scanner = scanner;
    }

    public void show() {
        while (true) {
            System.out.println("\n=== ADMIN DASHBOARD ===");
            System.out.println("1. View All Users");
            System.out.println("2. Create Staff/Admin Account");
            System.out.println("3. Update User Role");
            System.out.println("4. Reset User Password");
            System.out.println("5. Delete User");
            System.out.println("6. View System Activity Logs");
            System.out.println("7. Clear Activity Logs");
            System.out.println("0. Logout");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> viewUsers();
                case "2" -> createAccount();
                case "3" -> updateUserRole();
                case "4" -> resetUserPassword();
                case "5" -> deleteUser();
                case "6" -> viewLogs();
                case "7" -> clearLogs();
                case "0" -> {
                    logDao.logActivity(currentUser.getId(), "Logged out");
                    return;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private void viewUsers() {
        List<User> users = userDao.getAllUsers();
        System.out.println("\n--- Registered Users ---");
        if (users.isEmpty()) {
            System.out.println("No users found.");
        } else {
            for (User u : users) {
                System.out.printf("ID: %d | Username: %s | Name: %s | Role: %s%n",
                        u.getId(), u.getUsername(), u.getFullName(), u.getRole());
            }
        }
        logDao.logActivity(currentUser.getId(), "Viewed all users");
    }

    private void createAccount() {
        System.out.print("Full Name: ");
        String fullName = scanner.nextLine().trim();
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();

        if (fullName.isEmpty() || username.isEmpty()) {
            System.out.println("Full name and username cannot be empty.");
            return;
        }
        if (userDao.usernameExists(username)) {
            System.out.println("Username '" + username + "' is already taken.");
            return;
        }

        System.out.print("Password: ");
        String password = scanner.nextLine();
        if (password.isEmpty()) {
            System.out.println("Password cannot be empty.");
            return;
        }

        System.out.print("Role (ADMIN / STAFF / RESIDENT): ");
        String role = scanner.nextLine().trim().toUpperCase();

        if (!role.equals("ADMIN") && !role.equals("STAFF") && !role.equals("RESIDENT")) {
            System.out.println("Invalid role specified.");
            return;
        }

        User user = new User(0, username, password, fullName, role);
        if (userDao.registerUser(user)) {
            System.out.println("Account created successfully.");
            logDao.logActivity(currentUser.getId(), "Created " + role + " account: " + username);
        } else {
            System.out.println("Failed to create account.");
        }
    }

    private void updateUserRole() {
        System.out.print("Enter User ID to update role: ");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());
            if (id == currentUser.getId()) {
                System.out.println("You cannot change your own role while logged in.");
                return;
            }
            User target = userDao.getUserById(id);
            if (target == null) {
                System.out.println("User not found.");
                return;
            }

            System.out.println("Selected User: " + target.getFullName() + " (Current Role: " + target.getRole() + ")");
            System.out.print("New Role (ADMIN / STAFF / RESIDENT): ");
            String newRole = scanner.nextLine().trim().toUpperCase();

            if (!newRole.equals("ADMIN") && !newRole.equals("STAFF") && !newRole.equals("RESIDENT")) {
                System.out.println("Invalid role specified.");
                return;
            }

            if (userDao.updateUserRole(id, newRole)) {
                System.out.println("User role updated to " + newRole + ".");
                logDao.logActivity(currentUser.getId(), "Updated role of " + target.getUsername() + " to " + newRole);
            } else {
                System.out.println("Failed to update user role.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID format.");
        }
    }

    private void resetUserPassword() {
        System.out.print("Enter User ID to reset password: ");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());
            User target = userDao.getUserById(id);
            if (target == null) {
                System.out.println("User not found.");
                return;
            }

            System.out.print("Enter new password for " + target.getUsername() + ": ");
            String newPassword = scanner.nextLine();
            if (newPassword.isEmpty()) {
                System.out.println("Password cannot be empty.");
                return;
            }

            if (userDao.updatePassword(id, newPassword)) {
                System.out.println("Password reset successfully.");
                logDao.logActivity(currentUser.getId(), "Reset password for user: " + target.getUsername());
            } else {
                System.out.println("Failed to reset password.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID format.");
        }
    }

    private void deleteUser() {
        System.out.print("Enter User ID to delete: ");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());
            if (id == currentUser.getId()) {
                System.out.println("You cannot delete your own active account.");
                return;
            }
            User target = userDao.getUserById(id);
            if (target == null) {
                System.out.println("User not found.");
                return;
            }

            System.out.print("Are you sure you want to delete '" + target.getUsername() + "'? (Y/N): ");
            String confirm = scanner.nextLine().trim();
            if (!confirm.equalsIgnoreCase("Y")) {
                System.out.println("Deletion cancelled.");
                return;
            }

            if (userDao.deleteUser(id)) {
                System.out.println("User deleted.");
                logDao.logActivity(currentUser.getId(), "Deleted user: " + target.getUsername() + " (ID: " + id + ")");
            } else {
                System.out.println("Failed to delete user.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID format.");
        }
    }

    private void viewLogs() {
        List<ActivityLog> logs = logDao.getAllLogs();
        System.out.println("\n--- System Activity Logs ---");
        if (logs.isEmpty()) {
            System.out.println("No activity logs recorded.");
        } else {
            for (ActivityLog log : logs) {
                System.out.println(log);
            }
        }
    }

    private void clearLogs() {
        System.out.print("Are you sure you want to clear all activity logs? (Y/N): ");
        String confirm = scanner.nextLine().trim();
        if (confirm.equalsIgnoreCase("Y")) {
            if (logDao.clearAllLogs()) {
                System.out.println("All activity logs cleared.");
                logDao.logActivity(currentUser.getId(), "Cleared all system activity logs");
            } else {
                System.out.println("Failed to clear logs.");
            }
        } else {
            System.out.println("Action cancelled.");
        }
    }
}