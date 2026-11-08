package com.kvs.communication.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kvs.core.exception.ForbiddenException;
import com.kvs.core.util.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

/**
 * Ownership and tenant checks for parent-scoped records. authservice tokens carry the caller's id in a {@code userId}
 * claim and their institution in a {@code tenantId} claim; a parent may only read their own records, while staff may
 * read any parent's records of their own tenant. A SUPER_ADMIN may cross tenants. (Same claim-reading approach as
 * studentservice's {@code TenantAccessGuard}.)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ParentAccessGuard {

    /** Staff roles that may read another parent's records (the endpoint's @PreAuthorize list, without the parent roles). */
    private static final Set<String> STAFF_ROLES = Set.of("ROLE_SUPER_ADMIN", "ROLE_ADMIN");

    private final ObjectMapper objectMapper;

    public boolean isSuperAdmin() {
        return SecurityUtils.hasRole("SUPER_ADMIN");
    }

    public boolean isStaff() {
        return SecurityUtils.getCurrentUserRoles().stream().anyMatch(STAFF_ROLES::contains);
    }

    public Optional<String> currentUserId() {
        return claim("userId");
    }

    public Optional<String> currentTenantId() {
        return claim("tenantId");
    }

    /**
     * Allows the parent named in the path, or a staff caller.
     *
     * @throws ForbiddenException otherwise
     */
    public void requireSelfOrStaff(String parentId) {
        if (isStaff()) {
            return;
        }
        boolean self = parentId != null && currentUserId().map(parentId::equalsIgnoreCase).orElse(false);
        if (!self) {
            throw new ForbiddenException("You may only access your own records");
        }
    }

    /**
     * Rejects records belonging to a tenant other than the caller's. A super administrator is exempt; a caller whose
     * token has no tenant claim matches no tenant.
     *
     * @throws ForbiddenException when any of the given tenants is not the caller's
     */
    public void requireOwnTenant(Collection<String> recordTenantIds) {
        if (isSuperAdmin() || recordTenantIds.isEmpty()) {
            return;
        }
        Optional<String> mine = currentTenantId();
        boolean foreign = recordTenantIds.stream()
                .anyMatch(t -> mine.map(m -> !m.equalsIgnoreCase(t)).orElse(true));
        if (foreign) {
            throw new ForbiddenException("You may only access records of your own institution");
        }
    }

    private Optional<String> claim(String name) {
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes)) {
            return Optional.empty();
        }
        HttpServletRequest request = attributes.getRequest();
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            return Optional.empty();
        }
        // The JwtAuthenticationFilter has already validated this token's signature and expiry; only its payload is read here
        String[] parts = header.substring(7).split("\\.");
        if (parts.length < 2) {
            return Optional.empty();
        }
        try {
            JsonNode payload = objectMapper.readTree(new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8));
            return Optional.ofNullable(payload.path(name).textValue()).filter(v -> !v.isBlank());
        } catch (Exception e) {
            log.debug("No {} claim in the bearer token: {}", name, e.getMessage());
            return Optional.empty();
        }
    }
}
