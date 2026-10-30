package com.ridebooking.repository;

import com.ridebooking.model.Payment;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Payment entity persistence.
 */
public interface PaymentRepository {
    Payment save(Payment payment);
    Optional<Payment> findById(String id);
    Optional<Payment> findByTripId(String tripId);
    List<Payment> findByRiderId(String riderId);
    List<Payment> findByDriverId(String driverId);
    List<Payment> findFailedOrPendingPaymentsByRider(String riderId);
    List<Payment> findAll();
    void update(Payment payment);
    void clear();
}
