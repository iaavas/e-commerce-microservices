package com.ecommerce.product.web.dto;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

public record ProductResponse(Long id, String name, String description, BigDecimal price, Integer stockQuantity,
		CategorySummary category, String imageUrl, Instant createdAt) implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;
}
