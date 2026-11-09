package com.kvs.communication.controller;

import com.kvs.communication.config.CommunicationPerms;
import com.kvs.core.dto.ApiResponse;
import com.kvs.communication.dto.MessageResponse;
import com.kvs.communication.dto.SendMessageRequest;
import com.kvs.communication.service.MessagingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping({"/api/v1/communication/messaging", "/api/v1/communication/messages"})
@RequiredArgsConstructor
public class MessagingController {

    private final MessagingService messagingService;

    // US-09004: Facilitate secure two-way messaging between parents and class teachers
    @PostMapping({"", "/send"})
    @PreAuthorize(CommunicationPerms.MESSAGES_MANAGE)
    public ResponseEntity<ApiResponse<MessageResponse>> sendMessage(
            @Valid @RequestBody SendMessageRequest request) {
        MessageResponse data = messagingService.sendMessage(request);
        return ResponseEntity.ok(ApiResponse.<MessageResponse>builder()
                .success(true)
                .message("Confidential message sent successfully")
                .data(data)
                .build());
    }

    @GetMapping({"", "/conversations"})
    @PreAuthorize(CommunicationPerms.MESSAGES_VIEW)
    public ResponseEntity<ApiResponse<List<MessageResponse>>> getConversations(
            @RequestParam(required = false) String userId) {
        List<MessageResponse> data = messagingService.getConversations(userId);
        return ResponseEntity.ok(ApiResponse.<List<MessageResponse>>builder()
                .success(true)
                .message("Conversations retrieved successfully")
                .data(data)
                .build());
    }

    @GetMapping("/threads/{threadId}")
    @PreAuthorize(CommunicationPerms.MESSAGES_VIEW)
    public ResponseEntity<ApiResponse<List<MessageResponse>>> getThreadMessages(
            @PathVariable UUID threadId) {
        List<MessageResponse> data = messagingService.getThreadMessages(threadId);
        return ResponseEntity.ok(ApiResponse.<List<MessageResponse>>builder()
                .success(true)
                .message("Message conversation thread retrieved successfully")
                .data(data)
                .build());
    }

    @GetMapping("/inbox/{recipientId}")
    @PreAuthorize(CommunicationPerms.MESSAGES_VIEW)
    public ResponseEntity<ApiResponse<List<MessageResponse>>> getUserInbox(
            @PathVariable String recipientId) {
        List<MessageResponse> data = messagingService.getUserInbox(recipientId);
        return ResponseEntity.ok(ApiResponse.<List<MessageResponse>>builder()
                .success(true)
                .message("User inbox messages retrieved successfully")
                .data(data)
                .build());
    }

    @PutMapping("/{messageId}/read")
    @PreAuthorize(CommunicationPerms.MESSAGES_MANAGE)
    public ResponseEntity<ApiResponse<MessageResponse>> markMessageRead(
            @PathVariable UUID messageId) {
        MessageResponse data = messagingService.markMessageRead(messageId);
        return ResponseEntity.ok(ApiResponse.<MessageResponse>builder()
                .success(true)
                .message("Message marked as read")
                .data(data)
                .build());
    }
}
