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

import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderController {

	private final OrderService orderService;

	public AdminOrderController(OrderService orderService) {
		this.orderService = orderService;
	}

	@PutMapping("/{orderId}/status")
	public OrderResponse updateStatus(@PathVariable Long orderId, @Valid @RequestBody UpdateOrderStatusRequest request) {
		return orderService.updateStatus(orderId, request.getStatus());
	}
}
