package com.ecommerce.notification.kafka;

public record OrderItemEvent(Long productId, Integer quantity) {
}
