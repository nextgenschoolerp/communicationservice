package com.kvs.communication.controller;

import com.kvs.core.dto.ApiResponse;
import com.kvs.communication.config.CommunicationRoles;
import com.kvs.communication.dto.*;
import com.kvs.communication.service.SupportTicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/** Help & support page: support tickets raised by school staff. */
@RestController
@RequestMapping("/api/v1/communication/support/tickets")
@RequiredArgsConstructor
public class SupportTicketController {

    private final SupportTicketService supportService;

    @GetMapping
    @PreAuthorize(CommunicationRoles.ANY_AUTHENTICATED)
    public ResponseEntity<ApiResponse<List<SupportTicketResponse>>> list(@RequestParam("tenantId") String tenantId,
                                                                         @RequestParam(name = "requesterUserId", required = false) String requesterUserId) {
        return ok("Support tickets retrieved successfully", supportService.list(tenantId, requesterUserId));
    }

    @PostMapping
    @PreAuthorize(CommunicationRoles.ANY_AUTHENTICATED)
    public ResponseEntity<ApiResponse<SupportTicketResponse>> create(@Valid @RequestBody SupportTicketRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.<SupportTicketResponse>builder()
                .success(true).message("Support ticket created").data(supportService.create(request)).build());
    }

    @GetMapping("/{ticketId}")
    @PreAuthorize(CommunicationRoles.ANY_AUTHENTICATED)
    public ResponseEntity<ApiResponse<SupportTicketResponse>> get(@PathVariable("ticketId") UUID ticketId) {
        return ok("Support ticket retrieved successfully", supportService.get(ticketId));
    }

    @PostMapping("/{ticketId}/messages")
    @PreAuthorize(CommunicationRoles.ANY_AUTHENTICATED)
    public ResponseEntity<ApiResponse<SupportTicketResponse>> reply(@PathVariable("ticketId") UUID ticketId,
                                                                    @Valid @RequestBody SupportMessageRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.<SupportTicketResponse>builder()
                .success(true).message("Reply sent").data(supportService.reply(ticketId, request)).build());
    }

    @PutMapping("/{ticketId}/status")
    @PreAuthorize(CommunicationRoles.ANY_AUTHENTICATED)
    public ResponseEntity<ApiResponse<SupportTicketResponse>> changeStatus(@PathVariable("ticketId") UUID ticketId,
                                                                           @Valid @RequestBody SupportStatusRequest request) {
        return ok("Ticket status updated", supportService.changeStatus(ticketId, request));
    }

    private static <T> ResponseEntity<ApiResponse<T>> ok(String message, T data) {
        return ResponseEntity.ok(ApiResponse.<T>builder().success(true).message(message).data(data).build());
    }
}
