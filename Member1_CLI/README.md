# Member 1: CLI Module Documentation

## 1. Overview and Responsibilities
Member 1 is responsible for the Presentation Layer (CLI) of the Ride Booking System. This module provides a professional, bulletproof console user experience:
- **Crash Prevention:** Reusable `InputValidator` handles `Scanner` input mismatches and empty inputs safely, ensuring the application never crashes on invalid inputs.
- **Visual Design:** Clean ASCII banners, structured section headers, formatted ASCII tables with dynamic column widths, and live trip status progression indicators.
- **Interactive Workflows:**
  - Rider registration, login, profile view, and wallet top-ups.
  - Interactive ride booking flow with Euclidean distance calculation, vehicle category selection, transparent fare breakdowns, and surge notices.
  - Driver offer dispatch simulation (allowing simulated acceptance or rejection to offer to the next nearest driver).
  - OTP display and driver verification.
  - Driver console for registration, vehicle association, status toggling, arriving at pickup, starting trips via OTP, completing trips, and collecting payments.
  - Administrative oversight for reviewing pending drivers, inspecting review flags, and viewing platform revenue.
  - Built-in automated 21-scenario test runner.

---

## 2. Package Structure
```
Member1_CLI/
├── cli/
│   ├── AdminCli.java
│   ├── ConsoleUi.java
│   ├── DriverCli.java
│   ├── InputValidator.java
│   ├── MainMenuCli.java
│   ├── RiderCli.java
│   └── SystemTestSuite.java
├── Member1Demo.java
└── README.md
```

---

## 3. Classes and Responsibilities

### 3.1 `ConsoleUi`
- `printWelcomeBanner()`: Renders the system ASCII banner.
- `printHeader(String title)`: Formats standard section headers.
- `printSuccess(String msg)`, `printError(String msg)`, `printWarning(msg)`, `printInfo(msg)`: Formatted alerts.
- `formatCurrency(double amount)`: Returns standard `Rs. XX.XX` representation.
- `printTripStatusTimeline(String status)`: Renders lifecycle progression arrows.
- `printTable(List<String> headers, List<List<String>> rows)`: Generates auto-sized ASCII borders.

### 3.2 `InputValidator`
- `readInt(String prompt, int min, int max)`: Safe numeric range parser.
- `readDouble(String prompt, double min)`: Safe floating-point parser.
- `readString(String prompt, boolean allowEmpty)`: Non-blank string reader.
- `readPhone(String prompt)`: Validates 10-digit format.
- `readEmail(String prompt)`: Basic regex email format validation.
- `readConfirmation(String prompt)`: Parses `(y/n)` confirmations.

### 3.3 `RiderCli`
- `register()`, `login()`: Account onboarding and session initiation.
- `bookRideFlow()`: End-to-end booking, driver candidate discovery ($\le 5.0$ km), fare breakdown, driver dispatch simulation, and OTP generation.
- `manageActiveTripFlow()`: Real-time progress monitoring and cancellation fee evaluation.
- `walletRechargeFlow()`, `settleOutstandingDuesFlow()`: Financial management.
- `tripHistoryFlow()`: Past trip records and 1-5 star rating submission.

### 3.4 `DriverCli`
- `register()`, `login()`: Driver onboarding.
- `registerVehicleFlow()`: Vehicle association.
- `toggleAvailabilityFlow()`: Goes online/offline with compliance checks.
- `manageActiveTripFlow()`: Marking arrival, starting trip via 4-digit OTP, completing trip, and tender collection (Cash, UPI, Wallet).
- `tripHistoryFlow()`: Driver history.

### 3.5 `AdminCli`
- `viewRiders()`: Highlights riders with $< 3.0$ rating after 10+ rides.
- `viewDrivers()`: Shows compliance, ratings, and vehicle links.
- `reviewPendingDrivers()`: Approves/rejects driver applications.
- `financialSummary()`: Gross fares, driver 80% earnings, and platform 20% commission.
- `surgeStatus()`: Live ratio of pending requests vs available drivers.

### 3.6 `SystemTestSuite`
- Executes all 21 automated test scenarios defined in Section 6 and prints a comprehensive results table.

### 3.7 `MainMenuCli`
- Main console loop routing users to Rider, Driver, Admin, or Test Suite consoles.

---

## 4. Dependencies on Other Team Members
- **Upstream Dependencies:**
  - **Member 3 (Service):** Calls business logic methods.
  - **Member 2 (Model):** Reads domain models and enums.
  - **Member 4 (Repository):** Used for test harness execution.
- **Downstream Consumers:** Injected into `src/com/ridebooking/Main.java`.

---

## 5. Standalone Compilation & Demo Execution
To compile and run Member 1's CLI demo independently:

```powershell
cd C:\Users\Nishanth\.gemini\antigravity\scratch\RideBookingSystem
javac -d bin src/com/ridebooking/exception/*.java Member2_Model/model/*.java Member4_Repository/repository/*.java Member3_Service/service/*.java Member1_CLI/cli/*.java Member1_CLI/Member1Demo.java
java -cp bin com.ridebooking.Member1Demo
```
