package com.ecommerce.order.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public class CheckoutRequest {

	@NotNull
	@Valid
	private ShippingAddressRequest shippingAddress;

	public ShippingAddressRequest getShippingAddress() {
		return shippingAddress;
	}

	public void setShippingAddress(ShippingAddressRequest shippingAddress) {
		this.shippingAddress = shippingAddress;
	}
}
