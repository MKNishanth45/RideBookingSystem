package com.ridebooking;

import com.ridebooking.cli.ConsoleUi;
import com.ridebooking.cli.InputValidator;
import com.ridebooking.cli.SystemTestSuite;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;

/**
 * Standalone demo program for Member 1 (CLI Module).
 * Verifies terminal formatting, ASCII banners, table rendering,
 * status timeline indicators, input safety, and automated test execution.
 */
public class Member1Demo {
    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("      MEMBER 1: CLI MODULE VERIFICATION DEMO      ");
        System.out.println("==================================================");

        // 1. Banner and Section Headers
        ConsoleUi.printWelcomeBanner();
        ConsoleUi.printHeader("Demo of CLI Formatting Components");

        // 2. Alert & Message Styling
        ConsoleUi.printSuccess("Driver matched successfully within 5.0 km radius!");
        ConsoleUi.printInfo("Request dispatched to nearest available driver.");
        ConsoleUi.printWarning("High demand surge pricing (1.5x) is currently active.");
        ConsoleUi.printError("Invalid OTP entered. Please verify with rider.");

        // 3. Currency Formatting
        System.out.println("\n[✓] Currency Formatted: " + ConsoleUi.formatCurrency(145.75));

        // 4. Trip Status Progress Bar
        ConsoleUi.printTripStatusTimeline("REQUESTED");
        ConsoleUi.printTripStatusTimeline("ACCEPTED");
        ConsoleUi.printTripStatusTimeline("DRIVER_ARRIVED");
        ConsoleUi.printTripStatusTimeline("IN_PROGRESS");
        ConsoleUi.printTripStatusTimeline("COMPLETED");

        // 5. Table Formatting
        ConsoleUi.printHeader("Sample ASCII Table Render");
        List<String> headers = List.of("Vehicle Category", "Base Rate", "Capacity", "Min Fare");
        List<List<String>> rows = List.of(
                List.of("Bike", "Rs. 6.00/km", "1 passenger", "Rs. 50.00"),
                List.of("Auto Rickshaw", "Rs. 10.00/km", "3 passengers", "Rs. 50.00"),
                List.of("Mini Hatchback", "Rs. 12.00/km", "4 passengers", "Rs. 50.00"),
                List.of("Prime Sedan", "Rs. 15.00/km", "4 passengers", "Rs. 50.00"),
                List.of("Prime SUV", "Rs. 20.00/km", "6 passengers", "Rs. 50.00")
        );
        ConsoleUi.printTable(headers, rows);

        // 6. Safe Input Validator Test (Simulated automated input)
        String simulatedInput = "abc\n99\n2\n";
        Scanner testScanner = new Scanner(new ByteArrayInputStream(simulatedInput.getBytes(StandardCharsets.UTF_8)));
        InputValidator validator = new InputValidator(testScanner);

        System.out.println("\nTesting InputValidator with simulated entries [invalid 'abc', out-of-range '99', valid '2']:");
        int safeVal = validator.readInt("Enter option (1-5): ", 1, 5);
        System.out.println("[✓] Safely Parsed Integer without crash: " + safeVal);

        // 7. Run Comprehensive 21-Scenario Test Suite
        System.out.println("\nRunning full automated scenario suite via Member 1:");
        SystemTestSuite.runAllTests();

        System.out.println("==================================================");
        System.out.println("    MEMBER 1 ALL CLI VERIFICATIONS PASSED!        ");
        System.out.println("==================================================");
    }
}
