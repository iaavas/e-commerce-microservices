package com.ecommerce.product.cart;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class RedisCartStore implements CartStore {

	private final StringRedisTemplate redisTemplate;

	public RedisCartStore(StringRedisTemplate redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	@Override
	public void putItem(Long userId, Long productId, int quantity) {
		redisTemplate.opsForHash().put(cartKey(userId), productId.toString(), Integer.toString(quantity));
	}

	@Override
	public void removeItem(Long userId, Long productId) {
		redisTemplate.opsForHash().delete(cartKey(userId), productId.toString());
	}

	@Override
	public Map<Long, Integer> getItems(Long userId) {
		Map<Object, Object> rawItems = redisTemplate.opsForHash().entries(cartKey(userId));
		Map<Long, Integer> parsed = new LinkedHashMap<>();
		rawItems.forEach((k, v) -> parsed.put(Long.valueOf(k.toString()), Integer.valueOf(v.toString())));
		return parsed;
	}

	private String cartKey(Long userId) {
		return "cart:user:" + userId;
	}
}
