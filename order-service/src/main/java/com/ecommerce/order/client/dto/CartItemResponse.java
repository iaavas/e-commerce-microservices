package com.ecommerce.order.client.dto;

import java.math.BigDecimal;

public record CartItemResponse(Long productId, String productName, BigDecimal price, Integer quantity, BigDecimal lineTotal) {
}
