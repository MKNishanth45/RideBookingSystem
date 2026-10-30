# RIDE BOOKING SYSTEM – TEAM INTEGRATION & COLLABORATION GUIDE

This document serves as the master coordination and defense manual for the 4-member student team developing the **Ride Booking System** Java Low-Level Design (LLD) project.

---

## 1. FOUR-MEMBER RESPONSIBILITY MATRIX & ARCHITECTURE

The application implements a strict **Layered Architecture** adhering to SOLID principles:
```
Presentation Layer (Member 1)
        ↓ calls
Business Logic Layer (Member 3)
        ↓ calls
Data Access Layer (Member 4)
        ↓ manipulates
Domain Model Layer (Member 2)
```

```
RideBookingSystem/
├── Member1_CLI/          <-- Assigned to Student 1 (CLI & Presentation)
├── Member2_Model/        <-- Assigned to Student 2 (Domain Entities & Enums)
├── Member3_Service/      <-- Assigned to Student 3 (Business Logic & Pricing)
├── Member4_Repository/   <-- Assigned to Student 4 (In-Memory Data Access)
├── src/                  <-- Integrated Production Codebase (com.ridebooking)
├── INTERFACE_CONTRACT.md <-- Specification preventing merge conflicts
├── TEAM_GUIDE.md         <-- This team guide
└── README.md             <-- Setup and execution documentation
```

### Detailed Student Assignment:

| Team Member | Module Folder | Root Package | Primary Responsibilities |
|---|---|---|---|
| **Member 1 (Student 1)** | `Member1_CLI/` | `com.ridebooking.cli` | Console menus, ASCII tables, input validation (`Scanner`), interactive dispatch simulation, status timeline, test runner UI |
| **Member 2 (Student 2)** | `Member2_Model/` | `com.ridebooking.model` | 8 Domain entities, 6 enums, encapsulation, constructor validation, Euclidean distance formula in `Location`, rolling ratings |
| **Member 3 (Student 3)** | `Member3_Service/` | `com.ridebooking.service` | Business rules, proximity matching (5 km radius, 3-driver offer limit), pricing & dynamic surge (1.5x), OTP verification, cancellation fees, driver suspension |
| **Member 4 (Student 4)** | `Member4_Repository/` | `com.ridebooking.repository` | Pure data access without business logic, interfaces & in-memory `HashMap`/`ArrayList` implementations for all 7 entities |

---

## 2. GITHUB BRANCHING & COMMIT STRATEGY

To simulate a professional software development lifecycle and ensure zero merge conflicts, the team must use dedicated feature branches:

### Branch Naming Conventions:
- `main`: Production-ready, fully integrated codebase.
- `feature/member2-domain-models`: Student 2's development branch.
- `feature/member4-data-repositories`: Student 4's development branch.
- `feature/member3-business-services`: Student 3's development branch.
- `feature/member1-console-cli`: Student 1's development branch.

### Suggested Git Commit Milestones:

#### Student 2 (Model Module):
```bash
git checkout -b feature/member2-domain-models
git add Member2_Model/
git commit -m "feat(model): implement domain entities, enums, and Euclidean coordinate distance"
git push origin feature/member2-domain-models
```

#### Student 4 (Repository Module):
```bash
git checkout -b feature/member4-data-repositories
git add Member4_Repository/
git commit -m "feat(repo): implement in-memory repositories using HashMap and ArrayList"
git push origin feature/member4-data-repositories
```

#### Student 3 (Service Module):
```bash
git checkout -b feature/member3-business-services
git add Member3_Service/
git commit -m "feat(service): implement fare pricing, 5km matching, trip lifecycle, and suspension"
git push origin feature/member3-business-services
```

#### Student 1 (CLI Module):
```bash
git checkout -b feature/member1-console-cli
git add Member1_CLI/
git commit -m "feat(cli): implement interactive console UX, ASCII tables, and automated test suite"
git push origin feature/member1-console-cli
```

---

## 3. INDEPENDENT DEMO EXECUTION (BEFORE INTEGRATION)

Each team member has a dedicated, standalone test demo class inside their respective folder. Evaluators can independently compile and verify any individual member's work without running the full application:

### Member 2 Demo (Domain Models):
```powershell
javac -d bin Member2_Model/model/*.java Member2_Model/Member2Demo.java
java -cp bin com.ridebooking.Member2Demo
```
*Verifies: Euclidean distance between coordinates, entity encapsulation, constructor validation, immutability, rolling rating recalculations.*

### Member 4 Demo (Repositories):
```powershell
javac -d bin Member2_Model/model/*.java Member4_Repository/repository/*.java Member4_Repository/Member4Demo.java
java -cp bin com.ridebooking.Member4Demo
```
*Verifies: $O(1)$ ID lookups via `HashMap`, filtered queries via `ArrayList`, finding available drivers by vehicle type, pending request queries.*

