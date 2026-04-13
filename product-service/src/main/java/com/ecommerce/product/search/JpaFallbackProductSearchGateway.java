package com.ecommerce.product.search;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.ecommerce.product.domain.Category;
import com.ecommerce.product.domain.Product;
import com.ecommerce.product.repo.ProductRepository;
import com.ecommerce.product.web.dto.CategorySummary;
import com.ecommerce.product.web.dto.ProductResponse;

@Component
@Profile("test")
public class JpaFallbackProductSearchGateway implements ProductSearchGateway {

	private final ProductRepository productRepository;

	public JpaFallbackProductSearchGateway(ProductRepository productRepository) {
		this.productRepository = productRepository;
	}

	@Override
	public List<ProductResponse> search(String query, Long categoryId) {
		String normalized = query.toLowerCase(Locale.ROOT);
		return productRepository.findAll().stream()
				.filter(product -> categoryId == null || categoryId.equals(product.getCategory().getId()))
				.filter(product -> containsIgnoreCase(product.getName(), normalized)
						|| containsIgnoreCase(product.getDescription(), normalized))
				.sorted(Comparator.comparing(Product::getName))
				.map(this::toResponse)
				.toList();
	}

	@Override
	public void upsert(Product product) {
		// no-op for test profile
	}

	@Override
	public void deleteById(Long productId) {
		// no-op for test profile
	}

	@Override
	public void reindexAll() {
		// no-op for test profile
	}

	private boolean containsIgnoreCase(String value, String normalizedTerm) {
		return value != null && value.toLowerCase(Locale.ROOT).contains(normalizedTerm);
	}

	private ProductResponse toResponse(Product product) {
		Category cat = product.getCategory();
		CategorySummary summary = new CategorySummary(cat.getId(), cat.getName());
		return new ProductResponse(product.getId(), product.getName(), product.getDescription(), product.getPrice(),
				product.getStockQuantity(), summary, product.getImageUrl(), product.getCreatedAt());
	}
}
