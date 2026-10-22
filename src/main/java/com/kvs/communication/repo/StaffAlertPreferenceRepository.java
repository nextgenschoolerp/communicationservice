package com.kvs.communication.repo;

import com.kvs.communication.entity.StaffAlertPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface StaffAlertPreferenceRepository extends JpaRepository<StaffAlertPreference, UUID> {

    Optional<StaffAlertPreference> findByTenantIdAndUserId(UUID tenantId, String userId);
}
