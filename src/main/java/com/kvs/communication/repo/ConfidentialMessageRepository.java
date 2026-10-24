package com.kvs.communication.repo;

import com.kvs.communication.entity.ConfidentialMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ConfidentialMessageRepository extends JpaRepository<ConfidentialMessage, UUID> {

    List<ConfidentialMessage> findByThreadIdOrderByCreatedAtAsc(UUID threadId);

    List<ConfidentialMessage> findByRecipientIdOrderByCreatedAtDesc(String recipientId);

    List<ConfidentialMessage> findBySenderIdOrderByCreatedAtDesc(String senderId);

    List<ConfidentialMessage> findBySenderIdOrRecipientIdOrderByCreatedAtDesc(String senderId, String recipientId);
}
