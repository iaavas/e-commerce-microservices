package com.ecommerce.product.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.product.service.ProductService;
import com.ecommerce.product.web.dto.ProductResponse;

@RestController
@RequestMapping("/api/products")
public class ProductController {

	private final ProductService productService;

	public ProductController(ProductService productService) {
		this.productService = productService;
	}

	@GetMapping
	public List<ProductResponse> list(@RequestParam(name = "category", required = false) Long categoryId,
			@RequestParam(name = "q", required = false) String query) {
		return productService.listProducts(categoryId, query);
	}

	@GetMapping("/{id}")
	public ProductResponse getById(@PathVariable Long id) {
		return productService.getProduct(id);
	}
}
