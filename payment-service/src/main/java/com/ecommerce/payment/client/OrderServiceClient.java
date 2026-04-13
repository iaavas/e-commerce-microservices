package com.ecommerce.payment.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class OrderServiceClient {

	private static final String INTERNAL_AUTH_HEADER = "X-Internal-Api-Key";

	private final RestClient restClient;
	private final String internalApiKey;

	public OrderServiceClient(RestClient.Builder builder, @Value("${internal.api-key}") String internalApiKey) {
		this.restClient = builder.baseUrl("http://order-service").build();
		this.internalApiKey = internalApiKey;
	}

	public void confirmOrder(Long orderId) {
		restClient.post()
				.uri("/api/internal/orders/{orderId}/confirm", orderId)
				.header(INTERNAL_AUTH_HEADER, internalApiKey)
				.header(HttpHeaders.CONTENT_TYPE, "application/json")
				.retrieve()
				.toBodilessEntity();
	}
}
