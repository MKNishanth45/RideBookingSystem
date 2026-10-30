# Ride Booking System – Java Low-Level Design (LLD)

A clean, modular, beginner-friendly, and professional Java console application simulating an enterprise ride-hailing platform (like Uber or Ola). Designed as a comprehensive college team project for four members following strict Layered Architecture, SOLID design principles, and robust Object-Oriented Programming (OOP) practices.

---

## 1. PROJECT HIGHLIGHTS & ARCHITECTURAL OVERVIEW

- **Language & Runtime:** Java 17+ (Tested with Java 23)
- **Interface:** Interactive Visual Studio Code integrated terminal (CLI)
- **Architecture:** Layered Architecture (`CLI` → `Service` → `Repository` → `Model`)
- **Storage:** In-memory collections (`HashMap` for $O(1)$ key lookups, `ArrayList` for ordered filtering)
- **Dependencies:** Pure Java SE Standard Library (No external DB, no bulky frameworks, 100% zero third-party JAR dependencies)
- **Team Structure:** 4 completely independent, isolated member modules ready for individual GitHub feature-branch commits.

```
RideBookingSystem/
│
├── Member1_CLI/                 # Presentation Layer (Assigned to Student 1)
│   ├── cli/                     # Console UI, input validators, rider/driver controllers
│   ├── Member1Demo.java         # Independent runnable CLI test runner
│   └── README.md                # Responsibilities, class list, and integration guide
│
├── Member2_Model/               # Domain Model Layer (Assigned to Student 2)
│   ├── model/                   # 8 Domain entities, 6 Enums, Euclidean coordinate math
│   ├── Member2Demo.java         # Independent runnable model verification program
│   └── README.md                # Data integrity, validations, and class signatures
│
├── Member3_Service/             # Business Logic Layer (Assigned to Student 3)
│   ├── service/                 # Proximity matching, surge, lifecycle, cancellations, suspension
│   ├── Member3Demo.java         # Independent runnable business logic test runner
│   └── README.md                # Enterprise business rules and pricing formulas
│
├── Member4_Repository/          # Data Access Layer (Assigned to Student 4)
│   ├── repository/              # In-memory storage interfaces & HashMap implementations
│   ├── Member4Demo.java         # Independent runnable repository CRUD test runner
│   └── README.md                # Storage specs, query operations, and complexity
│
├── src/                         # Unified Production Codebase
│   └── com/ridebooking/
│       ├── cli/
│       ├── model/
│       ├── service/
│       ├── repository/
│       ├── exception/
│       └── Main.java            # Main entrypoint with sample data seeding
│
├── INTERFACE_CONTRACT.md        # Public method signatures and architectural contract
├── TEAM_GUIDE.md                # 4-member integration, Git branching, and viva defense guide
├── .gitignore                   # Standard Java & VS Code ignore rules
└── README.md                    # Main project manual (this file)
```

---

## 2. COMPREHENSIVE LIST OF IMPLEMENTED BUSINESS RULES

### 2.1 Registration & Compliance
1. **Unique Phone Numbers:** Riders and drivers must have unique 10-digit phone numbers.
2. **Unique Vehicle Registration Numbers:** Vehicle plates are unique platform-wide.
3. **Driver License Validation:** Enforces non-empty, alphanumeric license format.
4. **Administrative Verification:** Only administratively `APPROVED` drivers can toggle their status to `AVAILABLE`.
5. **Active Vehicle Requirement:** A driver cannot go `AVAILABLE` without registering an active vehicle.
6. **Automatic Driver Suspension:** A driver whose average rating drops below **3.0★ after at least 20 completed rides** is automatically suspended and set `OFFLINE`.
7. **Rider Review Flag:** A rider whose average rating drops below **3.0★ after at least 10 completed rides** is flagged for administrative review.

### 2.2 Proximity Matching Algorithm
1. **Euclidean Distance:** Coordinate distance calculated as:
   $$\text{Distance} = \sqrt{(x_2 - x_1)^2 + (y_2 - y_1)^2}$$
2. **Radius Constraint:** Only drivers within a **5.0 km radius** are eligible.
3. **Category Matching:** Matches drivers possessing the exact requested `VehicleType`.
4. **Nearest-First Dispatch:** Candidates are sorted ascending by distance.
5. **Sequential Offers:** Offers are dispatched to a **maximum of 3 drivers** sequentially.
6. **No Duplicate Offers:** A driver who rejects or times out is never re-offered the same request.
7. **Driver Busy State:** Once a driver accepts, their status becomes `BUSY`.

