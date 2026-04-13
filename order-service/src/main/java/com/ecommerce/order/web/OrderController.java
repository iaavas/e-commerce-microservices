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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

@Validated
@RestController
@RequestMapping("/api/orders")
@Tag(name = "Orders", description = "Checkout and order tracking endpoints")
public class OrderController {

	private final OrderService orderService;

	public OrderController(OrderService orderService) {
		this.orderService = orderService;
	}

	@PostMapping("/checkout")
	@Operation(summary = "Checkout cart into an order")
	@ApiResponse(responseCode = "200", description = "Order created successfully")
	public OrderResponse checkout(@AuthenticationPrincipal Jwt jwt, @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
			@Valid @RequestBody CheckoutRequest request) {
		return orderService.checkout(userId(jwt), request, authHeader);
	}

	@GetMapping
	@Operation(summary = "List current user's orders")
	@ApiResponse(responseCode = "200", description = "Orders returned")
	public List<OrderResponse> list(@AuthenticationPrincipal Jwt jwt) {
		return orderService.listOrders(userId(jwt));
	}

	@GetMapping("/{orderId}")
	@Operation(summary = "Get current user's order details")
	@ApiResponse(responseCode = "200", description = "Order returned")
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
