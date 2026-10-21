package com.kvs.communication.service;

import com.kvs.core.exception.ResourceNotFoundException;
import com.kvs.communication.dto.*;
import com.kvs.communication.entity.DeviceRegistration;
import com.kvs.communication.entity.NotificationPreference;
import com.kvs.communication.entity.ParentStudentMapping;
import com.kvs.communication.repo.DeviceRegistrationRepository;
import com.kvs.communication.repo.NotificationPreferenceRepository;
import com.kvs.communication.repo.ParentStudentMappingRepository;
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
public class MobileAppService {

    private final DeviceRegistrationRepository deviceRegistrationRepository;
    private final ParentStudentMappingRepository parentStudentMappingRepository;
    private final NotificationPreferenceRepository notificationPreferenceRepository;
    private final CommunicationAuditService auditService;

    // US-09002: Register device push token for real-time mobile app updates
    @Transactional
    public DeviceRegistrationResponse registerDeviceToken(DeviceRegistrationRequest request) {
        DeviceRegistration registration = deviceRegistrationRepository
                .findByUserIdAndDeviceToken(request.getUserId(), request.getDeviceToken())
                .orElse(DeviceRegistration.builder()
                        .tenantId(request.getTenantId())
                        .userId(request.getUserId())
                        .deviceToken(request.getDeviceToken())
                        .build());

        registration.setPlatform(request.getPlatform());
        registration.setAppVersion(request.getAppVersion());
        registration.setIsActive(true);
        registration.setLastActiveAt(Instant.now());
        registration = deviceRegistrationRepository.save(registration);

        auditService.logSuccess("DeviceRegistration", "REGISTER_DEVICE_TOKEN",
                "Registered device token for user " + request.getUserId() + " on platform " + request.getPlatform());

        return DeviceRegistrationResponse.builder()
                .registrationId(registration.getRegistrationId())
                .tenantId(registration.getTenantId())
                .userId(registration.getUserId())
                .deviceToken(registration.getDeviceToken())
                .platform(registration.getPlatform())
                .appVersion(registration.getAppVersion())
                .isActive(registration.getIsActive())
                .lastActiveAt(registration.getLastActiveAt())
                .build();
    }

    // US-09003: Multi-child profile mapping and switching under single parent credentials
    @Transactional
    public ChildProfileResponse mapParentStudent(ParentChildMappingRequest request) {
        ParentStudentMapping mapping = parentStudentMappingRepository
                .findByParentUserIdAndStudentId(request.getParentUserId(), request.getStudentId())
                .orElse(ParentStudentMapping.builder()
                        .tenantId(request.getTenantId())
                        .parentUserId(request.getParentUserId())
                        .studentId(request.getStudentId())
                        .build());

        mapping.setParentPhone(request.getParentPhone());
        mapping.setParentName(request.getParentName());
        mapping.setStudentName(request.getStudentName());
        mapping.setGrade(request.getGrade());
        mapping.setSection(request.getSection());
        mapping.setIsPrimary(request.getIsPrimary() != null ? request.getIsPrimary() : true);
        mapping.setRelationship(request.getRelationship() != null ? request.getRelationship() : "FATHER");

        mapping = parentStudentMappingRepository.save(mapping);

        auditService.logSuccess("ParentStudentMapping", "MAP_PARENT_CHILD",
                "Linked child " + mapping.getStudentName() + " to parent " + mapping.getParentName());

        return mapToChildProfile(mapping);
    }

    @Transactional(readOnly = true)
    public List<ChildProfileResponse> getLinkedChildren(UUID parentUserId) {
        return parentStudentMappingRepository.findByParentUserId(parentUserId).stream()
                .map(this::mapToChildProfile)
                .collect(Collectors.toList());
    }

    // US-09012: Configure notification preferences for parents
    @Transactional
    public NotificationPreferenceResponse configurePreferences(NotificationPreferenceRequest request) {
        NotificationPreference preference = notificationPreferenceRepository
                .findByTenantIdAndUserId(request.getTenantId(), request.getUserId())
                .orElse(NotificationPreference.builder()
                        .tenantId(request.getTenantId())
                        .userId(request.getUserId())
                        .build());

        if (request.getWhatsappEnabled() != null) preference.setWhatsappEnabled(request.getWhatsappEnabled());
        if (request.getSmsEnabled() != null) preference.setSmsEnabled(request.getSmsEnabled());
        if (request.getEmailEnabled() != null) preference.setEmailEnabled(request.getEmailEnabled());
        if (request.getPushEnabled() != null) preference.setPushEnabled(request.getPushEnabled());
        if (request.getQuietHoursStart() != null) preference.setQuietHoursStart(request.getQuietHoursStart());
        if (request.getQuietHoursEnd() != null) preference.setQuietHoursEnd(request.getQuietHoursEnd());

        preference = notificationPreferenceRepository.save(preference);

        auditService.logSuccess("NotificationPreference", "UPDATE_PREFERENCES",
                "Updated alert preferences for user ID " + request.getUserId());

        return NotificationPreferenceResponse.builder()
                .preferenceId(preference.getPreferenceId())
                .tenantId(preference.getTenantId())
                .userId(preference.getUserId())
                .whatsappEnabled(preference.getWhatsappEnabled())
                .smsEnabled(preference.getSmsEnabled())
                .emailEnabled(preference.getEmailEnabled())
                .pushEnabled(preference.getPushEnabled())
                .quietHoursStart(preference.getQuietHoursStart())
                .quietHoursEnd(preference.getQuietHoursEnd())
                .build();
    }

    @Transactional(readOnly = true)
    public NotificationPreferenceResponse getPreferences(UUID tenantId, UUID userId) {
        NotificationPreference preference = notificationPreferenceRepository
                .findByTenantIdAndUserId(tenantId, userId)
                .orElseGet(() -> NotificationPreference.builder()
                        .tenantId(tenantId)
                        .userId(userId)
                        .whatsappEnabled(true)
                        .smsEnabled(true)
                        .emailEnabled(true)
                        .pushEnabled(true)
                        .quietHoursStart("22:00")
                        .quietHoursEnd("07:00")
                        .build());

        return NotificationPreferenceResponse.builder()
                .preferenceId(preference.getPreferenceId())
                .tenantId(preference.getTenantId())
                .userId(preference.getUserId())
                .whatsappEnabled(preference.getWhatsappEnabled())
                .smsEnabled(preference.getSmsEnabled())
                .emailEnabled(preference.getEmailEnabled())
                .pushEnabled(preference.getPushEnabled())
                .quietHoursStart(preference.getQuietHoursStart())
                .quietHoursEnd(preference.getQuietHoursEnd())
                .build();
    }

    private ChildProfileResponse mapToChildProfile(ParentStudentMapping m) {
        return ChildProfileResponse.builder()
                .mappingId(m.getMappingId())
                .tenantId(m.getTenantId())
                .parentUserId(m.getParentUserId())
                .parentName(m.getParentName())
                .studentId(m.getStudentId())
                .studentName(m.getStudentName())
                .grade(m.getGrade())
                .section(m.getSection())
                .isPrimary(m.getIsPrimary())
                .relationship(m.getRelationship())
                .build();
    }
}
