package com.ridebooking.repository;

import com.ridebooking.model.Vehicle;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Vehicle entity persistence.
 */
public interface VehicleRepository {
    Vehicle save(Vehicle vehicle);
    Optional<Vehicle> findById(String id);
    Optional<Vehicle> findByRegistrationNumber(String regNo);
    List<Vehicle> findByDriverId(String driverId);
    List<Vehicle> findAll();
    void update(Vehicle vehicle);
    void clear();
}
