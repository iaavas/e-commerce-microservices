package com.ecommerce.product.cart;

import java.util.Map;

public interface CartStore {

	void putItem(Long userId, Long productId, int quantity);

	void removeItem(Long userId, Long productId);

	Map<Long, Integer> getItems(Long userId);
}