### Member 3 Demo (Services):
```powershell
javac -d bin src/com/ridebooking/exception/*.java Member2_Model/model/*.java Member4_Repository/repository/*.java Member3_Service/service/*.java Member3_Service/Member3Demo.java
java -cp bin com.ridebooking.Member3Demo
```
*Verifies: Proximity matching within 5 km, 1.5x surge pricing, 4-digit OTP security, cancellation fee calculation, driver suspension below 3.0 stars, and rider review flagging.*

### Member 1 Demo (CLI & Test Suite):
```powershell
javac -d bin src/com/ridebooking/exception/*.java Member2_Model/model/*.java Member4_Repository/repository/*.java Member3_Service/service/*.java Member1_CLI/cli/*.java Member1_CLI/Member1Demo.java
java -cp bin com.ridebooking.Member1Demo
```
*Verifies: ASCII banners, dynamic column tables, status timeline progression, bulletproof `Scanner` validation, and execution of all 21 automated system tests.*

---

## 4. INTEGRATION VERIFICATION

When all members are ready to assemble their components into `src/`:
1. Ensure all files adhere strictly to the signatures in `INTERFACE_CONTRACT.md`.
2. Compile the unified codebase:
   ```powershell
   Get-ChildItem -Path src -Recurse -Filter "*.java" | ForEach-Object { $_.FullName } | Out-File -Encoding ascii sources.txt
   javac -d bin @sources.txt
   ```
3. Run the complete application:
   ```powershell
   java -cp bin com.ridebooking.Main
   ```
4. From the Main Menu, choose Option `6` to run the **System Test Suite (21 Scenarios)**. Ensure that 21/21 tests pass with 100% success rate.

---

## 5. COLLEGE PROJECT PRESENTATION & DEFENSE GUIDE

During the project evaluation and viva-voce, each student should speak about their specific architectural contributions and design decisions:

### Student 1 (Member 1 - CLI Layer):
- **Key Talking Points:**
  - "I designed the presentation layer using the Model-View-Controller (MVC) separation pattern."
  - "To prevent common beginner crashes caused by invalid inputs in `Scanner`, I created `InputValidator`, which safely traps `NumberFormatException` and prompts the user without exiting."
  - "I created a custom tabular formatter that calculates column widths dynamically to render clean ASCII tables."
  - "I built an interactive driver dispatch simulation that models real-world ride acceptance timeouts and sequential driver offers."
  - "I incorporated an automated 21-scenario test runner directly into the console interface."

### Student 2 (Member 2 - Model Layer):
- **Key Talking Points:**
  - "I established the domain model adhering strictly to encapsulation. All fields are private, and immutable attributes like IDs and timestamps lack setters."
  - "Constructors validate data integrity: phone numbers must be 10 digits, email must be valid, and wallet balances cannot be negative."
  - "In the `Location` class, I encapsulated the Euclidean distance formula: $\sqrt{(x_2 - x_1)^2 + (y_2 - y_1)^2}$."
  - "Entities like `Rider` and `Driver` compute a rolling weighted average rating, eliminating floating-point drift."

### Student 3 (Member 3 - Service Layer):
- **Key Talking Points:**
  - "I implemented all enterprise business logic in the Service layer, ensuring repositories remain agnostic of business rules."
  - "The `MatchingService` filters online drivers within a 5.0 km radius who match the requested vehicle category, sorting them nearest-first and capping offers at 3 drivers."
  - "The `FareCalculatorService` enforces the fare formula: $\text{Rs. } 30 + (\text{Distance} \times \text{Rate}) + (\text{Duration} \times 1)$, with a minimum floor of Rs. 50."
  - "I introduced dynamic surge pricing: when active pending requests exceed twice the available drivers, a 1.5x multiplier is applied."
  - "The trip lifecycle follows a strict finite state machine: `REQUESTED` $\rightarrow$ `ACCEPTED` $\rightarrow$ `DRIVER_ARRIVED` $\rightarrow$ `IN_PROGRESS` $\rightarrow$ `COMPLETED`, secured by a 4-digit OTP."
  - "I enforced quality control rules: drivers with $< 3.0$ rating after 20 rides are automatically suspended, and riders with $< 3.0$ rating after 10 rides are flagged for admin review."

### Student 4 (Member 4 - Repository Layer):
- **Key Talking Points:**
  - "I built the Data Access Layer using the Repository Pattern with pure in-memory collections."
  - "By defining interfaces (e.g. `RiderRepository`) and decoupled implementations (`InMemoryRiderRepository`), our design follows the Dependency Inversion Principle (DIP). In the future, switching from in-memory collections to SQL/MongoDB requires zero changes to the service layer."
  - "Primary key lookups utilize `HashMap` for $O(1)$ query complexity."
  - "Filtered queries return independent `ArrayList` copies to protect internal data structures from external mutation."
