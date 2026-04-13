package com.ecommerce.product.search;

import java.util.List;

import com.ecommerce.product.domain.Product;
import com.ecommerce.product.web.dto.ProductResponse;

public interface ProductSearchGateway {

	List<ProductResponse> search(String query, Long categoryId);

	void upsert(Product product);

	void deleteById(Long productId);

	void reindexAll();
}
