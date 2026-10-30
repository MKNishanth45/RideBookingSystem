package com.ridebooking.service;

import com.ridebooking.exception.*;
import com.ridebooking.model.*;
import com.ridebooking.repository.*;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service orchestrating the complete lifecycle of trips, fare calculation,
 * OTP verification, state transitions, cancellations, and payment processing.
 */
public class TripService {
    private final TripRepository tripRepository;
    private final RideRequestRepository rideRequestRepository;
    private final RiderRepository riderRepository;
    private final DriverRepository driverRepository;
    private final PaymentRepository paymentRepository;
    private final FareCalculatorService fareCalculatorService;
    private final MatchingService matchingService;

    private final AtomicInteger requestIdSeq = new AtomicInteger(400);
    private final AtomicInteger tripIdSeq = new AtomicInteger(500);
    private final AtomicInteger paymentIdSeq = new AtomicInteger(600);
    private final SecureRandom secureRandom = new SecureRandom();

    public TripService(TripRepository tripRepository, RideRequestRepository rideRequestRepository,
                       RiderRepository riderRepository, DriverRepository driverRepository,
                       PaymentRepository paymentRepository, FareCalculatorService fareCalculatorService,
                       MatchingService matchingService) {
        this.tripRepository = tripRepository;
        this.rideRequestRepository = rideRequestRepository;
        this.riderRepository = riderRepository;
        this.driverRepository = driverRepository;
        this.paymentRepository = paymentRepository;
        this.fareCalculatorService = fareCalculatorService;
        this.matchingService = matchingService;
    }

    /**
     * Creates a new ride request after validating rider payment eligibility and existing active trips.
     */
    public RideRequest createRideRequest(String riderId, Location pickup, Location drop, VehicleType vehicleType) {
        Rider rider = riderRepository.findById(riderId)
                .orElseThrow(() -> new EntityNotFoundException("Rider with ID " + riderId + " not found."));

        // Rule: A rider with a failed payment cannot book another ride until cleared
        List<Payment> unresolved = paymentRepository.findFailedOrPendingPaymentsByRider(rider.getId());
        if (!unresolved.isEmpty()) {
            throw new PaymentException(String.format("Booking Blocked! You have %d outstanding payment(s) totaling Rs. %.2f. Please clear them first.",
                    unresolved.size(), unresolved.stream().mapToDouble(Payment::getAmount).sum()));
        }

        // Rule: Rider cannot have more than one active trip simultaneously
        if (tripRepository.findActiveTripByRiderId(rider.getId()).isPresent()) {
            throw new TripLifecycleException("You already have an ongoing active trip. Complete or cancel it before booking a new ride.");
        }

        double distanceKm = pickup.distanceTo(drop);
        if (distanceKm <= 0.0) {
            throw new InvalidInputException("Pickup and drop locations cannot be identical.");
        }

        int pendingRequestsCount = rideRequestRepository.findPendingRequests().size();
        int availableDriversCount = driverRepository.findAvailableDrivers().size();

        FareEstimate estimate = fareCalculatorService.calculateFare(distanceKm, vehicleType, pendingRequestsCount, availableDriversCount);

        String reqId = "REQ-" + requestIdSeq.incrementAndGet();
        RideRequest request = new RideRequest(reqId, rider.getId(), pickup, drop, vehicleType, distanceKm, estimate.getSurgeMultiplier(), estimate.getTotalFare());
        return rideRequestRepository.save(request);
    }

    /**
     * Driver accepts a ride request.
     * Transitions state from REQUESTED to ACCEPTED.
     * Generates a 4-digit numeric OTP and marks the driver as BUSY.
     */
    public Trip acceptRide(String rideRequestId, String driverId) {
        RideRequest request = rideRequestRepository.findById(rideRequestId)
                .orElseThrow(() -> new EntityNotFoundException("Ride request " + rideRequestId + " not found."));

        if (!"PENDING".equalsIgnoreCase(request.getStatus())) {
            throw new TripLifecycleException("Ride request is no longer available. Status: " + request.getStatus());
        }

        if (request.isExpired()) {
            request.setStatus("EXPIRED");
            rideRequestRepository.update(request);
            throw new TripLifecycleException("Ride request has expired (2-minute window elapsed).");
        }

        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new EntityNotFoundException("Driver with ID " + driverId + " not found."));

        if (driver.getAvailability() != DriverAvailability.AVAILABLE) {
            throw new UnauthorizedException("Driver is not available to accept trips.");
        }
        if (driver.getActiveVehicle() == null) {
            throw new InvalidInputException("Driver has no registered active vehicle.");
        }

        // Generate random 4-digit OTP
        String otp = String.format("%04d", secureRandom.nextInt(10000));

        String tripId = "TRP-" + tripIdSeq.incrementAndGet();
        Trip trip = new Trip(tripId, request.getId(), request.getRiderId(), driver.getId(),
                driver.getActiveVehicle().getId(), request.getPickupLocation(), request.getDropLocation(),
                request.getEstimatedDistanceKm(), request.getEstimatedFare(), otp);

