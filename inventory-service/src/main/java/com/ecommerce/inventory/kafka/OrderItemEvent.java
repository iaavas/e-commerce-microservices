package com.ecommerce.inventory.kafka;

public record OrderItemEvent(Long productId, Integer quantity) {
}
