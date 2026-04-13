package com.ecommerce.order.web;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.ecommerce.order.service.OrderService;
import com.ecommerce.order.web.dto.CheckoutRequest;
import com.ecommerce.order.web.dto.OrderResponse;

import jakarta.validation.Valid;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

@Validated
@RestController
@RequestMapping("/api/orders")
public class OrderController {

	private final OrderService orderService;

	public OrderController(OrderService orderService) {
		this.orderService = orderService;
	}

	@PostMapping("/checkout")
	public OrderResponse checkout(@AuthenticationPrincipal Jwt jwt, @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
			@Valid @RequestBody CheckoutRequest request) {
		return orderService.checkout(userId(jwt), request, authHeader);
	}

	@GetMapping
	public List<OrderResponse> list(@AuthenticationPrincipal Jwt jwt) {
		return orderService.listOrders(userId(jwt));
	}

	@GetMapping("/{orderId}")
	public OrderResponse get(@AuthenticationPrincipal Jwt jwt, @PathVariable Long orderId) {
		return orderService.getOrder(userId(jwt), orderId);
	}

	private Long userId(Jwt jwt) {
		Object uid = jwt.getClaim("uid");
		if (uid instanceof Number number) {
			return number.longValue();
		}
		throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing uid claim");
	}
}
