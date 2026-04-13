package com.ecommerce.inventory.kafka;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderPlacedEvent(Long orderId, Long userId, BigDecimal totalAmount, Instant createdAt, List<OrderItemEvent> items) {
}
