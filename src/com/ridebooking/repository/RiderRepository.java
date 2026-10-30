package com.ridebooking.repository;

import com.ridebooking.model.Rider;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Rider entity persistence.
 */
public interface RiderRepository {
    Rider save(Rider rider);
    Optional<Rider> findById(String id);
    Optional<Rider> findByPhone(String phone);
    List<Rider> findAll();
    void update(Rider rider);
    boolean delete(String id);
    void clear();
}
