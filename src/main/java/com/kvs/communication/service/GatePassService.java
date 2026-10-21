package com.kvs.communication.service;

import com.kvs.core.exception.BadRequestException;
import com.kvs.core.exception.ResourceNotFoundException;
import com.kvs.communication.dto.*;
import com.kvs.communication.entity.GateAuditLog;
import com.kvs.communication.entity.StudentGatePass;
import com.kvs.communication.enums.GatePassStatus;
import com.kvs.communication.repo.GateAuditLogRepository;
import com.kvs.communication.repo.StudentGatePassRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class GatePassService {

    private final StudentGatePassRepository gatePassRepository;
    private final GateAuditLogRepository gateAuditLogRepository;
    private final CommunicationAuditService auditService;

    // US-09005: Generate digital gate passes on parent apps for early student pickups
    @Transactional
    public GatePassResponse generateGatePass(GatePassRequest request) {
        String qrToken = "GP-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();

        StudentGatePass pass = StudentGatePass.builder()
                .tenantId(request.getTenantId())
                .studentId(request.getStudentId())
                .studentName(request.getStudentName())
                .grade(request.getGrade())
                .section(request.getSection())
                .guardianId(request.getGuardianId())
                .guardianName(request.getGuardianName())
                .pickupPersonName(request.getPickupPersonName())
                .pickupPersonPhone(request.getPickupPersonPhone())
                .reason(request.getReason())
                .qrCode(qrToken)
                .status(GatePassStatus.ISSUED)
                .guardianApproved(true)
                .build();

        pass = gatePassRepository.save(pass);

        auditService.logSuccess("StudentGatePass", "GENERATE_GATE_PASS",
                "Issued gate pass ID " + pass.getPassId() + " for student " + pass.getStudentName() + " with QR " + qrToken);

        return mapToResponse(pass);
    }

    // US-09006: Scan gate pass QR codes at security gates to authorize student departure
    @Transactional
    public GatePassResponse verifyAndAuthorizeDeparture(GatePassVerifyRequest request) {
        StudentGatePass pass = gatePassRepository.findByQrCode(request.getQrCode())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid or unrecognized gate pass QR code: " + request.getQrCode()));

        if (pass.getStatus() == GatePassStatus.DEPARTED) {
            throw new BadRequestException("Gate pass has already been used for departure at " + pass.getExitTime());
        }
        if (pass.getStatus() == GatePassStatus.CANCELLED) {
            throw new BadRequestException("Gate pass has been cancelled by administration or parent");
        }

        Instant now = Instant.now();
        pass.setStatus(GatePassStatus.DEPARTED);
        pass.setSecurityVerifiedBy(request.getSecurityGuardName());
        pass.setVerifiedAt(now);
        pass.setExitTime(now);
        pass = gatePassRepository.save(pass);

        // Record security gate audit entry
        GateAuditLog auditLog = GateAuditLog.builder()
                .tenantId(pass.getTenantId())
                .gateId(request.getGateId() != null ? request.getGateId() : "MAIN_GATE")
                .gateName("Campus Main Perimeter Gate")
                .passId(pass.getPassId())
                .passType("STUDENT_GATE_PASS")
                .personName(pass.getPickupPersonName())
                .eventType("EXIT")
                .outcome("GRANTED")
                .notes("Student " + pass.getStudentName() + " departure authorized by " + request.getSecurityGuardName())
                .timestamp(now)
                .build();
        gateAuditLogRepository.save(auditLog);

        auditService.logSuccess("StudentGatePass", "VERIFY_GATE_PASS",
                "Security verified departure for pass ID " + pass.getPassId() + " by " + request.getSecurityGuardName());

        return mapToResponse(pass);
    }

    // US-09017: Audit security gate entry and exit logs for unauthorized visitor attempts
    @Transactional
    public GateAuditLogResponse recordGateAuditLog(GateAuditLogRequest request) {
        GateAuditLog log = GateAuditLog.builder()
                .tenantId(request.getTenantId())
                .gateId(request.getGateId())
                .gateName(request.getGateName())
                .passId(request.getPassId())
                .passType(request.getPassType())
                .personName(request.getPersonName())
                .eventType(request.getEventType())
                .outcome(request.getOutcome())
                .notes(request.getNotes())
                .timestamp(Instant.now())
                .build();

        log = gateAuditLogRepository.save(log);

        auditService.logSuccess("GateAuditLog", "LOG_GATE_EVENT",
                "Perimeter security logged " + log.getEventType() + " for " + log.getPersonName() + " (Outcome: " + log.getOutcome() + ")");

        return mapToAuditResponse(log);
    }

    @Transactional(readOnly = true)
    public List<GateAuditLogResponse> getGateAuditLogs(UUID tenantId) {
        return gateAuditLogRepository.findByTenantIdOrderByTimestampDesc(tenantId).stream()
                .map(this::mapToAuditResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<GatePassResponse> getStudentGatePasses(UUID tenantId, UUID studentId) {
        return gatePassRepository.findByTenantIdAndStudentId(tenantId, studentId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private GatePassResponse mapToResponse(StudentGatePass pass) {
        return GatePassResponse.builder()
                .passId(pass.getPassId())
                .tenantId(pass.getTenantId())
                .studentId(pass.getStudentId())
                .studentName(pass.getStudentName())
                .grade(pass.getGrade())
                .section(pass.getSection())
                .guardianId(pass.getGuardianId())
                .guardianName(pass.getGuardianName())
                .pickupPersonName(pass.getPickupPersonName())
                .pickupPersonPhone(pass.getPickupPersonPhone())
                .reason(pass.getReason())
                .qrCode(pass.getQrCode())
                .status(pass.getStatus())
                .guardianApproved(pass.getGuardianApproved())
                .securityVerifiedBy(pass.getSecurityVerifiedBy())
                .verifiedAt(pass.getVerifiedAt())
                .exitTime(pass.getExitTime())
                .createdAt(pass.getCreatedAt())
                .build();
    }

    private GateAuditLogResponse mapToAuditResponse(GateAuditLog l) {
        return GateAuditLogResponse.builder()
                .auditId(l.getAuditId())
                .tenantId(l.getTenantId())
                .gateId(l.getGateId())
                .gateName(l.getGateName())
                .passId(l.getPassId())
                .passType(l.getPassType())
                .personName(l.getPersonName())
                .eventType(l.getEventType())
                .outcome(l.getOutcome())
                .notes(l.getNotes())
                .timestamp(l.getTimestamp())
                .build();
    }
}
