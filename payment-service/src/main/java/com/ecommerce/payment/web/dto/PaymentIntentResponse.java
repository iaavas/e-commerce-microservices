package com.ecommerce.payment.web.dto;

public record PaymentIntentResponse(String clientSecret, String paymentIntentId) {
}
