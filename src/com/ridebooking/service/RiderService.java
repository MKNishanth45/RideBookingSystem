package com.ridebooking.service;

import com.ridebooking.exception.*;
import com.ridebooking.model.*;
import com.ridebooking.repository.PaymentRepository;
import com.ridebooking.repository.RiderRepository;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service orchestrating Rider registration, profile management, wallet operations,
 * and unresolved payment compliance.
 */
public class RiderService {
    private final RiderRepository riderRepository;
    private final PaymentRepository paymentRepository;
    private final AtomicInteger idSequence = new AtomicInteger(100);

    public RiderService(RiderRepository riderRepository, PaymentRepository paymentRepository) {
        this.riderRepository = riderRepository;
        this.paymentRepository = paymentRepository;
    }

    /**
     * Registers a new rider ensuring unique phone number.
     */
    public Rider registerRider(String name, String phone, String email, double initialWallet) {
        if (phone == null || !phone.trim().matches("\\d{10}")) {
            throw new InvalidInputException("Phone number must be exactly 10 digits.");
        }

        if (riderRepository.findByPhone(phone.trim()).isPresent()) {
            throw new InvalidInputException("A rider with phone number " + phone.trim() + " already exists.");
        }

        String riderId = "RDR-" + idSequence.incrementAndGet();
        Rider rider = new Rider(riderId, name, phone, email, initialWallet);
        return riderRepository.save(rider);
    }

    public Rider getRiderById(String id) {
        return riderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Rider with ID " + id + " not found."));
    }

    public Rider getRiderByPhone(String phone) {
        return riderRepository.findByPhone(phone)
                .orElseThrow(() -> new EntityNotFoundException("No rider registered with phone number " + phone + "."));
    }

    public List<Rider> getAllRiders() {
        return riderRepository.findAll();
    }

    /**
     * Credits funds into a rider's wallet.
     */
    public void addWalletBalance(String riderId, double amount) {
        if (amount <= 0) {
            throw new InvalidInputException("Top-up amount must be greater than zero.");
        }
        Rider rider = getRiderById(riderId);
        rider.creditWallet(amount);
        riderRepository.update(rider);
    }

    /**
     * Checks if a rider has any outstanding failed or pending payments.
     * Rule: A rider with a failed payment cannot book another ride until cleared.
     */
    public boolean hasUnresolvedPayments(String riderId) {
        return !paymentRepository.findFailedOrPendingPaymentsByRider(riderId).isEmpty();
    }

    public List<Payment> getUnresolvedPayments(String riderId) {
        return paymentRepository.findFailedOrPendingPaymentsByRider(riderId);
    }

    /**
     * Resolves a pending or failed payment.
     */
    public void resolvePayment(String paymentId, PaymentMethod paymentMethod) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new EntityNotFoundException("Payment ID " + paymentId + " not found."));

        if (payment.getStatus() == PaymentStatus.PAID) {
            throw new InvalidInputException("Payment is already settled.");
        }

        Rider rider = getRiderById(payment.getRiderId());

        if (paymentMethod == PaymentMethod.WALLET) {
            if (rider.getWalletBalance() < payment.getAmount()) {
                throw new PaymentException(String.format("Insufficient wallet balance. Required: Rs. %.2f, Available: Rs. %.2f",
                        payment.getAmount(), rider.getWalletBalance()));
            }
            rider.debitWallet(payment.getAmount());
            riderRepository.update(rider);
        }

        payment.setStatus(PaymentStatus.PAID);
        payment.setRemarks("Cleared via " + paymentMethod);
        paymentRepository.update(payment);
    }
}
