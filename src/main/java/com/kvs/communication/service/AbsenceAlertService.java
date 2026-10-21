package com.kvs.communication.service;

import com.kvs.core.exception.ResourceNotFoundException;
import com.kvs.communication.dto.AbsenceAlertRequest;
import com.kvs.communication.dto.AbsenceAlertResponse;
import com.kvs.communication.entity.StudentAbsenceAlert;
import com.kvs.communication.enums.DeliveryStatus;
import com.kvs.communication.repo.StudentAbsenceAlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AbsenceAlertService {

    private final StudentAbsenceAlertRepository absenceAlertRepository;
    private final CommunicationAuditService auditService;

    // US-09001: Dispatch automated alerts within 30 minutes of unexcused student absence
    @Transactional
    public AbsenceAlertResponse dispatchAbsenceAlert(AbsenceAlertRequest request) {
        StudentAbsenceAlert alert = StudentAbsenceAlert.builder()
                .tenantId(request.getTenantId())
                .studentId(request.getStudentId())
                .studentName(request.getStudentName())
                .guardianPhone(request.getGuardianPhone())
                .guardianName(request.getGuardianName())
                .absenceDate(request.getAbsenceDate())
                .alertChannel(request.getAlertChannel() != null ? request.getAlertChannel() : "WHATSAPP")
                .deliveryStatus(DeliveryStatus.SENT)
                .escalated(false)
                .build();

        alert = absenceAlertRepository.save(alert);

        auditService.logSuccess("StudentAbsenceAlert", "DISPATCH_ABSENCE_ALERT",
                "Dispatched 30-minute absence alert for student " + alert.getStudentName() + " to " + alert.getGuardianPhone());

        return mapToResponse(alert);
    }

    @Transactional
    public AbsenceAlertResponse escalateAlert(UUID alertId) {
        StudentAbsenceAlert alert = absenceAlertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Absence alert not found with ID: " + alertId));

        alert.setEscalated(true);
        alert.setEscalatedAt(Instant.now());
        alert = absenceAlertRepository.save(alert);

        auditService.logSuccess("StudentAbsenceAlert", "ESCALATE_ABSENCE_ALERT",
                "Escalated absence alert ID " + alertId + " to school counselor");

        return mapToResponse(alert);
    }

    @Transactional(readOnly = true)
    public List<AbsenceAlertResponse> getAbsenceAlerts(UUID tenantId, LocalDate date) {
        LocalDate searchDate = date != null ? date : LocalDate.now();
        return absenceAlertRepository.findByTenantIdAndAbsenceDate(tenantId, searchDate).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private AbsenceAlertResponse mapToResponse(StudentAbsenceAlert alert) {
        return AbsenceAlertResponse.builder()
                .alertId(alert.getAlertId())
                .tenantId(alert.getTenantId())
                .studentId(alert.getStudentId())
                .studentName(alert.getStudentName())
                .guardianPhone(alert.getGuardianPhone())
                .guardianName(alert.getGuardianName())
                .absenceDate(alert.getAbsenceDate())
                .alertChannel(alert.getAlertChannel())
                .deliveryStatus(alert.getDeliveryStatus())
                .escalated(alert.getEscalated())
                .escalatedAt(alert.getEscalatedAt())
                .createdAt(alert.getCreatedAt())
                .build();
    }
}