### 2.3 Trip Lifecycle & State Machine
Strict unidirectional progression:
$$\text{REQUESTED} \longrightarrow \text{ACCEPTED} \longrightarrow \text{DRIVER\_ARRIVED} \longrightarrow \text{IN\_PROGRESS} \longrightarrow \text{COMPLETED}$$
1. **Skipping / Reversing Blocked:** States cannot be skipped or moved backwards.
2. **Secure 4-Digit OTP:** Generated automatically on acceptance; trip cannot start until driver enters the matching 4-digit numeric OTP.
3. **Rider Cancellation Policy:**
   - **Free Cancellation:** Within **2 minutes** of acceptance.
   - **Cancellation Fee:** **Rs. 30.00** applied if cancelled after 2 minutes.
   - **Driver Compensation:** 100% of the cancellation fee is credited directly to the driver.
4. **Driver Cancellation Policy:**
   - Permitted **only before arrival** (while in `ACCEPTED` state).
   - **Daily Quota:** Maximum of **3 cancellations per day**; 4th cancellation is blocked.
5. **Active Transit Protection:** Neither rider nor driver can cancel an active `IN_PROGRESS` trip.
6. **Driver Reset:** Driver returns to `AVAILABLE` upon successful trip completion.

### 2.4 Pricing & Dynamic Surge
1. **Fare Formula:**
   $$\text{Fare} = \text{Rs. } 30 + (\text{Distance} \times \text{Rate per km}) + (\text{Duration} \times \text{Rs. } 1)$$
2. **Vehicle Category Rates:**
   - **Bike:** Rs. 6.00 / km (Capacity: 1)
   - **Auto Rickshaw:** Rs. 10.00 / km (Capacity: 3)
   - **Mini Hatchback:** Rs. 12.00 / km (Capacity: 4)
   - **Prime Sedan:** Rs. 15.00 / km (Capacity: 4)
   - **Prime SUV:** Rs. 20.00 / km (Capacity: 6)
3. **Minimum Fare Threshold:** Minimum fare is **Rs. 50.00** across all categories.
4. **Dynamic Surge Pricing:**
   - A **1.5x multiplier** is automatically applied when active pending requests exceed twice the available drivers:
     $$\text{Pending Requests} > 2 \times \text{Available Drivers}$$
   - Transparent surge notice is displayed to the rider before booking confirmation.

### 2.5 Payments & Revenue Settlement
1. **Supported Tender Types:** `CASH`, `UPI`, `WALLET`.
2. **Wallet Protection:** Wallet payment fails if balance is insufficient.
3. **Revenue Distribution:**
   - **Driver Share:** **80%** of total fare.
   - **Platform Commission:** **20%** of total fare.
   - **Cancellation Fees:** **100%** credited to driver.
4. **Defaulting Rider Lockdown:** A rider with an outstanding failed or pending payment is **strictly blocked** from booking new rides until the dues are cleared.

### 2.6 Mutual Ratings
1. **Score Range:** Strictly 1 to 5 stars.
2. **Quota:** Exactly 1 rating per participant per trip.
3. **Time Window:** Allowed within **24 hours** of trip completion.
4. **Rolling Averages:** Instant recalculation of cumulative average ratings.

---

## 3. PRE-LOADED SAMPLE DATA

The application starts pre-seeded with realistic data for immediate testing:

| Role | Name | Phone | Details |
|---|---|---|---|
| **Rider** | Rahul Sharma | `9876543210` | Wallet: Rs. 750.00, 5.0★ rating |
| **Rider** | Ananya Verma | `9811223344` | Wallet: Rs. 300.00, 5.0★ rating |
| **Rider** | Vikram Patel | `9822334455` | Wallet: Rs. 25.00 (Useful to test insufficient funds!) |
| **Driver** | Rajesh Gowda | `9123456780` | Prime Sedan (Maruti Dzire), Status: `APPROVED`, `AVAILABLE` at (1.2, 1.3) |
| **Driver** | Mohammed Arif | `9123456781` | Mini Hatchback (Tata Tiago), Status: `APPROVED`, `AVAILABLE` at (1.5, 1.8) |
| **Driver** | Suresh Kumar | `9123456782` | Auto Rickshaw (Bajaj RE), Status: `APPROVED`, `AVAILABLE` at (1.1, 1.1) |
| **Driver** | David Pinto | `9123456783` | Bike (Hero Splendor), Status: `APPROVED`, `AVAILABLE` at (1.4, 1.2) |
| **Driver** | Harish Nair | `9123456784` | Prime SUV (Toyota Innova), Status: `APPROVED`, `AVAILABLE` at (2.0, 2.2) |
| **Driver** | Karthik Murthy | `9123456785` | Hyundai Creta, Status: `PENDING_APPROVAL` (For testing admin approval) |

