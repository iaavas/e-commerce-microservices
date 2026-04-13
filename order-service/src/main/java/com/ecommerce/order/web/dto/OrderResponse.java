package com.ecommerce.order.web.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(Long id, Long userId, String status, BigDecimal totalAmount, Instant createdAt,
		ShippingAddressResponse shippingAddress, List<OrderItemResponse> items) {
}
