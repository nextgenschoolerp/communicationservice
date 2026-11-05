package com.kvs.communication.controller;

import com.kvs.core.dto.ApiResponse;
import com.kvs.communication.config.CommunicationRoles;
import com.kvs.communication.dto.SupportContactRequest;
import com.kvs.communication.dto.SupportContactResponse;
import com.kvs.communication.service.SupportContactService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Help & support page: Contact support card (email, phone, opening hours, response time, open now).
 * Everyone reads it; leadership and governance roles save it.
 */
@RestController
@RequestMapping("/api/v1/communication/support/contact")
@RequiredArgsConstructor
public class SupportContactController {

    private final SupportContactService contactService;

    @GetMapping
    @PreAuthorize(CommunicationRoles.ANY_AUTHENTICATED)
    public ResponseEntity<ApiResponse<SupportContactResponse>> get(@RequestParam("tenantId") String tenantId) {
        return ok("Support contact retrieved successfully", contactService.get(tenantId));
    }

    @PutMapping
    @PreAuthorize(CommunicationRoles.LEADERSHIP_SETTINGS)
    public ResponseEntity<ApiResponse<SupportContactResponse>> save(@Valid @RequestBody SupportContactRequest request) {
        return ok("Support contact saved", contactService.save(request));
    }

    private static <T> ResponseEntity<ApiResponse<T>> ok(String message, T data) {
        return ResponseEntity.ok(ApiResponse.<T>builder().success(true).message(message).data(data).build());
    }
}
