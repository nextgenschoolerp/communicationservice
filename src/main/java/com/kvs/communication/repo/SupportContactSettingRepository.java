package com.kvs.communication.repo;

import com.kvs.communication.entity.SupportContactSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SupportContactSettingRepository extends JpaRepository<SupportContactSetting, UUID> {

    Optional<SupportContactSetting> findByTenantId(String tenantId);
}
