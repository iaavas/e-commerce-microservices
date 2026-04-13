package com.ecommerce.order.client;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.ecommerce.order.client.dto.CartResponse;

@Component
public class ProductServiceClient {

	private final RestClient restClient;

	public ProductServiceClient(RestClient.Builder builder) {
		this.restClient = builder.baseUrl("http://product-service").build();
	}

	public CartResponse getCart(String bearerToken) {
		return restClient.get()
				.uri("/api/cart")
				.header(HttpHeaders.AUTHORIZATION, bearerToken)
				.retrieve()
				.body(new ParameterizedTypeReference<>() {
				});
	}

	public void deductStock(String bearerToken, Long productId, int quantity) {
		restClient.post()
				.uri("/api/admin/products/{id}/deduct-stock", productId)
				.header(HttpHeaders.AUTHORIZATION, bearerToken)
				.body(new DeductStockRequest(quantity))
				.retrieve()
				.toBodilessEntity();
	}

	public void removeCartItem(String bearerToken, Long productId) {
		restClient.delete()
				.uri("/api/cart/remove/{itemId}", productId)
				.header(HttpHeaders.AUTHORIZATION, bearerToken)
				.retrieve()
				.toBodilessEntity();
	}

	record DeductStockRequest(int quantity) {
	}
}
