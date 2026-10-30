package com.ridebooking.repository;

import com.ridebooking.model.Driver;
import com.ridebooking.model.DriverAvailability;
import com.ridebooking.model.DriverStatus;
import com.ridebooking.model.VehicleType;
import java.util.*;
import java.util.stream.Collectors;

/**
 * In-memory storage for Drivers using HashMap and ArrayList.
 */
public class InMemoryDriverRepository implements DriverRepository {
    private final Map<String, Driver> driverMap = new HashMap<>();

    @Override
    public Driver save(Driver driver) {
        if (driver == null) {
            throw new IllegalArgumentException("Cannot save null driver.");
        }
        driverMap.put(driver.getId(), driver);
        return driver;
    }

    @Override
    public Optional<Driver> findById(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(driverMap.get(id));
    }

    @Override
    public Optional<Driver> findByPhone(String phone) {
        if (phone == null) return Optional.empty();
        return driverMap.values().stream()
                .filter(d -> d.getPhone().equals(phone.trim()))
                .findFirst();
    }

    @Override
    public Optional<Driver> findByLicenseNumber(String licenseNumber) {
        if (licenseNumber == null) return Optional.empty();
        return driverMap.values().stream()
                .filter(d -> d.getLicenseNumber().equalsIgnoreCase(licenseNumber.trim()))
                .findFirst();
    }

    @Override
    public List<Driver> findAll() {
        return new ArrayList<>(driverMap.values());
    }

    @Override
    public List<Driver> findAvailableDrivers() {
        return driverMap.values().stream()
                .filter(d -> d.getAvailability() == DriverAvailability.AVAILABLE &&
                             d.getStatus() == DriverStatus.APPROVED &&
                             d.getActiveVehicle() != null)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public List<Driver> findAvailableDriversByVehicleType(VehicleType vehicleType) {
        if (vehicleType == null) return Collections.emptyList();
        return findAvailableDrivers().stream()
                .filter(d -> d.getActiveVehicle() != null && d.getActiveVehicle().getVehicleType() == vehicleType)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public void update(Driver driver) {
        if (driver != null && driverMap.containsKey(driver.getId())) {
            driverMap.put(driver.getId(), driver);
        }
    }

    @Override
    public boolean delete(String id) {
        if (id == null) return false;
        return driverMap.remove(id) != null;
    }

    @Override
    public void clear() {
        driverMap.clear();
    }
}
