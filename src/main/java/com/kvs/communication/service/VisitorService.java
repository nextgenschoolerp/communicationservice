package com.kvs.communication.service;

import com.kvs.core.exception.BadRequestException;
import com.kvs.core.exception.ResourceNotFoundException;
import com.kvs.communication.dto.*;
import com.kvs.communication.entity.AppointmentSlot;
import com.kvs.communication.entity.VisitorAppointment;
import com.kvs.communication.repo.AppointmentSlotRepository;
import com.kvs.communication.repo.VisitorAppointmentRepository;
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
public class VisitorService {

    private final VisitorAppointmentRepository visitorRepository;
    private final AppointmentSlotRepository slotRepository;
    private final CommunicationAuditService auditService;

    // US-09007: Register visitor appointments and issue digital visitor passes at reception
    @Transactional
    public VisitorAppointmentResponse registerVisitor(VisitorAppointmentRequest request) {
        String badge = "VIS-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        String hostStaffName = (request.getHostStaffName() != null && !request.getHostStaffName().isBlank())
                ? request.getHostStaffName()
                : "School Administration";
        Instant appointmentTime = request.getAppointmentTime() != null
                ? request.getAppointmentTime()
                : Instant.now();

        UUID hostStaffUuid = null;
        if (request.getHostStaffId() != null && !request.getHostStaffId().isBlank()) {
            try {
                hostStaffUuid = UUID.fromString(request.getHostStaffId());
            } catch (IllegalArgumentException ignored) {}
        }

        VisitorAppointment appointment = VisitorAppointment.builder()
                .tenantId(request.getTenantId())
                .visitorName(request.getVisitorName())
                .visitorPhone(request.getVisitorPhone())
                .visitorIdType(request.getVisitorIdType())
                .visitorIdNumber(request.getVisitorIdNumber())
                .purpose(request.getPurpose())
                .hostStaffId(hostStaffUuid)
                .hostStaffName(hostStaffName)
                .appointmentTime(appointmentTime)
                .badgeNumber(badge)
                .status("SCHEDULED")
                .build();

        appointment = visitorRepository.save(appointment);

        auditService.logSuccess("VisitorAppointment", "REGISTER_VISITOR",
                "Registered visitor " + appointment.getVisitorName() + " to visit " + appointment.getHostStaffName() + " (Badge: " + badge + ")");

        return mapToResponse(appointment);
    }

    @Transactional
    public VisitorAppointmentResponse checkInVisitor(UUID appointmentId, VisitorCheckInRequest request) {
        VisitorAppointment appointment = visitorRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Visitor appointment not found with ID: " + appointmentId));

        appointment.setStatus("CHECKED_IN");
        appointment.setCheckInTime(Instant.now());
        if (request.getBadgeNumber() != null && !request.getBadgeNumber().isBlank()) {
            appointment.setBadgeNumber(request.getBadgeNumber());
        }
        appointment = visitorRepository.save(appointment);

        auditService.logSuccess("VisitorAppointment", "CHECK_IN_VISITOR",
                "Checked in visitor " + appointment.getVisitorName() + " with badge " + appointment.getBadgeNumber());

        return mapToResponse(appointment);
    }

    @Transactional
    public VisitorAppointmentResponse checkOutVisitor(UUID appointmentId) {
        VisitorAppointment appointment = visitorRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Visitor appointment not found with ID: " + appointmentId));

        appointment.setStatus("CHECKED_OUT");
        appointment.setCheckOutTime(Instant.now());
        appointment = visitorRepository.save(appointment);

        auditService.logSuccess("VisitorAppointment", "CHECK_OUT_VISITOR",
                "Checked out visitor " + appointment.getVisitorName());

        return mapToResponse(appointment);
    }

    @Transactional(readOnly = true)
    public List<VisitorAppointmentResponse> listAppointments(String tenantId) {
        return visitorRepository.findByTenantIdOrderByAppointmentTimeDesc(tenantId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // US-09016: Manage appointment booking slots for principal/leadership meetings
    @Transactional
    public AppointmentSlotResponse createSlot(AppointmentSlotRequest request) {
        AppointmentSlot slot = AppointmentSlot.builder()
                .tenantId(request.getTenantId())
                .hostUserId(request.getHostUserId())
                .hostName(request.getHostName())
                .slotDate(request.getSlotDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .isBooked(false)
                .build();

        slot = slotRepository.save(slot);

        auditService.logSuccess("AppointmentSlot", "CREATE_SLOT",
                "Created meeting slot on " + slot.getSlotDate() + " (" + slot.getStartTime() + " - " + slot.getEndTime() + ") with " + slot.getHostName());

        return mapToSlotResponse(slot);
    }

    @Transactional
    public AppointmentSlotResponse bookSlot(UUID slotId, BookSlotRequest request) {
        AppointmentSlot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment slot not found with ID: " + slotId));

        if (Boolean.TRUE.equals(slot.getIsBooked())) {
            throw new BadRequestException("Appointment slot on " + slot.getSlotDate() + " at " + slot.getStartTime() + " is already booked");
        }

        slot.setIsBooked(true);
        slot.setBookedByParentId(request.getParentId());
        slot.setBookedByParentName(request.getParentName());
        slot = slotRepository.save(slot);

        auditService.logSuccess("AppointmentSlot", "BOOK_SLOT",
                "Booked slot ID " + slotId + " by parent " + request.getParentName());

        return mapToSlotResponse(slot);
    }

    @Transactional(readOnly = true)
    public List<AppointmentSlotResponse> getAvailableSlots(String tenantId, LocalDate date) {
        LocalDate searchDate = date != null ? date : LocalDate.now();
        return slotRepository.findByTenantIdAndSlotDateOrderByStartTimeAsc(tenantId, searchDate).stream()
                .map(this::mapToSlotResponse)
                .collect(Collectors.toList());
    }

    private VisitorAppointmentResponse mapToResponse(VisitorAppointment v) {
        return VisitorAppointmentResponse.builder()
                .appointmentId(v.getAppointmentId())
                .tenantId(v.getTenantId())
                .visitorName(v.getVisitorName())
                .visitorPhone(v.getVisitorPhone())
                .visitorIdType(v.getVisitorIdType())
                .visitorIdNumber(v.getVisitorIdNumber())
                .purpose(v.getPurpose())
                .hostStaffId(v.getHostStaffId())
                .hostStaffName(v.getHostStaffName())
                .appointmentTime(v.getAppointmentTime())
                .checkInTime(v.getCheckInTime())
                .checkOutTime(v.getCheckOutTime())
                .badgeNumber(v.getBadgeNumber())
                .visitorBadgeNumber(v.getBadgeNumber())
                .status(v.getStatus())
                .createdAt(v.getCreatedAt())
                .build();
    }

    private AppointmentSlotResponse mapToSlotResponse(AppointmentSlot s) {
        return AppointmentSlotResponse.builder()
                .slotId(s.getSlotId())
                .tenantId(s.getTenantId())
                .hostUserId(s.getHostUserId())
                .hostName(s.getHostName())
                .slotDate(s.getSlotDate())
                .startTime(s.getStartTime())
                .endTime(s.getEndTime())
                .isBooked(s.getIsBooked())
                .bookedByParentId(s.getBookedByParentId())
                .bookedByParentName(s.getBookedByParentName())
                .build();
    }
}
