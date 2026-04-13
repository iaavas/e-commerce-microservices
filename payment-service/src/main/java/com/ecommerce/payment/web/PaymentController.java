package com.ecommerce.payment.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.payment.service.PaymentService;
import com.ecommerce.payment.web.dto.CreatePaymentIntentRequest;
import com.ecommerce.payment.web.dto.PaymentIntentResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/payments")
@Tag(name = "Payments", description = "Stripe payment intent and webhook endpoints")
public class PaymentController {

	private static final String STRIPE_SIGNATURE_HEADER = "Stripe-Signature";
	private final PaymentService paymentService;

	public PaymentController(PaymentService paymentService) {
		this.paymentService = paymentService;
	}

	@PostMapping("/create-intent")
	@Operation(summary = "Create Stripe payment intent", description = "Creates a Stripe payment intent and returns client secret")
	@ApiResponse(responseCode = "200", description = "Payment intent created", content = @Content(schema = @Schema(implementation = PaymentIntentResponse.class)))
	public PaymentIntentResponse createIntent(@Valid @RequestBody CreatePaymentIntentRequest request) {
		return paymentService.createPaymentIntent(request.getOrderId(), request.getAmount(), request.getCurrency());
	}

	@PostMapping("/webhook")
	@Operation(summary = "Handle Stripe webhooks", description = "Processes payment webhook events from Stripe")
	@ApiResponse(responseCode = "200", description = "Webhook processed")
	public ResponseEntity<Void> webhook(@RequestHeader(name = STRIPE_SIGNATURE_HEADER, required = false) String signatureHeader,
			@RequestBody String payload) {
		paymentService.handleWebhook(payload, signatureHeader);
		return ResponseEntity.status(HttpStatus.OK).build();
	}
}
