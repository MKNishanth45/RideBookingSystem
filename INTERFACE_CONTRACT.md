# RIDE BOOKING SYSTEM – INTERFACE CONTRACT & ARCHITECTURE SPECIFICATION

**Version:** 1.0.0  
**Target Environment:** Java 17+ Console Application  
**Root Package:** `com.ridebooking`  
**Team Architecture:** Layered Architecture (`CLI` → `Service` → `Repository` → `Model`)

---

## 1. ARCHITECTURAL OVERVIEW & RESPONSIBILITY MATRIX

```
+-----------------------------------------------------------+
|              Member 1: CLI Layer (Presentation)           |
|  - ConsoleUi, InputValidator, RiderCli, DriverCli, Main   |
+-----------------------------+-----------------------------+
                              | calls
                              v
+-----------------------------------------------------------+
|             Member 3: Service Layer (Business Logic)      |
|  - RiderService, DriverService, MatchingService,          |
|    TripService, FareCalculatorService, RatingService      |
+-----------------------------+-----------------------------+
                              | calls
                              v
+-----------------------------------------------------------+
|           Member 4: Repository Layer (Data Access)        |
|  - In-Memory Stores (ArrayList, HashMap)                  |
|  - RiderRepo, DriverRepo, VehicleRepo, TripRepo, etc.     |
+-----------------------------+-----------------------------+
                              | manipulates
                              v
+-----------------------------------------------------------+
|             Member 2: Model Layer (Domain Entities)       |
|  - Rider, Driver, Vehicle, Trip, RideRequest, Payment     |
|  - Enums: VehicleType, DriverStatus, TripStatus, etc.     |
+-----------------------------------------------------------+
```

| Member | Package | Primary Responsibility | Key Classes / Interfaces |
|---|---|---|---|
| **Member 1** | `com.ridebooking.cli` | Menus, Input Validation, Table Formatting, Interactive Flow | `ConsoleUi`, `InputValidator`, `RiderCli`, `DriverCli`, `AdminCli`, `MainMenuCli` |
| **Member 2** | `com.ridebooking.model` | Domain Entities, Enums, Encapsulation, Coordinate Math | `Rider`, `Driver`, `Vehicle`, `Location`, `RideRequest`, `Trip`, `Payment`, `Rating`, 6 Enums |
| **Member 3** | `com.ridebooking.service` | Business Rules, Distance Matching, Fares, State Transitions | `RiderService`, `DriverService`, `MatchingService`, `TripService`, `FareCalculatorService`, `RatingService` |
| **Member 4** | `com.ridebooking.repository` | Pure In-Memory Data Storage & Querying (CRUD) | `RiderRepository`, `DriverRepository`, `VehicleRepository`, `RideRequestRepository`, `TripRepository`, `PaymentRepository`, `RatingRepository` |

---

## 2. MEMBER 2: MODEL CONTRACT (`com.ridebooking.model`)

### 2.1 Enums

#### `VehicleType`
```java
public enum VehicleType {
    BIKE(6.0, 1, "Bike"),
    AUTO(10.0, 3, "Auto"),
    MINI(12.0, 4, "Mini Hatchback"),
    SEDAN(15.0, 4, "Prime Sedan"),
    SUV(20.0, 6, "Prime SUV");

    public double getRatePerKm();
    public int getCapacity();
    public String getDisplayName();
}
```

#### `DriverStatus`
```java
public enum DriverStatus {
    PENDING_APPROVAL,
    APPROVED,
    REJECTED,
    SUSPENDED
}
```

#### `DriverAvailability`
```java
public enum DriverAvailability {
    AVAILABLE,
    BUSY,
    OFFLINE
}
```

#### `TripStatus`
```java
public enum TripStatus {
    REQUESTED,
    ACCEPTED,
    DRIVER_ARRIVED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}
```

#### `PaymentMethod`
```java
public enum PaymentMethod {
    CASH,
    UPI,
    WALLET
}
```

#### `PaymentStatus`
```java
public enum PaymentStatus {
    PAID,
    FAILED,
    PENDING
}
```

