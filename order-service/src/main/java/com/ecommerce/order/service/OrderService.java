package com.ecommerce.order.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.ecommerce.order.client.ProductServiceClient;
import com.ecommerce.order.client.dto.CartItemResponse;
import com.ecommerce.order.client.dto.CartResponse;
import com.ecommerce.order.domain.Order;
import com.ecommerce.order.domain.OrderItem;
import com.ecommerce.order.domain.OrderStatus;
import com.ecommerce.order.domain.ShippingAddress;
import com.ecommerce.order.repo.OrderRepository;
import com.ecommerce.order.web.dto.CheckoutRequest;
import com.ecommerce.order.web.dto.OrderItemResponse;
import com.ecommerce.order.web.dto.OrderResponse;
import com.ecommerce.order.web.dto.ShippingAddressRequest;
import com.ecommerce.order.web.dto.ShippingAddressResponse;

@Service
public class OrderService {

	private final OrderRepository orderRepository;
	private final ProductServiceClient productServiceClient;

	public OrderService(OrderRepository orderRepository, ProductServiceClient productServiceClient) {
		this.orderRepository = orderRepository;
		this.productServiceClient = productServiceClient;
	}

	@Transactional
	public OrderResponse checkout(Long userId, CheckoutRequest request, String bearerToken) {
		CartResponse cart = productServiceClient.getCart(bearerToken);
		if (cart == null || cart.items() == null || cart.items().isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cart is empty");
		}

		for (CartItemResponse item : cart.items()) {
			productServiceClient.deductStock(bearerToken, item.productId(), item.quantity());
		}

		Order order = new Order();
		order.setUserId(userId);
		order.setStatus(OrderStatus.PENDING);
		order.setTotalAmount(cart.totalPrice());
		order.setShippingAddress(toShippingAddress(request.getShippingAddress()));

		for (CartItemResponse item : cart.items()) {
			OrderItem orderItem = new OrderItem();
			orderItem.setProductId(item.productId());
			orderItem.setProductName(item.productName());
			orderItem.setQuantity(item.quantity());
			orderItem.setPriceAtPurchase(item.price());
			order.addItem(orderItem);
		}

		Order saved = orderRepository.save(order);

		for (CartItemResponse item : cart.items()) {
			productServiceClient.removeCartItem(bearerToken, item.productId());
		}

		return toResponse(saved);
	}

	@Transactional(readOnly = true)
	public List<OrderResponse> listOrders(Long userId) {
		return orderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream().map(this::toResponse).toList();
	}

	@Transactional(readOnly = true)
	public OrderResponse getOrder(Long userId, Long orderId) {
		Order order = orderRepository.findById(orderId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
		if (!order.getUserId().equals(userId)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Order access denied");
		}
		return toResponse(order);
	}

	@Transactional
	public OrderResponse updateStatus(Long orderId, String statusValue) {
		Order order = orderRepository.findById(orderId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
		try {
			OrderStatus status = OrderStatus.valueOf(statusValue.toUpperCase());
			order.setStatus(status);
			return toResponse(orderRepository.save(order));
		} catch (IllegalArgumentException ex) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid order status");
		}
	}

	private ShippingAddress toShippingAddress(ShippingAddressRequest request) {
		ShippingAddress address = new ShippingAddress();
		address.setFullName(request.getFullName());
		address.setLine1(request.getLine1());
		address.setLine2(request.getLine2());
		address.setCity(request.getCity());
		address.setState(request.getState());
		address.setPostalCode(request.getPostalCode());
		address.setCountry(request.getCountry());
		return address;
	}

	private OrderResponse toResponse(Order order) {
		List<OrderItemResponse> items = order.getItems().stream()
				.map(item -> new OrderItemResponse(item.getId(), item.getProductId(), item.getProductName(),
						item.getQuantity(), item.getPriceAtPurchase()))
				.toList();
		ShippingAddress address = order.getShippingAddress();
		ShippingAddressResponse shipping = new ShippingAddressResponse(address.getFullName(), address.getLine1(),
				address.getLine2(), address.getCity(), address.getState(), address.getPostalCode(), address.getCountry());
		return new OrderResponse(order.getId(), order.getUserId(), order.getStatus().name(), order.getTotalAmount(),
				order.getCreatedAt(), shipping, items);
	}
}
