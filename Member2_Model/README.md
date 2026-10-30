# Member 2: Model Module Documentation

## 1. Overview and Responsibilities
Member 2 is responsible for defining the domain model and core entities of the Ride Booking System. This module adheres strictly to Object-Oriented Programming (OOP) principles:
- **Encapsulation:** All class attributes are `private`, accessed via getters, and manipulated only through domain methods that protect business invariants.
- **Data Integrity:** Strict constructor validations ensure invalid phone numbers, blank identifiers, negative wallet balances, and out-of-bounds ratings cannot exist in memory.
- **Immutability:** Identity fields (`id`, `createdAt`, `licenseNumber`, etc.) do not have setters.
- **Geometrical Calculations:** The `Location` class encapsulates Euclidean coordinate calculation for distance matching.

---

## 2. Package Structure
```
Member2_Model/
├── model/
│   ├── Driver.java
│   ├── DriverAvailability.java
│   ├── DriverStatus.java
│   ├── Location.java
│   ├── Payment.java
│   ├── PaymentMethod.java
│   ├── PaymentStatus.java
│   ├── Rating.java
│   ├── Rider.java
│   ├── RideRequest.java
│   ├── Trip.java
│   ├── TripStatus.java
│   ├── Vehicle.java
│   └── VehicleType.java
├── Member2Demo.java
└── README.md
```

---

## 3. Classes and Enums

### 3.1 Enums
1. **`VehicleType`**: Available vehicle categories (`BIKE`, `AUTO`, `MINI`, `SEDAN`, `SUV`), per-km pricing (`6`, `10`, `12`, `15`, `20`), capacity, and display names.
2. **`DriverStatus`**: Administrative approval status (`PENDING_APPROVAL`, `APPROVED`, `REJECTED`, `SUSPENDED`).
3. **`DriverAvailability`**: Real-time driver operational state (`AVAILABLE`, `BUSY`, `OFFLINE`).
4. **`TripStatus`**: Trip lifecycle states (`REQUESTED`, `ACCEPTED`, `DRIVER_ARRIVED`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`).
5. **`PaymentMethod`**: Supported tender types (`CASH`, `UPI`, `WALLET`).
6. **`PaymentStatus`**: Financial settlement statuses (`PAID`, `FAILED`, `PENDING`).

### 3.2 Domain Classes
1. **`Location`**: Latitude, longitude, address, and `distanceTo(Location other)` calculating Euclidean distance in kilometers.
2. **`Rider`**: Encapsulates rider personal info, wallet balance (`creditWallet`, `debitWallet`), rolling average rating, total completed trips, and admin review flagging (`flaggedForReview`).
3. **`Driver`**: Encapsulates driver license, active vehicle reference, approval status, availability state, daily cancellation tracking with automatic date resets, total earnings, rolling rating, and automatic suspension if average rating < 3.0 after 20 trips.
4. **`Vehicle`**: Vehicle registration number, make/model, category, driver mapping, and active flag.
5. **`RideRequest`**: Pickup and drop locations, vehicle category, estimated distance and fare, surge multiplier, offered drivers list, and 2-minute request expiration check.
6. **`Trip`**: Lifecycle state, 4-digit OTP, timestamps (`acceptedTime`, `driverArrivedTime`, `startTime`, `endTime`), fare, cancellation fees, and cancellation reasons.
7. **`Payment`**: Breakdown of fare into 80% driver share and 20% platform share (or 100% driver share for cancellation fees), payment method, status, and remarks.
8. **`Rating`**: Score between 1 and 5, feedback text, timestamp, and mutual rating indicators.

---

## 4. Dependencies on Other Team Members
- **Zero Inbound Dependencies:** The Model module is the foundational layer of the architecture. It depends on no other module (`CLI`, `Service`, or `Repository`), ensuring no circular dependencies exist.
- **Downstream Consumers:**
  - **Member 4 (Repository):** Persists and queries these models.
  - **Member 3 (Service):** Enforces business operations on these models.
  - **Member 1 (CLI):** Renders these models in terminal tables and views.

---

## 5. Standalone Compilation & Demo Execution
To verify that the model module compiles and passes all domain invariants independently:

```powershell
cd C:\Users\Nishanth\.gemini\antigravity\scratch\RideBookingSystem
javac -d bin Member2_Model/model/*.java Member2_Model/Member2Demo.java
java -cp bin com.ridebooking.Member2Demo
```
