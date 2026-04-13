package com.ecommerce.order.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.ecommerce.order.domain.OrderStatus;
import com.ecommerce.order.service.OrderService;

@RestController
@RequestMapping("/api/internal/orders")
public class InternalOrderController {

	private static final String INTERNAL_AUTH_HEADER = "X-Internal-Api-Key";

	private final OrderService orderService;
	private final String internalApiKey;

	public InternalOrderController(OrderService orderService, @Value("${internal.api-key}") String internalApiKey) {
		this.orderService = orderService;
		this.internalApiKey = internalApiKey;
	}

	@PostMapping("/{orderId}/confirm")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void confirmOrder(@PathVariable Long orderId, @RequestHeader(name = INTERNAL_AUTH_HEADER, required = false) String apiKey) {
		if (!internalApiKey.equals(apiKey)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid internal API key");
		}
		orderService.updateStatus(orderId, OrderStatus.CONFIRMED.name());
	}
}
