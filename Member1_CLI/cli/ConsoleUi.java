package com.ridebooking.cli;

import java.util.List;

/**
 * Utility for formatting terminal output with clean ASCII borders,
 * formatted tables, currency symbols, and status indicators.
 */
public class ConsoleUi {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BOLD = "\u001B[1m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_CYAN = "\u001B[36m";

    public static void printWelcomeBanner() {
        System.out.println();
        System.out.println("================================================================");
        System.out.println("                   RIDE BOOKING SYSTEM                          ");
        System.out.println("       Low-Level Design (LLD) Modular Java Application         ");
        System.out.println("================================================================");
    }

    public static void printHeader(String title) {
        System.out.println();
        System.out.println("----------------------------------------------------------------");
        System.out.println("  " + title.toUpperCase());
        System.out.println("----------------------------------------------------------------");
    }

    public static void printSection(String sectionName) {
        System.out.println();
        System.out.println(">>> " + sectionName + " <<<");
    }

    public static void printSuccess(String message) {
        System.out.println("[SUCCESS] " + message);
    }

    public static void printError(String message) {
        System.out.println("[ERROR]   " + message);
    }

    public static void printWarning(String message) {
        System.out.println("[WARNING] " + message);
    }

    public static void printInfo(String message) {
        System.out.println("[INFO]    " + message);
    }

    public static String formatCurrency(double amount) {
        return String.format("Rs. %.2f", amount);
    }

    public static void printTripStatusTimeline(String status) {
        System.out.println();
        System.out.println("Trip Lifecycle Progress:");
        String req = "REQUESTED".equalsIgnoreCase(status) ? "[* REQUESTED *]" : " REQUESTED ";
        String acc = "ACCEPTED".equalsIgnoreCase(status) ? "[* ACCEPTED *]" : " ACCEPTED ";
        String arr = "DRIVER_ARRIVED".equalsIgnoreCase(status) ? "[* DRIVER ARRIVED *]" : " DRIVER ARRIVED ";
        String inp = "IN_PROGRESS".equalsIgnoreCase(status) ? "[* IN PROGRESS *]" : " IN PROGRESS ";
        String com = "COMPLETED".equalsIgnoreCase(status) ? "[* COMPLETED *]" : " COMPLETED ";

        System.out.println("  " + req + " -> " + acc + " -> " + arr + " -> " + inp + " -> " + com);
        if ("CANCELLED".equalsIgnoreCase(status)) {
            System.out.println("  ==> [X] TRIP CANCELLED");
        }
        System.out.println();
    }

    /**
     * Prints a nicely formatted ASCII table.
     */
    public static void printTable(List<String> headers, List<List<String>> rows) {
        if (headers == null || headers.isEmpty()) return;

        int cols = headers.size();
        int[] widths = new int[cols];

        for (int i = 0; i < cols; i++) {
            widths[i] = headers.get(i).length();
        }

        if (rows != null) {
            for (List<String> row : rows) {
                for (int i = 0; i < cols && i < row.size(); i++) {
                    String val = row.get(i) != null ? row.get(i) : "";
                    if (val.length() > widths[i]) {
                        widths[i] = val.length();
                    }
                }
            }
        }

        // Add padding
        for (int i = 0; i < cols; i++) {
            widths[i] += 2;
        }

        // Top line
        printDivider(widths);

        // Header
        System.out.print("|");
        for (int i = 0; i < cols; i++) {
            System.out.printf(" %-" + (widths[i] - 1) + "s|", headers.get(i));
        }
        System.out.println();

        // Header separator
        printDivider(widths);

        // Rows
        if (rows == null || rows.isEmpty()) {
            System.out.print("|");
            int totalWidth = 0;
            for (int w : widths) totalWidth += w + 1;
            String emptyMsg = "No records found.";
            System.out.printf(" %-" + (totalWidth - 3) + "s|\n", emptyMsg);
        } else {
            for (List<String> row : rows) {
                System.out.print("|");
                for (int i = 0; i < cols; i++) {
                    String val = (i < row.size() && row.get(i) != null) ? row.get(i) : "";
                    System.out.printf(" %-" + (widths[i] - 1) + "s|", val);
                }
                System.out.println();
            }
        }

        // Bottom line
        printDivider(widths);
    }

    private static void printDivider(int[] widths) {
        System.out.print("+");
        for (int w : widths) {
            System.out.print("-".repeat(w) + "+");
        }
        System.out.println();
    }
}
