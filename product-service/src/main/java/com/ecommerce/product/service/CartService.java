package com.ecommerce.product.service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.ecommerce.product.cart.CartStore;
import com.ecommerce.product.domain.Product;
import com.ecommerce.product.repo.ProductRepository;
import com.ecommerce.product.web.dto.CartItemResponse;
import com.ecommerce.product.web.dto.CartResponse;

@Service
public class CartService {

	private final CartStore cartStore;
	private final ProductRepository productRepository;

	public CartService(CartStore cartStore, ProductRepository productRepository) {
		this.cartStore = cartStore;
		this.productRepository = productRepository;
	}

	@Transactional(readOnly = true)
	public CartResponse getCart(Long userId) {
		Map<Long, Integer> items = cartStore.getItems(userId);
		List<CartItemResponse> lines = items.entrySet().stream()
				.map(entry -> toLine(entry.getKey(), entry.getValue()))
				.sorted(Comparator.comparing(CartItemResponse::productId))
				.toList();

		BigDecimal totalPrice = lines.stream()
				.map(CartItemResponse::lineTotal)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		return new CartResponse(lines, totalPrice);
	}

	public CartResponse addItem(Long userId, Long productId, int quantity) {
		assertProductExists(productId);
		Map<Long, Integer> items = cartStore.getItems(userId);
		int updatedQuantity = items.getOrDefault(productId, 0) + quantity;
		cartStore.putItem(userId, productId, updatedQuantity);
		return getCart(userId);
	}

	public CartResponse updateItem(Long userId, Long productId, int quantity) {
		assertProductExists(productId);
		cartStore.putItem(userId, productId, quantity);
		return getCart(userId);
	}

	public CartResponse removeItem(Long userId, Long productId) {
		cartStore.removeItem(userId, productId);
		return getCart(userId);
	}

	private void assertProductExists(Long productId) {
		if (!productRepository.existsById(productId)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
		}
	}

	private CartItemResponse toLine(Long productId, Integer quantity) {
		Product product = productRepository.findById(productId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
		BigDecimal lineTotal = product.getPrice().multiply(BigDecimal.valueOf(quantity));
		return new CartItemResponse(product.getId(), product.getName(), product.getPrice(), quantity, lineTotal);
	}
}
