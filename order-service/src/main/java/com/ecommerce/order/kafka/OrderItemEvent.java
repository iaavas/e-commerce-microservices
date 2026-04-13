package com.ecommerce.order.kafka;

public record OrderItemEvent(Long productId, Integer quantity) {
}