---

## 4. SETUP & EXECUTION GUIDE

### Prerequisites
- Java Development Kit (JDK) 17 or later installed.
- Visual Studio Code with the integrated terminal (PowerShell or Bash).

### Option A: Running in Visual Studio Code Terminal (Windows / PowerShell)

1. Open the project folder in VS Code:
   ```powershell
   cd C:\Users\Nishanth\.gemini\antigravity\scratch\RideBookingSystem
   ```
2. Compile the complete application:
   ```powershell
   Get-ChildItem -Path src -Recurse -Filter "*.java" | ForEach-Object { $_.FullName } | Out-File -Encoding ascii sources.txt
   javac -d bin @sources.txt
   ```
3. Run the application:
   ```powershell
   java -cp bin com.ridebooking.Main
   ```

### Option B: Running on Linux / macOS / Git Bash

```bash
cd RideBookingSystem
find src -name "*.java" > sources.txt
javac -d bin @sources.txt
java -cp bin com.ridebooking.Main
```

---

## 5. AUTOMATED TEST SUITE (21 SCENARIOS VERIFIED)

You can run the built-in automated test suite by selecting **Option 6** in the main menu:

```
+----+-----------------------------------------------------+--------+---------------------------------------------------------------------------------------+
| #  | Test Scenario                                       | Result | Evaluation Details                                                                    |
+----+-----------------------------------------------------+--------+---------------------------------------------------------------------------------------+
| 1  | Duplicate Phone Registration                        | [PASS] | Caught: A rider with phone number 9000000001 already exists.                          |
| 2  | Unapproved Driver Going Online                      | [PASS] | Blocked: Only approved drivers can become Available. Current status: PENDING_APPROVAL |
| 3  | Driver Without Vehicle Going Online                 | [PASS] | Blocked: Driver must have an active vehicle before becoming Available.                |
| 4  | No Driver Within 5 km Radius                        | [PASS] | Candidate count: 0 (Driver is at (1.0, 1.0), distance > 5 km)                         |
| 5  | Nearest Driver Rejection Handling                   | [PASS] | Offered to Driver 1 & 2 sequentially after rejections                                 |
| 6  | Third Driver Accepting Ride                         | [PASS] | Trip created with Driver 3: TRP-501                                                   |
| 7  | Request Expiration Logic                            | [PASS] | 2-minute window set: 2026-10-30T14:53:04.573304200                                    |
| 8  | Invalid OTP Rejection                               | [PASS] | Incorrect OTP successfully rejected                                                   |
| 9  | Correct OTP Starts Trip                             | [PASS] | Trip state: IN_PROGRESS                                                               |
| 10 | Invalid Trip State Transition Rejection             | [PASS] | Reversing to DRIVER_ARRIVED from IN_PROGRESS blocked                                  |
| 11 | Free Cancellation Within 2 Minutes                  | [PASS] | Cancellation fee charged: Rs. 0.0                                                     |
| 12 | Cancellation Fee After 2 Minutes (Rs. 30)           | [PASS] | Fee applied: Rs. 30.0 (100% credited to driver)                                       |
| 13 | Driver Daily Cancellation Limit (3/day)             | [PASS] | 4th cancellation blocked: 3 cancellations today                                       |
| 14 | Minimum Fare Enforcement (Rs. 50.00)                | [PASS] | Calculated Subtotal: Rs. 38.0 -> Min Threshold: Rs. 50.0                              |
| 15 | Surge Pricing Dynamic Multiplier (1.5x)             | [PASS] | Multiplier: 1.5x on 5 pending vs 2 drivers                                            |
| 16 | Insufficient Wallet Payment Failure                 | [PASS] | Wallet: Rs. 10.0 | Required: Rs. 108.92 | Status: FAILED                              |
| 17 | Failed Payment Restricts New Bookings               | [PASS] | Rider blocked from new ride bookings until outstanding balance cleared                |
| 18 | Driver 80% & Platform 20% Earnings Split            | [PASS] | Driver: Rs. 80.0 | Platform: Rs. 20.0                                                 |
| 19 | Rating Restrictions (1-5 range & Single submission) | [PASS] | Blocked score 6 and duplicate submission                                              |
| 20 | Automatic Driver Suspension (<3.0★ after 20 rides)  | [PASS] | Avg: 2.0★ (20 rides) -> Status: SUSPENDED                                             |
| 21 | Rider Admin Review Flag (<3.0★ after 10 rides)      | [PASS] | Avg: 2.0★ (10 rides) -> Flagged: true                                                 |
+----+-----------------------------------------------------+--------+---------------------------------------------------------------------------------------+
```