### 2.2 Domain Classes

#### `Location`
```java
public class Location {
    public Location(String address, double latitude, double longitude);
    public String getAddress();
    public double getLatitude();
    public double getLongitude();
    public double distanceTo(Location other); // Euclidean distance = sqrt((x2-x1)^2 + (y2-y1)^2)
}
```

#### `Rider`
```java
public class Rider {
    public Rider(String id, String name, String phone, String email, double walletBalance);
    public String getId();
    public String getName();
    public String getPhone();
    public String getEmail();
    public double getWalletBalance();
    public void setWalletBalance(double walletBalance);
    public void creditWallet(double amount);
    public boolean debitWallet(double amount);
    public double getAverageRating();
    public int getCompletedTripsCount();
    public int getTotalRatingsCount();
    public boolean isFlaggedForReview();
    public void setFlaggedForReview(boolean flaggedForReview);
    public void updateRating(int newScore);
    public void incrementCompletedTrips();
    public LocalDateTime getCreatedAt();
}
```

#### `Driver`
```java
public class Driver {
    public Driver(String id, String name, String phone, String email, String licenseNumber, Location initialLocation);
    public String getId();
    public String getName();
    public String getPhone();
    public String getEmail();
    public String getLicenseNumber();
    public DriverStatus getStatus();
    public void setStatus(DriverStatus status);
    public DriverAvailability getAvailability();
    public void setAvailability(DriverAvailability availability);
    public Vehicle getActiveVehicle();
    public void setActiveVehicle(Vehicle activeVehicle);
    public Location getCurrentLocation();
    public void setCurrentLocation(Location currentLocation);
    public double getAverageRating();
    public int getCompletedTripsCount();
    public int getTotalRatingsCount();
    public int getDailyCancellationCount();
    public LocalDate getLastCancellationDate();
    public void resetDailyCancellationIfNewDay();
    public void incrementDailyCancellation();
    public double getTotalEarnings();
    public void addEarnings(double amount);
    public void updateRating(int newScore);
    public void incrementCompletedTrips();
    public LocalDateTime getCreatedAt();
}
```

#### `Vehicle`
```java
public class Vehicle {
    public Vehicle(String id, String driverId, String registrationNumber, String makeModel, VehicleType vehicleType);
    public String getId();
    public String getDriverId();
    public String getRegistrationNumber();
    public String getMakeModel();
    public VehicleType getVehicleType();
    public boolean isActive();
    public void setActive(boolean active);
}
```

#### `RideRequest`
```java
public class RideRequest {
    public RideRequest(String id, String riderId, Location pickupLocation, Location dropLocation, VehicleType vehicleType, double estimatedDistanceKm, double surgeMultiplier, double estimatedFare);
    public String getId();
    public String getRiderId();
    public Location getPickupLocation();
    public Location getDropLocation();
    public VehicleType getVehicleType();
    public double getEstimatedDistanceKm();
    public double getSurgeMultiplier();
    public double getEstimatedFare();
    public LocalDateTime getRequestedTime();
    public LocalDateTime getExpiryTime();
    public boolean isExpired();
    public List<String> getOfferedDriverIds();
    public void addOfferedDriver(String driverId);
    public String getStatus(); // PENDING, ACCEPTED, EXPIRED, CANCELLED
    public void setStatus(String status);
}
```

#### `Trip`
```java
public class Trip {
    public Trip(String id, String rideRequestId, String riderId, String driverId, String vehicleId, Location pickupLocation, Location dropLocation, double distanceKm, double fare, String otp);
    public String getId();
    public String getRideRequestId();
    public String getRiderId();
    public String getDriverId();
    public String getVehicleId();
    public Location getPickupLocation();
    public Location getDropLocation();
    public double getDistanceKm();
    public double getFare();
    public double getCancellationFee();
    public void setCancellationFee(double fee);
    public String getOtp();
    public TripStatus getStatus();
    public void setStatus(TripStatus status);
    public String getCancelledBy();
    public void setCancelledBy(String cancelledBy); // "RIDER", "DRIVER", "NONE"
    public String getCancellationReason();
    public void setCancellationReason(String reason);
    public LocalDateTime getRequestedTime();
    public LocalDateTime getAcceptedTime();
    public void setAcceptedTime(LocalDateTime acceptedTime);
    public LocalDateTime getDriverArrivedTime();
    public void setDriverArrivedTime(LocalDateTime arrivedTime);
    public LocalDateTime getStartTime();
    public void setStartTime(LocalDateTime startTime);
    public LocalDateTime getEndTime();
    public void setEndTime(LocalDateTime endTime);
}
```

