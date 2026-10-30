package com.ridebooking.repository;

import com.ridebooking.model.Payment;
import com.ridebooking.model.PaymentStatus;
import java.util.*;
import java.util.stream.Collectors;

/**
 * In-memory storage for Payments using HashMap and ArrayList.
 */
public class InMemoryPaymentRepository implements PaymentRepository {
    private final Map<String, Payment> paymentMap = new HashMap<>();

    @Override
    public Payment save(Payment payment) {
        if (payment == null) {
            throw new IllegalArgumentException("Cannot save null payment.");
        }
        paymentMap.put(payment.getId(), payment);
        return payment;
    }

    @Override
    public Optional<Payment> findById(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(paymentMap.get(id));
    }

    @Override
    public Optional<Payment> findByTripId(String tripId) {
        if (tripId == null) return Optional.empty();
        return paymentMap.values().stream()
                .filter(p -> p.getTripId().equalsIgnoreCase(tripId.trim()))
                .findFirst();
    }

    @Override
    public List<Payment> findByRiderId(String riderId) {
        if (riderId == null) return Collections.emptyList();
        return paymentMap.values().stream()
                .filter(p -> p.getRiderId().equalsIgnoreCase(riderId.trim()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public List<Payment> findByDriverId(String driverId) {
        if (driverId == null) return Collections.emptyList();
        return paymentMap.values().stream()
                .filter(p -> p.getDriverId().equalsIgnoreCase(driverId.trim()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public List<Payment> findFailedOrPendingPaymentsByRider(String riderId) {
        if (riderId == null) return Collections.emptyList();
        return paymentMap.values().stream()
                .filter(p -> p.getRiderId().equalsIgnoreCase(riderId.trim()) &&
                             (p.getStatus() == PaymentStatus.FAILED || p.getStatus() == PaymentStatus.PENDING))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public List<Payment> findAll() {
        return new ArrayList<>(paymentMap.values());
    }

    @Override
    public void update(Payment payment) {
        if (payment != null && paymentMap.containsKey(payment.getId())) {
            paymentMap.put(payment.getId(), payment);
        }
    }

    @Override
    public void clear() {
        paymentMap.clear();
    }
}
