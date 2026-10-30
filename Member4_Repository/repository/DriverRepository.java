package com.ridebooking.repository;

import com.ridebooking.model.Driver;
import com.ridebooking.model.VehicleType;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Driver entity persistence.
 */
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
    void clear();
}
