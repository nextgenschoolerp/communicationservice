package com.kvs.communication.repo;

import com.kvs.communication.entity.DailyBulletinPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface DailyBulletinPostRepository extends JpaRepository<DailyBulletinPost, UUID> {

    List<DailyBulletinPost> findByTenantIdAndEffectiveDate(UUID tenantId, LocalDate date);

    List<DailyBulletinPost> findByTenantIdAndBulletinTypeAndEffectiveDate(UUID tenantId, String type, LocalDate date);
}
