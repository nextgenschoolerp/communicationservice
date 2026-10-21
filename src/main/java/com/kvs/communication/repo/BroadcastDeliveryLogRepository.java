package com.kvs.communication.repo;

import com.kvs.communication.entity.BroadcastDeliveryLog;
import com.kvs.communication.enums.DeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BroadcastDeliveryLogRepository extends JpaRepository<BroadcastDeliveryLog, UUID> {

    List<BroadcastDeliveryLog> findByBroadcastId(UUID broadcastId);

    List<BroadcastDeliveryLog> findByBroadcastIdAndDeliveryStatus(UUID broadcastId, DeliveryStatus status);
}
