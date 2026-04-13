package com.ecommerce.product.web.dto;

import java.io.Serial;
import java.io.Serializable;

public record CategorySummary(Long id, String name) implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;
}
