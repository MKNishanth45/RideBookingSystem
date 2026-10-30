# Member 3: Service Module Documentation

## 1. Overview and Responsibilities
Member 3 is responsible for the Business Logic Layer (BLL) of the Ride Booking System. This module enforces all enterprise rules, safety invariants, pricing models, and workflow lifecycles:
- **Registration Validations:** Phone uniqueness (10 digits), license formatting, unique vehicle registration number across the platform.
- **Compliance Rules:** Drivers can only go online if they are administratively approved AND possess an active vehicle.
- **Driver Suspension:** Automatic suspension when a driver's average rating falls below 3.0 after at least 20 trips.
- **Rider Admin Review:** Automatic flagging when a rider's average rating drops below 3.0 after at least 10 completed trips.
- **Proximity Matching:** Euclidean distance calculation, 5 km radius ceiling, vehicle type filtering, nearest-first sorting, 3-driver maximum offer limit, and avoidance of duplicate assignments.
- **Trip Lifecycle Integrity:** Unidirectional progression (`REQUESTED -> ACCEPTED -> DRIVER_ARRIVED -> IN_PROGRESS -> COMPLETED`). Strict OTP validation before trip starts.
- **Cancellation Policies:** Free cancellation within 2 minutes of acceptance; Rs. 30 fee after 2 minutes (100% credited to driver). Drivers can only cancel before arrival, capped at 3 cancellations per day.
- **Pricing & Surge:** Formula: `Fare = Rs. 30 + (Distance * Rate) + (Duration * Rs. 1)`. Minimum fare: Rs. 50. Dynamic 1.5x surge applied when pending requests > 2 * available drivers.
- **Revenue Settlement:** 80% to driver, 20% to platform. Instant wallet balance check, blocking riders with unpaid/failed balances from booking further rides.

---

## 2. Package Structure
```
Member3_Service/
├── service/
│   ├── DriverService.java
│   ├── FareCalculatorService.java
│   ├── FareEstimate.java
│   ├── MatchingCandidate.java
│   ├── MatchingService.java
│   ├── RatingService.java
│   ├── RiderService.java
│   └── TripService.java
├── Member3Demo.java
└── README.md
```

---

## 3. Services and Core Methods

### 3.1 `FareCalculatorService`
- `calculateFare(double distanceKm, VehicleType vehicleType, int pendingRequests, int availableDrivers)`: Computes base, distance, and duration components, applies minimum fare threshold (Rs. 50), and conditionally applies 1.5x surge.
- `calculateCancellationFee(LocalDateTime acceptedTime, LocalDateTime cancelTime)`: Enforces 2-minute free grace period, returning Rs. 30 fee thereafter.

### 3.2 `MatchingService`
- `findEligibleDrivers(Location pickup, VehicleType vehicleType, List<String> excludedDriverIds)`: Queries available drivers, computes Euclidean distance, filters by $\le 5.0$ km, sorts nearest first, and caps at top 3 candidates.

### 3.3 `RiderService`
- `registerRider(String name, String phone, String email, double initialWallet)`: Validates phone uniqueness and initializes wallet.
- `addWalletBalance(String riderId, double amount)`: Recharges rider wallet.
- `hasUnresolvedPayments(String riderId)`: Checks for unpaid debts.
- `resolvePayment(String paymentId, PaymentMethod method)`: Settles pending/failed dues.

### 3.4 `DriverService`
- `registerDriver(String name, String phone, String email, String licenseNumber, Location initialLocation)`: Validates unique phone and license.
- `registerVehicle(String driverId, String regNumber, String makeModel, VehicleType type)`: Validates plate uniqueness and sets as active vehicle.
- `approveDriver(String driverId)`: Admin approval.
- `setDriverAvailability(String driverId, DriverAvailability availability)`: Enforces approval and active vehicle prerequisites.
- `recordDriverCancellation(String driverId)`: Enforces max 3 cancellations per day.
- `checkDriverSuspension(String driverId)`: Evaluates 20-trip threshold and $< 3.0$ rating condition.

### 3.5 `TripService`
- `createRideRequest(String riderId, Location pickup, Location drop, VehicleType vehicleType)`: Pre-checks unresolved debts, active trips, and computes transparent upfront fare.
- `acceptRide(String rideRequestId, String driverId)`: Generates 4-digit OTP and marks driver `BUSY`.
- `driverArrived(String tripId, String driverId)`: Transitions to `DRIVER_ARRIVED`.
- `startTrip(String tripId, String driverId, String enteredOtp)`: Validates 4-digit OTP and begins trip.
- `completeTrip(String tripId, PaymentMethod paymentMethod)`: Transitions to `COMPLETED`, calculates 80/20 revenue split, debits wallet, and resets driver to `AVAILABLE`.
- `cancelTripByRider(String tripId, String riderId, String reason)`: Applies fee if $> 2$ minutes and credits 100% to driver.
- `cancelTripByDriver(String tripId, String driverId, String reason)`: Allows cancellation only before arrival, checking 3/day quota.

### 3.6 `RatingService`
- `submitRating(String tripId, String fromUserId, int score, String feedback)`: Validates 1-5 score, 24-hour window, single review per participant, updates rolling averages, and triggers review flags.

---

## 4. Dependencies on Other Team Members
- **Upstream Dependencies:**
  - **Member 2 (Model):** Utilizes `Rider`, `Driver`, `Vehicle`, `Trip`, `Payment`, `Location`, and enums.
  - **Member 4 (Repository):** Persists and queries data via repository interfaces.
  - **Exceptions:** Standard exceptions (`InvalidInputException`, `TripLifecycleException`, etc.).
- **Downstream Consumers:** Injected into **Member 1 (CLI)** to execute user actions.

---

## 5. Standalone Compilation & Demo Execution
To compile and run Member 3's comprehensive business rule verification:

```powershell
cd C:\Users\Nishanth\.gemini\antigravity\scratch\RideBookingSystem
javac -d bin src/com/ridebooking/exception/*.java Member2_Model/model/*.java Member4_Repository/repository/*.java Member3_Service/service/*.java Member3_Service/Member3Demo.java
java -cp bin com.ridebooking.Member3Demo
```