        // Update Driver to BUSY
        driver.setAvailability(DriverAvailability.BUSY);
        driverRepository.update(driver);

        // Update RideRequest to ACCEPTED
        request.setStatus("ACCEPTED");
        rideRequestRepository.update(request);

        return tripRepository.save(trip);
    }

    /**
     * Driver notifies arrival at pickup location.
     * Transitions status from ACCEPTED to DRIVER_ARRIVED.
     */
    public void driverArrived(String tripId, String driverId) {
        Trip trip = getTripById(tripId);

        if (!trip.getDriverId().equalsIgnoreCase(driverId)) {
            throw new UnauthorizedException("Unauthorized: You are not the assigned driver for this trip.");
        }

        if (trip.getStatus() != TripStatus.ACCEPTED) {
            throw new TripLifecycleException("Invalid state transition. Driver can only mark arrived when trip is in ACCEPTED state. Current: " + trip.getStatus());
        }

        trip.setStatus(TripStatus.DRIVER_ARRIVED);
        trip.setDriverArrivedTime(LocalDateTime.now());
        tripRepository.update(trip);
    }

    /**
     * Starts trip upon validation of 4-digit OTP.
     * Transitions status from DRIVER_ARRIVED to IN_PROGRESS.
     */
    public void startTrip(String tripId, String driverId, String enteredOtp) {
        Trip trip = getTripById(tripId);

        if (!trip.getDriverId().equalsIgnoreCase(driverId)) {
            throw new UnauthorizedException("Unauthorized: You are not the assigned driver for this trip.");
        }

        if (trip.getStatus() != TripStatus.DRIVER_ARRIVED) {
            throw new TripLifecycleException("Cannot start trip: Driver has not yet arrived at pickup location. Current status: " + trip.getStatus());
        }

        if (enteredOtp == null || !enteredOtp.trim().equals(trip.getOtp())) {
            throw new InvalidInputException("Invalid OTP! Please enter the correct 4-digit OTP provided by the rider.");
        }

        trip.setStatus(TripStatus.IN_PROGRESS);
        trip.setStartTime(LocalDateTime.now());
        tripRepository.update(trip);
    }

    /**
     * Completes trip and processes payment settlement.
     * Transitions status from IN_PROGRESS to COMPLETED.
     * Revenue Split: 80% to driver, 20% to platform.
     * Driver becomes AVAILABLE again.
     */
    public Payment completeTrip(String tripId, PaymentMethod paymentMethod) {
        Trip trip = getTripById(tripId);

        if (trip.getStatus() != TripStatus.IN_PROGRESS) {
            throw new TripLifecycleException("Only in-progress trips can be completed. Current status: " + trip.getStatus());
        }

        trip.setStatus(TripStatus.COMPLETED);
        trip.setEndTime(LocalDateTime.now());
        tripRepository.update(trip);

        Rider rider = riderRepository.findById(trip.getRiderId())
                .orElseThrow(() -> new EntityNotFoundException("Rider not found: " + trip.getRiderId()));
        Driver driver = driverRepository.findById(trip.getDriverId())
                .orElseThrow(() -> new EntityNotFoundException("Driver not found: " + trip.getDriverId()));

        double fare = trip.getFare();
        double driverShare = fare * 0.80;   // 80% to driver
        double platformShare = fare * 0.20; // 20% to platform

        PaymentStatus paymentStatus = PaymentStatus.PAID;
        String remarks = "Payment successful via " + paymentMethod;

        if (paymentMethod == PaymentMethod.WALLET) {
            if (!rider.debitWallet(fare)) {
                paymentStatus = PaymentStatus.FAILED;
                remarks = "Wallet deduction failed: Insufficient balance (Balance: Rs. " + rider.getWalletBalance() + ")";
            } else {
                riderRepository.update(rider);
            }
        } else if (paymentMethod == PaymentMethod.CASH) {
            // Cash collected by driver
            paymentStatus = PaymentStatus.PAID;
            remarks = "Cash received by driver";
        }

        // Driver receives earnings if payment was successful or cash collected
        if (paymentStatus == PaymentStatus.PAID) {
            driver.addEarnings(driverShare);
        }

        // Update trip count metrics
        rider.incrementCompletedTrips();
        driver.incrementCompletedTrips();

        // Driver becomes Available after successful completion if eligible
        if (driver.getStatus() == DriverStatus.APPROVED) {
            driver.setAvailability(DriverAvailability.AVAILABLE);
        } else {
            driver.setAvailability(DriverAvailability.OFFLINE);
        }

        riderRepository.update(rider);
        driverRepository.update(driver);

        String paymentId = "PAY-" + paymentIdSeq.incrementAndGet();
        Payment payment = new Payment(paymentId, trip.getId(), rider.getId(), driver.getId(),
                fare, driverShare, platformShare, paymentMethod, paymentStatus);
        payment.setRemarks(remarks);
        return paymentRepository.save(payment);
    }

    /**
     * Handles rider trip cancellation before trip start.
     * Enforces cancellation fee policy:
     * - Free cancellation within 2 minutes of acceptance.
     * - Rs. 30 cancellation fee after 2 minutes (100% credited to driver).
     * Cannot cancel during an active trip (IN_PROGRESS).
     */
    public void cancelTripByRider(String tripId, String riderId, String reason) {
        Trip trip = getTripById(tripId);

        if (!trip.getRiderId().equalsIgnoreCase(riderId)) {
            throw new UnauthorizedException("Unauthorized: You cannot cancel another rider's trip.");
        }

        if (trip.getStatus() == TripStatus.IN_PROGRESS) {
            throw new TripLifecycleException("Cancellation not permitted during an active in-progress trip!");
        }

        if (trip.getStatus() == TripStatus.COMPLETED || trip.getStatus() == TripStatus.CANCELLED) {
            throw new TripLifecycleException("Trip is already finalized: " + trip.getStatus());
        }

        LocalDateTime now = LocalDateTime.now();
        double fee = fareCalculatorService.calculateCancellationFee(trip.getAcceptedTime(), now);

        trip.setStatus(TripStatus.CANCELLED);
        trip.setCancelledBy("RIDER");
        trip.setCancellationReason(reason != null ? reason : "Cancelled by rider");
        trip.setCancellationFee(fee);
        tripRepository.update(trip);

        Driver driver = driverRepository.findById(trip.getDriverId())
                .orElseThrow(() -> new EntityNotFoundException("Driver not found: " + trip.getDriverId()));

        // If cancellation fee is applicable, driver gets 100% of the cancellation fee
        if (fee > 0.0) {
            driver.addEarnings(fee);

            // Record cancellation fee transaction
            String payId = "PAY-" + paymentIdSeq.incrementAndGet();
            Payment cancellationPayment = new Payment(payId, trip.getId(), riderId, driver.getId(),
                    fee, fee, 0.0, PaymentMethod.WALLET, PaymentStatus.PENDING);
            cancellationPayment.setRemarks("Rider cancellation fee (>2 mins elapsed)");

            Rider rider = riderRepository.findById(riderId).orElse(null);
            if (rider != null && rider.debitWallet(fee)) {
                cancellationPayment.setStatus(PaymentStatus.PAID);
                cancellationPayment.setRemarks("Cancellation fee debited from wallet");
                riderRepository.update(rider);
            }
            paymentRepository.save(cancellationPayment);
        }

        // Driver becomes Available again
        if (driver.getStatus() == DriverStatus.APPROVED) {
            driver.setAvailability(DriverAvailability.AVAILABLE);
        }
        driverRepository.update(driver);
    }

    /**
     * Handles driver trip cancellation.
     * Rules:
     * - Driver can cancel only BEFORE arrival (ACCEPTED status).
     * - Driver cancellation limit: three rides per day.
     * - Cannot cancel once arrived or during trip.
     */
    public void cancelTripByDriver(String tripId, String driverId, String reason) {
        Trip trip = getTripById(tripId);

        if (!trip.getDriverId().equalsIgnoreCase(driverId)) {
            throw new UnauthorizedException("Unauthorized: You are not the driver assigned to this trip.");
        }

        if (trip.getStatus() != TripStatus.ACCEPTED) {
            throw new TripLifecycleException("Driver can only cancel before arrival (while in ACCEPTED state). Current state: " + trip.getStatus());
        }

        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new EntityNotFoundException("Driver not found: " + driverId));

        // Enforce daily limit: max 3 cancellations per day
        driver.resetDailyCancellationIfNewDay();
        if (driver.getDailyCancellationCount() >= DriverService.MAX_DAILY_CANCELLATIONS) {
            throw new TripLifecycleException("Driver cancellation limit reached! You have already cancelled "
                    + DriverService.MAX_DAILY_CANCELLATIONS + " rides today.");
        }
        driver.incrementDailyCancellation();

        trip.setStatus(TripStatus.CANCELLED);
        trip.setCancelledBy("DRIVER");
        trip.setCancellationReason(reason != null ? reason : "Cancelled by driver");
        tripRepository.update(trip);

        // Driver reset to Available
        driver.setAvailability(DriverAvailability.AVAILABLE);
        driverRepository.update(driver);
    }

    public Trip getTripById(String tripId) {
        return tripRepository.findById(tripId)
                .orElseThrow(() -> new EntityNotFoundException("Trip with ID " + tripId + " not found."));
    }

    public Optional<Trip> getActiveTripForRider(String riderId) {
        return tripRepository.findActiveTripByRiderId(riderId);
    }

    public Optional<Trip> getActiveTripForDriver(String driverId) {
        return tripRepository.findActiveTripByDriverId(driverId);
    }

    public List<Trip> getTripHistoryForRider(String riderId) {
        return tripRepository.findByRiderId(riderId);
    }

    public List<Trip> getTripHistoryForDriver(String driverId) {
        return tripRepository.findByDriverId(driverId);
    }
}