---

## 6. SAMPLE APPLICATION CONSOLE RUN

```
================================================================
                   RIDE BOOKING SYSTEM                          
       Low-Level Design (LLD) Modular Java Application         
================================================================

========================================
          MAIN SELECTION MENU           
========================================
1. Rider Registration
2. Driver Registration
3. Rider Login
4. Driver Login
5. Admin & Operations Management
6. Run System Test Suite (21 Scenarios)
7. Exit Application
========================================

Please enter your choice (1-7): 3

----------------------------------------------------------------
  RIDER LOGIN
----------------------------------------------------------------
Enter registered 10-digit phone number: 9876543210
[SUCCESS] Welcome back, Rahul Sharma!

----------------------------------------------------------------
  RIDER DASHBOARD - RAHUL SHARMA (RDR-101)
----------------------------------------------------------------
Wallet Balance: Rs. 750.00 | Rating: 5.0★ (0 trips) | Flagged for Review: No

1. Book a Ride
2. Manage Active Trip / View Status
3. Wallet Recharge
4. Settle Outstanding Dues
5. Trip History & Rating
6. Logout to Main Menu

Select an option (1-6): 1

----------------------------------------------------------------
  BOOK A RIDE
----------------------------------------------------------------
Select Pickup Location Mode:
1. Choose from popular locations
2. Enter custom coordinates
Select (1-2): 1

Available Locations:
1. Indiranagar 100ft Road (1.00, 1.00)
2. Koramangala Sony World Signal (4.00, 5.00)
3. MG Road Metro Station (2.00, 2.50)
4. Whitefield ITPL Gate 2 (8.00, 9.00)
5. HSR Layout Sector 1 (5.00, 6.00)
6. Electronic City Phase 1 (12.00, 14.00)
Select Pickup Location (1-6): 1
Select Drop Location (1-6): 2

Calculated Euclidean Distance: 5.00 km

Select Vehicle Category:
1. Bike            (Rate: Rs. 6.00/km, Capacity: 1)
2. Auto Rickshaw   (Rate: Rs. 10.00/km, Capacity: 3)
3. Mini Hatchback  (Rate: Rs. 12.00/km, Capacity: 4)
4. Prime Sedan     (Rate: Rs. 15.00/km, Capacity: 4)
5. Prime SUV       (Rate: Rs. 20.00/km, Capacity: 6)
Select Vehicle (1-5): 3

========================================================
                 FARE ESTIMATE BREAKDOWN                
========================================================
Route: Indiranagar 100ft Road -> Koramangala Sony World Signal
Distance: 5.00 km | Category: Mini Hatchback
Base Fare: Rs. 30.00 | Rate/km: Rs. 12.00
Surge Multiplier: 1.0x (Standard)
Estimated Total Fare: Rs. 100.00 (Minimum Fare: Rs. 50.00)
========================================================
Confirm and broadcast ride request to nearby drivers? (y/n): y
[INFO]    Searching for nearest available Mini Hatchback drivers within 5 km...

Matching Drivers Found (1):
  1. Mohammed Arif | Distance: 0.94 km | Rating: 5.0★

>>> Driver Dispatch Simulation <<<

[Offer 1/1] Request sent to: Mohammed Arif (0.94 km away)...
Simulating driver response (30-second window):
1. Driver Accepts Offer
2. Driver Rejects / Ignores Offer
Simulate response (1-Accept, 2-Reject): 1
[SUCCESS] Driver Mohammed Arif accepted your ride request!

----------------------------------------------------------------
  TRIP CONFIRMED
----------------------------------------------------------------
Trip ID:         TRP-501
Driver Name:     DRV-202
Pickup Location: Indiranagar 100ft Road
Drop Location:   Koramangala Sony World Signal
Total Fare:      Rs. 100.00
--------------------------------------------------------
>>> YOUR 4-DIGIT START OTP: 9143 <<<
Share this OTP with your driver ONLY after they arrive!
--------------------------------------------------------
```
