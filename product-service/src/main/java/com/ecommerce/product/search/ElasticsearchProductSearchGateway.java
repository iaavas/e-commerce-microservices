package com.ecommerce.product.search;

import java.util.Comparator;
import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.ecommerce.product.domain.Product;
import com.ecommerce.product.repo.ProductRepository;
import com.ecommerce.product.web.dto.CategorySummary;
import com.ecommerce.product.web.dto.ProductResponse;

@Component
@Profile("!test")
public class ElasticsearchProductSearchGateway implements ProductSearchGateway {

	private final ProductSearchRepository productSearchRepository;
	private final ProductRepository productRepository;

	public ElasticsearchProductSearchGateway(ProductSearchRepository productSearchRepository, ProductRepository productRepository) {
		this.productSearchRepository = productSearchRepository;
		this.productRepository = productRepository;
	}

	@Override
	public List<ProductResponse> search(String query, Long categoryId) {
		return productSearchRepository.findByNameContainingOrDescriptionContaining(query, query).stream()
				.filter(doc -> categoryId == null || categoryId.equals(doc.getCategoryId()))
				.sorted(Comparator.comparing(ProductDocument::getName))
				.map(this::toResponse)
				.toList();
	}

	@Override
	public void upsert(Product product) {
		productSearchRepository.save(toDocument(product));
	}

	@Override
	public void deleteById(Long productId) {
		productSearchRepository.deleteById(productId);
	}

	@Override
	public void reindexAll() {
		List<ProductDocument> docs = productRepository.findAll().stream().map(this::toDocument).toList();
		productSearchRepository.saveAll(docs);
	}

	private ProductDocument toDocument(Product product) {
		ProductDocument doc = new ProductDocument();
		doc.setId(product.getId());
		doc.setName(product.getName());
		doc.setDescription(product.getDescription());
		doc.setPrice(product.getPrice());
		doc.setStockQuantity(product.getStockQuantity());
		doc.setCategoryId(product.getCategory().getId());
		doc.setCategoryName(product.getCategory().getName());
		doc.setImageUrl(product.getImageUrl());
		doc.setCreatedAt(product.getCreatedAt());
		return doc;
	}

	private ProductResponse toResponse(ProductDocument doc) {
		CategorySummary category = new CategorySummary(doc.getCategoryId(), doc.getCategoryName());
		return new ProductResponse(doc.getId(), doc.getName(), doc.getDescription(), doc.getPrice(), doc.getStockQuantity(),
				category, doc.getImageUrl(), doc.getCreatedAt());
	}
}
