package com.ecommerce.product.web;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.ecommerce.product.service.CartService;
import com.ecommerce.product.web.dto.AddCartItemRequest;
import com.ecommerce.product.web.dto.CartResponse;
import com.ecommerce.product.web.dto.UpdateCartItemRequest;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;

@Validated
@RestController
@RequestMapping("/api/cart")
public class CartController {

	private final CartService cartService;

	public CartController(CartService cartService) {
		this.cartService = cartService;
	}

	@PostMapping("/add")
	public CartResponse add(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody AddCartItemRequest request) {
		return cartService.addItem(userId(jwt), request.getProductId(), request.getQuantity());
	}

	@PutMapping("/update/{itemId}")
	public CartResponse update(@AuthenticationPrincipal Jwt jwt, @PathVariable("itemId") Long productId,
			@Valid @RequestBody UpdateCartItemRequest request) {
		return cartService.updateItem(userId(jwt), productId, request.getQuantity());
	}

	@DeleteMapping("/remove/{itemId}")
	public CartResponse remove(@AuthenticationPrincipal Jwt jwt, @PathVariable("itemId") Long productId) {
		return cartService.removeItem(userId(jwt), productId);
	}

	@GetMapping
	public CartResponse get(@AuthenticationPrincipal Jwt jwt) {
		return cartService.getCart(userId(jwt));
	}

	private Long userId(Jwt jwt) {
		Object uid = jwt.getClaim("uid");
		if (uid instanceof Number number) {
			return number.longValue();
		}
		throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing uid claim");
	}
}
