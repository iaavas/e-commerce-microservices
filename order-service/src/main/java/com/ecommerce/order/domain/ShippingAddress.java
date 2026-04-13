package com.ecommerce.order.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class ShippingAddress {

	@Column(nullable = false, length = 255)
	private String fullName;

	@Column(nullable = false, length = 255)
	private String line1;

	@Column(length = 255)
	private String line2;

	@Column(nullable = false, length = 128)
	private String city;

	@Column(nullable = false, length = 128)
	private String state;

	@Column(nullable = false, length = 32)
	private String postalCode;

	@Column(nullable = false, length = 128)
	private String country;

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getLine1() {
		return line1;
	}

	public void setLine1(String line1) {
		this.line1 = line1;
	}

	public String getLine2() {
		return line2;
	}

	public void setLine2(String line2) {
		this.line2 = line2;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getPostalCode() {
		return postalCode;
	}

	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}
}
