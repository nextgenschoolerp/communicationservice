package com.kvs.communication.config;

import com.kvs.core.config.audit.EnableCoreJpaAuditing;
import org.springframework.context.annotation.Configuration;

/** Fills created_at / updated_at / created_by on every BaseAuditEntity (circular dates, notification feed order). */
@Configuration
@EnableCoreJpaAuditing
public class JpaAuditConfig {
}
