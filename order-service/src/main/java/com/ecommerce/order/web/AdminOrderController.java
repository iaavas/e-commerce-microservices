package com.ecommerce.order.web;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.order.service.OrderService;
import com.ecommerce.order.web.dto.OrderResponse;
import com.ecommerce.order.web.dto.UpdateOrderStatusRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/admin/orders")
@Tag(name = "Admin Orders", description = "Administrative order management")
public class AdminOrderController {

	private final OrderService orderService;

	public AdminOrderController(OrderService orderService) {
		this.orderService = orderService;
	}

	@PutMapping("/{orderId}/status")
	@Operation(summary = "Update order status")
	@ApiResponse(responseCode = "200", description = "Order status updated")
	public OrderResponse updateStatus(@PathVariable Long orderId, @Valid @RequestBody UpdateOrderStatusRequest request) {
		return orderService.updateStatus(orderId, request.getStatus());
	}
}
