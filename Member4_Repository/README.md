# Member 4: Repository Module Documentation

## 1. Overview and Responsibilities
Member 4 is responsible for the Data Access Layer (DAL) of the Ride Booking System. This module provides a clean separation of concerns:
- **Pure Data Access:** Repositories only perform storage, querying, indexing, updating, and deletion. They enforce **no business rules** (business rules reside strictly in Member 3's Service Layer).
- **Interface Abstraction:** Every entity has a public interface (e.g., `RiderRepository`) and a decoupled in-memory implementation (`InMemoryRiderRepository`).
- **High-Performance In-Memory Collections:** Lookups by primary key (`id`) are backed by `HashMap<String, T>` for $O(1)$ efficiency. Filtered queries (by phone, vehicle type, pending status) return detached `ArrayList<T>` copies to prevent external collection mutation.

---

## 2. Package Structure
```
Member4_Repository/
├── repository/
│   ├── DriverRepository.java
│   ├── InMemoryDriverRepository.java
│   ├── InMemoryPaymentRepository.java
│   ├── InMemoryRatingRepository.java
│   ├── InMemoryRideRequestRepository.java
│   ├── InMemoryRiderRepository.java
│   ├── InMemoryTripRepository.java
│   ├── InMemoryVehicleRepository.java
│   ├── PaymentRepository.java
│   ├── RatingRepository.java
│   ├── RideRequestRepository.java
│   ├── RiderRepository.java
│   ├── TripRepository.java
│   └── VehicleRepository.java
├── Member4Demo.java
└── README.md
```

---

## 3. Repositories and Methods

### 3.1 `RiderRepository` & `InMemoryRiderRepository`
- `save(Rider rider)`: Stores rider.
- `findById(String id)`: Retrieves rider by ID.
- `findByPhone(String phone)`: Finds rider by unique phone.
- `findAll()`: Returns all registered riders.
- `update(Rider rider)`: Replaces state of existing rider.
- `delete(String id)`: Removes rider.

### 3.2 `DriverRepository` & `InMemoryDriverRepository`
- `save(Driver driver)`: Stores driver.
- `findById(String id)`: Retrieves driver by ID.
- `findByPhone(String phone)`: Finds driver by phone.
- `findByLicenseNumber(String licenseNumber)`: Finds driver by license.
- `findAll()`: Returns all registered drivers.
- `findAvailableDrivers()`: Returns approved drivers with active vehicles who are currently available.
- `findAvailableDriversByVehicleType(VehicleType vehicleType)`: Filters available drivers matching requested vehicle category.
- `update(Driver driver)`: Updates driver record.
- `delete(String id)`: Removes driver.

### 3.3 `VehicleRepository` & `InMemoryVehicleRepository`
- `save(Vehicle vehicle)`: Registers a vehicle.
- `findById(String id)`: Retrieves vehicle by ID.
- `findByRegistrationNumber(String regNo)`: Uniqueness check on license plate.
- `findByDriverId(String driverId)`: Retrieves all vehicles belonging to a driver.
- `findAll()`: Lists all vehicles.
- `update(Vehicle vehicle)`: Updates vehicle record.

### 3.4 `RideRequestRepository` & `InMemoryRideRequestRepository`
- `save(RideRequest request)`: Saves new ride request.
- `findById(String id)`: Retrieves request by ID.
- `findPendingRequests()`: Returns all non-expired pending requests.
- `findByRiderId(String riderId)`: Queries requests created by a rider.
- `findAll()`: Returns all requests.
- `update(RideRequest request)`: Updates request status.

### 3.5 `TripRepository` & `InMemoryTripRepository`
- `save(Trip trip)`: Saves trip record.
- `findById(String id)`: Retrieves trip by ID.
- `findByRiderId(String riderId)`: Returns trip history for rider.
- `findByDriverId(String driverId)`: Returns trip history for driver.
- `findActiveTripByRiderId(String riderId)`: Finds ongoing trip for rider (excluding COMPLETED/CANCELLED).
- `findActiveTripByDriverId(String driverId)`: Finds ongoing trip for driver.
- `findAll()`: Lists all trips.
- `update(Trip trip)`: Updates trip lifecycle state.

### 3.6 `PaymentRepository` & `InMemoryPaymentRepository`
- `save(Payment payment)`: Records transaction.
- `findById(String id)`: Retrieves payment by ID.
- `findByTripId(String tripId)`: Retrieves payment associated with a trip.
- `findByRiderId(String riderId)`: Lists all payments for rider.
- `findByDriverId(String driverId)`: Lists all payments credited to driver.
- `findFailedOrPendingPaymentsByRider(String riderId)`: Identifies unresolved dues blocking new bookings.
- `findAll()`: Lists all payments.
- `update(Payment payment)`: Updates payment status.

### 3.7 `RatingRepository` & `InMemoryRatingRepository`
- `save(Rating rating)`: Records rating entry.
- `findById(String id)`: Retrieves rating by ID.
- `findByTripId(String tripId)`: Lists ratings for a trip.
- `findByTripAndRater(String tripId, String fromUserId)`: Enforces 1 rating per person per trip.
- `findByToUserId(String toUserId)`: Lists ratings received by a user.
- `findAll()`: Lists all ratings.

---

## 4. Dependencies on Other Team Members
- **Upstream Dependency:** Depends on **Member 2 (Model Module)** for domain entities (`Rider`, `Driver`, `Vehicle`, `Trip`, etc.).
- **Downstream Consumers:** Injected into **Member 3 (Service Layer)** via constructor injection.

---

## 5. Standalone Compilation & Demo Execution
To verify that all repositories compile and perform in-memory operations correctly:

```powershell
cd C:\Users\Nishanth\.gemini\antigravity\scratch\RideBookingSystem
javac -d bin Member2_Model/model/*.java Member4_Repository/repository/*.java Member4_Repository/Member4Demo.java
java -cp bin com.ridebooking.Member4Demo
```