#### `Payment`
```java
public class Payment {
    public Payment(String id, String tripId, String riderId, String driverId, double amount, double driverShare, double platformShare, PaymentMethod method, PaymentStatus status);
    public String getId();
    public String getTripId();
    public String getRiderId();
    public String getDriverId();
    public double getAmount();
    public double getDriverShare();
    public double getPlatformShare();
    public PaymentMethod getMethod();
    public PaymentStatus getStatus();
    public void setStatus(PaymentStatus status);
    public LocalDateTime getTimestamp();
    public String getRemarks();
    public void setRemarks(String remarks);
}
```

#### `Rating`
```java
public class Rating {
    public Rating(String id, String tripId, String fromUserId, String toUserId, boolean isRiderRatingDriver, int score, String feedback);
    public String getId();
    public String getTripId();
    public String getFromUserId();
    public String getToUserId();
    public boolean isRiderRatingDriver();
    public int getScore();
    public String getFeedback();
    public LocalDateTime getTimestamp();
}
```

---

## 3. MEMBER 4: REPOSITORY CONTRACT (`com.ridebooking.repository`)

```java
public interface RiderRepository {
    Rider save(Rider rider);
    Optional<Rider> findById(String id);
    Optional<Rider> findByPhone(String phone);
    List<Rider> findAll();
    void update(Rider rider);
    boolean delete(String id);
}

public interface DriverRepository {
    Driver save(Driver driver);
    Optional<Driver> findById(String id);
    Optional<Driver> findByPhone(String phone);
    Optional<Driver> findByLicenseNumber(String licenseNumber);
    List<Driver> findAll();
    List<Driver> findAvailableDrivers();
    List<Driver> findAvailableDriversByVehicleType(VehicleType vehicleType);
    void update(Driver driver);
    boolean delete(String id);
}

public interface VehicleRepository {
    Vehicle save(Vehicle vehicle);
    Optional<Vehicle> findById(String id);
    Optional<Vehicle> findByRegistrationNumber(String regNo);
    List<Vehicle> findByDriverId(String driverId);
    List<Vehicle> findAll();
    void update(Vehicle vehicle);
}

public interface RideRequestRepository {
    RideRequest save(RideRequest request);
    Optional<RideRequest> findById(String id);
    List<RideRequest> findPendingRequests();
    List<RideRequest> findByRiderId(String riderId);
    List<RideRequest> findAll();
    void update(RideRequest request);
}

public interface TripRepository {
    Trip save(Trip trip);
    Optional<Trip> findById(String id);
    List<Trip> findByRiderId(String riderId);
    List<Trip> findByDriverId(String driverId);
    Optional<Trip> findActiveTripByRiderId(String riderId);
    Optional<Trip> findActiveTripByDriverId(String driverId);
    List<Trip> findAll();
    void update(Trip trip);
}

public interface PaymentRepository {
    Payment save(Payment payment);
    Optional<Payment> findById(String id);
    Optional<Payment> findByTripId(String tripId);
    List<Payment> findByRiderId(String riderId);
    List<Payment> findByDriverId(String driverId);
    List<Payment> findFailedOrPendingPaymentsByRider(String riderId);
    List<Payment> findAll();
    void update(Payment payment);
}

public interface RatingRepository {
    Rating save(Rating rating);
    Optional<Rating> findById(String id);
    List<Rating> findByTripId(String tripId);
    Optional<Rating> findByTripAndRater(String tripId, String fromUserId);
    List<Rating> findByToUserId(String toUserId);
    List<Rating> findAll();
}
```

---

