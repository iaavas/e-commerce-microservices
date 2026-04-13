package com.ecommerce.product.search;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
@Profile("!test")
public class ProductSearchBootstrap {

	private final ProductSearchGateway productSearchGateway;

	public ProductSearchBootstrap(ProductSearchGateway productSearchGateway) {
		this.productSearchGateway = productSearchGateway;
	}

	@PostConstruct
	void reindexOnStartup() {
		productSearchGateway.reindexAll();
	}
}
