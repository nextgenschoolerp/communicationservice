package com.kvs.communication.repo;

import com.kvs.communication.entity.DeviceRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeviceRegistrationRepository extends JpaRepository<DeviceRegistration, UUID> {

    List<DeviceRegistration> findByUserIdAndIsActiveTrue(UUID userId);

    Optional<DeviceRegistration> findByUserIdAndDeviceToken(UUID userId, String deviceToken);
}
