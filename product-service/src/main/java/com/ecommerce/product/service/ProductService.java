package com.ecommerce.product.service;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.ecommerce.product.domain.Category;
import com.ecommerce.product.domain.Product;
import com.ecommerce.product.repo.CategoryRepository;
import com.ecommerce.product.repo.ProductRepository;
import com.ecommerce.product.search.ProductSearchGateway;
import com.ecommerce.product.web.dto.CategorySummary;
import com.ecommerce.product.web.dto.ProductRequest;
import com.ecommerce.product.web.dto.ProductResponse;

@Service
public class ProductService {

	private final ProductRepository productRepository;
	private final CategoryRepository categoryRepository;
	private final ProductSearchGateway productSearchGateway;

	public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository,
			ProductSearchGateway productSearchGateway) {
		this.productRepository = productRepository;
		this.categoryRepository = categoryRepository;
		this.productSearchGateway = productSearchGateway;
	}

	@Cacheable(value = "products", key = "'list:' + ( #categoryId == null ? 'all' : #categoryId )")
	@Transactional(readOnly = true)
	public List<ProductResponse> listProducts(Long categoryId, String query) {
		if (query != null && !query.isBlank()) {
			return productSearchGateway.search(query.trim(), categoryId);
		}
		List<Product> products = categoryId == null ? productRepository.findAll()
				: productRepository.findByCategory_Id(categoryId);
		return products.stream().map(this::toResponse).toList();
	}

	@Cacheable(value = "products", key = "'id:' + #id")
	@Transactional(readOnly = true)
	public ProductResponse getProduct(Long id) {
		Product product = productRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
		return toResponse(product);
	}

	@CacheEvict(value = "products", allEntries = true)
	@Transactional
	public ProductResponse createProduct(ProductRequest request) {
		Category category = categoryRepository.findById(request.getCategoryId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category not found"));
		Product product = new Product();
		applyRequest(product, request, category);
		Product saved = productRepository.save(product);
		productSearchGateway.upsert(saved);
		return toResponse(saved);
	}

	@CacheEvict(value = "products", allEntries = true)
	@Transactional
	public ProductResponse updateProduct(Long id, ProductRequest request) {
		Product product = productRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
		Category category = categoryRepository.findById(request.getCategoryId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category not found"));
		applyRequest(product, request, category);
		Product saved = productRepository.save(product);
		productSearchGateway.upsert(saved);
		return toResponse(saved);
	}

	@CacheEvict(value = "products", allEntries = true)
	@Transactional
	public void deleteProduct(Long id) {
		if (!productRepository.existsById(id)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
		}
		productRepository.deleteById(id);
		productSearchGateway.deleteById(id);
	}

	/**
	 * Reduces stock for an order line. Uses optimistic locking: concurrent updates may throw
	 * {@link OptimisticLockingFailureException} — callers can retry.
	 */
	@CacheEvict(value = "products", allEntries = true)
	@Transactional
	public void deductStock(Long productId, int quantity) {
		if (quantity <= 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity must be positive");
		}
		Product product = productRepository.findById(productId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
		int current = product.getStockQuantity();
		if (current < quantity) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient stock");
		}
		product.setStockQuantity(current - quantity);
		Product saved = productRepository.save(product);
		productSearchGateway.upsert(saved);
	}

	private void applyRequest(Product product, ProductRequest request, Category category) {
		product.setName(request.getName().trim());
		product.setDescription(request.getDescription());
		product.setPrice(request.getPrice());
		product.setStockQuantity(request.getStockQuantity());
		product.setCategory(category);
		product.setImageUrl(request.getImageUrl());
	}

	private ProductResponse toResponse(Product product) {
		Category cat = product.getCategory();
		CategorySummary summary = new CategorySummary(cat.getId(), cat.getName());
		return new ProductResponse(product.getId(), product.getName(), product.getDescription(), product.getPrice(),
				product.getStockQuantity(), summary, product.getImageUrl(), product.getCreatedAt());
	}
}
