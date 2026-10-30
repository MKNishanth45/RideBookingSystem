package com.ridebooking.cli;

import java.util.Scanner;

/**
 * Reusable utility for bulletproof console input handling.
 * Protects application against crashes caused by invalid input types or blank entries.
 */
public class InputValidator {
    private final Scanner scanner;

    public InputValidator(Scanner scanner) {
        this.scanner = scanner;
    }

    public int readInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                int value = Integer.parseInt(input);
                if (value >= min && value <= max) {
                    return value;
                }
                ConsoleUi.printWarning(String.format("Please enter a number between %d and %d.", min, max));
            } catch (NumberFormatException e) {
                ConsoleUi.printWarning("Invalid input. Please enter a valid integer.");
            }
        }
    }

    public double readDouble(String prompt, double min) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                double value = Double.parseDouble(input);
                if (value >= min) {
                    return value;
                }
                ConsoleUi.printWarning(String.format("Please enter a value greater than or equal to %.2f.", min));
            } catch (NumberFormatException e) {
                ConsoleUi.printWarning("Invalid input. Please enter a valid decimal number.");
            }
        }
    }

    public String readString(String prompt, boolean allowEmpty) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (allowEmpty || !input.isEmpty()) {
                return input;
            }
            ConsoleUi.printWarning("Input cannot be empty. Please enter a value.");
        }
    }

    public String readPhone(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.matches("\\d{10}")) {
                return input;
            }
            ConsoleUi.printWarning("Invalid phone number. Must be exactly 10 numeric digits.");
        }
    }

    public String readEmail(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                return input;
            }
            ConsoleUi.printWarning("Invalid email format (e.g. name@example.com).");
        }
    }

    public boolean readConfirmation(String prompt) {
        while (true) {
            System.out.print(prompt + " (y/n): ");
            String input = scanner.nextLine().trim().toLowerCase();
            if (input.equals("y") || input.equals("yes")) return true;
            if (input.equals("n") || input.equals("no")) return false;
            ConsoleUi.printWarning("Please enter 'y' for yes or 'n' for no.");
        }
    }
}
