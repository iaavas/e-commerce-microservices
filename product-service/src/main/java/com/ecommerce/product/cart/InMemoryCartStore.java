package com.ecommerce.product.cart;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("test")
public class InMemoryCartStore implements CartStore {

	private final Map<Long, Map<Long, Integer>> carts = new ConcurrentHashMap<>();

	@Override
	public void putItem(Long userId, Long productId, int quantity) {
		carts.computeIfAbsent(userId, ignored -> new ConcurrentHashMap<>()).put(productId, quantity);
	}

	@Override
	public void removeItem(Long userId, Long productId) {
		Map<Long, Integer> cart = carts.get(userId);
		if (cart != null) {
			cart.remove(productId);
		}
	}

	@Override
	public Map<Long, Integer> getItems(Long userId) {
		Map<Long, Integer> cart = carts.getOrDefault(userId, Map.of());
		return new LinkedHashMap<>(cart);
	}
}
