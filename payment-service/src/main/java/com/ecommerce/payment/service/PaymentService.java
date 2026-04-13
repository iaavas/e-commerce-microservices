package com.ecommerce.payment.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.ecommerce.payment.client.OrderServiceClient;
import com.ecommerce.payment.stripe.StripeProperties;
import com.ecommerce.payment.web.dto.PaymentIntentResponse;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;

@Service
public class PaymentService {

	private final StripeProperties stripeProperties;
	private final OrderServiceClient orderServiceClient;

	public PaymentService(StripeProperties stripeProperties, OrderServiceClient orderServiceClient) {
		this.stripeProperties = stripeProperties;
		this.orderServiceClient = orderServiceClient;
	}

	public PaymentIntentResponse createPaymentIntent(Long orderId, Long amount, String currency) {
		requireSecretKey();
		Stripe.apiKey = stripeProperties.getSecretKey();
		PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
				.setAmount(amount)
				.setCurrency(currency.toLowerCase())
				.putMetadata("orderId", orderId.toString())
				.build();
		try {
			PaymentIntent intent = PaymentIntent.create(params);
			return new PaymentIntentResponse(intent.getClientSecret(), intent.getId());
		} catch (StripeException ex) {
			throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Failed to create payment intent", ex);
		}
	}

	public void handleWebhook(String payload, String signatureHeader) {
		requireWebhookSecret();
		if (signatureHeader == null || signatureHeader.isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing Stripe signature");
		}
		Event event;
		try {
			event = Webhook.constructEvent(payload, signatureHeader, stripeProperties.getWebhookSecret());
		} catch (SignatureVerificationException ex) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Stripe signature", ex);
		}

		if ("payment_intent.succeeded".equals(event.getType())) {
			PaymentIntent intent = (PaymentIntent) event.getDataObjectDeserializer().getObject().orElse(null);
			if (intent != null && intent.getMetadata() != null && intent.getMetadata().get("orderId") != null) {
				Long orderId = Long.valueOf(intent.getMetadata().get("orderId"));
				orderServiceClient.confirmOrder(orderId);
			}
		}
	}

	private void requireSecretKey() {
		if (stripeProperties.getSecretKey() == null || stripeProperties.getSecretKey().isBlank()) {
			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Stripe secret key not configured");
		}
	}

	private void requireWebhookSecret() {
		if (stripeProperties.getWebhookSecret() == null || stripeProperties.getWebhookSecret().isBlank()) {
			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Stripe webhook secret not configured");
		}
	}
}
