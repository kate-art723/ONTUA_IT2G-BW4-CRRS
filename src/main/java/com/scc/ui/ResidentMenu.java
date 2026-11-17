package com.scc.ui;

import com.scc.Model.ActivityLog;
import com.scc.Model.User;
import com.scc.dao.ActivityLogDao;
import com.scc.dao.UserDao;
import java.util.List;
import java.util.Scanner;

public class ResidentMenu {
    private final User currentUser;
    private final Scanner scanner;
    private final ActivityLogDao logDao = new ActivityLogDao();
    private final UserDao userDao = new UserDao();

    public ResidentMenu(User currentUser, Scanner scanner) {
        this.currentUser = currentUser;
        this.scanner = scanner;
    }

    public void show() {
        while (true) {
            System.out.println("\n=== RESIDENT PORTAL ===");
            System.out.println("1. View My Profile");
            System.out.println("2. Submit Document / Clearance Request");
            System.out.println("3. Submit Concern / Report");
            System.out.println("4. View My Activity History");
            System.out.println("5. Change Password");
            System.out.println("0. Logout");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> viewProfile();
                case "2" -> submitRequest();
                case "3" -> submitConcern();
                case "4" -> viewMyHistory();
                case "5" -> changePassword();
                case "0" -> {
                    logDao.logActivity(currentUser.getId(), "Logged out");
                    return;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private void viewProfile() {
        System.out.println("\n--- My Profile ---");
        System.out.println("User ID   : " + currentUser.getId());
        System.out.println("Full Name : " + currentUser.getFullName());
        System.out.println("Username  : " + currentUser.getUsername());
        System.out.println("Role      : " + currentUser.getRole());
        logDao.logActivity(currentUser.getId(), "Viewed own profile");
    }

    private void submitRequest() {
        System.out.print("Enter document/clearance type needed: ");
        String docType = scanner.nextLine().trim();
        if (docType.isEmpty()) {
            System.out.println("Document type cannot be empty.");
            return;
        }
        System.out.println("Your request for '" + docType + "' has been submitted.");
        logDao.logActivity(currentUser.getId(), "Requested document: " + docType);
    }

    private void submitConcern() {
        System.out.print("Describe your concern or issue: ");
        String concern = scanner.nextLine().trim();
        if (concern.isEmpty()) {
            System.out.println("Concern description cannot be empty.");
            return;
        }
        System.out.println("Concern logged. Staff will review it shortly.");
        logDao.logActivity(currentUser.getId(), "Submitted concern: " + concern);
    }

    private void viewMyHistory() {
        List<ActivityLog> myLogs = logDao.getLogsByUserId(currentUser.getId());
        System.out.println("\n--- My Activity History ---");
        if (myLogs.isEmpty()) {
            System.out.println("No activity history found.");
        } else {
            for (ActivityLog log : myLogs) {
                System.out.println(log);
            }
        }
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