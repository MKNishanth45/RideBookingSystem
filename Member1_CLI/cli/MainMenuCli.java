package com.ridebooking.cli;

import java.util.Scanner;

/**
 * Top-level application controller and main entry menu.
 */
public class MainMenuCli {
    private final RiderCli riderCli;
    private final DriverCli driverCli;
    private final AdminCli adminCli;
    private final InputValidator input;

    public MainMenuCli(RiderCli riderCli, DriverCli driverCli, AdminCli adminCli, Scanner scanner) {
        this.riderCli = riderCli;
        this.driverCli = driverCli;
        this.adminCli = adminCli;
        this.input = new InputValidator(scanner);
    }

    public void start() {
        ConsoleUi.printWelcomeBanner();

        while (true) {
            System.out.println();
            System.out.println("========================================");
            System.out.println("          MAIN SELECTION MENU           ");
            System.out.println("========================================");
            System.out.println("1. Rider Registration");
            System.out.println("2. Driver Registration");
            System.out.println("3. Rider Login");
            System.out.println("4. Driver Login");
            System.out.println("5. Admin & Operations Management");
            System.out.println("6. Run System Test Suite (21 Scenarios)");
            System.out.println("7. Exit Application");
            System.out.println("========================================");

            int choice = input.readInt("\nPlease enter your choice (1-7): ", 1, 7);
            switch (choice) {
                case 1 -> riderCli.register();
                case 2 -> driverCli.register();
                case 3 -> riderCli.login();
                case 4 -> driverCli.login();
                case 5 -> adminCli.showMenu();
                case 6 -> SystemTestSuite.runAllTests();
                case 7 -> {
                    ConsoleUi.printWelcomeBanner();
                    System.out.println("Thank you for using Ride Booking System. Goodbye!");
                    System.out.println("================================================================");
                    return;
                }
            }
        }
    }
}
