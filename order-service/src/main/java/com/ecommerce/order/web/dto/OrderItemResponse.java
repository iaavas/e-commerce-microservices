package com.ecommerce.order.web.dto;

import java.math.BigDecimal;

public record OrderItemResponse(Long id, Long productId, String productName, Integer quantity, BigDecimal priceAtPurchase) {
}
