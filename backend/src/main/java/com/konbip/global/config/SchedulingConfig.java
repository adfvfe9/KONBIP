package com.konbip.global.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * PRODUCT_SYNC_ENABLED=true 일 때만 @Scheduled 작업(상품 하루 1회 동기화)을 켠다.
 */
@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "konbip.product-sync.enabled", havingValue = "true")
public class SchedulingConfig {
}