## 4. MEMBER 3: SERVICE CONTRACT (`com.ridebooking.service`)

### 4.1 Fare Calculation Service (`FareCalculatorService`)
```java
public class FareEstimate {
    private final double distanceKm;
    private final double estimatedMinutes;
    private final double baseFare;
    private final double distanceFare;
    private final double timeFare;
    private final double minFareAdjustment;
    private final double surgeMultiplier;
    private final double totalFare;
    // Getters and formatted breakdown
}

public class FareCalculatorService {
    public FareEstimate calculateFare(double distanceKm, VehicleType vehicleType, int pendingRequestsCount, int availableDriversCount);
    public double calculateCancellationFee(LocalDateTime acceptedTime, LocalDateTime cancelTime); // Free <= 2 mins, Rs. 30 after 2 mins
}
```

### 4.2 Rider Service (`RiderService`)
```java
public class RiderService {
    public Rider registerRider(String name, String phone, String email, double initialWallet);
    public Rider getRiderById(String riderId);
    public Rider getRiderByPhone(String phone);
    public void addWalletBalance(String riderId, double amount);
    public boolean hasUnresolvedPayments(String riderId);
    public List<Payment> getUnresolvedPayments(String riderId);
    public void resolvePayment(String paymentId, PaymentMethod method);
}
```

### 4.3 Driver Service (`DriverService`)
```java
public class DriverService {
    public Driver registerDriver(String name, String phone, String email, String licenseNumber, Location initialLocation);
    public Driver getDriverById(String driverId);
    public Driver getDriverByPhone(String phone);
    public Vehicle registerVehicle(String driverId, String regNumber, String makeModel, VehicleType vehicleType);
    public void approveDriver(String driverId);
    public void setDriverAvailability(String driverId, DriverAvailability availability);
    public void updateDriverLocation(String driverId, Location newLocation);
    public void recordDriverCancellation(String driverId);
    public void checkDriverSuspension(String driverId);
}
```

### 4.4 Matching Service (`MatchingService`)
```java
public class MatchingCandidate {
    private final Driver driver;
    private final double distanceKm;
    // Getters
}

public class MatchingService {
    public List<MatchingCandidate> findEligibleDrivers(Location pickupLocation, VehicleType vehicleType, List<String> excludedDriverIds);
}
```

### 4.5 Trip Service (`TripService`)
```java
public class TripService {
    public RideRequest requestRide(String riderId, Location pickup, Location drop, VehicleType vehicleType);
    public Trip acceptRide(String rideRequestId, String driverId);
    public void driverArrive(String tripId, String driverId);
    public void startTrip(String tripId, String driverId, String enteredOtp);
    public Payment completeTrip(String tripId, PaymentMethod paymentMethod);
    public void cancelTripByRider(String tripId, String riderId, String reason);
    public void cancelTripByDriver(String tripId, String driverId, String reason);
    public Optional<Trip> getActiveTripForRider(String riderId);
    public Optional<Trip> getActiveTripForDriver(String driverId);
}
```

### 4.6 Rating Service (`RatingService`)
```java
public class RatingService {
    public Rating submitRating(String tripId, String fromUserId, int score, String feedback);
}
```

---

## 5. EXCEPTIONS (`com.ridebooking.exception`)
- `RideBookingException` (base runtime exception)
- `EntityNotFoundException` (when an entity is missing)
- `InvalidInputException` (validation failure)
- `TripLifecycleException` (illegal state transition or timing violation)
- `PaymentException` (insufficient funds, payment failure)
- `UnauthorizedException` (acting on another user's trip, or unapproved driver actions)

---

## 6. MEMBER 1: CLI INTERACTION PROTOCOL (`com.ridebooking.cli`)
- Uses `Scanner` with reusable safe reading utilities (`InputValidator`).
- Handles menu navigation, registration, login, interactive simulation of ride offers and acceptance.
- Formats monetary values as `Rs. XX.XX`.
- Displays ASCII headers, formatted box borders, and tabular lists.
- Includes an **Admin / Simulation Mode** and **Automated System Test Runner** for testing all 20 business scenarios.
